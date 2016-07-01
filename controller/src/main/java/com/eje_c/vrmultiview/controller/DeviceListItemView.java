package com.eje_c.vrmultiview.controller;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eje_c.vrmultiview.common.GearVRDeviceInfo;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.device_list_item)
public class DeviceListItemView extends LinearLayout {

    @ViewById
    TextView text;

    public DeviceListItemView(Context context) {
        super(context);
    }

    public void bind(GearVRDeviceInfo gearVR) {
        text.setText(gearVR.imei);
    }
}
