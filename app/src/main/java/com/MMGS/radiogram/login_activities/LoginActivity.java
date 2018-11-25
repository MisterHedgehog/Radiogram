package com.MMGS.radiogram.login_activities;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.MMGS.radiogram.R;
import com.MMGS.radiogram.main_activities.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText mPasswordView;
    private EditText mEmailView;
    private Button mSignInButton;
    private Button mRegisterButton;
    private Button mResetPasswordButton;
    private ImageView mEyePasswordImageView;
    private TextInputLayout mEmailLayout;
    private CoordinatorLayout mPasswordLayout;
    private ProgressDialog dialog;

    private Activity activity;
    private Intent mRegIntent;
    private Intent mMainIntent;

    private DatabaseReference database;

    private final int APP_VERSION = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        database = FirebaseDatabase.getInstance().getReference();
        activity = this;
        mMainIntent = new Intent(activity, MainActivity.class);
        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setMessage("Соединение с сервером...");
        dialog.show();
        super.onCreate(savedInstanceState);
    }
    @Override
    protected void onResume() {
        isUserSignIn();
        super.onResume();
    }

    @Override
    protected void onStop() {
        activity.setContentView(new View(activity), new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        super.onStop();
    }

    private void isUserSignIn(){
        database.child("version").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dialog.dismiss();
                //noinspection ConstantConditions
                int version = dataSnapshot.getValue(Integer.class);
                if(version == 0){
                    activity.setContentView(R.layout.start_activity);
                } else
                if (APP_VERSION >= version) {
                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                        startActivity(mMainIntent);
                    } else SetView();
                } else {

                    activity.setContentView(R.layout.update_version);
                    findViewById(R.id.update_button).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.MMGS.radiogram")));
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }
    private void SetView() {
        setContentView(R.layout.activity_login);

        mRegIntent = new Intent(this, RegActivity.class);
        mEmailLayout = findViewById(R.id.email_layout);
        mPasswordLayout = findViewById(R.id.password_layout);
        mPasswordView = findViewById(R.id.password);
        mResetPasswordButton = findViewById(R.id.forgot_button);
        mEmailView = findViewById(R.id.email);
        mEyePasswordImageView = findViewById(R.id.eye);
        mSignInButton = findViewById(R.id.sign_in_button);
        mRegisterButton = findViewById(R.id.reg_button);
        PlayAnim();
        mEyePasswordImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPasswordView.getInputType()==(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)){
                    mPasswordView.setInputType(InputType.TYPE_CLASS_TEXT |InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    mPasswordView.setSelection(mPasswordView.getText().length());
                    mEyePasswordImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_visibility_white_24dp));
                }
                else {
                    mPasswordView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    mPasswordView.setSelection(mPasswordView.getText().length());
                    mEyePasswordImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_visibility_off_white_24dp));
                }
            }
        });
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCorrectPass())
                SignIn(mEmailView.getText().toString(), mPasswordView.getText().toString());
            }
        });
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(mRegIntent);
            }
        });
        mResetPasswordButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mEmailView.getText().toString().length() > 7){
                    FirebaseAuth.getInstance().sendPasswordResetEmail(mEmailView.getText().toString());
                    Snackbar.make(v,"Пароль выслан по адресу электронной почты.", Snackbar.LENGTH_SHORT).show();
                } else Snackbar.make(v,"Короткий адрес электронной почты.", Snackbar.LENGTH_SHORT).show();
            }
        });
    }
    private void SignIn(String email, String password) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    startActivity(mMainIntent);
                } else Toast.makeText(LoginActivity.this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void PlayAnim(){
        mPasswordLayout.startAnimation( AnimationUtils.loadAnimation(this,R.anim.password));
        mEmailLayout.startAnimation( AnimationUtils.loadAnimation(this,R.anim.login));
        mSignInButton.startAnimation( AnimationUtils.loadAnimation(this,R.anim.button_enter));
        mRegisterButton.startAnimation( AnimationUtils.loadAnimation(this,R.anim.button_reg));
        mResetPasswordButton.startAnimation(AnimationUtils.loadAnimation(this,R.anim.button_reg));

    }
    private boolean isCorrectPass(){
        if(mPasswordView.getText().length()<6){
            if(mPasswordView.getText().length()==0 || mEmailView.getText().length()==0){
                Toast.makeText(activity, getResources().getText(R.string.reg_password_settings_4), Toast.LENGTH_LONG).show();
                return false;
            }
            Toast.makeText(activity, getResources().getText(R.string.reg_password_settings_3), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}