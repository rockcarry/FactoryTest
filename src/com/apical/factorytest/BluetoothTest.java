package com.apical.factorytest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Log;


public class BluetoothTest extends Activity {
    private Button           mBtnNG;
    private Button           mBtnOK;
    private Button           mBtnScan;
    private TextView         mTxtStatus;
    private EditText         mTxtList;
    private Intent           mNextTest;
    private BluetoothAdapter mBtAdapter;
    private int              mScanProgress;

    private static final int MSG_BT_SCAN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth);
        TestReport.init(this);

        mNextTest = new Intent(this, GpsTest.class );
        mBtnNG    = (Button) findViewById(R.id.btn_failed);
        mBtnOK    = (Button) findViewById(R.id.btn_pass  );
        mBtnScan  = (Button) findViewById(R.id.btn_scan  );
        mBtnNG  .setOnClickListener(mOnClickListener);
        mBtnOK  .setOnClickListener(mOnClickListener);
        mBtnScan.setOnClickListener(mOnClickListener);

        mTxtStatus = (TextView) findViewById(R.id.txt_status);
        mTxtStatus.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);

        mTxtList = (EditText) findViewById(R.id.txt_devlist);
        mTxtList.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter != null) {
            mBtAdapter.startDiscovery();
            mTxtStatus.setText("bt adapter ok !");
            mTxtStatus.setTextColor(Color.GREEN);
            mHandler.sendEmptyMessageDelayed(MSG_BT_SCAN, 2000);
        } else {
            mTxtStatus.setText("didn't find the bt adapter !");
            mTxtStatus.setTextColor(Color.RED);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.btn_failed:
                finish();
                startActivity(mNextTest);
                TestReport.set(TestReport.TEST_RESULT_BT, "ng");
                break;
            case R.id.btn_pass:
                finish();
                startActivity(mNextTest);
                TestReport.set(TestReport.TEST_RESULT_BT, "pass");
                break;
            case R.id.btn_scan:
                if (mBtAdapter != null && !mBtAdapter.isDiscovering()) {
                    mHandler.removeMessages  (MSG_BT_SCAN);
                    mHandler.sendEmptyMessage(MSG_BT_SCAN);
                    mTxtList  .setText("");
                    mBtAdapter.startDiscovery();
                }
                break;
            }
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_BT_SCAN: {
                    mHandler.sendEmptyMessageDelayed(MSG_BT_SCAN, 100);
                    mScanProgress++;
                    mScanProgress %= 4;
                    String t[] = { "|", "/", "-", "\\" };
                    mTxtStatus.setTextColor(Color.YELLOW);
                    mTxtStatus.setText("scanning... " + t[mScanProgress]);
                }
                break;
            }
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String devstr = String.format("%s - %s\n", device.getAddress(), device.getName());
                mTxtList.getText().append(devstr);
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                mHandler.removeMessages(MSG_BT_SCAN);
                mTxtStatus.setText("scan finish !");
            }
        }
    };
}

