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

package com.umeng.comm.ui.utils;

import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.umeng.comm.core.beans.CommConfig;

public final class FontUtils {
    /**
     * 为所有textView类型和其子类型修改字体
     */
    public static void changeTypeface(View rootView) {
        Typeface typeface = CommConfig.getConfig().getTypeface();
        if (typeface == null || !(rootView instanceof ViewGroup)) {
            return;
        }

        ViewGroup rootViewGroup = (ViewGroup) rootView;
        // 迭代所有View, 如果是TextView的子类就修改字体
        int childCount = rootViewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = rootViewGroup.getChildAt(i);
            if (view instanceof ViewGroup) {
                changeTypeface(view);
            } else if (view instanceof TextView) {
                ((TextView) view).setTypeface(typeface);
            }
        }

    }
}
