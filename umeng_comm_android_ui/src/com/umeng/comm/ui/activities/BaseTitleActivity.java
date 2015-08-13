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

package com.umeng.comm.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.umeng.comm.core.utils.ResFinder;

/**
 * 含有Title的Fragment Activity
 * 
 * @author mrsimple
 */
public abstract class BaseTitleActivity extends BaseFragmentActivity {
    protected ImageView mTitleBackBtn;
    protected TextView mTitleTextView;
    protected Button mTitleRightBtn;

    @Override
    protected final void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        // 设置布局
        setContentView(getContentView());
        // 初始化title区域
        initTitleLayout();
        initFragment();
    }

    /**
     * 返回布局id
     * 
     * @return
     */
    protected abstract int getContentView();

    /**
     * 初始化Title区域，含有一个返回按钮、文本区域、设置按钮
     */
    protected void initTitleLayout() {
        mTitleBackBtn = findViewByIdWithFinder(ResFinder.getId("umeng_comm_setting_back"));
        mTitleBackBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTitleTextView = findViewByIdWithFinder(ResFinder.getId("umeng_comm_setting_title"));
        mTitleRightBtn = findViewByIdWithFinder(ResFinder.getId("umeng_comm_save_bt"));
    }

    /**
     * 初始化Fragment
     */
    protected void initFragment() {

    }

}
