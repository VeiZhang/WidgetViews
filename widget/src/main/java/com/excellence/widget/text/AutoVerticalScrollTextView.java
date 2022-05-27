package com.excellence.widget.text;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Handler;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.excellence.basetoolslibrary.utils.ViewUtils;
import com.excellence.widget.R;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * 基于https://github.com/EngrZhou/AutoVerticalScrollTextView改善
 */
public class AutoVerticalScrollTextView extends AppCompatTextView {

    private static final String TAG = AutoVerticalScrollTextView.class.getSimpleName();

    private static final int DEFAULT_DELAY_START_SECOND = 5 * 1000;
    private static final int SPEED_SCALE = 50;
    private static final int TYPE_TRANSLATE = 0;
    private static final int TYPE_SCROLL_TO = 1;

    /**
     * scroll delay time
     */
    private int mDelayStart = DEFAULT_DELAY_START_SECOND;
    private int mStep = 5;
    private int mSpeed = 1;
    private int mMaxHeight;
    private boolean isScrolled = false;
    private Handler mMarquee = null;
    private StaticLayout mTextLayout;
    private int mNowPoint;
    private int mMyHeight;
    private boolean isScrollEnabled = true;
    /**
     * 滚动方式 0：绘制文本高度，但是少于三行时，Series detail会有重叠的情况；
     *         1：通过TextView#scrollBy控制，但是小窗口Channel播放VideoView刷新时，会频繁闪烁
     */
    private int mScrollType = TYPE_TRANSLATE;

    public AutoVerticalScrollTextView(Context context) {
        this(context, null);
    }

    public AutoVerticalScrollTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoVerticalScrollTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);

        reset();
        requestLayout();
        ViewUtils.observeViewLayout(this, this::reset);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == View.VISIBLE) {
            reset();
            requestLayout();
            ViewUtils.observeViewLayout(this, this::reset);
        }
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.AutoVerticalScrollTextView, 0, 0);
            mDelayStart = array.getInt(R.styleable.AutoVerticalScrollTextView_delayStart, DEFAULT_DELAY_START_SECOND);
            mStep = array.getInt(R.styleable.AutoVerticalScrollTextView_step, 1);
            mSpeed = array.getInt(R.styleable.AutoVerticalScrollTextView_speed, 2);
            mMaxHeight = array.getDimensionPixelOffset(R.styleable.AutoVerticalScrollTextView_maxHeight, 0);
            isScrollEnabled = array.getBoolean(R.styleable.AutoVerticalScrollTextView_scrollEnabled, isScrollEnabled);
            mScrollType = array.getInt(R.styleable.AutoVerticalScrollTextView_scrollType, mScrollType);
            array.recycle();
        }
        setSingleLine(false);
        if (mMaxHeight > 0) {
            setMaxHeight(mMaxHeight);
        }
    }

    public int getDelayStart() {
        return mDelayStart;
    }

    public void setDelayStart(int delayStart) {
        this.mDelayStart = delayStart;
    }

    public int getStep() {
        return mStep;
    }

    public void setStep(int step) {
        this.mStep = step;
    }

    public int getSpeed() {
        return mSpeed;
    }

    public void setSpeed(int speed) {
        this.mSpeed = speed;
    }

    public boolean isScrollEnabled() {
        return isScrollEnabled;
    }

    public void setScrollEnabled(boolean scrollEnabled) {
        isScrollEnabled = scrollEnabled;
        reset();
    }

    public void reset() {
        mNowPoint = 0;
        if (mScrollType == TYPE_SCROLL_TO) {
            scrollTo(0, mNowPoint);
        }
        resetStatus();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        if (mMaxHeight > 0 && params.height > mMaxHeight) {
            params.height = mMaxHeight;
        }
        super.setLayoutParams(params);
        post(this::reset);
    }

    private synchronized void resetStatus() {
        if (!isScrollEnabled) {
            stopMarquee();
            postInvalidate();
            return;
        }
        resetTextParams();
        resetThread();
        postInvalidate();
    }

    private synchronized void resetTextParams() {
        int currentTextColor = getCurrentTextColor();
        TextPaint textPaint = getPaint();
        textPaint.setColor(currentTextColor);
        mTextLayout = new StaticLayout(getText(), getPaint(),
                getWidth(), Layout.Alignment.ALIGN_NORMAL,
                getLineSpacingMultiplier(), getLineSpacingExtra(), false);
    }

    protected final synchronized void resetThread() {
        mMyHeight = getLineHeight() * getLineCount();
        /**
         * 判断是否超出范围 -> 滚动
         * 相等时，有可能是wrap_content，则不能滚动
         */
        int measureHeight = getMeasuredHeight();
//        Log.i(TAG, "Text can scroll: " + mMyHeight + " - " + measureHeight);
        isScrolled = mMyHeight > measureHeight;
        /**
         * 当从不可见变成可见的时候，没有完成绘制时measureHeight为0 ,mMyHeight > 0
         * 从而导致不管是否超出实际maxHeight 都会滚动一次
         * 所以如果measureHeight为0的时候不滚动
         */
        if (isScrolled && measureHeight > 0) {
            startMarquee();
        }
    }

    public void stopMarquee() {
        if (mMarquee != null) {
            mMarquee.removeCallbacksAndMessages(null);
        }
    }

    private void startMarquee() {
        if (mMarquee == null) {
            mMarquee = new Handler();
        }
        stopMarquee();

        mMarquee.postDelayed(mMarqueeRunnable, mDelayStart);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        reset();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopMarquee();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mScrollType == TYPE_TRANSLATE) {
            if (isScrollEnabled && isScrolled) {
                canvas.save();
                float textX = 0;
                float textY = mNowPoint;
                canvas.translate(textX, textY);
                if (mTextLayout != null) {
                    mTextLayout.draw(canvas);
                }
                canvas.restore();
                return;
            }
        }
        super.onDraw(canvas);
    }

    private Runnable mMarqueeRunnable = new Runnable() {

        @Override
        public void run() {
            if (mStep <= 0) {
                stopMarquee();
                return;
            } else {
                mNowPoint -= mStep;
                if (mMyHeight != 0 && mNowPoint < -mMyHeight) {
                    reset();
                    return;
                }
                if (mScrollType == TYPE_TRANSLATE) {
                    postInvalidate();
                } else {
                    scrollTo(0, -mNowPoint);
                }
            }
            mMarquee.postDelayed(mMarqueeRunnable, SPEED_SCALE * mSpeed);
        }
    };
}
