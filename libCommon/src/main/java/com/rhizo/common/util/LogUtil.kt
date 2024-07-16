package com.rhizo.common.util

import android.util.Log
import org.apache.commons.lang3.StringUtils

object LogUtil {
    val TAG = "LogUtil"
    private val LEVEL_I = 0
    private val LEVEL_D = 1
    private val LEVEL_W = 2
    private val LEVEL_E = 3

    var mLogEnable = true

    fun String.logi() {
        log(TAG, this, LEVEL_I)
    }

    fun String.logi(tag: String) {
       log(tag, this, LEVEL_I)
    }

    fun String.logd() {
        log(TAG, this, LEVEL_D)
    }

    fun String.logd(tag: String) {
        log(tag, this, LEVEL_D)
    }

    fun String.logw() {
        log(TAG, this, LEVEL_W)
    }

    fun String.logw(tag: String) {
        log(tag, this, LEVEL_W)
    }

    fun String.loge() {
        log(TAG, this, LEVEL_E)
    }

    fun String.loge(tag: String) {
        log(tag, this, LEVEL_E)
    }

    private fun log(tag: String, msg: String, level: Int) {
        if (mLogEnable) {
            if (msg.length > 4000) {
                var text: String
                for (i in 0..msg.length step 4000) {
                    //当前截取的长度<总长度则继续截取最大的长度来打印
                    text = if (i + 4000 < msg.length) {
                        StringUtils.substring(msg, i, i + 4000)
                    } else {
                        //当前截取的长度已经超过了总长度，则打印出剩下的全部信息
                        StringUtils.substring(msg, i, msg.length)
                    }
                    when (level) {
                        LEVEL_I -> {
                            Log.i(tag, text)
                        }

                        LEVEL_D -> {
                            Log.d(tag, text)
                        }

                        LEVEL_W -> {
                            Log.w(tag, text)
                        }

                        LEVEL_E -> {
                            Log.e(tag, text)
                        }
                    }
                }
            } else {
                //直接打印
                when (level) {
                    LEVEL_I -> {
                        Log.i(tag, msg)
                    }

                    LEVEL_D -> {
                        Log.d(tag, msg)
                    }

                    LEVEL_W -> {
                        Log.w(tag, msg)
                    }

                    LEVEL_E -> {
                        Log.e(tag, msg)
                    }
                }
            }
        }
    }


}