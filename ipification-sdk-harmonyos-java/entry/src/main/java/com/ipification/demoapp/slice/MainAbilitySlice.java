package com.ipification.demoapp.slice;

import com.ipification.demoapp.ResourceTable;
import com.ipification.sdk.IPConfiguration;
import com.ipification.sdk.IPEnvironment;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.colors.RgbColor;
import ohos.agp.colors.RgbPalette;
import ohos.agp.components.*;
import ohos.agp.components.element.ShapeElement;
import ohos.agp.components.element.StateElement;

/**
 * MainAbilitySlice
 */
public class MainAbilitySlice extends AbilitySlice {
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);
        RadioContainer radioContainer = (RadioContainer) findComponentById(ResourceTable.Id_radio_container);
        radioContainer.mark(1);
        int count = radioContainer.getChildCount();
        for (int i = 0; i < count; i++){
            ((RadioButton) radioContainer.getComponentAt(i)).setButtonElement(createStateElement());
        }

        Button button1 = (Button) findComponentById(ResourceTable.Id_button1);
        // Set a click event listener for the button.
        button1.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                // Add the event processing logic for a click event on the button.
                Intent intent = new Intent();
                present(new PNVAbilitySlice(), intent);
            }
        });

        Button button2 = (Button) findComponentById(ResourceTable.Id_button2);
        // Set a click event listener for the button.
        button2.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                // Add the event processing logic for a click event on the button.
                Intent intent = new Intent();
                intent.setParam("scope", "openid ip:phone ip:mobile_id");
                present(new ProcessAbilitySlice(), intent);
            }
        });

        Button button3 = (Button) findComponentById(ResourceTable.Id_button3);
        // Set a click event listener for the button.
        button3.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                // Add the event processing logic for a click event on the button.
                Intent intent = new Intent();
                intent.setParam("scope", "openid ip:mobile_id");
                present(new ProcessAbilitySlice(), intent);
            }
        });
        RadioContainer container = (RadioContainer) findComponentById(ResourceTable.Id_radio_container);
        container.setMarkChangedListener(new RadioContainer.CheckedStateChangedListener() {
            @Override
            public void onCheckedChanged(RadioContainer radioContainer, int index) {
                // You can refer to the following code to implement the function.
                if(index == 0){
                    IPConfiguration.getInstance().ENV = IPEnvironment.SANDBOX;
                }
                if(index == 1){
                    IPConfiguration.getInstance().ENV = IPEnvironment.PRODUCTION;
                }
            }
        });

    }

    private StateElement createStateElement() {
        ShapeElement elementButtonOn = new ShapeElement();
        elementButtonOn.setRgbColor(new RgbColor(15,44,211));
        elementButtonOn.setShape(ShapeElement.OVAL);

        ShapeElement elementButtonOff = new ShapeElement();
        elementButtonOff.setRgbColor(RgbPalette.LIGHT_GRAY);
        elementButtonOff.setShape(ShapeElement.OVAL);

        StateElement checkElement = new StateElement();
        checkElement.addState(new int[]{ComponentState.COMPONENT_STATE_CHECKED}, elementButtonOn);
        checkElement.addState(new int[]{ComponentState.COMPONENT_STATE_EMPTY}, elementButtonOff);
        return checkElement;
    }



}
