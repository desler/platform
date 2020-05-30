package com.avit.platform.base;

public interface ISystem {
    boolean isInit();

    ISystem check(Callback callback);

    interface Callback {
        void onCallback(Object value);
    }

    interface Complete{
        void onComplete();
    }

    interface Error{
        void onError(String error);
    }

    interface OnChangeListener {
        void onChange(ISystem system, Class cfgClass);
    }
}