package com.rhizo.libcontrol.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.google.gson.Gson
import com.rhizo.libcontrol.bean.BluetoothMessage

object BluetoothSender {
    private val mGson by lazy { Gson() }

    private fun toJson(data: Any?): String {
        if (data == null) return ""
        return mGson.toJson(data)
    }

    /**
     * 客户端发送消息
     */
    @SuppressLint("MissingPermission")
    fun sendClientMessage(command: Int, contentJson: String) {
        val bluetoothMessage = BluetoothMessage(
            command,
            contentJson
        )
        val data = toJson(bluetoothMessage)
        BluetoothClient.getInstance().write(data.toByteArray())
    }

    /**
     * 服务端发送消息
     */
    @SuppressLint("MissingPermission")
    fun sendServerMessage(command: Int, contentJson: String, device: BluetoothDevice?) {
        if (device == null) {
            return
        }
        val bluetoothMessage = BluetoothMessage(
            command,
            contentJson
        )
        val data = toJson(bluetoothMessage)
        BluetoothServer.getInstance().write(device.address, data.toByteArray())
    }


}