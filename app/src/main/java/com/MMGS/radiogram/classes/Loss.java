package com.MMGS.radiogram.classes;

public class Loss {

    private String userName;
    private String userID;
    private String key;
    private String name;
    private String date;
    private String description;
    private long time;

    public Loss(String userName, String userID, String name, String date, String key, String description){
        this.userName = userName;
        this.userID = userID;
        this.name = name;
        this.date = date;
        this.key = key;
        this.description = description;
        this.time = 0;

    }

    public Loss() { }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
