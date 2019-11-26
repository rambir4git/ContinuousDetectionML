package com.example.continuousdetectionml.Helper;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class RectOverlay extends GraphicOverlay.Graphic {
    String text;
    private int mRecrColor = Color.GREEN;
    private float mStrokeWidth = 4.0f;
    private Paint mRectPaint, mPaint;
    private GraphicOverlay graphicOverlay;
    private Rect rect;
    private Bitmap bitmap;

    public RectOverlay(GraphicOverlay overlay, Rect rect, String text) {
        super(overlay);
        mRectPaint = new Paint();
        mPaint = new Paint();
        mRectPaint.setColor(mRecrColor);
        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setColor(Color.RED);
        mPaint.setTextSize(25.0f);
        this.graphicOverlay = overlay;
        this.rect = rect;
        this.text = text;
        postInvalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        RectF rectF = new RectF(rect);
        rectF.left = translateX(rectF.left);
        rectF.right = translateX(rectF.right);
        rectF.bottom = translateX(rectF.bottom);
        rectF.top = translateX(rectF.top);
        canvas.drawRect(rectF, mRectPaint);
        canvas.drawText(text, rect.centerX(), rect.centerY(), mPaint);
    }
}
