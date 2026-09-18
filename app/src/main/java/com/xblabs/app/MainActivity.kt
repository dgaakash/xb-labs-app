package com.xblabs.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xblabs.app.ui.*
import com.xblabs.app.update.UpdateManager
import com.xblabs.app.update.UpdateState

class MainActivity : ComponentActivity() {

    private val updateManager: UpdateManager by viewModels {
        UpdateManager.Factory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val state by updateManager.state.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0F172A)
                ) {
                    when (val current = state) {
                        is UpdateState.Checking -> {
                            LoadingScreen(message = "Checking for updates…")
                        }

                        is UpdateState.UpToDate -> {
                            // Welcome screen is rendered ONLY when update state is verified UpToDate
                            WelcomeScreen(
                                versionName = current.installedVersionName,
                                versionCode = current.installedVersionCode
                            )
                        }

                        is UpdateState.UpdateRequired -> {
                            UpdateRequiredScreen(
                                updateResponse = current.response,
                                installedVersionName = current.installedVersionName,
                                requiresPermission = current.requiresPermission,
                                onUpdateClick = { updateManager.startUpdateDownload() }
                            )
                        }

                        is UpdateState.Downloading -> {
                            DownloadScreen(
                                progressFraction = current.progressFraction,
                                downloadedBytes = current.downloadedBytes,
                                totalBytes = current.totalBytes,
                                versionName = current.versionName
                            )
                        }

                        is UpdateState.Verifying -> {
                            LoadingScreen(message = "Verifying APK integrity (SHA-256)…")
                        }

                        is UpdateState.Installing -> {
                            LoadingScreen(message = "Launching package installer…")
                        }

                        is UpdateState.UpdateFailed -> {
                            ErrorScreen(
                                title = "UPDATE FAILED",
                                errorMessage = current.error,
                                onRetryClick = { updateManager.startUpdateDownload() }
                            )
                        }

                        is UpdateState.NetworkError -> {
                            ErrorScreen(
                                title = "CONNECTION ERROR",
                                errorMessage = current.message,
                                onRetryClick = { updateManager.checkForUpdates() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check version on app resume to verify if installer completed
        updateManager.onAppResume()
    }
}

@Composable
fun LoadingScreen(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = Color(0xFF38BDF8),
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF94A3B8)
            )
        }
    }
}
