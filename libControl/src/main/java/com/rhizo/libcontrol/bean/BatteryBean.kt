package com.rhizo.libcontrol.bean

data class BatteryBean(val level: Int, val isCharging: Boolean, var isOffline: Boolean = true) :
    BaseBody()
