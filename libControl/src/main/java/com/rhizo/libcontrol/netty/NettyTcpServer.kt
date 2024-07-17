package com.rhizo.libtcp.netty

import android.os.SystemClock
import android.util.Log
import com.rhizo.libtcp.util.NettyTcpCommand
import com.rhizo.libtcp.util.PackageSplitter
import com.rhizo.libtcp.util.XXByteArray
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.bytes.ByteArrayEncoder
import java.util.concurrent.Executors


/**
 * 机器人端的服务端
 */
object NettyTcpServer {
    private val TAG = NettyTcpServer.javaClass.simpleName
    private lateinit var mListener: NettyTcpServerListener
    private var mBossGroup: EventLoopGroup? = null
    private var mWorkerGroup: EventLoopGroup? = null
    private var mChannel: Channel? = null
    private var mIsServerStart: Boolean = false
    private var mChannelActive: Boolean = false
    private var mInterruptSendData: Boolean = false //中断分片发送数据
    private var mServerIp = "" //ip地址
    private var mServerPort = 0 //端口
    private var mReconnectFlag = true //是否重连
    private val mWorkerThread = Executors.newSingleThreadExecutor()

    fun startServer(ip: String, port: Int, listener: NettyTcpServerListener) {
        mServerIp = ip
        mServerPort = port
        mListener = listener
        mReconnectFlag = true
        // 使用Thread线程进行异步连接
        mWorkerThread.execute {
            doServerRun()
        }
    }

    private fun doServerRun() {
        try {
            mBossGroup = NioEventLoopGroup(1)
            mWorkerGroup = NioEventLoopGroup()
            val serverBootstrap = ServerBootstrap()
            serverBootstrap.group(mBossGroup, mWorkerGroup)
                .channel(NioServerSocketChannel::class.java)
                .option(ChannelOption.SO_RCVBUF, 10)
                .option(ChannelOption.SO_SNDBUF, 10)
                .option(ChannelOption.SO_BACKLOG, 5).childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel?) {
                        val pipeline = ch?.pipeline()
                        var ip = ""
                        val remoteAddress = ch?.remoteAddress()?.toString() ?: ""
                        if (remoteAddress.isNotEmpty() && remoteAddress.contains(":")) {
                            ip = remoteAddress.split(":")[0]
                        }
                        pipeline?.addLast(PackageSplitter())
                        pipeline?.addLast(ByteArrayEncoder())
                        pipeline?.addLast(NettyTcpServerHandler(mListener))
                    }
                })
            val channelFuture = serverBootstrap.bind(mServerIp, mServerPort).sync()
            //XXFunction.i("started and listen on " + channelFuture.channel().localAddress())
            Log.d(TAG, "started and listen on:$mServerIp")
            mIsServerStart = true
            mListener.onStartServer()
            channelFuture.channel().closeFuture().sync()
        } catch (e: Exception) {
            e.localizedMessage?.let {
                Log.e(TAG, "开启服务异常：$it")
            }
            e.printStackTrace()
        } finally {
            mIsServerStart = false
            mListener.onStopServer()
            mWorkerGroup?.shutdownGracefully()
            mBossGroup?.shutdownGracefully()
            reconnect()
        }
    }

    /**
     * 断开
     */
    fun disconnect(reconnect: Boolean) {
        Log.e(TAG, "disconnect reconnect:$reconnect")
        mReconnectFlag = reconnect
        mWorkerGroup?.shutdownGracefully()
        mBossGroup?.shutdownGracefully()
        getChannel()?.disconnect()
        getChannel()?.closeFuture()
    }

    /**
     * 重连
     */
    private fun reconnect() {
        if (mReconnectFlag) {
            Log.d(TAG, "reconnect")
            SystemClock.sleep(3000)
            doServerRun()
        }
    }

    /**
     * 设置渠道是否活跃
     */
    fun setChannelActive(active: Boolean) {
        mChannelActive = active
        mInterruptSendData = !active
    }

    /**
     * 渠道是否活跃
     */
    fun getChannelActive(): Boolean {
        return mChannelActive
    }

    /**
     * 服务是否启动
     */
    fun isServerStart(): Boolean {
        return mIsServerStart
    }

    /**
     * 设置中断发送地图
     */
    fun setInterruptSendData(interrupt: Boolean) {
        mInterruptSendData = interrupt
    }

    /**
     * 是否中断发送地图
     */
    fun getInterruptSendData(): Boolean {
        return mInterruptSendData
    }

    /**
     * 当前连接渠道
     */
    fun getChannel(): Channel? {
        return mChannel
    }

    /**
     * 发送数据
     */
    private fun sendData(
        channel: Channel,
        bytes: ByteArray,
        listener: ChannelFutureListener
    ): Boolean {
        val flag = channel.isActive && getChannelActive()
        if (flag) {
            channel.writeAndFlush(bytes)?.addListener(listener)
        }
        return flag
    }

    /**
     * 发送数据(包含分片)
     */
    fun sendMessage(
        command: Int,
        json: String,
        clientChanel: ClientChannel,
        listener: ChannelFutureListener? = null
    ) {
        val callback = listener
            ?: ChannelFutureListener { future ->
                if (future?.isSuccess == true) {
                    Log.d(
                        TAG,
                        "sendMessage Success->command:$command ip:${clientChanel.clientIp}"
                    )
                }
            }

        Log.d(TAG, "sendMessage command:$command")
        val dataHex = XXByteArray.toHeX(json.toByteArray())
        var data: ByteArray

        if (dataHex.length > 40000) {
            val dataHexList = XXByteArray.splitStringByLength(dataHex, 40000)
            Log.e(TAG, "sendMessage 分片:${dataHexList.size}")
            for (i in dataHexList.indices) {
                if (mInterruptSendData) {
                    Log.e(TAG, "中断分片发送")
                    break
                }
                data =
                    NettyTcpCommand.getCommand(
                        command,
                        dataHexList[i],
                        dataHexList.size,
                        i + 1
                    )
                sendData(clientChanel.channel, data, callback)
            }
        } else {
            data = NettyTcpCommand.getCommand(command, dataHex)
            sendData(clientChanel.channel, data, callback)
        }
    }

}