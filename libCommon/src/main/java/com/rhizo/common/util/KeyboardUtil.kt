package com.rhizo.common.util

import android.app.Activity
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

object KeyboardUtil {

    /**
     * 显示软键盘
     */
    fun showInputMethod(view: View) {
        if (view == null) {
            return
        }
        if (view is EditText) {
            view.requestFocus()
        }
        val inputmethodManager =
            view.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputmethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    /**
     * 隐藏软键盘
     */
    fun hideInputMethod(view: View) {
        try {
            val inputmethodManager =
                view.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputmethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 强制隐藏软键盘
     * @param activity 当前Activity
     */
    fun hideInputMethod(activity: Activity) {
        val inputmethodManager =
            activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputmethodManager.isActive) {
            val binder = activity.window.decorView.windowToken
            inputmethodManager.hideSoftInputFromWindow(binder, 0)
        }
    }

    /**
     * 强制隐藏软键盘
     * @param context 当前Activity
     */
    fun hideInputMethod(context: Context) {
        if (context is Activity) {
            hideInputMethod(context)
        }
    }
}