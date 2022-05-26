package com.ipification.demoapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;


import com.ipification.demoapp.Constant;
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
        initIPification();
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

        CountryPicker picker = builder.build();
        Country country = picker.getCountryFromSIM();

        binding.countryCodeEditText.setText(country.getDialCode());
        binding.countryCodeEditText.setShowSoftInputOnFocus(false);
        //
        binding.countryCodeEditText.setOnFocusChangeListener((view12, b) -> {
            if (b) {
                picker.showBottomSheet(PhoneVerifyActivity.this);
                hideKeyboard(binding.countryCodeEditText);
            }
        });
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
    }
    private void initIPification() {

        IPConfiguration.getInstance().setCOVERAGE_URL(Uri.parse(Constant.CHECK_COVERAGE_URL));
        IPConfiguration.getInstance().setAUTHORIZATION_URL(Uri.parse(Constant.AUTH_URL));
        IPConfiguration.getInstance().setCLIENT_ID(Constant.CLIENT_ID);
        IPConfiguration.getInstance().setREDIRECT_URI(Uri.parse(Constant.REDIRECT_URI));

    }
    private void callIPFlow() {
        IPConfiguration.getInstance().setDebug(true);
        callIPFlow(new IPificationCallback() {

            @Override
            public void onIMCancel() {
                // do nothing, only check if IM is enabled
                
            }

            @Override
            public void onSuccess(@NonNull AuthResponse authResponse) {
                if(authResponse.getCode() != null){
                    callTokenExchange(authResponse.getCode());
                }else{
                    Log.e(TAG, "error: code is empty");
                    openErrorActivity("code is empty");
                }
            }

            @Override
            public void onError(@NonNull IPificationError iPificationError) {
                Log.e(TAG, "error: "+ iPificationError.getErrorMessage());
                openErrorActivity(iPificationError.getErrorMessage());
            }
        });
    }




    private void callIPFlow(IPificationCallback callback) {
        AuthRequest.Builder authRequestBuilder = new AuthRequest.Builder();
        String phoneNumber = binding.countryCodeEditText.getText().toString() + binding.phoneCodeEditText.getText().toString();
        authRequestBuilder.addQueryParam("login_hint", phoneNumber);
        authRequestBuilder.setState(ApiManager.currentState); // set your State if you want to receive notification
        authRequestBuilder.setScope("openid ip:phone_verify");
//        authRequestBuilder.addQueryParam("channel", "ip wa viber telegram");


        IPificationServices.Factory.startAuthentication(this, authRequestBuilder.build(), callback);
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {// API 5+ solution
            onBackPressed();
            return true;
        } else {
            super.onOptionsItemSelected(item);
        }
        return false;
    }
}