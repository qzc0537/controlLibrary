package com.rhizo.libentity

data class GotoLocationStatus(
    val location: String,
    val status: String,
    val descriptionId: Int,
    val description: String
) : BaseBody() {
    companion object {
        const val START = "start" //导航开始

        const val CALCULATING = "calculating" //正在规划前往目的地的路线

        const val GOING = "going" //路线规划完成并正在前往目的地点

        const val COMPLETE = "complete" //到达目的地点

        const val ABORT = "abort" //导航终止

        const val REPOSING = "reposing" //导航过程中的重定位
    }
}
