package com.MMGS.radiogram.main_activities;

import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.MMGS.radiogram.classes.ImageDownloaderListener;
import com.MMGS.radiogram.profile_activities.FriendsActivity;
import com.MMGS.radiogram.services.LEDFlashingService;
import com.MMGS.radiogram.else_activities.AppActivity;
import com.MMGS.radiogram.R;
import com.MMGS.radiogram.classes.FirebaseManager;
import com.MMGS.radiogram.classes.User;
import com.MMGS.radiogram.else_activities.BrowserActivity;
import com.MMGS.radiogram.profile_activities.ProfileActivity;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FileUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Time;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.provider.Telephony.Mms.Part.FILENAME;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        AppBarLayout.OnOffsetChangedListener{

    private AppCompatActivity activity;
    private Context context;
    private BottomNavigationView bottomNavigationView;
    private CoordinatorLayout rootLayout;
    private FrameLayout contentMain;
    private Intent mSiteBrowser, mVKBrowser, mBrowserActivity, mProfileActivity, mLibraryBrowser, mAppInfoActivity, mFriendsActivity;
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private AppBarLayout appBarLayout;
    private TextView title, headerName, headerEmail;
    private ImageView imageView, headerBackground;
    private CircleImageView headerImage;

    private FirebaseUser user;
    private boolean isUserAdmin = false;

    private boolean isNavigationViewVisible = true;
    private String homeFragmentTitle;
    private String chatFragmentTitle;
    private String findFragmentTitle;

    private  boolean isFragmentAnimated = false;
    private FragmentTransaction transaction;
    private ChatFragment chatFragment = new ChatFragment();
    private FindFragment findFragment = new FindFragment();
    private HomeFragment homeFragment = new HomeFragment();

    private int currentFragment;
    private final int FRAGMENT_HOME = 0;
    private final int FRAGMENT_CHAT = 1;
    private final int FRAGMENT_FIND = 2;

    private final int MODE_SCROLLING = 0;
    private final int MODE_UNSCROLLING = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SetViews();
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        appBarLayout.addOnOffsetChangedListener(this);
        bottomNavigationView.setOnNavigationItemSelectedListener(new MyListener());
        CreateFragments();
    }
    @Override
    protected void onResume() {
        ControlNavigationView();
        super.onResume();
    }
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.news:
                ShowFragment(FRAGMENT_HOME);
                break;
            case R.id.chat:
                ShowFragment(FRAGMENT_CHAT);
                break;
            case R.id.losses:
                ShowFragment(FRAGMENT_FIND);
                break;
            case R.id.table:
                new FirebaseManager.GoForTable(this, mBrowserActivity).execute();
                break;
            case R.id.my_profile:
                mProfileActivity.putExtra("show_user_profile", "TRUE");
                startActivity(mProfileActivity);
                break;
            case R.id.my_friends:
                startActivity(mFriendsActivity);
                break;
            case R.id.go_vk:
                startActivity(mVKBrowser);
                break;
            case R.id.go_site:
                startActivity(mSiteBrowser);
                break;
            case R.id.go_library:
                startActivity(mLibraryBrowser);
                break;
            case R.id.sign_out: //Выйти
                FirebaseAuth.getInstance().signOut();
                GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                        GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
                signInClient.signOut().addOnCompleteListener(this,
                        task -> finish());
                break;
            case R.id.app_info:
                startActivity(mAppInfoActivity);
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;
        title.setAlpha(percentage);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_loss, menu);
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean isVisible = currentFragment == FRAGMENT_FIND;
            for (int i = 0; i < menu.size(); i++)
                menu.getItem(i).setVisible(isVisible);
            menu.getItem(3).setVisible(isUserAdmin);

        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_loss:
                findFragment.AddThingInStorage(findFragmentTitle);
                break;
            case R.id.menu_show_finds:
                findFragmentTitle = getResources().getString(R.string.finds);
                SetTitle(findFragmentTitle);
                findFragment.ChangeAdapter(findFragmentTitle);
                break;
            case R.id.menu_show_losses:
                findFragmentTitle = getResources().getString(R.string.losses);
                SetTitle(findFragmentTitle);
                findFragment.ChangeAdapter(findFragmentTitle);
                break;
            case R.id.reset_users:
                UpdateUsers();
                break;
            case R.id.lights:
                startService(new Intent(this, LEDFlashingService.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void UpdateUsers() {
        FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot user : dataSnapshot.getChildren()){
                    int i = -1;
                    for(DataSnapshot chat : user.child("chats").getChildren()){
                        String title = chat.child("title").getValue(String.class);
                        String image = "";
                        boolean isGroup = true;
                        if(chat.hasChild("image")) {
                            image = chat.child("image").getValue(String.class);
                            isGroup = false;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
                /*
                // Добавляет глобальный чат
        FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> ids = new ArrayList<>();
                for (DataSnapshot user : dataSnapshot.getChildren()){
                    ids.add(user.getKey());
                }
                String[] arr = {"Глобальный чат"};
                new Chat(ids, true, arr);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Toast.makeText(activity, String.valueOf(dataSnapshot.getChildrenCount()), Toast.LENGTH_SHORT).show();
                for(DataSnapshot value : dataSnapshot.getChildren()){
                    User user = new User(value.child("uName").getValue(String.class),
                            value.child("uSurname").getValue(String.class),
                            value.child("uEmail").getValue(String.class),
                            value.child("uPassword").getValue(String.class),
                            value.child("uColor").getValue(Integer.class));
                    user.setuKurs(value.child("uKurs").getValue(String.class));
                    user.setuGroup(value.child("uGroup").getValue(String.class));
                    user.setLastAddMobTime(value.child("lastAddMobTime").getValue(Long.class));
                    user.setuAdres(value.child("uAdres").getValue(String.class));
                    user.setuBirthday(value.child("uBirthday").getValue(String.class));
                    user.setuTelephone(value.child("uTelephone").getValue(String.class));
                    user.setRank(value.child("rank").getValue(String.class));
                    HashMap<String,Achievement> achievements = new HashMap<>();
                    for(DataSnapshot achiv : value.child("achievements").getChildren())
                        achievements.put(achiv.getKey(), achiv.getValue(Achievement.class));
                    user.addAchievements(achievements);
                    FirebaseDatabase.getInstance().getReference().child("users").child(value.getKey()).setValue(user);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        */
    }
    private void SetViews() {
        activity = this;
        context = this;
        bottomNavigationView = findViewById(R.id.navigation);
        navigationView = findViewById(R.id.nav_view);
        contentMain = findViewById(R.id.layout_main);
        rootLayout = findViewById(R.id.container);
        drawer = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        title = findViewById(R.id.toolbar_title);
        appBarLayout = findViewById(R.id.main_appbar);
        collapsingToolbarLayout = findViewById(R.id.main_toolbar);
        imageView = findViewById(R.id.app_bar_image);

        View header = navigationView.getHeaderView(0);
        headerEmail = header.findViewById(R.id.header_email);
        headerName = header.findViewById(R.id.header_name);
        headerBackground = header.findViewById(R.id.header_background);
        headerImage = header.findViewById(R.id.header_image);

        mSiteBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse("http://mrk-bsuir.by/"));
        mVKBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/podslushano_mgvrk"));
        mLibraryBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse("http://lib.mrk-bsuir.by"));
        mBrowserActivity = new Intent(context, BrowserActivity.class);
        mProfileActivity = new Intent(context, ProfileActivity.class);
        mFriendsActivity = new Intent(context, FriendsActivity.class);
        mAppInfoActivity = new Intent(context, AppActivity.class);

        homeFragmentTitle = getResources().getString(R.string.news);
        chatFragmentTitle = getResources().getString(R.string.chat);
        findFragmentTitle = getResources().getString(R.string.losses);

        user = FirebaseAuth.getInstance().getCurrentUser();
    }
    @SuppressLint("ResourceType")
    private void CreateFragments(){
        transaction = getFragmentManager().beginTransaction();
        ((FrameLayout) findViewById(R.id.layout_main)).removeAllViews();
        transaction.add(R.id.layout_main, homeFragment, "home");
        transaction.add(R.id.layout_main, chatFragment, "chat");
        transaction.add(R.id.layout_main, findFragment, "find");
        transaction.hide(homeFragment);
        transaction.hide(findFragment);
        SetTitle(chatFragmentTitle);
        currentFragment = FRAGMENT_CHAT;
        bottomNavigationView.getMenu().getItem(FRAGMENT_CHAT).setChecked(true);
        SetNesteredModeToAppbar(MODE_UNSCROLLING, 55);
        transaction.commit();
    }
    private void ControlNavigationView() {
        FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User u = dataSnapshot.getValue(User.class);
                isUserAdmin = u.getRank().equals("admin");
                headerEmail.setText(u.getuEmail());
                headerName.setText(u.getuName() + " " + u.getuSurname());
                // Добавляем фоновое изображение
                FirebaseManager.downloadImage(context, "images", user.getUid(), "icon_big", (bitmap, fromData) -> {
                    headerBackground.setImageBitmap(bitmap);});
                // Добавляем изображение пользавателя на кругое поле
                FirebaseManager.downloadImage(context, "images", user.getUid(), "icon", (bitmap, fromData) -> {
                    headerImage.setImageBitmap(bitmap);
                });
                FirebaseManager.downloadImage(context, "images", "appbar", "appbar", (bitmap, fromData) -> {
                    imageView.setImageBitmap(bitmap);
                });
                FirebaseManager.AddPointToAchievement(activity, rootLayout, user.getUid(),"alpha");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int heightDiff = rootLayout.getRootView().getHeight() - rootLayout.getHeight();
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            float dpToPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, metrics);
            if (heightDiff < dpToPx) { // клавиатура исчезла
                if (!isNavigationViewVisible) {
                    bottomNavigationView.setVisibility(View.VISIBLE);
                    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) contentMain.getLayoutParams();
                    params.setMargins(0, 0, 0, bottomNavigationView.getHeight());
                    contentMain.setLayoutParams(params);
                    isNavigationViewVisible = true;
                }
            } else // клавиатура появилась
                if (isNavigationViewVisible) {
                    bottomNavigationView.setVisibility(View.GONE);
                    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) contentMain.getLayoutParams();
                    params.setMargins(0, 0, 0, 0);
                    contentMain.setLayoutParams(params);
                    isNavigationViewVisible = false;
                }
        });
    }
    private void SetNesteredModeToAppbar(int mode, int dpOfImage) {
        AppBarLayout.LayoutParams toolbarLayoutParams = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();
        CollapsingToolbarLayout.LayoutParams imageParams = (CollapsingToolbarLayout.LayoutParams) imageView.getLayoutParams();
        switch (mode) {
            case MODE_SCROLLING:
                imageView.setVisibility(View.VISIBLE);
                toolbarLayoutParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
                break;
            case MODE_UNSCROLLING:
                imageView.setVisibility(View.INVISIBLE);
                toolbarLayoutParams.setScrollFlags(0);
                break;
        }
        imageParams.height = FirebaseManager.DpToPx(context, dpOfImage);
        imageView.setLayoutParams(imageParams);
        collapsingToolbarLayout.setLayoutParams(toolbarLayoutParams);
    }
    public void SetTitle(String t) {
        title.setText(t);
        invalidateOptionsMenu();
    }
    private void HideFragment(FragmentTransaction transaction, final int FRAGMENT) {
        switch (FRAGMENT) {
            case FRAGMENT_HOME: transaction.hide(homeFragment); break;
            case FRAGMENT_CHAT: transaction.hide(chatFragment); break;
            case FRAGMENT_FIND: transaction.hide(findFragment); break;
        }
    }
    @SuppressLint("ResourceType")
    private void ShowFragment(final int FRAGMENT) {
        if(FRAGMENT != currentFragment) {
            transaction = getFragmentManager().beginTransaction();
            switch (FRAGMENT) {
                case FRAGMENT_HOME:
                    if(homeFragment == null){
                        homeFragment = new HomeFragment();
                        transaction.add(R.id.layout_main, homeFragment);
                    }
                    transaction.setCustomAnimations(R.anim.slide_in_middle_from_left, R.anim.slide_in_right);
                    HideFragment(transaction, currentFragment);
                    transaction.show(homeFragment);
                    SetTitle(homeFragmentTitle);
                    SetNesteredModeToAppbar(MODE_SCROLLING, 190);
                    appBarLayout.setExpanded(false, false);
                    break;
                case FRAGMENT_CHAT:
                    if(chatFragment == null) {
                        chatFragment = new ChatFragment();
                        transaction.add(R.id.layout_main, chatFragment);
                    }
                    switch (currentFragment) {
                        case FRAGMENT_FIND:
                            transaction.setCustomAnimations(R.anim.slide_in_middle_from_left, R.anim.slide_in_right);
                            break;
                        case FRAGMENT_HOME:
                            transaction.setCustomAnimations(R.anim.slide_in_middle_from_right, R.anim.slide_in_left);
                            break;
                    }
                    HideFragment(transaction, currentFragment);
                    transaction.show(chatFragment);
                    SetTitle(chatFragmentTitle);
                    SetNesteredModeToAppbar(MODE_UNSCROLLING, 55);
                    break;
                case FRAGMENT_FIND:
                    if(findFragment == null){
                        findFragment = new FindFragment();
                        transaction.add(R.id.layout_main, findFragment);
                    }
                    transaction.setCustomAnimations(R.anim.slide_in_middle_from_right, R.anim.slide_in_left);
                    HideFragment(transaction, currentFragment);
                    transaction.show(findFragment);
                    SetTitle(findFragmentTitle);
                    SetNesteredModeToAppbar(MODE_UNSCROLLING, 55);
                    break;
            }
            currentFragment = FRAGMENT;
            final Menu menu = bottomNavigationView.getMenu();
            for(int i = 0; i < menu.size(); i++)
                if (i != currentFragment)
            menu.getItem(i).setCheckable(false);
            else {
                    menu.getItem(i).setCheckable(true);
                    menu.getItem(i).setChecked(true);
                }

            isFragmentAnimated = true;
            transaction.commit();
            Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                isFragmentAnimated = false;
            });
            thread.start();
        }
    }
    private class MyListener implements BottomNavigationView.OnNavigationItemSelectedListener{
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Menu menu = bottomNavigationView.getMenu();
            if (isFragmentAnimated) {
                return false;
            } else {
                menu.getItem(0).setCheckable(true);
                menu.getItem(1).setCheckable(true);
                menu.getItem(2).setCheckable(true);
                switch (item.getItemId()) {
                    case R.id.home_bottom:
                        ShowFragment(FRAGMENT_HOME);
                        break;
                    case R.id.chat_bottom:
                        ShowFragment(FRAGMENT_CHAT);
                        break;
                    case R.id.lost_bottom:
                        ShowFragment(FRAGMENT_FIND);
                        break;
                }
            }
                return true;
        }
    }
}
