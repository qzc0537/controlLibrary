package com.rhizo.libtcp.netty

import io.netty.channel.Channel

data class ClientChannel(
    var clientIp: String,//客户端ip
    var channel: Channel,//与客户端建立的通道
    var shortId: String //通道的唯一标示
)