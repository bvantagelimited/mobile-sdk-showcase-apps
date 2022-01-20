package com.ipification.demoapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;


import com.ipification.demoapp.callback.TokenCallback;
import com.ipification.demoapp.data.TokenInfo;
import com.ipification.demoapp.databinding.ActivityPhoneVerifyBinding;
import com.ipification.demoapp.manager.ApiManager;
import com.ipification.demoapp.util.Util;
import com.ipification.mobile.sdk.android.IPConfiguration;
import com.ipification.mobile.sdk.android.IPificationServices;
import com.ipification.mobile.sdk.android.callback.IPificationCallback;
import com.ipification.mobile.sdk.android.exception.IPificationError;
import com.ipification.mobile.sdk.android.request.AuthRequest;
import com.ipification.mobile.sdk.android.response.AuthResponse;
import com.ipification.mobile.sdk.im.IMLocale;
import com.ipification.mobile.sdk.im.IMService;
import com.ipification.mobile.sdk.im.IMTheme;
import com.mukesh.countrypicker.Country;
import com.mukesh.countrypicker.CountryPicker;
import com.mukesh.countrypicker.listeners.OnCountryPickerListener;

import org.json.JSONObject;

import java.lang.reflect.Method;

public class PhoneVerifyActivity extends AppCompatActivity {

    private static final String TAG = "PhoneVerifyActivity" ;
    private ActivityPhoneVerifyBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneVerifyBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        initView();
    }

    private void initView() {
        hideKeyboard(binding.countryCodeEditText);
        binding.loginBtn.setOnClickListener(view1 -> {
            callIPFlow();

        });

        CountryPicker.Builder builder =
                new CountryPicker.Builder().with(this)
                        .listener(new OnCountryPickerListener() {
                            @Override
                            public void onSelectCountry(Country country) {
                                Log.d(TAG, "country code = " + country.getDialCode());
                                binding.countryCodeEditText.setText(country.getDialCode());
                                binding.phoneCodeEditText.requestFocus();
                            }
                        });

        binding.phoneCodeEditText.requestFocus();
        CountryPicker picker = builder.build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.countryCodeEditText.setShowSoftInputOnFocus(false);
        } else {
            try {
                final Method method = EditText.class.getMethod("setShowSoftInputOnFocus", boolean.class);

                method.setAccessible(true);
                method.invoke(binding.countryCodeEditText, false);
            } catch (Exception e) {
                // ignore
            }
        }
//
        binding.countryCodeEditText.setOnFocusChangeListener((view12, b) -> {
            if (b) {
                picker.showBottomSheet(PhoneVerifyActivity.this);
                hideKeyboard(binding.countryCodeEditText);
            }
        });
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
    }

    private void callIPFlow() {
        IPConfiguration.getInstance().setDebug(true);
        requestIPification(new IPificationCallback() {

            @Override
            public void onSuccess(@NonNull AuthResponse authResponse) {
                if(authResponse.getCode() != null){
                    callTokenExchange(authResponse.getCode());
                }else{
                    Log.e(TAG, "error: code is empty");
                    openErrorActivity("code is empty", null);
                }
            }

            @Override
            public void onError(@NonNull IPificationError iPificationError) {
                Log.e(TAG, "error: "+ iPificationError.getErrorMessage());
                openErrorActivity(iPificationError.getErrorMessage(), null);
            }
        });
    }


    private void requestIPification(IPificationCallback callback) {
        AuthRequest.Builder authRequestBuilder = new AuthRequest.Builder();
        String phoneNumber = binding.countryCodeEditText.getText().toString() + binding.phoneCodeEditText.getText().toString();
        authRequestBuilder.addQueryParam("login_hint", phoneNumber);
        authRequestBuilder.setState(ApiManager.currentState);
        authRequestBuilder.setScope("openid ip:phone_verify");
        authRequestBuilder.addQueryParam("channel", "ip wa viber telegram");

        // 5
        IPificationServices.Factory.setTheme(new IMTheme(Color.parseColor("#FFFFFF"), Color.parseColor("#E35259"),  Color.parseColor("#ACE1AF"),  "IPification Verification", View.VISIBLE));
        IPificationServices.Factory.setLocale(new IMLocale("IPification", "Description", "Whatsapp",  "Telegram",  "Viber"));

        IPificationServices.Factory.startAuthentication(this, authRequestBuilder.build(), callback);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IMService.Factory.onActivityResult(requestCode, resultCode, data);

    }

    private void hideKeyboard(EditText countryCodeEditText) {
        InputMethodManager inputMethodManager  =
                (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(countryCodeEditText.getWindowToken(), 0);
    }
}