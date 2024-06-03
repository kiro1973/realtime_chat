package com.example.splitapplication.ui.home.modules;


import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.splitapplication.R;
import com.example.splitapplication.SocketSingleton;
import com.example.splitapplication.chatRoomTimeReceiver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class chatroom extends AppCompatActivity  implements TextToSpeech.OnInitListener{

    public static final String currentUserPreference = "user_id_preference";//this is userid saved in mongo It should be returned form server upon login
    SharedPreferences settings ;
    SharedPreferences.Editor editor;
    String myMsg="!";
    EditText ChatMsg;
    Button ChatBtn,Backing ;
    String comb="!";
    TextView me,you,clock;

    String Hey;

    String TAG="chatroom: ";

    TextToSpeech textToSpeech;

    private ProgressBar mProgressCircle;
    private chatRoomTimeReceiver timeBr;
    private RecyclerView mRecyclerView;
    private chatroomAdapter mAdapter;
    private Socket mSocket;
    private List<chatroomGetSet> mchat;
    private static final int Total_load_chat =10;
    public int Current_page=1;
    ImageView img_y;
    private int item_no=0;
    private String send_Ready="true";
    String selected_User_imaged="!";
    String selected_User_child="!";
    String selected_Chat_id="!";
    SwipeRefreshLayout mSwipeRefreshLayout;
    String selected_User_nick;
    Intent intent;
    String currentUserId;
    String currentUserImage;

    private TextView speakButton;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.chat_room);

        SharedPreferences sp = getApplicationContext().getSharedPreferences("Setting", MODE_PRIVATE);
        settings = getApplicationContext().getSharedPreferences(currentUserPreference, 0);
        editor = settings.edit();
        mSocket = SocketSingleton.getInstance();
        currentUserId = settings.getString("uid", "1234");
        currentUserImage = settings.getString("myimage", "");

        //set rotation settings
        if (sp.getBoolean("rotate", false)) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        intent = getIntent();
        selected_User_child = intent.getStringExtra("user_name");
        selected_User_nick = intent.getStringExtra("user_nick");
        selected_User_imaged = intent.getStringExtra("imaged");
        selected_Chat_id = intent.getStringExtra("chatId");
        mSocket.emit("markChatAsRead",currentUserId,selected_Chat_id);
        img_y = findViewById(R.id.userProfileImage);


        LinearLayout menu = findViewById(R.id.menu_layout);
        Backing = findViewById(R.id.backing);
        Backing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        speakButton = findViewById(R.id.button_voice);
        speakButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                askSpeechInput();
            }
        });

        textToSpeech = new TextToSpeech(getApplicationContext(), this, "com.google.android.tts");
        you = findViewById(R.id.chatUserName);
        Glide
                .with(getApplicationContext())
                .load(selected_User_imaged)
                .thumbnail(0.1f)
                .apply(RequestOptions.circleCropTransform())
                .into(img_y);
        you.setText(selected_User_child);

        mSwipeRefreshLayout = findViewById(R.id.swipe_RefreshLayout);

        mRecyclerView = findViewById(R.id.chat_recycle);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayout = new LinearLayoutManager(getApplicationContext());
        linearLayout.setReverseLayout(false);
        linearLayout.setStackFromEnd(true);
        linearLayout.setSmoothScrollbarEnabled(true);
        mRecyclerView.setLayoutManager(linearLayout);

        mProgressCircle = findViewById(R.id.chat_circle);

        ChatBtn = findViewById(R.id.chat_send);
        ChatMsg = findViewById(R.id.chat_str);
        clock = findViewById(R.id.clock);

        ChatMsg.setVisibility(View.VISIBLE);
        clock.setVisibility(View.INVISIBLE);

        mchat = new ArrayList<>();
        mAdapter = new chatroomAdapter(chatroom.this, mchat, selected_User_imaged, currentUserId);
        mRecyclerView.setAdapter(mAdapter);

        ChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myMsg = String.valueOf(ChatMsg.getText());
                if (send_Ready.equals("true")) {
                    if (!ChatMsg.getText().toString().trim().equals("")) {
                        long timer = (long) (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
                        try {
                            // Create a Socket.IO instance


                            // Create a JSON object for the message
                            JSONObject messageJson = new JSONObject();
                            messageJson.put("chatId", selected_Chat_id);
                            messageJson.put("userId", currentUserId);
                            messageJson.put("message", myMsg);

                            // Emit the message to the "sendMessage" service
                            mSocket.emit("sendMessage", messageJson);


                        } catch ( JSONException e) {
                            e.printStackTrace();
                        }
                        senders(myMsg);
                        send_Ready = "false";
                        new CountDownTimer(4000, 1000) {

                            @SuppressLint("SetTextI18n")
                            public void onTick(long millisUntilFinished) {
                                clock.setVisibility(View.VISIBLE);
                                ChatBtn.setVisibility(View.INVISIBLE);
                                clock.setText("" + millisUntilFinished / 1000);
                            }

                            public void onFinish() {
                                send_Ready = "true";
                                clock.setVisibility(View.INVISIBLE);
                                ChatBtn.setVisibility(View.VISIBLE);
                            }

                        }.start();

                    } else {
                        Toast.makeText(getApplicationContext(), "Please Write your Message!", Toast.LENGTH_SHORT).show();
                    }
                } else {

                    Toast.makeText(getApplicationContext(), "Please Wait for 5 seconds!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        read();
        timeBr=new chatRoomTimeReceiver(mAdapter);
        IntentFilter timeFilter =new IntentFilter(Intent.ACTION_TIME_TICK);
        registerReceiver(timeBr, timeFilter);


        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                item_no = 0;
                Current_page++;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                               // readMore();
                                mSwipeRefreshLayout.setRefreshing(false);
                                mProgressCircle.setVisibility(View.INVISIBLE);
                            }
                        }, 1000);
                    }
                });
            }
        });
        //speakOut("Hey");

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("menu clicked","clicked on menu");
                PopupMenu popupMenu = new PopupMenu(chatroom.this, menu);
                popupMenu.getMenuInflater().inflate(R.menu.chat_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.remove) {
                            SharedPreferences User_getSharedPrefrence = getApplicationContext().getSharedPreferences("Setting", MODE_PRIVATE);
                            String user_child = User_getSharedPrefrence.getString("Always_User_Child", "!!!!");


                            JSONObject data = new JSONObject();
                            try {
                                // Add userId and gName to the JSONObject
                                data.put("userId", currentUserId);
                                data.put("chatId", selected_Chat_id);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

// Emit the JSON object
                            mSocket.emit("removeChat", data);



                            finish();
                        } else {
                            //Send Mail
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

    }

    //////////////////////////////////////Send_message///////////////////////////////////////////////////

    public void readMore() {

        if (mchat.size() == 0) {
            mProgressCircle.setVisibility(View.INVISIBLE);
            mSwipeRefreshLayout.setRefreshing(false);
        }

        chatroomGetSet chatroomGetSet = new chatroomGetSet();
        chatroomGetSet.setSenderId("4321");
        chatroomGetSet.setm("Thanks you for adding me as friend.");

        chatroomGetSet.setImage("https://a.storyblok.com/f/191576/1200x800/215e59568f/round_profil_picture_after_.webp");
        mchat.add(chatroomGetSet);
        mAdapter.notifyDataSetChanged();
        mRecyclerView.scrollBy(5, 0);

    }

    public void senders(String chat) {
        ChatMsg.getText().clear();

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    //////////////////////////////////////read_message///////////////////////////////////////////////////
    public void read() {
        if (mchat.size() == 0) {
            mProgressCircle.setVisibility(View.INVISIBLE);
            mSwipeRefreshLayout.setRefreshing(false);
        }
        SharedPreferences sp = getApplicationContext().getSharedPreferences("Setting", MODE_PRIVATE);
        final SharedPreferences.Editor editor = sp.edit();

        //Add Chat 1
//        chatroomGetSet chatroomGetSet = new chatroomGetSet();
//        chatroomGetSet.setSenderId("1234");
//        chatroomGetSet.setm("Hello how are you?");
//        chatroomGetSet.setImage(selected_User_imaged);//I want this image if this message is of other user
//        item_no++;
//        mchat.add(chatroomGetSet);
//        mAdapter.notifyDataSetChanged();
//        //Add Chat 2
        mSocket.on("chatHistory", onChatHistoryReceived);
        mSocket.emit("getChatHistory",selected_Chat_id );


//        mRecyclerView.scrollToPosition(mchat.size() - 1);////////////MOMKEN NESHIL COMMENT HANGARRAN NEHOTTAHA TAHT FEL EVENT
//        mSwipeRefreshLayout.setRefreshing(false);/////////////////MOMKEN NESHIL COMMENT HANGARRAN NEHOTTAHA TAHT FEL EVENT
//        mProgressCircle.setVisibility(View.INVISIBLE);   ///////MOMKEN NESHIL COMMENT HANGARRAN NEHOTTAHA TAHT FEL EVENT
    }

    private void askSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Hi! Say Your Message");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(),
                    "Speech Not Supported",
                    Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Speech Not Supported: " + e);
        }
    }

    // Receiving speech input
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    ChatMsg.setText(result.get(0));
                }
                break;
            }

        }
    }

    @Override
    public void onDestroy() {

        textToSpeech.shutdown();

        super.onDestroy();
    }

    @Override
    public void onInit(int status) {

        SharedPreferences sp = getApplicationContext().getSharedPreferences("Setting", MODE_PRIVATE);

        SharedPreferences User_getSharedPrefrence = getApplicationContext().getSharedPreferences("Setting", MODE_PRIVATE);
        String user_child = User_getSharedPrefrence.getString("Always_User_Child", "!!!!");

        if (sp.getBoolean(user_child + "speak", true) == true) {

            if (status == TextToSpeech.SUCCESS) {
                Set<String> a = new HashSet<>();

                String locale = getApplicationContext().getResources().getConfiguration().locale.getCountry();

                String Voices;
                if (selected_User_nick == null || selected_User_nick.equals("")) {
                    selected_User_nick = " ";
                }
                if (selected_User_nick.trim().length() >= 1 && (selected_User_nick.trim().substring(0, 1).equals("M"))) {
                    a.add("male");
                    Voices = "en-us-x-sfg#male_2-local";
                } else {
                    a.add("female");
                    Voices = "en-us-x-sfg#female_2-local";
                }


                Voice v = new Voice(Voices.trim(), new Locale(Locale.getDefault().getLanguage(), locale), 4000, 200, true, a);

                textToSpeech.setVoice(v);
                textToSpeech.setSpeechRate(0.9f);

                int result = textToSpeech.setVoice(v);

                if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.i(TAG, "This Language Changed");
                    result = textToSpeech.setLanguage(Locale.US);
                }

                if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w(TAG, "This Language is not supported");
                } else {
                    speakOut("Hey");
                }

            } else {
                Log.e(TAG, "This Language initialization failed!");
            }
        }
    }






    private Emitter.Listener onChatHistoryReceived = args -> {
        runOnUiThread(() -> {
            JSONArray chatListArray = (JSONArray) args[0];
            mchat.clear();
            Log.d("JSON msg array length",String.valueOf(chatListArray.length() ));
            Log.d("messagesList",chatListArray.toString());

            for (int i = 0; i < chatListArray.length(); i++) {
                try {

                    JSONObject chatJson = chatListArray.getJSONObject(i);
                    Log.d("chatJson.message",chatJson.getString("message"));
                    Log.d("chatJson.from",chatJson.getString("from"));
                    Log.d("chatJson.timeStamp",chatJson.getString("timestamp"));
                    chatroomGetSet chat = new chatroomGetSet(
                            chatJson.getString("message"),
                            chatJson.getString("from"),
                            chatJson.getString("timestamp"),

                            chatJson.getString("_id")

                    );
                    String ImageToPut = (chat.getSenderId().equals(currentUserId) ) ? currentUserImage : selected_User_imaged;
                    chat.setImage(ImageToPut);

                    //Log.d("chat Image",chat.getImage());
//                    Log.d("currentUserId",currentUserId);
//                    Log.d("currentUserImage",currentUserImage);
//                    Log.d("selected_User_imaged",selected_User_imaged);
//                    Log.d("chat.getSenderId()",chat.getSenderId());
                    mchat.add(chat);
                    //Log.d("mchat in if",mchat.toString());
                    if (mchat.size()>0){
//                        no_friend_image.setVisibility(View.INVISIBLE);
//                        no_friend_text.setVisibility(View.INVISIBLE);
                        //Log.d("mchat in if",mchat.toString());
                        Log.d("mchat is not empty","mchat is not empty");
                        mRecyclerView.scrollToPosition(mchat.size() - 1);////////////MOMKEN NESHIL COMMENT HANGARRAN NEHOTTAHA TAHT FEL EVENT
                        mSwipeRefreshLayout.setRefreshing(false);/////////////////MOMKEN NESHIL COMMENT HANGARRAN NEHOTTAHA TAHT FEL EVENT
                        mProgressCircle.setVisibility(View.INVISIBLE);/////////////////MOMKEN NESHIL COMMENT HANGARRAN NEHOTTAHA TAHT FEL EVENT

                    }
                    else {
                        Log.d("mchat is empty", "mchat is empty");
//                        no_friend_image.setVisibility(View.VISIBLE);
//                        no_friend_text.setVisibility(View.VISIBLE);
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










    private void speakOut(String message) {

        textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null);
    }

}