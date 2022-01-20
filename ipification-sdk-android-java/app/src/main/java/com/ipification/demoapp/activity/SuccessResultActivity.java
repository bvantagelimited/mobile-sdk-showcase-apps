package com.ipification.demoapp.activity;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.ipification.demoapp.R;
import com.ipification.demoapp.data.TokenInfo;
import com.ipification.demoapp.databinding.ActivityPhoneVerifyBinding;
import com.ipification.demoapp.databinding.ActivitySuccessResultBinding;

public class SuccessResultActivity extends AppCompatActivity {

    private ActivitySuccessResultBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySuccessResultBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        initView();
    }

    private void initView() {
        TokenInfo tokenInfo = getIntent().getParcelableExtra("tokenInfo");
        if(tokenInfo == null){
            return;
        }
        Log.d("tokenInfo", ""+ tokenInfo.phoneNumber);
        String phoneNumber = tokenInfo.phoneNumber;
        boolean phoneNumberVerified = tokenInfo.phoneNumberVerified;
        String sub = tokenInfo.sub != null ? " | sub: " + tokenInfo.sub : "";
        String mobileID = tokenInfo.mobileID != null ? " | mobileID: " + tokenInfo.mobileID  : "";
        String loginHint = tokenInfo.loginHint != null ? " | Phone Number: " + tokenInfo.loginHint :  "";
        String result = "Phone Number Verified: " + phoneNumberVerified + " " + loginHint;
        if(phoneNumber != null){
            result = "Phone Number: " + phoneNumber;
        }


        binding.tvMainDetail.setText(result);
        binding.detail.setText(result + sub + mobileID);
        binding.btnRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        ActionBar actionbar = getSupportActionBar();
        if(actionbar != null){
            //set actionbar title
            actionbar.setTitle("Result");
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
    }
}