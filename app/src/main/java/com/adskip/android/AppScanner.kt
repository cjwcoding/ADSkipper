package com.adskip.android

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo

data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: android.graphics.drawable.Drawable
)

object AppScanner {
    fun scanLauncherApps(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val activities = pm.queryIntentActivities(intent, 0)
        val byPackage = LinkedHashMap<String, ResolveInfo>()
        for (info in activities) {
            val pkg = info.activityInfo.packageName
            if (!byPackage.containsKey(pkg)) {
                byPackage[pkg] = info
            }
        }
        val apps = byPackage.values.map { info ->
            AppInfo(
                packageName = info.activityInfo.packageName,
                label = info.loadLabel(pm).toString(),
                icon = info.loadIcon(pm)
            )
        }
        return apps.sortedBy { it.label.lowercase() }
    }

    fun scanLauncherPackages(context: Context): Set<String> {
        return scanLauncherApps(context).map { it.packageName }.toSet()
    }
}

