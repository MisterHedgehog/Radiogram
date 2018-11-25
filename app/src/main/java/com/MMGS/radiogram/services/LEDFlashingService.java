package com.MMGS.radiogram.services;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.MMGS.radiogram.R;

import java.util.Random;

public class LEDFlashingService extends Service {

    private Context context;
    private NotificationManagerCompat nm;
    private Runnable LEDcontroller;
    private Handler handler = new Handler();
    private Notification notification;
    private PowerManager.WakeLock wakeLock;
    private ScreenOffListener screenOffListener = new ScreenOffListener();
    private ScreenOnListener screenOnListener = new ScreenOnListener();
    private NotificationClickListener notificationClickListener = new NotificationClickListener();

    private final String BROADCAST_ACTION = "com.MMGS.radiogram.main_activities.MainActivity";
    private final int NOTIFICATION_ID = 5;
    private final String WAKE_LOCK_ID = "sleep_lock";
    private final int INTERVAL = 10;

    int color_1 = Color.RED;
    int color_2 = Color.YELLOW;
    int col_1 = 10;
    int cursor = -1;

    @SuppressLint("HandlerLeak")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = this;
        PendingIntent resultPendingIntent =
                PendingIntent.getBroadcast(context, 0, new Intent(BROADCAST_ACTION), PendingIntent.FLAG_UPDATE_CURRENT);
        nm = NotificationManagerCompat.from(context);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_ID);
        LEDcontroller = new Runnable() {
            @Override
            public void run() {
                color_1 = color_2;
                int random = new Random().nextInt(7);

                cursor = (cursor == random) ? random + 1 : random;
                switch (cursor) {
                    case 0:color_2 = 0xFFFF00FF; break; // фиолетовый
                    case 1:color_2 = 0xFFFF0000; break; // красный
                    case 2:color_2 = 0xFF0000FF; break; // синий
                    case 3:color_2 = 0xFF00FFFF; break; // голубой
                    case 4:color_2 = 0xFFFFFFFF; break; // розовый
                    case 5:color_2 = 0xFF00FF00; break; // зелённый
                    case 6:color_2 = 0xFFFFFF00; break; // жёлтый
                    //case 7:color = Color.BLACK; break;
                }
                for (col_1 = 20; col_1 > 0; col_1--){
                    for (int i = 0; i < col_1; i++) {
                        notification.ledARGB = color_1;
                        nm.notify(NOTIFICATION_ID, notification);
                        try {
                            Thread.sleep(INTERVAL);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    notification.ledARGB = color_2;
                    nm.notify(NOTIFICATION_ID, notification);
                    try {
                        Thread.sleep(INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                    for (col_1 = 0; col_1 < 20; col_1++) {
                        for (int i = 0; i < col_1; i++) {
                            notification.ledARGB = color_2;
                            nm.notify(NOTIFICATION_ID, notification);
                            try {
                                Thread.sleep(INTERVAL);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        notification.ledARGB = color_1;
                        nm.notify(NOTIFICATION_ID, notification);
                        try {
                            Thread.sleep(INTERVAL);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                handler.post(this);
            }
        };
        notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.main_icon)
                .setContentTitle("Светофор")
                //.setVibrate(new long[]{100,100})
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent)
                .setLights(color_1, INTERVAL, 0)
                .build();
        nm.notify(NOTIFICATION_ID, notification);
        registerReceiver(screenOnListener, new IntentFilter("android.intent.action.SCREEN_ON"));
        registerReceiver(screenOffListener, new IntentFilter("android.intent.action.SCREEN_OFF"));
        registerReceiver(notificationClickListener, new IntentFilter(BROADCAST_ACTION));
        return super.onStartCommand(intent, flags, startId);
    }
    public class ScreenOnListener extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            handler.removeCallbacks(LEDcontroller);
            wakeLock.release();
        }
    }
    public class ScreenOffListener extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            wakeLock.acquire();
            handler.postDelayed(LEDcontroller, 500);
        }
    }
    public class NotificationClickListener extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            stopSelf();
        }
    }
    public IBinder onBind(Intent intent) {
        return null;
    }
}

