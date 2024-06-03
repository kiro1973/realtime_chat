package com.example.splitapplication.ui.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static android.content.Context.MODE_PRIVATE;

import static androidx.core.content.ContextCompat.registerReceiver;

import com.example.splitapplication.R;
import com.example.splitapplication.ServerStatusChecker;
import com.example.splitapplication.SocketSingleton;
import com.example.splitapplication.TimeReceiver;
import com.example.splitapplication.ui.home.modules.clanAdapter;
import com.example.splitapplication.ui.home.modules.clanGetSet;

import io.socket.client.Socket;


@SuppressLint("Registered")
public class bottomFragClan extends  Fragment implements clanAdapter.OnItemClickListener {

    public String TAG="bottomFragClan: ";

    private RecyclerView mRecyclerView;
    private clanAdapter mAdapter;

    private ProgressBar mProgressCircle;

    private List<clanGetSet> mClanGetSet;

    ImageView no_friend_image;
    TextView no_friend_text;

    private Socket mSocket;
    Intent timeIntent;// to handle each minute change the time ago
    private TimeReceiver timeBr;
     Button favBtn;

    private ServerStatusChecker serverStatusChecker;
    SwipeRefreshLayout mSwipeRefreshLayout;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,  Bundle savedInstanceState) {

        mSocket = SocketSingleton.getInstance();
        serverStatusChecker = new ServerStatusChecker();

        // Start monitoring server status
        serverStatusChecker.startCheckingServerStatus();
        final View view = inflater.inflate(R.layout.bottom_frag_myclan, container, false);

        SharedPreferences User_getSharedPrefrence = getActivity().getSharedPreferences("Setting", MODE_PRIVATE);
        final String user_child= User_getSharedPrefrence.getString("Always_User_Child","!!!!");

        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayout = new LinearLayoutManager(getActivity());
        linearLayout.setReverseLayout(false);
        linearLayout.setSmoothScrollbarEnabled(true);
        mRecyclerView.setLayoutManager(linearLayout);

        mSwipeRefreshLayout=view.findViewById(R.id.swipeRefresh);
        no_friend_image=view.findViewById(R.id.no_friend_image);
        no_friend_text = view.findViewById(R.id.no_friend_text);

        no_friend_text.setVisibility(view.GONE);
        no_friend_image.setVisibility(view.GONE);

        mProgressCircle = view.findViewById(R.id.progress_circle);

        mClanGetSet = new ArrayList<>();

        mAdapter = new clanAdapter(mActivity, mClanGetSet);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(bottomFragClan.this);
        mClanGetSet.clear();
        //add user 1
        clanGetSet clanGetSet = new clanGetSet();

        //no_friend_image.setVisibility(View.INVISIBLE);
        //no_friend_text.setVisibility(View.INVISIBLE);
        mSocket.on("chats", onChatsReceived);
        mSocket.emit("getChats", "63a55e6d3a23b7a4b28590db");

        if(mAdapter.getItemCount()==0) {
            mProgressCircle.setVisibility(View.VISIBLE);
            mSwipeRefreshLayout.setRefreshing(true);
            no_friend_image.setVisibility(View.VISIBLE);
            no_friend_text.setVisibility(View.VISIBLE);
        }

        mAdapter.notifyDataSetChanged();
        //mProgressCircle.setVisibility(View.INVISIBLE);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
                mSocket.emit("getChats", "63a55e6d3a23b7a4b28590db");
                mAdapter.notifyDataSetChanged();
            }
        });
        return view;
    }

    void refreshItems() {
        onItemsLoadComplete();
    }

    void onItemsLoadComplete() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private Activity mActivity;


    @Override
    public void onResume() {
        super.onResume();
        timeBr=new TimeReceiver(mAdapter);
        IntentFilter timeFilter =new IntentFilter(Intent.ACTION_TIME_TICK);
        requireActivity().registerReceiver(timeBr, timeFilter);
        mSocket.on("chats", onChatsReceived);
        mSocket.emit("getChats", "63a55e6d3a23b7a4b28590db");
        mAdapter.notifyDataSetChanged();


    }

    @Override
    public void onPause() {
        super.onPause();
        mSocket.off("chats", onChatsReceived);
        if (timeBr != null) {
            requireActivity().unregisterReceiver(timeBr);
            timeBr = null;
        }
    }

    private Emitter.Listener onChatsReceived = args -> {
        mActivity.runOnUiThread(() -> {
            JSONArray chatListArray = (JSONArray) args[0];
            Log.d("chatListArray",chatListArray.toString());
            mClanGetSet.clear();
            for (int i = 0; i < chatListArray.length(); i++) {
                try {
                    JSONObject chatJson = chatListArray.getJSONObject(i);
                    clanGetSet chat = new clanGetSet( //mehtagin nezawwed 1-isThereUnReadMessage 2-isChatFavorite
                            chatJson.getString("chatId"),
                            chatJson.getString("friendName"),
                            chatJson.getString("friendImage"),
                            chatJson.getString("lastMessage"),
                            chatJson.getString("lastMessageTimeStamp"),
                            chatJson.getBoolean("isChatFavorite"), // Extract boolean directly
                            chatJson.getBoolean("isThereUnreadMessages") // Extract boolean directly
                                     );
                    mClanGetSet.add(chat);
                    if (mClanGetSet.size()>0){
                        no_friend_image.setVisibility(View.INVISIBLE);
                        no_friend_text.setVisibility(View.INVISIBLE);
                    }
                    else {
                        no_friend_image.setVisibility(View.VISIBLE);
                        no_friend_text.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            mAdapter.notifyDataSetChanged();
            mSwipeRefreshLayout.setRefreshing(false);
            mProgressCircle.setVisibility(View.INVISIBLE);
        });
    };





    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    private void doAction() {
        if (mActivity == null) {
            return;
        }
    }

    public void onItemClick(int position) {
        Log.v(TAG, "Normal click at position:"  + position);
    }

    public void onGetReport(int position) {
        clanGetSet selectedItem = mClanGetSet.get(position);

    }
    public void onGetDeleteClick(int position) {
        final clanGetSet selectedItem = mClanGetSet.get(position);
    }

}