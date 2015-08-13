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
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * 解决开发者使用ViewPager的方式并使用Fragment的方式集成，ViewPager嵌套ViewPager的滑动事件冲突
 */
public class CommunityViewPager extends ViewPager {

    private ViewPager mSubViewPager;

    /**
     * @param context
     */
    public CommunityViewPager(Context context) {
        super(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public CommunityViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean canScroll(View v, boolean arg1, int dx, int arg3, int arg4) {
        if (v != this && v instanceof ViewPager) {
            // 处于社区的viewpager，且在“所有”页面且向做滑动操作
            if (mSubViewPager != null && dx > 0 && (mSubViewPager.getCurrentItem() == 0)) {
                return false;
            } else if (mSubViewPager != null
                    && dx < 0
                    && (mSubViewPager.getCurrentItem() == mSubViewPager.getAdapter().getCount() - 1)) {
                // 处于社区的viewpager，且在“所有”页面且向做滑动操作
                return false;
            }
        }
        return super.canScroll(v, arg1, dx, arg3, arg4);
    }

}
