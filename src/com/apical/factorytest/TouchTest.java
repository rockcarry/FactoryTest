package com.apical.factorytest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

import java.io.*;

public class TouchTest extends Activity {
    private final static String TAG = "TouchTest";
    private Button   mBtnNG;
    private Button   mBtnOK;
    private TextView mTxtInfo;
    private TextView mTxtSdcard;
    private TextView mTxtUsbOtg;
    private TextView mTxtBat;
    private TextView mTxtMap;
    private TextView mTxtResult;
    private Intent   mNextTest;
    private int      mSdcardDet;
    private int      mUsbOtgDet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.touchtest);
        TestReport.init(this);

        mNextTest  = new Intent(this, KeypadTest.class);
        mBtnNG     = (Button  ) findViewById(R.id.btn_failed );
        mBtnOK     = (Button  ) findViewById(R.id.btn_pass   );
        mBtnNG.setOnClickListener(mOnClickListener);
        mBtnOK.setOnClickListener(mOnClickListener);

        mTxtInfo   = (TextView) findViewById(R.id.txt_info   );
        mTxtSdcard = (TextView) findViewById(R.id.txt_sdcard );
        mTxtUsbOtg = (TextView) findViewById(R.id.txt_usbotg );
        mTxtBat    = (TextView) findViewById(R.id.txt_battery);
        mTxtMap    = (TextView) findViewById(R.id.txt_mapdata);
        mTxtResult = (TextView) findViewById(R.id.txt_test_result);
        mTxtInfo  .setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        mTxtSdcard.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        mTxtUsbOtg.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        mTxtBat   .setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        mTxtMap   .setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        mTxtResult.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);

        String sdstr = "";
        if (!Utils.isSdcardInsert()) {
            mTxtSdcard.setTextColor(Color.RED);
            sdstr = "sdcard status  : please plug-in sdcard !";
        } else {
            mTxtSdcard.setTextColor(Color.YELLOW);
            sdstr = "sdcard status  : plug-in OK.";
            mSdcardDet |= (1 << 0);
        }
        TestReport.set(TestReport.TEST_RESULT_SDCARD, sdstr);
        mTxtSdcard.setText(sdstr);

        String usbstr = "";
        mTxtUsbOtg.setTextColor(Color.RED);
        usbstr = "usbotg status  : please plug-in usb cable !";
        TestReport.set(TestReport.TEST_RESULT_USBOTG, usbstr);
        mTxtUsbOtg.setText(usbstr);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Settings.System.putInt(getContentResolver(), Settings.System.SHOW_TOUCHES    , 1);
        Settings.System.putInt(getContentResolver(), Settings.System.POINTER_LOCATION, 1);

        mTxtInfo  .setText(TestReport.generateReport(this, TestReport.TEST_REPORT_DEVICE_INFO));
        mTxtResult.setText(TestReport.generateReport(this, TestReport.TEST_REPORT_HARDWARE_INFO));

        String  MAP_PATH= "/mnt/sdcard/.Navi";
        File    file    = new File(MAP_PATH);
        boolean mapok   = file.exists();
        String  mapstr  = "map data       : " + (mapok ? "OK" : "NG") + "\n";
        mTxtMap.setText(mapstr);
        mTxtMap.setTextColor(mapok ? Color.GREEN : Color.RED);
        TestReport.set(TestReport.TEST_RESULT_MAPDATA, mapstr);

        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter1.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter1.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter1.addAction("android.hardware.usb.action.USB_STATE");
        registerReceiver(mBatteryReceiver, filter1);

        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(Intent.ACTION_MEDIA_EJECT  );
        filter2.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter2.addDataScheme("file");
        registerReceiver(mMediaChangeReceiver, filter2);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Settings.System.putInt(getContentResolver(), Settings.System.SHOW_TOUCHES    , 0);
        Settings.System.putInt(getContentResolver(), Settings.System.POINTER_LOCATION, 0);

        unregisterReceiver(mBatteryReceiver);
        unregisterReceiver(mMediaChangeReceiver);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.btn_failed:
                finish();
                startActivity(mNextTest);
                TestReport.set(TestReport.TEST_RESULT_TOUCH, "ng");
                break;
            case R.id.btn_pass:
                finish();
                startActivity(mNextTest);
                TestReport.set(TestReport.TEST_RESULT_TOUCH, "pass");
                break;
            }
        }
    };

    private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                int     status      = intent.getIntExtra("status", 0);
                int     health      = intent.getIntExtra("health", 0);
                boolean present     = intent.getBooleanExtra("present", false);
                int     level       = intent.getIntExtra("level", 0);
                int     scale       = intent.getIntExtra("scale", 0);
                int     icon_small  = intent.getIntExtra("icon-small", 0);
                int     plugged     = intent.getIntExtra("plugged", 0);
                int     voltage     = intent.getIntExtra("voltage", 0);
                int     temperature = intent.getIntExtra("temperature", 0);
                String  technology  = intent.getStringExtra("technology");

                String statusString = "";
                switch (status) {
                case BatteryManager.BATTERY_STATUS_UNKNOWN:
                    statusString = "unknown";
                    break;
                case BatteryManager.BATTERY_STATUS_CHARGING:
                    statusString = "charging";
                    break;
                case BatteryManager.BATTERY_STATUS_DISCHARGING:
                    statusString = "discharging";
                    break;
                case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                    statusString = "not charging";
                    break;
                case BatteryManager.BATTERY_STATUS_FULL:
                    statusString = "full";
                    break;
                }

                String acString = "none";
                switch (plugged) {
                case BatteryManager.BATTERY_PLUGGED_AC:
                    acString = "ac";
                    break;
                case BatteryManager.BATTERY_PLUGGED_USB:
                    acString = "usb";
                    break;
                case 3:
                    acString = "usb-charger";
                    break;
                }

                String s;
                if (present) {
                    s = "battery status : "
                      + statusString + " "
                      + String.valueOf(level) + "% "
                      + acString + " "
                      + String.valueOf(voltage) + "V "
                      + String.valueOf(temperature/10.0) + "'C";
                } else {
                    s = "not present";
                }
                mTxtBat.setText(s);
                if (!present || temperature > 550) {
                    mTxtBat.setTextColor(Color.RED);
                } else {
                    mTxtBat.setTextColor(Color.GREEN);
                }
                TestReport.set(TestReport.TEST_RESULT_BATTERY, s);
            } else if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
//              mUsbOtgDet |= (1 << 0);
            } else if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
//              mUsbOtgDet |= (1 << 1);
            } else if (action.equals("android.hardware.usb.action.USB_STATE")) {
                if (intent.getExtras().getBoolean("connected")) {
                    mUsbOtgDet |= (1 << 0);
                } else {
                    mUsbOtgDet |= (1 << 1);
                }
            }

            String usbstr = "usbotg status  : please plug-in usb cable !";
            if ((mUsbOtgDet & 0x3) == 0x3) {
                usbstr = "usbotg status  : plug-in OK and plug-out OK.";
                mTxtUsbOtg.setTextColor(Color.GREEN);
            } else if ((mUsbOtgDet & (1 << 0)) != 0) {
                usbstr = "usbotg status  : plug-in OK.";
                mTxtUsbOtg.setTextColor(Color.YELLOW);
            } else if ((mUsbOtgDet & (1 << 1)) != 0) {
                usbstr = "usbotg status  : plug-out OK.";
                mTxtUsbOtg.setTextColor(Color.YELLOW);
            }
            mTxtUsbOtg.setText(usbstr);
            TestReport.set(TestReport.TEST_RESULT_USBOTG, usbstr);
        }
    };

    private BroadcastReceiver mMediaChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            final Uri uri = intent.getData();
            String   path = uri.getPath();

            if (  action.equals(Intent.ACTION_MEDIA_EJECT)
               || action.equals(Intent.ACTION_MEDIA_UNMOUNTED) ) {
                Log.i(TAG, "Intent.ACTION_MEDIA_EJECT path = " + path);
                if (path.contains("extsd")) {
                    Log.i(TAG, "sdcard removed");
                    mSdcardDet |= (1 << 1);
                }
            } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                Log.i(TAG, "Intent.ACTION_MEDIA_MOUNTED = " + path);
                if (path.contains("extsd")) {
                    Log.i(TAG, "sdcard inserted");
                    mSdcardDet |= (1 << 0);
                }
            }

            String sdstr = "";
            if ((mSdcardDet & 0x3) == 0x3) {
                sdstr = "sdcard status  : plug-in OK and plug-out OK.";
                mTxtSdcard.setTextColor(Color.GREEN);
            } else if ((mSdcardDet & (1 << 0)) != 0) {
                sdstr = "sdcard status  : plug-in OK.";
                mTxtSdcard.setTextColor(Color.YELLOW);
            } else if ((mSdcardDet & (1 << 1)) != 0) {
                sdstr = "sdcard status  : plug-out OK.";
                mTxtSdcard.setTextColor(Color.YELLOW);
            }
            mTxtSdcard.setText(sdstr);
            TestReport.set(TestReport.TEST_RESULT_SDCARD, sdstr);
        }
    };

    public static long getFolderSize(File file) {
        long size = 0;
        try {
            java.io.File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    size += getFolderSize(fileList[i]);
                } else {
                    size += fileList[i].length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }
}

