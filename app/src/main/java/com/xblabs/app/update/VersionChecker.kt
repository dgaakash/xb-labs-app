package com.xblabs.app.update

import android.content.Context
import androidx.core.content.pm.PackageInfoCompat

/**
 * Data holder for installed version details.
 */
data class InstalledVersionInfo(
    val versionCode: Long,
    val versionName: String
)

/**
 * Utility for reading the currently installed application version.
 */
class VersionChecker(private val context: Context) {

    fun getInstalledVersion(): InstalledVersionInfo {
        return try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            val versionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
            val versionName = packageInfo.versionName ?: "0.0.0"
            InstalledVersionInfo(versionCode, versionName)
        } catch (e: Exception) {
            InstalledVersionInfo(1L, "1.0.0")
        }
    }
}
