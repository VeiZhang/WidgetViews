package com.excellence.widget.sample.base;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2020/6/7
 *     desc   :
 * </pre> 
 */
public abstract class BaseFragment<Binding extends ViewDataBinding> extends Fragment {

    protected Context mContext = null;

    protected Binding mViewDataBinding;
    protected View mRootView;

    @Override
    @CallSuper
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Nullable
    @Override
    @CallSuper
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (mViewDataBinding == null) {
            mViewDataBinding = DataBindingUtil.inflate(inflater, getContentViewId(),
                    container, false);
            /**
             * 当xml里面没有<layout/>标签时，会提示空指针，因为ViewDataBinding没有生成
             */
            mRootView = mViewDataBinding.getRoot();
            init();
        }
        mViewDataBinding.setLifecycleOwner(getViewLifecycleOwner());
        return mRootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        onSupportVisible();
    }

    @Override
    public void onStop() {
        super.onStop();
        onSupportInvisible();
    }

    protected void onSupportVisible() {

    }

    protected void onSupportInvisible() {

    }

    @Override
    @CallSuper
    public void onDestroyView() {
        /**
         * 当A->B后，但B快速回退到A时，A执行onDestroyView与onCreateView间隔时间过短，
         * onDestroyView还没来得及将{@link mRootView}移除，从而导致onCreateView抛出异常
         */
        if (mRootView.getParent() != null) {
            ((ViewGroup) mRootView.getParent()).removeView(mRootView);
        }
        super.onDestroyView();
    }

    public void moveTaskToBack() {
        requireActivity().moveTaskToBack(true);
    }

    public void post(Runnable runnable) {
        mRootView.post(runnable);
    }

    public void postDelayed(Runnable runnable, long delayMillis) {
        mRootView.postDelayed(runnable, delayMillis);
    }

    /**
     * get content view id
     *
     * @return
     */
    @LayoutRes
    protected abstract int getContentViewId();

    /**
     * create -> init -> visible
     * <p>
     * init data and view once
     */
    protected void init() {

    }
}
