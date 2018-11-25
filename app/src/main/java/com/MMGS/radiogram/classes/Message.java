package com.MMGS.radiogram.classes;

import android.content.Context;
import android.text.format.DateFormat;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Андрюшка on 24.02.2018.
 */

public class Message {

    private ArrayList<String> messageText;
    private String messageUser;
    private String userID;
    private boolean isMessageNewInDay;
    private long time;
    private boolean isLast;


    public Message(ArrayList<String> messageText, String messageUser, String userID, long time, boolean isMessageNewInDay) {
        this.isLast = true;
        this.messageText = messageText;
        this.messageUser = messageUser;
        this.userID = userID;
        this.time = time;
        this.isMessageNewInDay = isMessageNewInDay;
    }

    public Message(){

    }

    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean last) {
        isLast = last;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isMessageNewInDay() {
        return isMessageNewInDay;
    }

    public void setMessageNewInDay(boolean messageNewInDay) {
        isMessageNewInDay = messageNewInDay;
    }

    public ArrayList<String> getMessageText() {
        return messageText;
    }

    public void setMessageText(ArrayList<String> messageText) {
        this.messageText = messageText;
    }

    public void addMessageText(String message){this.messageText.add(message); }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

}
