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

import org.json.JSONException;
import org.json.JSONObject;

public class SuccessResultActivity extends AppCompatActivity {

    private ActivitySuccessResultBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySuccessResultBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        initView();
        setupActionBar();
    }

    private void initView() {
        String tokenInfo = getIntent().getStringExtra("responseStr");
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(tokenInfo != null ? tokenInfo : "");
            binding.tvMainDetail.setText(jsonObject.toString(4)); // 4 is the number of spaces for indentation

        } catch (JSONException e) {
            binding.tvMainDetail.setText(tokenInfo);
        }

    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Result");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
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
}
