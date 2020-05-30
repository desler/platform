package com.avit.platform.backdoor;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class LocalFileSystem extends FileSystem {

    private final static String TAG = "LocalFileSystem";
    public LocalFileSystem(Context context, String filePath) {
        super(context, filePath);
    }

    @Override
    public InputStream getInputStream() throws IOException {

        String lp = filePath.substring(FileSystem.FILE_BASE.length());

        return new FileInputStream(filePath + File.separator + systemInfoClass.getName().toLowerCase() + ".config");
    }
}
