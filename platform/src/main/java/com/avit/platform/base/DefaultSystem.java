package com.avit.platform.base;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * 此处仅作为 以 消息系统为例
 */
public class DefaultSystem extends AbstractSystem {

    protected final static String TAG = "DefaultSystem";

    @Override
    public boolean isInit() {
        return true;
    }

    @Override
    protected ISystem onCheck(Callback callback) {

        String sn = Build.SERIAL;

        Class cls = systemInfoClass;
        try {
            systemInfo = cls.newInstance();
        } catch (Throwable e) {
            Log.e(TAG, "onCheck: ", e);
            return this;
        }

        String client;
        if (!TextUtils.isEmpty(sn) && !sn.equalsIgnoreCase("unknown")) {
            client = sn;
        } else {
            String mac = getMACAddress().trim().toUpperCase();
            Log.w(TAG, "onCheck: ignore SERIAL, use mac = " + mac);
            client = mac.replace(":", "-");
        }
        saveSystemInfo("client", client);
        saveSystemInfo("deviceId", client);

        Log.d(TAG, "onCheck: serial = " + client);
//        saveSystemInfo("mqttLvsAddr", BuildConfig.MQTT_LVS_ADDR);

        callback.onCallback(systemInfo);
        return this;
    }

    @Override
    protected Object onFetchValue(Field field) {
        throw new IllegalStateException(DefaultSystem.class.getName() + " never call this function");
    }

    public final void saveSystemInfo(String fieldName, String value){
        Class cls = systemInfoClass;

        Field field = null;
        while (cls != null && cls != Object.class) {
            try {
                field = cls.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(systemInfo, value);
                break;
            } catch (Throwable e) {
                Log.w(TAG, "saveSystemInfo: " + e);
            }

            cls = cls.getSuperclass();
        }
    }


    public static String getMACAddress() {
        String mac = "";

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mac = getMac0();
        } else {
            mac = getMac1();
        }
        return mac;
    }

    private static String getMac0() {
        String str = "";
        String macSerial = "";

        try {
            macSerial = loadFileAsString("/sys/class/net/eth0/address")
                    .toUpperCase().substring(0, 17);
        } catch (Exception e) {
            e.printStackTrace();

        }

        if (TextUtils.isEmpty(macSerial)) {

            Log.w(TAG, "getMac0: use wlan0 mac");

            try {
                Process pp = Runtime.getRuntime().exec(
                        "cat /sys/class/net/wlan0/address ");
                InputStreamReader ir = new InputStreamReader(pp.getInputStream());
                LineNumberReader input = new LineNumberReader(ir);

                for (; null != str; ) {
                    str = input.readLine();
                    if (str != null) {
                        macSerial = str.trim();// 去空格
                        break;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return macSerial;
    }

    public static String loadFileAsString(String fileName) throws Exception {
        FileReader reader = new FileReader(fileName);
        String text = loadReaderAsString(reader);
        reader.close();
        return text;
    }

    public static String loadReaderAsString(Reader reader) throws Exception {
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[4096];
        int readLength = reader.read(buffer);
        while (readLength >= 0) {
            builder.append(buffer, 0, readLength);
            readLength = reader.read(buffer);
        }
        return builder.toString();
    }

    /**
     * 根据IP地址获取MAC地址
     *
     * @return
     */
    private static String getMac1() {
        String strMacAddr = null;
        try {
            //获得IpD地址
            InetAddress ip = getLocalInetAddress();
            byte[] b = NetworkInterface.getByInetAddress(ip).getHardwareAddress();
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < b.length; i++) {
                if (i != 0) {
                    buffer.append(':');
                }
                String str = Integer.toHexString(b[i] & 0xFF);
                buffer.append(str.length() == 1 ? 0 + str : str);
            }
            strMacAddr = buffer.toString().toLowerCase();
        } catch (Exception e) {

        }

        return strMacAddr;
    }

    private static InetAddress getLocalInetAddress() {
        InetAddress ip = null;
        try {
            //列举
            Enumeration<NetworkInterface> en_netInterface = NetworkInterface.getNetworkInterfaces();
            while (en_netInterface.hasMoreElements()) {//是否还有元素
                NetworkInterface ni = (NetworkInterface) en_netInterface.nextElement();//得到下一个元素
                Enumeration<InetAddress> en_ip = ni.getInetAddresses();//得到一个ip地址的列举
                while (en_ip.hasMoreElements()) {
                    ip = en_ip.nextElement();
                    if (!ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1) {
                        break;
                    } else {
                        ip = null;
                    }
                }

                if (ip != null) {
                    break;
                }
            }
        } catch (SocketException e) {

            e.printStackTrace();
        }
        return ip;
    }
}
