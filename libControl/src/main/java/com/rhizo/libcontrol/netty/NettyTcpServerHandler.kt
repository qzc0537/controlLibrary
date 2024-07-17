package com.rhizo.libtcp.netty

import android.util.Log
import com.rhizo.libtcp.util.XXByteArray
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.group.DefaultChannelGroup
import io.netty.util.concurrent.GlobalEventExecutor


@ChannelHandler.Sharable
class NettyTcpServerHandler(var mTcpServerListener: NettyTcpServerListener?) :
    SimpleChannelInboundHandler<String>() {
    private val TAG = "NettyTcpServerHandler"
    private val mChannelGroup = DefaultChannelGroup(GlobalEventExecutor.INSTANCE)
    private val mDataMap = mutableMapOf<String, String>()

    override fun channelRead0(ctx: ChannelHandlerContext?, msgHex: String?) {
        if (!msgHex.isNullOrEmpty()) {
            val commandHex = msgHex.substring(4, 8)
            val frameAmount = msgHex.substring(12, 16).toInt(16)
            val frameIndex = msgHex.substring(16, 20).toInt(16)
            val timeSecond = msgHex.substring(20, 28).toInt(16)
            val timeMills = timeSecond * 1000L
            if (System.currentTimeMillis() - timeMills > 10000) {
                Log.e(TAG, "指令过期")
                return
            }
            val contentHex = msgHex.substring(32, msgHex.length - 6)
            val data = XXByteArray.fromHex(contentHex)
            val json = String(data)

            if (frameAmount == 1) {
                Log.d(TAG, "channelRead0 command:$commandHex json:$json")
                mTcpServerListener?.onMessageResponseServer(commandHex.toInt(), json)
            } else {
                Log.d(TAG, "channelRead0 command:$commandHex $frameIndex/$frameAmount")
                if (mDataMap.containsKey(commandHex)) {
                    mDataMap[commandHex] += json
                } else {
                    mDataMap[commandHex] = json
                }
                if (frameIndex == frameAmount) {
                    mTcpServerListener?.onMessageResponseServer(commandHex.toInt(), mDataMap[commandHex] ?: "")
                    mDataMap.remove(commandHex)
                }
            }
        } else {
            Log.e(TAG, "硬件无效指令 $msgHex")
        }
    }

    /**
     * 表示服务端与客户端连接建立
     */
    override fun handlerAdded(ctx: ChannelHandlerContext?) {
        super.handlerAdded(ctx)
        val channel = ctx?.channel()

        /**
         * 调用channelGroup的writeAndFlush其实就相当于channelGroup中的每个channel都writeAndFlush
         *
         * 先去广播，再将自己加入到channelGroup中
         */
        mChannelGroup.writeAndFlush(" Server -" + channel?.remoteAddress() + " Add\n")
        mChannelGroup.add(channel)
    }

    /**
     * 客户端断开连接
     */
    override fun handlerRemoved(ctx: ChannelHandlerContext?) {
        super.handlerRemoved(ctx)
        val channel = ctx?.channel()
        mChannelGroup.writeAndFlush(" Server -" + channel?.remoteAddress() + " Leave\n")
        //验证一下每次客户端断开连接，连接自动地从channelGroup中删除调。
        println(mChannelGroup.size)
        //当客户端和服务端断开连接的时候，下面的那段代码netty会自动调用，所以不需要人为的去调用它
        //channelGroup.remove(channel);
    }

    /**
     * 在连接被建立并且准备进行通信时被调用。
     */
    override fun channelActive(ctx: ChannelHandlerContext?) {
        super.channelActive(ctx)
        val channel = ctx?.channel()
        Log.d(TAG, channel?.remoteAddress().toString() + " 上线了")
        mTcpServerListener?.onChannelConnect(channel!!)
        NettyTcpServer.setChannelActive(true)
        mTcpServerListener?.onServiceStatusConnectChanged(NettyTcpServerListener.STATUS_CONNECT_SUCCESS)
    }

    /**
     * 连接断开
     */
    override fun channelInactive(ctx: ChannelHandlerContext?) {
        super.channelInactive(ctx)
        val channel = ctx?.channel()
        Log.e(TAG, channel?.remoteAddress().toString() + " 下线了")
        mTcpServerListener?.onChannelDisConnect(channel!!)
        NettyTcpServer.setChannelActive(false)
        mTcpServerListener?.onServiceStatusConnectChanged(NettyTcpServerListener.STATUS_CONNECT_CLOSED)
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext?) {
        super.channelReadComplete(ctx)
        ctx?.fireChannelReadComplete()
        //XXFunction.i("channelReadComplete")
    }

    /**
     * exceptionCaught()事件处理方法是当出现Throwable对象才会被调用，
     * 即当Netty由于IO错误或者处理器在处理事件时抛出的异常时。
     * 在大部分情况下，捕获的异常应该被记录下来并且把关联的channel给关闭掉。
     */
    @Deprecated("Deprecated in Java")
    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        cause?.printStackTrace()
        ctx?.close()
        Log.e(TAG, "exceptionCaught:${cause?.message}")
    }
}