package com.example.adskip

import android.content.Context

class RuleStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun setInstalledPackages(packages: Set<String>) {
        prefs.edit().putStringSet(KEY_INSTALLED_PACKAGES, packages).apply()
    }

    fun getInstalledPackages(): Set<String> {
        return prefs.getStringSet(KEY_INSTALLED_PACKAGES, emptySet()) ?: emptySet()
    }

    fun getPackageKeywordsRaw(packageName: String): String {
        return prefs.getString("$KEY_PACKAGE_PREFIX$packageName", "") ?: ""
    }

    fun setPackageKeywordsRaw(packageName: String, raw: String) {
        val key = "$KEY_PACKAGE_PREFIX$packageName"
        val editor = prefs.edit()
        if (raw.isBlank()) {
            editor.remove(key)
        } else {
            editor.putString(key, raw.trim())
        }
        editor.apply()
    }

    fun getPackageKeywordList(packageName: String): List<String> {
        val raw = getPackageKeywordsRaw(packageName)
        return raw.split(Regex("[,，\\n]"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    companion object {
        private const val PREFS_NAME = "adskip_rules"
        private const val KEY_INSTALLED_PACKAGES = "installed_packages"
        private const val KEY_PACKAGE_PREFIX = "pkg_keywords_"
    }
}
