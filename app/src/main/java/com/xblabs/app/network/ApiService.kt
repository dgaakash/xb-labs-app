package com.xblabs.app.network

import com.xblabs.app.settings.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Service handling network API requests for application updates.
 */
class ApiService(
    private val overrideUrl: String? = null
) {
    /**
     * Fetches update metadata from the update endpoint.
     */
    suspend fun fetchUpdateInfo(targetUrl: String = overrideUrl ?: AppConfig.UPDATE_API_URL): UpdateResponse =
        withContext(Dispatchers.IO) {
            val url = URL(targetUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = AppConfig.CONNECT_TIMEOUT_MS
                readTimeout = AppConfig.READ_TIMEOUT_MS
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "XBLabs-Android-Updater")
            }

            try {
                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    throw IllegalStateException("Server returned HTTP error code $responseCode")
                }

                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val content = reader.use { it.readText() }
                return@withContext UpdateResponse.fromJson(content)
            } finally {
                connection.disconnect()
            }
        }
}
