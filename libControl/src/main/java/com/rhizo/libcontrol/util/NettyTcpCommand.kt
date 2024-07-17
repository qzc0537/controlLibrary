package com.rhizo.libcontrol.util

import com.rhizo.libcontrol.bean.NettyTcpEncode
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicInteger

object NettyTcpCommand {
    private val mAtomicInteger = AtomicInteger(0)

    private fun getOrderIndex(): Int {
        val value = mAtomicInteger.incrementAndGet()
        if (value > 40000) {
            mAtomicInteger.set(1)
            return 1
        }
        return value
    }

    fun contentHeartbeat(): String {
        return "00"
    }

    fun getCommand(
        commandNo: Int, //指令
        dataHex: String, //内容的16进制字符串
        frameAmount: Int = 1, //数据总帧数
        frameIndex: Int = 1 //数据序号
    ): ByteArray {
        val encode = NettyTcpEncode().apply {
            commandNoHex = commandNo.toString()
            if (frameAmount != 0) {
                frameAmountHex = XXByteArray.toHexOf2Bytes(frameAmount)
                frameIndexHex = XXByteArray.toHexOf2Bytes(frameIndex)
            }
            commandOrderHex = XXByteArray.toHexOf2Bytes(getOrderIndex())
            timestampHex = XXByteArray.toHexOf4Bytes(
                java.lang.Long.valueOf(System.currentTimeMillis() / 1000).toInt()
            )
            contentHex = dataHex
            contentLengthHex = XXByteArray.toHexOf2Bytes(contentHex.length / 2)
            val dataBeforeCRC = buildString {
                append(commandNoHex)
                append(commandOrderHex)
                append(frameAmountHex)
                append(frameIndexHex)
                append(timestampHex)
                append(contentLengthHex)
                append(contentHex)
            }
            val crcValueBytes = XXByteArray.unsignedShortTo2Byte(
                XXByteArray.crcCheckInt(
                    XXByteArray.hexToBytes(dataBeforeCRC)
                )
            )
            crcValueHex = XXByteArray.toHeX(crcValueBytes)
        }
//        AdLog.d("encode:$encode")

        val startBytes = XXByteArray.hexToBytes(encode.start)
        val endBytes = XXByteArray.hexToBytes(encode.end)
        val noStartEndBytes = XXByteArray.hexToBytes(
            encode.commandNoHex + encode.commandOrderHex + encode.frameAmountHex
                    + encode.frameIndexHex + encode.timestampHex + encode.contentLengthHex
                    + encode.contentHex + encode.crcValueHex
        )
        val reverseBytes = CmdCalibrateUtil.reverseCalibrate(noStartEndBytes)
        val sendBuffer = ByteBuffer.allocate(startBytes.size + endBytes.size + reverseBytes.size)
        sendBuffer.put(startBytes)
        sendBuffer.put(reverseBytes)
        sendBuffer.put(endBytes)

        return sendBuffer.array()
    }


}