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

package com.umeng.comm.ui.adapters.viewholders;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.umeng.comm.ui.utils.FontUtils;
import com.umeng.comm.ui.utils.ViewFinder;

/**
 * ViewHolder抽象类,用于将ListView等列表视图的ViewHolder抽象画
 */
public abstract class ViewHolder implements ViewParser {
    public View itemView;
    protected Context mContext;
    protected ViewFinder mViewFinder;

    @Override
    public final View inflate(Context context, ViewGroup parent, boolean attachToRoot) {
        mContext = context;
        itemView = LayoutInflater.from(context).inflate(getItemLayout(), parent, attachToRoot);
        // 设置tag
        itemView.setTag(this);
        mViewFinder = new ViewFinder(itemView);
        initWidgets();
        FontUtils.changeTypeface(itemView);
        return itemView;
    }

    /**
     * 获取ItemView的布局Id
     * 
     * @return Item View布局
     */
    protected abstract int getItemLayout();

    /**
     * 初始化各个子视图
     */
    protected void initWidgets() {

    }

    public <T extends View> T findViewById(int id) {
        return mViewFinder.findViewById(id);
    }

}
