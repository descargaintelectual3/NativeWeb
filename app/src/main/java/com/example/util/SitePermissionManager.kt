package com.example.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import org.json.JSONObject

data class SitePermissionRule(
    val domain: String,
    val micAllowed: Boolean = true,
    val cameraAllowed: Boolean = true,
    val locationAllowed: Boolean = true,
    val filesAllowed: Boolean = true,
    val adBlockEnabled: Boolean = true,
    val autoPlayAudio: Boolean = true
)

object SitePermissionManager {
    private const val PREFS_NAME = "webnative_site_permissions_prefs"
    private const val KEY_GLOBAL_MIC_DEFAULT = "global_mic_default"
    private const val KEY_GLOBAL_CAM_DEFAULT = "global_cam_default"
    private const val KEY_GLOBAL_LOC_DEFAULT = "global_loc_default"
    private const val KEY_GLOBAL_FILES_DEFAULT = "global_files_default"
    private const val KEY_GLOBAL_AUTOPLAY_DEFAULT = "global_autoplay_default"
    private const val KEY_PER_SITE_RULES_JSON = "per_site_rules_json"

    // System Permissions required for full web functionality
    val REQUIRED_SYSTEM_PERMISSIONS = buildList {
        add(Manifest.permission.RECORD_AUDIO)
        add(Manifest.permission.CAMERA)
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.READ_MEDIA_IMAGES)
            add(Manifest.permission.READ_MEDIA_VIDEO)
            add(Manifest.permission.READ_MEDIA_AUDIO)
            add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }.toTypedArray()

    fun extractDomain(url: String): String {
        return try {
            val uri = Uri.parse(url)
            val host = uri.host
            if (!host.isNullOrBlank()) {
                host.lowercase().removePrefix("www.")
            } else {
                url.lowercase().removePrefix("https://").removePrefix("http://").split("/").firstOrNull() ?: "desconocido"
            }
        } catch (e: Exception) {
            "sitio-web"
        }
    }

    // ----------------------------------------------------------------
    // GLOBAL DEFAULT SETTINGS
    // ----------------------------------------------------------------
    fun isGlobalMicEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_GLOBAL_MIC_DEFAULT, true)
    }

    fun setGlobalMicEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_GLOBAL_MIC_DEFAULT, enabled).apply()
    }

    fun isGlobalCamEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_GLOBAL_CAM_DEFAULT, true)
    }

    fun setGlobalCamEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_GLOBAL_CAM_DEFAULT, enabled).apply()
    }

    fun isGlobalLocEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_GLOBAL_LOC_DEFAULT, true)
    }

    fun setGlobalLocEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_GLOBAL_LOC_DEFAULT, enabled).apply()
    }

    fun isGlobalFilesEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_GLOBAL_FILES_DEFAULT, true)
    }

    fun setGlobalFilesEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_GLOBAL_FILES_DEFAULT, enabled).apply()
    }

    fun isGlobalAutoplayEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_GLOBAL_AUTOPLAY_DEFAULT, true)
    }

    fun setGlobalAutoplayEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_GLOBAL_AUTOPLAY_DEFAULT, enabled).apply()
    }

    // ----------------------------------------------------------------
    // PER-SITE PERMISSIONS
    // ----------------------------------------------------------------
    fun getSiteRule(context: Context, domain: String): SitePermissionRule {
        val cleanDomain = domain.lowercase().removePrefix("www.")
        val allRules = getAllSiteRules(context)
        return allRules[cleanDomain] ?: SitePermissionRule(
            domain = cleanDomain,
            micAllowed = isGlobalMicEnabled(context),
            cameraAllowed = isGlobalCamEnabled(context),
            locationAllowed = isGlobalLocEnabled(context),
            filesAllowed = isGlobalFilesEnabled(context),
            adBlockEnabled = true,
            autoPlayAudio = isGlobalAutoplayEnabled(context)
        )
    }

    fun saveSiteRule(context: Context, rule: SitePermissionRule) {
        val cleanDomain = rule.domain.lowercase().removePrefix("www.")
        val allRules = getAllSiteRules(context).toMutableMap()
        allRules[cleanDomain] = rule.copy(domain = cleanDomain)
        saveAllSiteRules(context, allRules)
    }

    fun resetSiteRule(context: Context, domain: String) {
        val cleanDomain = domain.lowercase().removePrefix("www.")
        val allRules = getAllSiteRules(context).toMutableMap()
        allRules.remove(cleanDomain)
        saveAllSiteRules(context, allRules)
    }

    fun getAllSiteRules(context: Context): Map<String, SitePermissionRule> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_PER_SITE_RULES_JSON, null) ?: return emptyMap()
        return try {
            val json = JSONObject(jsonStr)
            val result = mutableMapOf<String, SitePermissionRule>()
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val obj = json.getJSONObject(key)
                result[key] = SitePermissionRule(
                    domain = key,
                    micAllowed = obj.optBoolean("micAllowed", true),
                    cameraAllowed = obj.optBoolean("cameraAllowed", true),
                    locationAllowed = obj.optBoolean("locationAllowed", true),
                    filesAllowed = obj.optBoolean("filesAllowed", true),
                    adBlockEnabled = obj.optBoolean("adBlockEnabled", true),
                    autoPlayAudio = obj.optBoolean("autoPlayAudio", true)
                )
            }
            result
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun saveAllSiteRules(context: Context, rules: Map<String, SitePermissionRule>) {
        val json = JSONObject()
        for ((domain, rule) in rules) {
            val obj = JSONObject().apply {
                put("micAllowed", rule.micAllowed)
                put("cameraAllowed", rule.cameraAllowed)
                put("locationAllowed", rule.locationAllowed)
                put("filesAllowed", rule.filesAllowed)
                put("adBlockEnabled", rule.adBlockEnabled)
                put("autoPlayAudio", rule.autoPlayAudio)
            }
            json.put(domain, obj)
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putString(KEY_PER_SITE_RULES_JSON, json.toString()).apply()
    }

    // ----------------------------------------------------------------
    // QUICK ACCESS PERMISSION CHECKS FOR WEBVIEW
    // ----------------------------------------------------------------
    fun isMicAllowedForOrigin(context: Context, origin: String): Boolean {
        val domain = extractDomain(origin)
        return getSiteRule(context, domain).micAllowed
    }

    fun isCameraAllowedForOrigin(context: Context, origin: String): Boolean {
        val domain = extractDomain(origin)
        return getSiteRule(context, domain).cameraAllowed
    }

    fun isLocationAllowedForOrigin(context: Context, origin: String): Boolean {
        val domain = extractDomain(origin)
        return getSiteRule(context, domain).locationAllowed
    }

    fun isFilesAllowedForOrigin(context: Context, origin: String): Boolean {
        val domain = extractDomain(origin)
        return getSiteRule(context, domain).filesAllowed
    }

    // ----------------------------------------------------------------
    // ANDROID SYSTEM RUNTIME PERMISSION CHECKS
    // ----------------------------------------------------------------
    fun isSystemMicGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isSystemCameraGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isSystemLocationGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isSystemStorageGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}
