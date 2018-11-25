package com.MMGS.radiogram.login_activities;

/**
 * Created by Андрюшка on 29.01.2018.
 */

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.MMGS.radiogram.classes.FirebaseManager;
import com.MMGS.radiogram.R;
import com.MMGS.radiogram.classes.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;


public class Step2 extends Fragment {

    private EditText rNameT, rSurnameT;
    private CircleImageView circleImageView;
    private RegActivity regActivity;
    private Context context;
    private Step2 fragment;

    private Intent cameraIntent;
    private Intent photoPickerIntent;

    private final int PHOTO_RESULT = 0;
    private final int GALLERY_RESULT = 1;

    private Bitmap userBitmap;

    private DatabaseReference database;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.step2, container, false);
        regActivity =  (RegActivity) getActivity();
        context = this.getActivity();
        fragment = this;

        cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        userBitmap = null;

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        rNameT = rootView.findViewById(R.id.name);
        rSurnameT = rootView.findViewById(R.id.sur_name);
        Button rCompleteButton = rootView.findViewById(R.id.complete_button);
        circleImageView = rootView.findViewById(R.id.profile_image);

        rCompleteButton.setOnClickListener(view -> {
            if(userBitmap == null) {
                Toast.makeText(regActivity, "Вам неоходимо добавить изображение.", Toast.LENGTH_LONG).show();
            } else if(rNameT.getText().length() < 1){
                Toast.makeText(regActivity, "Строка для имени пуста.", Toast.LENGTH_LONG).show();
            } else if(rSurnameT.getText().length() < 1){
                Toast.makeText(regActivity, "Строка для фамилии пуста.", Toast.LENGTH_LONG).show();
            } else SignIn(regActivity.getUserEmail(),regActivity.getUserPassword());
        });

        circleImageView.setOnClickListener(view -> CropImage.activity()
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
                .setRequestedSize(400,400)
                .start(context, fragment));
        return rootView;
    }
    @Override
    public void onResume() {
        super.onResume();
        if(userBitmap != null)
            circleImageView.setImageBitmap(userBitmap);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        circleImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (resultCode == RESULT_OK) {
            Toast.makeText(context, "Изображение успешно загружено", Toast.LENGTH_LONG).show();
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            userBitmap = BitmapFactory.decodeFile(result.getUri().getPath());
            circleImageView.setImageBitmap(userBitmap);
        }
    }

    private void SignIn(final String email, final String password) {
        final ProgressDialog d = new ProgressDialog(context);
        d.setCancelable(false);
        d.setMessage("Сохранение данных...");
        d.setIndeterminate(true);
        d.show();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(regActivity, task -> {
            if (task.isSuccessful()) {
                currentUser = FirebaseAuth.getInstance().getCurrentUser();
                int color = getResources().getColor(R.color.colorAccent);
                User user = new User(
                        rNameT.getText().toString(),
                        rSurnameT.getText().toString(),
                        email,
                        password,
                        color);
                FirebaseManager.SaveImageAndUserToDatabase(context, userBitmap,
                        "images",
                        currentUser.getUid(),
                        "icon",
                        user, regActivity, d);
            }
        });

        }
}