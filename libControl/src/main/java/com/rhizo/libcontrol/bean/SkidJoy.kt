package com.rhizo.libcontrol.bean

/**
 * @param x 线速度，取值范围为 -1 ~ 1
 * @param y 角速度，取值范围为 -1 ~ 1
 * @param smart 移动时是否自动绕过障碍，为 0.10.79 版本新增参数
 */
data class SkidJoy(var x: Float, var y: Float, var smart: Boolean = true) : BaseBody()