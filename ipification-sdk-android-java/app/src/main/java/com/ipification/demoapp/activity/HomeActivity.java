package com.ipification.demoapp.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ipification.demoapp.Constant;
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
import com.ipification.mobile.sdk.im.IMLocale;
import com.ipification.mobile.sdk.im.IMService;
import com.ipification.mobile.sdk.im.IMTheme;

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

        initIPification();
        initActions();
        initFirebase();
    }

    private void initIPification() {

        IPConfiguration.getInstance().setCOVERAGE_URL(Uri.parse(Constant.CHECK_COVERAGE_URL));
        IPConfiguration.getInstance().setAUTHORIZATION_URL(Uri.parse(Constant.AUTH_URL));
        IPConfiguration.getInstance().setCLIENT_ID(Constant.CLIENT_ID);
        IPConfiguration.getInstance().setREDIRECT_URI(Uri.parse(Constant.REDIRECT_URI));

//       4. Theme (optional)
//        IPificationServices.Factory.setTheme(new IMTheme(Color.parseColor("#FFFFFF"), Color.parseColor("#E35259"),  Color.parseColor("#ACE1AF")));
//       5. Locale (optional)
//        IPificationServices.Factory.setLocale(new IMLocale( "IPification", "Description", "Whatsapp",  "Telegram",  "Viber","", View.VISIBLE));

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
        registerDevice();

        doIMAuth(new IPificationCallback() {
            @Override
            public void onIMCancel() {
                // hide loading or do nothing
            }

            @Override
            public void onSuccess(@NonNull AuthResponse authResponse) {
                if(authResponse.getCode() != null){
                    callTokenExchange(authResponse.getCode());
                }else{
                    openErrorActivity(authResponse.getErrorMessage());
                }
            }

            @Override
            public void onError(@NonNull IPificationError iPificationError) {
                openErrorActivity(iPificationError.getErrorMessage());
            }
        });
    }

    private void registerDevice() {
        ApiManager.currentState = IPificationServices.Factory.generateState();
        ApiManager.registerDevice(ApiManager.currentToken, ApiManager.currentState);
    }


    private void openSuccessActivity(String responseStr) {
        Intent intent = new Intent(this, SuccessResultActivity.class);
        intent.putExtra("responseStr", responseStr);
        startActivity(intent);

    }
    private void openErrorActivity(String error){
        Intent intent = new Intent(this, FailResultActivity.class);
        intent.putExtra(
                "error",
                error
        );

        startActivity(intent);
    }

    private void callTokenExchange(String code) {
        ApiManager.doPostToken(code, (response, errorMessage) -> {
            if(!response.equals("")){
                String phoneNumberVerified = Util.parseUserInfoJSON(response, "phone_number_verified");
                String phoneNumber = Util.parseUserInfoJSON(response, "phone_number");
                if(phoneNumberVerified.equals("true") || !phoneNumber.equals("")){
                    openSuccessActivity(response);
                }else{
                    openErrorActivity(response);
                }
            }else{
                openErrorActivity(errorMessage);
            }
        });
    }

    private void doIMAuth(IPificationCallback callback) {
        AuthRequest.Builder authRequestBuilder = new AuthRequest.Builder();
        authRequestBuilder.setState(ApiManager.currentState);
        authRequestBuilder.setScope("openid ip:phone");
        authRequestBuilder.addQueryParam("channel", "wa viber telegram");


        IPificationServices.Factory.startIMAuthentication(this, authRequestBuilder.build(), callback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IMService.Factory.onActivityResult(requestCode, resultCode, data);
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
                    ApiManager.currentToken = token;
                }
            });
    }
}