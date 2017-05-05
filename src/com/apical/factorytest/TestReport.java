package com.apical.factorytest;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.SystemProperties;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestReport {
    public static final String TEST_RESULT_TOUCH     = "TEST_RESULT_TOUCH";
    public static final String TEST_RESULT_SDCARD    = "TEST_RESULT_SDCARD";
    public static final String TEST_RESULT_USBOTG    = "TEST_RESULT_USBOTG";
    public static final String TEST_RESULT_BATTERY   = "TEST_RESULT_BATTERY";
    public static final String TEST_RESULT_MAPDATA   = "TEST_RESULT_MAPDATA";
    public static final String TEST_RESULT_KEYPAD    = "TEST_RESULT_KEYPAD";
    public static final String TEST_RESULT_BACKLIGHT = "TEST_RESULT_BACKLIGHT";
    public static final String TEST_RESULT_LCD_R     = "TEST_RESULT_LCD_R";
    public static final String TEST_RESULT_LCD_G     = "TEST_RESULT_LCD_G";
    public static final String TEST_RESULT_LCD_B     = "TEST_RESULT_LCD_B";
    public static final String TEST_RESULT_LCD_C     = "TEST_RESULT_LCD_C";
    public static final String TEST_RESULT_GSENSOR   = "TEST_RESULT_GSENSOR";
    public static final String TEST_RESULT_AUDIO     = "TEST_RESULT_AUDIO";
    public static final String TEST_RESULT_HPDET     = "TEST_RESULT_HPDET";
    public static final String TEST_RESULT_WIFI      = "TEST_RESULT_WIFI";
    public static final String TEST_RESULT_BT        = "TEST_RESULT_BT";
    public static final String TEST_RESULT_GPS       = "TEST_RESULT_GPS";

    public static final int TEST_REPORT_ALL            = 0;
    public static final int TEST_REPORT_SERIAL_NUMBER  = 1;
    public static final int TEST_REPORT_DEVICE_INFO    = 2;
    public static final int TEST_REPORT_HARDWARE_INFO  = 3;

    private static final String FACTORY_TEST_SHARED_PREFS = "FACTORY_TEST_SHARED_PREFS";
    private static Context           mContext;
    private static SharedPreferences mSharedPref;

    public static void init(Context c) {
        mContext    = c;
        mSharedPref = mContext.getSharedPreferences(FACTORY_TEST_SHARED_PREFS, Context.MODE_PRIVATE);
    }

    public static void set(String key, String value) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String get(String key) {
        return mSharedPref.getString(key, "-");
    }
    
    public static String generateReport(Context context, int type) {
        switch (type) {
        case TEST_REPORT_SERIAL_NUMBER:
            return SystemProperties.get("vold.apk.uuid", Build.SERIAL);
        case TEST_REPORT_DEVICE_INFO: {
                WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
                wm.setWifiEnabled(true);
                WifiInfo    wi = wm.getConnectionInfo();
                String devinfo = "model name     : " + Build.MODEL                 + "\n"
                               + "android version: " + Build.VERSION.RELEASE       + "\n"
                               + "kernel version : " + getFormattedKernelVersion() + "\n"
                               + "build number   : " + Build.DISPLAY               + "\n"
                               + "serial number  : " + SystemProperties.get("vold.apk.uuid", Build.SERIAL) + "\n"
                               + "wifi mac       : " + wi.getMacAddress()          + "\n";
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (adapter != null) {
                    adapter.enable();
                    devinfo += "bt mac         : " + adapter.getAddress() + "\n";
                }
                return devinfo;
            }
        case TEST_REPORT_HARDWARE_INFO: {
                String info = "touch key  blk  R    G    B    color gsensor audio hpdet wifi bt   gps\n"
                            + String.format("%-5s %-4s %-4s %-4s %-4s %-4s %-5s %-7s %-5s %-5s %-4s %-4s %-4s",
                                get(TEST_RESULT_TOUCH    ),
                                get(TEST_RESULT_KEYPAD   ),
                                get(TEST_RESULT_BACKLIGHT),
                                get(TEST_RESULT_LCD_R    ),
                                get(TEST_RESULT_LCD_G    ),
                                get(TEST_RESULT_LCD_B    ),
                                get(TEST_RESULT_LCD_C    ),
                                get(TEST_RESULT_GSENSOR  ),
                                get(TEST_RESULT_AUDIO    ),
                                get(TEST_RESULT_HPDET).contains("plug-in OK and plug-out OK") ? "pass" : "ng",
                                get(TEST_RESULT_WIFI     ),
                                get(TEST_RESULT_BT       ),
                                get(TEST_RESULT_GPS      ) );
                return info;
            }
        case TEST_REPORT_ALL: {
                Date date = new Date(System.currentTimeMillis());
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String all = generateReport(context, TEST_REPORT_DEVICE_INFO) + "\n"
                           + get(TEST_RESULT_SDCARD ) + "\n"
                           + get(TEST_RESULT_USBOTG ) + "\n"
                           + get(TEST_RESULT_BATTERY) + "\n"
                           + get(TEST_RESULT_MAPDATA) + "\n"
                           + generateReport(context, TEST_REPORT_HARDWARE_INFO) + "\n\n"
                           + df.format(date)  + "\n";
                return all;
            }
        }
        return "";
    }




    private static final String FILENAME_PROC_VERSION = "/proc/version";
    private static final String LOG_TAG = "TouchTest";

    /**
     * Reads a line from the specified file.
     * @param filename the file to read from
     * @return the first line, if any.
     * @throws IOException if the file couldn't be read
     */
    private static String readLine(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename), 256);
        try {
            return reader.readLine();
        } finally {
            reader.close();
        }
    }

    public static String getFormattedKernelVersion() {
        try {
            return formatKernelVersion(readLine(FILENAME_PROC_VERSION));

        } catch (IOException e) {
            Log.e(LOG_TAG,
                "IO Exception when getting kernel version for Device Info screen",
                e);

            return "Unavailable";
        }
    }

    public static String formatKernelVersion(String rawKernelVersion) {
        // Example (see tests for more):
        // Linux version 3.0.31-g6fb96c9 (android-build@xxx.xxx.xxx.xxx.com) \
        //     (gcc version 4.6.x-xxx 20120106 (prerelease) (GCC) ) #1 SMP PREEMPT \
        //     Thu Jun 28 11:02:39 PDT 2012

        final String PROC_VERSION_REGEX =
            "Linux version (\\S+) " + /* group 1: "3.0.31-g6fb96c9" */
            "\\((\\S+?)\\) " +        /* group 2: "x@y.com" (kernel builder) */
            "(?:\\(gcc.+? \\)) " +    /* ignore: GCC version information */
            "(#\\d+) " +              /* group 3: "#1" */
            "(?:.*?)?" +              /* ignore: optional SMP, PREEMPT, and any CONFIG_FLAGS */
            "((Sun|Mon|Tue|Wed|Thu|Fri|Sat).+)"; /* group 4: "Thu Jun 28 11:02:39 PDT 2012" */

        Matcher m = Pattern.compile(PROC_VERSION_REGEX).matcher(rawKernelVersion);
        if (!m.matches()) {
            Log.e(LOG_TAG, "Regex did not match on /proc/version: " + rawKernelVersion);
            return "Unavailable";
        } else if (m.groupCount() < 4) {
            Log.e(LOG_TAG, "Regex match on /proc/version only returned " + m.groupCount()
                    + " groups");
            return "Unavailable";
        }
        /*
        return m.group(1) + "\n" +                 // 3.0.31-g6fb96c9
            m.group(2) + " " + m.group(3) + "\n" + // x@y.com #1
            m.group(4);                            // Thu Jun 28 11:02:39 PDT 2012
        */
        return m.group(1) + " " + m.group(4);
    }
};

