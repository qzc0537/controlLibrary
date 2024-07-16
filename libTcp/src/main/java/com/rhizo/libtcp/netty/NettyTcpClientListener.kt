package com.rhizo.libtcp.netty

interface NettyTcpClientListener {
    fun onConnectStatusCallback(statusCode: Int, message: String)
    fun onMessageReceived(command: Int, json: String)
}