package com.avit.platform.base;

import android.content.Context;
import android.content.SharedPreferences;

public class Shared {
    public final static String TAG = "Shared";

    private static Shared INSTANCE;
    public SharedPreferences.Editor editor;
    public SharedPreferences shared;

    public static Shared getInstance(Context context) {

        if (INSTANCE == null) {
            synchronized (TAG) {
                if (INSTANCE == null) {
                    INSTANCE = new Shared(context);
                }
            }
        }

        return INSTANCE;
    }

    private Shared(Context ctx) {

        String sharedName = ctx.getPackageName() + ".shared.platform";

        shared = ctx.getSharedPreferences(sharedName.toUpperCase(), Context.MODE_PRIVATE);
        editor = shared.edit();
    }
}
