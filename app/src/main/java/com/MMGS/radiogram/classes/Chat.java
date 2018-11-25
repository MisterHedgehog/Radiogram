package com.MMGS.radiogram.classes;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Chat {

    private String title;
    private String key;
    private Map<String,Member> members;
    private boolean isGroupChat;

    public Chat(){}
    // Конструктор создаёт чат на сервере и ссылку у каждого собеседника на этот чат
    // Чат на двух человек
    public Chat(final Map<String, Member> members){
        this.key = FirebaseDatabase.getInstance().getReference().child("chats").push().getKey();
        this.members = members;
        this.isGroupChat = false;
        this.title = "";
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users");
        for(final String m : members.keySet()) {
            reference.child(m).child("chats").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot chat : dataSnapshot.getChildren()) {
                        if (!chat.getKey().equals("-LOxTLOZFrZ9mHPXp8aQ")) {
                            reference.child(m).child("chats").child(chat.getKey()).setValue(chat.getValue(Integer.class) + 1);
                        }
                        reference.child(m).child("chats").child(key).setValue(1);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
        FirebaseDatabase.getInstance().getReference().child("chats").child(key).setValue(this);
    }
    // Групповой чат
    public Chat(final Context context, final Map<String, Member> members, String title, Bitmap bitmap, final DownloadListener listener){
        this.key = FirebaseDatabase.getInstance().getReference().child("chats").push().getKey();
        this.members = members;
        this.isGroupChat = true;
        this.title = title;
        Toast.makeText(context, String.valueOf(members.size()), Toast.LENGTH_SHORT).show();
        FirebaseManager.saveImage(context, bitmap, "chats", key, "icon", listener);
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users");
            for(final String m : members.keySet()){
                reference.child(m).child("chats").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            if (dataSnapshot.exists())
                                for (DataSnapshot chat : dataSnapshot.getChildren()) {
                                    if (!chat.getKey().equals("-LOxTLOZFrZ9mHPXp8aQ")) {
                                        reference.child(m).child("chats").child(chat.getKey()).setValue(chat.getValue(Integer.class) + 1);
                                    }
                                    reference.child(m).child("chats").child(key).setValue(1);
                                }
                            else {
                                reference.child(m).child("chats").child("-LOxTLOZFrZ9mHPXp8aQ").setValue(0);
                                reference.child(m).child("chats").child(key).setValue(1);
                            }
                            if (m.equals(members.keySet().toArray()[members.size() - 1])) {
                            }
                        } catch (Exception e){
                            Toast.makeText(context, "Error: Ошибка создания группового диалога\n Java: Chat\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        FirebaseDatabase.getInstance().getReference().child("chats").child(key).setValue(this).addOnSuccessListener(aVoid -> Toast.makeText(context, "Новый чат добавлен!", Toast.LENGTH_SHORT).show());
    }
    public boolean isGroupChat() {
        return isGroupChat;
    }
    public void setGroupChat(boolean groupChat) {
        isGroupChat = groupChat;
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public Map<String, Member> getMembers() {
        return members;
    }

    public void setMembers(Map<String, Member> members) {
        this.members = members;
    }
}
