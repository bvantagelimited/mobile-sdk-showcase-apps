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

import com.ipification.demoapp.BuildConfig;
import com.ipification.demoapp.activity.FailResultActivity;
import com.ipification.demoapp.activity.SuccessResultActivity;
import com.ipification.demoapp.callback.TokenCallback;
import com.ipification.demoapp.databinding.ActivityImBinding;
import com.ipification.demoapp.manager.IMHelper;
import com.ipification.mobile.sdk.android.IPConfiguration;
import com.ipification.mobile.sdk.android.IPEnvironment;
import com.ipification.mobile.sdk.android.exception.IPificationError;
import com.ipification.mobile.sdk.android.model.IMSession;
import com.ipification.mobile.sdk.android.request.AuthRequest;
import com.ipification.mobile.sdk.android.response.AuthResponse;
import com.ipification.mobile.sdk.android.response.IMResponse;
import com.ipification.mobile.sdk.im.IMService;
import com.ipification.mobile.sdk.im.data.IMInfo;
import com.ipification.mobile.sdk.im.listener.IMPublicAPICallback;
import com.ipification.mobile.sdk.android.IMPublicAPIServices;
import com.ipification.mobile.sdk.im.listener.RedirectDataCallback;

import java.util.List;

public class IMAuthManualActivity extends AppCompatActivity {
    private static final String TAG = "IMAuthManualActivity";
    private ActivityImBinding binding;
    private Boolean onNewIntent = false;
    private IMSession sessionInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initIPification();
        initView();
    }

    private void initIPification() {
        IPConfiguration.getInstance().setENV(BuildConfig.ENVIRONMENT.equals("sandbox") ?
                IPEnvironment.SANDBOX : IPEnvironment.PRODUCTION);
        IPConfiguration.getInstance().setCLIENT_ID(BuildConfig.CLIENT_ID);
        IPConfiguration.getInstance().setIM_AUTO_MODE(true);
        IPConfiguration.getInstance().setREDIRECT_URI(Uri.parse(BuildConfig.REDIRECT_URI));
    }

    private void initView() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("IM - Manual Implementation - " + BuildConfig.VERSION_NAME);
        }
        checkAndShowIMButtons();

        binding.whatsappBtn.setOnClickListener(v -> doIMFlow("wa"));
        binding.viberBtn.setOnClickListener(v -> doIMFlow("viber"));
        binding.telegramBtn.setOnClickListener(v -> doIMFlow("telegram"));
    }

    private void checkAndShowIMButtons() {
        disableButtonIfNotInstalled(binding.whatsappBtn, IPConfiguration.getInstance().getWhatsappPackageName());
        disableButtonIfNotInstalled(binding.telegramBtn, IPConfiguration.getInstance().getTelegramPackageName());
        disableButtonIfNotInstalled(binding.viberBtn, IPConfiguration.getInstance().getViberPackageName());
    }

    private void disableButtonIfNotInstalled(View button, String packageName) {
        PackageManager packageManager = getPackageManager();
        if (!isPackageInstalled(packageManager, packageName)) {
            button.setEnabled(false);
            button.setAlpha(0.3f);
        }
    }
    public boolean isPackageInstalled(PackageManager packageManager, String packageName) {
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IMService.Factory.onActivityResult(requestCode, resultCode, data);
    }

    private void doIMFlow(String channel) {
//        updateStateAndDeviceToken();

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
                //do nothing
            }
        };

        doIMAuth(channel, callback);
    }

    private void handleIMFlowSuccess(IMResponse imResponse) {
        sessionInfo = imResponse.getSessionInfo();
        List<IMInfo> validApp = IMPublicAPIServices.Factory.checkValidApps(sessionInfo, getPackageManager());
        runOnUiThread(new Runnable() {
            public void run() {
                IMPublicAPIServices.Factory.startGetRedirect(validApp.get(0).getMessage(), IMAuthManualActivity.this, new RedirectDataCallback() {
                    @Override
                    public void onResponse(String res) {
                        IMPublicAPIServices.Factory.openAppViaDeepLink(IMAuthManualActivity.this, res);
                    }
                });
            }
        });

    }

    private void handleIMFlowError(IPificationError error) {
        Log.d(TAG, "doIMAuth - error " + error.getErrorMessage());
//        Util.openErrorActivity(IMAuthManualActivity.this, error.getErrorMessage());
    }

    private void doIMAuth(String channel, IMPublicAPICallback callback) {
        AuthRequest.Builder authRequestBuilder = new AuthRequest.Builder();
        authRequestBuilder.setScope("openid ip:phone");
        authRequestBuilder.addQueryParam("channel", channel);
        IMPublicAPIServices.Factory.startAuthentication(IMAuthManualActivity.this, authRequestBuilder.build(), callback);
    }

    private void updateStateAndDeviceToken() {
//        IMHelper.setCurrentState(IPificationServices.generateState());
//        IMHelper.registerDevice(IMHelper.getDeviceToken(), IMHelper.getCurrentState());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        onNewIntent = true;
        finishSession(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!onNewIntent) {
            finishSession(getIntent());
        }
    }

    private void finishSession(Intent intent) {
         // cancel IP Notification if it's showing
         try {
             NotificationManagerCompat.from(this).cancel(IPConfiguration.getInstance().getNOTIFICATION_ID());
         } catch (Exception e) {
             e.printStackTrace();
         }

        if (sessionInfo == null) {
            return;
        }

        IMHelper.signIn(IPConfiguration.getInstance().getCurrentState(), new TokenCallback() {

            @Override
            public void onSuccess(String response) {
                sessionInfo = null;
                Intent intent = new Intent(IMAuthManualActivity.this, SuccessResultActivity.class);
                intent.putExtra("responseStr", response);
                startActivity(intent);
            }

            @Override
            public void onError(String errorMessage) {
                sessionInfo = null;
                Intent intent = new Intent(IMAuthManualActivity.this, FailResultActivity.class);
                intent.putExtra("error", errorMessage);
                startActivity(intent);
            }
        });
    }

}
