package com.eje_c.vrmultiview.common;

public class GearVRDeviceInfo {
    public String ipAddress;
    public String imei;
    public int port;

    @Override
    public String toString() {
        return JSON.stringify(this);
    }
}
