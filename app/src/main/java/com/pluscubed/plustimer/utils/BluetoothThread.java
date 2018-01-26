package com.pluscubed.plustimer.utils;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothThread extends Thread {
    private static final char DELIMITER = '\n';
    private static final String TAG = "BluetoothThread";
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final String address;
    private InputStream inStream;
    private OutputStream outStream;
    private final Handler readHandler;
    private String rx_buffer = "";
    private BluetoothSocket socket;
    private final Handler writeHandler;

    @SuppressLint("HandlerLeak")
    public BluetoothThread(String address, Handler handler) {
        this.address = address.toUpperCase();
        this.readHandler = handler;
        this.writeHandler = new Handler() {
            public void handleMessage(Message message) {
                BluetoothThread.this.write((String) message.obj);
            }
        };
    }

    public Handler getWriteHandler() {
        return this.writeHandler;
    }

    private void connect() throws Exception {
        Log.i(TAG, "Attempting connection to " + this.address + "...");
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            throw new Exception("Bluetooth adapter not found or not enabled!");
        }
        this.socket = adapter.getRemoteDevice(this.address).createRfcommSocketToServiceRecord(uuid);
        adapter.cancelDiscovery();
        this.socket.connect();
        this.outStream = this.socket.getOutputStream();
        this.inStream = this.socket.getInputStream();
        Log.i(TAG, "Connected successfully to " + this.address + ".");
    }

    private void disconnect() {
        if (this.inStream != null) {
            try {
                this.inStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (this.outStream != null) {
            try {
                this.outStream.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        if (this.socket != null) {
            try {
                this.socket.close();
            } catch (Exception e22) {
                e22.printStackTrace();
            }
        }
    }

    private String read() {
        Exception e;
        String s = "";
        try {
            if (this.inStream.available() > 0) {
                byte[] inBuffer = new byte[1024];
                int bytesRead = this.inStream.read(inBuffer);
                String s2 = new String(inBuffer, "ASCII");
                try {
                    s = s2.substring(0, bytesRead);
                } catch (Exception e2) {
                    e = e2;
                    s = s2;
                    Log.e(TAG, "Read failed!", e);
                    return s;
                }
            }
        } catch (Exception e3) {
            e = e3;
            Log.e(TAG, "Read failed!", e);
            return s;
        }
        return s;
    }

    private void write(String s) {
        try {
            this.outStream.write(s.getBytes());
            Log.i(TAG, "[SENT] " + s);
        } catch (Exception e) {
            Log.e(TAG, "Write failed!", e);
        }
    }

    private void sendToReadHandler(String s) {
        Message msg = Message.obtain();
        msg.obj = s;
        this.readHandler.sendMessage(msg);
        Log.i(TAG, "[RECV] " + s);
    }

    private void parseMessages() {
        int inx = this.rx_buffer.indexOf(10);
        if (inx != -1) {
            String s = this.rx_buffer.substring(0, inx);
            this.rx_buffer = this.rx_buffer.substring(inx + 1);
            sendToReadHandler(s);
            parseMessages();
        }
    }

    public void run() {
        try {
            connect();
            sendToReadHandler("CONNECTED");
            while (!isInterrupted()) {
                if (this.inStream == null || this.outStream == null) {
                    Log.e(TAG, "Lost bluetooth connection!");
                    break;
                }
                String s = read();
                if (s.length() > 0) {
                    this.rx_buffer += s;
                }
                parseMessages();
            }
            disconnect();
            sendToReadHandler("DISCONNECTED");
        } catch (Exception e) {
            Log.e(TAG, "Failed to connect!", e);
            sendToReadHandler("CONNECTION FAILED");
            disconnect();
        }
    }
}
