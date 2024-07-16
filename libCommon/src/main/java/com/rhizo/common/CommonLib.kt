package com.rhizo.common

import android.app.Application
import android.content.Context
import com.rhizo.common.util.LogUtil

object CommonLib {
    private var mApplication: Application? = null

    fun init(application: Application, enableLog: Boolean) {
        this.mApplication = application
        LogUtil.mLogEnable = enableLog
    }

    fun getContext(): Context {
        if (mApplication == null) {
            throw RuntimeException("Please call [CommonLib.init()]")
        }
        return mApplication!!.applicationContext
    }

}