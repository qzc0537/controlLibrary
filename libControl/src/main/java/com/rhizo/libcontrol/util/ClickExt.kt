package com.rhizo.libcontrol.util

import android.app.Activity
import android.view.View
import androidx.fragment.app.Fragment

object ClickExt {
    private val CLICK_TIME = 500
    private var lastClickTime = 0L

    fun View.setOnSafeClick(block: (View) -> Unit) {
        this.setOnClickListener {
            val now = System.currentTimeMillis()
            if (now - lastClickTime > CLICK_TIME) {
                lastClickTime = now
                block(it)
            }
        }
    }

    fun Activity.setClick(block: (View) -> Unit, vararg views: View) {
        for (view in views) {
            view.setOnSafeClick {
                block(it)
            }
        }
    }

    fun Fragment.setClick(block: (View) -> Unit, vararg views: View) {
        for (view in views) {
            view.setOnSafeClick {
                block(it)
            }
        }
    }

}