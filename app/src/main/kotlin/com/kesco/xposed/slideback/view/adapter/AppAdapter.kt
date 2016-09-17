package com.kesco.xposed.slideback.view.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import com.kesco.adk.ui.bindViewById
import com.kesco.xposed.slideback.R
import com.kesco.xposed.slideback.domain.AppInfo
import java.util.*

class AppAdapter(val ctx: Context) : RecyclerView.Adapter<AppAdapter.AppVH>() {
    interface OnCheckListener {
        fun onCheckChanged(app: AppInfo, ok: Boolean)
    }

    val layoutInflater: LayoutInflater

    private val _apps = ArrayList<AppInfo>()
    var listener: OnCheckListener? = null

    var applist: List<AppInfo>
        get() = _apps
        set(value) {
            _apps.clear()
            _apps.addAll(value)
            notifyDataSetChanged()
        }

    init {
        layoutInflater = LayoutInflater.from(ctx)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppVH {
        val view = layoutInflater.inflate(R.layout.item_apps, parent, false)
        return AppVH(view)
    }

    override fun onBindViewHolder(holder: AppVH, position: Int) {
        val app = _apps.get(position)
        holder.tvAppName.text = app.name
        holder.ivAppIcon.setImageDrawable(app.icon)
        holder.switchLock.setChecked(app.doSlide)
        holder.switchLock.setOnClickListener { v ->
            app.doSlide = !app.doSlide
            holder.switchLock.setChecked(app.doSlide)
            listener?.onCheckChanged(app, app.doSlide)
        }
    }

    override fun getItemCount(): Int = _apps.size

    class AppVH(view: View) : RecyclerView.ViewHolder(view) {
        val tvAppName: TextView by bindViewById(R.id.tv_app_name)
        val ivAppIcon: ImageView by bindViewById(R.id.iv_app_icon)
        val switchLock: Switch by bindViewById(R.id.switch_lock)
    }
}
