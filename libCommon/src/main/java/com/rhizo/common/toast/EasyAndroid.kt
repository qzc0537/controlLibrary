package com.rhizo.common.toast

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object EasyAndroid {
    private var context : Context? = null

    @JvmStatic
    fun getApplicationContext() : Context {
        if (context == null) {
            throw RuntimeException("Please call [EasyAndroid.init(context)] first")
        } else {
            return context as Context
        }
    }

    @JvmStatic
    fun init(context : Context) {
        if (EasyAndroid.context != null) return
        EasyAndroid.context = context.applicationContext
    }
}