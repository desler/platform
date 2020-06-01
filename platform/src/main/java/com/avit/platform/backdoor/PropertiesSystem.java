package com.avit.platform.backdoor;

import android.text.TextUtils;
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

    private String propPrefix;

    @Override
    public boolean isInit() {
        return true;
    }

    @Override
    protected Object onFetchValue(Field field) {

        if (TextUtils.isEmpty(propPrefix)) {
            this.propPrefix = ("avit." + systemInfoClass.getSimpleName()).toLowerCase();
//            Log.d(TAG, "onFetchValue: prefix --> " + propPrefix);
        }

        String propName = (propPrefix + "." + field.getName()).toLowerCase();
        int ol = 31 - (propPrefix + ".").length();
        if (propName.length() > 31) {
            Log.e(TAG, "fillSystemInfo: field " + field.getName() + " too long, never over " + ol);
            return null;
        }

//        Log.d(TAG, "onFetchValue: propName = " + propName);

        return Utils.getSystemProperties(propName);
    }
}
