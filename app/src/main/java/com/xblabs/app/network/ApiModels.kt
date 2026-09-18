package com.xblabs.app.network

import org.json.JSONObject

/**
 * Data class representing the server update payload response.
 */
data class UpdateResponse(
    val latestVersionCode: Long,
    val latestVersionName: String,
    val minimumSupportedVersionCode: Long,
    val forceUpdate: Boolean,
    val apkUrl: String,
    val sha256: String,
    val fileSize: Long,
    val releaseNotes: List<String>,
    val message: String
) {
    companion object {
        /**
         * Parses a JSON string into an UpdateResponse instance.
         */
        fun fromJson(jsonStr: String): UpdateResponse {
            val obj = JSONObject(jsonStr)
            val notes = mutableListOf<String>()
            val notesArray = obj.optJSONArray("releaseNotes")
            if (notesArray != null) {
                for (i in 0 until notesArray.length()) {
                    notes.add(notesArray.getString(i))
                }
            }
            return UpdateResponse(
                latestVersionCode = obj.optLong("latestVersionCode", 0L),
                latestVersionName = obj.optString("latestVersionName", ""),
                minimumSupportedVersionCode = obj.optLong("minimumSupportedVersionCode", 0L),
                forceUpdate = obj.optBoolean("forceUpdate", false),
                apkUrl = obj.optString("apkUrl", ""),
                sha256 = obj.optString("sha256", ""),
                fileSize = obj.optLong("fileSize", 0L),
                releaseNotes = notes,
                message = obj.optString("message", "")
            )
        }
    }
}
