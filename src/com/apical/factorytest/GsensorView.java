package com.apical.factorytest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;
import android.util.AttributeSet;

public class GsensorView extends View {
    private float mX;
    private float mY;
    private float mZ;

    public GsensorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.rgb(255, 255, 255));
        paint.setStrokeWidth(1f);

        float w = canvas.getWidth();
        float h = canvas.getHeight();
        float x = (float)(w - (w * (mX + 9.8) / 19.6));
        float y = (float)(0 + (h * (mY + 9.8) / 19.6));
        float r = (float)(19.6 - mZ);

        canvas.drawLine(0, h / 2, w, h / 2, paint);
        canvas.drawLine(w / 2, 0, w / 2, h, paint);
        canvas.drawCircle(x, y, r, paint);
    }
    
    public void setXYZ(float x, float y, float z) {
        mX = x;
        mY = y;
        mZ = z;
    }
}

