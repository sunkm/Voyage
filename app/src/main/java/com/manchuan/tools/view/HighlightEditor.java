package com.manchuan.tools.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.Layout;
import android.os.Handler;
import android.os.Message;
import android.text.TextWatcher;
import android.text.Editable;
import android.text.style.ForegroundColorSpan;
import android.text.Spanned;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.text.style.BackgroundColorSpan;
import android.view.View;
import android.view.MotionEvent;
import android.util.Log;
import android.text.SpannableStringBuilder;
import androidx.appcompat.widget.AppCompatEditText;

public class HighlightEditor extends AppCompatEditText {
    public static final Pattern a = Pattern.compile("<|>|\\?|\\/");
    public static final Pattern b = Pattern.compile("(?<=\\<).*(?=\\s)");
    public static final Pattern c = Pattern.compile("\".+?\"");
    public static final Pattern d = Pattern.compile(".*?(?=:)");
    public static final Pattern e = Pattern.compile(":.*?=");
    public static final Pattern f = Pattern.compile(".*?(?=\")");
    public static final int COLOR_A = 0xFF000000;
    public static final int COLOR_B = 0xFF020272;
    public static final int COLOR_C = 0xFF027E03;
    public static final int COLOR_D = 0xFF631077;
    public static final int COLOR_E = 0xFF0000FF;

    private Paint leftPaint;//左边栏背景色画笔
    private int paddingLeft = 60;//左边边距宽度
    private TextPaint textPaint;

    private SpannableStringBuilder ed;

    private String text;//左边文字画笔

    public HighlightEditor(Context context) {
        this(context, null);
    }

    public HighlightEditor(Context context, AttributeSet attr) {
        super(context, attr);
        setEnabled(false);
        setPadding(paddingLeft, 0, 0, 0);
        setTextColor(0xFF000000);

        this.leftPaint = new Paint();//左边栏背景色画笔
        this.leftPaint.setColor(Color.parseColor("#64646464"));

        textPaint = new TextPaint();//创建一个textPaint画笔
        textPaint.setColor(Color.parseColor("#FF4081"));
        textPaint.setTextSize(24);
        textPaint.setAntiAlias(true);
    }

    public void setTextMode(boolean isText){
        this.isText = isText;
        if(isText){
            super.setText(text);
        }else{
            setText(ed);
        }
    }
    
    private boolean isText = false;
    public void setText(String text) {
        this.text = text;
        highlight();
        if(isText){
            super.setText(text);
        }else{
            setText(ed);
        }
    }
    
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int LineCount = getLineCount();//编辑框行数
        int LineHeight = getLineHeight();//编辑框每行高度
        if (LineHeight != 0) {
            canvas.drawRect(0, 0, paddingLeft, getHeight(), leftPaint);//绘制左边栏
            for (int i = 0;i < LineCount;i++) {
                StaticLayout layout = new StaticLayout(String.valueOf(i + 1), textPaint, paddingLeft,
                                                       Layout.Alignment.ALIGN_CENTER, 1.0F, 0.0F, true);
                canvas.save();
                canvas.translate(0, (i) * LineHeight);
                layout.draw(canvas);
                canvas.restore();
            }

        }
    }

    
    private void highlight()
    {
        ed = new SpannableStringBuilder(text);
        Matcher A = a.matcher(text);
        Matcher B = b.matcher(text);
        Matcher C = c.matcher(text);
        Matcher D = d.matcher(text);
        Matcher E = e.matcher(text);
        Matcher F = f.matcher(text);
        while (F.find()) {
            ed.setSpan(new ForegroundColorSpan(COLOR_E), F.start(), F.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        while (D.find()) {
            ed.setSpan(new ForegroundColorSpan(COLOR_D), D.start(), D.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        while (B.find()) {
            ed.setSpan(new ForegroundColorSpan(COLOR_B), B.start(), B.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        while (E.find()) {
            ed.setSpan(new ForegroundColorSpan(COLOR_E), E.start(), E.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        while (C.find()) {
            ed.setSpan(new ForegroundColorSpan(COLOR_C), C.start(), C.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        while (A.find()) {
            ed.setSpan(new ForegroundColorSpan(COLOR_A), A.start(), A.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}

