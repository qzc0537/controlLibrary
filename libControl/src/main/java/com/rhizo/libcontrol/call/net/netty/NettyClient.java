package com.rhizo.libcontrol.call.net.netty;


import android.util.Log;

import com.google.gson.Gson;
import com.rhizo.libcontrol.call.net.INetClient;
import com.rhizo.libcontrol.call.net.Message;

import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * Describe: 使用netty框架构建音频传输客户端
 * 此代理只是监听本地端口，然后在发送数据的时候需要指定对方ip和对方端口
 */
public class NettyClient implements INetClient {
    private static final String TAG = "NettyClient";

    ChannelHandlerContext mChannelHandlerContext;

    private int localPort = 7777; //绑定本地端口
    private String targetIp = "127.0.0.1";//对方ip地址
    private int targetPort = 7777; // 对方端口
    private INetClient.IReceiveHandler mReceiveHandler;
    private final Gson gson = new Gson();
    private EventLoopGroup group;
    private Channel channel;


    //构造者模式进行构建ip和端口。
    public NettyClient(int localPort, IReceiveHandler receiveHandler) {
        this.localPort = localPort;
        this.mReceiveHandler = receiveHandler;
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        //初始化receiverHandler.
        //启动客户端进行发送数据
        new Thread(() -> {
            Bootstrap b = new Bootstrap();
            group = new NioEventLoopGroup();
            try {
                //设置netty的连接属性。
                b.group(group)
                        .channel(NioDatagramChannel.class) //异步的 UDP 连接
                        .option(ChannelOption.SO_REUSEADDR, true)
                        .option(ChannelOption.SO_BROADCAST, true)
                        .option(ChannelOption.SO_RCVBUF, 1024 * 1024)//接收区2m缓存
                        .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(65535))//加上这个，里面是最大接收、发送的长度
                        .handler(handler); //设置数据的处理器

                Log.d(TAG, "closeFuture().await()");
                channel = b.bind(localPort).sync().channel();
                channel.closeFuture().await();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "init() e:" + e.getMessage());
            } finally {
                group.shutdownGracefully();
                Log.e(TAG, "init() finally");
            }
        }).start();
    }

    private final SimpleChannelInboundHandler<DatagramPacket> handler = new SimpleChannelInboundHandler<DatagramPacket>() {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
            Log.d(TAG, "channelRead0() called with: ctx = [" + ctx + "], packet = [" + packet + "]");
            if (mReceiveHandler == null) return;
            ByteBuf buf = (ByteBuf) packet.copy().content(); //字节缓冲区
            byte[] req = new byte[buf.readableBytes()];
            String senderIp = packet.sender().getAddress().getHostAddress();
            int senderPort = packet.sender().getPort();
            buf.readBytes(req);
            String str = new String(req, "UTF-8");
            Message message = gson.fromJson(str, Message.class);
            mReceiveHandler.onReceived(message, senderIp, senderPort);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            Log.d(TAG, "channelActive() called with: ctx = [" + ctx + "]");
            mChannelHandlerContext = ctx;
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
            Log.e(TAG, "channelInactive() called with: ctx = [" + ctx + "]");
        }
    };

    @Override
    public void send(Message message) {
        String json = gson.toJson(message);
        Log.d(TAG, "send() json:" + json);
        if (mChannelHandlerContext == null) {
            Log.e(TAG, "send() mChannelHandlerContext is null");
            return;
        }
        mChannelHandlerContext.writeAndFlush(new DatagramPacket(
                Unpooled.copiedBuffer(json.getBytes()),
                new InetSocketAddress(targetIp, targetPort))
        );
    }

    @Override
    public void setReceiveHandler(IReceiveHandler receiveHandler) {
        mReceiveHandler = receiveHandler;
    }

    @Override
    public void setRemote(String targetIp, int targetPort) {
        this.targetIp = targetIp;
        this.targetPort = targetPort;
    }

    @Override
    public void close() {
        if (channel != null) {
            Log.d(TAG, "close()");
            group.shutdownGracefully();
            channel.disconnect();
            channel.closeFuture();
        }
    }
}
