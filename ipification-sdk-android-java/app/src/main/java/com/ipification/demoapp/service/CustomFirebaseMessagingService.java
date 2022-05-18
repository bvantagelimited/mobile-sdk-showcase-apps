package com.ipification.demoapp.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ipification.demoapp.R;
import com.ipification.demoapp.manager.ApiManager;
import com.ipification.mobile.sdk.android.IPConfiguration;
import com.ipification.mobile.sdk.im.IMService;
import com.ipification.mobile.sdk.im.ui.IMVerificationActivity;

import java.util.List;

public class CustomFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "CustomFMS";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        sendNotification(remoteMessage.getData().get("body"));
    }

    @Override
    public void onNewToken(@NonNull String token) {
        sendRegistrationToServer(token);
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM registration token with any
     * server-side account maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
        ApiManager.registerDevice(token, ApiManager.currentState);
    }


    private boolean isNotificationActivityRunning() {
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
        if (tasks != null && !tasks.isEmpty()) {
            return tasks.get(0).topActivity.getClassName().equals(IMVerificationActivity.class.getName());
        }
        return false;
    }
    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        IMService.Factory.showIPNotification(this, getString(R.string.app_name), messageBody, R.string.notification_channel_id, "Notification", R.mipmap.ic_launcher);
//        Class accessClass = IMVerificationActivity.class;
//        if (!isNotificationActivityRunning()) {
//            // if app is not running , do nothing
//            return;
//        }
//        Log.d(TAG, "isNotificationActivityRunning()$isNotificationActivityRunning");
//        Intent intent = new Intent(this, accessClass);
//        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
//        PendingIntent pendingIntent;
//        // support android 12
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            pendingIntent = PendingIntent.getActivity(
//                    this, IPConfiguration.getInstance().getREQUEST_CODE() /* Request code */, intent,
//                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
//            );
//        }else{
//            pendingIntent = PendingIntent.getActivity(
//                    this, IPConfiguration.getInstance().getREQUEST_CODE()  /* Request code */, intent,
//                    PendingIntent.FLAG_UPDATE_CURRENT
//            );
//        }
//
//        String channelId = getString(R.string.notification_channel_id);
//        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setContentTitle(getString(R.string.app_name))
//                .setContentText(messageBody)
//                .setAutoCancel(true)
//                .setSound(defaultSoundUri)
//                .setPriority(Notification.PRIORITY_MAX)
//                .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
//                .setContentIntent(pendingIntent);
//        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//
//        // Since android Oreo notification channel is needed.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(
//                    channelId,
//                    "Notification",
//                    NotificationManager.IMPORTANCE_HIGH
//            );
//            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
//            channel.enableVibration(true);
//            notificationManager.createNotificationChannel(channel);
//        }
//        notificationManager.notify(IPConfiguration.getInstance().getNOTIFICATION_ID() /* ID of notification */, notificationBuilder.build());
    }
}
