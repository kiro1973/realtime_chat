package com.example.splitapplication.ui.home.modules;

import android.util.Log;

import androidx.annotation.Keep;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Keep
public class chatroomGetSet {
    public String messageText,senderId,image,messageTimeStamp,messageId;

    public chatroomGetSet(String messageText,String senderId,String messageTimeStamp,String messageId)
    {
        this.messageText=messageText;
        this.senderId=senderId;
        //this.image=image;
        this.messageTimeStamp=messageTimeStamp;
        this.messageId=messageId;
        Log.d("message Object created","Created Chat Object");

    }
    public chatroomGetSet(){
    }

    public String getm() { return messageText; }

    public void setm(String messageText) { this.messageText = messageText; }
    public String getImage() { return image; }

    public void setImage(String image) { this.image = image; }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
public String getMessageId(){
        return messageId;
}






    public String getMessageTimeAgo(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        String timeAgo;
        try {
            Date date = format.parse(messageTimeStamp);
            System.out.println("Parsed Date: " + date);

            timeAgo = calculateTimeAgo(date);
            System.out.println("Time ago: " + timeAgo);
            return(timeAgo.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return(messageTimeStamp);
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



////////////////////////////////////////////////////////////////////////////////////////////////////
/*
done

 */








}




