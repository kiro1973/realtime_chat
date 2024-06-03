package com.example.splitapplication;

import android.app.Application;

import io.socket.client.IO;
import io.socket.client.Socket;

import java.net.URISyntaxException;

public class MySocket extends Application {

    private static Socket mSocket;

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            mSocket = IO.socket("http://10.0.2.2:3000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        // Connect the socket when the application starts
        if (mSocket != null && !mSocket.connected()) {
            mSocket.connect();
        }
    }

    public static Socket getSocket() {
        return mSocket;
    }
}
