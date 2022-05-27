package com.excellence.widget.sample.fragments;

import com.excellence.basetoolslibrary.recycleradapter.BaseRecyclerListAdapter;
import com.excellence.basetoolslibrary.recycleradapter.RecyclerViewHolder;
import com.excellence.widget.sample.R;
import com.excellence.widget.sample.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.leanback.widget.VerticalGridView;
import androidx.recyclerview.widget.DiffUtil;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2022/5/27
 *     desc   :
 * </pre> 
 */
public class ScrollBarVerticalGridViewFragment extends BaseFragment<ViewDataBinding> {

    public static Fragment newInstance() {
        return new ScrollBarVerticalGridViewFragment();
    }

    private ScrollBarVerticalGridViewFragment() {
    }


    @Override
    protected int getContentViewId() {
        return R.layout.fragment_scrollbar_vertical_grid_view;
    }

    @Override
    protected void init() {
        BaseRecyclerListAdapter<String> adapter = new BaseRecyclerListAdapter<String>(
                new DiffUtil.ItemCallback<String>() {
                    @Override
                    public boolean areItemsTheSame(@NonNull String oldItem, @NonNull String newItem) {
                        return false;
                    }

                    @Override
                    public boolean areContentsTheSame(@NonNull String oldItem, @NonNull String newItem) {
                        return false;
                    }
                }, R.layout.simple_list_item_1) {
            @Override
            public void convert(RecyclerViewHolder viewHolder, String item, int position) {
                viewHolder.setText(android.R.id.text1, item);
            }
        };

        VerticalGridView gridView = mRootView.findViewById(R.id.scroll_vertical_gv);
        gridView.setAdapter(adapter);

        List<String> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add("number " + i);
        }
        adapter.submitList(list);
    }
}
