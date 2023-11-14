package com.ipification.demoapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
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
        setupActionBar();
    }

    private void initView() {
        String result = getIntent().getStringExtra("error");

        binding.tvMainDetail.setText(result);

        binding.btnRestart.setOnClickListener(view -> onBackPressed());
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Set action bar title
            actionBar.setTitle("Result");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // API 5+ solution
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
