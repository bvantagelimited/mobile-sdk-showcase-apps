package com.example.ipificationsdk;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.auth0.android.jwt.JWT;
import com.ipification.mobile.sdk.android.CellularService;
import com.ipification.mobile.sdk.android.callback.CellularCallback;
import com.ipification.mobile.sdk.android.exception.CellularException;
import com.ipification.mobile.sdk.android.request.AuthRequest;
import com.ipification.mobile.sdk.android.response.AuthResponse;
import com.ipification.mobile.sdk.android.response.CellularResponse;
import com.ipification.mobile.sdk.android.response.CoverageResponse;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashSet;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private Button button;

    private TextView resultTextView;
    private EditText phoneInputText;

    private TextView coverageLabel;
    private TextView coverageView;
    private TextView authLabel;
    private TextView authView;
    private LinkedHashSet<Long> coverageStats;
    private LinkedHashSet<Long> authStats;

    private final String EXCHANGE_TOKEN_ENDPOINT = "https://stage.ipification.com/auth/realms/ipification/protocol/openid-connect/token";
    private boolean progressing;

    private boolean hideStats = true;
    private long startCoverageTime;
    private long startAuthTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.buttonSDK);
        resultTextView = findViewById(R.id.textViewSDK);
        phoneInputText = findViewById(R.id.inputPhoneEditText);
        coverageLabel = findViewById(R.id.coverageLabel);
        coverageView = findViewById(R.id.coverageView);
        authLabel = findViewById(R.id.authLabel);
        authView = findViewById(R.id.authView);

        if (hideStats) {
            coverageLabel.setVisibility(TextView.INVISIBLE);
            coverageView.setVisibility(TextView.INVISIBLE);
            authLabel.setVisibility(TextView.INVISIBLE);
            authView.setVisibility(TextView.INVISIBLE);
        }
        coverageStats = new LinkedHashSet<Long>();
        authStats = new LinkedHashSet<Long>();

        initActions();
    }

    private void initActions() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resultTextView.setText("checking coverage....");
                checkCoverage();
            }
        });
    }

    private void checkCoverage() {
        if (progressing) {
            return;
        }
        startCoverageTime = System.currentTimeMillis();
        progressing = true;
        CellularService<CoverageResponse> cellularService = new CellularService<>(this);
        cellularService.checkCoverage(new CellularCallback<CoverageResponse>() {
            @Override
            public void onSuccess(CoverageResponse coverageResponse) {
                boolean isAvailable = coverageResponse.isAvailable();
                if(isAvailable){
                    doAuthorization();
                } else {
                    showErrorMessage("checkCoverage - unsupported network");
                }
            }

            @Override
            public void onError(@NotNull CellularException e) {
                showErrorMessage("checkCoverage Error: " + e.getError_code() + " - " + e.getException().getMessage());
            }
        });
    }

    private void doAuthorization() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultTextView.setText("authorizing....");
            }
        });
        startAuthTime = System.currentTimeMillis();
        CellularService<AuthResponse> authService = new CellularService<>(this);
        CellularCallback<AuthResponse> callback =  new CellularCallback<AuthResponse>() {
            @Override
            public void onSuccess(AuthResponse authResponse) {
                if(authResponse.getCode() != null){
                    showMessage("code: "+authResponse.getCode());
                    exchangeToken(authResponse.getCode(), new CellularCallback<CellularResponse>() {
                        @Override
                        public void onSuccess(CellularResponse cellularResponse) {

                            JSONObject jObject;
                            try {
                                jObject = new JSONObject(cellularResponse.getResponseData());
                                JWT jtw = new JWT(jObject.getString("access_token"));
                                Boolean phone_number_verified = jtw.getClaim("phone_number_verified").asBoolean();
                                String sub = jtw.getClaim("sub").asString();
                                showMessage("- phone_number_verified: " + phone_number_verified + "\n- sub: " + sub);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                showErrorMessage(e.getMessage());
                            }
                        }

                        @Override
                        public void onError(@NotNull CellularException e) {
                            if (e.getException() != null) {
                                showErrorMessage(e.getException().getMessage());
                            } else {
                                showErrorMessage(e.getError_code());
                            }
                        }
                    });
                } else {
                    showErrorMessage("Authorization Error: code is null");
                }
            }

            @Override
            public void onError(@NotNull CellularException e) {
                showErrorMessage("Authorization Error: " + e.getError_code() + " - " + e.getException().getMessage());
            }
        };
        AuthRequest.Builder authRequestBuilder = new AuthRequest.Builder();
        authRequestBuilder.addQueryParam("state", "213e23423423423423423423"); // generate state https://auth0.com/docs/protocols/state-parameters
        authRequestBuilder.addQueryParam("login_hint", phoneInputText.getText().toString());
        authService.performAuth(authRequestBuilder.build(), callback);
    }

    private void showErrorMessage(final String message) {
        Log.d("message", "message" + message);
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                progressing = false;
                resultTextView.setText(message);
                resultTextView.setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_red_dark));
            }
        });
    }

    private void showMessage(final String message) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                progressing = false;
                resultTextView.setText(message);
                resultTextView.setTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_blue_dark));
            }
        });
    }

    private void exchangeToken(String code, CellularCallback<CellularResponse> callBack) {
        try {
            String url = EXCHANGE_TOKEN_ENDPOINT;
            CellularService cellularService = new CellularService<CoverageResponse>(this);
            RequestBody body = new FormBody.Builder()
                    .add("client_id", cellularService.getConfiguration("client_id"))
                    .add("redirect_uri", cellularService.getConfiguration("redirect_uri"))
                    .add("grant_type", "authorization_code")
                    .add("client_secret", "")
                    .add("code", code)
                    .build();

            OkHttpClient client = new OkHttpClient.Builder().build();
            Request request = new Request.Builder().url(url).post(body)
                    .build();
            Call call = client.newCall(request);
            Response response = call.execute();
            if (response.code() == 200) {
                callBack.onSuccess(new CellularResponse(response.code(), response.body().string()));
            } else {
                callBack.onError(new CellularException(new Exception(response.body().string())));
            }

        } catch (Exception e) {
            e.printStackTrace();
            callBack.onError(new CellularException(e));
        }
    }
}