package com.rhizo.libcontrol.bean


data class GotoLocation(
    var name: String,
    var x: Float = 0F, //位置坐标 x
    var y: Float = 0F, //位置坐标 y
    var yaw: Float = 0F, //弧度，取值为 [-π, π]，传参超出范围会 ± 2π 重新取值。0 表示在充电桩上重置地图时，temi 的朝向
    var tiltAngle: Int = 0, //头部倾斜角度
    var backwards: Boolean? = null, //传入 true 让 temi 倒着前往目的地
    var noBypass: Boolean? = null, //传入 true 将不允许在 go-to 过程中绕过障碍物
    var speedLevel: SpeedLevel? = null //此次 go-to 的最大运行速度
) : BaseBody()


enum class SpeedLevel(val value: String) {

    HIGH("high"),
    MEDIUM("medium"),
    SLOW("slow");

    companion object {

        @JvmField
        val DEFAULT = HIGH

        @JvmStatic
        fun valueToEnum(value: String): SpeedLevel {
            return when (value) {
                SLOW.value -> SLOW
                MEDIUM.value -> MEDIUM
                HIGH.value -> HIGH
                else -> DEFAULT
            }
        }
    }
}