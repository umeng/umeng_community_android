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

package com.umeng.comm.ui.fragments;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;

import com.umeng.comm.ui.presenter.impl.FeedListPresenter;

/**
 * 含有Feed发布按钮下拉隐藏、上拉显示效果的Feed 列表Fragment
 *
 * @param <P>
 */
public abstract class PostBtnAnimFragment<P extends FeedListPresenter> extends FeedListFragment<P> {

    private static final int STATUS_NORMAL = 0x01;// 正常状态。无意义
    private static final int STATUS_SHOW = 0x02;// 显示状态
    private static final int STATUS_DISMISS = 0x03;// 隐藏状态

    private int mLastScrollY = 0;// 上次滑动时Y的起始坐标
    private int mSlop;
    private transient int currentStatus = STATUS_NORMAL; // 当前Float Button的状态
    private transient boolean isExecutingAnim = false; // 是否正在执行动画

    @Override
    protected void initRefreshView() {
        super.initRefreshView();
        mFeedsListView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                checkWhetherExecuteAnimation(event);
                if (mCommentLayout.isShown()) {
                    hideCommentLayoutAndInputMethod();
                    return true;
                }
                return false;
            }
        });
        mSlop = ViewConfiguration.get(getActivity()).getScaledTouchSlop();
    }

    /**
     * 检查是否为Float button执行动画</br>
     * 
     * @param event
     */
    private void checkWhetherExecuteAnimation(MotionEvent event) {
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastScrollY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaY = mLastScrollY - y;
                mLastScrollY = y;
                if (Math.abs(deltaY) < mSlop) {
                    return;
                }
                if (deltaY > 0) {
                    executeAnimation(false);
                } else {
                    executeAnimation(true);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 为Float button执行动画</br>
     * 
     * @param show 显示 or 隐藏
     */
    private void executeAnimation(final boolean show) {

        if (isListViewEmpty()) {
            return;
        }

        if (isExecutingAnim || (show && currentStatus == STATUS_SHOW)
                || (!show && currentStatus == STATUS_DISMISS)) {
            return;
        }
        isExecutingAnim = true;
        int moveDis = ((FrameLayout.LayoutParams) (mPostBtn.getLayoutParams())).bottomMargin
                + mPostBtn.getHeight();
        Animation animation = null;
        if (show) {
            animation = new TranslateAnimation(0, 0, moveDis, 0);
        } else {
            animation = new TranslateAnimation(0, 0, 0, moveDis);
        }
        animation.setDuration(300);
        animation.setFillAfter(true);
        animation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isExecutingAnim = false;
                if (show) {
                    currentStatus = STATUS_SHOW;
                } else {
                    currentStatus = STATUS_DISMISS;
                }
                // 对于3.0以下系统，原来的地方仍有点击事件。由于我们的需要是处理可见性，因此此处不在对Float
                // Button做layout处理。
                mPostBtn.setClickable(show);
            }
        });
        mPostBtn.startAnimation(animation);
    }

    private boolean isListViewEmpty() {
        int count = mFeedsListView.getAdapter().getCount();
        int otherCount = mFeedsListView.getFooterViewsCount()
                + mFeedsListView.getHeaderViewsCount();
        // listview中没有数据时不隐藏发布按钮
        return count == otherCount;
    }
}
