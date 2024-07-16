package com.rhizo.common.util

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.provider.Settings

object BluetoothUtil {
    private var mBluetoothAdapter: BluetoothAdapter? = null

    /**
     * 蓝牙管理器
     */
    fun getBluetoothAdapter(): BluetoothAdapter? {
        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        }
        return mBluetoothAdapter
    }

    /**
     * 打开蓝牙设置页
     */
    fun openBluetoothSettings(context: Context) {
        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
        context.startActivity(intent)
    }
}