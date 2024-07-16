package com.rhizo.libcontrol.bean

data class RegisterCloudBean(
    val deviceModel: String,
    val secretCode: String,
    val secretKey: String,
    val serialNo: String,
    val timezone: Int,
    val time: String,
    val version: String
)

data class VersionBean(
    val androidVersion: String,
    val baseVersion: String,
    val hardwareVersion: String
)
