package com.apical.factorytest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

import com.apical.factorytest.R;

public class AudioTest extends Activity {
    private static final String TAG = "AudioTest";
    private Button        mBtnNG;
    private Button        mBtnOK;
    private Button        mBtnPP;
    private TextView      mTxtInfo;
    private TextView      mHPStatus;
    private Intent        mNextTest;
    private MediaPlayer   mPlayer;
    private int           mHPTest;

    private static final int SAMPLE_RATE_RECORD = 44100;
    private static final int MSG_UPDATE_WAVEFORM_VIEW = 1;
    private AudioRecord    mRecorder;
    private int            mMinBufSizeR;
    private RecordThread   mRecThread;
    private WaveformView   mWaveformView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audiotest);
        TestReport.init(this);

        mNextTest = new Intent(this, WiFiTest.class);
        mBtnNG    = (Button) findViewById(R.id.btn_failed   );
        mBtnOK    = (Button) findViewById(R.id.btn_pass     );
        mBtnPP    = (Button) findViewById(R.id.btn_playpause);
        mBtnNG.setOnClickListener(mOnClickListener);
        mBtnOK.setOnClickListener(mOnClickListener);
        mBtnPP.setOnClickListener(mOnClickListener);

        mTxtInfo  = (TextView) findViewById(R.id.txt_audiotest);
        mHPStatus = (TextView) findViewById(R.id.txt_earphone );
        mTxtInfo .setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        mHPStatus.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        String hpdetstr = "earphone status: please plug-in earphone !";
        mHPStatus.setText(hpdetstr);
        mHPStatus.setTextColor(Color.RED);
        TestReport.set(TestReport.TEST_RESULT_HPDET, hpdetstr);

        mPlayer = MediaPlayer.create(this, R.raw.music);
        mPlayer.setLooping(true);
        mPlayer.seekTo(20000);
        
        mWaveformView = (WaveformView)findViewById(R.id.view_waveform);
        mMinBufSizeR  = AudioRecord.getMinBufferSize(SAMPLE_RATE_RECORD,
                            AudioFormat.CHANNEL_CONFIGURATION_MONO,
                            AudioFormat.ENCODING_PCM_16BIT);
        mRecorder     = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_RECORD,
                            AudioFormat.CHANNEL_CONFIGURATION_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            mMinBufSizeR * 2);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPlayer.start();
        mBtnPP.setText(R.string.btn_pause);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mHeadsetPlugReceiver, filter);

        mRecThread = new RecordThread();
        mRecThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPlayer.pause();

        unregisterReceiver(mHeadsetPlugReceiver);

        mRecThread.stopRecord();
        mRecThread = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayer.release();
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.btn_failed:
                finish();
                startActivity(mNextTest);
                TestReport.set(TestReport.TEST_RESULT_AUDIO, "ng");
                break;
            case R.id.btn_pass:
                finish();
                startActivity(mNextTest);
                TestReport.set(TestReport.TEST_RESULT_AUDIO, "pass");
                break;
            case R.id.btn_playpause:
                if (mPlayer.isPlaying()) {
                    mPlayer.pause();
                    mBtnPP.setText(R.string.btn_play);
                } else {
                    mPlayer.start();
                    mBtnPP.setText(R.string.btn_pause);
                }
                break;
            }
        }
    };

    private BroadcastReceiver mHeadsetPlugReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
                if (intent.getIntExtra("state", 0) == 1) {
                    mHPTest |= (1 << 0);
                } else {
 
                    mHPTest |= (1 << 1);
                }

                String hpdetstr = "";
                if ((mHPTest & 0x3) == 0x3) {
                    hpdetstr = "headset status: plug-in OK and plug-out OK.";
                    mHPStatus.setTextColor(Color.GREEN);
                } else if ((mHPTest & (1 << 0)) != 0) {
                    hpdetstr = "headset status: plug-in OK.";
                    mHPStatus.setTextColor(Color.YELLOW);
                } else if ((mHPTest & (1 << 1)) != 0) {
                    hpdetstr = "headset status: plug-out OK.";
                    mHPStatus.setTextColor(Color.YELLOW);
                }
                mHPStatus.setText(hpdetstr);
                TestReport.set(TestReport.TEST_RESULT_HPDET, hpdetstr);
            }
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_UPDATE_WAVEFORM_VIEW:
                mWaveformView.invalidate();
                break;
            }
        }
    };

    class RecordThread extends Thread
    {
        private boolean isRecording = false;

        @Override
        public void run() {
            isRecording = true;
            mRecorder.startRecording();

            while (isRecording) {
                int sampnum = mMinBufSizeR / 2;
                int offset  = 0;
                short[] buf = new short[sampnum];
                while (isRecording && offset != sampnum) {
                    offset += mRecorder.read(buf, offset, sampnum);
//                  Log.d(TAG, "read record data offset = " + offset);
                }
                mWaveformView.setAudioData(buf);
                Message msg = mHandler.obtainMessage();  
                msg.what = MSG_UPDATE_WAVEFORM_VIEW;  
                mHandler.sendMessage(msg);  
            }
            try {
                mRecorder.stop();
            } catch (Exception e) {}
        }

        public void stopRecord() {
            isRecording = false;
        }
    }

}

