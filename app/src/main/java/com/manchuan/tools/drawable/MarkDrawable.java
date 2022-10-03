package com.manchuan.tools.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;

public class MarkDrawable extends Drawable {
    private TextPaint mPaint;
    private int mTextColor= 0xFFf8e1e2;
    private int mBackgroundColor= 0xffffff;
    private RectF mBoundRect;
    private final String mMarkStr;
    private static final int inset = 80;
    private int mTextSize=40;
    private final int mDegree=30;

    public MarkDrawable(String mMarkStr) {
        this.mMarkStr = mMarkStr;
        init();
    }

    public MarkDrawable(String mMarkStr,int textColor) {
        this.mMarkStr = mMarkStr;
        this.mTextColor=textColor;
        init();
    }


    public MarkDrawable(String mMarkStr,int textColor,int backgroundColor) {
        this.mMarkStr = mMarkStr;
        this.mTextColor=textColor;
        this.mBackgroundColor=backgroundColor;
        init();
    }

    public MarkDrawable(String mMarkStr,int textColor,int textSize,int backgroundColor) {
        this.mMarkStr = mMarkStr;
        this.mTextColor=textColor;
        this.mTextSize=textSize;
        this.mBackgroundColor=backgroundColor;
        init();
    }


    private void init(){
        mPaint=new TextPaint();
        mPaint.setTextSize(mTextSize);
        mPaint.setAntiAlias(true);
        mPaint.setColor(mTextColor);
        final float width=mPaint.measureText(mMarkStr,0,mMarkStr.length());

        Rect rect=new Rect();
        mPaint.getTextBounds(mMarkStr,0,mMarkStr.length(),rect);
        mBoundRect=new RectF();
        mBoundRect.set(0,0,(float) (width*Math.cos(Math.toRadians(mDegree) ))+inset,(float)(width*Math.sin(Math.toRadians(mDegree)))+inset);
    }
    @Override
    public void draw(Canvas canvas) {
        canvas.save();
        canvas.drawColor(mBackgroundColor);
        canvas.translate(mBoundRect.width()/2,mBoundRect.height()/2);
        canvas.rotate(-mDegree);
        canvas.drawText(mMarkStr,inset/2-mBoundRect.width()/2,0,mPaint);
        canvas.restore();

    }

    @Override
    public void setAlpha(int i) {
        mPaint.setAlpha(i);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;

    }

    @Override
    public int getIntrinsicHeight() {
        return (int)mBoundRect.height();
    }

    @Override
    public int getIntrinsicWidth() {
        return (int)mBoundRect.width();
    }
}