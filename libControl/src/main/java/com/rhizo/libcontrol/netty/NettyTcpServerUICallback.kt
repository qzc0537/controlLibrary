package com.rhizo.libcontrol.netty

interface NettyTcpServerUICallback {
    fun onTcpConnected()
    fun onTcpDisConnected()
    fun onTcpMessage(command: Int, json: String)
}