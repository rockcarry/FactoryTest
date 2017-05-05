package com.apical.factorytest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;

import java.util.List;

public class WiFiTest extends Activity {
    private Button           mBtnNG;
    private Button           mBtnOK;
    private EditText         mTxtInfo;
    private Intent           mNextTest;
    private WifiManager      mWifiManager;
    private WifiInfo         mWifiInfo;
    private List<ScanResult> mWifiApList;

    private static final int MSG_WIFI_SCAN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifitest);
        TestReport.init(this);

        mNextTest = new Intent(this, BluetoothTest.class);
        mBtnNG    = (Button) findViewById(R.id.btn_failed   );
        mBtnOK    = (Button) findViewById(R.id.btn_pass     );
        mBtnNG.setOnClickListener(mOnClickListener);
        mBtnOK.setOnClickListener(mOnClickListener);

        mTxtInfo = (EditText) findViewById(R.id.txt_wifiinfo);
        mTxtInfo.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);

        mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        mWifiInfo    = mWifiManager.getConnectionInfo();
        mWifiManager.startScan();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.sendEmptyMessage(MSG_WIFI_SCAN);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeMessages(MSG_WIFI_SCAN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.btn_failed:
                finish();
                startActivity(mNextTest);
                TestReport.set(TestReport.TEST_RESULT_WIFI, "ng");
                break;
            case R.id.btn_pass:
                finish();
                startActivity(mNextTest);
                TestReport.set(TestReport.TEST_RESULT_WIFI, "pass");
                break;
            }
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_WIFI_SCAN: {
                    mHandler.sendEmptyMessageDelayed(MSG_WIFI_SCAN, 2000);
                    mWifiApList = mWifiManager.getScanResults();
                    String info = "wifi mac: " + mWifiInfo.getMacAddress() + "\n"
                                + "-------------------------------------------\n";
                    for (ScanResult sr : mWifiApList) {
                        info += String.format("%s, %3ddBm, %4dMHz, %s\n", sr.BSSID, sr.level, sr.frequency, sr.SSID);
                    }
                    if (mWifiApList.size() == 0) {
                        info += "no ap found !\n";
                        mTxtInfo.setTextColor(Color.RED);
                    } else {
                        mTxtInfo.setTextColor(Color.GREEN);
                    }
                    mTxtInfo.setText(info);
                }
                break;
            }
        }
    };
}

