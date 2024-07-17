/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rhizo.libcontrol.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;

import com.rhizo.bluetooth.bluetooth.OnBluetoothUICallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;


/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
@SuppressLint("MissingPermission")
public class BluetoothServer {
    // Debugging
    private static final String TAG = BluetoothServer.class.getSimpleName();
    private static BluetoothServer mInstance;

    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothServerSecure";
    private static final String NAME_INSECURE = "BluetoothServerInsecure";

    // Member fields
    private final BluetoothAdapter mAdapter;
    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private int mState;
    private int mNewState;

    //所有接入的客户端连接线程
    private final HashMap<String, ConnectedThread> mClientConnectedThread = new HashMap<>();
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final LinkedHashSet<WeakReference<OnBluetoothUICallback>> mOnBluetoothUICallbacks = new LinkedHashSet<>();

    /**
     * Constructor. Prepares a new BluetoothChat session.
     */
    private BluetoothServer() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = BluetoothConst.STATE_NONE;
        mNewState = mState;
    }

    public static BluetoothServer getInstance() {
        if (mInstance == null) {
            synchronized (BluetoothServer.class) {
                if (mInstance == null) {
                    mInstance = new BluetoothServer();
                }
            }
        }
        return mInstance;
    }

    public BluetoothServer addCallback(OnBluetoothUICallback callback) {
        mOnBluetoothUICallbacks.add(new WeakReference<>(callback));
        return this;
    }

    public BluetoothServer removeCallback(OnBluetoothUICallback callback) {
        Iterator<WeakReference<OnBluetoothUICallback>> iterator = mOnBluetoothUICallbacks.iterator();
        while (iterator.hasNext()) {
            WeakReference<OnBluetoothUICallback> reference = iterator.next();
            if (reference.get() != null && reference.get() == callback) {
                iterator.remove();
                reference.clear();
            }
        }
        return this;
    }

    /**
     * Update UI title according to the current state of the chat connection
     */
    private synchronized void updateUserInterface() {
        mState = getState();
        BluetoothLog.i(TAG, "updateUserInterfaceTitle() " + mNewState + " -> " + mState);
        mNewState = mState;

        // Give the new state to the Handler so the UI Activity can update
//        mHandler.obtainMessage(BluetoothConst.MESSAGE_STATE_CHANGE, mNewState, -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        BluetoothLog.i(TAG, "start");

        //关闭所有客户端线程
        for (ConnectedThread connectedThread : mClientConnectedThread.values()) {
            connectedThread.cancel();
        }
        mClientConnectedThread.clear();
        // Start the thread to listen on a BluetoothServerSocket
        /*if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread(true);
            mSecureAcceptThread.start();
        }*/
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread(false);
            mInsecureAcceptThread.start();
        }
        // Update UI title
        updateUserInterface();
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        BluetoothLog.i(TAG, "connected, device address:" + device.getAddress());

        // Start the thread to manage the connection and perform transmissions
        ConnectedThread connectedThread = new ConnectedThread(socket, device);
        connectedThread.start();
        mClientConnectedThread.put(device.getAddress(), connectedThread);

        // Send the name of the connected device back to the UI Activity
       /* Message msg = mHandler.obtainMessage(BluetoothConst.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothConst.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);*/
        mHandler.post(() -> {
            for (WeakReference<OnBluetoothUICallback> reference : mOnBluetoothUICallbacks) {
                if (reference.get() != null) {
                    reference.get().onBluetoothConnected(device);
                }
            }
        });
        // Update UI title
        updateUserInterface();
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        BluetoothLog.i(TAG, "stop");

        //关闭所有客户端线程
        for (ConnectedThread connectedThread : mClientConnectedThread.values()) {
            connectedThread.cancel();
        }
        mClientConnectedThread.clear();
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
        mState = BluetoothConst.STATE_NONE;
        // Update UI title
        updateUserInterface();
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[], boolean)
     */
    public void write(String clientDeviceAddress, byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != BluetoothConst.STATE_CONNECTED) return;
            r = mClientConnectedThread.get(clientDeviceAddress);
        }
        // Perform the write unsynchronized
        if (r != null) {
            r.write(out, true);
        } else {
            mHandler.post(() -> {
                for (WeakReference<OnBluetoothUICallback> reference : mOnBluetoothUICallbacks) {
                    if (reference.get() != null) {
                        reference.get().onBluetoothWriteDataFailed("当前客户端已断开");
                    }
                }
            });
        }
    }

    /**
     * 服务端发布公告
     *
     * @param out
     */
    public void writeToAllClient(byte[] out) {
        for (Map.Entry<String, ConnectedThread> item : mClientConnectedThread.entrySet()) {
            item.getValue().write(out, true);
        }
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        // Send a failure message back to the Activity
       /* Message msg = mHandler.obtainMessage(BluetoothConst.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothConst.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);*/

        mState = BluetoothConst.STATE_NONE;
        // Update UI title
        updateUserInterface();

        // Start the service over to restart listening mode
        BluetoothServer.this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // Send a failure message back to the Activity
       /* Message msg = mHandler.obtainMessage(BluetoothConst.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothConst.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);*/

        mState = BluetoothConst.STATE_NONE;
        // Update UI title
        updateUserInterface();

        // Start the service over to restart listening mode
        BluetoothServer.this.start();
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread(boolean secure) {
            BluetoothServerSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Create a new listening server socket
            try {
                if (secure) {
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,
                            BluetoothConst.INSTANCE.getMY_UUID_SECURE());
                } else {
                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(
                            NAME_INSECURE, BluetoothConst.INSTANCE.getMY_UUID_INSECURE());
                }
            } catch (IOException e) {
                BluetoothLog.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
            }
            mmServerSocket = tmp;
            mState = BluetoothConst.STATE_LISTEN;
        }

        public void run() {
            BluetoothLog.i(TAG, "Socket Type: " + mSocketType + ", BEGIN AcceptThread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (true) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    if (mmServerSocket != null) {
                        socket = mmServerSocket.accept();
                    } else {
                        BluetoothLog.d(TAG, "mmServerSocket is null");
                        break;
                    }
                } catch (IOException e) {
                    BluetoothLog.e(TAG, "Socket Type: " + mSocketType + " accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    connected(socket, socket.getRemoteDevice());
                }
            }
            BluetoothLog.i(TAG, "END Accept Thread, socket Type: " + mSocketType);
        }

        public void cancel() {
            BluetoothLog.i(TAG, "Socket Type" + mSocketType + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                BluetoothLog.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        //连接的蓝牙设备，此处为客户端
        private final BluetoothDevice mmBluetoothDevice;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket, BluetoothDevice bluetoothDevice) {
            BluetoothLog.i(TAG, "create ConnectedThread: " + bluetoothDevice.getAddress());
            mmSocket = socket;
            mmBluetoothDevice = bluetoothDevice;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                BluetoothLog.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mState = BluetoothConst.STATE_CONNECTED;
        }

        public void run() {
            BluetoothLog.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;
            // Keep listening to the InputStream while connected
            while (mState == BluetoothConst.STATE_CONNECTED) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    String message = new String(buffer, 0, bytes);
                    BluetoothLog.i(TAG, "message:" + message);

                    mHandler.post(() -> {
                        for (WeakReference<OnBluetoothUICallback> reference : mOnBluetoothUICallbacks) {
                            if (reference.get() != null) {
                                reference.get().onBluetoothMessage(message);
                            }
                        }
                    });
                } catch (IOException e) {
                    BluetoothLog.e(TAG, "disconnected", e);
                    mClientConnectedThread.remove(mmBluetoothDevice.getAddress());
                    //connectionLost();
                    mHandler.post(() -> {
                        for (WeakReference<OnBluetoothUICallback> reference : mOnBluetoothUICallbacks) {
                            if (reference.get() != null) {
                                reference.get().onBluetoothDisConnected();
                            }
                        }
                    });
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer     The bytes to write
         * @param isUpdateUI 控制是否作为输出方打印到UI界面，转发消息给其他客户端时不需要
         */
        public void write(byte[] buffer, boolean isUpdateUI) {
            try {
                mmOutStream.write(buffer);

                if (isUpdateUI) {
                    // Share the sent message back to the UI Activity
                 /*   mHandler.obtainMessage(BluetoothConst.MESSAGE_WRITE, -1, -1, buffer)
                            .sendToTarget();*/
                    mHandler.post(() -> {
                        for (WeakReference<OnBluetoothUICallback> reference : mOnBluetoothUICallbacks) {
                            if (reference.get() != null) {
                                reference.get().onBluetoothWriteDataSuccess();
                            }
                        }
                    });
                }
            } catch (IOException e) {
                BluetoothLog.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                if (mmInStream != null) {
                    mmInStream.close();
                }
                if (mmOutStream != null) {
                    mmOutStream.close();
                }
                if (mmSocket != null) {
                    mmSocket.close();
                }
            } catch (IOException e) {
                BluetoothLog.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
