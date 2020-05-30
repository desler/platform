package com.avit.platform.backdoor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.avit.platform.base.ISystem;
import com.avit.platform.base.Shared;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public final class USBReceiver extends BroadcastReceiver {

    private static final String TAG = "UsbReceiver";

    private Context mContext;
    private SharedPreferences.Editor editor;
    private Handler mHandler;

    private static ProxyRegister sInstance;

    public final static ProxyRegister getRegister() {
        if (sInstance == null) {
            synchronized (sInstance) {
                if (sInstance == null) {
                    sInstance = new ProxyRegister(new USBReceiver());
                }
            }
        }
        return sInstance;
    }

    @Override
    public void onReceive(Context context, Intent arg1) {

        if (this.mContext == null) {
            Log.d(TAG, "onReceive: init");
            this.mContext = context;
            this.editor = Shared.getInstance(context).editor;
            this.mHandler = new MountHandler(this);
        }

        Log.v(TAG, "usb action = " + arg1.getAction());

        if (arg1.getAction().equals(Intent.ACTION_MEDIA_MOUNTED) //
                || arg1.getAction().equals(Intent.ACTION_MEDIA_CHECKING)) {

            Message msg = Message.obtain(mHandler);
            String dir = arg1.getDataString();

            Log.d(TAG, "onReceive: dir" + dir);

            if (TextUtils.isEmpty(dir))
                return;

            msg.what = 0x1000;
            msg.obj = dir;
            msg.sendToTarget();
        }
    }


    private boolean loadProperties2Shared(String path, Class cls) {

        Properties properties = new Properties();

        String configPath = (path + File.separator + cls.getName()).toLowerCase() + ".config";

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(configPath);
            properties.load(fileInputStream);
            Enumeration enumeration = properties.elements();

            Log.d(TAG, "loadProperties2Shared: config path = " + configPath);

            /**
             * 加载所有的属性
             */
            boolean isNeedCommit = false;
            while (enumeration.hasMoreElements()) {
                String fieldName = (String) enumeration.nextElement();

                /**
                 * 寻找类中 与 属性 name 相同的 field
                 */
                Field field = null;
                Class tmpCls = cls;
                while (tmpCls != null && tmpCls != Object.class) {
                    try {
                        field = tmpCls.getDeclaredField(fieldName);
                        break;
                    } catch (NoSuchFieldException e) {
                        Log.w(TAG, "saveSystemInfo: " + e);
                    }

                    tmpCls = tmpCls.getSuperclass();
                }

                /**
                 * 如果没有找到，则 此属性 没有意义，不做处理
                 */
                if (field == null) {
                    Log.w(TAG, "loadProperties2Shared: " + fieldName + " mismatch in class " + cls.getSimpleName());
                    continue;
                }

                /**
                 * 如果获取的属性值是空，则也无需保存，没有意义
                 */
                String value = properties.getProperty(fieldName);
                if (TextUtils.isEmpty(value)) {
                    Log.w(TAG, "loadProperties2Shared: " + fieldName + " value is nothing");
                    continue;
                }

                String sharedKey = (cls.getName() + "." + field.getName()).toUpperCase();
                editor.putString(sharedKey, value);
                isNeedCommit = true;
            }

            if (isNeedCommit) {
                /**
                 * 如果 发现 对应的配置文件， 则 提交 存在路径，commit
                 */
                editor.putString((cls.getName() + ".path").toUpperCase(), configPath);
                editor.commit();

                /**
                 * 通过 应用 可以重新读取 数据配置了
                 */
                List<ISystem.OnChangeListener> listeners = getRegister().classOnChangeListenerMap.get(cls);
                if (listeners != null) {
                    for (ISystem.OnChangeListener listener : listeners) {
                        listener.onChange(null, cls);
                    }
                }
            }

            return true;

        } catch (Throwable e) {
            Log.e(TAG, "loadProperties2Shared: ", e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    Log.w(TAG, "loadProperties2Shared: " + e);
                }
            }
            properties.clear();
        }

        return false;
    }

    public final static class ProxyRegister {

        private USBReceiver usbReceiver;
        private boolean isAlreadyRegister;
        private Map<String, Class> configMap = new HashMap<>();
        private Map<Class, List<ISystem.OnChangeListener>> classOnChangeListenerMap = new HashMap<>();

        public ProxyRegister(USBReceiver usbReceiver) {
            this.usbReceiver = usbReceiver;
        }

        public ProxyRegister register(Context context) {

            if (!isAlreadyRegister) {
                isAlreadyRegister = true;

                IntentFilter filter = new IntentFilter();
                filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
                filter.addAction(Intent.ACTION_MEDIA_CHECKING);
                filter.addAction(Intent.ACTION_MEDIA_EJECT);
                filter.addAction(Intent.ACTION_MEDIA_REMOVED);
                // must
                filter.addDataScheme("file");

                context.registerReceiver(usbReceiver, filter);
            }

            return this;
        }

        public void addReceiveClass(Class cls, ISystem.OnChangeListener... onChangeListeners) {
            if (cls == null) {
                return;
            }
            configMap.put(cls.getName().toLowerCase() + ".config", cls);

            if (onChangeListeners != null) {
                List<ISystem.OnChangeListener> list = classOnChangeListenerMap.get(cls);
                if (list == null) {
                    list = new ArrayList<>();
                    classOnChangeListenerMap.put(cls, list);
                }
                list.addAll(Arrays.asList(onChangeListeners));
            }
        }

        public void unRegister(Context context) {
            try {
                context.unregisterReceiver(usbReceiver);
            } catch (Throwable e) {
                Log.e(TAG, "unRegisterReceiver: ", e);
            }
            isAlreadyRegister = false;

            configMap.clear();
        }
    }

    private final static class MountHandler extends Handler {

        private final USBReceiver receiver;

        public MountHandler(USBReceiver receiver) {
            super(Looper.getMainLooper());
            this.receiver = receiver;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            String usbDir = (String) msg.obj;
            usbDir = usbDir.substring("file://".length());
            Log.d(TAG, "handleMessage: mount path = " + usbDir);

            File file = new File(usbDir);
            if (!file.exists() || !file.isDirectory()) {
                Log.e(TAG, "handleMessage: " + usbDir + " isInvalid");
                return;
            }

            final Map<String, Class> configMap = getRegister().configMap;
            /**
             * 读取配置文件列表
             */
            String[] names = file.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    /**
                     * 文件名
                     * 1，是否 以 .config 结尾，
                     * 2，是否是已注册的 配置文件名字
                     */
                    return name.endsWith(".config") && configMap.containsKey(name);
                }
            });

            if (names == null) {
                Log.e(TAG, "handleMessage: names is null");
                return;
            }

            Log.d(TAG, "handleMessage: config size = " + names.length);

            for (String name : names) {
                receiver.loadProperties2Shared(usbDir, configMap.get(name));
            }
        }
    }
}
