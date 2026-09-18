package com.xblabs.app.update

import android.content.Context
import com.xblabs.app.settings.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.DigestInputStream
import java.security.MessageDigest

/**
 * Progress snapshot emitted during APK download.
 */
data class DownloadProgress(
    val progressFraction: Float,
    val bytesDownloaded: Long,
    val totalBytes: Long
)

/**
 * Direct HTTPS APK Downloader with real-time stream SHA-256 integrity verification.
 */
class ApkDownloader(private val context: Context) {

    /**
     * Downloads an APK from [apkUrl] to app cache and verifies its [expectedSha256].
     * Invokes [onProgress] on each chunk write.
     * Returns the verified local [File].
     */
    suspend fun downloadAndVerify(
        apkUrl: String,
        expectedSha256: String,
        expectedFileSize: Long = 0L,
        onProgress: (DownloadProgress) -> Unit
    ): File = withContext(Dispatchers.IO) {
        if (!apkUrl.startsWith("https://", ignoreCase = true) && !apkUrl.startsWith("http://localhost", ignoreCase = true)) {
            // HTTPS mandatory rule enforced
            throw IllegalArgumentException("Only secure HTTPS URLs are permitted for APK download.")
        }

        val destinationDir = File(context.cacheDir, "apks").apply { mkdirs() }
        val outputFile = File(destinationDir, "update_${System.currentTimeMillis()}.apk")

        // Clean up any old files in cache directory
        destinationDir.listFiles()?.forEach { file ->
            if (file.name != outputFile.name) {
                file.delete()
            }
        }

        val url = URL(apkUrl)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = AppConfig.CONNECT_TIMEOUT_MS
            readTimeout = AppConfig.READ_TIMEOUT_MS
            setRequestProperty("Accept-Encoding", "identity")
            setRequestProperty("User-Agent", "XBLabs-Android-Updater")
        }

        val digest = MessageDigest.getInstance("SHA-256")

        try {
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                throw IllegalStateException("Failed to download APK. Server returned HTTP $responseCode")
            }

            val totalLength = connection.contentLengthLong.let { if (it > 0) it else expectedFileSize }
            var bytesDownloaded = 0L

            val rawInputStream: InputStream = connection.inputStream
            val digestInputStream = DigestInputStream(rawInputStream, digest)
            val fileOutputStream = FileOutputStream(outputFile)

            val buffer = ByteArray(AppConfig.DOWNLOAD_BUFFER_SIZE)
            var readBytes: Int

            fileOutputStream.use { out ->
                digestInputStream.use { input ->
                    while (input.read(buffer).also { readBytes = it } != -1) {
                        out.write(buffer, 0, readBytes)
                        bytesDownloaded += readBytes

                        val progressFraction = if (totalLength > 0) {
                            (bytesDownloaded.toFloat() / totalLength.toFloat()).coerceIn(0f, 1f)
                        } else {
                            0f
                        }

                        onProgress(
                            DownloadProgress(
                                progressFraction = progressFraction,
                                bytesDownloaded = bytesDownloaded,
                                totalBytes = totalLength
                            )
                        )
                    }
                }
            }

            // Verify file size if totalLength is known
            if (totalLength > 0 && bytesDownloaded != totalLength) {
                outputFile.delete()
                throw IllegalStateException("Downloaded APK size ($bytesDownloaded bytes) does not match expected size ($totalLength bytes).")
            }

            // Calculate computed SHA-256 hex string
            val calculatedHash = digest.digest().joinToString("") { "%02x".format(it) }
            val cleanExpectedHash = expectedSha256.trim().lowercase()

            if (cleanExpectedHash.isNotEmpty() && !calculatedHash.equals(cleanExpectedHash, ignoreCase = true)) {
                // Reject corrupted/mismatched file
                outputFile.delete()
                throw SecurityException(
                    "SHA-256 hash mismatch! APK integrity check failed.\nExpected: $cleanExpectedHash\nActual:   $calculatedHash"
                )
            }

            return@withContext outputFile
        } catch (e: Exception) {
            if (outputFile.exists()) {
                outputFile.delete()
            }
            throw e
        } finally {
            connection.disconnect()
        }
    }
}
