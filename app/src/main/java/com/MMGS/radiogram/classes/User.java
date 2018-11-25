package com.MMGS.radiogram.classes;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class User {

    private String uName;
    private String uSurname;
    private String uEmail;
    private String uPassword;
    private String uGroup;
    private String uKurs;
    private String uBirthday;
    private String uTelephone;
    private String uAdres;
    private Integer uColor;
    private long lastAddMobTime;
    private String rank;
    private String think;
    private Map<String,Achievement> achievements;
    private Map<String,Integer> chats;
    private Map<String,Friend> subs;
    private Map<String,Friend> mySubs;
    private Map<String,Friend> friends;
    private Map<String,String> liked;

    public User(String uName,String uSurname, String uEmail, String uPassword, int uColor) {
        this.uSurname = uSurname;
        this.uName = uName;
        this.uEmail = uEmail;
        this.uPassword = uPassword;
        this.lastAddMobTime = 0;
        this.uGroup = "non";
        this.uKurs = "non";
        this.uBirthday = "00.00.0000";
        this.uAdres = "non";
        this.uTelephone = "non";
        this.uColor = uColor;
        this.rank = "user";
        this.think = "non";
        this.achievements = new HashMap<>();
        chats = new HashMap<>();
        achievements.put("alpha",new Achievement("alpha", Achievement.CLOSED_ACHIEVEMENT, 1));
        achievements.put("beta",new Achievement("beta", Achievement.CLOSED_ACHIEVEMENT, 1));
        achievements.put("background_edit",new Achievement("background_edit", Achievement.CLOSED_ACHIEVEMENT, 1));
        achievements.put("messages_x5",new Achievement("messages_x5", Achievement.CLOSED_ACHIEVEMENT, 5));
        achievements.put("messages_x25",new Achievement("messages_x25", Achievement.CLOSED_ACHIEVEMENT, 25));
        achievements.put("messages_x100",new Achievement("messages_x100", Achievement.CLOSED_ACHIEVEMENT, 100));
        chats.put("-LOxTLOZFrZ9mHPXp8aQ", 0);
    }

    public Map<String, String> getLiked() {
        return liked;
    }

    public void setLiked(Map<String, String> liked) {
        this.liked = liked;
    }

    public Map<String, Friend> getFriends() {
        return friends;
    }

    public void setFriends(Map<String, Friend> friends) {
        this.friends = friends;
    }

    public Map<String, Integer> getChats() {
        return chats;
    }

    public void setChats(Map<String, Integer> chats) {
        this.chats = chats;
    }

    public void setAchievements(Map<String, Achievement> achievements) {
        this.achievements = achievements;
    }

    public Map<String, Friend> getSubs() {
        return subs;
    }

    public void setSubs(Map<String, Friend> subs) {
        this.subs = subs;
    }

    public Map<String, Friend> getMySubs() {
        return mySubs;
    }

    public void setMySubs(Map<String, Friend> mySubs) {
        this.mySubs = mySubs;
    }

    public User(){}

    public Map<String, Achievement> getAchievements() {
        return achievements;
    }
    public void addAchievements(Map<String, Achievement> achievements) {
        this.achievements.putAll(achievements);
    }
    public String getRank() {
        return rank;
    }
    public void setRank(String rank) {
        this.rank = rank;
    }
    public long getLastAddMobTime() {
        return lastAddMobTime;
    }
    public void setLastAddMobTime(long lastAddMobTime) {
        this.lastAddMobTime = lastAddMobTime;
    }
    public String getuName() {
        return uName;
    }
    public void setuName(String uName) {
        this.uName = uName;
    }
    public String getuSurname() {
        return uSurname;
    }
    public void setuSurname(String uSurname) {
        this.uSurname = uSurname;
    }

    public String getuEmail() {
        return uEmail;
    }

    public void setuEmail(String uEmail) {
        this.uEmail = uEmail;
    }

    public String getuPassword() {
        return uPassword;
    }

    public void setuPassword(String uPassword) {
        this.uPassword = uPassword;
    }

    public String getuGroup() {
        return uGroup;
    }

    public void setuGroup(String uGroup) {
        this.uGroup = uGroup;
    }

    public String getuKurs() {
        return uKurs;
    }

    public void setuKurs(String uKurs) {
        this.uKurs = uKurs;
    }

    public String getuBirthday() {
        return uBirthday;
    }

    public void setuBirthday(String uBirthday) {
        this.uBirthday = uBirthday;
    }

    public String getuTelephone() {
        return uTelephone;
    }

    public void setuTelephone(String uTelephone) {
        this.uTelephone = uTelephone;
    }

    public String getuAdres() {
        return uAdres;
    }

    public void setuAdres(String uAdres) {
        this.uAdres = uAdres;
    }

    public Integer getuColor() {
        return uColor;
    }

    public void setuColor(Integer uColor) {
        this.uColor = uColor;
    }

    public String getThink() {
        return think;
    }

    public void setThink(String think) {
        this.think = think;
    }
}
