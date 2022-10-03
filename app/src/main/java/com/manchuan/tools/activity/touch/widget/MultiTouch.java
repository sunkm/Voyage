package com.manchuan.tools.activity.touch.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.manchuan.tools.activity.touch.MainActivity;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class MultiTouch extends View {

	private int a;
    private int b;
    private float c;
    private float d;
    private final Paint[][] e;
    private boolean[][] f;
    private final MainActivity g;
    private String h;
    private long i;
    private int j;
    private int k;
    private int l;
    private int m;
    private int n;
    private float o;
    private float p;
    private boolean q;
    private final List<Integer> r;
    private final List<Integer> s;

	public MultiTouch(Context context) {
        this(context, null);
    }

    public MultiTouch(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.e = (Paint[][]) Array.newInstance(Paint.class, new int[]{2, 2});
        this.i = 0;
        this.j = 0;
        this.k = 0;
        this.l = 0;
        this.m = 0;
        this.n = 0;
        this.o = 0.0f;
        this.p = 0.0f;
        this.q = false;
        this.r = new ArrayList<>();
        this.s = new ArrayList<>();
        this.g = (MainActivity) context;
        this.b = 0;
        this.a = 0;
        int applyDimension = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15.0f, getResources().getDisplayMetrics());
        this.e[0][0] = a(Color.parseColor("#200000"), applyDimension);
        this.e[0][1] = a(Color.parseColor("#400000"), applyDimension);
        this.e[1][0] = a(Color.parseColor("#116611"), applyDimension);
        this.e[1][1] = a(Color.parseColor("#55aa55"), applyDimension);
    }

	private Paint a(int i2, int i3) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(i2);
        paint.setTextSize((float) i3);
        paint.setTextAlign(Paint.Align.LEFT);
        return paint;
    }

    private void b() {
        this.c = (((float) getWidth())) / ((float) this.a);
        this.d = (((float) getHeight())) / ((float) this.b);
        this.f = (boolean[][]) Array.newInstance(Boolean.TYPE, new int[]{this.a, this.b});
        invalidate();
    }

    private void c() {
        for (int i2 = 0; i2 < this.a; i2++) {
            int i3 = 0;
            while (i3 < this.b) {
                if (this.f[i2][i3]) {
                    i3++;
                } else {
                    return;
                }
            }
        }
        this.g.b();
    }

    public boolean a() {
        return this.q;
    }

    public int getColumns() {
        return this.a;
    }

    public int getMaxMultiTouch() {
        return this.l;
    }

    public int getMaxSampleRate() {
        return this.j;
    }

    public int getRows() {
        return this.b;
    }

	@SuppressLint({"DefaultLocale", "DrawAllocation"})
    @Override
    protected void onDraw(Canvas canvas) {
        for (int i2 = 0; i2 < this.a; i2++) {
            for (int i3 = 0; i3 < this.b; i3++) {
                canvas.drawRect(this.c * ((float) i2), this.d * ((float) i3), this.c * ((float) (i2 + 1)), this.d * ((float) (i3 + 1)), this.e[this.f[i2][i3] ? (char) 1 : 0][(i2 + i3) % 2]);
            }
        }
        Paint.FontMetrics fontMetrics = this.e[1][1].getFontMetrics();
        int ceil = (int) Math.ceil(fontMetrics.descent - fontMetrics.ascent);
        int i4 = (int) (((float) ceil) - fontMetrics.descent);
        if ("sample".equals(this.h)) {
            canvas.drawText(String.format("最大采样率: %1$dhz, 当前采样率: %2$dhz", this.j, this.k), 0.0f, (float) i4, this.e[1][1]);
        } else if ("multi".equals(this.h)) {
            canvas.drawText(String.format("最大触摸数: %1$d, 当前触摸数: %2$d", this.l, this.m), 0.0f, (float) i4, this.e[1][1]);
            for (int i5 = 0; i5 < this.r.size(); i5++) {
                canvas.drawCircle((float) this.r.get(i5), (float) this.s.get(i5), (float) (ceil * 2), this.e[1][0]);
            }
        } else if ("pressure".equals(this.h)) {
            canvas.drawText(String.format("当前压力: %1$.2f, 面积: %2$.2f, 支持压力触控: %3$s", this.o, this.p, TouchUtils.a(this.q)), 0.0f, (float) i4, this.e[1][1]);
        }
    }

	@Override
    protected void onSizeChanged(int i2, int i3, int i4, int i5) {
        super.onSizeChanged(i2, i3, i4, i5);
        b();
    }

	@SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean z = false;
        int actionMasked = motionEvent.getActionMasked();
        long uptimeMillis = SystemClock.uptimeMillis();
        if (actionMasked == 0 || actionMasked == 2 || actionMasked == 1) {
            int x = (int) (motionEvent.getX() / this.c);
            int y = (int) (motionEvent.getY() / this.d);
            if (x >= 0 && x < this.a && y >= 0 && y < this.b) {
                this.f[x][y] = true;
                c();
                invalidate();
            }
            if (uptimeMillis - this.i > 1000) {
                this.i = uptimeMillis;
                this.k = this.n;
                this.n = 1;
                invalidate();
            } else if (motionEvent.getPointerCount() <= 1) {
                this.n += Math.max(motionEvent.getHistorySize(), 1);
                this.j = Math.max(this.j, this.n);
            }
        }
        this.r.clear();
        this.s.clear();
        if (TouchUtils.a(actionMasked, new Integer[]{6, 1})) {
            this.m = motionEvent.getPointerCount() - 1;
            this.o = 0.0f;
            this.p = 0.0f;
            invalidate();
        } else {
            if (TouchUtils.a(actionMasked, new Integer[]{5, 0})) {
                this.m = motionEvent.getPointerCount();
                this.l = Math.max(this.m, this.l);
                this.o = motionEvent.getPressure();
                this.p = motionEvent.getSize();
                if ((((double) this.o) > 0.01d && ((double) this.o) < 0.99d) || ((double) this.o) > 1.01d) {
                    z = true;
                }
                this.q = z;
                invalidate();
            } else if (actionMasked == 2) {
                for (int i2 = 0; i2 < motionEvent.getPointerCount(); i2++) {
                    this.r.add((int) motionEvent.getX(i2));
                    this.s.add((int) motionEvent.getY(i2));
                }
                this.o = motionEvent.getPressure();
                this.p = motionEvent.getSize();
                if ((((double) this.o) > 0.01d && ((double) this.o) < 0.99d) || ((double) this.o) > 1.01d) {
                    z = true;
                }
                this.q = z;
                invalidate();
            } else if (actionMasked == 3) {
                this.m = 0;
                this.o = 0.0f;
                this.p = 0.0f;
                invalidate();
            }
        }
        return true;
    }

    public void setColumns(int i2) {
        this.a = Math.max(1, i2);
        b();
    }

    public void setRows(int i2) {
        this.b = Math.max(1, i2);
        b();
    }

    public void setShowInfo(String str) {
        this.h = str;
    }
	
}
