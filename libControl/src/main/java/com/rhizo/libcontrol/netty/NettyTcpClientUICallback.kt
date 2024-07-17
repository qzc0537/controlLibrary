package com.rhizo.libcontrol.netty

interface NettyTcpClientUICallback {
    fun onTcpConnected() {}
    fun onTcpDisConnected() {}
    fun onTcpError(message: String) {}
    fun onTcpMessage(command: Int, json: String) {}
}