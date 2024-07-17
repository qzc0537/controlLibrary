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
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.rhizo.bluetooth.bluetooth.OnBluetoothUICallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
@SuppressLint("MissingPermission")
public class BluetoothClient {
    // Debugging
    private static final String TAG = BluetoothClient.class.getSimpleName();
    private static BluetoothClient mInstance;

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private int mNewState;
    private final LinkedHashSet<WeakReference<OnBluetoothUICallback>> mOnBluetoothUICallbacks = new LinkedHashSet<>();

    /**
     * Constructor. Prepares a new BluetoothChat session.
     */
    private BluetoothClient() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = BluetoothConst.STATE_NONE;
        mNewState = mState;
    }

    public static BluetoothClient getInstance() {
        if (mInstance == null) {
            synchronized (BluetoothClient.class) {
                if (mInstance == null) {
                    mInstance = new BluetoothClient();
                }
            }
        }
        return mInstance;
    }

    public BluetoothClient addCallback(OnBluetoothUICallback callback) {
        mOnBluetoothUICallbacks.add(new WeakReference<>(callback));
        return this;
    }

    public BluetoothClient removeCallback(OnBluetoothUICallback callback) {
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
    private synchronized void updateUserInterfaceTitle() {
        mState = getState();
        BluetoothLog.d(TAG, "updateUserInterfaceTitle() " + mNewState + " -> " + mState);
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
        BluetoothLog.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Update UI title
        updateUserInterfaceTitle();
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device, boolean secure) {
        BluetoothLog.d(TAG, "connect to: " + device);
        if (!mAdapter.isEnabled()) {
            return;
        }
        mReconnect = true;

        // Cancel any thread attempting to make a connection
        if (mState == BluetoothConst.STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();
        // Update UI title
        updateUserInterfaceTitle();
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device,
                                       final String socketType) {
        BluetoothLog.d(TAG, "connected, Socket Type:" + socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        /*Message msg = mHandler.obtainMessage(BluetoothConst.MESSAGE_DEVICE_NAME);
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
        updateUserInterfaceTitle();
    }

    private boolean mReconnect = true;

    /**
     * Stop all threads
     */
    public synchronized void stop(boolean reconnect) {
        BluetoothLog.d(TAG, "stop");
        mReconnect = reconnect;

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mState = BluetoothConst.STATE_NONE;
        // Update UI title
        updateUserInterfaceTitle();
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != BluetoothConst.STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
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
        mHandler.post(() -> {
            for (WeakReference<OnBluetoothUICallback> reference : mOnBluetoothUICallbacks) {
                if (reference.get() != null) {
                    reference.get().onBluetoothConnectFailed();
                }
            }
        });

        mState = BluetoothConst.STATE_NONE;
        // Update UI title
        updateUserInterfaceTitle();

        // Start the service over to restart listening mode
        BluetoothClient.this.start();
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
        mHandler.post(() -> {
            for (WeakReference<OnBluetoothUICallback> reference : mOnBluetoothUICallbacks) {
                if (reference.get() != null) {
                    reference.get().onBluetoothDisConnected();
                }
            }
        });

        mState = BluetoothConst.STATE_NONE;
        // Update UI title
        updateUserInterfaceTitle();

        // Start the service over to restart listening mode
        if (mReconnect) {
            BluetoothClient.this.start();
        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if (secure) {
                    tmp = device.createRfcommSocketToServiceRecord(
                            BluetoothConst.INSTANCE.getMY_UUID_SECURE());
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(
                            BluetoothConst.INSTANCE.getMY_UUID_INSECURE());
                }
            } catch (IOException e) {
                BluetoothLog.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
            mState = BluetoothConst.STATE_CONNECTING;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (Exception e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (Exception e2) {
                    BluetoothLog.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothClient.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (Exception e) {
                BluetoothLog.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            BluetoothLog.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
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
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (mState == BluetoothConst.STATE_CONNECTED) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    String message = new String(buffer, 0, bytes);
                    Log.i(TAG, "message:" + message);

                    // Send the obtained bytes to the UI Activity
                 /*   mHandler.obtainMessage(BluetoothConst.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();*/
                    mHandler.post(() -> {
                        for (WeakReference<OnBluetoothUICallback> reference : mOnBluetoothUICallbacks) {
                            if (reference.get() != null) {
                                reference.get().onBluetoothMessage(message);
                            }
                        }
                    });
                } catch (IOException e) {
                    BluetoothLog.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                /*mHandler.obtainMessage(BluetoothConst.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();*/
                mHandler.post(() -> {
                    for (WeakReference<OnBluetoothUICallback> reference : mOnBluetoothUICallbacks) {
                        if (reference.get() != null) {
                            reference.get().onBluetoothWriteDataSuccess();
                        }
                    }
                });
            } catch (IOException e) {
                BluetoothLog.e(TAG, "Exception during write", e);
                mHandler.post(() -> {
                    for (WeakReference<OnBluetoothUICallback> reference : mOnBluetoothUICallbacks) {
                        if (reference.get() != null) {
                            reference.get().onBluetoothWriteDataFailed(e.getMessage() == null ? "" : e.getMessage());
                        }
                    }
                });
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
