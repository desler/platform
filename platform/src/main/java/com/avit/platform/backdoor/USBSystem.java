package com.avit.platform.backdoor;

import android.content.Context;
import android.util.Log;

import com.avit.platform.base.AbstractSystem;
import com.avit.platform.base.ISystem;
import com.avit.platform.base.Shared;

import java.io.File;
import java.lang.reflect.Field;

public final class USBSystem extends AbstractSystem {

    protected final static String TAG = "USBSystem";

    private Context context;
    private Shared shared;

    public USBSystem(Context context) {
        this.context = context;
    }

    @Override
    public boolean isInit() {
        return true;
    }

    @Override
    protected ISystem onCheck(ISystem.Callback callback) {
        if (this.shared == null) {
            shared = Shared.getInstance(context);
        }

        String cfgPath = shared.shared.getString((systemInfoClass.getName() + ".path").toUpperCase(), "AVIT.CFG.INVALID.PATH");

        File cfgF = new File(cfgPath);
        if (cfgF.exists()) {
            Log.d(TAG, "config ==>" + cfgPath);
            return super.onCheck(callback);
        }

        clear(shared);
        callback.onCallback(null);
        return this;
    }

    @Override
    protected Object onFetchValue(Field field) {
        String usbName = (systemInfoClass.getName() + "." + field.getName()).toUpperCase();
        return shared.shared.getString(usbName, "");
    }

    private void clear(Shared shared) {

        Class cls = systemInfoClass;

        while (cls != null) {
            Field[] fields = cls.getDeclaredFields();

            for (Field f : fields) {
                String usbName = (systemInfoClass.getName() + "." + f.getName()).toUpperCase();
                shared.editor.remove(usbName);
            }

            cls = cls.getSuperclass();
        }
        shared.editor.remove((systemInfoClass.getName() + ".path").toUpperCase());

        Log.w(TAG, "clear: ");

        shared.editor.commit();
    }
}
