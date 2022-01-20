package com.ipification.demoapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.ipification.demoapp.R;
import com.ipification.demoapp.data.TokenInfo;
import com.ipification.demoapp.databinding.ActivityFailResultBinding;
import com.ipification.demoapp.databinding.ActivityPhoneVerifyBinding;

public class FailResultActivity extends AppCompatActivity {

    private ActivityFailResultBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFailResultBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        initView();
    }

    private void initView() {
        String errorMessage = getIntent().getStringExtra("error");
        String result = errorMessage;
        TokenInfo tokenInfo = getIntent().getParcelableExtra("tokenInfo");
        if(tokenInfo != null){
            boolean phoneNumberVerified = tokenInfo.phoneNumberVerified;
            String phoneNumber = tokenInfo.phoneNumber;
            String sub = tokenInfo.sub != null ? " | sub: " + tokenInfo.sub : "";
            String mobileID = tokenInfo.mobileID != null ? " | mobileID: " + tokenInfo.mobileID  : "";
            String loginHint = tokenInfo.loginHint != null ? " | Phone Number: " + tokenInfo.loginHint :  "";
            result = "Phone Number Verified: " + phoneNumberVerified + " " + loginHint;
            if(phoneNumber != null){
                result = "Phone Number: " + phoneNumber;
            }
            binding.detail.setText(result + sub + mobileID);
        }
        binding.tvMainDetail.setText(result);


        binding.btnRestart.setOnClickListener(view -> onBackPressed());
    }
}