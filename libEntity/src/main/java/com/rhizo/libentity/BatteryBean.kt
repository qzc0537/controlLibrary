package com.rhizo.libentity

data class BatteryBean(val level: Int, val isCharging: Boolean, var isOffline: Boolean = true) :
    BaseBody()
