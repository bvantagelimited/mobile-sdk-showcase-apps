package com.ipification.demoapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ipification.demoapp.BuildConfig;
import com.ipification.demoapp.activity.im.IMAuthManualActivity;
import com.ipification.demoapp.databinding.ActivityMainBinding;
import com.ipification.demoapp.manager.IMHelper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.ipification.mobile.sdk.android.IPConfiguration;
import com.ipification.mobile.sdk.android.IPEnvironment;
import com.ipification.mobile.sdk.android.IPificationServices;
import com.ipification.mobile.sdk.android.callback.IPificationCallback;
import com.ipification.mobile.sdk.android.request.AuthRequest;
import com.ipification.mobile.sdk.im.IMService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        initIPification();
        initActions();
        initFirebase();

    }

    private void initIPification() {
        IPConfiguration.getInstance().setENV(IPEnvironment.SANDBOX);
        IPConfiguration.getInstance().setCLIENT_ID(BuildConfig.CLIENT_ID);
        IPConfiguration.getInstance().setREDIRECT_URI(Uri.parse(BuildConfig.REDIRECT_URI));
    }

    private void initActions() {
        binding.imButton.setOnClickListener(view -> doIMFlow());
        binding.ipButton.setOnClickListener(view -> doIPFlow());
    }

    private void doIPFlow() {
        Intent intent = new Intent(getApplicationContext(), PhoneVerifyActivity.class);
        startActivity(intent);
    }

    private void doIMFlow() {
//        registerDevice();
//
//        doIMAuth(new IPificationCallback() {
//            @Override
//            public void onIMCancel() {
//                // hide loading or do nothing
//            }
//
//            @Override
//            public void onSuccess(@NonNull AuthResponse authResponse) {
//                if (authResponse.getCode() != null) {
//                    callTokenExchange(authResponse.getCode());
//                } else {
//                    openErrorActivity(authResponse.getErrorMessage());
//                }
//            }
//
//            @Override
//            public void onError(@NonNull IPificationError iPificationError) {
//                openErrorActivity(iPificationError.getErrorMessage());
//            }
//        });
        Intent intent = new Intent(this, IMAuthManualActivity.class);
//        intent.putExtra("responseStr", responseStr);
        startActivity(intent);
    }

    private void registerDevice() {
        IMHelper.currentState = IPificationServices.Factory.generateState();
        IMHelper.registerDevice(IMHelper.currentToken, IMHelper.currentState);
    }

    private void openSuccessActivity(String responseStr) {
        Intent intent = new Intent(this, SuccessResultActivity.class);
        intent.putExtra("responseStr", responseStr);
        startActivity(intent);
    }

    private void openErrorActivity(String error) {
        Intent intent = new Intent(this, FailResultActivity.class);
        intent.putExtra("error", error);
        startActivity(intent);
    }

    // Todo: should be done on your server side (S2S)
    private void callTokenExchange(String code) {
//        IPHelper.doPostToken(code, (response, errorMessage) -> {
//            if (!response.equals("")) {
//                String phoneNumberVerified = Util.parseUserInfoJSON(response, "phone_number_verified");
//                String phoneNumber = Util.parseUserInfoJSON(response, "phone_number");
//                if (phoneNumberVerified.equals("true") || !phoneNumber.equals("")) {
//                    openSuccessActivity(response);
//                } else {
//                    openErrorActivity(response);
//                }
//            } else {
//                openErrorActivity(errorMessage);
//            }
//        });
    }

    private void doIMAuth(IPificationCallback callback) {
        AuthRequest.Builder authRequestBuilder = new AuthRequest.Builder();
        authRequestBuilder.setState(IMHelper.currentState);
        authRequestBuilder.setScope("openid ip:phone");
        authRequestBuilder.addQueryParam("channel", "wa viber telegram");

        IPificationServices.Factory.startAuthentication(this, authRequestBuilder.build(), callback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IMService.Factory.onActivityResult(requestCode, resultCode, data);
    }

    // Register FCM notification service
    public void initFirebase() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();
                    if (token != null) {
                        Log.d(TAG, "device token: " + token);
                        IMHelper.currentToken = token;
                    }
                });
    }
}
