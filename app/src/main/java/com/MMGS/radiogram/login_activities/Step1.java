package com.MMGS.radiogram.login_activities;

/**
 * Created by Андрюшка on 29.01.2018.
 */

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.MMGS.radiogram.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class Step1 extends Fragment {

    private EditText pass_1, pass_2,rEmailText;

    private RegActivity regActivity;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.step1, container, false);
        mAuth = FirebaseAuth.getInstance();
        regActivity = (RegActivity) getActivity();

        rEmailText=v.findViewById(R.id.email);
        pass_1=v.findViewById(R.id.pass_1);
        pass_2=v.findViewById(R.id.pass_2);
        Button nextB = v.findViewById(R.id.next_reg);
        nextB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pass_1.getText().length()==0 || pass_2.getText().length()==0 || rEmailText.getText().length()==0){
                    Toast.makeText(regActivity, getResources().getText(R.string.reg_password_settings_4), Toast.LENGTH_LONG).show();
                    ChangeKeyBoard();
                }
                else if(pass_1.getText().length() < 6) {
                    Toast.makeText(regActivity, getResources().getText(R.string.reg_password_settings_3), Toast.LENGTH_LONG).show();
                }
                else if(!pass_1.getText().toString().equals(pass_2.getText().toString())){
                    Toast.makeText(regActivity, getResources().getText(R.string.reg_password_settings_5), Toast.LENGTH_LONG).show();
                        pass_2.setText("");
                        pass_2.requestFocus();
                        ChangeKeyBoard();
                    }
                    else
                        Register(rEmailText.getText().toString(),pass_1.getText().toString());
                }

        });
        return v;
    }


    private void Register(final String email, final String password) {
        final ProgressDialog dialog = new ProgressDialog(regActivity);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setMessage("Синхронизация электронной почты...");
        dialog.show();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(regActivity, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    dialog.dismiss();
                    Objects.requireNonNull(mAuth.getCurrentUser()).delete();
                    regActivity.setUserEmail(email);
                    regActivity.setUserPassword(password);
                    Toast.makeText(regActivity, "Ещё немного данных...", Toast.LENGTH_SHORT).show();
                    regActivity.ShowFragment(2);
                } else  if (Objects.requireNonNull(task.getException()).getMessage().equals("The email address is already in use by another account.")) {
                    rEmailText.requestFocus();
                    ChangeKeyBoard();
                    dialog.dismiss();
                }else  //noinspection ThrowableNotThrown
                    if (task.getException().getMessage().equals("The email address is badly formatted.")) {
                    dialog.dismiss();
                    Toast.makeText(regActivity, "Данный адресс не существует", Toast.LENGTH_LONG).show();
                    rEmailText.requestFocus();
                    ChangeKeyBoard();
                } else {
                    dialog.dismiss();
                    Toast.makeText(regActivity, task.getException().getMessage() , Toast.LENGTH_SHORT).show();
                    }

            }
        });
    }
    private void ChangeKeyBoard(){
        if(regActivity.getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) regActivity.getSystemService(INPUT_METHOD_SERVICE);
            assert imm != null;
            imm.hideSoftInputFromWindow(regActivity.getCurrentFocus().getWindowToken(), 0);
        }
    }

}
