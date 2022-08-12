package com.excellence.widget.leanback;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.excellence.basetoolslibrary.utils.ViewUtils;
import com.excellence.widget.R;

import androidx.annotation.Nullable;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2020/5/11
 *     desc   : 单行 垂直列表自适应高度 + 循环和翻页
 * </pre>
 */
public class WrapContentVerticalGridView extends LoopVerticalGridView {

    private boolean isWrapContent = true;
    private int mOriginalHeight;

    public WrapContentVerticalGridView(Context context) {
        this(context, null);
    }

    public WrapContentVerticalGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WrapContentVerticalGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.WrapContentVerticalGridView);
        isWrapContent = typedArray.getBoolean(R.styleable.WrapContentVerticalGridView_wrap_content, isWrapContent);
        typedArray.recycle();

        ViewUtils.observeViewLayout(this, () -> {
            /**
             * 记录原始高度
             */
            if (getVisibility() == GONE) {
                /**
                 * 表示隐藏的
                 */
                mOriginalHeight = -1;
            } else {
                mOriginalHeight = getMeasuredHeight();
            }
            setWrapContent();
        });
    }

    @Override
    public void setAdapter(@Nullable Adapter adapter) {
        super.setAdapter(adapter);

        if (adapter != null) {
            /**
             * 目的是为了每次有改变时，自适应
             */
            adapter.registerAdapterDataObserver(new AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    postSetWrapContent();
                }

                @Override
                public void onItemRangeChanged(int positionStart, int itemCount) {
                    super.onItemRangeChanged(positionStart, itemCount);
                    postSetWrapContent();
                }

                @Override
                public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
                    super.onItemRangeChanged(positionStart, itemCount, payload);
                    postSetWrapContent();
                }

                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    super.onItemRangeInserted(positionStart, itemCount);
                    postSetWrapContent();
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    super.onItemRangeRemoved(positionStart, itemCount);
                    postSetWrapContent();
                }

                @Override
                public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                    super.onItemRangeMoved(fromPosition, toPosition, itemCount);
                    postSetWrapContent();
                }
            });
        }
    }

    private void postSetWrapContent() {
        setWrapContent();
        ViewUtils.observeViewLayout(this, this::setWrapContent);
    }

    private void setWrapContent() {
        if (mOriginalHeight == -1) {
            /**
             * 如果一开始隐藏，则高度为-1，再一次测量原始高度
             */
            mOriginalHeight = 0;
            ViewUtils.observeViewLayout(this, () -> {
                mOriginalHeight = getMeasuredHeight();
                setWrapContent();
            });
            return;
        }

        if (!isWrapContent) {
            return;
        }

        int childCount = getChildCount();
        if (getAdapter() == null) {
            return;
        }
        if (childCount <= 0) {
            /**
             * 先判断有没有childView
             */
            if (childCount != getAdapter().getItemCount()) {
                post(this::setWrapContent);
            }
            return;
        }

        /**
         * 仅取一个作为标准，多布局暂不考虑
         */
        View child = getChildAt(0);
        int itemHeight = child.getMeasuredHeight();
        int itemMarginTop = 0;
        int itemMarginBottom = 0;
        if (child.getLayoutParams() instanceof MarginLayoutParams) {
            MarginLayoutParams params = (MarginLayoutParams) child.getLayoutParams();
            itemMarginTop = params.topMargin;
            itemMarginBottom = params.bottomMargin;
        }

        int rowNumber = getRowNumber(getAdapter().getItemCount() - 1) + 1;
        int height = getVerticalSpacing() * (rowNumber - 1)
                + (itemHeight + itemMarginTop + itemMarginBottom) * rowNumber
                + getPaddingTop() + getPaddingBottom();

        if (mOriginalHeight == 0 || height < mOriginalHeight) {
            ViewGroup.LayoutParams lp = getLayoutParams();
            /**
             * 高度设置时，防止{@link androidx.recyclerview.widget.ListAdapter#submitList}绘制出现跳空换行绘制
             */
            lp.height = height + 1;
            setLayoutParams(lp);
        } else {
            ViewGroup.LayoutParams lp = getLayoutParams();
            lp.height = mOriginalHeight;
            setLayoutParams(lp);
        }
    }

    public void setWrapContent(boolean centerVertical) {
        isWrapContent = centerVertical;
        setWrapContent();
        requestLayout();
    }

}
