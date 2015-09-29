package com.kesco.xposed.slideback.domain

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

class AppInfo(val name: String, val pack: String, val icon: Drawable?, var doSlide: Boolean = false) {
    private val _avaliableActivities: MutableSet<String> = HashSet()
    private val _disableActivitiies: MutableSet<String> = HashSet()

    var avaliableSlideActivities: Set<String>
        get() = _avaliableActivities
        set(values) {
            _avaliableActivities.addAll(values)
        }

    var disableSlideActivities: Set<String>
        get() = _disableActivitiies
        set(values) {
            _disableActivitiies.addAll(values)
        }

    fun makeActivityDoSlide(act: String) {
        if (_disableActivitiies.contains(act)) throw IllegalArgumentException("Has no $act")
        _avaliableActivities.add(act)
        _disableActivitiies.remove(act)
    }

    fun makeActivityDoNotSlide(act: String) {
        if (_avaliableActivities.contains(act)) throw IllegalArgumentException("Has no $act")
        _disableActivitiies.add(act)
        _avaliableActivities.remove(act)
    }

}

fun genAppInfo(ctx: Context, packinfo: PackageInfo, doSlide: Boolean = false): AppInfo {
    val manager = ctx.packageManager
    val appName = packinfo.applicationInfo.loadLabel(manager).toString()
    val packName = packinfo.packageName
    val appIcon = packinfo.applicationInfo.loadIcon(manager)
    val app = AppInfo(appName, packName, appIcon, doSlide)
    if (packinfo.activities != null && packinfo.activities.isNotEmpty()) {
        val activities = HashSet<String>()
        for (act in packinfo.activities) {
            activities.add(act.name)
        }
        app.avaliableSlideActivities = activities
    }
    return app
}

fun saveAppInfo(ctx: Context, app: AppInfo) {
    val prefName = app.pack.replace(".", "_")
    val pref = ctx.getSharedPreferences(prefName, Context.MODE_WORLD_READABLE)
    val editor = pref.edit()
    editor.putString("name", app.name)
    editor.putString("pack", app.pack)
    editor.putBoolean("do_slide", app.doSlide)
    editor.putStringSet("avaliable_slide_activities", app.avaliableSlideActivities)
    editor.putStringSet("disable_slide_activities", app.disableSlideActivities)

    editor.commit()
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
