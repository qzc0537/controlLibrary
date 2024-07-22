package com.rhizo.libcontrol.websokcet

import androidx.annotation.Nullable
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit


class WebSocketClient(private val url: String) {
    private val TAG = "WebSocketClient"
    private var webSocket: WebSocket? = null
    private var client: OkHttpClient? = null

    fun start(listener: com.rhizo.libcontrol.websokcet.WebSocketListener) {
        client = OkHttpClient.Builder()
            .pingInterval(10, TimeUnit.SECONDS) // 设置ping消息发送的间隔
            .build()
        val request: Request = Request.Builder()
            .url(url) // 替换为你的wss URL
            .build()
        webSocket = client!!.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                // 连接打开时的回调
                listener.onConnected(webSocket)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                // 接收到文本消息时的回调
                listener.onMessage(text)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                // 连接即将关闭时的回调
                webSocket.close(1000, null)
                listener.onDisconnected()
            }

            override fun onFailure(
                webSocket: WebSocket,
                t: Throwable,
                @Nullable response: Response?
            ) {
                // 连接失败时的回调
                t.printStackTrace()
                listener.onFailure(t)
            }
        })
    }

    fun stop() {
        webSocket?.close(1000, "Goodbye!")
    }
}