package com.excellence.widget.leanback;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.excellence.basetoolslibrary.utils.ViewUtils;
import com.excellence.widget.R;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2020/5/11
 *     desc   : 单列 横向宽度自适应 + 循环和翻页处理
 * </pre>
 */
public class WrapContentHorizontalGridView extends LoopHorizontalGridView {

    private boolean isWrapContent = true;
    private int mOriginalWidth;

    public WrapContentHorizontalGridView(Context context) {
        this(context, null);
    }

    public WrapContentHorizontalGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WrapContentHorizontalGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.WrapContentHorizontalGridView);
        isWrapContent = typedArray.getBoolean(R.styleable.WrapContentHorizontalGridView_wrap_content, isWrapContent);
        typedArray.recycle();

        ViewUtils.observeViewLayout(this, () -> {
            /**
             * 记录原始宽度
             */
            if (getVisibility() == GONE) {
                /**
                 * 表示隐藏的
                 */
                mOriginalWidth = -1;
            } else {
                mOriginalWidth = getMeasuredWidth();
            }
            setWrapContent();
        });
    }

    @Override
    public void setAdapter(@Nullable RecyclerView.Adapter adapter) {
        super.setAdapter(adapter);

        if (adapter != null) {
            /**
             * 目的是为了每次有改变时，自适应
             */
            adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
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
        if (mOriginalWidth == -1) {
            /**
             * 如果一开始隐藏，则高度为-1，再一次测量原始高度
             */
            mOriginalWidth = 0;
            ViewUtils.observeViewLayout(this, () -> {
                mOriginalWidth = getMeasuredHeight();
                setWrapContent();
            });
            return;
        }

        if (!isWrapContent || mNumRows != 1) {
            return;
        }

        int count = getChildCount();
        if (getAdapter() == null) {
            return;
        }
        if (count <= 0) {
            /**
             * 先判断有没有childView
             */
            if (getChildCount() != getAdapter().getItemCount()) {
                post(this::setWrapContent);
            }
            return;
        }

        /**
         * 仅取一个作为标准，多布局暂不考虑
         */
        View child = getChildAt(0);
        int itemWidth = child.getMeasuredWidth();
        int itemMarginLeft = 0;
        int itemMarginRight = 0;
        if (child.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
            itemMarginLeft = params.leftMargin;
            itemMarginRight = params.rightMargin;
        }

        count = getAdapter().getItemCount();
        int width = getHorizontalSpacing() * (count - 1)
                + (itemWidth + itemMarginLeft + itemMarginRight) * count
                + getPaddingLeft() + getPaddingRight();

        if (width < mOriginalWidth) {
            ViewGroup.LayoutParams lp = getLayoutParams();
            lp.width = width;
            setLayoutParams(lp);
        } else {
            ViewGroup.LayoutParams lp = getLayoutParams();
            lp.width = mOriginalWidth;
            setLayoutParams(lp);
        }
    }

    public void setWrapContent(boolean wrapContent) {
        isWrapContent = wrapContent;
        setWrapContent();
        requestLayout();
    }
}
