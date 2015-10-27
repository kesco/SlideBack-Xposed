package com.kesco.adk.rx

import android.os.Process
import rx.Scheduler
import rx.Subscription
import rx.functions.Action0
import rx.internal.schedulers.ScheduledAction
import rx.plugins.RxJavaPlugins
import rx.subscriptions.CompositeSubscription
import rx.subscriptions.MultipleAssignmentSubscription
import rx.subscriptions.Subscriptions
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

public class WorkThreadScheduler(val executor: ScheduledExecutorService) : Scheduler() {

    override fun createWorker(): Scheduler.Worker = ThreadWorker(executor)

    class ThreadWorker(val executor: ScheduledExecutorService) : Scheduler.Worker(), Runnable {
        val tasks: CompositeSubscription = CompositeSubscription()
        val queue: ConcurrentLinkedQueue<ScheduledAction> = ConcurrentLinkedQueue()
        val count: AtomicInteger = AtomicInteger()

        override fun unsubscribe() = tasks.unsubscribe()

        override fun isUnsubscribed(): Boolean = tasks.isUnsubscribed()

        override fun schedule(action: Action0): Subscription {
            if (isUnsubscribed) {
                return Subscriptions.unsubscribed()
            }
            val sa = ScheduledAction(action, tasks)
            tasks.add(sa)
            queue.offer(sa)
            if (count.getAndIncrement() == 0) {
                try {
                    executor.execute(this)
                } catch(e: RejectedExecutionException) {
                    tasks.remove(sa)
                    count.decrementAndGet()
                    RxJavaPlugins.getInstance().getErrorHandler().handleError(e)
                    throw e
                }
            }
            return sa
        }

        override fun schedule(action: Action0, delayTime: Long, unit: TimeUnit): Subscription {
            if (delayTime <= 0) return schedule(action)
            if (isUnsubscribed) return Subscriptions.unsubscribed()

            val first = MultipleAssignmentSubscription()
            val mas = MultipleAssignmentSubscription()
            mas.set(first)
            tasks.add(mas)
            val removeMas = Subscriptions.create { tasks.remove(mas) }

            val ea = ScheduledAction {
                if (!mas.isUnsubscribed()) {
                    val s2 = schedule(action)
                    mas.set(s2)
                    (s2 as? ScheduledAction)?.add(removeMas)
                }
            }
            first.set(ea);

            try {
                val f = executor.schedule(ea, delayTime, unit)
                ea.add(f)
            } catch (t: RejectedExecutionException) {
                RxJavaPlugins.getInstance().getErrorHandler().handleError(t)
                throw t
            }

            return removeMas;
        }

        override fun run() {
            // TODO: 统一在线程池内设置线程优先级
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
            do {
                val sa = queue.poll()
                if (!sa.isUnsubscribed()) {
                    sa.run()
                }
            } while (count.decrementAndGet() > 0)
        }
    }
}
