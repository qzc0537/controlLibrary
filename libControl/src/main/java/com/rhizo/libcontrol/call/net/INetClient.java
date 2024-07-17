package com.rhizo.libcontrol.call.net;

/**
 * author : Jiyf
 * e-mail : ffaa30703@icloud.com
 * time   : 2020/06/15
 * desc   :
 * version: 1.0
 */
public interface INetClient {

    void send(Message message);

    void setReceiveHandler(IReceiveHandler receiveHandler);

    void setRemote(String targetIp, int targetPort);

    void close();


    interface IReceiveHandler {
        void onReceived(Message message, String senderIp, int senderPort);
    }


}
