package com.apical.factorytest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;

import java.io.*;
import java.util.List;

public class GpsTest extends Activity {
    private final static String TAG = "GpsTest";
    private Button           mBtnNG;
    private Button           mBtnOK;
    private Button           mBtnGPSTest;
    private Button           mBtnDoneWipe;
    private EditText         mTxtInfo;
    private Intent           mNextTest;
    private LocationManager  mLocManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gpstest);
        TestReport.init(this);

        mNextTest   = new Intent(this, BluetoothTest.class);
        mBtnNG      = (Button) findViewById(R.id.btn_failed  );
        mBtnOK      = (Button) findViewById(R.id.btn_done    );
        mBtnGPSTest = (Button) findViewById(R.id.btn_gpstest );
        mBtnDoneWipe= (Button) findViewById(R.id.btn_donewipe);
        mBtnNG      .setOnClickListener(mOnClickListener);
        mBtnOK      .setOnClickListener(mOnClickListener);
        mBtnGPSTest .setOnClickListener(mOnClickListener);
        mBtnDoneWipe.setOnClickListener(mOnClickListener);

        mTxtInfo = (EditText) findViewById(R.id.txt_gpsinfo);
        mTxtInfo.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        mTxtInfo.setText("no gps data !\n");
        mTxtInfo.setTextColor(Color.RED);

        mLocManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mLocManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 500, 1, mLocationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocManager.addNmeaListener(mNmeaListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocManager.removeNmeaListener(mNmeaListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocManager.removeUpdates(mLocationListener);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.btn_failed:
//              startActivity(mNextTest);
                TestReport.set(TestReport.TEST_RESULT_GPS, "ng");
                if (saveTestReport()) {
                    finish();
                } else {
                    showSaveReportFailedDialog();
                }
                break;
            case R.id.btn_done:
//              startActivity(mNextTest);
                TestReport.set(TestReport.TEST_RESULT_GPS, "pass");
                if (saveTestReport()) {
                    finish();
                } else {
                    showSaveReportFailedDialog();
                }
                break;
            case R.id.btn_gpstest: {
                    Intent i = new Intent();
                    ComponentName c = new ComponentName("com.chartcross.gpstest", "com.chartcross.gpstest.GPSTest");
                    i.setComponent(c);
                    startActivity(i);
                }
                break;
            case R.id.btn_donewipe:
                if (saveTestReport()) {
                    sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
                } else {
                    showSaveReportFailedDialog();
                }
                break;
            }
        }
    };

    private boolean saveTestReport() {
        if (!Utils.isSdcardInsert()) {
            Log.d(TAG, "sdcard is not inserted !");
            return false;
        }

        File file = new File(Utils.SDCARD_ROOT + "/TestReport/");
        file.mkdirs();

        FileWriter fw = null;
        try {
            fw = new FileWriter(Utils.SDCARD_ROOT + "/TestReport/" + TestReport.generateReport(this, TestReport.TEST_REPORT_SERIAL_NUMBER) + ".txt");
            fw.write(TestReport.generateReport(this, TestReport.TEST_REPORT_ALL));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (fw != null) try { fw.close(); } catch (Exception e) { e.printStackTrace(); }
        }
        return true;
    }

    private void showSaveReportFailedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Warning")
                .setMessage("Failed to save test report into sdcard !\n\nPlease make sure the sdcard is inserted !\n\nIf inserted, maybe it's the sdcard or slot hardware issue !\n\nPress FINISH to finish test.")
                .setPositiveButton("FINISH",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            finish();
                        }
                    } )
                .setNegativeButton("CANCEL" , null)
                .show();
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
        }
        @Override
        public void onProviderDisabled(String arg0) {
        }
        @Override
        public void onProviderEnabled(String arg0) {
        }
        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        }
    };

    private GpsStatus.NmeaListener mNmeaListener = new GpsStatus.NmeaListener() {
        @Override
        public void onNmeaReceived(long timestamp, String nmea) {
            mTxtInfo.setTextColor(Color.GREEN);
            int len = mTxtInfo.getText().length();
            if (len > 0x10000) {
                mTxtInfo.getText().delete(0, 0x10000 / 2);
            }
            mTxtInfo.getText().append(nmea);
            mTxtInfo.setSelection(mTxtInfo.getText().length(), mTxtInfo.getText().length());
        }
    };
}

