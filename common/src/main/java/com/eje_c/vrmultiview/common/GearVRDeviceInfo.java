package com.eje_c.vrmultiview.common;

import java.io.Serializable;

public class GearVRDeviceInfo implements Serializable {
    public String ipAddress;
    public String imei;
    public int port;

    @Override
    public String toString() {
        return JSON.stringify(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GearVRDeviceInfo info = (GearVRDeviceInfo) o;

        if (port != info.port) return false;
        if (ipAddress != null ? !ipAddress.equals(info.ipAddress) : info.ipAddress != null)
            return false;
        return imei != null ? imei.equals(info.imei) : info.imei == null;

    }

    @Override
    public int hashCode() {
        int result = ipAddress != null ? ipAddress.hashCode() : 0;
        result = 31 * result + (imei != null ? imei.hashCode() : 0);
        result = 31 * result + port;
        return result;
    }
}
