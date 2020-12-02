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
import com.ipification.mobile.auth.gmidbox.CellularCallback;
import com.ipification.mobile.auth.gmidbox.CellularException;
import com.ipification.mobile.auth.gmidbox.CellularRequest;
import com.ipification.mobile.auth.gmidbox.CellularResponse;
import com.ipification.mobile.auth.gmidbox.CellularService;

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
        MaterialButton button = findViewById(R.id.button);



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    dismissKeyboard();
                    Uri endPoint = Uri.parse(textInputEditText.getText().toString());
                    if(endPoint == null || endPoint.getHost() == null){
                        textView.setText("error: url is not correct");
                        textView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        return;
                    }
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

    private void makeRequest(Uri endPoint){
        CellularRequest cellularRequest;
        CellularService cellularService = new CellularService(this);

        CellularRequest.Builder builder = new CellularRequest.Builder(endPoint);
        builder.addHeader("Accept", "application/json");

        builder.addQueryParam("param1","value1");
        builder.addQueryParam("param2","value2");



        builder.setConnectTimeout(10000);
        builder.setReadTimeout(12000);
        cellularRequest = builder.build();
        cellularService.registerCallback(new CellularCallback() {
            @Override
            public void onSuccess(@NotNull final CellularResponse cellularResponse) {
                textView.post(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(cellularResponse.getResponseData());
                    }
                });
            }

            @Override
            public void onError(@NotNull final CellularException e) {
                if (e.getException() != null) {
                    textView.post(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText(e.getException().getMessage());
                        }
                    });
                }else{
                    textView.post(new Runnable() {

                        @Override
                        public void run() {
                            textView.setText("something went wrong");
                        }
                    });
                }
            }
        });

        cellularService.performRequest(cellularRequest);
    }
}
