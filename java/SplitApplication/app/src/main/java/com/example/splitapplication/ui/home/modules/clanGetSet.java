package com.example.splitapplication.ui.home.modules;

import androidx.annotation.Keep;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Keep
public class clanGetSet {

    public String name;
    public String image;
    public String lastMessage;
    public String lastMessageTimeStamp;
    public String mKey;
    public String chatId;
    public boolean isChatFavorite,isThereUnreadMessages;

    public clanGetSet() {
    }
    public clanGetSet(String chatId,String name, String image,String lastMessage,String lastMessageTimeStamp,boolean isChatFavorite,boolean isThereUnreadMessages) {
        this.chatId=chatId;
        this.name = name;
        this.image = image;
        this.lastMessage = lastMessage;
        this.lastMessageTimeStamp = lastMessageTimeStamp;
        this.isChatFavorite=isChatFavorite;
        this.isThereUnreadMessages=isThereUnreadMessages;
    }

    public String getn() {
        return name;
    }

    public void setn(String n) {
        name = n;
    }


    public String getm() {
        return image;
    }

    public void setm(String m) {
        image = m;
    }


    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessageParameter) {
        lastMessage = lastMessageParameter;
    }

    public String getLastMessageTimeStamp() {
        return lastMessageTimeStamp;
    }

    public void setLastMessageTimeStamp(String lastMessageTimeStampParameter) {
        lastMessageTimeStamp = lastMessageTimeStampParameter;
    }
    public String getLastMessageTimeAgo(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        String timeAgo;
        try {
            Date date = format.parse(lastMessageTimeStamp);
            System.out.println("Parsed Date: " + date);

             timeAgo = calculateTimeAgo(date);
            System.out.println("Time ago: " + timeAgo);
            return(timeAgo.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return(lastMessageTimeStamp);
    }

    public static String calculateTimeAgo(Date date) {
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - date.getTime();

        // Calculate time difference in minutes
        long minutes = timeDifference / (1000 * 60);

        if (minutes < 60) {
            return minutes + " minutes ago";
        } else {
            long hours = minutes / 60;
            if (hours < 24) {
                return hours + " hours ago";
            } else {
                long days = hours / 24;
                return days + " days ago";
            }
        }
    }
    public String getKey() {
        return mKey;
    }

    public String getChatId() {
        return chatId;
    }
    public void setKey(String key) {
        mKey = key;
    }



    public boolean getIsChatFavorite() {
        return isChatFavorite;
    }
    public void setIsChatFavorite(boolean isChatFavorite) {
        this.isChatFavorite = isChatFavorite;
    }


    public boolean getIsThereUnreadMessages() {
        return isThereUnreadMessages;
    }
    public void setIsThereUnreadMessages(boolean isThereUnreadMessages) {
        this.isThereUnreadMessages = isThereUnreadMessages;
    }


}
////////////////////////////////////////////////////////////////////////////////////////////////////
/*
done
 */