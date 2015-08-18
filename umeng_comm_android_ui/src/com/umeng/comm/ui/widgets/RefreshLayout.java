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
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

import com.umeng.comm.core.listeners.Listeners.OnResultListener;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ResFinder.ResType;

/**
 * 继承自SwipeRefreshLayout,从而实现滑动到底部时上拉加载更多的功能. 注意 :
 * 在下拉刷新完成时需要调用RefreshLayout的setRefreshing(false)方法来停止刷新过程；
 * 在上拉加载更多完成时需要调用setLoading(false)来标识加载完成。
 */
public abstract class RefreshLayout<T extends AbsListView> extends SwipeRefreshLayout implements
        OnScrollListener {

    /**
     * 滑动到最下面时的上拉操作
     */
    private int mTouchSlop;

    /**
     * 
     */
    protected T mAbsListView;

    /**
     * ListView滚动监听器,用于外部
     */
    private OnScrollListener mListViewOnScrollListener;

    private OnResultListener mScrollListener;

    /**
     * 上拉监听器, 到了最底部的上拉加载操作
     */
    private OnLoadListener mOnLoadListener;
    /**
     * 按下时的y坐标
     */
    protected int mYDown;
    /**
     * 抬起时的y坐标, 与mYDown一起用于滑动到底部时判断是上拉还是下拉
     */
    protected int mLastY;
    /**
     * 是否在加载中 ( 上拉加载更多 )
     */
    protected boolean isLoading = false;

    private int mColor1;
    private int mColor2;
    private int mColor3;
    private int mColor4;

    /**
     * ListView的加载中footer
     */
    protected View mFooterView;

    private int mScrollDirection = 0; // 0:代表向下滚动；1：向上滚动

    /**
     * @param context
     */
    public RefreshLayout(Context context) {
        this(context, null);
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (isInEditMode()) {
            return;
        }

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        // 获取color资源的id
        mColor1 = ResFinder.getResourceId(ResType.COLOR, "umeng_comm_lv_header_color1");
        mColor2 = ResFinder.getResourceId(ResType.COLOR, "umeng_comm_lv_header_color2");
        mColor3 = ResFinder.getResourceId(ResType.COLOR, "umeng_comm_lv_header_color3");
        mColor4 = ResFinder.getResourceId(ResType.COLOR, "umeng_comm_lv_header_color4");

    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        if (isInEditMode()) {
            return;
        }
        super.onLayout(changed, left, top, right, bottom);

        // 初始化ListView对象
        if (mAbsListView == null) {
            getRefreshView();
        }

        // 设置颜色
        this.setColorScheme(mColor1, mColor2, mColor3, mColor4);
    }

    /**
     * 获取ListView对象
     */
    @SuppressWarnings("unchecked")
    protected void getRefreshView() {
        int childs = getChildCount();
        if (childs <= 0) {
            return;
        }
        View childView = null;
        for (int i = 0; i < childs; i++) {
            childView = getChildAt(i);
            if (childView instanceof AbsListView) {
                mAbsListView = (T) childView;
                // 设置滚动监听器给ListView, 使得滚动的情况下也可以自动加载
                mAbsListView.setOnScrollListener(this);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public T findRefreshViewById(int id) {
        mAbsListView = (T) this.findViewById(id);
        mAbsListView.setOnScrollListener(this);
        return mAbsListView;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        try {
            return dealTouchEvent(event);
        } catch (IllegalStateException exception) {
        }
        return true;
    }

    private boolean dealTouchEvent(MotionEvent event) {
        final int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // 按下
                mYDown = (int) event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                // 移动
                mLastY = (int) event.getRawY();
                break;

            case MotionEvent.ACTION_UP:
                // 抬起
                if (canLoad()) {
                    loadData();
                }
                if (mScrollListener != null) {
                    if (isPullUp()) {
                        mScrollDirection = 1;
                    } else {
                        mScrollDirection = 0;
                    }
                }
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * 是否可以加载更多, 条件是到了最底部, listview不在加载中, 且为上拉操作.
     * 
     * @return
     */
    private boolean canLoad() {
        return isBottom() && !isLoading && isPullUp() && mOnLoadListener != null;
    }

    /**
     * 判断是否到了最底部
     */
    private boolean isBottom() {
        if (mAbsListView == null) {
            getRefreshView();
        }
        // 已经到最后一项且可见的第一项>0
        if (mAbsListView != null && mAbsListView.getAdapter() != null) {
            int childCount = mAbsListView.getAdapter().getCount();
            return childCount > 1
                    && mAbsListView.getLastVisiblePosition() == childCount - 1;
            // && mAbsListView.getFirstVisiblePosition() >= 0;
        }
        return false;
    }

    /**
     * 是否是上拉操作
     * 
     * @return
     */
    private boolean isPullUp() {
        return mLastY > 0 && (mYDown - mLastY) >= mTouchSlop * 3;
    }

    /**
     * 如果到了最底部,而且是上拉操作.那么执行onLoad方法
     */
    protected void loadData() {
        setLoading(true);
        mOnLoadListener.onLoad();
    }

    /**
     * @param loading
     */
    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    public AbsListView getListView() {
        if (mAbsListView == null) {
            getRefreshView();
        }
        return mAbsListView;
    }

    /**
     * 使外部可以监听到listview的滚动
     * 
     * @param listener
     */
    public void addOnScrollListener(OnScrollListener listener) {
        mListViewOnScrollListener = listener;
    }

    /**
     * @param loadListener
     */
    public void setOnLoadListener(OnLoadListener loadListener) {
        mOnLoadListener = loadListener;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // 回调给外部的监听器
        if (mListViewOnScrollListener != null) {
            mListViewOnScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {

        // 回调给外部的监听器
        if (mListViewOnScrollListener != null) {
            mListViewOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount,
                    totalItemCount);
        }

        if (mScrollListener != null) {
            mScrollListener.onResult(mScrollDirection);
        }

        // 滚动时到了最底部也可以加载更多
        if (canLoad()) {
            loadData();
        }
    }

    /**
     * 加载更多的监听器
     * 
     * @author mrsimple
     */
    public static interface OnLoadListener {
        public void onLoad();
    }

    public void setOnScrollDirectionListener(OnResultListener listener) {
        mScrollListener = listener;
    }
}
