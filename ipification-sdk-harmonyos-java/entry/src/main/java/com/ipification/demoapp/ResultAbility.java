package com.ipification.demoapp;

import com.ipification.demoapp.slice.MainAbilitySlice;
import com.ipification.demoapp.slice.ResultAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;

/**
 * MainAbility.
 */
public class ResultAbility extends Ability {
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setMainRoute(ResultAbilitySlice.class.getName());

    }
}
