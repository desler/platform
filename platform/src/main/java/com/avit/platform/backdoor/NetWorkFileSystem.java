package com.avit.platform.backdoor;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public final class NetWorkFileSystem extends FileSystem {
    private final static String TAG = "NetWorkFileSystem";

    public NetWorkFileSystem(Context context, String filePath) {
        super(context, filePath);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        URL url = new URL(filePath + File.separator + systemInfoClass.getName().toLowerCase() + ".config");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        return connection.getInputStream();
    }
}
