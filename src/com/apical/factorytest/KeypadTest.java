package com.apical.factorytest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

public class KeypadTest extends Activity {
    private TextView mKeyHome;
    private TextView mKeyPower;
    private TextView mKeyVolDec;
    private TextView mKeyVolInc;
    private Button   mBtnNG;
    private Button   mBtnOK;
    private Intent   mNextTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.keypad);
        mNextTest = new Intent(this, LcdTest.class);
        TestReport.init(this);

        mKeyHome   = (TextView) findViewById(R.id.key_home   );
        mKeyPower  = (TextView) findViewById(R.id.key_power  );
        mKeyVolDec = (TextView) findViewById(R.id.key_vol_dec);
        mKeyVolInc = (TextView) findViewById(R.id.key_vol_inc);
        mBtnNG     = (Button  ) findViewById(R.id.btn_failed );
        mBtnOK     = (Button  ) findViewById(R.id.btn_pass   );
        mBtnNG.setOnClickListener(mOnClickListener);
        mBtnOK.setOnClickListener(mOnClickListener);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_HOME:
            mKeyHome.setBackgroundColor(Color.YELLOW);
            break;

        case KeyEvent.KEYCODE_POWER:
            mKeyPower.setBackgroundColor(Color.YELLOW);
            break;

        case KeyEvent.KEYCODE_VOLUME_DOWN:
            mKeyVolDec.setBackgroundColor(Color.YELLOW);
            return true;

        case KeyEvent.KEYCODE_VOLUME_UP:
            mKeyVolInc.setBackgroundColor(Color.YELLOW);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_HOME:
            mKeyHome.setBackgroundColor(Color.GREEN);
            break;

        case KeyEvent.KEYCODE_POWER:
            mKeyPower.setBackgroundColor(Color.GREEN);
            break;

        case KeyEvent.KEYCODE_VOLUME_DOWN:
            mKeyVolDec.setBackgroundColor(Color.GREEN);
            return true;

        case KeyEvent.KEYCODE_VOLUME_UP:
            mKeyVolInc.setBackgroundColor(Color.GREEN);
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.btn_failed:
                finish();
                startActivity(mNextTest);
                TestReport.set(TestReport.TEST_RESULT_KEYPAD, "ng");
                break;
            case R.id.btn_pass:
                finish();
                startActivity(mNextTest);
                TestReport.set(TestReport.TEST_RESULT_KEYPAD, "pass");
                break;
            }
        }
    };
}

