package com.excellence.widget.sample;

import android.util.Pair;

import com.excellence.basetoolslibrary.recycleradapter.BaseRecyclerListAdapter;
import com.excellence.basetoolslibrary.recycleradapter.RecyclerViewHolder;
import com.excellence.widget.sample.base.BaseActivity;
import com.excellence.widget.sample.fragments.ScrollBarVerticalGridViewFragment;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.leanback.widget.VerticalGridView;
import androidx.recyclerview.widget.DiffUtil;

public class MainActivity extends BaseActivity<ViewDataBinding> {

    @Override
    protected int getContentViewId() {
        return R.layout.activity_main;
    }

    @Override
    protected void init() {
        BaseRecyclerListAdapter<Pair<String, Fragment>> adapter = new BaseRecyclerListAdapter<Pair<String, Fragment>>(
                new DiffUtil.ItemCallback<Pair<String, Fragment>>() {
                    @Override
                    public boolean areItemsTheSame(@NonNull Pair<String, Fragment> oldItem, @NonNull Pair<String, Fragment> newItem) {
                        return false;
                    }

                    @Override
                    public boolean areContentsTheSame(@NonNull Pair<String, Fragment> oldItem, @NonNull Pair<String, Fragment> newItem) {
                        return false;
                    }
                }, R.layout.simple_list_item_1) {
            @Override
            public void convert(RecyclerViewHolder viewHolder, Pair<String, Fragment> item, int position) {
                viewHolder.setText(android.R.id.text1, item.first);
            }
        };

        VerticalGridView gridView = mRootView.findViewById(R.id.widget_lv);
        gridView.setAdapter(adapter);

        List<Pair<String, Fragment>> list = new ArrayList<>();
        list.add(Pair.create("ScrollBar VerticalGridView", ScrollBarVerticalGridViewFragment.newInstance()));
        adapter.submitList(list);

        adapter.setOnItemFocusChangeListener((viewHolder, v, hasFocus, position) -> {
            Pair<String, Fragment> item = adapter.getItem(position);
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, item.second).commit();
        });
    }
}