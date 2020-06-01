package com.avit.platform.backdoor;

import android.content.Context;
import android.util.Log;

import com.avit.platform.base.ISystem;
import com.avit.platform.base.Shared;

import java.io.File;

public final class USBSystem extends LocalFileSystem {

    protected final static String TAG = "USBSystem";
    private Shared shared;

    public USBSystem(Context context) {
        super(context);
    }

    @Override
    public boolean isInit() {
        return true;
    }

    @Override
    protected ISystem onCheck(ISystem.Callback callback) {
        if (this.shared == null) {
            shared = Shared.getInstance(getContext());
        }
        String cfgPath = shared.shared.getString((systemInfoClass.getName() + ".path").toUpperCase(), "AVIT.CFG.INVALID.PATH");

        File cfgF = new File(cfgPath);
        if (cfgF.exists()) {
            filePath = FileSystem.FILE_BASE + cfgPath.substring(0, cfgPath.lastIndexOf(File.separator));
            Log.d(TAG, "config ==>" + filePath);
            return super.onCheck(callback);
        }

        clear(shared);
        callback.onCallback(null);
        return this;
    }

    private void clear(Shared shared) {

        Class cls = systemInfoClass;
        shared.editor.remove((cls.getName() + ".path").toUpperCase());

        Log.w(TAG, "clear: ");
        shared.editor.commit();
    }
}
