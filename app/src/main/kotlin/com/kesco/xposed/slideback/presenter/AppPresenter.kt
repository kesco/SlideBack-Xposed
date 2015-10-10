package com.kesco.xposed.slideback.presenter

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.kesco.adk.rx.AndroidRxPlugin
import com.kesco.xposed.slideback.R
import com.kesco.xposed.slideback.domain.AppInfo
import com.kesco.xposed.slideback.model.AppModel
import com.kesco.xposed.slideback.view.AppView
import com.kesco.xposed.slideback.view.dialog.AboutDialog
import rx.Subscriber

interface AppPresenter {
    fun bindView(view: AppView)
    fun bindModel(model: AppModel)
    fun init(args: Bundle?)
    fun monitorAppState(): Subscriber<AppInfo>
    fun menuSetting(menu: Menu)
    fun menuChoosing(menuItem: MenuItem): Boolean
}

class AppPresenterImpl(val act: AppCompatActivity) : AppPresenter {
    private var appView: AppView? = null
    private var appModel: AppModel? = null

    override fun bindView(view: AppView) {
        appView = view
    }

    override fun bindModel(model: AppModel) {
        appModel = model
    }

    override fun init(args: Bundle?) {
        loadApps()
    }

    override fun monitorAppState(): Subscriber<AppInfo> = appModel!!.changeAppState()

    override fun menuSetting(menu: Menu) {
        var menuItem = menu.findItem(R.id.action_show_system_app)
        menuItem.setChecked(appModel!!.showSystemApp())
    }

    override fun menuChoosing(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_about -> {
            val dialog = AboutDialog()
            dialog.show(act.supportFragmentManager, "about_dialog")
            true
        }
        R.id.action_show_system_app -> {
            item.setChecked(!item.isChecked)
            appModel!!.showSystemApp(item.isChecked)
            loadApps()
            true
        }
        else -> false
    }

    private fun loadApps() {
        appModel!!.data()
                .subscribeOn(AndroidRxPlugin.workerThread)
                .observeOn(AndroidRxPlugin.mainThread)
                .subscribe(appView!!.renderData())
    }
}
