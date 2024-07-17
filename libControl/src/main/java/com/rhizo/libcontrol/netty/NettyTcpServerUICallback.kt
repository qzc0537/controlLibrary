package com.rhizo.libtcp.netty

interface NettyTcpServerUICallback {
    fun onTcpConnected()
    fun onTcpDisConnected()
    fun onTcpMessage(command: Int, json: String)
}