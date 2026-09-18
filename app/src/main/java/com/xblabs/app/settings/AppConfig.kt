package com.xblabs.app.settings

/**
 * Application-wide configuration for update endpoints and network policies.
 */
object AppConfig {
    /**
     * The primary HTTPS update API endpoint.
     * Can be replaced with real backend URL in production.
     */
    const val UPDATE_API_URL = "https://xb-labs-app.vercel.app/api/app/update"

    /**
     * Connection timeout in milliseconds.
     */
    const val CONNECT_TIMEOUT_MS = 15000

    /**
     * Read timeout in milliseconds.
     */
    const val READ_TIMEOUT_MS = 30000

    /**
     * Buffer size for file downloads.
     */
    const val DOWNLOAD_BUFFER_SIZE = 8192
}
