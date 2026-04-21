package com.example.gmidboxsdk;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;


import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.ipification.mobile.auth.gmidbox.GMIDBoxRequest;
import com.ipification.mobile.auth.gmidbox.GMIDBoxResponse;
import com.ipification.mobile.auth.gmidbox.GMIDBoxServices;
import com.ipification.mobile.auth.gmidbox.callback.GMIDBoxCallback;
import com.ipification.mobile.auth.gmidbox.exception.GMIDBoxException;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity {

    private TextView textView;
    private TextInputEditText textInputEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        textInputEditText = findViewById(R.id.input);
        MaterialButton button = findViewById(R.id.button_connect);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    dismissKeyboard();
                    String endPoint = textInputEditText.getText().toString();
                    textView.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
                    textView.setText("Connecting ...");
                    makeRequest(endPoint);
                }catch (Exception e){
                    textView.setText("error: " + e.getMessage());
                }
            }
        });

    }

    private void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(textInputEditText.getWindowToken(), 0);
        }

    }

    private void makeRequest(String endpointUrl){
        GMIDBoxRequest gmidBoxRequest;
        GMIDBoxRequest.Builder builder = new GMIDBoxRequest.Builder();
        builder.addHeader("Accept", "application/json");
//
        builder.addQueryParam("format","json");
//        builder.addQueryParam("param2","value2");
//        builder.setConnectTimeout(3000);
//        builder.setReadTimeout(3000);
        gmidBoxRequest = builder.build();

        GMIDBoxCallback callback = new GMIDBoxCallback() {
            @Override
            public void onSuccess(@NotNull final GMIDBoxResponse gmidBoxResponse) {
                textView.post(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(gmidBoxResponse.getResponseData());
                    }
                });
            }

            @Override
            public void onError(@NotNull final GMIDBoxException e) {
                if (e.getException() != null) {
                    textView.post(() -> textView.setText(e.getException().getMessage()));
                }else{
                    textView.post(() -> textView.setText("something went wrong"));
                }
            }
        };

        GMIDBoxServices.Factory.requestTo(endpointUrl, this, gmidBoxRequest, callback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GMIDBoxServices.Factory.unregisterNetwork(MainActivity.this);
    }
}
