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
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.widget.TextView;

import com.umeng.comm.core.utils.ResFinder;

/**
 * 
 */
public class TopicTipView extends TextView {

    private Path mPath = new Path();
    private int mWidth = 0;
    private float mFontHeiht = 0;
    private Paint mPaint = new Paint();
    
    /**
     * @param context
     */
    public TopicTipView(Context context) {
        super(context);
        init();
    }

    /**
     * @param context
     * @param attrs
     */
    public TopicTipView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public TopicTipView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    private void init(){
        mWidth = (int) getPaint().measureText(getText().toString());
        FontMetrics fm = getPaint().getFontMetrics();
        mFontHeiht = (float)Math.ceil(fm.descent - fm.ascent);
        int horizontalPadding = getHorizontalPaddint();
        int verticalPadding = getVerticalPaddint();
        mPath.reset();
        mPath.moveTo(0, 0);
        mPath.lineTo(mWidth + horizontalPadding, 0);
        mPath.lineTo(mWidth + horizontalPadding, mFontHeiht + verticalPadding);
        mPath.lineTo(25, mFontHeiht + verticalPadding);
        mPath.lineTo(0, mFontHeiht+verticalPadding+25);
        mPath.close();
        
        mPaint.setAntiAlias(true);
        mPaint.setColor(ResFinder.getColor("umeng_comm_topic_tip_bg"));
        mPaint.setStyle(Style.FILL);
        
        getPaint().setColor(Color.WHITE);// 不知道什么原因，xml设置无效。暂时先在代码中设置。
    }
    
    private int getHorizontalPaddint(){
        return getPaddingLeft() + getPaddingRight();
    }
    
    private int getVerticalPaddint(){
        return getPaddingBottom() + getPaddingTop();
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int with = getHorizontalPaddint() + mWidth;
        int height = (int)(getVerticalPaddint() + mFontHeiht + 25);
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(with, MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    
    
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(mPath, mPaint);
        canvas.drawText(getText().toString(), getPaddingLeft(), mFontHeiht + getPaddingTop() / 2, getPaint());
    }

}
