package com.rhizo.libtcp.netty

interface TcpConstants {

    object IP {
        const val SERVER_PORT = 7801
    }

    object ConnectStatus {
        const val STATUS_NONE = 0
        const val STATUS_CONNECTING = 1
        const val STATUS_CONNECTED = 2
        const val STATUS_DISCONNECTED = 3
        const val STATUS_ERROR = -1
    }

    object CloudCommand { //与Java服务器交互的命令
        const val HEART_BEAT = 1000
        const val REGISTER_CLOUD = 1001
    }

    object LocalCommand { //用户端与机器人端服务交互的命令
        const val CODE_DEVICE_INIT = 4000 //设备初始化
        const val CODE_CONNECT_WIFI = 4001 //连接wifi
        const val CODE_WIFI_INFO = 4002 //wifi信息
        const val CODE_SCAN_MAP = 6001 //扫描地图
        const val CODE_STOP_MOVEMENT = 6002 //停止移动
        const val CODE_BE_WITH_ME = 6003 //跟随
        const val CODE_BE_WITH_ME_STATUS = 6004 //跟随状态
        const val CODE_GET_MAP_DATA = 6005 //获取地图数据
        const val CODE_MAP_DATA = 6006 //地图数据
        const val CODE_NO_MAP = 6007 //没有地图
        const val CODE_MOVE = 6008 //移动
        const val CODE_TURN_BY = 6009 //旋转
        const val CODE_TILT_ANGLE = 7000 //头部倾斜角度
        const val CODE_SAVE_LOCATION = 7001 //保存标点
        const val CODE_DELETE_LOCATION = 7002 //删除标点
        const val CODE_UPDATE_LOCATION = 7003 //修改标点
        const val CODE_UPDATE_MAP_NAME = 7004 //设置地图名称
        const val CODE_BATTERY_CHANGED = 7005 //电量改变
        const val CODE_GOTO_HOME_BASE = 7006 //返回充电桩
        const val CODE_GOTO_LOCATION = 7007 //前往标点
        const val CODE_NEW_PATH = 7008 //新增路径
        const val CODE_NEW_ELECTRONIC_FENCE = 7009 //新增电子围栏
        const val CODE_GOTO_LOCATION_STATUS = 7010 //前往标点状态
        const val CODE_RESET_MAP = 7011 //重置地图
        const val CODE_FINISH_MAPPING = 7012 //停止扫描地图
        const val CODE_SET_HOME_BASE = 7013 //设置充电桩
        const val CODE_INTERRUPT_SEND_DATA = 7014 //中断分片发送数据
        const val CODE_GET_CHASSIS_SERIAL_NUMBER = 7015 //获取底盘序列号
        const val CODE_POSITION_CHANGED = 7016 //当前位置改变
        const val CODE_VIDEO_CALL = 7017 //视频通话
        const val CODE_GET_CURRENT_FLOOR = 7018 //获取楼层
    }

    object LOCATION {
        const val HOME_BASE = "home base"
    }
}