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

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridView;

import com.umeng.comm.core.constants.Constants;

/**
 * 解决ScrollView嵌套GridView(ListView方法同理)显示不全的问题
 */
public class WrapperGridView extends GridView {

    public boolean hasScrollBar = true;

    /**
     * @param context
     */
    public WrapperGridView(Context context) {
        this(context, null);
    }

    public WrapperGridView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public WrapperGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int expandSpec = heightMeasureSpec;
        if (hasScrollBar) {
            expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                    MeasureSpec.AT_MOST);
            super.onMeasure(widthMeasureSpec, expandSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

    }

    /**
     * 根据Child数量来设置列数,如果child小于3,那么列数就是child的数量;否则列数为3
     * 
     * @param maxColumn
     */
    public void updateColumns(int maxColumn) {
        int childCount = this.getAdapter().getCount();
        if (childCount > 0) {
            // 列数
            int columns = childCount < maxColumn ? childCount % maxColumn : maxColumn;
            // 设置列数
            this.setNumColumns(columns);
        }

        //
        if (childCount == 1) {
            this.setLayoutParams(new FrameLayout.LayoutParams(Constants.SCREEN_WIDTH / 3,
                    Constants.SCREEN_HEIGHT / 3));
        } else {
            this.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }
}
