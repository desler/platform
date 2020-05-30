package com.avit.platform.base;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import java.lang.reflect.Field;

public final class ManifestSystem extends AbstractSystem {

    protected final static String TAG = "ManifestSystem";

    private Context context;
    private Bundle metaData;

    public ManifestSystem(Context context) {
        this.context = context;
    }

    @Override
    public boolean isInit() {
        return true;
    }

    @Override
    protected ISystem onCheck(Callback callback) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            metaData = appInfo.metaData;
        } catch (Exception e) {
            Log.e(TAG, "onCheck: ", e);
        }

        if (metaData == null) {
            Log.e(TAG, "onCheck : NOT FOUND !!!");
            callback.onCallback(null);
            return this;
        }

        return super.onCheck(callback);
    }

    @Override
    protected Object onFetchValue(Field field) {
        String metaName = (systemInfoClass.getName()+ "." + field.getName()).toLowerCase();
        return metaData.get(metaName);
    }
}
