package com.example.splitapplication;
import android.util.Log;

import io.socket.client.IO;
import io.socket.client.Socket;

import java.net.URISyntaxException;
import android.content.SharedPreferences;

public class SocketSingleton {
    private static final String SERVER_URL = "http://10.0.2.2:3000";

        private static final String USER_ID_PREFERENCE = "user_id_preference";

    private static Socket mSocket;

    private SocketSingleton() {
        // Private constructor to prevent instantiation
    }

    public static synchronized Socket getInstance() {
//        if (mSocket == null) {
//            try {
//                mSocket = IO.socket("http://10.0.2.2:3000");
//            } catch (URISyntaxException e) {
//                e.printStackTrace();
//            }

//        }
        if (mSocket == null) {
            try {
                // Retrieve user ID from SharedPreferences using the provided context

                // Create options for Socket.IO connection
                IO.Options options = new IO.Options();
                options.forceNew = true; // Force a new connection instead of reusing an existing one

                // Initialize the Socket instance with the server URL and options
                mSocket = IO.socket(SERVER_URL, options);

                // Handle connection event
                mSocket.on(Socket.EVENT_CONNECT, args -> {
                    // Connection successful, emit "saveSocketId" event with the user ID
                    mSocket.emit("saveSocketId", "63a55e6d3a23b7a4b28590db");
                });

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            // Connect to the Socket.IO server
            mSocket.connect();
        }
        return mSocket;
    }
    public static synchronized void disconnect() {
        if (mSocket != null && mSocket.connected()) {
            mSocket.disconnect();
            mSocket=null;
            Log.d("SocketStatus","disconnected");
        }
    }
}