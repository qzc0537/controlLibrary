package com.rhizo.libcontrol.bean


data class UpdateLocation(
    var layerId: String?,
    val layerCategory: Int,
    val layerPoses: List<LayerPose>,
    val tiltAngle: Int? = null, //倾斜角度 -25~55
    val status: Int
) : BaseBody() {
    companion object {
        const val GREEN_PATH = 0
        const val VIRTUAL_WALL = 3
        const val LOCATION = 4
        const val MAP_ERASER = 6

        const val STATUS_CURRENT = 0
        const val STATUS_UPDATE = 1
        const val STATUS_ADD_POSE = 2
        const val STATUS_DELETE = 3
    }
}