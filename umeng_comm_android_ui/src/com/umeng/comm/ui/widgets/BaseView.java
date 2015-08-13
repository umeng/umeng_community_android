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
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.umeng.comm.core.utils.ResFinder;

/**
 * 包含加载中、加载失败视图的FrameLayout
 */
public class BaseView extends FrameLayout {

    private TextView mEmptyView; // 此处empty view暂时使用TextView来充当
    private View mLoadingView;
    private ViewStub mEmptyViewStub;
    private ViewStub mLoadingViewStub;
    private View mRootView;
    private OnClickListener mListener;
    private String mText;

    /**
     * @param context
     */
    public BaseView(Context context) {
        super(context);
        addEmptyView();
    }

    /**
     * @param context
     * @param attrs
     */
    public BaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        addEmptyView();
    }

    /**
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public BaseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        addEmptyView();
    }

    /**
     * show empty on screen</br>
     */
    public void showEmptyView() {
        if (mEmptyView == null) {
            mEmptyView = (TextView) mEmptyViewStub.inflate();
            if (mListener != null) {
                mEmptyView.setOnClickListener(mListener);
            }
            if (!TextUtils.isEmpty(mText)) {
                mEmptyView.setText(mText);
            }
        }
        mRootView.setVisibility(View.VISIBLE);
        mEmptyView.setVisibility(View.VISIBLE);
        if (mLoadingView != null) {
            mLoadingView.setVisibility(View.GONE);
        }
    }

    public void setEmptyViewText(String text) {
        mText = text;
    }

    /**
     * hide empty view and then show customer data</br>
     */
    public void hideEmptyView() {
        if (mRootView == null || mRootView.getVisibility() == View.GONE) {
            return;
        }
        resetViewStatus();
    }

    /**
     * show loading progressbar when load data</br>
     */
    public void showLoadingView() {
        if (mLoadingView == null) {
            mLoadingView = mLoadingViewStub.inflate();
        }
        mRootView.setVisibility(View.VISIBLE);
        mLoadingView.setVisibility(View.VISIBLE);
        if (mEmptyView != null) {
            mEmptyView.setVisibility(View.GONE);
        }
    }

    /**
     * hide loading progressbar when load finish</br>
     */
    public void hideLoadingView() {
        if (mRootView.getVisibility() == View.GONE) {
            return;
        }
        resetViewStatus();
    }

    /**
     * reset views to gone</br>
     */
    private void resetViewStatus() {
        if (mRootView.getVisibility() == View.GONE) {
            return;
        }
        mRootView.setVisibility(View.GONE);
        if (mEmptyView != null) {
            mEmptyView.setVisibility(View.GONE);
        }
        if (mLoadingView != null) {
            mLoadingView.setVisibility(View.GONE);
        }
    }

    /**
     * set clickable event for empty view . it's usefull when loading failed and
     * repeat load</br>
     * 
     * @param listener
     */
    public void setEmptyViewEvent(OnClickListener listener) {
        this.mListener = listener;
    }

    /**
     * add view</br>
     */
    private void addEmptyView() {
        if (mRootView == null) {
            mRootView = LayoutInflater.from(getContext()).inflate(
                    ResFinder.getLayout("umeng_comm_base_view"),
                    null);
            mEmptyViewStub = (ViewStub) mRootView.findViewById(ResFinder
                    .getId("umeng_comm_base_empty_viewstub"));
            mLoadingViewStub = (ViewStub) mRootView.findViewById(ResFinder
                    .getId("umeng_comm_base_loading_viewstub"));
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            addViewInLayout(mRootView, -1, params);
            mRootView.setVisibility(View.GONE);
            return;
        }
    }

    @Override
    protected void attachViewToParent(View child, int index,
            android.view.ViewGroup.LayoutParams params) {
        super.attachViewToParent(child, index, params);
    }
}
