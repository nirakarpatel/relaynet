package com.nmiet.app.data.remote

/**
 * Cloudflare R2 object storage configuration for RelayNet Content Library.
 *
 * CRITICAL SECURITY & ARCHITECTURE NOTICE:
 * 1. This is a testing / evaluation configuration using a public read-only R2 development URL.
 * 2. It is NOT the final production hosting setup.
 * 3. Never embed R2 secret keys, AWS secret credentials, or administrative tokens in the Android client.
 * 4. URLs are kept configurable so development and staging endpoints can be swapped cleanly.
 */
object R2ContentConfig {
    const val DEFAULT_PUBLIC_BASE_URL = "https://pub-5cd70efc05b9415d8f5df5e4aed4e50a.r2.dev/Nirmit/"
    const val CATALOG_FILENAME = "catalog.json"
    const val CONFIG_LABEL = "Cloudflare R2 Dev Storage"
    const val CONFIG_NOTE = "Testing endpoint (Public Read-Only, no secret keys)"

    fun getCatalogUrl(baseUrl: String = DEFAULT_PUBLIC_BASE_URL): String {
        val cleanBase = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return "$cleanBase$CATALOG_FILENAME"
    }

    fun getObjectUrl(objectPath: String, baseUrl: String = DEFAULT_PUBLIC_BASE_URL): String {
        val cleanBase = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val cleanPath = objectPath.removePrefix("/")
        return "$cleanBase$cleanPath"
    }
}
