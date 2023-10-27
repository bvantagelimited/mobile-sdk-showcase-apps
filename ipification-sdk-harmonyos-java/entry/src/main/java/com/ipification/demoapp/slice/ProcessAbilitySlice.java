package com.ipification.demoapp.slice;

import com.ipification.demoapp.Constant;
import com.ipification.demoapp.IPUtil;
import com.ipification.demoapp.ResourceTable;
import com.ipification.demoapp.TokenCallback;
import com.ipification.sdk.IPConfiguration;
import com.ipification.sdk.IPEnvironment;
import com.ipification.sdk.IPificationServices;
import com.ipification.sdk.ip.callback.IPAuthCallback;
import com.ipification.sdk.ip.callback.IPCoverageCallback;
import com.ipification.sdk.ip.error.IPificationError;
import com.ipification.sdk.ip.request.AuthRequest;
import com.ipification.sdk.ip.response.AuthResponse;
import com.ipification.sdk.ip.response.CoverageResponse;
import com.ipification.sdk.ip.response.IPAuthResponse;
import com.ipification.sdk.util.LogUtil;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.utils.net.Uri;


/**
 * MainAbilitySlice
 */
public class ProcessAbilitySlice extends AbilitySlice {
    private String scope = "openid mobile_id";
    private String login_hint = "";

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_loading);
        if (intent.hasParameter("scope")) {
            scope = intent.getStringParam("scope");
        }
        if(intent.hasParameter("login_hint")){
            login_hint = intent.getStringParam("login_hint");
        }

        initData();
    }
    private void initData(){
        if (IPConfiguration.getInstance().ENV == IPEnvironment.SANDBOX) {
            // Sandbox environment
            IPConfiguration.getInstance().CLIENT_ID = Constant.STAGE_CLIENT_ID;
            IPConfiguration.getInstance().REDIRECT_URI = Uri.parse(Constant.STAGE_REDIRECT_URI);
        } else {
            IPConfiguration.getInstance().CLIENT_ID = Constant.LIVE_CLIENT_ID;
            IPConfiguration.getInstance().REDIRECT_URI = Uri.parse(Constant.LIVE_REDIRECT_URI);
        }
        doAuthentication();
    }

    private void doAuthentication() {
        String phoneNo = "";
        IPCoverageCallback coverageCallback = new IPCoverageCallback() {
            @Override
            public void onSuccess(CoverageResponse response) {
                LogUtil.info("IPification", response.getResponseData());
                if(response.isAvailable()){
                    callIPAuth();
                }else{
                    IPUtil.handleTokenExchangeError(ProcessAbilitySlice.this, "your telco is not supported");
                }
            }

            @Override
            public void onError(IPificationError error) {
                LogUtil.error("IPification", error.getErrorMessage());
                IPUtil.handleTokenExchangeError(ProcessAbilitySlice.this, error.getErrorMessage());
            }
        };
        IPificationServices.startCheckCoverage(this,  phoneNo,  coverageCallback);
    }

    private void callIPAuth() {
        IPAuthCallback authCallback = new IPAuthCallback() {
            @Override
            public void onSuccess(IPAuthResponse response) {
                LogUtil.info("IPification", response.fullResponse);
                IPUtil.doPostToken(ProcessAbilitySlice.this, response.code, new TokenCallback(){
                    @Override
                    public void onError(String error) {
                        IPUtil.handleTokenExchangeError(ProcessAbilitySlice.this, error);
                    }
                    @Override
                    public void onSuccess(String response) {
                        IPUtil.handleTokenExchangeSuccess(ProcessAbilitySlice.this, response);
                    }
                });

            }

            @Override
            public void onError(IPificationError error) {
                LogUtil.error("IPification", error.getErrorMessage());
                IPUtil.handleTokenExchangeError(ProcessAbilitySlice.this, error.getErrorMessage());
            }
        };
        AuthRequest.Builder authRequestBuilder = new AuthRequest.Builder();
        authRequestBuilder.setScope(scope);
        if(!login_hint.isEmpty()){
            authRequestBuilder.addQueryParam("login_hint", login_hint);
        }
        AuthRequest authRequest = authRequestBuilder.build();
        IPificationServices.startAuthentication(getAbility(), authRequest, authCallback);
    }


}
