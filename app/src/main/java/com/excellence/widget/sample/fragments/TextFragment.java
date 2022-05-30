package com.excellence.widget.sample.fragments;

import com.excellence.widget.sample.R;
import com.excellence.widget.sample.base.BaseFragment;

import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2022/5/30
 *     desc   :
 * </pre> 
 */
public class TextFragment extends BaseFragment<ViewDataBinding> {

    public static Fragment newInstance() {
        return new TextFragment();
    }

    @Override
    protected int getContentViewId() {
        return R.layout.text_fragment;
    }
}
