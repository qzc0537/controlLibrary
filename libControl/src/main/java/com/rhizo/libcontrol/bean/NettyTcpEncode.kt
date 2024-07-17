package com.rhizo.libcontrol.bean

/**
 * @author HuFei
 * [ 解码协议 ]
 * start：帧头 FF FF 2 个字节
 * commandNoHex：业务指令 2 个字节
 * commandOrderHex：本指令累加计数 2 个字节
 * frameAmountHex：数据总帧数 2 个字节
 * frameIndexHex：本次数据的帧位置 2 个字节
 * timestampHex：时间戳 4 个字节
 * contentLengthHex：数据长度值 bodyLengthInt 2 个字节 (真实body的)
 * contentHex： 数据内容 bodyLengthInt 个字节
 * crcValueHex：CRC校验值 2 个字节
 * end：帧尾 FF 1 个字节
 * 正确的帧数据至少 2+2+2+2+2+4+2+0+2+1 = 19 个字节
 */
data class NettyTcpEncode(
    val start: String = "FFFF",
    var commandNoHex: String = "1000",
    var commandOrderHex: String = "04C3",
    var frameAmountHex: String = "0001",
    var frameIndexHex: String = "0001",
    var timestampHex: String = "64268A73",
    var contentLengthHex: String = "0000",
    var contentHex: String = "",
    var crcValueHex: String = "E425",
    val end: String = "FF"
)
