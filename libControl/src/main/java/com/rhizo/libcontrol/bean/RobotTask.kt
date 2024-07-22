package com.rhizo.libcontrol.bean

data class RobotTask(
    val taskName: String,
    val taskLocations: List<RobotTaskLocations>,
    val executionTime: Int,
    val createTime: Long = System.currentTimeMillis(),
    var executing: Boolean = false
)

data class RobotTaskLocations(val locationName: String)
