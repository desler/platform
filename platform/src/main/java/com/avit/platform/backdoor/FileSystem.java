package com.avit.platform.backdoor;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;

import com.avit.platform.base.AbstractSystem;
import com.avit.platform.base.ISystem;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 * local files, asset files, network
 */
public abstract class FileSystem extends AbstractSystem {

    private final static String TAG = "FileSystem";

    public static final String ASSET_BASE = "file:///android_asset/";
    //    public static final String RESOURCE_BASE = "file:///android_res/";
    public static final String FILE_BASE = "file://";
//    public static final String PROXY_BASE = "file:///cookieless_proxy/";
//    public static final String CONTENT_BASE = "content:";

    protected String filePath;
    private InputStream inputStream;
    private Properties properties;
    private Context context;

    public FileSystem(Context context, String filePath) {
        this.filePath = filePath;
        this.context = context;
    }

    public FileSystem(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    @Override
    protected ISystem onCheck(Callback callback) {

        if (TextUtils.isEmpty(this.filePath)) {
            callback.onCallback(null);
            return this;
        }

        properties = new Properties();
        try {
            inputStream = getInputStream();
            properties.load(inputStream);
        } catch (IOException e) {
            Log.e(TAG, "onCheck: " + e);
            callback.onCallback(null);
            return this;
        } finally {
            closeInputStream();
        }

        return super.onCheck(callback);
    }

    public abstract InputStream getInputStream() throws IOException;

    private void closeInputStream() {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "closeInputStream: ", e);
            }
        }
    }

    @Override
    protected Object onFetchValue(Field field) {
        if (properties == null) {
            return null;
        }
        return properties.getProperty(field.getName());
    }

    /**
     * 总是从 文件中读取
     *
     * @return
     */
    @Override
    public boolean isInit() {
        return false;
    }

    /**
     * @param context
     * @param filePath 这里的路径 是指的 到 某个类 对应的配置的文件的 全路径，但不包括 配置文件本身
     * @return
     */
    public final static FileSystem create(Context context, String filePath) {

        if (TextUtils.isEmpty(filePath)) {
            filePath = context.getFilesDir().getPath();
            Log.w(TAG, "create: use default path = " + filePath);
        }

        Uri fileUri = Uri.parse(filePath);

        if (TextUtils.isEmpty(fileUri.getScheme())) {
            filePath = FILE_BASE + filePath;
        }

        if (URLUtil.isNetworkUrl(filePath)) {
            return new NetWorkFileSystem(context, filePath);
        } else if (URLUtil.isFileUrl(filePath)) {
            return new LocalFileSystem(context, filePath);
        } else if (URLUtil.isAssetUrl(filePath)) {
            return new AssetFileSystem(context, filePath);
        } else {
            Log.e(TAG, "create: this file path not support, " + filePath);
            throw new IllegalArgumentException(filePath + " , not support");
        }
    }
}
