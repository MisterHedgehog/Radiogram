package com.MMGS.radiogram.profile_activities;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.MMGS.radiogram.GlideApp;
import com.MMGS.radiogram.R;
import com.MMGS.radiogram.classes.FirebaseManager;
import com.MMGS.radiogram.classes.Friend;
import com.MMGS.radiogram.classes.ImageDownloaderListener;
import com.MMGS.radiogram.classes.User;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private CoordinatorLayout container;
    private Toolbar toolbar;
    private BottomNavigationView navigation;
    private ListView listView;
    private TextView searchText;
    private LinearLayout emptyView;

    private FirebaseUser user;
    private Activity activity;

    private FriendsAdapter adapter;

    private final int SUBS = 0;
    private final int FRIENDS = 1;
    private final int MY_SUBS = 2;
    private int show = FRIENDS;
    private String hint = "поиск среди друзей";
    private String query = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        SetView();
        navigation.setOnNavigationItemSelectedListener(this);
        toolbar.getBackground().setAlpha(255);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Друзья");
        navigation.getMenu().getItem(show).setChecked(true);
        SetSearchAdapter();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconified(false);
        searchView.onActionViewExpanded();
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                searchText.setVisibility(View.VISIBLE);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searchText.setVisibility(View.GONE);
                return true;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                query = newText;
                adapter.SetFilter(query);
                return true;
            }
        });

        return true;
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.subs:
                show = SUBS;
                hint = "поиск среди подписчиков";
                toolbar.setTitle("Подписчики");
                break;
            case R.id.friends:
                show = FRIENDS;
                hint = "поиск среди друзей";
                toolbar.setTitle("Друзья");
                break;
            case R.id.my_subs:
                show = MY_SUBS;
                hint = "поиск среди подписок";
                toolbar.setTitle("Подписки");
                break;
        }
        if(adapter != null)
            adapter.SetFilter(query);
        return true;
    }
    private void SetView(){
        container = findViewById(R.id.container);
        searchText = findViewById(R.id.text_search);
        toolbar = findViewById(R.id.friends_toolbar);
        navigation = findViewById(R.id.navigation);
        listView = findViewById(R.id.list);
        emptyView = findViewById(R.id.empty);
        user = FirebaseAuth.getInstance().getCurrentUser();
        activity = this;

    }
    private class FriendsAdapter extends BaseAdapter {

        Context context;
        LayoutInflater inflater;
        ArrayList<Friend> listOfFriends;
        ArrayList<Friend> mFriendsFilterList;
        ArrayList<Friend> mSubsFilterList;
        ArrayList<Friend> mMySubsFilterList;
        ArrayList<Friend> mUsersFilterList = null;

        FriendsAdapter(Context c, ArrayList<Friend> friends, ArrayList<Friend> subs, ArrayList<Friend> mySubs ) {
            this.context = c;
            this.listOfFriends = friends;
            this.mFriendsFilterList = friends;
            this.mSubsFilterList = subs;
            this.mMySubsFilterList = mySubs;
            this.inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            emptyView.setVisibility(listOfFriends.size() == 0 ? View.VISIBLE : View.GONE);
        }
        @Override
        public int getCount() {
            return listOfFriends.size();
        }

        @Override
        public void notifyDataSetChanged() {
            emptyView.setVisibility(listOfFriends.size() == 0 ? View.VISIBLE : View.GONE);
            super.notifyDataSetChanged();
        }

        @Override
        public Friend getItem(int position) {
            return listOfFriends.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            if (view == null) {
                view = inflater.inflate(R.layout.friend_list_item, parent, false);
            }
            final CircleImageView image = view.findViewById(R.id.circle_image);
            final TextView text = view.findViewById(R.id.text);
            final StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            view.findViewById(R.id.main).setOnClickListener(v -> {
                final Intent profileActivity = new Intent(activity, ProfileActivity.class);
                profileActivity.putExtra("show_user_profile", "FALSE");
                profileActivity.putExtra("userID", getItem(position).getId());
                activity.finish();
                activity.startActivity(profileActivity);
            });
            text.setText(getItem(position).getName());
            FirebaseManager.SetColorOnCircleImage(activity, image, getItem(position).getId());
            FirebaseManager.downloadImage(context, "images", getItem(position).getId(), "icon", (bitmap, fromData) -> image.setImageBitmap(bitmap));
            return view;
        }
        public void DownloadUsers(final CharSequence filter) {
            searchText.setText("загрузка...");
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users");
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot users) {
                    mUsersFilterList = new ArrayList<>();
                    for (DataSnapshot user : users.getChildren()) {
                        User u = user.getValue(User.class);
                        mUsersFilterList.add(new Friend(u.getuName() + " " + u.getuSurname(), user.getKey()));
                    }
                    ArrayList<Friend> filterList = new ArrayList<>();
                    for (Friend friend : mUsersFilterList) {
                        if ((friend.getName().toUpperCase()).contains(filter.toString().toUpperCase()))
                            filterList.add(friend);
                    }
                    searchText.setText("глобальный поиск");
                    listOfFriends = filterList;
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        private void SetFilter(CharSequence filter){
            ArrayList<Friend> filterFriendsList = new ArrayList<>();
            switch (show){
                case 0 : filterFriendsList = mSubsFilterList; break;
                case 1 : filterFriendsList = mFriendsFilterList; break;
                case 2 : filterFriendsList = mMySubsFilterList; break;
            }
            if (filter != null && filter.length() > 0) {
                ArrayList<Friend> filterList = new ArrayList<>();
                for (Friend friend : filterFriendsList) {
                    if ((friend.getName().toUpperCase()).contains(filter.toString().toUpperCase()))
                        filterList.add(friend);
                }
                searchText.setText(hint);
                if(filterList.size() == 0) {
                    if (mUsersFilterList != null) {
                        for (Friend friend : mUsersFilterList) {
                            if ((friend.getName().toUpperCase()).contains(filter.toString().toUpperCase()))
                                filterList.add(friend);
                        }
                        searchText.setText("глобальный поиск");
                    } else
                        DownloadUsers(filter);
                }
                listOfFriends = filterList;
                notifyDataSetChanged();
            } else {
                searchText.setText(hint);
                listOfFriends = filterFriendsList;
                notifyDataSetChanged();
            }
        }
    }
    private void SetSearchAdapter(){
        FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Friend> friends = new ArrayList<>();
                ArrayList<Friend> subs = new ArrayList<>();
                ArrayList<Friend> mySubs = new ArrayList<>();
                if (dataSnapshot.hasChild("friends"))
                    for (DataSnapshot f : dataSnapshot.child("friends").getChildren()){
                        friends.add(f.getValue(Friend.class));
                    }
                if (dataSnapshot.hasChild("subs"))
                    for (DataSnapshot f : dataSnapshot.child("subs").getChildren()){
                        subs.add(f.getValue(Friend.class));
                    }
                if (dataSnapshot.hasChild("mySubs"))
                    for (DataSnapshot f : dataSnapshot.child("mySubs").getChildren()){
                        mySubs.add(f.getValue(Friend.class));
                    }
                adapter = new FriendsAdapter(activity, friends, subs, mySubs);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    /*
    private void SetupSearch(String search){
        Query query = FirebaseDatabase.getInstance().getReference().child("users").
                child(user.getUid()).child("friends").orderByKey().startAt(search).endAt(search + "\uf8ff");
        SnapshotParser<String> parser = new SnapshotParser<String>() {
            @NonNull
            @Override
            public String parseSnapshot(@NonNull DataSnapshot snapshot) {
                return snapshot.getValue(String.class);
            }
        };
        FirebaseRecyclerOptions<String> options = new FirebaseRecyclerOptions.Builder<String>()
                .setQuery(query, parser)
                .setLifecycleOwner(this)
                .build();
        FirebaseRecyclerAdapter<String, FriendViewHolder> adapter = new FirebaseRecyclerAdapter<String, FriendViewHolder>(options) {

            @NonNull
            @Override
            public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new FriendViewHolder(activity, LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.friend_list_item, parent, false));
            }

            @Override
            public void onDataChanged() {
                Toast.makeText(activity,String.valueOf(getItemCount()), Toast.LENGTH_SHORT).show();
                //if(emptyView != null)
                    //emptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            protected void onBindViewHolder(@NonNull FriendViewHolder holder, int position, @NonNull String model) {
                holder.SetFriend(model);
            }
        };
        //recyclerView.setAdapter(adapter);
    }
    private class FriendViewHolder extends RecyclerView.ViewHolder {
        View v;
        Activity activity;

        public FriendViewHolder(Activity activity, View itemView) {
            super(itemView);
            v = itemView;
            this.activity = activity;
        }

        public void SetFriend(final String model) {
            final StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users").child(model);
            final TextView text = v.findViewById(R.id.text);
            reference.child("uName").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    text.setText(dataSnapshot.getValue(String.class));
                    reference.child("uSurname").addListenerForSingleValueEvent(new ValueEventListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            text.setText(text.getText()+ " " + dataSnapshot.getValue(String.class));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            CircleImageView image = v.findViewById(R.id.circle_image);
            FirebaseManager.SetColorOnCircleImage(activity, image, firebaseUser.getUid());
            Glide.with(activity).load(storageReference.child("images").child(model).child("icon.jpg")).into(image);
        }
    }
    */
}
