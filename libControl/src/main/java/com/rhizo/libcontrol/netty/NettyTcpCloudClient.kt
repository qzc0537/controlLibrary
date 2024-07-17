package com.rhizo.libcontrol.netty

import android.os.SystemClock
import android.util.Log
import com.rhizo.libcontrol.util.NettyTcpCommand
import com.rhizo.libcontrol.util.PackageSplitter
import com.rhizo.libcontrol.util.XXByteArray
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.bytes.ByteArrayEncoder
import io.netty.handler.timeout.IdleStateHandler
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * 与服务器通信的客户端
 */
object NettyTcpCloudClient {
    private val TAG = NettyTcpCloudClient.javaClass.simpleName
    private var mChannel: Channel? = null
    private var mListener: NettyTcpClientListener? = null
    private var mBootstrap: Bootstrap? = null
    private var mEventLoopGroup: NioEventLoopGroup? = null
    private var mIp = ""
    private var mPort = 0
    private var mReconnectFlag = true
    private val mWorkerThread = Executors.newSingleThreadExecutor()
    private var mState = TcpConstants.ConnectStatus.STATUS_NONE


    fun connect(ip: String, port: Int, listener: NettyTcpClientListener) {
        mIp = ip
        mPort = port
        mListener = listener
        mWorkerThread.execute {
            connectTcp()
        }
    }

    private fun connectTcp() {
        mState = TcpConstants.ConnectStatus.STATUS_CONNECTING
        synchronized(NettyTcpCloudClient::class.java) {
            Log.d(TAG, "发起了Netty connect")
            try {
                mReconnectFlag = true
                mEventLoopGroup = NioEventLoopGroup()
                mBootstrap = Bootstrap()
                val config = InetSocketAddress(mIp, mPort)
                mBootstrap?.group(mEventLoopGroup)?.channel(NioSocketChannel::class.java)
                    ?.remoteAddress(config)
                    ?.option(ChannelOption.TCP_NODELAY, true)
                    ?.handler(object : ChannelInitializer<SocketChannel>() {
                        override fun initChannel(socketChannel: SocketChannel?) { //副屏连接主屏
                            if (socketChannel != null) {
                                val pipeline = socketChannel.pipeline()
                                pipeline.addLast(PackageSplitter())
                                pipeline.addLast(ByteArrayEncoder())
                                pipeline.addLast(IdleStateHandler(0, 0, 30, TimeUnit.SECONDS))
                                pipeline.addLast(NettyTcpCloudClientHandler(mListener))
                            }
                        }
                    })
                val channelFuture = mBootstrap?.connect()?.sync() ?: return
                mChannel = channelFuture.channel()
                mChannel?.closeFuture()?.sync()
            } catch (e: InterruptedException) {
                Log.e(TAG, "InterruptedException:${e.message}")
                mListener?.onConnectStatusCallback(
                    TcpConstants.ConnectStatus.STATUS_ERROR,
                    "云端 InterruptedException ${e.message}"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception:${e.message}")
                mListener?.onConnectStatusCallback(
                    TcpConstants.ConnectStatus.STATUS_ERROR,
                    "云端 Exception ${e.message}"
                )
            } finally {
                if (mChannel?.isOpen == true) {
                    mChannel?.close()
                }
                mEventLoopGroup?.shutdownGracefully()
                if (mReconnectFlag) {
                    reconnect()
                }
            }
        }
    }

    /**
     * 发送数据
     */
    private fun sendData(channel: Channel, data: ByteArray, listener: ChannelFutureListener) {
        channel.writeAndFlush(data).addListener(listener)
    }

    /**
     * 发送数据(包含分片)
     */
    fun sendMessage(
        command: Int,
        json: String,
        listener: ChannelFutureListener? = null
    ) {
        val channel = mChannel ?: return
        if (!channel.isActive) {
            return
        }
        val callback = listener
            ?: ChannelFutureListener { future ->
                if (future?.isSuccess == true) {
                    Log.d(
                        TAG,
                        "sendMessage Success->command:$command"
                    )
                }
            }

        Log.d(TAG, "sendMessage command:$command")
        val dataHex = XXByteArray.toHeX(json.toByteArray())
        var data: ByteArray

        if (dataHex.length > 40000) {
            val dataHexList = XXByteArray.splitStringByLength(dataHex, 40000)
            for (i in dataHexList.indices) {
                data = NettyTcpCommand.getCommand(
                    command,
                    dataHexList[i],
                    dataHexList.size,
                    i + 1
                )
                sendData(channel, data, callback)
            }
        } else {
            data = NettyTcpCommand.getCommand(command, dataHex)
            sendData(channel, data, callback)
        }
    }

    /**
     * 断开
     */
    fun disconnect(reconnect: Boolean = true) {
        Log.e(TAG, "disconnect")
        mReconnectFlag = reconnect
        mEventLoopGroup?.shutdownGracefully()
        mChannel?.closeFuture()
    }

    /**
     * 重连
     */
    private fun reconnect() {
        Log.d(TAG, "reconnect")
        SystemClock.sleep(3000)
        connectTcp()
    }

    fun setState(state: Int) {
        mState = state
    }

    fun getState(): Int {
        return mState
    }

}