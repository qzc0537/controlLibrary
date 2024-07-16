package com.rhizo.libtcp.netty

import android.os.Handler
import android.os.Looper
import android.os.Message
import io.netty.channel.Channel

object TcpManager {
    private val TAG = "TcpManager"
    val mClientChannelSet = mutableSetOf<ClientChannel>()
    private var mServerCallback: NettyTcpServerUICallback? = null
    private var mClientCallback = mutableSetOf<NettyTcpClientUICallback>()
    private var mCloudClientCallback: NettyTcpClientUICallback? = null
    private val CODE_START_CLIENT = 1

    private val mHandler = Handler(Looper.getMainLooper()) {
        when (it.what) {
            CODE_START_CLIENT -> {
                _startClient(it.obj as String, it.arg1)
            }
        }
        true
    }

    /**
     * 开启服务
     */
    fun startServer(
        serverIp: String,
        callback: NettyTcpServerUICallback
    ) {
        if (serverIp.isEmpty()) {
            throw Exception("请先设置服务端IP和端口")
        }
        val port = TcpConstants.IP.SERVER_PORT
        mServerCallback = callback
        NettyTcpServer.startServer(serverIp, port, object : NettyTcpServerListener {
            override fun onChannelConnect(channel: Channel) {
                val socketStr = channel.remoteAddress().toString().split(":")[0]
                val clientChanel = ClientChannel(socketStr, channel, channel.id().asShortText())
                synchronized(mClientChannelSet) {
                    mClientChannelSet.add(clientChanel)
                }
                mHandler.post { mServerCallback?.onTcpConnected() }
            }

            override fun onChannelDisConnect(channel: Channel) {
                for (clientChannel in mClientChannelSet) {
                    synchronized(mClientChannelSet) {
                        if (clientChannel.shortId == channel.id().asShortText()) {
                            mClientChannelSet.remove(clientChannel)
                        }
                    }
                }
                mHandler.post { mServerCallback?.onTcpDisConnected() }
            }

            override fun onStartServer() {
            }

            override fun onStopServer() {
            }

            override fun onServiceStatusConnectChanged(statusCode: Int) {
            }

            override fun onMessageResponseServer(command: Int, json: String) {
                mHandler.post { mServerCallback?.onTcpMessage(command, json) }
            }

        })
    }

    /**
     * 开启客户端
     */
    fun startClient(serverIp: String, serverPort: Int) {
        val msg = Message.obtain().apply {
            this.what = CODE_START_CLIENT
            this.arg1 = serverPort
            this.obj = serverIp
        }
        if (NettyTcpClient.getState() == TcpConstants.ConnectStatus.STATUS_CONNECTED &&
            NettyTcpClient.getIp() == serverIp
        ) {
            mHandler.post {
                val iterator = mClientCallback.iterator()
                while (iterator.hasNext()) {
                    val callback = iterator.next()
                    callback.onTcpConnected()
                }
            }
        } else if (NettyTcpClient.getState() != TcpConstants.ConnectStatus.STATUS_NONE) {
            stopClient(false)
            mHandler.sendMessageDelayed(msg, 1000)
        } else {
            mHandler.sendMessageDelayed(msg, 0)
        }
    }

    /**
     * 开启客户端
     */
    private fun _startClient(serverIp: String, serverPort: Int) {
        NettyTcpClient.connect(serverIp, serverPort, object : NettyTcpClientListener {
            override fun onConnectStatusCallback(statusCode: Int, message: String) {
                NettyTcpClient.setState(statusCode)
                when (statusCode) {
                    TcpConstants.ConnectStatus.STATUS_CONNECTED -> {
                        mHandler.post {
                            val iterator = mClientCallback.iterator()
                            while (iterator.hasNext()) {
                                val callback = iterator.next()
                                callback.onTcpConnected()
                            }
                        }
                    }

                    TcpConstants.ConnectStatus.STATUS_DISCONNECTED -> {
                        mHandler.post {
                            val iterator = mClientCallback.iterator()
                            while (iterator.hasNext()) {
                                val callback = iterator.next()
                                callback.onTcpDisConnected()
                            }
                        }
                    }

                    TcpConstants.ConnectStatus.STATUS_ERROR -> {
                        mHandler.post {
                            val iterator = mClientCallback.iterator()
                            while (iterator.hasNext()) {
                                val callback = iterator.next()
                                callback.onTcpError(message)
                            }
                        }
                    }
                }
            }

            override fun onMessageReceived(command: Int, json: String) {
                mHandler.post {
                    val iterator = mClientCallback.iterator()
                    while (iterator.hasNext()) {
                        val callback = iterator.next()
                        callback.onTcpMessage(command, json)
                    }
                }
            }

        })
    }

    /**
     * 开启与Java服务器通信的客户端
     */
    fun startCloudClient(
        serverIp: String,
        serverPort: Int,
        callback: NettyTcpClientUICallback?
    ) {
        mCloudClientCallback = callback
        NettyTcpCloudClient.connect(serverIp, serverPort, object : NettyTcpClientListener {
            override fun onConnectStatusCallback(statusCode: Int, message: String) {
                NettyTcpClient.setState(statusCode)
                when (statusCode) {
                    TcpConstants.ConnectStatus.STATUS_CONNECTED -> {
                        mHandler.post {
                            mCloudClientCallback?.onTcpConnected()
                        }
                    }

                    TcpConstants.ConnectStatus.STATUS_DISCONNECTED -> {
                        mHandler.post {
                            mCloudClientCallback?.onTcpDisConnected()
                        }
                    }

                    TcpConstants.ConnectStatus.STATUS_ERROR -> {
                        mHandler.post {
                            mCloudClientCallback?.onTcpError(message)
                        }
                    }
                }
            }

            override fun onMessageReceived(command: Int, json: String) {
                mHandler.post {
                    mCloudClientCallback?.onTcpMessage(command, json)
                }
            }

        })
    }

    /**
     * 添加客户端回调
     */
    fun addClientCallback(callback: NettyTcpClientUICallback) {
        mClientCallback.add(callback)
    }

    /**
     * 移除客户端回调
     */
    fun removeClientCallback(callback: NettyTcpClientUICallback): TcpManager {
        val iterator = mClientCallback.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item == callback) {
                iterator.remove()
                break
            }
        }
        return this
    }

    /**
     * 移除回调
     */
    fun removeServerCallback(): TcpManager {
        mServerCallback = null
        return this
    }

    /**
     * 移除回调
     */
    fun removeCloudClientCallback(): TcpManager {
        mCloudClientCallback = null
        return this
    }

    /**
     * 停止服务
     */
    fun stopServer(reconnect: Boolean): TcpManager {
        NettyTcpServer.disconnect(reconnect)
        return this
    }

    /**
     * 停止客户端
     */
    fun stopClient(reconnect: Boolean): TcpManager {
        NettyTcpClient.disconnect(reconnect)
        return this
    }

    /**
     * 停止客户端
     */
    fun stopCloudClient(reconnect: Boolean): TcpManager {
        NettyTcpCloudClient.disconnect(reconnect)
        return this
    }

    /**
     * 服务端发送消息
     */
    fun sendServerMessage(command: Int, json: String) {
        val iterator = mClientChannelSet.iterator()
        while (iterator.hasNext()) {
            val channel = iterator.next()
            NettyTcpServer.sendMessage(command, json, channel, null)
        }
    }

    /**
     * 中断分片发送数据
     */
    fun setInterruptSendData(interrupt: Boolean) {
        NettyTcpServer.setInterruptSendData(interrupt)
    }

    /**
     * 客户端发送消息
     */
    fun sendClientMessage(command: Int, json: String) {
        NettyTcpClient.sendMessage(command, json, null)
    }

    /**
     * 客户端发送消息
     */
    fun sendCloudClientMessage(command: Int, json: String) {
        NettyTcpCloudClient.sendMessage(command, json, null)
    }

}