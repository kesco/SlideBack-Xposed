package com.kesco.xposed.slideback.model

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.kesco.xposed.slideback.domain.AppInfo
import com.kesco.xposed.slideback.domain.genAppInfo
import com.kesco.xposed.slideback.domain.getInstalledPackagesAppCompat
import rx.Observable
import rx.Subscriber
import rx.observers.Subscribers
import java.util.*

interface AppModel {
    fun data(): Observable<List<AppInfo>>
    fun changeAppState(): Subscriber<AppInfo>
}

class AppModelImpl(val ctx: Context) : AppModel {
    val apps: MutableList<AppInfo> = ArrayList()

    override fun data(): Observable<List<AppInfo>> {
        return Observable.create { subscriber ->
            val packs = ctx.packageManager.getInstalledPackagesAppCompat(PackageManager.GET_ACTIVITIES)
            val slideAppStrList = loadSlideAppsList()
            for (pack in packs) {
                if (pack.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                    apps.add(genAppInfo(ctx, pack, pack.packageName in slideAppStrList))
                }
            }
            subscriber.onNext(apps)
            subscriber.onCompleted()
        }
    }

    override fun changeAppState(): Subscriber<AppInfo> {
        return Subscribers.create { app ->
            val slideAppStrList = loadSlideAppsList()
            if (app.doSlide) {
                slideAppStrList.add(app.pack)
            } else {
                slideAppStrList.remove(app.pack)
            }
            insertSlideAppsList(slideAppStrList)
        }
    }

    private fun loadPref(): SharedPreferences = ctx.getSharedPreferences("app_settings", Context.MODE_WORLD_READABLE)

    private fun loadSlideAppsList(): MutableSet<String> = loadPref().getStringSet("slide_app_list", HashSet<String>())

    private fun insertSlideAppsList(list: Set<String>) {
        val editor = loadPref().edit()
        editor.remove("slide_app_list")
        editor.commit()
        editor.putStringSet("slide_app_list", list)
        editor.commit()
    }
}