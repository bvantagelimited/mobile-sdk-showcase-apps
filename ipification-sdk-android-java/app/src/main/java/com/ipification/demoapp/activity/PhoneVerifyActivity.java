package com.ipification.demoapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;


import com.ipification.demoapp.BuildConfig;
import com.ipification.demoapp.callback.CoverageCallback;
import com.ipification.demoapp.databinding.ActivityPhoneVerifyBinding;
import com.ipification.demoapp.manager.IPHelper;
import com.ipification.demoapp.util.Util;
import com.ipification.mobile.sdk.android.IPConfiguration;
import com.ipification.mobile.sdk.android.IPEnvironment;
import com.ipification.mobile.sdk.android.IPificationServices;
import com.ipification.mobile.sdk.android.callback.IPAuthCallback;
import com.ipification.mobile.sdk.android.callback.IPCoverageCallback;
import com.ipification.mobile.sdk.android.exception.IPificationError;
import com.ipification.mobile.sdk.android.request.AuthRequest;
import com.ipification.mobile.sdk.android.response.CoverageResponse;
import com.ipification.mobile.sdk.android.response.IPAuthResponse;
import com.ipification.mobile.sdk.im.IMService;
import com.mukesh.countrypicker.Country;
import com.mukesh.countrypicker.CountryPicker;

public class PhoneVerifyActivity extends AppCompatActivity {

    private static final String TAG = "PhoneVerifyActivity";
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
        binding.loginBtn.setOnClickListener(view1 -> callIPFlow());

        CountryPicker.Builder builder = new CountryPicker.Builder().with(this)
                .listener(country -> {
                    binding.countryCodeEditText.setText(country.getDialCode());
                    binding.phoneCodeEditText.requestFocus();
                });

        CountryPicker picker = builder.build();
        binding.countryCodeEditText.setShowSoftInputOnFocus(false);

        if (BuildConfig.ENVIRONMENT == "sandbox") {
            initializeSandboxEnvironment();
        } else {
            initializeProductionEnvironment(picker);
        }

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

    private void initializeSandboxEnvironment() {
        binding.countryCodeEditText.setText("+999");
        binding.countryCodeEditText.setEnabled(false);
        binding.phoneCodeEditText.setText("123456789");
        binding.phoneCodeEditText.requestFocus();
    }

    private void initializeProductionEnvironment(CountryPicker picker) {
        Country country = picker.getCountryFromSIM();
        binding.countryCodeEditText.setText(country.getDialCode());
        binding.countryCodeEditText.setEnabled(true);
        binding.phoneCodeEditText.setText("");
    }

    private void initIPification() {
        IPConfiguration.getInstance().setENV(BuildConfig.ENVIRONMENT.equals("sandbox") ? IPEnvironment.SANDBOX : IPEnvironment.PRODUCTION);
        IPConfiguration.getInstance().setCLIENT_ID(BuildConfig.CLIENT_ID);
        IPConfiguration.getInstance().setREDIRECT_URI(Uri.parse(BuildConfig.REDIRECT_URI));
    }

    private void callIPFlow() {
        disableBtn();

        callCheckCoverage((isAvailable, operatorCode, errorMessage) -> {
            if (isAvailable) {
                startAuth();
            } else {
                Log.e(TAG, "callCheckCoverage failed");
                openErrorActivity("callCheckCoverage failed");
            }
        });
    }

    private void startAuth() {
        IPAuthCallback callback = new IPAuthCallback() {
            @Override
            public void onSuccess(@NonNull IPAuthResponse authResponse) {
                callTokenExchange(authResponse.getCode());
            }

            @Override
            public void onError(@NonNull IPificationError iPificationError) {
                Log.e(TAG, "startAuth - error: " + iPificationError.getErrorMessage());
                openErrorActivity(iPificationError.getErrorMessage());
            }
        };

        AuthRequest.Builder authRequestBuilder = new AuthRequest.Builder();
        String phoneNumber = binding.countryCodeEditText.getText().toString() + binding.phoneCodeEditText.getText().toString();
        authRequestBuilder.addQueryParam("login_hint", phoneNumber);
        IPificationServices.Factory.startAuthentication(this, authRequestBuilder.build(), callback);
    }

    private void callCheckCoverage(CoverageCallback coverageCallback) {
        String phoneNumber = binding.countryCodeEditText.getText().toString() + binding.phoneCodeEditText.getText().toString();
        IPificationServices.Factory.startCheckCoverage(phoneNumber, this, new IPCoverageCallback() {
            @Override
            public void onSuccess(@NonNull CoverageResponse coverageResponse) {
                handleCoverageResponse(coverageResponse, coverageCallback);
            }

            @Override
            public void onError(@NonNull IPificationError e) {
                Log.e(TAG, "CheckCoverage Error: " + e.getErrorMessage());
                coverageCallback.result(false, "", e.getErrorMessage());
            }
        });
    }

    private void handleCoverageResponse(CoverageResponse coverageResponse, CoverageCallback coverageCallback) {
        if (coverageResponse.isAvailable()) {
            coverageCallback.result(true, coverageResponse.getOperatorCode(), "");
        } else {
            coverageCallback.result(false, coverageResponse.getOperatorCode(), "");
            Log.e(TAG, "CheckCoverage Failed: Not supported");
        }
    }

    private void openSuccessActivity(String responseStr) {
        enableBtn();
        Intent intent = new Intent(this, SuccessResultActivity.class);
        intent.putExtra("responseStr", responseStr);
        startActivity(intent);
    }

    private void openErrorActivity(String error) {
        enableBtn();
        Intent intent = new Intent(this, FailResultActivity.class);
        intent.putExtra("error", error);
        startActivity(intent);
    }

    private void callTokenExchange(String code) {
        IPHelper.doPostToken(code, (response, errorMessage) -> {
            handleTokenExchangeResponse(response, errorMessage);
        });
    }

    private void handleTokenExchangeResponse(String response, String errorMessage) {
        if (!response.equals("")) {
            String phoneNumberVerified = Util.parseUserInfoJSON(response, "phone_number_verified");
            String phoneNumber = Util.parseUserInfoJSON(response, "phone_number");
            if (phoneNumberVerified.equals("true") || !phoneNumber.equals("")) {
                openSuccessActivity(response);
            } else {
                openErrorActivity(response);
            }
        } else {
            openErrorActivity(errorMessage);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IMService.Factory.onActivityResult(requestCode, resultCode, data);
    }
    private void hideKeyboard(EditText countryCodeEditText) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(countryCodeEditText.getWindowToken(), 0);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
    public void disableBtn(){
        runOnUiThread(() -> {
            // UI-related code
            binding.loginBtn.setEnabled(false); // Disable the button during API call
            binding.loginBtn.setAlpha(0.5f); // Set alpha to 0.5 during API call
        });


    }

    public void enableBtn(){
        runOnUiThread(() -> {
            binding.loginBtn.setEnabled(true); // Enable the button after API call completes
            binding.loginBtn.setAlpha(1.0f); // Reset alpha to 1 (fully opaque) after API call completes
        });
    }
}