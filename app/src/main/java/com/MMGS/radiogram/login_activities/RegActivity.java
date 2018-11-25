package com.MMGS.radiogram.login_activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.app.FragmentTransaction;

import com.MMGS.radiogram.R;

public class RegActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private LinearLayout tabs;
    private Step1 mStep1;
    private Step2 mStep2;
    private FragmentTransaction mManager;
    private boolean isFragmentAnimated = false;

    private String userPassword;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);

        mStep1 = new Step1();
        mStep2 = new Step2();

        mManager = getFragmentManager().beginTransaction();
        mManager.add(R.id.fragment_container, mStep1);
        mManager.add(R.id.fragment_container, mStep2);
        mManager.hide(mStep2);
        mManager.commit();

        tabLayout = findViewById(R.id.tabs);
                // устанавливаем запрет на переход ко второму фрагменту
        tabs=(LinearLayout) tabLayout.getChildAt(0);
        tabs.getChildAt(1).setEnabled(false);
        tabs.getChildAt(1).setClickable(false);
        tabs.getChildAt(1).setAlpha(0.6f);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Прячем клавиатуру
                if(getCurrentFocus() != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
                if(!isFragmentAnimated) {
                    mManager = getFragmentManager().beginTransaction();
                    if (tab.getPosition() == 0) {
                        mManager.setCustomAnimations(R.anim.slide_in_middle_from_left, R.anim.slide_in_right);
                        mManager.hide(mStep2);
                        mManager.show(mStep1);
                    } else {
                        mManager.setCustomAnimations(R.anim.slide_in_middle_from_right, R.anim.slide_in_left);
                        mManager.hide(mStep1);
                        mManager.show(mStep2);
                    }
                    isFragmentAnimated = true;
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(400);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            isFragmentAnimated = false;
                        }
                    });
                    thread.start();
                    mManager.commit();
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }
    @SuppressLint("ResourceType")
    public void ShowFragment(int fragID){
    switch (fragID){
        case 1:
            TabLayout.Tab tab1 = tabLayout.getTabAt(0);
            tab1.select();
            break;
        case 2:
            tabs.getChildAt(1).setEnabled(true);
            tabs.getChildAt(1).setClickable(true);
            tabs.getChildAt(1).setAlpha(1f);
            TabLayout.Tab tab = tabLayout.getTabAt(1);
            tab.select();
            break;
    }
}
    public String getUserPassword() {
        return userPassword;
    }
    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }
    public String getUserEmail() {
        return userEmail;
    }
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}


