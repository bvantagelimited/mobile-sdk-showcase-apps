package com.ipification.demoapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ipification.demoapp.R;
import com.ipification.demoapp.data.TokenInfo;
import com.ipification.demoapp.databinding.ActivityHomeBinding;
import com.ipification.demoapp.callback.TokenCallback;
import com.ipification.demoapp.manager.ApiManager;
import com.ipification.demoapp.util.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.ipification.mobile.sdk.android.CellularService;
import com.ipification.mobile.sdk.android.IPConfiguration;
import com.ipification.mobile.sdk.android.IPificationServices;
import com.ipification.mobile.sdk.android.callback.IPificationCallback;
import com.ipification.mobile.sdk.android.exception.IPificationError;
import com.ipification.mobile.sdk.android.request.AuthRequest;
import com.ipification.mobile.sdk.android.response.AuthResponse;
import com.ipification.mobile.sdk.im.IMService;

import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    ActivityHomeBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        ApiManager.currentState = IPificationServices.Factory.generateState();
        initActions();
        initFirebase();
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
        doIMAuth(new IPificationCallback() {
            @Override
            public void onSuccess(@NonNull AuthResponse authResponse) {
                if(authResponse.getCode() != null){
                    callTokenExchange(authResponse.getCode());
                }else{
                    openErrorActivity("code is empty", null);
                }
            }

            @Override
            public void onError(@NonNull IPificationError iPificationError) {
                openErrorActivity(iPificationError.getErrorMessage(), null);
            }
        });
    }

    private void handleTokenExchangeSuccess(String response) {
        try{
            JSONObject jObject = new JSONObject(response);
            String accessToken = jObject.getString("access_token");
            TokenInfo tokenInfo = Util.parseAccessToken(accessToken);
            if(tokenInfo != null && tokenInfo.phoneNumberVerified){
                openSuccessActivity(tokenInfo);
            }else{
                openErrorActivity("", tokenInfo);
            }
        }catch (Exception error){
            openErrorActivity(error.getLocalizedMessage(), null);
        }
    }


    private void handleTokenExchangeError(String error) {
        if(!error.equals("USER_CANCELED")){
            openErrorActivity(error, null);
        }
    }
    private void openSuccessActivity(TokenInfo tokenInfo) {
        Intent intent = new Intent(this, SuccessResultActivity.class);
        intent.putExtra("tokenInfo", tokenInfo);
        startActivity(intent);

    }
    private void openErrorActivity(String error, TokenInfo tokenInfo){
        Intent intent = new Intent(this, FailResultActivity.class);
        intent.putExtra(
                "error",
                error
        );
        if(tokenInfo != null){
            intent.putExtra("tokenInfo", tokenInfo);
        }
        startActivity(intent);
    }

    private void callTokenExchange(String code) {
        ApiManager.doPostToken(code, new TokenCallback() {
            @Override
            public void onError(String error) {
                handleTokenExchangeError(error);
            }

            @Override
            public void onSuccess(String response) {
                handleTokenExchangeSuccess(response);
            }
        });
    }

    private void doIMAuth(IPificationCallback callback) {
        AuthRequest.Builder authRequestBuilder = new AuthRequest.Builder();
        authRequestBuilder.setState(ApiManager.currentState);
        authRequestBuilder.setScope("openid ip:phone");
        authRequestBuilder.addQueryParam("channel", "wa viber telegram");
        // 5
//        IPificationServices.Factory.setTheme(new IMTheme(Color.parseColor("#FFFFFF"), Color.parseColor("#E35259"),  Color.parseColor("#ACE1AF"),  "IPification Verification", View.VISIBLE));
//        IPificationServices.Factory.setLocale(new IMLocale("IPification", "Description", "Whatsapp",  "Telegram",  "Viber"));

        IPificationServices.Factory.startIMAuthentication(this, authRequestBuilder.build(), callback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IMService.Factory.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        boolean result = CellularService.Companion.unregisterNetwork(this);
        Log.d("onDestroy", "unregisterNetwork: " + result);
    }


    //register FCM notification service
    public void initFirebase(){
        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                    return;
                }

                // Get new FCM registration token
                String token = task.getResult();
                if (token != null) {
                    Log.d(TAG, "device token: "+token);
                    ApiManager.registerDevice(token, ApiManager.currentState);
                }
            });
    }
}