package com.rhizo.libentity

data class BeWithMeStatus(val status: String) : BaseBody() {
    companion object {

        const val ABORT = "abort" //用户或 temi 中止跟随指令

        const val CALCULATING = "calculating" //temi 被障碍物挡住并试图计算出绕过障碍物的路线

        const val SEARCH = "search" //跟随模式开启并正在搜索要跟随的人体

        const val START = "start" //temi 找到要跟随的人并开始跟随

        const val TRACK = "track" //temi 正在跟随

        const val OBSTACLE_DETECTED = "obstacle detected" //temi 检测到障碍物
    }
}