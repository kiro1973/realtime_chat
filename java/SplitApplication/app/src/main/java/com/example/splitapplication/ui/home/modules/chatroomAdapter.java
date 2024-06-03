package com.example.splitapplication.ui.home.modules;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.splitapplication.R;
import com.example.splitapplication.SocketSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.socket.client.Socket;


public class chatroomAdapter extends RecyclerView.Adapter<chatroomAdapter.ImageViewHolder> {
    public static final int MSG_Left=0;
    public static final int MSG_Right=1;
    SharedPreferences settings ;
    private Context mContext;
    private List<chatroomGetSet> mchat;
    String mImaged="!";
    String currenUserId;
    Socket mSocket;

    public chatroomAdapter(Context context, List<chatroomGetSet> chat, String imaged,String currenUserId) {
        mContext = context;
        mchat = chat;
        //mImaged = imaged;
        this.currenUserId=currenUserId;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == MSG_Right) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.chat_message_right,parent, false);
            return new ImageViewHolder(v);
        }else {
            View v = LayoutInflater.from(mContext).inflate(R.layout.chat_message_left,parent, false);
            return new ImageViewHolder(v);
        }


    }


    @Override
    public void onBindViewHolder(ImageViewHolder holder, final int position) {
        final chatroomGetSet chat = mchat.get(position);
        mSocket= SocketSingleton.getInstance();
        String Message_e= chat.getm();
        holder.show_msg.setText(Message_e);
        String SenderImage= mchat.get(position).getImage();
         if(mContext!=null) {
             Glide
                     .with(mContext)
                     .load(SenderImage)
                     .thumbnail(0.2f)
                     .apply(RequestOptions.circleCropTransform())
                     .into(holder.imageView);
         }
        holder.show_date.setText(mchat.get(position).getMessageTimeAgo()); // We made a change here to be dynamic



        final int MENU_COPY_ID = R.id.action_copy;

        holder.show_msg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupMenu popupMenu = new PopupMenu(mContext, v);
                popupMenu.inflate(R.menu.context_menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.action_copy) {
                            String text = holder.show_msg.getText().toString();
                            ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("Copied Text", text);
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(mContext, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
                            return true;
                        } else if (item.getItemId() == R.id.action_delete) {




                            try {
                                // Create a Socket.IO instance


                                // Create a JSON object for the message
                                JSONObject messageJson = new JSONObject();
                                messageJson.put("messageId",chat.getMessageId() );
                                messageJson.put("userId", currenUserId);


                                // Emit the message to the "sendMessage" service
                                mSocket.emit("deleteMessage", messageJson);


                            } catch ( JSONException e) {
                                e.printStackTrace();
                            }

                            return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
                return true; // Return true to indicate that the long click event is consumed
            }
        });




    }

    @Override
    public int getItemCount() {
        return mchat.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder  {
        public TextView show_msg;
        public ImageView imageView;
        public TextView show_date;


        public ImageViewHolder(View itemView) {
            super(itemView);

            show_date=itemView.findViewById(R.id.userChatDate);
            show_msg = itemView.findViewById(R.id.userMessage);
            imageView = itemView.findViewById(R.id.userProfileImage);

        }

    }

    @Override
    public   int getItemViewType(int position){

        String sender= mchat.get(position).getSenderId();
        if (!sender.equals(currenUserId)){
            return  MSG_Right;
        } else {
            return MSG_Left;
        }
    }

}

