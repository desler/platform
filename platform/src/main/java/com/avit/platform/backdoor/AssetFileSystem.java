package com.avit.platform.backdoor;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public final class AssetFileSystem extends FileSystem {

    private final static String TAG = "AssetFileSystem";

    public AssetFileSystem(Context context, String filePath) {
        super(context, filePath);
    }

    @Override
    public InputStream getInputStream() throws IOException {

        String ap = filePath.substring(ASSET_BASE.length());
        Log.d(TAG, "getInputStream: " + ap);

        return getContext().getAssets().open(ap + File.separator + systemInfoClass.getName().toLowerCase() + ".config");
    }
}
