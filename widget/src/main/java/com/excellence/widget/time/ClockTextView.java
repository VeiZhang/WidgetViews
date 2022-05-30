package com.excellence.widget.time;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.AttributeSet;

import com.excellence.basetoolslibrary.utils.TimeUtils;
import com.excellence.widget.R;

import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatTextView;

import static android.text.format.DateFormat.is24HourFormat;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2020/6/1
 *     desc   : 时钟文本
 * </pre>
 */
public class ClockTextView extends AppCompatTextView {

    private static final String TIME_FORMAT = "HH:mm";

    private String mOTimeFormat;
    private String mTimeFormat;

    private Handler mHandler;
    private Runnable mTicker;
    private FormatChangeObserver mFormatChangeObserver;
    private boolean isTickerStopped = false;
    private boolean isFollowSystem = true;

    public ClockTextView(Context context) {
        this(context, null);
    }

    public ClockTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClockTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ClockTextView);
        mOTimeFormat = typedArray.getString(R.styleable.ClockTextView_timeFormat);
        isFollowSystem = typedArray.getBoolean(R.styleable.ClockTextView_isFollowSystem, isFollowSystem);
        typedArray.recycle();

        setTimeFormat(mOTimeFormat);

        mHandler = new Handler();
        mTicker = new Ticker();
        mFormatChangeObserver = new FormatChangeObserver(mHandler);
    }

    private void runClock() {
        if (!isTickerStopped) {
            setText(DateFormat.format(mTimeFormat, System.currentTimeMillis()));
            invalidate();
            long uptimeMillis = SystemClock.uptimeMillis();
            uptimeMillis += TimeUtils.SEC - (uptimeMillis % TimeUtils.SEC);
            mHandler.postAtTime(mTicker, uptimeMillis);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isTickerStopped = false;
        getContext().getContentResolver()
                .registerContentObserver(Settings.System.CONTENT_URI, true, mFormatChangeObserver);
        mTicker.run();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isTickerStopped = true;
        getContext().getContentResolver().unregisterContentObserver(mFormatChangeObserver);
        mHandler.removeCallbacksAndMessages(null);
    }

    private String transferTimeFormat(String timeFormat) {
        if (timeFormat == null) {
            timeFormat = TIME_FORMAT;
        }

        if (isFollowSystem) {
            if (is24HourFormat(getContext())) {
                timeFormat = timeFormat.replace("a", "").trim()
                        .replace("hh", "HH");
            } else {
                timeFormat = timeFormat.replace("HH", "hh");
                if (timeFormat.contains("hh") || timeFormat.contains("mm")) {
                    if (!timeFormat.contains("a")) {
                        /**
                         * 注意有小时和分钟才增加a，否则如年月日不增加a
                         */
                        timeFormat += " " + "a";
                    }
                }
            }
        }
        return timeFormat;
    }

    public void setTimeFormat(@StringRes int timeFormatResId) {
        setTimeFormat(getResources().getString(timeFormatResId));
    }

    public void setTimeFormat(String timeFormat) {
        mOTimeFormat = timeFormat;
        mTimeFormat = transferTimeFormat(timeFormat);
    }

    private class FormatChangeObserver extends ContentObserver {

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        private FormatChangeObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            setTimeFormat(mOTimeFormat);
        }
    }

    private class Ticker implements Runnable {

        @Override
        public void run() {
            runClock();
        }
    }
}
