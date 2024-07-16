package com.rhizo.libcontrol.util

import androidx.annotation.WorkerThread
import com.rhizo.libentity.DeleteLocation
import com.rhizo.libentity.GotoLocation
import com.rhizo.libentity.SaveLocation
import com.rhizo.libentity.SkidJoy
import com.rhizo.libentity.SpeedLevel
import com.rhizo.libentity.TiltAngle
import com.rhizo.libentity.TurnBy
import com.rhizo.libentity.UpdateLocation
import com.robotemi.sdk.Robot
import com.robotemi.sdk.constants.Page
import com.robotemi.sdk.constants.SdkConstants
import com.robotemi.sdk.map.Floor
import com.robotemi.sdk.map.Layer
import com.robotemi.sdk.map.LayerPose
import com.robotemi.sdk.navigation.model.Position

/**
 * 机器人API工具类
 */
object RobotUtil {

    /**
     * 打开地图页
     */
    fun startMapPage() {
        Robot.getInstance().startPage(Page.values()[1])
    }

    /**
     * 重置地图
     */
    @WorkerThread
    fun resetMap(allFloor: Boolean): Int {
        return Robot.getInstance().resetMap(allFloor)
    }

    /**
     * 停止扫描地图
     */
    @WorkerThread
    fun finishMapping(mapName: String): Int {
        return Robot.getInstance().finishMapping(mapName)
    }

    /**
     * 跟随
     */
    fun beWithMe() {
        Robot.getInstance().beWithMe()
    }

    /**
     * 停止移动
     */
    fun stopMovement() {
        Robot.getInstance().stopMovement()
    }

    /**
     * 移动
     */
    fun skidJoy(skidJoy: SkidJoy?) {
        skidJoy?.let {
            Robot.getInstance().skidJoy(it.x, it.y, it.smart)
        }
    }

    /**
     * 旋转
     */
    fun turnBy(turnBy: TurnBy?) {
        turnBy?.let {
            Robot.getInstance().turnBy(it.degrees, it.speed)
        }
    }

    /**
     * 头部倾斜角度
     */
    fun tiltAngle(tiltAngle: TiltAngle?) {
        tiltAngle?.let {
            Robot.getInstance().tiltAngle(it.degrees, it.speed)
        }
    }

    /**
     * 保存标点
     */
    fun saveLocation(saveLocation: SaveLocation): Boolean {
        return Robot.getInstance().saveLocation(saveLocation.name)
    }

    /**
     * 删除标点
     */
    fun deleteLocation(deleteLocation: DeleteLocation): Boolean {
        return Robot.getInstance().deleteLocation(deleteLocation.name)
    }

    /**
     * 返回充电桩
     */
    fun gotoHomeBase() {
        Robot.getInstance().goTo(SdkConstants.LOCATION_HOME_BASE)
    }

    /**
     * 前往地点
     */
    fun gotoLocation(gotoLocation: GotoLocation) {
        if (gotoLocation.name.isNotEmpty()) {
            Robot.getInstance().goTo(gotoLocation.name)
        } else {
            val position =
                Position(gotoLocation.x, gotoLocation.y, gotoLocation.yaw, gotoLocation.tiltAngle)
            val speedLevel = when (gotoLocation.speedLevel) {
                SpeedLevel.HIGH -> com.robotemi.sdk.navigation.model.SpeedLevel.HIGH
                SpeedLevel.MEDIUM -> com.robotemi.sdk.navigation.model.SpeedLevel.MEDIUM
                SpeedLevel.SLOW -> com.robotemi.sdk.navigation.model.SpeedLevel.SLOW
                else -> null
            }
            Robot.getInstance()
                .goToPosition(position, gotoLocation.backwards, gotoLocation.noBypass, speedLevel)
        }
    }

    /**
     * 修改地图名称
     */
    fun updateMapName(mapName: String): Int {
        return Robot.getInstance().updateMapName(mapName)
    }

    /**
     * 修改标点
     */
    fun updateLocation(updateLocation: UpdateLocation): Int {
        val poses = mutableListOf<LayerPose>()
        for (item in updateLocation.layerPoses) {
            poses.add(LayerPose(item.x, item.y, item.theta))
        }
        val layer = Layer.upsertLayer(
            updateLocation.layerId,
            updateLocation.layerCategory,
            poses,
            updateLocation.tiltAngle
        )
        return if (layer != null) {
            layer.layerStatus = updateLocation.status
            Robot.getInstance().upsertMapLayer(layer)
        } else {
            -1
        }
    }

    /**
     * 获取当前楼层
     */
    fun getCurrentFloor(): Floor? {
        return Robot.getInstance().getCurrentFloor()
    }

}