package com.guy_gueta.post_pc_6;


import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class LocalSendSmsBroadcastReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "SMS_SEND_CHANNEL";
    private static final String CONTENT_NOTIFY = "sms was sent to  %1$s: %2$s";

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean hasSmsPermission =
                ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) ==
                        PackageManager.PERMISSION_GRANTED;
        if (hasSmsPermission) {
            smsSend(context, intent);
        }
        else {
            Log.e("MISSING_PERMISSION_SMS", "user has no sms permission");
        }
    }

    private void smsSend(Context context, Intent intent) {
        String phone_num = intent.getStringExtra( "PHONE_KEY");
        if (phone_num == null || phone_num.isEmpty()) {
            Log.e("MISSING_PHONE_NUMBER", "no phone number provided");
            return;
        }
        String phoneContent = intent.getStringExtra("CONTENT_KEY");
        if (phoneContent == null || phoneContent.isEmpty()) {
            Log.e("MISSING_PHONE_CONTENT", "no phone content provided");
            return;
        }
        SmsManager.getDefault().sendTextMessage(phone_num, null, phoneContent, null, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "push";
            String description = "notify when sending sms";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCreate(context, phone_num, phoneContent);
    }


    private void NotificationCreate(Context context, String phoneNum, String content) {
        Notification notify = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(String.format(CONTENT_NOTIFY, phoneNum, content))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
        int notificationId = 8648;
        NotificationManagerCompat.from(context).notify(notificationId, notify);
    }
}
