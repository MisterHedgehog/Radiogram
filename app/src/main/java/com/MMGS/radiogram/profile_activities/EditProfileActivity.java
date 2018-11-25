package com.MMGS.radiogram.profile_activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.MMGS.radiogram.R;
import com.MMGS.radiogram.classes.DownloadListener;
import com.MMGS.radiogram.classes.FirebaseManager;
import com.MMGS.radiogram.classes.ImageDownloaderListener;
import com.MMGS.radiogram.classes.User;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.auth.data.remote.GoogleSignInHandler;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
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
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.acl.Group;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private Context context;
    private AppCompatActivity activity;
    private LayoutInflater layoutInflater;
    private AlertDialog colorPickerDialog, eChooseGroupDialog, eChooseKursDialog, eChooseColorDialog,
            eChooseTelephoneDialog, eChooseAdresDialog, chooseThinkDialog;
    private DatePickerDialog eChooseBirthdayDialog;
    private ProgressDialog eProgressDialog;
    private RewardedVideoAd eRewardedVideoAd;

    private Button eBackgroundButton, ePhotoButton, eColorButton;
    private CircleImageView eUserPhoto, eUserBackground;
    private EditText eNameText, eSurnameText;
    private TextView eGroupText, eKursText, eTelephoneText, eAdresText, eBirthdayText, thinkText, thinkTextChange,
            eGroupTextChange, eKursTextChange, eTelephoneTextChange, eAdresTextChange, eBirthdayTextChange;
    private Toolbar eToolbar;

    private final int BACKGROUND_RESULT = 0;
    private final int AVATAR_RESULT = 1;

    private static final String AD_UNIT_ID = "ca-app-pub-2972366758632504/3657535020";
    // это ID рекламы, было при тесте: ca-app-pub-3940256099942544/5224354917
    // не тест: ca-app-pub-2972366758632504/7893798165
    // или ca-app-pub-2972366758632504/3657535020
    // или ca-app-pub-2972366758632504/2220324109

    private static final String APP_ID = "ca-app-pub-2972366758632504~4441622403";
    private Bitmap eUserBackgroundBitmap;
    private Bitmap eUserPhotoBitmap;
    private String[] eGroups = {
            "51191", "51391", "51392", "51591", "51592",
            "61111", "61191", "61391", "61591",
            "7K1111", "7K1191", "7K1391", "7K1591",
            "8K1111", "8K1191", "8K1391", "8K1591",

            "52491", "52492", "52493",
            "62411", "62491", "62492", "62493",
            "7K2411", "7K2491", "7K2492", "7K2493",
            "8K2411", "8K2491", "8K2492", "8K2493",

            "53291", "53292", "53791",
            "63291", "63791",
            "7K3291", "7K3791",
            "8K3291", "8K3791",
    };
    private String[] eKurses = {"1", "2", "3", "4"};
    private int eSelectedUserColor;
    private String eDate;
    private long eUserLastWatchedAddMobTime;

    private StorageReference eStorageRef;
    private FirebaseUser eUser;
    private DatabaseReference eUserRef, eTimeRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        SetView();
        eColorButton.setOnClickListener(this);
        eBackgroundButton.setOnClickListener(this);
        eUserBackground.setOnClickListener(this);
        ePhotoButton.setOnClickListener(this);
        eUserPhoto.setOnClickListener(this);
        eKursTextChange.setOnClickListener(this);
        eGroupTextChange.setOnClickListener(this);
        eAdresTextChange.setOnClickListener(this);
        eTelephoneTextChange.setOnClickListener(this);
        eBirthdayTextChange.setOnClickListener(this);
        thinkTextChange.setOnClickListener(this);
        setSupportActionBar(eToolbar);
        // Добавляем изображения пользавателя для дальнейшей их замены
        FirebaseManager.downloadImage(context, "images", eUser.getUid(), "icon_big",
                (bitmap, fromData) -> {
            eUserBackground.setImageBitmap(bitmap);
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) eUserBackground.getLayoutParams();
            params.height = eUserBackground.getWidth();
            eUserBackground.setLayoutParams(params);
        });
        FirebaseManager.downloadImage(context, "images", eUser.getUid(), "icon",
                (bitmap, fromData) -> eUserPhoto.setImageBitmap(bitmap));
        //Добавляем имя и фамилию пользователя для дальнейшего редактирования
        eUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    User user = dataSnapshot.getValue(User.class);
                    eSelectedUserColor = user.getuColor();
                    eNameText.setText(user.getuName());
                    eSurnameText.setText(user.getuSurname());
                    eUserLastWatchedAddMobTime = user.getLastAddMobTime();
                    eUserPhoto.setBorderColor(user.getuColor());
                    if (!user.getuGroup().equals("non"))
                        eGroupText.setText(user.getuGroup());
                    if (!user.getThink().equals("non"))
                        thinkText.setText(user.getThink());
                    if (!user.getuKurs().equals("non"))
                        eKursText.setText(user.getuKurs());
                    if (!user.getuAdres().equals("non"))
                        eAdresText.setText(user.getuAdres());
                    if (!user.getuTelephone().equals("non"))
                        eTelephoneText.setText(user.getuTelephone());
                    if (!user.getuBirthday().equals("00.00.0000"))
                        eBirthdayText.setText(user.getuBirthday());
                } catch (Exception e){
                    Toast.makeText(context, "Error: Ошибка доступа к пользователю\nJava: EditProfileActivity:onCreate\n"
                            + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "Нет доступа к серверу.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void SetView() {
        context = this;
        activity = this;
        layoutInflater = LayoutInflater.from(context);
        eStorageRef = FirebaseStorage.getInstance().getReference();
        eUser = FirebaseAuth.getInstance().getCurrentUser();
        eUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(eUser.getUid());
        eTimeRef = FirebaseDatabase.getInstance().getReference().child("time");
        //Intent eStorageIntent = new Intent(Intent.ACTION_PICK);
        //eStorageIntent.setType("image/*");

        eBackgroundButton = findViewById(R.id.edit_background_button);
        ePhotoButton = findViewById(R.id.edit_photo_button);
        eUserPhoto = findViewById(R.id.circle_image);
        eUserBackground = findViewById(R.id.imageView);
        eToolbar = findViewById(R.id.toolbar2);
        eNameText = findViewById(R.id.edit_name_text);
        thinkText = findViewById(R.id.think_text);
        eSurnameText = findViewById(R.id.edit_surname_text);
        eGroupText = findViewById(R.id.group_text);
        eKursText = findViewById(R.id.kurs_text);
        eGroupTextChange = findViewById(R.id.group_text_button);
        eKursTextChange = findViewById(R.id.kurs_text_button);
        eAdresText = findViewById(R.id.adres_text);
        eBirthdayText = findViewById(R.id.birthday_text);
        eTelephoneText = findViewById(R.id.telephone_text);
        eAdresTextChange = findViewById(R.id.adres_text_button);
        eTelephoneTextChange = findViewById(R.id.telephone_text_button);
        eBirthdayTextChange = findViewById(R.id.birthday_text_button);
        thinkTextChange = findViewById(R.id.think_text_button);

        eColorButton = findViewById(R.id.edit_color_button);
        eProgressDialog = new ProgressDialog(context);
        eUserPhotoBitmap = null;
        eUserBackgroundBitmap = null;
        eDate = "0";
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Toast.makeText(context, "Изображение успешно загружено", Toast.LENGTH_LONG).show();
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            switch (requestCode) {
                case BACKGROUND_RESULT:
                    eUserBackgroundBitmap = BitmapFactory.decodeFile(result.getUri().getPath());
                    eUserBackground.setImageBitmap(eUserBackgroundBitmap);
                    break;
                case AVATAR_RESULT:
                    eUserPhotoBitmap = BitmapFactory.decodeFile(result.getUri().getPath());
                    eUserPhoto.setImageBitmap(eUserPhotoBitmap);
                    break;
                    }
            }
        }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.complete_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_complete) {
            final String name = eNameText.getText().toString();
            final String surname = eSurnameText.getText().toString();
            if(surname.isEmpty()){
                Toast.makeText(context, "Фамилия не указана.", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (name.isEmpty()) {
                Toast.makeText(context, "Имя не указано.", Toast.LENGTH_SHORT).show();
                return false;
        }
            final String adres = eAdresText.getText().toString();
            final String telephone = eTelephoneText.getText().toString();
            final String kurs = eKursText.getText().toString();
            final String group = eGroupText.getText().toString();
            final String think = thinkText.getText().toString();
            final String birthday = eBirthdayText.getText().toString();
            final ProgressDialog dialog = new ProgressDialog(context);
            dialog.setMessage("Сохранение изображения на сервер...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();
            eUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    assert user != null;
                    user.setuName(name);
                    user.setuSurname(surname);
                    if (!adres.equals("Информация отсутствует"))
                        user.setuAdres(adres);
                    if (!telephone.equals("Информация отсутствует"))
                        user.setuTelephone(telephone);
                    if (!think.equals("Место для цитаты"))
                        user.setThink(think);
                    if (!kurs.equals("Курс"))
                        user.setuKurs(kurs);
                    if (!group.equals("Группа"))
                        user.setuGroup(group);
                    if (!birthday.equals("Информация отсутствует"))
                        user.setuBirthday(birthday);
                    eUserRef.setValue(user);

                    if (eUserBackgroundBitmap == null && eUserPhotoBitmap == null) {
                        dialog.dismiss();
                        finish();
                    } else {
                        if (eUserBackgroundBitmap != null)
                            FirebaseManager.saveImage(context, eUserBackgroundBitmap,
                                    "images", eUser.getUid(), "icon_big", isSuccess -> {
                                        dialog.dismiss();
                                        activity.finish();
                                    });
                        if (eUserPhotoBitmap != null)
                            FirebaseManager.saveImage(context, eUserPhotoBitmap,
                                    "images", eUser.getUid(), "icon", isSuccess -> {
                                        dialog.dismiss();
                                        activity.finish();
                                    });
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        return true;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.edit_color_button :
                if (colorPickerDialog == null) {
                    if(eChooseColorDialog == null){
                        eChooseColorDialog = new AlertDialog.Builder(context).create();
                        @SuppressLint("InflateParams")
                        View view = layoutInflater.inflate(R.layout.choose_color, null);
                        eChooseColorDialog.setView(view);
                        eChooseColorDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Далее", (dialog, which) -> MakeAdMob());
                        eChooseColorDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Назад", (DialogInterface.OnClickListener) null);
                    }
                    colorPickerDialog = ColorPickerDialogBuilder
                            .with(context)
                            .setTitle("Изменение цвета обводки")
                            .initialColor(eSelectedUserColor)
                            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                            .density(8)
                            .setOnColorSelectedListener(selectedColor -> eSelectedUserColor = selectedColor)
                            .setPositiveButton("Далее", (dialog, selectedColor, allColors) ->
                                    eTimeRef.setValue(ServerValue.TIMESTAMP).addOnSuccessListener(aVoid -> eTimeRef
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            //noinspection ConstantConditions
                                            final long time = dataSnapshot.getValue(long.class);
                                            eUserRef.child("lastAddMobTime").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    //noinspection ConstantConditions
                                                    eUserLastWatchedAddMobTime  = dataSnapshot.getValue(long.class);
                                                    // timeOffset: 600 000 = 10 минут
                                                    long offset = 600000 - (time - eUserLastWatchedAddMobTime);
                                                    if (offset < 0) {
                                                        eChooseColorDialog.show();
                                                    } else {
                                                        eUserRef.runTransaction(new Transaction.Handler() {
                                                            @NonNull
                                                            @Override
                                                            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                                                User user = mutableData.getValue(User.class);
                                                                assert user != null;
                                                                user.setuColor(eSelectedUserColor);
                                                                mutableData.setValue(user);
                                                                return Transaction.success(mutableData);
                                                            }

                                                            @Override
                                                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                                                                eUserPhoto.setBorderColor(eSelectedUserColor);
                                                            }
                                                        });
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    })))
                            .setNegativeButton("Назад", (dialog, which) -> {
                            })
                            .build();
                }
                colorPickerDialog.show();
                break;
            case R.id.imageView :
            case R.id.edit_background_button:
                Intent backgroundIntent = CropImage.activity()
                        .setAspectRatio(2,1)
                        .setMaxCropResultSize(4000,2000)
                        .setMinCropResultSize(800,400)
                        .setActivityTitle("Выбор фона")
                        .setCropMenuCropButtonTitle("Готово")
                        .setAllowCounterRotation(false)
                        .setAllowFlipping(false)
                        .setAllowRotation(false)
                        .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                        .setOutputCompressQuality(80)
                        .setRequestedSize(1200,600)
                        .getIntent(context);
                startActivityForResult(backgroundIntent, BACKGROUND_RESULT);

                break;
            case R.id.edit_photo_button :
            case R.id.circle_image :
                Intent intent = CropImage.activity()
                        .setAspectRatio(1,1)
                        .setMaxCropResultSize(4000,4000)
                        .setMinCropResultSize(200,200)
                        .setActivityTitle("Выбор фотографии")
                        .setCropMenuCropButtonTitle("Готово")
                        .setAllowCounterRotation(false)
                        .setAllowFlipping(false)
                        .setAllowRotation(false)
                        .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
                        .setOutputCompressQuality(80)
                        .setRequestedSize(400,400)
                        .getIntent(context);
                startActivityForResult(intent, AVATAR_RESULT);
                break;
            case R.id.kurs_text_button :
                if (eChooseKursDialog == null)
                    eChooseKursDialog = CreateChooseDialog("Курс", eKurses, eKursText);
                eChooseKursDialog.show();
                break;
            case R.id.group_text_button :
                if (eChooseGroupDialog == null)
                    eChooseGroupDialog = CreateChooseDialog("Номер группы", eGroups, eGroupText);
                eChooseGroupDialog.show();
                break;
            case R.id.adres_text_button :
                if (eChooseAdresDialog == null) {
                    eChooseAdresDialog = new AlertDialog.Builder(context).create();
                    eChooseAdresDialog.setTitle("Место жительства");
                    @SuppressLint("InflateParams") final View view = layoutInflater.inflate(R.layout.edit_text, null);
                    final EditText text = view.findViewById(R.id.edit_new_name_text);
                    text.setHint("Место для адреса");
                    eChooseAdresDialog.setView(view);
                    eChooseAdresDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Готово", (dialog, which) -> {
                        if (text.getText().length() > 3)
                            eAdresText.setText(text.getText().toString());
                        else
                            Toast.makeText(context, "Короткий адрес", Toast.LENGTH_LONG).show();
                    });
                }
                eChooseAdresDialog.show();
                break;
            case R.id.think_text_button :
                if (chooseThinkDialog == null) {
                    chooseThinkDialog = new AlertDialog.Builder(context).create();
                    chooseThinkDialog.setTitle("Цитата");
                    @SuppressLint("InflateParams")
                    final View view = layoutInflater.inflate(R.layout.edit_text, null);
                    final EditText text = view.findViewById(R.id.edit_new_name_text);
                    text.setHint("Место для цитаты");
                    chooseThinkDialog.setView(view);
                    chooseThinkDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Готово", (dialog, which) -> {
                        if (text.getText().length() > 3)
                            thinkText.setText(text.getText().toString());
                        else
                            Toast.makeText(context, "Короткая цитата", Toast.LENGTH_LONG).show();
                    });
                }
                chooseThinkDialog.show();
                break;
            case R.id.telephone_text_button :
                if (eChooseTelephoneDialog == null) {
                    eChooseTelephoneDialog = new AlertDialog.Builder(context).create();
                    eChooseTelephoneDialog.setTitle("Номер телефона");
                    @SuppressLint("InflateParams") final View view = layoutInflater.inflate(R.layout.choose_telephone, null);
                    final EditText number = view.findViewById(R.id.number);
                    final EditText kod = view.findViewById(R.id.kod);
                    eChooseTelephoneDialog.setView(view);
                    eChooseTelephoneDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Готово", (dialog, which) -> {
                        if (kod.getText().length() == 2 && number.getText().length() == 7)
                            eTelephoneText.setText(String.format("+375 (%s) %s", kod.getText().toString(), number.getText().toString()));
                        else
                            Toast.makeText(context, "Неверный формат номера", Toast.LENGTH_LONG).show();
                    });
                }
                eChooseTelephoneDialog.show();
                break;
            case R.id.birthday_text_button :
                if (eChooseBirthdayDialog == null) {
                    DatePickerDialog.OnDateSetListener myCallBack = (view, year, month, dayOfMonth) -> {
                        month++;
                        String Year = String.valueOf(year);
                        String Month = (month < 10) ? "0" + String.valueOf(month) : String.valueOf(month);
                        String Day = (dayOfMonth < 10) ? "0" + String.valueOf(dayOfMonth) : String.valueOf(dayOfMonth);
                        eDate = Day + "." + Month + "." + Year;
                        eBirthdayText.setText(eDate);
                    };
                    eChooseBirthdayDialog = new DatePickerDialog(context, myCallBack, 2000, 3, 27);
                }
                eChooseBirthdayDialog.show();
                break;
        }
    }
    private AlertDialog CreateChooseDialog(final String title, final String[] components, final TextView focusText) {
        final AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(title);
        adb.setSingleChoiceItems(components, -1, (dialog, which) -> {
            if (title.equals("Номер группы")) {
                String group = components[which];
                eGroupText.setText(group);
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR); // год на дворе в данный момент, например, 2018
                int month = calendar.get(Calendar.MONTH);
                int firstNumber = Integer.parseInt(String.valueOf(group.charAt(0)));
                if (month < 6) // типо если ещё март, то уже новый год (2019), но год учебный старый (2018)
                    year--;
                int kurs = year - 2009 - firstNumber;
                eKursText.setText(String.valueOf(kurs));

            } else focusText.setText(components[which]);
        });
        adb.setNeutralButton(getString(R.string.complete_2), null);
        return adb.create();
    }
    private void MakeAdMob() {
        eProgressDialog.setMessage("Загрузка видеоролика...");
        eProgressDialog.setIndeterminate(true);
        eProgressDialog.setCancelable(false);
        eProgressDialog.show();
        // это моё ID (при тесте можно оставить таким же) из базы AdMob, подключаемся к аккаунту
        MobileAds.initialize(context, APP_ID);
        eRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(context);
        eRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLeftApplication() {
            }

            @Override
            public void onRewardedVideoAdClosed() {
            }

            @Override
            public void onRewarded(RewardItem reward) {
                eUserRef.runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                        User user = mutableData.getValue(User.class);
                        assert user != null;
                        user.setuColor(eSelectedUserColor);
                        mutableData.setValue(user);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                        eUserPhoto.setBorderColor(eSelectedUserColor);
                    }
                });
            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int errorCode) {
                eProgressDialog.dismiss();
                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setTitle("Упс...");
                alertDialog.setMessage("Спонсор не оставил ни одного ролика. Код ошибки: " + String.valueOf(errorCode));
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Ясно", (DialogInterface.OnClickListener) null);
                alertDialog.show();
                eUserRef.runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                        User user = mutableData.getValue(User.class);
                        assert user != null;
                        user.setuColor(eSelectedUserColor);
                        mutableData.setValue(user);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                        eUserPhoto.setBorderColor(eSelectedUserColor);
                    }
                });
            }

            @Override
            public void onRewardedVideoAdLoaded() {
                eProgressDialog.dismiss();
                eRewardedVideoAd.show();
                // запаминаем время сервера, когда была просмотренна реклама
                final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("time");
                ref.setValue(ServerValue.TIMESTAMP).addOnSuccessListener(aVoid ->
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //noinspection ConstantConditions
                        eUserLastWatchedAddMobTime = dataSnapshot.getValue(long.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }));
            }

            @Override
            public void onRewardedVideoAdOpened() {
            }

            @Override
            public void onRewardedVideoStarted() {
            }

            @Override
            public void onRewardedVideoCompleted() {
            }
        });
        FirebaseDatabase.getInstance().getReference().child("users").child(eUser.getUid()).child("lastAddMobTime").setValue(ServerValue.TIMESTAMP);
        // AD_UNIT_ID  это ID рекламы, было при тесте: ca-app-pub-3940256099942544/5224354917
        // не тест: ca-app-pub-2972366758632504/7893798165 или ca-app-pub-2972366758632504/3657535020
        eRewardedVideoAd.loadAd(AD_UNIT_ID, new AdRequest.Builder().build());
    }
}