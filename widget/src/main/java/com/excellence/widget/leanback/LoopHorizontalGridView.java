package com.excellence.widget.leanback;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.KeyEvent;

import com.excellence.basetoolslibrary.utils.KeyController;
import com.excellence.widget.R;

import androidx.leanback.widget.HorizontalGridView;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2020/5/11
 *     desc   : 横向循环和翻页处理
 * </pre> 
 */
public class LoopHorizontalGridView extends HorizontalGridView {

    /**
     * 一页的个数
     */
    private int mPageItemCount = 0;
    /**
     * 是否循环
     */
    private boolean isLoop = true;
    protected int mNumRows = 1;

    /**
     * 有时候需要屏蔽循环key事件，则设置false。总开关
     */
    private boolean mHandKey = true;
    /**
     * 多行时，有时候上下使用系统处理，不需要额外处理时，默认true：额外处理，循环滚动；false：系统处理
     */
    private boolean mHandleUpKey = true;
    private boolean mHandleDownKey = true;

    private KeyController mKeyController = null;

    public LoopHorizontalGridView(Context context) {
        this(context, null);
    }

    public LoopHorizontalGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoopHorizontalGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoopHorizontalGridView);
        mPageItemCount = typedArray.getInteger(R.styleable.LoopVerticalGridView_pageItemCount, mPageItemCount);
        isLoop = typedArray.getBoolean(R.styleable.LoopVerticalGridView_loop, isLoop);

        mHandKey = typedArray.getBoolean(R.styleable.LoopHorizontalGridView_handleKey, mHandKey);
        mHandleUpKey = typedArray.getBoolean(R.styleable.LoopHorizontalGridView_handleUpKey, mHandleUpKey);
        mHandleDownKey = typedArray.getBoolean(R.styleable.LoopHorizontalGridView_handleDownKey, mHandleDownKey);
        typedArray.recycle();

        mKeyController = new KeyController();
    }

    private int getPageItemCount() {
        /**
         * childCount可能会发生变化
         */
        if (mPageItemCount <= 0) {
            return getChildCount();
        }
        return mPageItemCount;
    }

    public void setPageItemCount(int pageItemCount) {
        mPageItemCount = pageItemCount;
    }

    /**
     * 页数
     * @return
     */
    public int getPageCount() {
        if (getAdapter() == null) {
            return 0;
        }

        if (getPageItemCount() <= 0) {
            return 0;
        }
        return (int) Math.ceil((float) getAdapter().getItemCount() / getPageItemCount());
    }

    public boolean isLoop() {
        return isLoop;
    }

    public void setLoop(boolean loop) {
        isLoop = loop;
    }

    @Override
    public void setNumRows(int numRows) {
        mNumRows = numRows;
        super.setNumRows(numRows);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mKeyController.dispatchKeyEvent(event)) {
            return true;
        }

        int selectedPosition = getSelectedPosition();
        if (!mHandKey
                || getAdapter() == null || selectedPosition == NO_POSITION) {
            return super.dispatchKeyEvent(event);
        }
        int pageItemCount = getPageItemCount();

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int count = getAdapter().getItemCount();
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if (pgUp(selectedPosition, count, mNumRows)) {
                        return true;
                    }
                    break;

                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (pgDown(selectedPosition, count, mNumRows)) {
                        return true;
                    }
                    break;

                case KeyEvent.KEYCODE_DPAD_UP:
                    if (mNumRows > 1 && mHandleUpKey) {
                        selectedPosition = selectedPosition - 1;
                        if (selectedPosition < 0) {
                            if (isLoop) {
                                setSelectedPosition(count - 1);
                                return true;
                            }
                            selectedPosition = 0;
                        }
                        setSelectedPosition(selectedPosition);
                        return true;
                    }
                    break;

                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if (mNumRows > 1 && mHandleDownKey) {
                        selectedPosition = selectedPosition + 1;
                        if (selectedPosition == count) {
                            if (isLoop) {
                                setSelectedPosition(0);
                                return true;
                            }
                            selectedPosition = count - 1;
                        }
                        setSelectedPosition(selectedPosition);
                        return true;
                    }
                    break;

                case KeyEvent.KEYCODE_PAGE_DOWN:
                case KeyEvent.KEYCODE_CHANNEL_UP:
                    if (pgUp(selectedPosition, count, pageItemCount)) {
                        return true;
                    }
                    break;

                case KeyEvent.KEYCODE_PAGE_UP:
                case KeyEvent.KEYCODE_CHANNEL_DOWN:
                    if (pgDown(selectedPosition, count, pageItemCount)) {
                        return true;
                    }
                    break;

                default:
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private boolean pgDown(int selectedPosition, int count, int pageItemCount) {
        if (selectedPosition + pageItemCount < count) {
            setSelectedPosition(selectedPosition + pageItemCount);
            return true;
        } else {
            count--;
            if (selectedPosition != count) {
                setSelectedPosition(count);
                return true;
            } else if (isLoop) {
                setSelectedPosition(0);
                return true;
            }
        }
        return false;
    }

    private boolean pgUp(int selectedPosition, int count, int pageItemCount) {
        if (selectedPosition - pageItemCount > 0) {
            setSelectedPosition(selectedPosition - pageItemCount);
            return true;
        } else if (selectedPosition != 0) {
            setSelectedPosition(0);
            return true;
        } else if (isLoop) {
            setSelectedPosition(count - 1);
            return true;
        }
        return false;
    }
}
