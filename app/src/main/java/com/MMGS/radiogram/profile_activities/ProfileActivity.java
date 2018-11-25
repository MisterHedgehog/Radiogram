package com.MMGS.radiogram.profile_activities;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.MMGS.radiogram.R;
import com.MMGS.radiogram.classes.FirebaseManager;
import com.MMGS.radiogram.classes.Friend;
import com.MMGS.radiogram.classes.ImageDownloaderListener;
import com.MMGS.radiogram.classes.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {


    private CircleImageView circleImage;
    private Toolbar toolbar;
    private ImageView imageView;
    private TextView pUserKursText, pUserGroupText, pUserPhoneText, pUserBirthdayText, pUserAdressText,
            likesCountText, friendsCountText, subsCountText, likesText, friendsText, subsText, thinkText;
    private Button pEditButton, achievementButton;
    private CardView pBirthdayCardView, pAddressCardView, pPhoneCardView, pGroupCardView, pThinkCardView;
    private CoordinatorLayout rootLayout;
    private TextView nameInFrame;
    private TextView nameInToolbar;
    private AppBarLayout mAppBarLayout;
    private View textBackground;

    private Intent pEditActivity;

    private String pUserSurname;
    private String pUserName;
    private String pUserID;
    private String myID;
    private Context context;
    private AppCompatActivity activity;
    private Boolean isLiked = false;

    private FirebaseAuth pUser;
    private StorageReference pStorageRef;
    private DatabaseReference pUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        bindActivity();
        SetViews();
    }
    private void bindActivity() {
        pEditActivity = new Intent(this, EditProfileActivity.class);
        activity = this;
        context = this;

        nameInToolbar = findViewById(R.id.name_toolbar);
        nameInFrame = findViewById(R.id.name);
        mAppBarLayout = findViewById(R.id.appbar);
        pEditButton = findViewById(R.id.edit_button);
        pEditButton.setVisibility(View.GONE);
        achievementButton = findViewById(R.id.achievement_button);
        pUserKursText = findViewById(R.id.kurs_text);
        pUserGroupText = findViewById(R.id.group_text);
        pUserPhoneText = findViewById(R.id.telephone_text);
        pUserBirthdayText = findViewById(R.id.birthday_text);
        pUserAdressText = findViewById(R.id.adres_text);
        thinkText = findViewById(R.id.think_text);
        pBirthdayCardView = findViewById(R.id.card_view_birthday);
        pAddressCardView = findViewById(R.id.card_view_address);
        pPhoneCardView = findViewById(R.id.card_view_phone);
        pGroupCardView = findViewById(R.id.card_view_group);
        pThinkCardView = findViewById(R.id.card_view_think);
        rootLayout = findViewById(R.id.root);
        toolbar = findViewById(R.id.toolbar);
        circleImage = findViewById(R.id.circle_main);
        likesCountText = findViewById(R.id.likes_count);
        friendsCountText = findViewById(R.id.friends_count);
        subsCountText = findViewById(R.id.subs_count);
        likesText = findViewById(R.id.likes_text);
        friendsText = findViewById(R.id.friends_text);
        subsText = findViewById(R.id.subs_text);
        textBackground = findViewById(R.id.text_background);
    }

    @Override
    protected void onResume() {
        // Добавляем иконку пользавателя возле текста сообщения
        FirebaseManager.downloadImage(context, "images", pUserID, "icon_big", (bitmap, fromData) ->
        {
            imageView.setImageBitmap(bitmap);
        });
        // Добавляем изображение пользавателя на кругое поле
        FirebaseManager.downloadImage(context, "images", pUserID, "icon", (bitmap, fromData) -> {
            circleImage.setImageBitmap(bitmap);
        });
        super.onResume();
    }
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        pUserRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    User user = dataSnapshot.getValue(User.class);
                    pUserName = user.getuName();
                    pUserSurname = user.getuSurname();
                    String adress = user.getuAdres();
                    String phone = user.getuTelephone();
                    String birthday = user.getuBirthday();
                    String group = user.getuGroup();
                    String kurs = user.getuKurs();
                    String think = user.getThink();
                    if (!adress.equals("non")) {
                        pAddressCardView.setVisibility(View.VISIBLE);
                        pUserAdressText.setText(adress);
                    } else pAddressCardView.setVisibility(View.GONE);
                    if (!phone.equals("non")) {
                        pPhoneCardView.setVisibility(View.VISIBLE);
                        pUserPhoneText.setText(phone);
                    } else pPhoneCardView.setVisibility(View.GONE);
                    if (!think.equals("non")) {
                        pThinkCardView.setVisibility(View.VISIBLE);
                        thinkText.setText(think);
                    } else pPhoneCardView.setVisibility(View.GONE);
                    if (!birthday.equals("00.00.0000")) {
                        pBirthdayCardView.setVisibility(View.VISIBLE);
                        pUserBirthdayText.setText(birthday);
                    } else pBirthdayCardView.setVisibility(View.GONE);
                    nameInFrame.setText(pUserName + " " + pUserSurname);
                    nameInToolbar.setText(pUserName + " " + pUserSurname);
                    circleImage.setBorderColor(user.getuColor());
                    if (!group.equals("non") && !kurs.equals("non")) {
                        pGroupCardView.setVisibility(View.VISIBLE);
                        pUserGroupText.setText("Группа: " + user.getuGroup());
                        pUserKursText.setText("Курс: " + user.getuKurs());
                    } else pGroupCardView.setVisibility(View.GONE);
                    MenuItem item = menu.findItem(R.id.like);
                    SetEditButton(pUserID, dataSnapshot, item, menu.findItem(R.id.delete));
                    int friends = 0, likes = 0, subs = 0;
                    if (dataSnapshot.hasChild("liked")) {
                        if (user.getLiked().containsKey(myID))
                            item.setIcon(R.drawable.ic_favorite_white_24dp);
                        likes = user.getLiked().size();
                    }
                    if (dataSnapshot.hasChild("friends")) {
                        friends = user.getFriends().size();
                    }
                    if (dataSnapshot.hasChild("subs")) {
                        subs = user.getSubs().size();
                    }
                    setCardViewWithCounts(likes, friends, subs);
                } catch (Exception e){
                    Toast.makeText(context, "Error: Ошибка доступа к пользователю\nJava: ProfileActivity:onCreateOptionsMenu\n"
                            + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.delete:
                pUserRef.child("friends").child(myID).removeValue();
                pUserRef.child("subs").child(myID).removeValue();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users").child(myID);
                reference.child("friends").child(pUserID).removeValue();
                reference.child("mySubs").child(pUserID).removeValue();
                finish();
                return true;
            case R.id.like:
                isLiked = !isLiked;
                item.setEnabled(false);
                if(isLiked){
                    pUserRef.child("liked").child(pUser.getCurrentUser().getUid())
                            .setValue(pUser.getCurrentUser().getEmail()).addOnSuccessListener(aVoid -> {
                                item.setIcon(R.drawable.ic_favorite_white_24dp);
                                item.setEnabled(true);
                            });
                }
                else {
                    pUserRef.child("liked").child(pUser.getCurrentUser().getUid()).removeValue().addOnSuccessListener(aVoid -> {
                        item.setIcon(R.drawable.ic_favorite_gray_24dp);
                        item.setEnabled(true);
                    });
                }
                return true;
        }
        return false;
    }
    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll; // from 0 in bottom to 1
        int alpha = (int)((percentage - 0.5f) * 510);  // from -255 внизу to 255 вверху
        toolbar.getBackground().setAlpha(Math.max(0,alpha));
        nameInToolbar.setAlpha(Math.max(0,(float) alpha/255));
        nameInFrame.setAlpha(Math.max(0,(float) -alpha/255));
        ViewGroup.LayoutParams params = textBackground.getLayoutParams();
        params.height = 300 - (int)(300 * percentage); // from 300 to 0
        textBackground.setLayoutParams(params);
    }
    private void SetViews() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        imageView = findViewById(R.id.appbar_image);
        mAppBarLayout.addOnOffsetChangedListener(this);
        pStorageRef = FirebaseStorage.getInstance().getReference();
        pUser = FirebaseAuth.getInstance();
        myID = pUser.getUid();
        pUserRef = FirebaseDatabase.getInstance().getReference();
        pUserID = getIntent().getStringExtra("show_user_profile").equals("TRUE") ?
                myID : getIntent().getStringExtra("userID");
        pUserRef = pUserRef.child("users").child(pUserID);
    }
    private void setCardViewWithCounts(int likes, int friends, int subs){
        if(likes > 20 || likes < 10) {
            switch (likes%10) {
                case 1:
                    likesText.setText("лайк");
                    break;
                case 2:
                case 3:
                case 4:
                    likesText.setText("лайка");
                    break;
                default:
                    likesText.setText("лайков");
            }
        } else likesText.setText("лайков");
        likesCountText.setText(String.valueOf(likes));
        if(friends > 20 || friends < 10) {
            switch (friends%10) {
                case 1:
                    friendsText.setText("друг");
                    break;
                case 2:
                case 3:
                case 4:
                    friendsText.setText("друга");
                    break;
                default:
                    friendsText.setText("друзей");
            }
        } else friendsText.setText("друзей");
        friendsCountText.setText(String.valueOf(friends));
        if(subs > 20 || subs < 10) {
            switch (subs%10) {
                case 1:
                    subsText.setText("подписчик");
                    break;
                case 2:
                case 3:
                case 4:
                    subsText.setText("подписчика");
                    break;
                default:
                    subsText.setText("пописчиков");
            }
        } else subsText.setText("пописчиков");
        subsCountText.setText(String.valueOf(subs));
    }
    private void SetEditButton(final String userID, final DataSnapshot dataSnapshot, MenuItem menuItemLike, MenuItem menuItemDelete){
        //Добавляем возможность редактировать профиль
        try {
            if (myID.equals(userID)) {
                menuItemLike.setVisible(false);
                pEditButton.setVisibility(View.VISIBLE);
                pEditButton.setText("Редактировать");
                pEditButton.setOnClickListener(v -> startActivityForResult(pEditActivity, 0));
            } else {
                menuItemLike.setVisible(true);
                if (!(dataSnapshot.child("friends").hasChild(myID)
                        || dataSnapshot.child("subs").hasChild(myID))) {
                    pEditButton.setVisibility(View.VISIBLE);
                    pEditButton.setText("Добавить в друзья");
                    pEditButton.setOnClickListener(v -> {
                        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users");
                        reference.child(myID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                                Friend friend = new Friend(pUserName + " " + pUserSurname, userID);
                                FirebaseManager.CreateFriend(activity, rootLayout, dataSnapshot1, friend);
                                pEditButton.setVisibility(View.GONE);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    });
                } else {
                    pEditButton.setVisibility(View.GONE);
                    menuItemDelete.setVisible(true);
                }
            }
            achievementButton.setOnClickListener(v -> FirebaseManager.OpenAchievements(activity, userID));
        } catch (Exception e){
            Toast.makeText(context, "Error: Ошибка доступа к пользователю\nJava: ProfileActivity:SetEditButton\n"
                    + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
