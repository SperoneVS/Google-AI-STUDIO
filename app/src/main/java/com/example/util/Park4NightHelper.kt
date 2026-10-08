package com.example.util

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri

/**
 * Manages private on-device Park4night integration.
 * User credentials and vehicle profile remain strictly confidential on-device
 * and are NEVER shared or transmitted to other users or public databases.
 */
object Park4NightHelper {

    private const val PREFS_NAME = "private_camper_vault_prefs"
    private const val KEY_P4N_EMAIL = "p4n_user_email"
    private const val KEY_P4N_ACTIVE = "p4n_is_active"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun initPrivateAccount(context: Context) {
        val prefs = getPrefs(context)
        if (!prefs.contains(KEY_P4N_EMAIL)) {
            // Securely initialize private profile on device only
            prefs.edit()
                .putString(KEY_P4N_EMAIL, "csperone@gmx.net")
                .putBoolean(KEY_P4N_ACTIVE, true)
                .apply()
        }
    }

    fun getConnectedEmail(context: Context): String {
        return getPrefs(context).getString(KEY_P4N_EMAIL, "csperone@gmx.net") ?: "csperone@gmx.net"
    }

    fun isConnected(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_P4N_ACTIVE, true)
    }

    /**
     * Launch spot directly in park4night app or web view
     */
    fun openSpotInPark4Night(
        context: Context,
        latitude: Double,
        longitude: Double,
        spotName: String = ""
    ) {
        try {
            val url = "https://park4night.com/en/search?lat=$latitude&lng=$longitude"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallback = Intent(Intent.ACTION_VIEW, Uri.parse("https://park4night.com")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallback)
        }
    }

    /**
     * Launch Park4night portal
     */
    fun openPark4NightPortal(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://park4night.com")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}
