package com.MMGS.radiogram.classes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.MMGS.radiogram.R;
import com.MMGS.radiogram.main_activities.AddLossActivity;
import com.MMGS.radiogram.main_activities.MainActivity;
import com.MMGS.radiogram.profile_activities.ProfileActivity;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class FirebaseManager {
    public static void SaveImageAndUserToDatabase(final Context context, Bitmap bitmap,
                                                  String refOfImage, final String nameOfImage,
                                                  String typeOfImage, final User user,
                                                  final Activity activity, final ProgressDialog dialog) {
            saveImage(context, bitmap, refOfImage, nameOfImage, typeOfImage, isSuccess -> {
                try {
                    activity.finish();
                    dialog.dismiss();
                    Toast.makeText(context, "Сохранено!", Toast.LENGTH_SHORT).show();
                    FirebaseDatabase.getInstance().getReference().child("users").child(nameOfImage).setValue(user);
                }catch (Exception e){
                    Toast.makeText(context, "Error: Ошибка при записи аккаунта на сервер\nJava: FirebaseManager:SaveImageAndUserToDatabase\n"
                            + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }
    public static void SetColorOnCircleImage(final Activity activity, final CircleImageView image, String userID) {
        // Обробатываем нажатие на иконку автора сообщения
            image.setOnClickListener(v -> {
                final Intent profileActivity = new Intent(activity, ProfileActivity.class);
                profileActivity.putExtra("show_user_profile", "FALSE");
                profileActivity.putExtra("userID", userID);
                activity.finish();
                activity.startActivity(profileActivity);
            });
            FirebaseDatabase.getInstance().getReference().child("users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        User user = dataSnapshot.getValue(User.class);
                        image.setBorderColor(user.getuColor());
                    } catch (Exception e){
                        Toast.makeText(activity, "Error: Ошибка при обновлении цвета обводки\nJava: FirebaseManager:SetColorOnCircleImage\n"
                                + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
    }
    public static ProgressDialog MyProgressDialog(Context context, String title) {
        ProgressDialog dialog = new ProgressDialog(context);
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.dimAmount = 0.6f; // уровень затемнения от 1.0 до 0.0
        dialog.getWindow().setAttributes(lp);
        dialog.setMessage(title);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.show();
        return dialog;
    }
    public static String ShowDate(Context context, String format, double milliseconds) {
        // "dd/MM/yyyy  HH:mm:ss" - format
        // Определяем минское время у ползовател. Если пользователь в Сан-Франциско, то ему всё равно
        // возвратит минское время согласно формату
        try {
            Date date = new Date((long) (milliseconds));
            @SuppressLint("SimpleDateFormat")
                    // Заменияет число месяца на надпись
            SimpleDateFormat sdformat = new SimpleDateFormat(format, new DateFormatSymbols() {
                @Override
                public String[] getMonths() {
                    return new String[]{"Января", "Февраля", "Марта", "Апреля", "Мая", "Июня",
                            "Июля", "Августа", "Сентября", "Октября", "Ноября", "Декабря"};
                }
            });
            sdformat.setTimeZone(TimeZone.getTimeZone("Europe/Minsk"));
            String sdate = sdformat.format(date);
            return sdate;
        } catch (Exception e){
            Toast.makeText(context, "Error: Ошибка при обработки даты\nJava: FirebaseManager:ShowDate\n"
                    + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return "";
    }
    public static void CloseKeyBoard(Activity activity) {
        if (activity.getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }
    @SuppressLint("StaticFieldLeak")
    public static class GoForTable extends AsyncTask<Void, Void, String> {
        Activity a;
        Intent b;

        public GoForTable(Activity activity, Intent browser) {
            this.a = activity;
            this.b = browser;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String uri = " ";
            try {
                Document document = Jsoup.connect("http://mrk-bsuir.by/").get();
                Element rasp = document.getElementsByAttributeValue("id", "blockSidebar").first().selectFirst("div.block-content p");
                uri = rasp.select("a[href]").first().attr("abs:href");
            } catch (IOException e) {
                Toast.makeText(a, "Error: Ошибка при загрузке расписания\nJava: FirebaseManager:GoForTable\n"
                        + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            return uri;
        }

        @Override
        protected void onPostExecute(String uri) {
            super.onPostExecute(uri);
            b.putExtra("uri", uri);
            a.startActivity(b);

        }
    }
    public static int DpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
    public static int pxToDp(Context context, int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
    public static void OpenAchievements(final AppCompatActivity a, String id) {
        try {
            AlertDialog dialog = new AlertDialog.Builder(a).create();

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            LinearLayout layout = new LinearLayout(dialog.getContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setLayoutParams(params);

            ListView achievementsView = new ListView(layout.getContext());
            Query query = FirebaseDatabase.getInstance().getReference().child("users").child(id).child("achievements");
            SnapshotParser<Achievement> parser = snapshot -> {
                //noinspection ConstantConditions
                return snapshot.getValue(Achievement.class);
            };
            FirebaseListOptions<Achievement> options = new FirebaseListOptions.Builder<Achievement>()
                    .setQuery(query, parser)
                    .setLayout(R.layout.achievement)
                    .setLifecycleOwner(a)
                    .build();
            FirebaseListAdapter<Achievement> adapter = new FirebaseListAdapter<Achievement>(options) {
                @Override
                protected void populateView(View v, Achievement model, int position) {
                    TextView title = v.findViewById(R.id.title);
                    TextView text = v.findViewById(R.id.text);
                    final ImageView image = v.findViewById(R.id.image);
                    if (model.getValue() >= model.getMaxValue()) {
                        title.setTextColor(a.getResources().getColor(R.color.colorAccent));
                        text.setText(model.getText());
                        Glide.with(v.getContext())
                                .load(FirebaseStorage.getInstance().getReference().child("achievements").child(model.getName() + ".png"))
                                .into(image);
                    } else {
                        title.setTextColor(a.getResources().getColor(R.color.colorDarkGray));
                        text.setText("Необходимо потрудиться, чтобы разблокировать достижение.");
                        image.setImageDrawable(a.getDrawable(R.drawable.ic_help_outline_grey_100dp));
                    }
                    title.setText(model.getTitle());
                }
            };
            achievementsView.setAdapter(adapter);
            layout.addView(achievementsView, params);
            dialog.setTitle("Список достижений");
            dialog.setView(layout);
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Здорово!", (DialogInterface.OnClickListener) null);
            dialog.show();
        } catch (Exception e){
            Toast.makeText(a, "Error: Ошибка при загрузке достижений\nJava: FirebaseManager:OpenAchievements\n"
                    + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    public static Snackbar MakeSnackbar(Context context, View view, String text) {
        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG);
        try {
            View background = snackbar.getView();
            TextView textView = background.findViewById(android.support.design.R.id.snackbar_text);
            background.setBackgroundColor(Color.WHITE);
            textView.setTextColor(context.getResources().getColor(R.color.colorAccent));
        } catch (Exception e){
            Toast.makeText(context, "Error: Ошибка при показе сообщения\nJava: FirebaseManager:MakeSnackbar\n"
                    + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return snackbar;
    }
    public static void AddPointToAchievement(final AppCompatActivity a, final View view, final String id, final String achievement) {
            FirebaseDatabase.getInstance().getReference().child("users").child(id).child("achievements").child(achievement)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            try {
                                Achievement ach = dataSnapshot.getValue(Achievement.class);
                                int value = ach.getValue();
                                value++;
                                if (value == ach.getMaxValue()) {
                                    switch (achievement) {
                                        case "messages_x5":
                                            MakeSnackbar(a, view, "Это Ваше пятое сообщение!").show();
                                            break;
                                        case "messages_x25":
                                            MakeSnackbar(a, view, "Это Ваше двадцать пятое сообщение!").show();
                                            break;
                                        case "messages_x100":
                                            MakeSnackbar(a, view, "Это Ваше сотое сообщение!").show();
                                            break;
                                        case "background_edit":
                                            MakeSnackbar(a, view, "Благодарим за изменение заднего фона!").show();
                                            break;
                                        case "alpha":
                                            MakeSnackbar(a, view, "Благодарим за участие в Альфа-тестировании!").show();
                                            break;
                                        case "beta":
                                            MakeSnackbar(a, view, "Благодарим за участие в Бета-тестировании!").show();
                                            break;
                                    }

                                    FirebaseDatabase.getInstance().getReference().child("users").child(id).child("achievements").child(achievement).child("value").setValue(value);
                                } else if (value < ach.getMaxValue())
                                    FirebaseDatabase.getInstance().getReference().child("users").child(id).child("achievements").child(achievement).child("value").setValue(value);
                            } catch (Exception e){
                                Toast.makeText(a, "Error: Ошибка при обновлении достижений\nJava: FirebaseManager:AddPointToAchievement\n"
                                        + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
    }
    public static void DeleteSpaces(TextView text) {
        text.setText(text.getText().toString().replaceAll("\\p{Z}", ""));
    }
    public static class LossViewHolder extends RecyclerView.ViewHolder {
        View v;

        public LossViewHolder(View itemView) {
            super(itemView);
            v = itemView;
        }

        public void SetCard(final Loss model, final String ref, final MainActivity activity) {
            try {
                final StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                TextView nameView = v.findViewById(R.id.lost_name);
                TextView user = v.findViewById(R.id.lost_user_2);
                TextView time = v.findViewById(R.id.lost_time_2);
                final CircleImageView userImage = v.findViewById(R.id.user_image);
                final TextView descriptionView = v.findViewById(R.id.lost_description_2);
                final ImageView image = v.findViewById(R.id.lost_image);
                final ImageView delete = v.findViewById(R.id.loss_delete);
                final ImageView edit = v.findViewById(R.id.loss_edit);
                nameView.setText(model.getName());
                user.setText(model.getUserName());
                time.setText(model.getDate());
                descriptionView.setText(model.getDescription());
                FirebaseManager.downloadImage(v.getContext(), ref, model.getKey(), "main", (bitmap, fromData) -> {
                    image.setImageBitmap(bitmap);
                });
                FirebaseManager.downloadImage(v.getContext(), "images", model.getUserID(), "icon",
                        (bitmap, fromData) -> userImage.setImageBitmap(bitmap));
                FirebaseManager.SetColorOnCircleImage(activity, userImage, model.getUserID());
                if (firebaseUser.getUid().equals(model.getUserID())) {
                    delete.setVisibility(View.VISIBLE);
                    edit.setVisibility(View.VISIBLE);
                    edit.setOnClickListener(v -> {
                        Intent addLossActivity = new Intent(activity, AddLossActivity.class);
                        if (ref.equals("finds"))
                            addLossActivity.putExtra(AddLossActivity.ACTIVITY_TYPE, AddLossActivity.EDIT_FIND_ACTIVITY);
                        else
                            addLossActivity.putExtra(AddLossActivity.ACTIVITY_TYPE, AddLossActivity.EDIT_LOSS_ACTIVITY);
                        addLossActivity.putExtra("lossID", model.getKey());
                        activity.startActivity(addLossActivity);
                    });
                    delete.setOnClickListener(v -> {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(activity)
                                .setNeutralButton("Да", (dialog1, which) -> {
                                    databaseReference.child(ref).child(model.getKey()).removeValue();
                                    storageReference.child(ref).child(model.getKey()).child("main.jpg").delete();
                                })
                                .setNegativeButton("Нет", null)
                                .setTitle(activity.getResources().getString(R.string.dialog_delete_loss));
                        dialog.create().show();
                    });
                } else {
                    edit.setVisibility(View.GONE);
                    delete.setVisibility(View.GONE);
                }
            } catch (Exception e){
                Toast.makeText(activity, "Error: Ошибка при обновлении вида сообщения\nJava: FirebaseManager:LossViewHolder\n"
                        + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
    public static int MY_POSTS = 0;
    public static int ALL_POSTS = 1;
    public static FirebaseRecyclerAdapter<Loss, LossViewHolder> RecyclerAdapter(final String ref, int type, final MainActivity activity, final LinearLayout emptyView) {
        Query query;
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        query = type == 0 ?
                databaseReference.child(ref).limitToLast(50).orderByChild("userID").equalTo(firebaseUser.getUid()) :
                databaseReference.child(ref).limitToLast(50);
        SnapshotParser<Loss> parser = snapshot -> snapshot.getValue(Loss.class);
        FirebaseRecyclerOptions<Loss> options = new FirebaseRecyclerOptions.Builder<Loss>()
                .setQuery(query, parser)
                .setLifecycleOwner(activity)
                .build();

        return new FirebaseRecyclerAdapter<Loss, LossViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull LossViewHolder holder, int position, @NonNull Loss model) {
                holder.SetCard(model, ref, activity);
            }

            @NonNull
            @Override
            public LossViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new LossViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.lost_card, parent, false));
            }

            @Override
            public void onDataChanged() {
                if (emptyView != null)
                    emptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }
        };
    }
    public static void CreateFriend(Activity activity, View layout, DataSnapshot dataSnapshot, Friend friend) {
        // dataSnapshot - юзер, который добовляет друга (friend)
        try {
            User me = dataSnapshot.getValue(User.class);
            String name = me.getuName() + " " + me.getuSurname();
            Friend meFriend = new Friend(name, dataSnapshot.getKey());
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users");
            boolean isSub = dataSnapshot.hasChild("subs") && dataSnapshot.child("subs").hasChild(friend.getId());
            reference.child(dataSnapshot.getKey()).child("mySubs").child(friend.getId()).removeValue();
            reference.child(dataSnapshot.getKey()).child("subs").child(friend.getId()).removeValue();
            reference.child(friend.getId()).child("mySubs").child(dataSnapshot.getKey()).removeValue();
            reference.child(friend.getId()).child("subs").child(dataSnapshot.getKey()).removeValue();
            if (isSub) {
                FirebaseManager.MakeSnackbar(activity, layout, friend.getName() + " теперь в друзьях!").show();
                reference.child(meFriend.getId()).child("friends").child(friend.getId()).setValue(friend);
                reference.child(friend.getId()).child("friends").child(meFriend.getId()).setValue(meFriend);
                HashMap<String, Member> members = new HashMap<>();
                members.put(friend.getId(), new Member());
                members.put(meFriend.getId(), new Member());
                new Chat(members);
            } else {
                reference.child(meFriend.getId()).child("mySubs").child(friend.getId()).setValue(friend);
                reference.child(friend.getId()).child("subs").child(meFriend.getId()).setValue(meFriend);
            }
        } catch (Exception e){
            Toast.makeText(activity, "Error: Ошибка при обновлении вида сообщения\nJava: FirebaseManager:CreateFriend\n"
                    + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    public static void downloadImage(final Context context, final String category, final String group, final String name,
                                     final ImageDownloaderListener listener) {
        try {
            final String FILE_NAME = category + "_" + group + "_" + name + ".jpg";
            SharedPreferences memory = context.getSharedPreferences("images",Context.MODE_PRIVATE);
            final DatabaseReference database = FirebaseDatabase.getInstance().getReference()
                    .child("ImageInfo").child(category).child(group).child(name).child("version");
            final StorageReference storage = FirebaseStorage.getInstance().getReference().child(category).child(group).child(name + ".jpg");
            database.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (memory.contains(FILE_NAME) && dataSnapshot.exists()) {
                        // Если изображение уже было когда-то скачено с таким же адресом
                        try {
                            //noinspection ConstantConditions
                            final int version = dataSnapshot.getValue(Integer.class);
                            if (version > memory.getInt(FILE_NAME, 0)) {
                                // Если в базе данных есть новое изображение
                                long MB = 1024 * 1024;
                                storage.getBytes(MB).addOnSuccessListener(bytes -> {
                                    try {
                                        FileOutputStream stream = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
                                        stream.write(bytes);
                                        stream.close();
                                        memory.edit().putInt(FILE_NAME, version).apply();
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        listener.onDownload(bitmap, false);
                                    } catch (Exception e) {
                                        Toast.makeText(context, "Error: Ошибка при сохранении изображения\n Java: Manager, downloadImage : 1\n"
                                                + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }).addOnFailureListener(e ->
                                        Toast.makeText(context, "Error: Ошибка доступа к серверу\n Java: Manager, downloadImage : 2\n"
                                                + e.getMessage(), Toast.LENGTH_LONG).show());

                            } else {
                                // Если у пользователя есть новейшее изображение в памяти телефона
                                FileInputStream stream = context.openFileInput(FILE_NAME);
                                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                                listener.onDownload(bitmap, true);
                            }

                        } catch (Exception e) {
                            Toast.makeText(context, "Error: Ошибка доступа к файлам приложения\n Java: Manager, downloadImage : 3\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        // Если изображение никогда не скачиволось до этого на этот телефон
                        if (!dataSnapshot.exists())
                            database.setValue(1);
                        long MB = 1024 * 1024;
                        storage.getBytes(MB).addOnSuccessListener(bytes -> {
                            try {
                                memory.edit().putInt(FILE_NAME, 1).apply();
                                FileOutputStream stream = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
                                stream.write(bytes);
                                stream.close();
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                listener.onDownload(bitmap, false);
                            } catch (Exception e) {
                                Toast.makeText(context, "Error: Ошибка при сохранении изображения\n Java: Manager, downloadImage : 4\n"
                                        + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(e ->{}
                                //Toast.makeText(context, "Error: Ошибка доступа к серверу\n Java: Manager, downloadImage : 5\n"
                                //        +"path: " + FILE_NAME + "\n" + e.getMessage(), Toast.LENGTH_LONG).show()
                                );
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(context, String.valueOf(databaseError.getCode()) + "\n" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e){
            Toast.makeText(context, "Error: Ошибка доступа к памяти устройства\n Java: Manager, downloadImage : 6\n" + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    public static void saveImage(final Context context, Bitmap bitmap,
                                 final String category, final String group, final String name,
                                 final DownloadListener listener) {
        try {
            if (bitmap != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();
                FirebaseStorage.getInstance().getReference().child(category).child(group).child(name + ".jpg").putBytes(data)
                        .addOnFailureListener(e -> Toast.makeText(context, "Error: Ошибка при сохранении изображения\n" +
                                " Java: Manager, saveImage : 1\n"
                                + e.getMessage(), Toast.LENGTH_LONG).show())
                        .addOnSuccessListener(taskSnapshot -> {
                            final DatabaseReference database = FirebaseDatabase.getInstance().getReference()
                                    .child("ImageInfo").child(category).child(group).child(name).child("version");
                            database.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    try {
                                        final int version = dataSnapshot.exists() ? dataSnapshot.getValue(Integer.class) : 0;
                                        database.setValue(version + 1).addOnSuccessListener(aVoid -> {
                                            try {
                                                final String FILE_NAME = category + "_" + group + "_" + name + ".jpg";
                                                SharedPreferences memory = context.getSharedPreferences("images", Context.MODE_PRIVATE);
                                                memory.edit().putInt(FILE_NAME, version + 1).apply();
                                                FileOutputStream stream = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
                                                stream.write(data);
                                                stream.close();
                                                listener.AfterDownload(true);
                                            } catch (Exception e){
                                                Toast.makeText(context, "Error: Ошибка при сохранении изображения\n" +
                                                        " Java: Manager, saveImage : 2\n"
                                                        + e.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    } catch (Exception e) {
                                        Toast.makeText(context, "Error: Ошибка при сохранении изображения\n" +
                                                " Java: Manager, saveImage : 3\n"
                                                + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError e) {
                                    Toast.makeText(context, "Error: Ошибка при сохранении изображения\n" +
                                            " Java: Manager, saveImage : 4\n"
                                            + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        });
            } else listener.AfterDownload(false);
        } catch (Exception e){
            Toast.makeText(context, "Error: Ошибка при сохранении изображения\n" +
                    " Java: Manager, saveImage : 5\n"
                    + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    public static void setChatToFirst(final Context context, final DatabaseReference chatsRefInUser, final String firstChatID) {
        chatsRefInUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot chats) {
                try {
                    int number = chats.child(firstChatID).getValue(Integer.class);
                    for (DataSnapshot chat : chats.getChildren()){
                        int chatNumber = chat.getValue(Integer.class);
                        if(chatNumber != 0)
                        if (chatNumber < number) {
                            chatsRefInUser.child(chat.getKey()).setValue(chatNumber + 1);
                        }
                        chatsRefInUser.child(firstChatID).setValue(1);
                    }
                } catch (Exception e) {
                    Toast.makeText(context, "Error: Ошибка при обновлении списка диалогов\nJava: FirebaseManager:setChatToFirst\n"
                            + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public static void setChatToFirstInMembers(final Context context, ArrayList<String> members, final String firstChatID) {
        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference().child("users");
        for (String m : members) {
            setChatToFirst(context, chatsRef.child(m).child("chats"), firstChatID);
        }
    }
    public static void deleteChatFromUser(Context context, String chatID, String userID) {
        FirebaseDatabase.getInstance().getReference().child("chats").child(chatID).child("members").child(userID).removeValue();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(userID).child("chats");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot chats) {
                try {
                    int number = chats.child(chatID).getValue(Integer.class);
                    for (DataSnapshot chat : chats.getChildren()) {
                        int chatNumber = chat.getValue(Integer.class);
                        if (chatNumber > number) {
                            ref.child(chat.getKey()).setValue(chatNumber - 1);
                        }
                    }
                    ref.child(chatID).removeValue();
                } catch (Exception e) {
                    Toast.makeText(context, "Error: Ошибка при уничтожении диалога\nJava: FirebaseManager:deleteChatFromUser\n"
                            + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public static void addNotifyToChatMembers(Context context, String chatID) {
        if (!chatID.equals("-LOxTLOZFrZ9mHPXp8aQ")) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("chats").child(chatID).child("members");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot members) {
                    try {
                        for (DataSnapshot m : members.getChildren()) {
                            Member member= m.getValue(Member.class);
                            if(!member.isInChat())
                                ref.child(m.getKey()).child("lastMessage").setValue(member.getLastMessage() + 1);
                            setChatToFirst(context,
                                    FirebaseDatabase.getInstance().getReference().child("users").child(m.getKey()).child("chats"), chatID);
                        }
                    } catch (Exception e) {
                        Toast.makeText(context, "Error: Ошибка при обновлении списка диалогов\nJava: FirebaseManager:addNotifyToChatMembers\n"
                                + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}

        /*
        long MB = 1024 * 1024;
        eStorageRef.child("images").child(eUser.getUid()).child("icon_big.jpg").getBytes(MB).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
        */
                            /*  добавление текста в диалог
                            LinearLayout layout = new LinearLayout(context);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            layout.setOrientation(LinearLayout.VERTICAL);
                            layout.setLayoutParams(params);

                            TextView text = new TextView(alertDialog.getContext());
                            text.setText("У спонсора кочались видеоролики");

                            layout.addView(text, params);
                            alertDialog.setView(layout);
                            */
/*
                FirebaseDatabase.getInstance().getReference().child("appbar_image_title").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String title = dataSnapshot.getValue(String.class);
                        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), title);
                        if (file.exists()) {
                            imageView.setImageURI(Uri.fromFile(file));
                        } else{
                            ref.child("appbar_image").child(title).getBytes(MB).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    try {
                                        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), title);
                                        FileOutputStream stream = new FileOutputStream(file);
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        imageView.setImageBitmap(bitmap);
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream);
                                        stream.close();
                                    } catch (IOException e) {
                                        Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
 */

/*
                            GlideApp.with(context)
                                    .load(storage)
                                    .error(R.mipmap.main_icon)
                                    .signature(new ObjectKey(version))
                                    .listener(new RequestListener<Drawable>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                            listener.onDownload(resource, !isFirstResource);
                                            return true;
                                        }
                                    }).submit();
 */