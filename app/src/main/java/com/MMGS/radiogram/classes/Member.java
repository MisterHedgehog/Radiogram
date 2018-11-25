package com.MMGS.radiogram.classes;

public class Member {

    private int lastMessage;
    private boolean inChat;

    public Member(){
        this.inChat = false;
        lastMessage = 0;
    }

    public int getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(int lastMessage) {
        this.lastMessage = lastMessage;
    }

    public boolean isInChat() {
        return inChat;
    }

    public void setInChat(boolean inChat) {
        this.inChat = inChat;
    }
}
