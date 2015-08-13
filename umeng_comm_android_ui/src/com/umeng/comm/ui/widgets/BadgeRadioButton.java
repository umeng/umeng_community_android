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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.RadioButton;

import com.umeng.comm.core.utils.DeviceUtils;

/**
 * 该视图继承自RadioButton
 */
public class BadgeRadioButton extends RadioButton {
    Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    /**
     * 是否显示红点
     */
    protected boolean isShowBadge = false;
    /**
     * 是否已经显示过
     */
    protected boolean isShowed = false;

    public BadgeRadioButton(Context context) {
        this(context, null);
    }

    public BadgeRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint.setColor(Color.RED);
    }

    public BadgeRadioButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mPaint.setColor(Color.RED);
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        invalidate();
        if (!isShowed && checked) {
            isShowed = true;
        }
    }

    public void setShowBadge(boolean show) {
        this.isShowBadge = show;
        invalidate();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (isShowBadge && !isChecked() && !isShowed) {
            canvas.drawCircle(getWidth() - 20, getTop() + 15, DeviceUtils.dp2px(getContext(), 4),
                    mPaint);
        }

    }
}
