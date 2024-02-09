package com.ipification.demoapp.activity.im;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.ipification.demoapp.BuildConfig;
import com.ipification.demoapp.activity.FailResultActivity;
import com.ipification.demoapp.activity.SuccessResultActivity;
import com.ipification.demoapp.callback.TokenCallback;
import com.ipification.demoapp.databinding.ActivityImBinding;
import com.ipification.demoapp.manager.IMHelper;
import com.ipification.mobile.sdk.android.IPConfiguration;
import com.ipification.mobile.sdk.android.IPEnvironment;
import com.ipification.mobile.sdk.android.IPificationServices;
import com.ipification.mobile.sdk.android.exception.IPificationError;
import com.ipification.mobile.sdk.android.model.IMSession;
import com.ipification.mobile.sdk.android.request.AuthRequest;
import com.ipification.mobile.sdk.android.response.AuthResponse;
import com.ipification.mobile.sdk.android.response.IMResponse;
import com.ipification.mobile.sdk.im.IMService;
import com.ipification.mobile.sdk.im.data.IMInfo;
import com.ipification.mobile.sdk.im.di.RepositoryModule;
import com.ipification.mobile.sdk.im.listener.IMPublicAPICallback;
import com.ipification.mobile.sdk.im.listener.RedirectDataCallback;
import com.ipification.mobile.sdk.android.IMPublicAPIServices;
import java.util.List;

public class IMAuthManualActivity extends AppCompatActivity {
    private static final String TAG = "IMAuthManualActivity";
    private ActivityImBinding binding;
    private Boolean onNewIntent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initIPification();
        initView();

        //FCM
        initFirebase();

    }


    // Initialize view components
    private void initView() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("IM - Manual Implementation - " + BuildConfig.VERSION_NAME);
        }
        checkAndShowIMButtons();

        // Set click listeners for IM buttons
        binding.whatsappBtn.setOnClickListener(v -> doIMFlow("wa"));
        binding.viberBtn.setOnClickListener(v -> doIMFlow("viber"));
        binding.telegramBtn.setOnClickListener(v -> doIMFlow("telegram"));
    }


    // Initialize IPification SDK
    private void initIPification() {
        // Set environment based on build configuration
        IPConfiguration.getInstance().setENV(BuildConfig.ENVIRONMENT.equals("sandbox") ?
                IPEnvironment.SANDBOX : IPEnvironment.PRODUCTION);
        IPConfiguration.getInstance().setCLIENT_ID(BuildConfig.CLIENT_ID);
        IPConfiguration.getInstance().setREDIRECT_URI(Uri.parse(BuildConfig.REDIRECT_URI));

        // enable auto mode
        IPConfiguration.getInstance().setIM_AUTO_MODE(true);
    }

    // Initiate IM authentication flow
    private void doIMFlow(String channel) {
        IMPublicAPICallback callback = new IMPublicAPICallback() {
            @Override
            public void onSuccess(IMResponse imResponse, AuthResponse ipResponse) {
                handleIMFlowSuccess(imResponse);
            }

            @Override
            public void onError(IPificationError error) {
                handleIMFlowError(error);
            }

            @Override
            public void onCancel() {
                // Do nothing
            }
        };
        doIMAuth(channel, callback);
    }

    // Handle IM authentication flow success
    private void handleIMFlowSuccess(IMResponse imResponse) {
        IMHelper.sessionInfo = imResponse.getSessionInfo();
        List<IMInfo> validApp = IMPublicAPIServices.Factory.checkValidApps(IMHelper.sessionInfo, getPackageManager());
        if(validApp.size() > 0){
            runOnUiThread(() -> {
                IMPublicAPIServices.Factory.startGetRedirect(validApp.get(0).getMessage(), IMAuthManualActivity.this, new RedirectDataCallback() {
                    @Override
                    public void onResponse(String res) {
                        IMPublicAPIServices.Factory.openAppViaDeepLink(IMAuthManualActivity.this, res);
                    }
                });
            });
        }else{
            // TODO: handle case: no IM app
        }
    }

    // Handle IM authentication flow error
    private void handleIMFlowError(IPificationError error) {
        // TODO
        Log.d(TAG, "doIMAuth - error " + error.getErrorMessage());
        Intent intent = new Intent(IMAuthManualActivity.this, FailResultActivity.class);
        intent.putExtra("error", error.getErrorMessage());
        startActivity(intent);
    }

    // Initiate IM authentication
    private void doIMAuth(String channel, IMPublicAPICallback callback) {
        AuthRequest.Builder authRequestBuilder = new AuthRequest.Builder();
        authRequestBuilder.setState(IMHelper.currentState); // important when you send notification
        authRequestBuilder.setScope("openid ip:phone");
        authRequestBuilder.addQueryParam("channel", channel);
        IMPublicAPIServices.Factory.startAuthentication(IMAuthManualActivity.this, authRequestBuilder.build(), callback);
    }


    // Handle new intent
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        onNewIntent = true;
        finishSession(intent);
    }

    // Handle resume
    @Override
    protected void onResume() {
        super.onResume();
        if (!onNewIntent) {
            finishSession(getIntent());
        }
    }

    // Finish session
    private void finishSession(Intent intent) {
        // cancel IP Notification if it's showing
        try {
            NotificationManagerCompat.from(this).cancel(IPConfiguration.getInstance().getNOTIFICATION_ID());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (getSessionID() == null) {
            return;
        }

        IMHelper.signIn(IPConfiguration.getInstance().getCurrentState(), new TokenCallback() {
            @Override
            public void onSuccess(String response) {
                //clear sessionInfo
                clearSessionInfo();
                Intent intent = new Intent(IMAuthManualActivity.this, SuccessResultActivity.class);
                intent.putExtra("responseStr", response);
                startActivity(intent);
            }

            @Override
            public void onError(String errorMessage) {
                //clear sessionInfo
                clearSessionInfo();

                Intent intent = new Intent(IMAuthManualActivity.this, FailResultActivity.class);
                intent.putExtra("error", errorMessage);
                startActivity(intent);
            }
        });
    }

    private void clearSessionInfo() {
        IMHelper.sessionInfo = null;
    }

    private String getSessionID() {
        if(IMHelper.sessionInfo != null){
            return IMHelper.sessionInfo.getSessionId();
        }
        return null;
    }

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
                        IMHelper.currentDeviceToken = token;
                        registerDevice();
                    }
                });
    }
    private void registerDevice() {
        IMHelper.currentState = IPificationServices.Factory.generateState();
        IMHelper.registerDevice(IMHelper.currentDeviceToken, IMHelper.currentState);
    }
    // Check and show IM buttons based on installed apps
    private void checkAndShowIMButtons() {
        String[] whatsappPackages = {IPConfiguration.getInstance().getWhatsappPackageName()};
        disableButtonIfNotInstalled(binding.whatsappBtn, whatsappPackages);

        // Disable Telegram button if Telegram package is not installed
        String[] telegramPackages = {IPConfiguration.getInstance().getTelegramPackageName(), IPConfiguration.getInstance().getTelegramWebPackageName()};
        disableButtonIfNotInstalled(binding.telegramBtn, telegramPackages);

        // Disable Viber button if Viber package is not installed
        String[] viberPackages = {IPConfiguration.getInstance().getViberPackageName()};
        disableButtonIfNotInstalled(binding.viberBtn, viberPackages);
    }

    // Disable button if app is not installed
    private void disableButtonIfNotInstalled(View button, String[] packageNames) {
        PackageManager packageManager = getPackageManager();
        boolean isInstalled = false;
        for (String packageName : packageNames) {
            if (isPackageInstalled(packageManager, packageName)) {
                isInstalled = true;
                break;
            }
        }
        if (!isInstalled) {
            button.setEnabled(false);
            button.setAlpha(0.3f);
        }
    }


    // Check if app is installed
    public boolean isPackageInstalled(PackageManager packageManager, String packageName) {
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }



    // Handle options item selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
