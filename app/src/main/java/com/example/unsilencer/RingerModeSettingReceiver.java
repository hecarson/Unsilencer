package com.example.unsilencer;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

public class RingerModeSettingReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        int hour = extras.getInt("hour");
        int minute = extras.getInt("minute");
        int ringerMode = extras.getInt("ringerMode");
        int requestCode = extras.getInt("requestCode");

        Log.d(MainActivity.LOG_TAG, "setting broadcast received, hour " + hour + " minute " + minute + " ringerMode " + ringerMode);

        NotificationManager notifManager = context.getSystemService(NotificationManager.class);
        AudioManager audioManager = context.getSystemService(AudioManager.class);

        if (!notifManager.isNotificationPolicyAccessGranted()) {
            Log.d(MainActivity.LOG_TAG, "no notif policy perm");
            return;
        }

        // this entire project... just for this one line
        audioManager.setRingerMode(ringerMode);
        Log.d(MainActivity.LOG_TAG, "set ringer mode");

        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);

        if (Build.VERSION.SDK_INT >= 31 && !alarmManager.canScheduleExactAlarms()) {
            Log.d(MainActivity.LOG_TAG, "no exact alarm perm");
            return;
        }

        Intent ringerModeSettingIntent = new Intent(context, RingerModeSettingReceiver.class);
        ringerModeSettingIntent.putExtra("hour", hour);
        ringerModeSettingIntent.putExtra("minute", minute);
        ringerModeSettingIntent.putExtra("ringerMode", ringerMode);
        ringerModeSettingIntent.putExtra("requestCode", requestCode);

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                RingerModeSettingScheduler.findNextEpochTimeForRingerModeSetting(hour, minute),
                PendingIntent.getBroadcast(context, requestCode, ringerModeSettingIntent,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT)
        );

        Log.d(MainActivity.LOG_TAG, "scheduled next setting");
    }

}