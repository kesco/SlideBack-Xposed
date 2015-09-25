package com.kesco.xposed.slideback.view.recycleview

import android.R
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * [RecyclerView]的分隔线,注意只支持垂直方向

 * @author Kesco Lin
 */
public class LineDividerItemDecoration(ctx: Context, resId: Int = 0) : RecyclerView.ItemDecoration() {
    private val DEFAULT_ATTRS = intArrayOf(R.attr.listDivider)

    protected var divider: Drawable

    init {
        if (resId != 0) {
            divider = ctx.getDrawable(resId)
        } else {
            val a = ctx.obtainStyledAttributes(DEFAULT_ATTRS)
            divider = a.getDrawable(0)
            a.recycle()
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val endLeft = parent.paddingLeft
        val endRight = parent.width
        val left = endLeft
        val right = endRight - parent.paddingRight

        val childCount = parent.childCount
        for (i in 0..childCount - 1) {
            val child = parent.getChildAt(i)
            if (child.visibility != View.VISIBLE) {
                continue
            }
            val params = child.layoutParams as RecyclerView.LayoutParams

            val top = child.bottom + params.bottomMargin
            val bottom = top + divider.intrinsicHeight

            if (i == childCount - 1) {
                divider.setBounds(endLeft, top, endRight, bottom)
            } else {
                divider.setBounds(left, top, right, bottom)
            }
            divider.draw(c)
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.getChildLayoutPosition(view) < 1) {
            return
        }
        outRect.top = divider.intrinsicHeight
    }
}

