package com.apical.factorytest;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

public class LcdTest extends Activity {
    private Button   mBtnNG;
    private Button   mBtnOK;
    private Button   mBtnDec;
    private Button   mBtnInc;
    private View     mRGBView;
    private View     mBlkPanel;
    private TextView mBlkCur;
    private Intent   mNextTest;
    private int      mCurStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lcdtest);
        TestReport.init(this);

        mNextTest  = new Intent(this, GsensorTest.class);
        mBtnNG     = (Button  ) findViewById(R.id.btn_failed );
        mBtnOK     = (Button  ) findViewById(R.id.btn_pass   );
        mBtnDec    = (Button  ) findViewById(R.id.btn_bkl_dec);
        mBtnInc    = (Button  ) findViewById(R.id.btn_bkl_inc);
        mRGBView   = (View    ) findViewById(R.id.view_rgb   );
        mBlkPanel  = (View    ) findViewById(R.id.panel_blk  );
        mBlkCur    = (TextView) findViewById(R.id.txt_blk_cur);
        mRGBView.setBackgroundColor(Color.WHITE);
        mBtnNG  .setOnClickListener(mOnClickListener);
        mBtnOK  .setOnClickListener(mOnClickListener);
        mBtnDec .setOnClickListener(mOnClickListener);
        mBtnInc .setOnClickListener(mOnClickListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBlkCur.setText("" + getScreenBrightness());
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int val;

            if (v.getId() == R.id.btn_failed || v.getId() == R.id.btn_pass) {
                mCurStep++;
            }
            if (mCurStep == 1) {
                mBlkPanel.setVisibility(View.GONE);
            }

            switch (mCurStep) {
            case 1: mRGBView.setBackgroundColor(Color.RED  ); break;
            case 2: mRGBView.setBackgroundColor(Color.GREEN); break;
            case 3: mRGBView.setBackgroundColor(Color.BLUE ); break;
            case 4: mRGBView.setBackgroundDrawable(getResources().getDrawable(R.drawable.colorbar)); break;
            case 5: finish(); startActivity(mNextTest);
            }

            switch (v.getId()) {
            case R.id.btn_failed:
                switch (mCurStep) {
                case 1: TestReport.set(TestReport.TEST_RESULT_BACKLIGHT, "ng"); break;
                case 2: TestReport.set(TestReport.TEST_RESULT_LCD_R    , "ng"); break;
                case 3: TestReport.set(TestReport.TEST_RESULT_LCD_G    , "ng"); break;
                case 4: TestReport.set(TestReport.TEST_RESULT_LCD_B    , "ng"); break;
                case 5: TestReport.set(TestReport.TEST_RESULT_LCD_C    , "ng"); break;
                }
                break;
            case R.id.btn_pass:
                switch (mCurStep) {
                case 1: TestReport.set(TestReport.TEST_RESULT_BACKLIGHT, "pass"); break;
                case 2: TestReport.set(TestReport.TEST_RESULT_LCD_R    , "pass"); break;
                case 3: TestReport.set(TestReport.TEST_RESULT_LCD_G    , "pass"); break;
                case 4: TestReport.set(TestReport.TEST_RESULT_LCD_B    , "pass"); break;
                case 5: TestReport.set(TestReport.TEST_RESULT_LCD_C    , "pass"); break;
                }
                break;
            case R.id.btn_bkl_dec:
                val = getScreenBrightness();
                val-= 10;
                val = val > 0 ? val : 0;
                setScreenBrightness(val);
                mBlkCur.setText("" + getScreenBrightness());
                break;
            case R.id.btn_bkl_inc:
                val = getScreenBrightness();
                val+= 10;
                val = val < 255 ? val : 255;
                setScreenBrightness(val);
                mBlkCur.setText("" + getScreenBrightness());
                break;
            }
        }
    };

    public int getScreenBrightness() {
        int value = 0;
        try {
            value = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {}
        return value;
    }

    public void setScreenBrightness(int value) {
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, value);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.screenBrightness = value / 255f;
        getWindow().setAttributes(params);
    }
}

