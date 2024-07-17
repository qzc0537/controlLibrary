package com.rhizo.libcontrol.netty

import io.netty.channel.Channel

interface NettyTcpServerListener {
    companion object {
        const val STATUS_CONNECT_SUCCESS: Int = 1
        const val STATUS_CONNECT_CLOSED: Int = 0
        const val STATUS_CONNECT_ERROR: Int = 0
    }

    /**
     * 与客户端建立连接
     */
    fun onChannelConnect(channel: Channel)

    /**
     * 与客户端断开连接
     */
    fun onChannelDisConnect(channel: Channel)

    /**
     * server开启成功
     */
    fun onStartServer()

    /**
     * server关闭
     */
    fun onStopServer()

    fun onServiceStatusConnectChanged(statusCode: Int)

    fun onMessageResponseServer(command: Int, json: String)
}