package com.xblabs.app.settings

/**
 * Application-wide configuration for update endpoints, Supabase credentials, and network policies.
 */
object AppConfig {
    /**
     * The primary HTTPS update API endpoint.
     */
    const val UPDATE_API_URL = "https://xb-labs-app.vercel.app/api/app/update"

    /**
     * Supabase Cloud / Self-Hosted Configuration.
     * Replace with your own Supabase project credentials.
     */
    var SUPABASE_URL = "https://ldhtoeqyiunqpzpwdozx.supabase.co"
    var SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImxkaHRvZXF5aXVucXB6cHdkb3p4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTA0MzI1OTgsImV4cCI6MjEwNjAwODU5OH0.IxTQf3yHXlk3a8Slo37GMTM-g_feID6ZZZyM_K1iGKo"

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
