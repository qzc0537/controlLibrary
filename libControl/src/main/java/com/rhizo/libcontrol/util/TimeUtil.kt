package com.rhizo.libcontrol.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

object TimeUtil {
    private val mSimpleDateFormat by lazy { SimpleDateFormat() }

    /**
     * 格式化为 yyyy-MM-dd HH:mm:ss
     */
    fun format2FullTime(timeInMills: Long): String {
        mSimpleDateFormat.applyLocalizedPattern("yyyy-MM-dd HH:mm:ss")
        return mSimpleDateFormat.format(Date(timeInMills))
    }

    /**
     * 时区
     */
    fun getTimeZone(): TimeZone {
        return TimeZone.getDefault()
    }

    /**
     * 时区短数字
     */
    @SuppressLint("SimpleDateFormat")
    fun getTimeZoneShort(): Int {
        return TimeZone.getDefault().rawOffset / 3600000
    }

    /**
     * 格式化的时间
     */
    @SuppressLint("SimpleDateFormat")
    fun getTime_yyyy_MM_dd_hh_mm_ss(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(System.currentTimeMillis()))
    }
}