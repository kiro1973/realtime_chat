package com.example.splitapplication;

import android.util.Log;

import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

import io.socket.client.IO;
import io.socket.client.Socket;

public class ServerStatusChecker {

    private static final String SERVER_URL = "http://10.0.2.2:3000";
    private static final long CHECK_INTERVAL_MS = 5000; // Check every 5 seconds
    Socket socket;
    private Timer timer;

    public void startCheckingServerStatus() {
        socket = SocketSingleton.getInstance();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkServerStatus();
            }
        }, 0, CHECK_INTERVAL_MS);
    }

    private void checkServerStatus() {
        Log.d("checkServerStatus","5 seconds");
        try {

            if (!socket.connected()){
                //socket.disconnect();
               // socket=null;
                Log.d("called from check status","singleton null");
            }
        } catch (Exception e) {
            // Server is down or unreachable
            SocketSingleton.disconnect(); // Disconnect the Socket
            e.printStackTrace();
        }

    }
}
