package com.example.splitapplication;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.splitapplication.ui.home.modules.chatroomAdapter;
import com.example.splitapplication.ui.home.modules.clanAdapter;

import java.util.Calendar;

public class TimeReceiver extends BroadcastReceiver {
    private clanAdapter mAdapter;
    private chatroomAdapter chatAdapter;
    public TimeReceiver(clanAdapter adapter) {
        mAdapter = adapter;
    }
    public void setChatRoomAdapter(chatroomAdapter chatAdapter){

    }
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Calendar calendar=Calendar.getInstance();
        int minute=calendar.get(Calendar.MINUTE);
        mAdapter.notifyDataSetChanged();

        Log.d("BroadCast_Receiver ","receiver triggered");

    }

    }