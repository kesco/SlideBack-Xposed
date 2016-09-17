package com.kesco.adk.ui

import android.app.Activity
import android.app.Dialog
import android.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.View
import android.support.v4.app.Fragment as SupportFragment

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * 用于声明View的委托方法
 *
 * @author Kesco Lin
 */

public fun <T : View> Activity.bindViewById(id: Int): ReadOnlyProperty<Any, T> = ViewDelegate(id)
public fun <T : View> View.bindViewById(id: Int): ReadOnlyProperty<Any, T> = ViewDelegate(id)
public fun <T : View> Dialog.bindViewById(id: Int): ReadOnlyProperty<Any, T> = ViewDelegate(id)
public fun <T : View> Fragment.bindViewById(id: Int): ReadOnlyProperty<Any, T> = ViewDelegate(id)
public fun <T : View> SupportFragment.bindViewById(id: Int): ReadOnlyProperty<Any, T> = ViewDelegate(id)
public fun <T : View> RecyclerView.ViewHolder.bindViewById(id: Int): ReadOnlyProperty<Any, T> = ViewDelegate(id)

class ViewDelegate<T : View>(val id: Int) : ReadOnlyProperty<Any, T> {
    private var view: T? = null

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        if (view == null) {
            view = findViewById(thisRef, id)
            if (view == null) {
                throw IllegalArgumentException("Has no view bound to the $id ID.")
            }
        }
        return view as T
    }
}

private fun <T : View> findViewById(thisRef: Any, id: Int): T? {
    @Suppress("UNCHECKED_CAST")
    return when (thisRef) {
        is View -> thisRef.findViewById(id)
        is Activity -> thisRef.findViewById(id)
        is Dialog -> thisRef.findViewById(id)
        is Fragment -> thisRef.view.findViewById(id)
        is SupportFragment -> thisRef.view!!.findViewById(id)
        is RecyclerView.ViewHolder -> thisRef.itemView.findViewById(id)
        else -> throw IllegalStateException("Unable to use findViewById " +
                "method on the ${thisRef.javaClass.canonicalName}")
    } as? T
}
