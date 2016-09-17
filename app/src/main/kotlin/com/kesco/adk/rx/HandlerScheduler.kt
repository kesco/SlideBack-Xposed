package com.kesco.adk.rx

import android.os.Handler
import rx.Scheduler
import rx.Subscription
import rx.functions.Action0
import rx.internal.schedulers.ScheduledAction
import rx.subscriptions.CompositeSubscription
import rx.subscriptions.Subscriptions
import java.util.concurrent.TimeUnit

public class HandlerScheduler(val handler: Handler) : Scheduler() {

    override fun createWorker(): Scheduler.Worker = HandlerWorker(handler)

    class HandlerWorker(val handler: Handler) : Scheduler.Worker() {

        private val subscriptions = CompositeSubscription()

        override fun unsubscribe() = subscriptions.unsubscribe()

        override fun isUnsubscribed(): Boolean = subscriptions.isUnsubscribed

        override fun schedule(action: Action0): Subscription = schedule(action, 0L, TimeUnit.MILLISECONDS)

        override fun schedule(action: Action0, delayTime: Long, unit: TimeUnit): Subscription {
            if (subscriptions.isUnsubscribed) {
                return Subscriptions.unsubscribed()
            }
            val scheduledAction = ScheduledAction(action)
            scheduledAction.addParent(subscriptions)
            subscriptions.add(scheduledAction)
            handler.postDelayed(scheduledAction, unit.toMillis(delayTime))
            scheduledAction.add(Subscriptions.create { handler.removeCallbacks(scheduledAction) })

            return scheduledAction
        }
    }
}
