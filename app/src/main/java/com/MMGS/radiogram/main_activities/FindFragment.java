package com.MMGS.radiogram.main_activities;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.MMGS.radiogram.R;

public class FindFragment extends Fragment {

    private TabLayout tabLayout;
    private Intent mAddLossActivity;
    private Activity activity;

    private FragmentTransaction transaction;
    private MyFindFragment mMyThingsFragment;
    private AllFindFragment mAllThingsFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find, container, false);
        SetViews(view);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                transaction = getFragmentManager().beginTransaction();
                switch (tab.getPosition()){
                    case 0: transaction.hide(mAllThingsFragment).show(mMyThingsFragment).commit(); break;
                    case 1: transaction.hide(mMyThingsFragment).show(mAllThingsFragment).commit(); break;
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.container_find, mMyThingsFragment);
        transaction.add(R.id.container_find, mAllThingsFragment);
        transaction.hide(mAllThingsFragment);
        transaction.commit();
        return view;
    }

    private void SetViews(View v){
        activity = getActivity();
        tabLayout = v.findViewById(R.id.tabs);
        mAddLossActivity = new Intent(activity, AddLossActivity.class);
        mMyThingsFragment = new MyFindFragment();
        mAllThingsFragment = new AllFindFragment();
    }
    public void AddThingInStorage(String title){
        if (title.equals(getResources().getString(R.string.finds))){
            mAddLossActivity.putExtra(AddLossActivity.ACTIVITY_TYPE, AddLossActivity.ADD_FIND_ACTIVITY);
        }
        else {
            mAddLossActivity.putExtra(AddLossActivity.ACTIVITY_TYPE, AddLossActivity.ADD_LOSS_ACTIVITY);
        }
        startActivity(mAddLossActivity);
    }
    public void ChangeAdapter(String title){
        if (title.equals(getResources().getString(R.string.finds))){
            mMyThingsFragment.ChangeAdapter(mMyThingsFragment.FINDS_ADAPTER);
            mAllThingsFragment.ChangeAdapter(mAllThingsFragment.FINDS_ADAPTER);
        }
        else {
            mMyThingsFragment.ChangeAdapter(mMyThingsFragment.LOSSES_ADAPTER);
            mAllThingsFragment.ChangeAdapter(mAllThingsFragment.LOSSES_ADAPTER);
        }
    }

}
