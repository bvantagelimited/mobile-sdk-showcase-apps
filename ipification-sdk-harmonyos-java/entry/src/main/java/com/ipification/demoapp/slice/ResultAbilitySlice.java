package com.ipification.demoapp.slice;

import com.ipification.demoapp.ResourceTable;

import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.*;
import org.json.JSONObject;

/**
 * MainAbilitySlice
 */
public class ResultAbilitySlice extends AbilitySlice {
    private String response ;
    private String error;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_result);
        if (intent.hasParameter("response") && intent.getStringParam("response").isEmpty() == false) {
            response = intent.getStringParam("response");
            try {
                JSONObject jsonObject = new JSONObject(response);
                ((Text) findComponentById(ResourceTable.Id_response)).setText(jsonObject.toString(4));
            } catch (Exception e) {
                ((Text) findComponentById(ResourceTable.Id_response)).setText(response);
            }

        } else if (intent.hasParameter("error")) {
            error = intent.getStringParam("error");
            try {
                JSONObject jsonObject = new JSONObject(error);
                ((Text) findComponentById(ResourceTable.Id_response)).setText(jsonObject.toString(4));
            } catch (Exception e) {
                ((Text) findComponentById(ResourceTable.Id_response)).setText(error);
            }
        }

        findComponentById(ResourceTable.Id_sendlog).setClickedListener(new Component.ClickedListener() {

            @Override
            public void onClick(Component component) {
                //todo
            }
        });
        findComponentById(ResourceTable.Id_closelayout).setClickedListener(new Component.ClickedListener() {

            @Override
            public void onClick(Component component) {
                terminate();
            }
        });
    }
//        findComponentById(ResourceTable.Id_sendlog).addDrawTask((component, canvas) -> {
//            RichTextBuilder builder = new RichTextBuilder();
//            builder.mergeForm(new TextForm().setUnderline(true).setRelativeTextSize(1.5f));
//            builder.addText ("Underline 1.5x font size\n");
//            builder.build();
//        });

}
