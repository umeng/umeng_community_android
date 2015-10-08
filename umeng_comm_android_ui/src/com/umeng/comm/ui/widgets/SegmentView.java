/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Umeng, Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.umeng.comm.ui.widgets;

import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.umeng.comm.core.utils.DeviceUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ResFinder.ResType;

/**
 * SegmentView是一个类似于iOS的segment
 * control显示效果的一个控件，使用RadioGroup与RadioButton实现，使用时用户需要调用{@link #setTabs(List)}
 * 方法设置tabs,然后调用 {@link #setOnItemCheckedListener(OnItemCheckedListener)}
 * 设置点击每个tab时的回调函数.
 */
public class SegmentView extends RadioGroup {
    Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    /**
     * 默认的RadioButton id,从0 开始
     */
    int ids = 0;
    /**
     * RadioButton的数量
     */
    int childCount = 0;
    /**
     * 绘制分割线时的padding值
     */
    int linePadding = 0;

    int lastSelectedPos = 0;
    /**
     * 选中回调
     */
    private OnItemCheckedListener mCheckedListener;

    public SegmentView(Context context) {
        this(context, null);
    }

    public SegmentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        mPaint.setColor(ResFinder.getColor("umeng_comm_radio_stroke_color"));
        float strokeWidth = DeviceUtils.dp2px(getContext(), 0.5f);
        mPaint.setStrokeWidth(strokeWidth);
        setupOnItemClickListener();
    }

    boolean isShowBadge = false;

    public void showBadge(boolean show) {
        isShowBadge = show;
    }

    private void setupOnItemClickListener() {
        super.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // 包装回调
                if (mCheckedListener != null) {
                    RadioButton checkedButton = (RadioButton) findViewById(checkedId);
                    mCheckedListener.onCheck(checkedButton, checkedId, checkedButton.getText()
                            .toString());
                }
            }
        });
    }

    public void setTabs(String[] tabTitles) {
        removeAllViews();
        int i = 0;
        for (String title : tabTitles) {
            addTab(title, i);
            i++;
        }
    }

    public BadgeRadioButton getRadioButton(int childIndex) {
        if (childIndex < 0 || childIndex >= getChildCount()) {
            throw new RuntimeException("从" + getClass().getSimpleName() + "获取子视图越界啦! 最大index为 "
                    + (getChildCount() - 1));
        }

        return (BadgeRadioButton) getChildAt(childIndex);
    }

    public void selectItemIndex(int position) {
        if (position < 0 || position > childCount) {
            Log.e(VIEW_LOG_TAG, "### selectItemIndex 无效索引, childcount = " + childCount
                    + ", position = " + position);
            return;
        }
        RadioButton radioButton = (RadioButton) getChildAt(position);
        radioButton.setChecked(true);
    }

    public void addTab(String title, int pos) {
        BadgeRadioButton radioButton = (BadgeRadioButton) LayoutInflater.from(getContext())
                .inflate(
                        ResFinder.getLayout("umeng_comm_radio_button_item"), this, false);
        radioButton.setId(ids++);
        radioButton.setText(title);
        radioButton.setShowBadge(isShowBadge);
        if (pos != 0 && pos != childCount - 1) {
            radioButton.setBackgroundResource(ResFinder.getResourceId(ResType.DRAWABLE,
                    "umeng_comm_segment_shape_middle"));
        }
        // 添加到当前ViewGroup中
        this.addView(radioButton);
    }

    /**
     * 绘制tab之间的分割线
     * 
     * @param canvas
     */
    private void drawSeparateLines(Canvas canvas) {
        childCount = getChildCount();
        if (childCount == 0) {
            throw new IllegalArgumentException("SegmentView's child is zero !");
        }

        if (getOrientation() == HORIZONTAL) {
            int childWidth = this.getWidth() / childCount;
            for (int i = 1; i < childCount; i++) {
                int startX = childWidth * i;
                canvas.drawLine(startX, linePadding, startX, this.getHeight()
                        - linePadding, mPaint);
            }
        } else {
            int childHeight = this.getHeight() / childCount;
            for (int i = 1; i < childCount; i++) {
                int startY = childHeight * i;
                canvas.drawLine(linePadding, startY, this.getWidth() - linePadding, startY, mPaint);
            }
        }

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        drawSeparateLines(canvas);
    }

    public void setOnItemCheckedListener(OnItemCheckedListener listener) {
        mCheckedListener = listener;
    }

    /*
     * 使用 @see setOnItemCheckedListener 来设置回调
     * @see android.widget.RadioGroup#setOnCheckedChangeListener(android.widget.
     * RadioGroup.OnCheckedChangeListener)
     */
    @Deprecated
    @Override
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
    }

    /**
     * tab点击时的回调接口类
     * 
     * @author mrsimple
     */
    public static interface OnItemCheckedListener {
        /**
         * @param button 被选中的按钮
         * @param position 被选中的按钮所在的位置
         * @param title 被选中的按钮的文本,即标题
         */
        public void onCheck(RadioButton button, int position, String title);
    }

}
