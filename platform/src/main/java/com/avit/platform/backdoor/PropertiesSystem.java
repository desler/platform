package com.avit.platform.backdoor;

import android.util.Log;

import com.avit.platform.base.AbstractSystem;
import com.avit.platform.base.Utils;

import java.lang.reflect.Field;

/**
 * 如果希望使用 android 属性系统来做 app的后门，
 * packageName + className + fieldName 不要太长，否则因为系统限制可能支持不太好
 */
public final class PropertiesSystem extends AbstractSystem {

    protected final static String TAG = "PropertiesSystem";

    @Override
    public boolean isInit() {
        return true;
    }

    @Override
    protected Object onFetchValue(Field field) {

        String clsName = systemInfoClass.getName();
        String propPrefix = clsName;
        /**
         * 此处之所以要对 class 全路径 进行裁剪，是因为 android 属性系统，中 name的最大长度为 31.
         */
        String[] subs = clsName.split(".");
        if (subs.length > 2) {
            propPrefix = subs[subs.length - 2] + "." + subs[subs.length - 1];
        }

        String propName = (propPrefix + "." + field.getName()).toLowerCase();
        int ol = 31 - (propPrefix + ".").length();
        if (propName.length() > 31) {
            Log.e(TAG, "fillSystemInfo: field " + field.getName() + " too long, never over " + ol);
            return null;
        }

        return Utils.getSystemProperties(propName);
    }
}
