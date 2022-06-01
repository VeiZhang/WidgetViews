package com.excellence.widget.leanback;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.excellence.widget.R;

import androidx.leanback.widget.ItemAlignmentFacet;
import androidx.leanback.widget.VerticalGridView;

/**
 * 带滚动条的VerticalGridView
 */
public class ScrollbarVerticalGridView extends VerticalGridView {

    private static final String TAG = ScrollbarVerticalGridView.class.getSimpleName();

    private boolean isAlignTop = true;
    private int mItemHeight = 0;
    protected int mNumColumns = 1;
    private FastScroller mFastScroller;

    public ScrollbarVerticalGridView(Context context) {
        this(context, null);
    }

    public ScrollbarVerticalGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("VisibleForTests")
    public ScrollbarVerticalGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(false);
        setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        setVerticalScrollBarEnabled(true);
        readSuperAttr(context, attrs, defStyle);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScrollbarVerticalGridView,
                defStyle, 0);
        isAlignTop = typedArray.getBoolean(R.styleable.ScrollbarVerticalGridView_isAlignTop, isAlignTop);
        typedArray.recycle();

        setNumColumns(mNumColumns);
    }

    private void readSuperAttr(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RecyclerView,
                defStyle, 0);
        boolean enableFastScroller = a.getBoolean(R.styleable.RecyclerView_fastScrollEnabled, false);
        if (enableFastScroller) {
            StateListDrawable verticalThumbDrawable = (StateListDrawable) a
                    .getDrawable(R.styleable.RecyclerView_fastScrollVerticalThumbDrawable);
            Drawable verticalTrackDrawable = a
                    .getDrawable(R.styleable.RecyclerView_fastScrollVerticalTrackDrawable);
            StateListDrawable horizontalThumbDrawable = (StateListDrawable) a
                    .getDrawable(R.styleable.RecyclerView_fastScrollHorizontalThumbDrawable);
            Drawable horizontalTrackDrawable = a
                    .getDrawable(R.styleable.RecyclerView_fastScrollHorizontalTrackDrawable);

            Resources resources = getContext().getResources();
            mFastScroller = new FastScroller(this, verticalThumbDrawable, verticalTrackDrawable,
                    horizontalThumbDrawable, horizontalTrackDrawable,
                    resources.getDimensionPixelSize(R.dimen.fastscroll_default_thickness),
                    resources.getDimensionPixelSize(R.dimen.fastscroll_minimum_range),
                    resources.getDimensionPixelOffset(R.dimen.fastscroll_margin));
        }
        mNumColumns = a.getInt(R.styleable.lbVerticalGridView_numberOfColumns, 1);

        a.recycle();
    }

    private void setAlignRule() {
        if (mNumColumns == 1 && isAlignTop) {
            /**
             * 对齐顶部
             */
            setItemAlignmentOffset(0);
            setItemAlignmentOffsetPercent(VerticalGridView.ITEM_ALIGN_OFFSET_PERCENT_DISABLED);
            setItemAlignmentOffsetWithPadding(true);
        } else {
            ItemAlignmentFacet.ItemAlignmentDef def = new ItemAlignmentFacet.ItemAlignmentDef();
            setItemAlignmentOffset(def.getItemAlignmentOffset());
            setItemAlignmentOffsetPercent(def.getItemAlignmentOffsetPercent());
            setItemAlignmentOffsetWithPadding(def.isItemAlignmentOffsetWithPadding());
        }
    }

    @Override
    public void setNumColumns(int numColumns) {
        mNumColumns = numColumns;
        super.setNumColumns(numColumns);
        setAlignRule();
    }

    @Override
    public int computeVerticalScrollOffset() {
        // 偏移长度
        if (getItemCount() == 0) {
            return 0;
        }

        int selection = getSelectedPosition();
        int centerChildCount = getChildCount() / 2;
        int offset = getScrollRange(selection);

        // 上临界
        if (selection <= centerChildCount) {
            offset = mFastScroller.mVerticalThumbHeight / 2;
        }

        // 下临界
        if (selection >= (getItemCount() - 1) - centerChildCount) {
            offset = getScrollRange(selection);
        }

        return offset;
    }

    @Override
    public int computeVerticalScrollExtent() {
        // 滑块长度
        return super.computeVerticalScrollExtent();
    }

    @Override
    public int computeVerticalScrollRange() {
        // 总长度
        if (getItemCount() == 0) {
            return 0;
        }
        return getRowNumber(getItemCount() - 1) * getItemHeight();
    }

    private int getScrollRange(int selection) {
        // mFastScroller.mVerticalThumbCenterY
        return getRowNumber(selection) * getItemHeight() - mFastScroller.mVerticalThumbHeight / 2;
    }

    public int getItemCount() {
        if (getAdapter() != null) {
            return getAdapter().getItemCount();
        }
        return 0;
    }

    private int getRowNumber(int position) {
        return (int) Math.ceil((float) position / mNumColumns);
    }

    private int getItemHeight() {
        if (mItemHeight == 0) {
            View focusView = getChildAt(getSelectedPosition());
            if (focusView != null) {
                mItemHeight = focusView.getHeight();
            }
        }
        return mItemHeight;
    }

}
