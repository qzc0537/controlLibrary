package com.rhizo.libcontrol.bean

/** 让temi转身指定的角度
 * @param degrees 你要temi转身的角度。向 左 转用正（+）角度值, 向 右 转用负（-）角度值。
 * @param speed 【可选参数】最大转动速度的系数，取值范围为 0 ~ 1。从 0.10.77 开始支持。
 */
data class TurnBy(var degrees: Int = 50, var speed: Float = 1f) : BaseBody()
