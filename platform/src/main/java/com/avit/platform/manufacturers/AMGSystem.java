package com.avit.platform.manufacturers;

import android.util.Log;

import com.avit.platform.base.AbstractSystem;
import com.avit.platform.base.ISystem;
import com.avit.platform.SystemInfo;
import com.avit.platform.base.Utils;

import java.lang.reflect.Field;

@SuppressWarnings("demo")
public class AMGSystem extends AbstractSystem {

    protected final static String TAG = "AMGSystem";

    @Override
    public boolean isInit() {
        return true;
    }

    @Override
    protected ISystem onCheck(ISystem.Callback callback) {

        if (systemInfoClass != null) {
            try {
                systemInfo = systemInfoClass.newInstance();
            } catch (Throwable e) {
                Log.e(TAG, "onCheck: ", e);
            }
        }

        if (systemInfo == null){
            callback.onCallback(null);
            return this;
        }

        SystemInfo systemInfo = (SystemInfo) this.systemInfo;

        systemInfo.deviceId = Utils.getSystemProperties("persist.sys.IpOta.Sn");
        systemInfo.client = systemInfo.deviceId;

        systemInfo.caRegionCode = Utils.getSystemProperties("persist.sys.area.id");
        systemInfo.caId = Utils.getSystemProperties("persist.sys.IpOta.ScardSn");

        systemInfo.userType = Utils.getSystemProperties("persist.sys.IpOta.ScardType");
        systemInfo.softwareCode = Utils.getSystemProperties("ro.product.rom.provider");
        systemInfo.hardwareVersion = Utils.getSystemProperties("persist.sys.IpOta.HwVer");
        systemInfo.manuCode = Utils.getSystemProperties("persist.sys.IpOta.FirmId");
        systemInfo.deviceType = Utils.getSystemProperties("ro.product.rom.model");
        callback.onCallback(this.systemInfo);

        return this;
    }

    @Override
    protected Object onFetchValue(Field field) {
        throw new IllegalStateException(getClass().getName() + " never call this function");
    }
}
