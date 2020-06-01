package com.avit.platform;

import android.util.Log;

import com.avit.platform.backdoor.USBReceiver;
import com.avit.platform.base.AbstractSystem;
import com.avit.platform.base.DefaultSystem;
import com.avit.platform.base.ISystem;

import java.util.concurrent.ConcurrentHashMap;

public final class ObtainSystem implements ISystem.Complete, ISystem.Error, ISystem.OnChangeListener {

    public final static String TAG = "ObtainSystem";

    private final static ConcurrentHashMap<Class, ObtainSystem> cache = new ConcurrentHashMap<>();

    public static ObtainSystem getInstance(Class systemInfoCls) {

        ObtainSystem system = cache.get(systemInfoCls);
        if (system == null) {
            system = new ObtainSystem().withSystemInfoClass(systemInfoCls);
            cache.put(systemInfoCls, system);
        }

        return system;
    }

    private AbstractSystem head;
    private AbstractSystem prev;
    private AbstractSystem defaultSystem;
    private USBReceiver.ProxyRegister proxyRegister;

    private ISystem.Complete complete;
    private ISystem.Error error;

    private ISystem.Callback callback;

    private Class systemInfoClass;

    private ObtainSystem() {
        prev = head = new DefaultSystem();
        head.setComplete(this);
    }

    public ObtainSystem bindUSBReceiverRegister(USBReceiver.ProxyRegister register) {
        this.proxyRegister = register;
        this.proxyRegister.addReadClass(systemInfoClass, this);
        return this;
    }

    private ObtainSystem withSystemInfoClass(Class systemInfoClass) {
        this.systemInfoClass = systemInfoClass;
        this.head.setSystemInfoClass(systemInfoClass);
        return this;
    }

    public ObtainSystem addDefaultSystem(DefaultSystem defaultSystem) {

        Log.w(TAG, "addDefaultSystem: " + defaultSystem);

        defaultSystem.setSystemInfoClass(systemInfoClass);

        this.defaultSystem = defaultSystem;
        prev = head = defaultSystem;
        head.setComplete(this);

        return this;
    }

    public final ObtainSystem addSystem(AbstractSystem system) {
        Log.d(TAG, "addSystem: " + system);
        system.setSystemInfoClass(systemInfoClass);
        prev.next = system;
        prev = prev.next;
        prev.setComplete(this);
        return this;
    }

    public final void clear() {
        head.next = null;
        prev = head = (defaultSystem == null ? new DefaultSystem() : defaultSystem);
    }

    public final void obtain(ISystem.Callback callback) {
        Log.d(TAG, "check: " + callback);
        this.callback = callback;
        head.check(callback);
    }

    public final void obtain() {

        if (callback == null)
            throw new NullPointerException("system callback is null!!!");

        head.check(callback);
    }


    public final ObtainSystem setCallback(ISystem.Callback callback) {
        this.callback = callback;
        return this;
    }

    public final ObtainSystem setComplete(ISystem.Complete complete) {
        this.complete = complete;
        return this;
    }

    public final ObtainSystem setError(ISystem.Error error) {
        this.error = error;

        AbstractSystem prev = head;
        while (prev != null) {
            prev.setError(this);
            prev = prev.next;
        }

        return this;
    }

    @Override
    public void onComplete() {
        if (complete != null)
            complete.onComplete();
        else
            Log.d(TAG, "onComplete: DUMMY");
    }

    @Override
    public void onError(String error) {
        if (error != null) {
            this.error.onError(error);
        } else {
            Log.e(TAG, "onError: DUMMY");
        }
    }

    @Override
    public void onChange(ISystem system, Class cfgClass) {
        Log.w(TAG, system.getClass().getName() + "::onChange: " + cfgClass.getName());
    }
}
