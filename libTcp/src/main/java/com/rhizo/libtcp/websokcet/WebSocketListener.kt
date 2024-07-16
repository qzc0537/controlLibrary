package com.rhizo.libtcp.websokcet

import okhttp3.WebSocket

interface WebSocketListener {
    fun onConnected(webSocket: WebSocket)
    fun onMessage(message: String)
    fun onDisconnected()
    fun onFailure(t: Throwable)
}