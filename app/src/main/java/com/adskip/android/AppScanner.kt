package com.adskip.android

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.os.Build

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

    fun scanInstalledApps(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val apps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledApplications(android.content.pm.PackageManager.ApplicationInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledApplications(0)
        }
        return apps.map { info ->
            AppInfo(
                packageName = info.packageName,
                label = pm.getApplicationLabel(info).toString(),
                icon = info.loadIcon(pm)
            )
        }.sortedBy { it.label.lowercase() }
    }

    fun scanInstalledPackages(context: Context): Set<String> {
        return scanInstalledApps(context).map { it.packageName }.toSet()
    }
}

