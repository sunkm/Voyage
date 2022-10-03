package com.manchuan.tools.view;

import com.manchuan.tools.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.viewpager.widget.ViewPager;

/**
 * @author Felix.Liang
 */
public class GMTimeSwitcher extends FrameLayout implements ISwitcher {

    private int mCheckedIndex = -1;
    private int mNewIndex = -1;
    private float mPreviousOffsetPixels;
    private OnCheckedChangeListener mOnCheckedChangeListener;
    private AbsTimeView.AnimatedCallback mHideAnimatedCallback = new AbsTimeView.AnimatedCallback() {
        @Override
        public void onStart() {
        }

        @Override
        public void onEnd() {
            AbsTimeView oldCheck = getCheckedItem();
            oldCheck.setVisibility(GONE);
            showNewChecked();
        }
    };
    private AbsTimeView.AnimatedCallback mShowAnimatedCallback = new AbsTimeView.AnimatedCallback() {
        @Override
        public void onStart() {
        }

        @Override
        public void onEnd() {
        }
    };

    public GMTimeSwitcher(Context context) {
        this(context, null);
    }

    public GMTimeSwitcher(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GMTimeSwitcher(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public GMTimeSwitcher(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.GMTimeSwitcher, defStyleAttr, defStyleRes);
        final int checkedIndex = array.getInt(R.styleable.GMTimeSwitcher_checkPosition, 0);
        setCheckedIndex(checkedIndex);
        array.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        verifyChildren();
        checkWithoutAnimation(mCheckedIndex);
    }

    private void verifyChildren() {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child != null) {
                if (!(child instanceof AbsTimeView)) {
                    throw new IllegalArgumentException("TimeSwitcher can only hold subclass of AbsTimeView!");
                }
            }
        }
    }

    private void checkWithoutAnimation(int index) {
        setCheckedIndex(generateIndex(index));
        hindAllChildren();
        final View child = getChildAt(mCheckedIndex);
        if (child != null) {
            child.setVisibility(VISIBLE);
            if (mOnCheckedChangeListener != null)
                mOnCheckedChangeListener.onCheckedChanged((AbsTimeView) child, true);
        }
    }

    @Override
    public void check(int index) {
        if (index != mCheckedIndex) {
            mNewIndex = generateIndex(index);
            if (ViewConfig.SHOW_HIDE_ANIMATION)
                hideOldChecked();
            else {
                AbsTimeView oldCheck = getCheckedItem();
                oldCheck.setVisibility(GONE);
                showNewChecked();
            }
        }
    }

    private void hideOldChecked() {
        AbsTimeView oldCheck = getCheckedItem();
        oldCheck.setAnimatedCallback(mHideAnimatedCallback);
        oldCheck.animatedHide();
    }

    private void showNewChecked() {
        setCheckedIndex(mNewIndex);
        AbsTimeView newCheck = getCheckedItem();
        newCheck.setVisibility(VISIBLE);
        newCheck.setAnimatedCallback(mShowAnimatedCallback);
        newCheck.animatedShow();
    }

    @Override
    public AbsTimeView getItem(int index) {
        return (AbsTimeView) getChildAt(index);
    }

    @Override
    public AbsTimeView getCheckedItem() {
        return (AbsTimeView) getChildAt(mCheckedIndex);
    }

    @Override
    public int getCheckedViewId() {
        return getCheckedItem().getId();
    }

    private void setCheckedIndex(int index) {
        if (mCheckedIndex != index) {
            mCheckedIndex = index;
        }
    }

    private int generateIndex(int index) {
        if (index < 0) index = 0;
        final int count = getChildCount();
        if (index > count - 1) index = count - 1;
        return index;
    }

    @Override
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeListener = listener;
    }

    private void hindAllChildren() {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            child.setVisibility(GONE);
        }
    }

    @Override
    public void bindViewPager(ViewPager pager) {
        if (pager != null) {
            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if (positionOffsetPixels == 0) return;
                    if (positionOffsetPixels > mPreviousOffsetPixels) {
                        check(position + 1);
                    } else if (positionOffsetPixels < mPreviousOffsetPixels) {
                        check(position);
                    }
                    mPreviousOffsetPixels = positionOffsetPixels;
                }

                @Override
                public void onPageSelected(int position) {
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
        }
    }
}

