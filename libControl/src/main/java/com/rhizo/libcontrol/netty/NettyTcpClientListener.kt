package com.rhizo.libcontrol.netty

interface NettyTcpClientListener {
    fun onConnectStatusCallback(statusCode: Int, message: String)
    fun onMessageReceived(command: Int, json: String)
}