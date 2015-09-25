package com.kesco.xposed.slideback.view

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import com.kesco.adk.rx.AndroidRxPlugin
import com.kesco.adk.ui.bindViewById
import com.kesco.xposed.slideback.R
import com.kesco.xposed.slideback.domain.AppInfo
import com.kesco.xposed.slideback.domain.genAppInfo
import com.kesco.xposed.slideback.model.AppModelImpl
import com.kesco.xposed.slideback.presenter.AppPresenter
import com.kesco.xposed.slideback.presenter.AppPresenterImpl
import com.kesco.xposed.slideback.view.adapter.AppAdapter
import com.kesco.xposed.slideback.view.recycleview.LineDividerItemDecoration
import rx.Observable
import rx.Subscriber
import rx.observers.Subscribers

public class MainActivity : AppCompatActivity(), AppView, AppAdapter.OnCheckListener {

    val rvApps: RecyclerView by bindViewById(R.id.rv_apps)
    var adapter: AppAdapter? = null

    var presenter: AppPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvApps.layoutManager = LinearLayoutManager(this)
        rvApps.addItemDecoration(LineDividerItemDecoration(this))
        adapter = AppAdapter(this)
        adapter?.listener = this
        rvApps.adapter = adapter

        presenter = AppPresenterImpl(this)
        val model = AppModelImpl(this)
        presenter?.bindView(this)
        presenter?.bindModel(model)
        presenter?.init(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun renderData(): Subscriber<List<AppInfo>> = Subscribers.create { l -> adapter!!.applist = l }

    override fun onCheckChanged(app: AppInfo, ok: Boolean) {
        Observable.just(app)
                .observeOn(AndroidRxPlugin.workerThread)
                .subscribe(presenter!!.monitorAppState())
    }
}
