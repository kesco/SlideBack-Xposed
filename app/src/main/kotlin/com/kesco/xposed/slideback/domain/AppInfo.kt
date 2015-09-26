package com.kesco.xposed.slideback.domain

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

data class AppInfo(val name: String, val pack: String, val icon: Drawable, var doSlide: Boolean = false)

fun genAppInfo(ctx: Context, packinfo: PackageInfo, doSlide: Boolean = false): AppInfo {
    val manager = ctx.packageManager
    val appName = packinfo.applicationInfo.loadLabel(manager).toString()
    val packName = packinfo.packageName
    val appIcon = packinfo.applicationInfo.loadIcon(manager)
    return AppInfo(appName, packName, appIcon, doSlide)
}

/**
 * 当手机App装太多应用的时候，getInstalledPackages会溢出
 *
 * https://code.google.com/p/android/issues/attachmentText?id=172058&aid=1720580000000&name=Compat.java&token=ABZ6GAeUf-3eWLM54AjRmGqJKGE5ex9BEw%3A1443249874657#36
 */
fun PackageManager.getInstalledPackagesAppCompat(flags: Int): List<PackageInfo> {
    val list = ArrayList<PackageInfo>()
    var bufferedReader: BufferedReader? = null
    try {
        val process = Runtime.getRuntime().exec("pm list packages")
        bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
        var str: String?
        do {
            str = bufferedReader.readLine()
            if (str == null) break
            val packageName = str.substring(str.indexOf(':') + 1);
            val packageInfo = this.getPackageInfo(packageName, flags)
            list.add(packageInfo)
        } while (str != null)
        process.waitFor();
    } catch(ex: IOException) {
        /* Do nothing */
    } finally {
        try {
            bufferedReader?.close()
        } catch(ex: IOException) {
            /* Do nothing */
        }
    }

    return list
}
