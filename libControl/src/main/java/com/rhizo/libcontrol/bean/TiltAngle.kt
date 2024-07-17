package com.rhizo.libcontrol.bean

/** 让temi的头部倾斜到一个指定的角度（绝对角度）
 * @param degrees 要temi继续倾斜的角度值(-25 ~ 55，-25度意味着temi会一直向下看，+55度则意味着temi会一直向上看，0度则意味着temi会直视前方。)
 * @param speed 【可选参数】最大转动速度的系数，取值范围为 0 ~ 1。从 0.10.77 开始支持。
 */
data class TiltAngle(var degrees: Int = 50, var speed: Float = 0.6f) : BaseBody()
