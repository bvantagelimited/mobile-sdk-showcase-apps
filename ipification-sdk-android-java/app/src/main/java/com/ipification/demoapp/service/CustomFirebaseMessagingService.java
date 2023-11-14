package com.ipification.demoapp.service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ipification.demoapp.R;
import com.ipification.demoapp.manager.IMHelper;
import com.ipification.demoapp.manager.IPHelper;
import com.ipification.mobile.sdk.im.IMService;

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
        IMHelper.currentToken = token;
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        // TODO check if notification is from IPification Service
//        if(data["source"] == "ipification"){
            IMService.Factory.showIPNotification(this, getString(R.string.app_name), messageBody, R.mipmap.ic_launcher);
//        }
    }
}
