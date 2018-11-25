package com.MMGS.radiogram.main_activities;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.MMGS.radiogram.GlideApp;
import com.MMGS.radiogram.chat_activities.ChatActivity;
import com.MMGS.radiogram.R;
import com.MMGS.radiogram.classes.Chat;
import com.MMGS.radiogram.classes.FirebaseManager;
import com.MMGS.radiogram.classes.Friend;
import com.MMGS.radiogram.classes.DownloadListener;
import com.MMGS.radiogram.classes.ImageDownloaderListener;
import com.MMGS.radiogram.classes.Member;
import com.MMGS.radiogram.classes.User;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class ChatFragment extends Fragment implements View.OnClickListener {

    private Context context;
    private Fragment fragment;
    private Button addChatBtn;
    private ListView chatsListView;
    private CircleImageView imageOfNewChat;

    private ArrayList<String> chatTitles = new ArrayList<>();
    private ArrayList<Bitmap> chatImages = new ArrayList<>();
    private ArrayList<String> chatIDs = new ArrayList<>();
    private ArrayList<Friend> friends = new ArrayList<>();

    private Boolean[] friendsChecked;
    private Bitmap bitmap;
    private ChatsAdapter chatsAdapter;
    private FirebaseUser user;
    private StorageReference imagesRef;

    public ChatFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        BindFragment(view);
        AddListHeader();
        addChatBtn.setOnClickListener(this);
        return view;
    }
    @Override
    public void onResume() {
        if (chatsAdapter!=null){
            chatsAdapter.chatTitles.clear();
            chatsAdapter.notifyDataSetChanged();
        }
        FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild("chats")) {
                    FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("chats")
                            .child("-LOxTLOZFrZ9mHPXp8aQ").setValue(0).addOnSuccessListener(aVoid -> DownloadListViews(dataSnapshot));
                } else DownloadListViews(dataSnapshot);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        super.onResume();
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
    private void AddListHeader(){
        LinearLayout layout = (LinearLayout) ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.list_footer,null);
        View view = layout.findViewById(R.id.view);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.height = addChatBtn.getLayoutParams().height;
        view.setLayoutParams(params);
        chatsListView.addHeaderView(layout);
    }
    private void DownloadListViews(final DataSnapshot user){
        try {
            chatTitles.clear();
            chatImages.clear();
            chatIDs.clear();
            friends.clear();
            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("chats");
            final DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
            for (final DataSnapshot chat : user.child("chats").getChildren()) {
                chatTitles.add("");
                chatImages.add(BitmapFactory.decodeResource(getResources(), R.mipmap.main_icon));
                chatIDs.add("-LOxTLOZFrZ9mHPXp8aQ");
                final int order = chat.getValue(Integer.class);
                reference.child(chat.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            Chat c = dataSnapshot.getValue(Chat.class);
                            chatIDs.set(order, c.getKey());
                            if (c.isGroupChat()) {
                                chatTitles.set(order, c.getTitle());
                                if(!c.getTitle().equals("Глобальный чат"))
                                FirebaseManager.downloadImage(context, "chats", c.getKey(), "icon", (bitmap, fromData) -> {
                                    chatImages.set(order, bitmap);
                                    chatsAdapter.notifyDataSetChanged();
                                });
                            } else {
                                ArrayList<String> members = new ArrayList<>(c.getMembers().keySet());
                                String title = members.get(0).equals(user.getKey()) ? members.get(1) : members.get(0);
                                usersRef.child(title).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        try {
                                            User u = dataSnapshot.getValue(User.class);
                                            chatTitles.set(order, u.getuName() + " " + u.getuSurname());
                                            chatsAdapter.notifyDataSetChanged();
                                        } catch (Exception e){
                                            Toast.makeText(context, "Error: Ошибка доступа к списку пользователей\n" +
                                                    "Java: ChatFragment, DownloadListView 3\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                                FirebaseManager.downloadImage(context, "images", title, "icon", (drawable, fromData) -> {
                                    chatImages.set(order, drawable);
                                    chatsAdapter.notifyDataSetChanged();
                                });
                            }
                            chatsAdapter.notifyDataSetChanged();
                        } catch (Exception e){
                            Toast.makeText(context, "Error: Ошибка доступа к списку диалогов\n" +
                                    "Java: ChatFragment, DownloadListView 2\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                chatsAdapter = new ChatsAdapter(chatImages, chatTitles, chatIDs);
                chatsListView.setAdapter(chatsAdapter);
            }
            if (user.hasChild("friends")) {
                for (DataSnapshot f : user.child("friends").getChildren()) {
                    friends.add(f.getValue(Friend.class));
                }
            }
        } catch (Exception e){
            Toast.makeText(context, "Error: Системная ошибка\n" +
                    "Java: ChatFragment, DownloadListView 1\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void BindFragment(View v){
        imagesRef = FirebaseStorage.getInstance().getReference().child("images");
        user = FirebaseAuth.getInstance().getCurrentUser();
        addChatBtn = v.findViewById(R.id.add_chat_button);
        chatsListView = v.findViewById(R.id.list_of_chats);
        context = getActivity();
        fragment = this;
    }

    @Override
    public void onClick(View v) {
        if(friends.size() == 0){
            Toast.makeText(context, "Необходимо иметь друзей, чтобы создать диалог.", Toast.LENGTH_LONG).show();
            return;
        }
        bitmap = null;
        friendsChecked = new Boolean[friends.size()];
        for (int i = 0; i < friendsChecked.length; i++) {
            friendsChecked[i] = false;
        }
        final View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_chat_create, null);
        ((ListView)view.findViewById(R.id.listview)).setAdapter(new FriendsAdapter(friends));
        final EditText text = view.findViewById(R.id.chat_name);
        imageOfNewChat = view.findViewById(R.id.circle_image);
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
                .start(context, fragment));
        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Настройка беседы")
                .setView(view)
                .setNegativeButton("Отмена", (dialog1, which) -> Toast.makeText(context, "Создание беседы отменено.", Toast.LENGTH_SHORT).show())
                .setPositiveButton("Готово", null)
                .create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v1 -> {
            if (!Arrays.asList(friendsChecked).contains(true)) {
                Toast.makeText(context, "Необходимо добавить собеседников.", Toast.LENGTH_SHORT).show();
            } else if (text.getText().toString().isEmpty()){
                Toast.makeText(context, "Необходимо назвать беседу.", Toast.LENGTH_SHORT).show();
            } else{
                HashMap<String, Member> members = new HashMap<>();
                members.put(user.getUid(), new Member());
                for (int i = 0; i < friendsChecked.length; i++)
                    if(friendsChecked[i]) members.put(friends.get(i).getId(), new Member());
                new Chat(context, members, text.getText().toString(), bitmap,
                        isSuccess -> FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                        dialog.dismiss();
                        DownloadListViews(dataSnapshot);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                }));
            }
        });
        //LinearLayout layout = new LinearLayout(context);
        //layout.setOrientation(LinearLayout.VERTICAL);
        //ListView list = new ListView(builder.getContext());
        //ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //list.setAdapter(new FriendsAdapter(friends));
        //layout.addView(list, params);
        //builder.setView(layout);
    }
    private class ChatsAdapter extends BaseAdapter{
        private ArrayList<Bitmap> images;
        private ArrayList<String> chatTitles;
        private ArrayList<String> chatIDs;
        private LayoutInflater inflater;
        private DatabaseReference reference;
        public ChatsAdapter(ArrayList<Bitmap> images, ArrayList<String> chatTitles, ArrayList<String> chatIDs){
            this.images = images;
            this.chatTitles = chatTitles;
            this.chatIDs = chatIDs;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            reference = FirebaseDatabase.getInstance().getReference().child("chats");
        }
        @Override
        public int getCount() {
            return chatTitles.size();
        }

        @Override
        public String getItem(int position) {
            return chatTitles.get(position);
        }

        public Bitmap getImage(int position) {
            return images.get(position);
        }

        public String getChatID(int position) {
            return chatIDs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            if (view == null) {
                view = inflater.inflate(R.layout.chat, parent, false);
            }
            CardView card = view.findViewById(R.id.card_view);
            ImageView image = view.findViewById(R.id.circle_image);
            TextView title = view.findViewById(R.id.text);
            FrameLayout last = view.findViewById(R.id.last);
            TextView number = view.findViewById(R.id.number);
            if (!getChatID(position).equals("-LOxTLOZFrZ9mHPXp8aQ")) {
                reference.child(getChatID(position)).child("members").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            Member m = dataSnapshot.getValue(Member.class);
                            if (m.getLastMessage() > 0) {
                                last.setVisibility(View.VISIBLE);
                                number.setText(String.valueOf(m.getLastMessage()));
                            } else {
                                last.setVisibility(View.GONE);
                            }
                        } catch (Exception e) {
                            Toast.makeText(context, "Error: Ошибка доступа к списку пользователей\n" +
                                    "Java:ChatFragment, class: ChatAdapter\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            } else last.setVisibility(View.GONE);
            card.setOnClickListener(v -> {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("chatID", getChatID(position));
                intent.putExtra("title", getItem(position));
                startActivity(intent);
            });
            title.setText(getItem(position));
            image.setImageBitmap(getImage(position));
            return view;
        }
    }
    private class FriendsAdapter extends BaseAdapter{
        private ArrayList<Friend> friends;
        LayoutInflater inflater;
        public FriendsAdapter(ArrayList<Friend> array){
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
        public View getView(final int position, View view, ViewGroup parent) {
            if (view == null) {
                view = inflater.inflate(R.layout.friend_item_checked, parent, false);
            }
            final CircleImageView circleImageView = view.findViewById(R.id.circle_image);
            TextView title = view.findViewById(R.id.text);
            CheckBox check = view.findViewById(R.id.checkbox);
            check.setOnCheckedChangeListener((buttonView, isChecked) -> friendsChecked[position] = isChecked);
            title.setText(getItem(position).getName());
            FirebaseManager.downloadImage(context, "images", getItem(position).getId(), "icon",
                    (bitmap, fromData) -> circleImageView.setImageBitmap(bitmap));
            FirebaseManager.SetColorOnCircleImage(getActivity(), circleImageView, getItem(position).getId());
            return view;
        }
    }
    /*
    public class CustomList extends ArrayAdapter<Friend> {

        LayoutInflater inflater;
        private ArrayList<Friend> friends;

        public CustomList(ArrayList<Friend> friends) {
            super(context, R.layout.chat, friends);
            this.friends = friends;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = inflater.inflate(R.layout.friend_list_item, parent, false);
            }
            CircleImageView circleImageView = view.findViewById(R.id.circle_image);
            ImageView image = view.findViewById(R.id.add);
            TextView title = view.findViewById(R.id.text);
            CardView card = view.findViewById(R.id.card_view);

            image.setVisibility(View.GONE);
            title.setText(friends.get(position).getName());
            Glide.with(context)
                    .load(FirebaseStorage.getInstance().getReference().child("images").child(friends.get(position).getId()).child("icon.jpg"))
                    .into(circleImageView);
            FirebaseManager.SetColorOnCircleImage(getActivity(), circleImageView, friends.get(position).getId());
            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            return view;
        }
    }
    */
}