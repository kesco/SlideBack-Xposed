package com.kesco.xposed.slideback.view

import com.kesco.xposed.slideback.domain.AppInfo
import rx.Subscriber
import rx.Subscription

interface AppView {
    fun renderData(): Subscriber<List<AppInfo>>
}


