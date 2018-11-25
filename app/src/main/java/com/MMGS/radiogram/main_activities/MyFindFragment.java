package com.MMGS.radiogram.main_activities;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.MMGS.radiogram.R;
import com.MMGS.radiogram.classes.FirebaseManager;

import static com.MMGS.radiogram.classes.FirebaseManager.RecyclerAdapter;

public class MyFindFragment extends Fragment {

    private RecyclerView recyclerView;
    private MainActivity activity;
    private LinearLayout emptyView;

    public final int LOSSES_ADAPTER = 0;
    public final int FINDS_ADAPTER = 1;

    public MyFindFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_find, container, false);
        recyclerView = view.findViewById(R.id.list);
        activity = (MainActivity) getActivity();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(activity);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLayoutManager);
        emptyView = view.findViewById(R.id.empty);
        recyclerView.setAdapter(RecyclerAdapter("losses", FirebaseManager.MY_POSTS,activity,emptyView));
        emptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mAddLossActivity = new Intent(activity, AddLossActivity.class);
                TextView textView = activity.findViewById(R.id.toolbar_title);
                String title = textView.getText().toString();
                if (title.equals(getResources().getString(R.string.finds))){
                    mAddLossActivity.putExtra(AddLossActivity.ACTIVITY_TYPE, AddLossActivity.ADD_FIND_ACTIVITY);
                }
                else {
                    mAddLossActivity.putExtra(AddLossActivity.ACTIVITY_TYPE, AddLossActivity.ADD_LOSS_ACTIVITY);
                }
                startActivity(mAddLossActivity);
            }
        });
        return view;
    }
    public void ChangeAdapter(int type) {
        switch (type) {
            case LOSSES_ADAPTER:
                recyclerView.setAdapter(RecyclerAdapter("losses", FirebaseManager.MY_POSTS,activity,emptyView));
                break;
            case FINDS_ADAPTER:
                recyclerView.setAdapter(RecyclerAdapter("finds", FirebaseManager.MY_POSTS,activity,emptyView));
                break;
        }
    }
}
