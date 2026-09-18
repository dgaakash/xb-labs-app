package com.xblabs.app.update

import com.xblabs.app.network.UpdateResponse
import java.io.File

/**
 * Sealed interface representing all states in the update lifecycle.
 */
sealed interface UpdateState {

    /**
     * Initial startup state while inspecting current version and reaching API server.
     */
    object Checking : UpdateState

    /**
     * Application installed version is equal to or higher than latest required version.
     */
    data class UpToDate(
        val installedVersionName: String,
        val installedVersionCode: Long
    ) : UpdateState

    /**
     * Mandatory update is required before the app can be used.
     */
    data class UpdateRequired(
        val response: UpdateResponse,
        val installedVersionCode: Long,
        val installedVersionName: String,
        val requiresPermission: Boolean = false
    ) : UpdateState

    /**
     * APK download is currently in progress.
     */
    data class Downloading(
        val progressFraction: Float,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val versionName: String
    ) : UpdateState

    /**
     * Downloaded APK file is being verified via SHA-256 hash.
     */
    object Verifying : UpdateState

    /**
     * Android PackageInstaller flow has been launched.
     */
    data class Installing(
        val apkFile: File
    ) : UpdateState

    /**
     * Download, verification, or installation launch failed.
     */
    data class UpdateFailed(
        val error: String,
        val canRetry: Boolean = true
    ) : UpdateState

    /**
     * Server or network is unreachable.
     */
    data class NetworkError(
        val message: String
    ) : UpdateState
}
