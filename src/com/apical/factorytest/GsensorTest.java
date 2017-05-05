package com.apical.factorytest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.util.Log;

public class GsensorTest extends Activity {
    private Button        mBtnNG;
    private Button        mBtnOK;
    private Intent        mNextTest;
    private GsensorView   mGsensorView;
    private SensorManager mSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gsensortest);
        TestReport.init(this);

        mNextTest = new Intent(this, AudioTest.class);
        mBtnNG    = (Button) findViewById(R.id.btn_failed );
        mBtnOK    = (Button) findViewById(R.id.btn_pass   );
        mBtnNG.setOnClickListener(mOnClickListener);
        mBtnOK.setOnClickListener(mOnClickListener);

        mGsensorView   = (GsensorView) findViewById(R.id.view_gsensor);
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mGSensorListener,
            mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mGSensorListener);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.btn_failed:
                finish();
                startActivity(mNextTest);
                TestReport.set(TestReport.TEST_RESULT_GSENSOR, "ng");
                break;
            case R.id.btn_pass:
                finish();
                startActivity(mNextTest);
                TestReport.set(TestReport.TEST_RESULT_GSENSOR, "pass");
                break;
            }
        }
    };

    private SensorEventListener mGSensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];
                mGsensorView.setXYZ(x, y, z);
                mGsensorView.invalidate();
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };
}

