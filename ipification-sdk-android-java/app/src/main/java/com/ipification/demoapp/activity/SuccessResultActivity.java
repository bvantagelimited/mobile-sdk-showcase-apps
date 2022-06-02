package com.ipification.demoapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
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
        ActionBar actionbar = getSupportActionBar();
        if(actionbar != null){
            //set actionbar title
            actionbar.setTitle("Result");
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initView() {
        String result = getIntent().getStringExtra("responseStr");

        binding.tvMainDetail.setText(result);


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