package com.rhizo.libcontrol.call.webrtc;

import com.google.gson.Gson;
import com.rhizo.libcall.net.INetClient;
import com.rhizo.libcall.net.Message;
import com.rhizo.libcall.net.netty.NettyClient;

import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;
import org.webrtc.VideoTrack;

/**
 * author : Jiyf
 * e-mail : ffaa30703@icloud.com
 * time   : 2020/06/15
 * desc   :
 * version: 1.0
 */
public class SignallingClient {
    public static final String CALLOUT = "callout";
    public static final String ANSWER = "answer";
    public static final String HUNGUP = "hungup";
    private static final String OFFER_SDP = "offer_sdp";
    private static final String ANSWER_SDP = "answer_sdp";
    private static final String ICE_CANDIDATE = "ice_candidate";
    private static final int PORT = 10001;
    static SignallingClient INSTANCE = null;
    public boolean isInitiator = false;
    public boolean isStarted;
    public boolean isChannelReady;

    INetClient mNetClient;

    Gson gson = new Gson();
    SignalingInterface mSignalingInterface;


    INetClient.IReceiveHandler mReceiveHandler = (message, senderIp, senderPort) -> {
        switch (message.getType()) {
            case CALLOUT:
                if (mSignalingInterface != null) {
                    mSignalingInterface.callIn(senderIp);
                }
                mNetClient.setRemote(senderIp, senderPort);
                break;
            case ANSWER:
                isChannelReady = true;
                if (mSignalingInterface != null) {
                    mSignalingInterface.receiveAnswer();
                }
                break;
            case OFFER_SDP: {
                SessionDescription sessionDescription = gson.fromJson(message.getInfo(), SessionDescription.class);
                if (mSignalingInterface != null) {
                    mSignalingInterface.receiveOfferSdp(sessionDescription);
                }
            }
            break;
            case ANSWER_SDP: {
                SessionDescription sessionDescription = gson.fromJson(message.getInfo(), SessionDescription.class);
                if (mSignalingInterface != null) {
                    mSignalingInterface.receiveAnswerSdp(sessionDescription);
                }
            }
            break;
            case ICE_CANDIDATE:
                IceCandidate iceCandidate = gson.fromJson(message.getInfo(), IceCandidate.class);
                if (mSignalingInterface != null) {
                    mSignalingInterface.receiveIceCandidate(iceCandidate);
                }
                break;
            case HUNGUP:
                if (mSignalingInterface != null) {
                    mSignalingInterface.receiveHangup();
                }
                break;

        }

    };

    public SignallingClient() {

    }

    public static SignallingClient getInstance() {
        if (INSTANCE == null)
            INSTANCE = new SignallingClient();
        return INSTANCE;
    }

    public void init(SignalingInterface signalingInterface) {
        mSignalingInterface = signalingInterface;
        mNetClient = new NettyClient(PORT, mReceiveHandler);
        isInitiator = true;
    }

    public void callOut(String targetIp) {
        mNetClient.setRemote(targetIp, PORT);
        mNetClient.send(new Message(CALLOUT, ""));

    }

    public void answer() {
        isChannelReady = true;
        mNetClient.send(new Message(ANSWER, ""));
    }

    public void sendOfferSdp(SessionDescription sessionDescription) {
        Message message = new Message(OFFER_SDP, gson.toJson(sessionDescription));

        mNetClient.send(message);
    }

    public void sendAnswerSdp(SessionDescription sessionDescription) {
        Message message = new Message(ANSWER_SDP, gson.toJson(sessionDescription));
        mNetClient.send(message);
    }

    public void sendIceCandidate(IceCandidate iceCandidate) {
        Message message = new Message(ICE_CANDIDATE, gson.toJson(iceCandidate));
//        message.setObject(iceCandidate);
        mNetClient.send(message);

    }

    public void sendHangup() {
        Message message = new Message(HUNGUP, "");
        mNetClient.send(message);
    }

    public void reset() {
        isStarted = false;
        isChannelReady = false;
        mNetClient.close();
    }



    public interface SignalingInterface {

        void callIn(String senderIp);

        void receiveAnswer();

        void receiveOfferSdp(SessionDescription sessionDescription);

        void receiveAnswerSdp(SessionDescription sessionDescription);

        void receiveIceCandidate(IceCandidate iceCandidate);

        void receiveHangup();

    }
}
