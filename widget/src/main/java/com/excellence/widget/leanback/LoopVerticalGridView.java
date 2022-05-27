package com.excellence.widget.leanback;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.KeyEvent;

import com.excellence.basetoolslibrary.utils.KeyController;
import com.excellence.widget.R;

/**
 * <pre>
 *     author : VeiZhang
 *     blog   : http://tiimor.cn
 *     time   : 2020/5/11
 *     desc   : 垂直循环和翻页处理
 *     important: 如果需要对列表的上下键值做额外的处理,不要用这个循环功能,因为up/down按键已经被拦截,外层无法监听到
 * </pre> 
 */
public class LoopVerticalGridView extends ScrollbarVerticalGridView {

    /**
     * 一页的个数
     */
    private int mPageItemCount = 0;
    /**
     * 是否循环
     */
    private boolean isLoop = true;

    /**
     * 有时候需要屏蔽循环key事件，则设置false。总开关
     */
    private boolean mHandKey = true;
    /**
     * 多列时，有时候左右使用系统处理，不需要额外处理时，默认true：额外处理，循环滚动；false：系统处理
     */
    private boolean mHandleLeftKey = true;
    private boolean mHandleRightKey = true;

    private KeyController mKeyController = null;

    public LoopVerticalGridView(Context context) {
        this(context, null);
    }

    public LoopVerticalGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoopVerticalGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoopVerticalGridView);
        mPageItemCount = typedArray.getInteger(R.styleable.LoopVerticalGridView_pageItemCount, mPageItemCount);
        isLoop = typedArray.getBoolean(R.styleable.LoopVerticalGridView_loop, isLoop);

        mHandKey = typedArray.getBoolean(R.styleable.LoopVerticalGridView_handleKey, mHandKey);
        mHandleLeftKey = typedArray.getBoolean(R.styleable.LoopVerticalGridView_handleLeftKey, mHandleLeftKey);
        mHandleRightKey = typedArray.getBoolean(R.styleable.LoopVerticalGridView_handleRightKey, mHandleRightKey);
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
                case KeyEvent.KEYCODE_DPAD_UP:
                    if (keyUp(selectedPosition, count, mNumColumns)) {
                        return true;
                    }
                    break;

                case KeyEvent.KEYCODE_DPAD_DOWN:
                    if (keyDown(selectedPosition, count, mNumColumns)) {
                        return true;
                    }
                    break;

                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if (mNumColumns > 1 && mHandleLeftKey) {
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

                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (mNumColumns > 1 && mHandleRightKey) {
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

                case KeyEvent.KEYCODE_PAGE_UP:
                case KeyEvent.KEYCODE_CHANNEL_UP:
                    if (pgUp(selectedPosition, count, pageItemCount)) {
                        return true;
                    }
                    break;

                case KeyEvent.KEYCODE_PAGE_DOWN:
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

    private boolean keyUp(int selectedPosition, int count, int numColumns) {
        int lastPos = count - 1;
        /**
         * 取最大 <= firstLine 是第一行
         */
        int firstLine = numColumns - 1;
        /**
         * 取最小 >= lastLine 是最后一行
         */
        int lastLine = lastPos - lastPos % numColumns;
        if (selectedPosition <= firstLine) {
            if (isLoop) {
                selectedPosition = Math.min(lastLine + selectedPosition, lastPos);
                setSelectedPosition(selectedPosition);
                return true;
            }
        }

        /**
         * 手动设置
         */
        selectedPosition -= numColumns;
        if (selectedPosition < 0 && isLoop) {
            selectedPosition = lastPos;
            setSelectedPosition(selectedPosition);
            return true;
        }
        return false;
    }

    private boolean keyDown(int selectedPosition, int count, int numColumns) {
        int lastPos = count - 1;
        int lastLine = lastPos - lastPos % numColumns;
        if (selectedPosition >= lastLine) {
            if (isLoop) {
                selectedPosition = Math.min(selectedPosition % numColumns, lastPos);
                setSelectedPosition(selectedPosition);
                return true;
            }
        }

        /**
         * 手动设置
         */
        selectedPosition += numColumns;
        if (selectedPosition > lastPos && isLoop) {
            selectedPosition = 0;
            setSelectedPosition(selectedPosition);
            return true;
        }
        return false;
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
