package com.xblabs.app.update

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.xblabs.app.network.ApiService
import com.xblabs.app.network.UpdateResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel orchestrating update checking, downloading, verifying, installing, and re-checking.
 */
class UpdateManager(
    private val context: Context,
    private val apiService: ApiService = ApiService(),
    private val versionChecker: VersionChecker = VersionChecker(context),
    private val downloader: ApkDownloader = ApkDownloader(context),
    private val installer: ApkInstaller = ApkInstaller(context)
) : ViewModel() {

    private val _state = MutableStateFlow<UpdateState>(UpdateState.Checking)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    private var currentUpdateResponse: UpdateResponse? = null
    private var downloadedApkFile: File? = null

    init {
        checkForUpdates()
    }

    /**
     * Checks the update API for new version information and evaluates update requirements.
     */
    fun checkForUpdates(customUrl: String? = null) {
        viewModelScope.launch {
            _state.value = UpdateState.Checking
            val installed = versionChecker.getInstalledVersion()

            try {
                val response = apiService.fetchUpdateInfo(customUrl ?: com.xblabs.app.settings.AppConfig.UPDATE_API_URL)
                currentUpdateResponse = response

                val evaluation = UpdatePolicy.evaluate(installed.versionCode, response)
                when (evaluation) {
                    UpdatePolicyResult.UP_TO_DATE -> {
                        _state.value = UpdateState.UpToDate(
                            installedVersionName = installed.versionName,
                            installedVersionCode = installed.versionCode
                        )
                    }
                    UpdatePolicyResult.MANDATORY_UPDATE, UpdatePolicyResult.OPTIONAL_UPDATE -> {
                        val requiresPerm = !installer.canInstallPackages()
                        _state.value = UpdateState.UpdateRequired(
                            response = response,
                            installedVersionCode = installed.versionCode,
                            installedVersionName = installed.versionName,
                            requiresPermission = requiresPerm
                        )
                    }
                    UpdatePolicyResult.UPDATE_ERROR -> {
                        _state.value = UpdateState.UpdateFailed(
                            error = "Received invalid update response from server.",
                            canRetry = true
                        )
                    }
                }
            } catch (e: Exception) {
                // Fail-closed strategy: If update server fails and we suspect a required version, stay blocked or show network error
                _state.value = UpdateState.NetworkError(
                    message = e.localizedMessage ?: "Unable to connect to update server."
                )
            }
        }
    }

    /**
     * Initiates the in-app APK download, SHA-256 integrity verification, and package installer flow.
     */
    fun startUpdateDownload() {
        val response = currentUpdateResponse ?: run {
            _state.value = UpdateState.UpdateFailed("No update payload available.", canRetry = true)
            return
        }

        viewModelScope.launch {
            try {
                _state.value = UpdateState.Downloading(
                    progressFraction = 0f,
                    downloadedBytes = 0L,
                    totalBytes = response.fileSize,
                    versionName = response.latestVersionName
                )

                val apkFile = downloader.downloadAndVerify(
                    apkUrl = response.apkUrl,
                    expectedSha256 = response.sha256,
                    expectedFileSize = response.fileSize,
                    onProgress = { progress ->
                        _state.value = UpdateState.Downloading(
                            progressFraction = progress.progressFraction,
                            downloadedBytes = progress.bytesDownloaded,
                            totalBytes = progress.totalBytes,
                            versionName = response.latestVersionName
                        )
                    }
                )

                _state.value = UpdateState.Verifying
                downloadedApkFile = apkFile

                triggerInstallation(apkFile)
            } catch (e: SecurityException) {
                _state.value = UpdateState.UpdateFailed(
                    error = e.localizedMessage ?: "APK SHA-256 integrity verification failed.",
                    canRetry = true
                )
            } catch (e: Exception) {
                _state.value = UpdateState.UpdateFailed(
                    error = e.localizedMessage ?: "Download failed. Please check your connection.",
                    canRetry = true
                )
            }
        }
    }

    /**
     * Triggers the Android package installation flow.
     */
    fun triggerInstallation(apkFile: File? = downloadedApkFile) {
        val file = apkFile ?: run {
            _state.value = UpdateState.UpdateFailed("Update APK file missing.", canRetry = true)
            return
        }

        if (!installer.canInstallPackages()) {
            val response = currentUpdateResponse
            val installed = versionChecker.getInstalledVersion()
            if (response != null) {
                _state.value = UpdateState.UpdateRequired(
                    response = response,
                    installedVersionCode = installed.versionCode,
                    installedVersionName = installed.versionName,
                    requiresPermission = true
                )
            }
            context.startActivity(installer.getManageUnknownSourcesIntent())
            return
        }

        _state.value = UpdateState.Installing(file)
        val launched = installer.installApk(file)
        if (!launched) {
            _state.value = UpdateState.UpdateFailed("Failed to launch package installer.", canRetry = true)
        }
    }

    /**
     * Called on Activity resume to verify if the installation completed successfully.
     */
    fun onAppResume() {
        val currentState = _state.value
        if (currentState is UpdateState.UpToDate) {
            return
        }

        val installed = versionChecker.getInstalledVersion()
        val response = currentUpdateResponse

        if (response != null && installed.versionCode >= response.minimumSupportedVersionCode) {
            _state.value = UpdateState.UpToDate(
                installedVersionName = installed.versionName,
                installedVersionCode = installed.versionCode
            )
        } else if (currentState is UpdateState.Installing || currentState is UpdateState.Downloading) {
            // User returned from installer without completing or cancelled
            if (response != null) {
                _state.value = UpdateState.UpdateRequired(
                    response = response,
                    installedVersionCode = installed.versionCode,
                    installedVersionName = installed.versionName,
                    requiresPermission = !installer.canInstallPackages()
                )
            } else {
                checkForUpdates()
            }
        }
    }

    /**
     * Factory helper for creating UpdateManager with Context.
     */
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UpdateManager(context.applicationContext) as T
        }
    }
}
