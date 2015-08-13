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
import android.widget.ListView;

import com.umeng.comm.ui.adapters.CommonAdapter;

/**
 * 解决了ListView嵌套在ScrollView或者带滚动条的ViewGroup显示不全定位问题.
 */
public class WrapperListView extends ListView {
    /**
     * 是否嵌套在含有滚动条的ViewGroup中
     */
    public boolean hasScrollBar = false;
    CommonAdapter<?,?> mAdapter = null;

    /**
     * @param context
     */
    public WrapperListView(Context context) {
        this(context, null);
    }

    public WrapperListView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public WrapperListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (hasScrollBar) {
            int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                    MeasureSpec.AT_MOST);
            super.onMeasure(widthMeasureSpec, expandSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    public void setAdapter(android.widget.ListAdapter adapter) {
        super.setAdapter(adapter);
        if (adapter instanceof CommonAdapter<?,?>) {
            mAdapter = (CommonAdapter<?,?>) adapter;
        }
    };

    /**
     * @param adapter
     */
    @SuppressWarnings("rawtypes")
    public void setCommAdapter(CommonAdapter adapter) {
        super.setAdapter(adapter);
        mAdapter = adapter;
    }

    @SuppressWarnings("rawtypes")
    public CommonAdapter getCommAdapter() {
        return mAdapter;
    }
}
