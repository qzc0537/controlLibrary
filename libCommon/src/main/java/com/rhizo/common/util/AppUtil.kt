@file:Suppress("UNREACHABLE_CODE")

package com.rhizo.common.util

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Process
import androidx.appcompat.app.AppCompatActivity

object AppUtil {
    private var mAppInBackground = false
    const val TYPE_WIFI = 1
    const val TYPE_MOBILE = 2
    const val TYPE_BLUETOOTH = 3
    const val TYPE_UNKNOWN = 4
    const val TYPE_NO_NET = 5

    /**
     * 获取wifi信息
     */
    fun getWifiInfo(context: Context): WifiInfo {
        val wifiManager =
            context.applicationContext.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager
        return wifiManager.connectionInfo
    }

    /**
     * 获取wifi名称
     * [android.permission.ACCESS_FINE_LOCATION","android.permission.ACCESS_WIFI_STATE]
     */
    @SuppressLint("MissingPermission")
    fun getSSID(context: Context): String {
        val wifiManager =
            context.applicationContext.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        var ssid = wifiInfo.ssid
        if (ssid.contains("unknown")) {
            val configuredNetworks = wifiManager.configuredNetworks
            for (configuration in configuredNetworks) {
                if (configuration.networkId == wifiInfo.networkId) {
                    ssid = configuration.SSID
                    break
                }
            }
        }
        return ssid.replace("\"", "")
    }

    /**
     * 获取网络类型
     */
    fun getNetworkType(context: Context): Int {
        val connectivityManager =
            context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return if (networkInfo != null && networkInfo.isConnected) {
            when (networkInfo.type) {
                android.net.ConnectivityManager.TYPE_WIFI -> {
                    TYPE_WIFI
                }

                android.net.ConnectivityManager.TYPE_MOBILE -> {
                    TYPE_MOBILE
                }

                android.net.ConnectivityManager.TYPE_BLUETOOTH -> {
                    TYPE_BLUETOOTH
                }

                else -> {
                    TYPE_UNKNOWN
                }
            }
        } else {
            TYPE_NO_NET
        }
    }

    /**
     * 是否有网
     */
    fun hasNetwork(context: Context): Boolean {
        val connectivityManager =
            context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return if (networkInfo != null && networkInfo.isConnected) {
            when (networkInfo.type) {
                android.net.ConnectivityManager.TYPE_WIFI -> {
                    return true
                }

                android.net.ConnectivityManager.TYPE_MOBILE -> {
                    return true
                }

                else -> {
                    return false
                }
            }
        } else {
            return false
        }
    }

    fun isAppInBackground(): Boolean {
        return mAppInBackground
    }

    fun setAppInBackground(isInBackground: Boolean) {
        mAppInBackground = isInBackground
    }

    /**
     * 是否主进程
     */
    fun isMainProcess(context: Context): Boolean {
        return isPidOfProcessName(context, Process.myPid(), getMainProcessName(context))
    }

    /**
     * 是否指定进程id，进程名一致
     */
    fun isPidOfProcessName(context: Context, pid: Int, pName: String): Boolean {
        var isMain = false
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (process in manager.runningAppProcesses) {
            if (process.pid == pid) {
                if (process.processName == pName) {
                    isMain = true
                    break
                }
            }
        }

        return isMain
    }

    /**
     * 主进程名
     */
    fun getMainProcessName(context: Context): String =
        context.packageManager.getApplicationInfo(context.packageName, 0).processName

    /**
     * b包信息
     */
    fun getPackageInfo(context: Context): PackageInfo {
        val manager = context.applicationContext.packageManager
        val packageName = context.applicationContext.packageName
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            manager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            manager.getPackageInfo(packageName, 0)
        }
    }

    /**
     * 获取版本名
     */
    fun getVersionName(context: Context): String {
        return getPackageInfo(context).versionName
    }

    /**
     * 获取版本号
     */
    fun getVersionCode(context: Context): Int {
        return getPackageInfo(context).versionCode
    }

    fun startAppWithPackageName(context: Context) {
        context.packageManager.getLaunchIntentForPackage(context.packageName)?.let {
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(it)
        }
    }


}