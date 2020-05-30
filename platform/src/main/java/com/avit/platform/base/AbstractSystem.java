package com.avit.platform.base;

import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;

public abstract class AbstractSystem implements ISystem {

    protected final static String TAG = "AbstactSystem";

    public AbstractSystem next;

    private Complete complete;
    private Error error;

    protected Class systemInfoClass;
    protected Object systemInfo;

    public AbstractSystem() {
    }

    private Callback mCallback = new Callback() {
        @Override
        public void onCallback(Object value) {
            if (onCallback != null) {
                onCallback.onCallback(value);
            }
            if (next != null) {
                next.check(onCallback);
            } else if (complete != null) {
                complete.onComplete();
            }
        }
    };

    private Callback onCallback;

    @Override
    public final ISystem check(Callback callback) {
        onCallback = callback;
        return onCheck(mCallback);
    }

    public void setComplete(Complete complete) {
        this.complete = complete;
    }

    public void setError(Error error) {
        this.error = error;
    }

    protected boolean onError(String error) {
        if (this.error == null)
            return false;

        this.error.onError(error);
        return true;
    }

    public void setSystemInfoClass(Class systemInfoClass) {
        this.systemInfoClass = systemInfoClass;
    }

    protected ISystem onCheck(Callback callback) {
        fillSystemInfo();
        callback.onCallback(systemInfo);
        return this;
    }

    private void fillSystemInfo() {
        Class cls = systemInfoClass;

        String subTag = getClass().getSimpleName();
        try {
            systemInfo = cls.newInstance();
        } catch (Throwable e) {
            Log.e(TAG, subTag + "::fillSystemInfo: ", e);
            return;
        }

        while (cls != null && cls != Object.class) {
            Field[] fields = cls.getDeclaredFields();

            for (Field f : fields) {
                try {
                    Object value = onFetchValue(f);
                    if (value != null && !TextUtils.isEmpty(value.toString())) {
                        f.setAccessible(true);
                        f.set(systemInfo, value);
                        Log.d(TAG, subTag + "::fillSystemInfo: " + f.getName() + " = " + value);
                    }

                } catch (Throwable e) {
                    Log.w(TAG, subTag + "::fillSystemInfo: " + e);
                }
            }

            cls = cls.getSuperclass();
        }
    }

    protected abstract Object onFetchValue(Field field);
}
