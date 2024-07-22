package com.rhizo.libcontrol.bluetooth

import android.bluetooth.BluetoothDevice

interface OnBluetoothUICallback {

    fun onBluetoothConnecting() {}

    fun onBluetoothConnectFailed() {}

    fun onBluetoothConnected(device: BluetoothDevice) {}

    fun onBluetoothDisConnected() {}

    fun onBluetoothMessage(message: String) {}

    fun onBluetoothWriteDataSuccess() {}

    fun onBluetoothWriteDataFailed(message: String) {}
}