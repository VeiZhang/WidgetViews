package com.excellence.widget.sample.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2018/10/24
 *     desc   :
 * </pre>
 */
public abstract class BaseActivity<Binding extends ViewDataBinding> extends AppCompatActivity {

    protected Context mContext = null;
    protected Binding mViewDataBinding = null;
    protected View mRootView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            savedInstanceState = null;
        }
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mViewDataBinding = DataBindingUtil.setContentView(this, getContentViewId());
        mRootView = mViewDataBinding.getRoot();
        mViewDataBinding.setLifecycleOwner(this);
        mContext = this;
        init();
    }

    public void post(Runnable runnable) {
        mRootView.post(runnable);
    }

    /**
     * get content view id
     *
     * @return
     */
    @LayoutRes
    protected abstract int getContentViewId();

    /**
     * init data and view
     */
    protected abstract void init();

    /**
     * 控制按键速度，特别是有{@link androidx.leanback.widget.BaseGridView}的界面
     */
    private long mTimeLast = 0;
    private long mTimeSpace = 0;

    @SuppressLint("RestrictedApi")
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        long keyEventOutTime = keyEventOutTime();
        if (keyEventOutTime <= 0) {
            return super.dispatchKeyEvent(event);
        }

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            long nowTime = System.currentTimeMillis();
            long timeDelay = nowTime - mTimeLast;
            mTimeLast = nowTime;
            if (mTimeSpace <= keyEventOutTime && timeDelay <= keyEventOutTime) {
                mTimeSpace += timeDelay;
                return true;
            }
            mTimeSpace = 0L;
        }
        return super.dispatchKeyEvent(event);
    }

    /**
     * 按键间隔，防止长按过快
     *
     * @return
     */
    protected long keyEventOutTime() {
        return 0;
    }
}
