package com.kesco.xposed.slideback.presenter

import android.content.Context
import android.os.Bundle
import com.kesco.adk.rx.AndroidRxPlugin
import com.kesco.xposed.slideback.domain.AppInfo
import com.kesco.xposed.slideback.model.AppModel
import com.kesco.xposed.slideback.view.AppView
import rx.Subscriber
import rx.observers.Subscribers

interface AppPresenter {
    fun bindView(view: AppView)
    fun bindModel(model: AppModel)
    fun init(args: Bundle?)
    fun monitorAppState(): Subscriber<AppInfo>
}

class AppPresenterImpl(val ctx: Context) : AppPresenter {
    private var appView: AppView? = null
    private var appModel: AppModel? = null

    override fun bindView(view: AppView) {
        appView = view
    }

    override fun bindModel(model: AppModel) {
        appModel = model
    }

    override fun init(args: Bundle?) {
        appModel!!.data()
                .subscribeOn(AndroidRxPlugin.workerThread)
                .observeOn(AndroidRxPlugin.mainThread)
                .subscribe(appView!!.renderData())
    }

    override fun monitorAppState(): Subscriber<AppInfo> = appModel!!.changeAppState()
}
