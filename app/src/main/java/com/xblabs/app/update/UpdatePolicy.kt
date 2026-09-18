package com.xblabs.app.update

import com.xblabs.app.network.UpdateResponse

/**
 * Result enum for update policy evaluation.
 */
enum class UpdatePolicyResult {
    UP_TO_DATE,
    OPTIONAL_UPDATE,
    MANDATORY_UPDATE,
    UPDATE_ERROR
}

/**
 * Evaluates whether an update is required, optional, or unnecessary.
 */
object UpdatePolicy {

    fun evaluate(installedVersionCode: Long, serverResponse: UpdateResponse): UpdatePolicyResult {
        if (serverResponse.latestVersionCode <= 0) {
            return UpdatePolicyResult.UPDATE_ERROR
        }

        if (installedVersionCode >= serverResponse.latestVersionCode) {
            return UpdatePolicyResult.UP_TO_DATE
        }

        // Mandatory update logic
        if (installedVersionCode < serverResponse.minimumSupportedVersionCode || serverResponse.forceUpdate) {
            return UpdatePolicyResult.MANDATORY_UPDATE
        }

        return UpdatePolicyResult.OPTIONAL_UPDATE
    }
}
