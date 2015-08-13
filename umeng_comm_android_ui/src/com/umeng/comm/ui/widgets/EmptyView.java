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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.umeng.comm.core.utils.ResFinder;

/**
 * 在没有数据的时候显示空视图
 */
public class EmptyView extends RelativeLayout {

    private ViewStub mStub;
    private TextView mTextView;
    private String mText;
    
    public EmptyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setContentView(context);
    }

    public EmptyView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setContentView(context);
    }
    
    /**
     * 
     * 添加子视图</br>
     * @param context
     */
    private void setContentView(Context context){
        View view = LayoutInflater.from(context).inflate(ResFinder.getLayout("umeng_comm_empty_view"), null);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(view,params);
        setVisibility(View.GONE);
    }
    
    /**
     * 
     * 设置显示空视图时的显示文本</br>
     * @param textResName 文本资源名字
     */
    public void setShowText(String textResName){
        this.mText = ResFinder.getString(textResName);
    }
    
    /**
     * 
     * 显示无数据时的提示</br>
     */
    public void show(){
        setVisibility(View.VISIBLE);// 设置当前跟视图可见
        if ( mStub == null ) {
            mStub = (ViewStub) getChildAt(0);
        }
        if ( mTextView == null ) {
            mTextView = (TextView) mStub.inflate();
            mTextView.setVisibility(View.VISIBLE);
            mTextView.setText(mText);
        }
    }
    
    /**
     * 
     * 隐藏空视图</br>
     */
    public void hide(){
        setVisibility(View.GONE);
    }

}
