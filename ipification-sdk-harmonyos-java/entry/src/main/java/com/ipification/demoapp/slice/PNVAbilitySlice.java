package com.ipification.demoapp.slice;

import com.ipification.demoapp.Constant;
import com.ipification.demoapp.ResourceTable;
import com.ipification.sdk.IPConfiguration;
import com.ipification.sdk.IPEnvironment;
import com.ipification.sdk.IPificationServices;
import com.ipification.sdk.ip.callback.IPAuthCallback;
import com.ipification.sdk.ip.callback.IPCoverageCallback;
import com.ipification.sdk.ip.error.IPificationError;
import com.ipification.sdk.ip.request.AuthRequest;
import com.ipification.sdk.ip.response.AuthResponse;
import com.ipification.sdk.ip.response.CoverageResponse;
import com.ipification.sdk.util.LogUtil;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Button;
import ohos.agp.components.Component;
import ohos.agp.components.TextField;
import ohos.utils.net.Uri;


/**
 * MainAbilitySlice
 */
public class PNVAbilitySlice extends AbilitySlice {
    int mode = 1;
    private String scope = "openid mobile_id";

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_pnv);

        Button button1 = (Button) findComponentById(ResourceTable.Id_button1);
        // Set a click event listener for the button.
        button1.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                String loginHint = ((TextField)findComponentById(ResourceTable.Id_input_phone)).getText();
                // Add the event processing logic for a click event on the button.
                Intent intent = new Intent();
                intent.setParam("scope", "openid ip:phone_verify");
                intent.setParam("login_hint", loginHint);
                present(new ProcessAbilitySlice(), intent);
            }
        });

        Button button2 = (Button) findComponentById(ResourceTable.Id_button2);
        // Set a click event listener for the button.
        button2.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                String loginHint = ((TextField)findComponentById(ResourceTable.Id_input_phone)).getText();
                // Add the event processing logic for a click event on the button.
                Intent intent = new Intent();
                intent.setParam("scope", "openid ip:phone ip:mobile_id");
                intent.setParam("login_hint", loginHint);
                present(new ProcessAbilitySlice(), intent);
            }
        });

    }
}
