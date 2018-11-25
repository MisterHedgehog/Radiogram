package com.MMGS.radiogram.main_activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.MMGS.radiogram.GlideApp;
import com.MMGS.radiogram.classes.DownloadListener;
import com.MMGS.radiogram.classes.ImageDownloaderListener;
import com.bumptech.glide.Glide;
import com.MMGS.radiogram.R;
import com.MMGS.radiogram.classes.FirebaseManager;
import com.MMGS.radiogram.classes.Loss;
import com.MMGS.radiogram.classes.User;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class AddLossActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mEditLossButton, mDoneLossButton;
    private ImageView mLossImage;
    private TextView mNameText, mChangeName, mDescription, mDateTitle, mDateText, mChangeDate;
    private android.support.v7.widget.Toolbar mToolbar;

    private Context mContext;
    private Activity mActivity;
    //private Intent mGalleryActivity, mPhotoActivity;
    private Bitmap mLossBitmap;

    private DatabaseReference mDatabaseRef;
    private FirebaseUser mUser;

    private String mDate;

    static public final String ACTIVITY_TYPE = "ActivityType";
    static public final int ADD_LOSS_ACTIVITY = 0;
    static public final int ADD_FIND_ACTIVITY = 1;
    static public final int EDIT_LOSS_ACTIVITY = 2;
    static public final int EDIT_FIND_ACTIVITY = 3;
    private final String[] ITEMS = {"Флешка", "Кофта", "Телефон", "Другое"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_loss);
        SetViews();
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        SetupViews();
        mEditLossButton.setOnClickListener(this);
        mLossImage.setOnClickListener(this);
        mDoneLossButton.setOnClickListener(this);
        mChangeName.setOnClickListener(this);
        mChangeDate.setOnClickListener(this);
    }
    private void SetViews() {
        mEditLossButton = findViewById(R.id.loss_edit_photo_button);
        mDoneLossButton = findViewById(R.id.loss_done);
        mLossImage = findViewById(R.id.loss_photo);
        mLossBitmap = null;
        mNameText = findViewById(R.id.loss_name_text);
        mChangeName = findViewById(R.id.loss_name_button);
        mDescription = findViewById(R.id.loss_description);
        mToolbar = findViewById(R.id.toolbar_edit);
        mDateTitle = findViewById(R.id.date_thing);
        mDateText = findViewById(R.id.date_text);
        mChangeDate = findViewById(R.id.date_text_button);

        mContext = this;
        mActivity = this;
        //mGalleryActivity = new Intent(Intent.ACTION_PICK);
        //mGalleryActivity.setType("image/*");
        //mPhotoActivity = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mDate = "0";
    }
    private void SetupViews() {
        switch (getIntent().getIntExtra(ACTIVITY_TYPE, ADD_LOSS_ACTIVITY)) {
            case ADD_LOSS_ACTIVITY:
                mToolbar.setTitle("Добавить потерю");
                mDateTitle.setText("Дата потери:");
                break;
            case ADD_FIND_ACTIVITY:
                mToolbar.setTitle("Добавить находку");
                mDateTitle.setText("Дата находки:");
                break;
            case EDIT_LOSS_ACTIVITY:
                mToolbar.setTitle("Изменить потерю");
                mDateTitle.setText("Дата потери:");
                LoadUser("losses");
                break;
            case EDIT_FIND_ACTIVITY:
                mToolbar.setTitle("Изменить находку");
                mDateTitle.setText("Дата находки:");
                LoadUser("finds");
                break;
        }
        mNameText.setText(getResources().getString(R.string.flesh));
        mDateText.setText(DateFormat.format("dd.MM.yyyy",
                new Date().getTime()));
    }
    private void LoadUser(final String ref) {
        String id = getIntent().getStringExtra("lossID");
        mDatabaseRef.child(ref).child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Loss loss = dataSnapshot.getValue(Loss.class);
                assert loss != null;
                mNameText.setText(loss.getName());
                mDescription.setText(loss.getDescription());
                mDateText.setText(loss.getDate());
                mDate = loss.getDate();
                FirebaseManager.downloadImage(mContext, ref, loss.getKey(), "main",
                        (bitmap, fromData) -> {
                    mLossImage.setImageBitmap(bitmap);
                    if(fromData) Toast.makeText(mContext, "true", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mLossImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (resultCode == RESULT_OK) {
            Toast.makeText(mContext, "Изображение успешно загружено", Toast.LENGTH_LONG).show();
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
                    mLossBitmap = BitmapFactory.decodeFile(result.getUri().getPath());
                    mLossImage.setImageBitmap(mLossBitmap);
            }
        }
    private void SaveLossInDatabase(final int type) {
        final String ref = ((type == ADD_LOSS_ACTIVITY) || (type == EDIT_LOSS_ACTIVITY)) ? "losses" : "finds";
        final String name;
        final String date = mDate.equals("0") ?
                (new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH).format(new Date())) : mDate;
        final String description;
        final String userID = mUser.getUid();
        if (!mNameText.getText().toString().isEmpty()) {
            name = String.valueOf(mNameText.getText());
        } else {
            Toast.makeText(mContext, "Необходимо назвать вещь.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!mDescription.getText().toString().equals("")) {
            description = String.valueOf(mDescription.getText());
        } else {
            description = getResources().getString(R.string.no_description);
        }
        if (((type == ADD_LOSS_ACTIVITY) || (type == ADD_FIND_ACTIVITY)) && mLossBitmap == null) {
                Toast.makeText(mContext, "Необходимо добавить изображение.", Toast.LENGTH_SHORT).show();
                return;
            }
        final ProgressDialog dialog = new ProgressDialog(mActivity);
        dialog.setMessage("Сохранение данных на сервер...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.show();
        if ((type == EDIT_LOSS_ACTIVITY) || (type == EDIT_FIND_ACTIVITY)) {
            DatabaseReference pUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
            pUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    final User user = dataSnapshot.getValue(User.class);
                    assert user != null;
                    final String id = getIntent().getStringExtra("lossID");
                    final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(ref).child(id);
                    reference.runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                            Loss loss = new Loss(user.getuName() + " " + user.getuSurname(),
                                    userID,
                                    name,
                                    date,
                                    id,
                                    description);
                            mutableData.setValue(loss);
                            return Transaction.success(mutableData);
                        }
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                            if (mLossBitmap != null) {
                                FirebaseManager.saveImage(mContext, mLossBitmap, ref, id,
                                        "main", isSuccess -> {
                                            dialog.dismiss();
                                            mActivity.finish();
                                        });
                            } else {
                                dialog.dismiss();
                                mActivity.finish();
                            }
                        }
                    });
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    dialog.dismiss();
                    mActivity.finish();
                }
            });
        } else {
            DatabaseReference pUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
            pUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String key = mDatabaseRef.child(ref).push().getKey();
                    User user = dataSnapshot.getValue(User.class);
                    assert user != null;
                    assert key != null;
                    mDatabaseRef.child(ref).child(key).setValue(
                            new Loss(user.getuName() + " " + user.getuSurname(),
                                    userID,
                                    name,
                                    date,
                                    key,
                                    description));
                        FirebaseManager.saveImage(mContext, mLossBitmap, ref, key,
                                "main", isSuccess -> {
                                    dialog.dismiss();
                                    mActivity.finish();
                                });
                        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(ref).child(key).child("time");
                    reference.setValue(ServerValue.TIMESTAMP)
                            .addOnSuccessListener(aVoid -> reference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                            long time = dataSnapshot1.getValue(Long.class);
                            time = -time;
                            reference.setValue(time);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    }));
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    dialog.dismiss();
                    mActivity.finish();
                }
            });
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.complete_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_complete:
                SaveLossInDatabase(getIntent().getIntExtra(ACTIVITY_TYPE, ADD_LOSS_ACTIVITY));
                break;
        }
        return true;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.loss_done :
                SaveLossInDatabase(getIntent().getIntExtra(ACTIVITY_TYPE, ADD_LOSS_ACTIVITY));
                break;
            case R.id.loss_name_button :
                AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                View view = LayoutInflater.from(mContext).inflate(R.layout.edit_text, null);
                final EditText editText = view.findViewById(R.id.edit_new_name_text);
                editText.setText(mNameText.getText());
                dialog.setTitle("Изменение наименования");
                dialog.setView(view);
                final int[] checked = {ITEMS.length - 1};
                dialog.setSingleChoiceItems(ITEMS, checked[0], new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checked[0] = which;
                    }
                });
                dialog.setPositiveButton("Готово", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (checked[0] == ITEMS.length - 1)
                            if (editText.getText().toString().equals(""))
                                Toast.makeText(mContext, "Неоходимо назвать вещь", Toast.LENGTH_LONG).show();
                            else
                                mNameText.setText(String.valueOf(editText.getText()));
                        else
                            mNameText.setText(ITEMS[checked[0]]);
                    }
                });
                dialog.create().show();
                break;
            case R.id.date_text_button :
                DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month++;
                        String Year = String.valueOf(year);
                        String Month = (month < 10) ? "0" + String.valueOf(month) : String.valueOf(month);
                        String Day = (dayOfMonth < 10) ? "0" + String.valueOf(dayOfMonth) : String.valueOf(dayOfMonth);
                        mDate = Day + "." + Month + "." + Year;
                        mDateText.setText(mDate);
                    }
                };
                Calendar calendar = new GregorianCalendar();
                DatePickerDialog dialog1 = new DatePickerDialog(
                        mContext,
                        listener,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                dialog1.show();
                break;
            case R.id.loss_edit_photo_button :
            case R.id.loss_photo :
                CropImage.activity()
                        .setAspectRatio(3,2)
                        .setMaxCropResultSize(3000,2000)
                        .setMinCropResultSize(300,200)
                        .setActivityTitle("Выбор изображения")
                        .setCropMenuCropButtonTitle("Готово")
                        .setAllowCounterRotation(false)
                        .setAllowFlipping(false)
                        .setAllowRotation(false)
                        .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                        .setOutputCompressQuality(80)
                        .setRequestedSize(900,600)
                        .start(mActivity);
                break;
        }
    }
}