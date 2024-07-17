package com.rhizo.libcontrol.netty

import android.util.Log
import com.rhizo.libcontrol.util.XXByteArray
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent

/**
 * 客户端连接回调
 */
class NettyTcpCloudClientHandler(private var mClientListener: NettyTcpClientListener?) :
    SimpleChannelInboundHandler<String>() {
    private val TAG = NettyTcpCloudClientHandler::class.java.simpleName
    private var mChannelHandlerContext: ChannelHandlerContext? = null
    private val mDataMap = mutableMapOf<String, String>()


    override fun channelWritabilityChanged(ctx: ChannelHandlerContext?) {
        super.channelWritabilityChanged(ctx)
    }

    override fun channelRead0(ctx: ChannelHandlerContext?, frameHex: String?) {
        if (!frameHex.isNullOrEmpty()) {
            if (frameHex.length < 38) {
                Log.e(TAG, "frameHex is invalid")
                return
            }
            val commandHex = frameHex.substring(4, 8)
            val frameAmount = frameHex.substring(12, 16).toInt(16)
            val frameIndex = frameHex.substring(16, 20).toInt(16)
            val timeSecond = frameHex.substring(20, 28).toInt(16)
            val timeMills = timeSecond * 1000L
            if (System.currentTimeMillis() - timeMills > 10000) {
                Log.e(TAG, "指令过期")
                return
            }
            val contentHex = frameHex.substring(32, frameHex.length - 6)
            val data = XXByteArray.fromHex(contentHex)
            val json = String(data)

            if (frameAmount == 1) {
                Log.d(TAG, "channelRead0 command:$commandHex")
                mClientListener?.onMessageReceived(commandHex.toInt(), json)
            } else {
                Log.d(TAG, "channelRead0 command:$commandHex $frameIndex/$frameAmount")
                if (mDataMap.containsKey(commandHex)) {
                    mDataMap[commandHex] += json
                } else {
                    mDataMap[commandHex] = json
                }
                if (frameIndex == frameAmount) {
                    mClientListener?.onMessageReceived(commandHex.toInt(), mDataMap[commandHex] ?: "")
                    mDataMap.remove(commandHex)
                }
            }
        } else {
            Log.e(TAG, "云端无效指令 $frameHex")
        }
    }

    override fun channelActive(ctx: ChannelHandlerContext?) {
        Log.d(TAG, "client channelActive")
        mChannelHandlerContext = ctx
        mClientListener?.onConnectStatusCallback(
            TcpConstants.ConnectStatus.STATUS_CONNECTED,
            "客户端已连接"
        )
    }

    override fun channelInactive(ctx: ChannelHandlerContext?) {
        Log.e(TAG, "client channelInactive")
        mClientListener?.onConnectStatusCallback(
            TcpConstants.ConnectStatus.STATUS_DISCONNECTED,
            "安卓已断开"
        )
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        Log.e(TAG, "client exceptionCaught：${cause?.message ?: ""}")
        cause?.message?.let {
            mClientListener?.onConnectStatusCallback(
                TcpConstants.ConnectStatus.STATUS_ERROR,
                it
            )
        }
        cause?.printStackTrace()
        ctx?.close()
    }

    override fun userEventTriggered(ctx: ChannelHandlerContext?, evt: Any?) {
        if (evt is IdleStateEvent) {
            if (evt.state() == IdleState.ALL_IDLE) {
                NettyTcpClient.sendMessage(
                    TcpConstants.CloudCommand.HEART_BEAT,
                    "{\"heartbeat\":0}"
                )
            }
        }
    }
}