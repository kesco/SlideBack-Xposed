package com.kesco.xposed.slideback.view.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.kesco.xposed.slideback.R


class AboutDialog : DialogFragment(), DialogInterface.OnClickListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.action_about)
        builder.setAdapter(DialogAdapter(), this)
        return builder.create()
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            0 -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://github.com/kesco/SlideBack-Xposed"))
                context.startActivity(intent)
            }
            1 -> {
                val act = context as? FragmentActivity
                val manager = act?.supportFragmentManager
                val sourceUsedDialog = SourceUsedDialog()
                if (manager != null) sourceUsedDialog.show(manager, "dialog_source_used")
            }
        }
        dialog.dismiss()
    }

    inner class DialogAdapter : BaseAdapter() {
        val inflater = LayoutInflater.from(activity)

        override fun getCount(): Int = 2

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
            val holder = inflater.inflate(R.layout.item_about_dialog, parent, false)
            val tvTitle = holder.findViewById(R.id.tv_title) as TextView
            val tvContent = holder.findViewById(R.id.tv_content) as TextView
            when (position) {
                0 -> {
                    tvTitle.setText(R.string.app_version)
                    tvContent.setText(R.string.app_source)
                }
                1 -> {
                    tvTitle.setText(R.string.open_source_license)
                    tvContent.setText(R.string.open_source_used)
                }
                else -> throw UnsupportedOperationException()
            }
            return holder
        }

        override fun getItem(position: Int): Any? = null

        override fun getItemId(position: Int): Long = position.toLong()
    }
}

