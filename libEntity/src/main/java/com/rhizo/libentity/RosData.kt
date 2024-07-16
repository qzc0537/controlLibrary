package com.rhizo.libentity

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class MapDataRequest(
    @SerializedName(value = "mapId", alternate = ["_id"]) val id: String? = "",
    @SerializedName("locations") val locations: List<LocationBean>? = listOf(),
    @SerializedName("map") val map: MapBean? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("requestId") val requestId: String? = null,
    @SerializedName("pbFilesUrl") val pbFilesUrl: String? = null,
    @SerializedName("virtualWalls") val virtualWalls: List<Layer>? = listOf(),
    @SerializedName("greenPath") val greenPath: List<Layer>? = listOf(),
    @SerializedName("mapEraser") val mapEraser: Layer? = null,
)

@Keep
data class LayerPoseBean(
        @SerializedName("x") val x: Float,
        @SerializedName("y") val y: Float,
        @SerializedName("theta") val theta: Float
)

@Keep
data class LocationBean(
        @SerializedName("x") val x: Float,
        @SerializedName("y") val y: Float,
        @SerializedName("yaw") val yaw: Float,
        @SerializedName("name") val name: String,
        @SerializedName("created") val created: Long = System.currentTimeMillis(),
        @SerializedName("lastUsed") val lastUsed: Long = 0L,
        @SerializedName("useNumber") val useNumber: Int = 0,
        @SerializedName("tilt_angle") val tiltAngle: Float = 0f
)

@Keep
data class TraversabilityLayer(
    @SerializedName("layer_creation_universal_time") val layerCreationUTC: Int,
    @SerializedName("layer_category") val layerCategory: Int,
    @SerializedName("layer_id") val layerId: String = "",
    @SerializedName("layer_thickness") val layerThickness: Float,
    @SerializedName("layer_status") val layerStatus: Int,
    @SerializedName("layer_poses") val layerPoses: List<LayerPose>?,
    @SerializedName("direction") private val pDirection: Int? = 0, // for one-way virtual wall, -1, 0, 1.
    @SerializedName("data") val data: String? = "", // added in sprint 133 for map eraser layer
) {
        val direction: Int
                get() = pDirection ?: 0
}

@Keep
data class DataBean(
        @SerializedName("cols") val cols: Int,
        @SerializedName("rows") val rows: Int,
        @SerializedName("data") val data: String,
        @SerializedName("dt") val dt: String,
        @SerializedName("type_id") val typeId: String
)

@Keep
data class MapBean(
    @SerializedName("data") val data: DataBean,
    @SerializedName("width") val width: Int,
    @SerializedName("height") val height: Int,
    @SerializedName("origin_x") val originX: Float,
    @SerializedName("origin_y") val originY: Float,
    @SerializedName("resolution") val resolution: Float
)

@Keep
data class Position(
        @SerializedName("x") val x: Float,
        @SerializedName("y") val y: Float
)

@Keep
data class Origin(
        @SerializedName("position") val position: Position
)

@Keep
data class MapInfo2(
    @SerializedName("height") val height: Int,
    @SerializedName("origin") val origin: Origin,
    @SerializedName("resolution") val resolution: Float,
    @SerializedName("width") val width: Int
)

@Keep
data class BaseInfoPosition(
        @SerializedName("robot_2d_map_pose") val pose: LayerPoseBean
)

@Keep
data class BaseMapFetch(
        val data: String,
        val info: MapInfo2
)

@Keep
data class BaseMapElement(
        val status: Int,
        @SerializedName("status_description") val statusDescription: Int,
        @SerializedName("traversability_layer_list") val traversabilityLayer: List<TraversabilityLayer>
)