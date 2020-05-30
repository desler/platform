package com.avit.platform.base;

import android.util.Log;

import java.lang.reflect.Method;

public class Utils {
    public static String getSystemProperties(String key) {
        String result = "";
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");

            Method get = c.getMethod("get", String.class);
            result = (String) get.invoke(c, key);

        } catch (Exception e) {
            Log.e("Utils", "getSystemProperties: ", e);
        }

        return result;
    }
}
