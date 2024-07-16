package com.rhizo.libcontrol.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import com.rhizo.common.util.LogUtil.logd
import com.rhizo.libcall.webrtc.WebrtcManager
import com.rhizo.libcall.ui.CallActivity
import org.webrtc.VideoTrack

class ControlService : Service() {

    companion object {
        const val STOP_SERVICE = "STOP_SERVICE"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        "ControlService onCreate".logd()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mReceiver, IntentFilter(STOP_SERVICE), RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(mReceiver, IntentFilter(STOP_SERVICE))
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }


    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == STOP_SERVICE) {
                stopSelf()
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        "ControlService onDestroy".logd()
        unregisterReceiver(mReceiver)
    }
}