package com.rhizo.bluetooth.util

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper

/**
 * 扫描蓝牙工具类
 */
class ScanBluetoothUtil {
    private var mContext: Context? = null
    private val mBluetoothAdapter by lazy { BluetoothAdapter.getDefaultAdapter() }
    private var mOnScanBluetoothListener: OnScanBluetoothListener? = null

    private val CODE_CANCEL_SCAN = 100
    private val mHandler = Handler(Looper.getMainLooper()) {
        when (it.what) {
            CODE_CANCEL_SCAN -> {
                cancelDiscovery()
            }
        }
        true
    }

    interface OnScanBluetoothListener {
        fun onNotSupportBluetooth() {}
        fun onBluetoothDisable() {}
        fun onStartDiscovery() {}
        fun onCancelDiscovery() {}
        fun onBluetoothDiscovery(device: BluetoothDevice) {}
    }

    constructor(context: Context) {
        mContext = context
        context.registerReceiver(mBluetoothReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
    }

    fun setOnConnectBluetoothListener(listener: OnScanBluetoothListener?) {
        mOnScanBluetoothListener = listener
    }

    /**
     * 释放资源
     */
    fun release() {
        cancelDiscovery()
        mOnScanBluetoothListener = null
        mContext?.unregisterReceiver(mBluetoothReceiver)
        mHandler.removeCallbacksAndMessages(null)
    }

    /**
     * 开始扫描
     */
    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        if (mBluetoothAdapter == null) {
            mOnScanBluetoothListener?.onNotSupportBluetooth()
            return
        }
        if (!mBluetoothAdapter.isEnabled) {
            mOnScanBluetoothListener?.onBluetoothDisable()
            return
        }
        mBluetoothAdapter?.startDiscovery()
        mOnScanBluetoothListener?.onStartDiscovery()
        mHandler.sendEmptyMessageDelayed(CODE_CANCEL_SCAN, 5000)
    }

    /**
     * 取消扫描
     */
    @SuppressLint("MissingPermission")
    fun cancelDiscovery() {
        mBluetoothAdapter?.cancelDiscovery()
        mOnScanBluetoothListener?.onCancelDiscovery()
        mHandler.removeMessages(CODE_CANCEL_SCAN)
    }

    private val mBluetoothReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        ?: return
                // 这里可以获取到设备信息，例如：device.getName(), device.getAddress()
                if (!device.name.isNullOrEmpty() && !device.address.isNullOrEmpty()) {
                    mOnScanBluetoothListener?.onBluetoothDiscovery(device)
                }
            }
        }
    }
}