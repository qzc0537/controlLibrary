package com.rhizo.libcontrol.util

import android.bluetooth.BluetoothDevice
import com.rhizo.libcontrol.bean.BaseMessage
import com.rhizo.libcontrol.bean.BatteryBean
import com.rhizo.libcontrol.bean.BeWithMeStatus
import com.rhizo.libcontrol.bean.ChassisSerialBean
import com.rhizo.libcontrol.bean.DeleteLocation
import com.rhizo.libcontrol.bean.DeviceInit
import com.rhizo.libcontrol.bean.FinishMapping
import com.rhizo.libcontrol.bean.GotoLocationStatus
import com.rhizo.libcontrol.bean.PositionBean
import com.rhizo.libcontrol.bean.ResetMap
import com.rhizo.libcontrol.bean.SaveLocation
import com.rhizo.libcontrol.bean.UpdateLocationResult
import com.rhizo.libcontrol.bean.UpdateMapName
import com.rhizo.libcontrol.bean.WifiBean
import com.rhizo.libcontrol.bluetooth.BluetoothConst
import com.rhizo.libcontrol.bluetooth.BluetoothSender
import com.rhizo.libcontrol.bluetooth.BluetoothServer
import com.rhizo.libcontrol.netty.NettyTcpServer
import com.rhizo.libcontrol.netty.TcpConstants
import com.rhizo.libcontrol.netty.TcpManager
import com.robotemi.sdk.map.Floor
import com.robotemi.sdk.map.MapDataModel

/**
 * 机器人端发送消息工具类
 */
object MessageUtil {

    /**
     * 发送设备初始化
     */
    fun sendDeviceInit(
        serialNo: String,
        deviceInit: DeviceInit,
        bluetoothDevice: BluetoothDevice?
    ) {
        val json = GsonUtil.toJson(BaseMessage(serialNo, GsonUtil.toJson(deviceInit)))
        if (BluetoothServer.getInstance().state == BluetoothConst.STATE_CONNECTED) {
            BluetoothSender.sendServerMessage(
                TcpConstants.LocalCommand.CODE_DEVICE_INIT,
                json,
                bluetoothDevice
            )
        }
    }

    /**
     * 发送wifi信息
     */
    fun sendWifiInfo(
        serialNo: String,
        wifiBean: WifiBean,
        bluetoothDevice: BluetoothDevice?
    ) {
        val json = GsonUtil.toJson(BaseMessage(serialNo, GsonUtil.toJson(wifiBean)))
        if (BluetoothServer.getInstance().state == BluetoothConst.STATE_CONNECTED) {
            BluetoothSender.sendServerMessage(
                TcpConstants.LocalCommand.CODE_WIFI_INFO,
                json,
                bluetoothDevice
            )
        }
    }

    /**
     * 发送地图
     */
    fun sendMap(
        serialNo: String,
        mapDataModel: MapDataModel?
    ) {
        val code =
            if (mapDataModel != null) TcpConstants.LocalCommand.CODE_MAP_DATA else TcpConstants.LocalCommand.CODE_NO_MAP
        val bodyJson = if (mapDataModel != null) GsonUtil.toJson(mapDataModel) else ""
        val json = GsonUtil.toJson(BaseMessage(serialNo, bodyJson))
        if (NettyTcpServer.getChannelActive()) {
            TcpManager.sendServerMessage(
                code,
                json
            )
        }
    }

    /**
     * 发送跟随状态
     */
    fun sendBeWithMeStatus(
        serialNo: String,
        beWithMeStatus: BeWithMeStatus,
        bluetoothDevice: BluetoothDevice?
    ) {
        val json = GsonUtil.toJson(BaseMessage(serialNo, GsonUtil.toJson(beWithMeStatus)))
        if (NettyTcpServer.getChannelActive()) {
            TcpManager.sendServerMessage(
                TcpConstants.LocalCommand.CODE_BE_WITH_ME_STATUS,
                json
            )
        } else if (BluetoothServer.getInstance().state == BluetoothConst.STATE_CONNECTED) {
            BluetoothSender.sendServerMessage(
                TcpConstants.LocalCommand.CODE_BE_WITH_ME_STATUS,
                json,
                bluetoothDevice
            )
        }
    }

    /**
     * 发送保存标点结果
     */
    fun sendSaveLocation(
        serialNo: String,
        saveLocation: SaveLocation,
        bluetoothDevice: BluetoothDevice?
    ) {
        val json = GsonUtil.toJson(BaseMessage(serialNo, GsonUtil.toJson(saveLocation)))
        if (NettyTcpServer.getChannelActive()) {
            TcpManager.sendServerMessage(
                TcpConstants.LocalCommand.CODE_SAVE_LOCATION,
                json
            )
        } else if (BluetoothServer.getInstance().state == BluetoothConst.STATE_CONNECTED) {
            BluetoothSender.sendServerMessage(
                TcpConstants.LocalCommand.CODE_SAVE_LOCATION,
                json,
                bluetoothDevice
            )
        }
    }

    /**
     * 发送删除标点结果
     */
    fun sendDeleteLocation(
        serialNo: String,
        deleteLocation: DeleteLocation,
        bluetoothDevice: BluetoothDevice?
    ) {
        val json = GsonUtil.toJson(BaseMessage(serialNo, GsonUtil.toJson(deleteLocation)))
        if (NettyTcpServer.getChannelActive()) {
            TcpManager.sendServerMessage(
                TcpConstants.LocalCommand.CODE_DELETE_LOCATION,
                json
            )
        } else if (BluetoothServer.getInstance().state == BluetoothConst.STATE_CONNECTED) {
            BluetoothSender.sendServerMessage(
                TcpConstants.LocalCommand.CODE_DELETE_LOCATION,
                json,
                bluetoothDevice
            )
        }
    }

    /**
     * 发送更新地图名称结果
     */
    fun sendUpdateMapName(
        serialNo: String,
        updateMapName: UpdateMapName,
        bluetoothDevice: BluetoothDevice?
    ) {
        val json = GsonUtil.toJson(BaseMessage(serialNo, GsonUtil.toJson(updateMapName)))
        if (NettyTcpServer.getChannelActive()) {
            TcpManager.sendServerMessage(
                TcpConstants.LocalCommand.CODE_UPDATE_MAP_NAME,
                json
            )
        } else if (BluetoothServer.getInstance().state == BluetoothConst.STATE_CONNECTED) {
            BluetoothSender.sendServerMessage(
                TcpConstants.LocalCommand.CODE_UPDATE_MAP_NAME,
                json,
                bluetoothDevice
            )
        }
    }

    /**
     * 发送更新标点结果
     */
    fun sendUpdateLocation(
        serialNo: String,
        updateLocationResult: UpdateLocationResult,
        bluetoothDevice: BluetoothDevice?
    ) {
        val json = GsonUtil.toJson(BaseMessage(serialNo, GsonUtil.toJson(updateLocationResult)))
        if (NettyTcpServer.getChannelActive()) {
            TcpManager.sendServerMessage(
                TcpConstants.LocalCommand.CODE_UPDATE_LOCATION,
                json
            )
        } else if (BluetoothServer.getInstance().state == BluetoothConst.STATE_CONNECTED) {
            BluetoothSender.sendServerMessage(
                TcpConstants.LocalCommand.CODE_UPDATE_LOCATION,
                json,
                bluetoothDevice
            )
        }
    }

    /**
     * 发送重置地图结果
     */
    fun sendResetMap(
        serialNo: String,
        resetMap: ResetMap,
        bluetoothDevice: BluetoothDevice?
    ) {
        val json = GsonUtil.toJson(BaseMessage(serialNo, GsonUtil.toJson(resetMap)))
        if (NettyTcpServer.getChannelActive()) {
            TcpManager.sendServerMessage(
                TcpConstants.LocalCommand.CODE_RESET_MAP,
                json
            )
        } else if (BluetoothServer.getInstance().state == BluetoothConst.STATE_CONNECTED) {
            BluetoothSender.sendServerMessage(
                TcpConstants.LocalCommand.CODE_RESET_MAP,
                json,
                bluetoothDevice
            )
        }
    }

    /**
     * 发送完成地图扫描结果
     */
    fun sendFinishMapping(
        serialNo: String,
        finishMapping: FinishMapping,
        bluetoothDevice: BluetoothDevice?
    ) {
        val json = GsonUtil.toJson(BaseMessage(serialNo, GsonUtil.toJson(finishMapping)))
        if (NettyTcpServer.getChannelActive()) {
            TcpManager.sendServerMessage(
                TcpConstants.LocalCommand.CODE_FINISH_MAPPING,
                json
            )
        } else if (BluetoothServer.getInstance().state == BluetoothConst.STATE_CONNECTED) {
            BluetoothSender.sendServerMessage(
                TcpConstants.LocalCommand.CODE_FINISH_MAPPING,
                json,
                bluetoothDevice
            )
        }
    }

    /**
     * 发送前往标点状态
     */
    fun sendGotoLocationStatus(
        serialNo: String,
        gotoLocationStatus: GotoLocationStatus,
        bluetoothDevice: BluetoothDevice?
    ) {
        val json = GsonUtil.toJson(BaseMessage(serialNo, GsonUtil.toJson(gotoLocationStatus)))
        if (NettyTcpServer.getChannelActive()) {
            TcpManager.sendServerMessage(
                TcpConstants.LocalCommand.CODE_GOTO_LOCATION_STATUS,
                json
            )
        } else if (BluetoothServer.getInstance().state == BluetoothConst.STATE_CONNECTED) {
            BluetoothSender.sendServerMessage(
                TcpConstants.LocalCommand.CODE_GOTO_LOCATION_STATUS,
                json,
                bluetoothDevice
            )
        }
    }

    /**
     * 发送电量数据
     */
    fun sendBatteryData(
        serialNo: String,
        batteryBean: BatteryBean,
        bluetoothDevice: BluetoothDevice?
    ) {
        val json = GsonUtil.toJson(BaseMessage(serialNo, GsonUtil.toJson(batteryBean)))
        if (NettyTcpServer.getChannelActive()) {
            TcpManager.sendServerMessage(
                TcpConstants.LocalCommand.CODE_BATTERY_CHANGED,
                json
            )
        } else if (BluetoothServer.getInstance().state == BluetoothConst.STATE_CONNECTED) {
            BluetoothSender.sendServerMessage(
                TcpConstants.LocalCommand.CODE_BATTERY_CHANGED,
                json,
                bluetoothDevice
            )
        }
    }

    /**
     * 发送当前位置
     */
    fun sendCurrentPosition(
        serialNo: String,
        positionBean: PositionBean,
        bluetoothDevice: BluetoothDevice?
    ) {
        val json = GsonUtil.toJson(BaseMessage(serialNo, GsonUtil.toJson(positionBean)))
        if (NettyTcpServer.getChannelActive()) {
            TcpManager.sendServerMessage(
                TcpConstants.LocalCommand.CODE_POSITION_CHANGED,
                json
            )
        } else if (BluetoothServer.getInstance().state == BluetoothConst.STATE_CONNECTED) {
            BluetoothSender.sendServerMessage(
                TcpConstants.LocalCommand.CODE_POSITION_CHANGED,
                json,
                bluetoothDevice
            )
        }
    }

    /**
     * 发送temi序列号
     */
    fun sendTemiSerialNumber(
        serialNo: String,
        chassisSerialBean: ChassisSerialBean,
        bluetoothDevice: BluetoothDevice?
    ) {
        val json = GsonUtil.toJson(BaseMessage(serialNo, GsonUtil.toJson(chassisSerialBean)))
        if (NettyTcpServer.getChannelActive()) {
            TcpManager.sendServerMessage(
                TcpConstants.LocalCommand.CODE_GET_CHASSIS_SERIAL_NUMBER,
                json
            )
        } else if (BluetoothServer.getInstance().state == BluetoothConst.STATE_CONNECTED) {
            BluetoothSender.sendServerMessage(
                TcpConstants.LocalCommand.CODE_GET_CHASSIS_SERIAL_NUMBER,
                json,
                bluetoothDevice
            )
        }
    }

    /**
     * 发送视频通话
     */
    fun sendVideoCall(serialNo: String) {
        val json = GsonUtil.toJson(BaseMessage(serialNo, ""))
        if (NettyTcpServer.getChannelActive()) {
            TcpManager.sendServerMessage(
                TcpConstants.LocalCommand.CODE_VIDEO_CALL,
                json
            )
        }
    }

    /**
     * 发送当前楼层
     */
    fun sendCurrentFloor(serialNo: String, floor: Floor) {
        val json = GsonUtil.toJson(BaseMessage(serialNo, GsonUtil.toJson(floor)))
        if (NettyTcpServer.getChannelActive()) {
            TcpManager.sendServerMessage(
                TcpConstants.LocalCommand.CODE_GET_CURRENT_FLOOR,
                json
            )
        }
    }
}