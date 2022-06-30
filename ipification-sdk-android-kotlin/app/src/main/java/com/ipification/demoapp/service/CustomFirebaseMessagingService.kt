package com.ipification.demoapp.service

import android.app.ActivityManager
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ipification.demoapp.R
import com.ipification.demoapp.manager.APIManager
import com.ipification.mobile.sdk.im.IMService
import com.ipification.mobile.sdk.im.ui.IMVerificationActivity

/**
 * NOTE: There can only be one service in each app that receives FCM messages. If multiple
 * are declared in the Manifest then the first one will be chosen.
 *
 * In order to make this Java sample functional, you must remove the following from the Kotlin messaging
 * service in the AndroidManifest.xml:
 *
 * <intent-filter>
 * <action android:name="com.google.firebase.MESSAGING_EVENT"></action>
</intent-filter> *
 */
class CustomFirebaseMessagingService : FirebaseMessagingService() {
    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.from)

        // Check if message contains a data payload.
        if (remoteMessage.data.size > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            handleNow(remoteMessage.data)
        }

        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            Log.d(
                TAG, "Message Notification Body: " + remoteMessage.notification!!
                    .body
            )
            sendNotification(remoteMessage.notification!!.body)
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]
    // [START on_new_token]
    /**
     * There are two scenarios when onNewToken is called:
     * 1) When a new token is generated on initial app startup
     * 2) Whenever an existing token is changed
     * Under #2, there are three scenarios when the existing token is changed:
     * A) App is restored to a new device
     * B) User uninstalls/reinstalls the app
     * C) User clears app data
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token)
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     * @param data
     */
    private fun handleNow(data: Map<String, String>) {
        Log.d(TAG, "Short lived task is done.")
        sendNotification(data["body"])
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM registration token with any
     * server-side account maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String) {
        // TODO: Implement this method to send token to your app server.
        APIManager.deviceToken = token
        APIManager.registerDevice(token)
    }

    private fun isNotificationActivityRunning(): Boolean{
            val activityManager = baseContext.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val tasks = activityManager.getRunningTasks(Int.MAX_VALUE)
            for (task in tasks) {
                if (task.topActivity!!.className == IMVerificationActivity::class.java.canonicalName) return true
            }
            return false
        }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private fun sendNotification(messageBody: String?) {
        // TODO check if notification is from IPification server
//        if(){
            IMService.showIPNotification(this, notificationTitle=getString(R.string.app_name), messageBody = messageBody ?: "", ic_notification= R.drawable.ic_stat_name)
//        }

    }

    companion object {
        private const val TAG = "CustomFMS"
    }
}