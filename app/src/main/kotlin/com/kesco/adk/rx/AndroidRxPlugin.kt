package com.kesco.adk.rx

import android.os.Handler
import android.os.Looper
import rx.Scheduler
import java.util.concurrent.Executors

/**
 * 修改自[RxAndroid](https://github.com/ReactiveX/RxAndroid)
 *
 * 因为我觉得不需要写得那么负责，同时加进自己修改的后台异步线程池Scheduler
 */
public object AndroidRxPlugin {
    private val _mainThreadScheduler = HandlerScheduler(Handler(Looper.getMainLooper()))
    private val _workerThreadScheduler = WorkThreadScheduler(Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors()))

    val mainThread: Scheduler
        get() = _mainThreadScheduler

    val workerThread: Scheduler
        get() = _workerThreadScheduler
}
