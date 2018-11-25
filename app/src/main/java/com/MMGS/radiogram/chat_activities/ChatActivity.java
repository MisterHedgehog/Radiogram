package com.MMGS.radiogram.chat_activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.MMGS.radiogram.R;
import com.MMGS.radiogram.classes.Chat;
import com.MMGS.radiogram.classes.FirebaseManager;
import com.MMGS.radiogram.classes.Friend;
import com.MMGS.radiogram.classes.Member;
import com.MMGS.radiogram.classes.Message;
import com.MMGS.radiogram.classes.User;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private FloatingActionButton mSendMessageButton;
    private EditText mInput;
    private ListView mListOfMessages;
    private TextView title;
    private CircleImageView chatImg;
    private CircleImageView imageOfNewChat;
    private Context context;

    private FirebaseUser mUser;
    private DatabaseReference chatRef;
    private DatabaseReference userRef;
    private DatabaseReference mMessageRef;
    private AppCompatActivity activity;

    private ArrayList<Friend> friendsForAdd;
    private ArrayList<String> members;
    private boolean isMessageSending;
    private String mUserName;
    private String mUserID;
    private String chatID;
    private MediaPlayer mMediaPlayer;
    private Bitmap bitmap;
    private Boolean[] friendsChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        SetViews();
        SetToolbar();
        AddVoiceToMessage();
        SetUserChat();
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                mUserName = user.getuName();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mSendMessageButton.setOnClickListener(view -> {
            if (!mInput.getText().toString().isEmpty()) {
                AddNewMessageToDatabase();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            item.setVisible(false);
            SpannableString s = new SpannableString(item.getTitle());
            s.setSpan(new ForegroundColorSpan(Color.BLACK), 0, s.length(), 0);
            item.setTitle(s);
        }
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final Chat chat = dataSnapshot.getValue(Chat.class);
                chatID = chat.getKey();
                chatImg.setImageDrawable(getResources().getDrawable(R.mipmap.main_icon));
                if(chat.isGroupChat()) {
                    if (!chat.getTitle().equals("Глобальный чат")) {
                        for (int i = 0; i < menu.size(); i++) {
                            menu.getItem(i).setVisible(true);
                        }
                        FirebaseManager.downloadImage(context, "chats", getIntent().getStringExtra("chatID"), "icon",
                                (bitmap, fromData) -> {
                            chatImg.setImageBitmap(bitmap);
                            if(fromData) Toast.makeText(context, "true at title", Toast.LENGTH_SHORT).show();
                        });
                        FirebaseDatabase.getInstance().getReference().child("users").child(mUserID).child("friends").
                                addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                friendsForAdd = new ArrayList<>();
                                members = new ArrayList<>();
                                try {
                                    members.addAll(chat.getMembers().keySet());
                                    for (DataSnapshot f : dataSnapshot.getChildren()) {
                                        Friend friend = f.getValue(Friend.class);
                                        if (!members.contains(friend.getId()))
                                            friendsForAdd.add(friend);

                                    }
                                } catch (Exception e){
                                    Toast.makeText(ChatActivity.this,
                                            String.valueOf(chat.getMembers().keySet().size()), Toast.LENGTH_SHORT).show();
                                    Toast.makeText(ChatActivity.this, "Error: Ошибка доступа к списку друзей \n"
                                            + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                        });
                    }
                }
                else {
                    for(final String user :chat.getMembers().keySet()){
                        if(!user.equals(mUserID)) {
                            FirebaseManager.downloadImage(context, "images", user, "icon",
                                    (bitmap, fromData) -> {
                                        chatImg.setImageBitmap(bitmap);
                                if(fromData)
                                    Toast.makeText(context, "true at title", Toast.LENGTH_SHORT).show();
                                    });
                            FirebaseManager.SetColorOnCircleImage(activity, chatImg, user);
                            break;
                        }
                    }
                }
                Member m = new Member();
                m.setInChat(true);
                chatRef.child("members").child(mUserID).setValue(m);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onPause() {
        chatRef.child("members").child(mUserID).setValue(new Member());
        super.onPause();
    }

    private void SetToolbar(){
        Toolbar toolbar =  findViewById(R.id.toolbar);
        toolbar.getBackground().setAlpha(255);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        title.setText(getIntent().getStringExtra("title"));

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Toast.makeText(context, "Изображение успешно загружено.", Toast.LENGTH_LONG).show();
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            bitmap = BitmapFactory.decodeFile(result.getUri().getPath());
            imageOfNewChat.setImageBitmap(bitmap);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.edit_chat:
                bitmap = ((BitmapDrawable)chatImg.getDrawable()).getBitmap();
                View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.dialog_chat_create, null);
                if(friendsForAdd!=null && friendsForAdd.size() > 0){
                    friendsChecked = new Boolean[friendsForAdd.size()];
                    for (int i = 0; i < friendsChecked.length; i++) {
                        friendsChecked[i] = false;
                    }
                    ((ListView)view.findViewById(R.id.listview)).setAdapter(new FriendsAdapter(friendsForAdd));
                }
                else {
                    view.findViewById(R.id.members).setVisibility(View.GONE);
                }
                final EditText text = view.findViewById(R.id.chat_name);
                text.setText(title.getText().toString());
                imageOfNewChat = view.findViewById(R.id.circle_image);
                imageOfNewChat.setImageBitmap(bitmap);
                Button button = view.findViewById(R.id.button);
                button.setOnClickListener(view1 -> CropImage.activity()
                        .setAspectRatio(1,1)
                        .setMaxCropResultSize(4000,4000)
                        .setMinCropResultSize(200,200)
                        .setActivityTitle("Выбор изображения")
                        .setCropMenuCropButtonTitle("Готово")
                        .setAllowCounterRotation(false)
                        .setAllowFlipping(false)
                        .setAllowRotation(false)
                        .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                        .setOutputCompressQuality(80)
                        .setRequestedSize(300,300)
                        .start(activity));
                final AlertDialog dialog = new AlertDialog.Builder(context)
                        .setView(view)
                        .setTitle("Настройки беседы")
                        .setPositiveButton("Готово", null)
                        .setNegativeButton("Отмена", null)
                        .create();
                dialog.show();
                dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    if (text.getText().toString().isEmpty()){
                        Toast.makeText(context, "Необходимо назвать беседу.", Toast.LENGTH_SHORT).show();
                    } else{
                        ArrayList<String> newMembers = new ArrayList<>(members);
                        if(friendsForAdd.size() > 0) {
                            for (int i = 0; i < friendsChecked.length; i++)
                                if (friendsChecked[i]) {
                                    newMembers.add(friendsForAdd.get(i).getId());
                                }
                            chatRef.child("members").setValue(newMembers);
                            ArrayList<Friend> buffer = friendsForAdd;
                            for (Friend friend : buffer) {
                                if (newMembers.contains(friend.getId())) {
                                    friendsForAdd.remove(friend);
                                }
                            }
                        }
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
                            for (String id : newMembers) {
                                FirebaseManager.setChatToFirst(context, ref.child(id).child("chats"),chatID);
                                FirebaseDatabase.getInstance().getReference().child("chats").child(chatID).child("title").setValue(text.getText().toString());
                            }
                        chatImg.setImageBitmap(bitmap);
                        FirebaseManager.saveImage(context, bitmap, "chats", chatID, "icon", isSuccess -> { });
                        title.setText(text.getText().toString());
                        dialog.dismiss();
                    }
                });
                return true;
            case R.id.delete:
                new AlertDialog.Builder(context)
                        .setTitle("Вы действительно хотите удалить диалог?")
                        .setNegativeButton("Отмена", null)
                        .setPositiveButton("Ок", (dialog1, which) -> {
                            FirebaseManager.deleteChatFromUser(context,chatID,mUserID);
                            activity.finish();
                        })
                        .create().show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void SetViews() {
        context = this;
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        assert mUser != null;
        mUserID = mUser.getUid();
        userRef = FirebaseDatabase.getInstance().getReference().
                child("users").
                child(mUserID);
        chatRef = FirebaseDatabase.getInstance().getReference().
                child("chats").
                child(getIntent().getStringExtra("chatID"));
        mMessageRef = FirebaseDatabase.getInstance().getReference()
                .child("messages")
                .child(getIntent().getStringExtra("chatID"));
        activity = this;
        isMessageSending = false;
        mMediaPlayer = MediaPlayer.create(activity, R.raw.message_voice);
        mListOfMessages = findViewById(R.id.list_of_messages);
        mInput = findViewById(R.id.input);
        mSendMessageButton = findViewById(R.id.fab);
        title = findViewById(R.id.title);
        chatImg = findViewById(R.id.image);
    }
    private void AddVoiceToMessage() {
        mMessageRef.limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message m = dataSnapshot.getValue(Message.class);
                assert m != null;
                if (!m.getUserID().equals(FirebaseAuth.getInstance().getUid())) {
                    mMediaPlayer.start();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void AddNewMessageToDatabase() {
        if (!isMessageSending) {
            isMessageSending = true;
            final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("time");
            ref.setValue(ServerValue.TIMESTAMP).addOnSuccessListener(aVoid -> ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull final DataSnapshot timeRef) {
                    @SuppressWarnings("ConstantConditions")
                    final long time = timeRef.getValue(long.class);
                    mMessageRef.limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                        // находим последнее сообщение чата
                        @Override
                        public void onDataChange(@NonNull DataSnapshot messageRef) {
                            if (messageRef.getChildrenCount() > 0) {
                                Message message = null;
                                String key = null;
                                for (DataSnapshot mess : messageRef.getChildren()) {
                                    // находим последние сообщение и его адрес
                                    message = mess.getValue(Message.class);
                                    key = mess.getKey();
                                }
                                // дата последнего сообщения
                                assert message != null;
                                String preData = String.valueOf(DateFormat.format("ddMMyyyy", message.getTime()));
                                // текущая дата сервера
                                String mesData = String.valueOf(DateFormat.format("ddMMyyyy", time));
                                if (mesData.equals(preData)) {
                                    // выполняется, если сообщение сегодня уже писалось
                                    if (message.getUserID().equals(mUserID)) {
                                        // выполняется, если пользователь писал последним
                                        assert key != null;
                                        AddTextInMessage(mMessageRef.child(key), message);
                                    } else {
                                        //выполняется, если пользователь не писал последним
                                        AddNewMessage(time, false);
                                    }
                                } else {
                                    // выполняется, если сегодня ещё не было сообщений
                                    AddNewMessage(time, true);
                                }
                            } else {
                                //создание самого первого сообщения
                                AddNewMessage(time);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            isMessageSending = false;
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    isMessageSending = false;
                }
            }));
        }
    }
    private void AddTextInMessage(DatabaseReference ref, Message m) {
        m.addMessageText(mInput.getText().toString());
        ref.setValue(m).addOnSuccessListener(aVoid -> prepareChat());
    }
    private void AddNewMessage(final long time, final boolean isMessageNewInDay) {
        ArrayList<String> messages = new ArrayList<>();
        messages.add(mInput.getText().toString());
        final Message mess = new Message(messages, mUserName, mUserID, time, isMessageNewInDay);
        mMessageRef.push().setValue(mess).addOnSuccessListener(aVoid -> prepareChat());
    }
    private void AddNewMessage(final long time) {
        ArrayList<String> messages = new ArrayList<>();
        messages.add(mInput.getText().toString());
        final Message mess = new Message(messages, mUserName, mUserID, time, true);
        mMessageRef.push().setValue(mess).addOnSuccessListener(aVoid -> prepareChat());
    }
    private void prepareChat(){
        FirebaseManager.addNotifyToChatMembers(context, chatID);
        FirebaseManager.AddPointToAchievement(activity,mListOfMessages,mUserID,"messages_x5");
        FirebaseManager.AddPointToAchievement(activity,mListOfMessages,mUserID,"messages_x25");
        FirebaseManager.AddPointToAchievement(activity,mListOfMessages,mUserID,"messages_x100");
        mInput.setText("");
        isMessageSending = false;
        FirebaseManager.CloseKeyBoard(activity);
        //FirebaseManager.setChatToFirstInMembers(context, members, chatID);
    }
    @SuppressLint("InflateParams")
    private void SetUserChat() {
        SnapshotParser<Message> parser = snapshot -> {
            //noinspection ConstantConditions
            return snapshot.getValue(Message.class);
        };
        final FirebaseListOptions<Message> options = new FirebaseListOptions.Builder<Message>()
                .setQuery(mMessageRef.limitToLast(60), parser)
                .setLayout(R.layout.message)
                .setLifecycleOwner(activity)
                .build();
        FirebaseListAdapter<Message> adapter = new FirebaseListAdapter<Message>(options) {
            @SuppressLint({"ResourceAsColor", "RtlHardcoded"})
            @Override
            protected void populateView(final View v, final Message model, final int position) {
                // Get references to the views of message.xml
                final TextView messageUser = v.findViewById(R.id.message_user);
                final TextView messageText = v.findViewById(R.id.message_text);
                final TextView messageDate = v.findViewById(R.id.message_date);
                final TextView messageTime = v.findViewById(R.id.message_time);
                final CardView cardView = v.findViewById(R.id.card_view);
                final CircleImageView userIcon = v.findViewById(R.id.circle_image);
                StringBuilder message = new StringBuilder();
                int size = model.getMessageText().size();
                for (int i = 0; i < size; i++) {
                    if (i < size - 1)
                        message.append(model.getMessageText().get(i)).append("\n");
                    else message.append(model.getMessageText().get(i));
                }
                if (model.isMessageNewInDay()) {
                    messageDate.setVisibility(View.VISIBLE);
                    StringBuilder date = new StringBuilder();
                    date.append(FirebaseManager.ShowDate(context,"d MMMM", model.getTime()));
                    if (!FirebaseManager.ShowDate(context,"yyyy", model.getTime())
                            .equals(FirebaseManager.ShowDate(context,"yyyy", new Date().getTime())))
                        date.append(FirebaseManager.ShowDate(context, "yyyy", model.getTime())).append(" года");
                    messageDate.setText(date);
                } else {
                    messageDate.setVisibility(View.GONE);
                }
                messageText.setText(message.toString());
                messageUser.setText(model.getMessageUser());
                CoordinatorLayout.LayoutParams layoutParamsCircle = (CoordinatorLayout.LayoutParams) userIcon.getLayoutParams();
                CoordinatorLayout.LayoutParams layoutParamsCard = (CoordinatorLayout.LayoutParams) cardView.getLayoutParams();
                if (model.getUserID().equals(mUser.getUid())) {
                    // Оформление своего сообщения
                    //cardView.setCardBackgroundColor(getResources().getColor(R.color.colorAccentVeryLight));
                    layoutParamsCircle.gravity = Gravity.RIGHT;
                    layoutParamsCircle.setMargins(0, 10, 4, 0);
                    layoutParamsCard.gravity = Gravity.RIGHT;
                    layoutParamsCard.setMargins(0, 0, 130, 0);
                } else {
                    // Оформление чужого сообщения
                    cardView.setCardBackgroundColor(Color.WHITE);
                    layoutParamsCircle.gravity = Gravity.LEFT;
                    layoutParamsCircle.setMargins(4, 10, 0, 0);
                    layoutParamsCard.gravity = Gravity.LEFT;
                    layoutParamsCard.setMargins(130, 0, 0, 0);
                }
                userIcon.setLayoutParams(layoutParamsCircle);
                cardView.setLayoutParams(layoutParamsCard);
                FirebaseManager.SetColorOnCircleImage(activity, userIcon, model.getUserID());
                FirebaseManager.downloadImage(context, "images", model.getUserID(), "icon",
                        ((bitmap, fromData) -> {
                            userIcon.setImageBitmap(bitmap);
                            if(fromData) Toast.makeText(context, "true at user image", Toast.LENGTH_SHORT).show();
                        }));
                messageTime.setText(FirebaseManager.ShowDate(context,"HH:mm", model.getTime()));
            }
        };
        mListOfMessages.addFooterView(getLayoutInflater().inflate(R.layout.list_footer, null));
        mListOfMessages.setAdapter(adapter);
    }
    private class FriendsAdapter extends BaseAdapter {
        private ArrayList<Friend> friends;
        LayoutInflater inflater;
        FriendsAdapter(ArrayList<Friend> array){
            friends = array;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public int getCount() {
            return friends.size();
        }

        @Override
        public Friend getItem(int position) {
            return friends.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = inflater.inflate(R.layout.friend_item_checked, parent, false);
            }
            CircleImageView circleImageView = view.findViewById(R.id.circle_image);
            TextView title = view.findViewById(R.id.text);
            CheckBox check = view.findViewById(R.id.checkbox);
            check.setOnCheckedChangeListener((buttonView, isChecked) -> friendsChecked[position] = isChecked);
            title.setText(getItem(position).getName());
            FirebaseManager.downloadImage(context, "images", getItem(position).getId(), "icon",
                    ((bitmap, fromData) -> {
                        circleImageView.setImageBitmap(bitmap);
                        if(fromData)
                            Toast.makeText(ChatActivity.this, "true at friends", Toast.LENGTH_SHORT).show();
                    }));
            FirebaseManager.SetColorOnCircleImage(activity, circleImageView, getItem(position).getId());
            return view;
        }
    }
}
