package com.rhizo.libtcp.util;

import android.util.Log;

import com.rhizo.libtcp.bean.PackageSplitterDto;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * 拆包器
 *
 * @Author: november
 * @CreateTime: 2021/12/22 10:28 上午
 * @UpdateTIme:
 */
public class PackageSplitter extends ByteToMessageDecoder {
    private static final String TAG = "PackageSplitter";
    private static final String PROXY_TCP4 = "50524F58592054435034203";

    // 用来临时保留没有处理过的请求报文
    ByteBuf tempMsg = Unpooled.buffer();

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
//        byte[] data = ByteBufUtil.getBytes(byteBuf);
//        String hex = FuncUtil.ByteArrToHex(data);
//        AdLog.INSTANCE.d("hex:" + hex);
        if (byteBuf.readableBytes() <= 0) {
            return;
        }
        //超过约定的数据长度就关闭通道
        if (byteBuf.readableBytes() > 1024 * 65) {
            Log.e(TAG, "超过约定的数据长度就关闭通道");
            channelHandlerContext.close();
        } else {
            //输出
//            if (SystemUtil.isWindows()) {
//                byte[] re = Unpooled.copiedBuffer(byteBuf).array();
//                log.info("当前收到:" + BytesUtil.bytesToHex(re));
//            }

            // 合并报文，如果暂存有上一次余下的请求报文，则合并
            ByteBuf message = Unpooled.buffer();
            if (!split(message, tempMsg, byteBuf, list, channelHandlerContext)) {
                return;
            }
            byteBuf.skipBytes(byteBuf.readableBytes());
            // 多余的报文存起来
            // 第一个报文： i+  暂存
            // 第二个报文： 1 与第一次
            int size = message.readableBytes();
            tempMsg.clear();
            if (size != 0) {
                // 剩下来的数据放到tempMsg暂存
                message.retain();
                tempMsg.writeBytes(message.readBytes(size));
            }
        }
    }

    public boolean split(ByteBuf message, ByteBuf tempMsg, ByteBuf currentMsg, List<Object> list, ChannelHandlerContext channelHandlerContext) {
        //如果有暂存下来的数据则先读取旧数据
        if (tempMsg.readableBytes() > 0) {
            message.writeBytes(tempMsg);
        }
        message.writeBytes(currentMsg);
        byte[] receive = ByteBufUtil.getBytes(message);
//        if (receive.length >= 5) {
//            byte[] bytesType = BytesUtil.subBytes(receive, 0, 5);
//            if ("check".equals(new String(bytesType))) {
//                channelHandlerContext.writeAndFlush(Unpooled.copiedBuffer((CommonEnum.ChannelType.TCP_SERVER.getName() + "-" + SystemUtil.getHostAddress() + ":" + CommonEnum.ChannelType.TCP_SERVER.getPort()).getBytes(StandardCharsets.UTF_8)));
//                return false;
//            }
//        }
        PackageSplitterDto back;
        byte[] cmd;
        String frameHex;
        while (true) {
            back = PackageSplitUtil.doSplit(receive);
            if (back != null) {
                cmd = CmdCalibrateUtil.calibrate(back.getOut());
                frameHex = XXByteArray.INSTANCE.removeSpace(XXByteArray.INSTANCE.bytesToSpaceHex(cmd));
                list.add(frameHex);
                receive = back.getNext();
                //读掉这一部分
                message.readBytes(back.getOut());
            } else {
                break;
            }
        }
        return true;
    }


}
