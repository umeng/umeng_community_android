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

import android.util.TypedValue;
import android.view.View;

import com.umeng.comm.core.beans.BaseBean;
import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.listeners.Listeners.OnResultListener;
import com.umeng.comm.ui.presenter.impl.PostedFeedPresenter;
import com.umeng.comm.ui.utils.Filter;

import java.util.Iterator;
import java.util.List;

/**
 * 用户已经已经发布的feed页面
 */
public class PostedFeedsFragment extends FeedListFragment<PostedFeedPresenter> {

    private CommUser mUser;

    /**
     * Feed 删除监听器,删除页面时回调给个人信息页面使得feed数量减1 [ TODO : 考虑是否和转发效果一样,使用广播 ]
     */
    private OnDeleteListener mDeleteListener;

    private PostedFeedsFragment(CommUser user) {
        mUser = user;
        setRetainInstance(true);
    }

    @Override
    protected void initViews() {
        super.initViews();
        mPostBtn.setVisibility(View.GONE);
        if (mListener != null) {
            mRefreshLayout.setOnScrollDirectionListener(new OnResultListener() {

                @Override
                public void onResult(int status) {
                    // 1:向上滑动且第一项显示 (up)
                    // 0:向下且大于第一项 (down)
                    int firstVisible = mFeedsListView.getFirstVisiblePosition();
                    int headerCount = mFeedsListView.getHeaderViewsCount();
                    if ((status == 1 && firstVisible >= headerCount)
                            || (status == 0 && firstVisible == headerCount)) {
                        mListener.onResult(status);
                    }
                }
            });
        }
    }

    @Override
    protected PostedFeedPresenter createPresenters() {
        PostedFeedPresenter presenter = new PostedFeedPresenter(this);
        presenter.setId(mUser.id);
        return presenter;
    }

    @Override
    protected void showPostButtonWithAnim() {
    }

    public static PostedFeedsFragment newInstance(CommUser user) {
        return new PostedFeedsFragment(user);
    }

    @Override
    public void initAdapter() {
        // 设置只显示当前用户创建的feeds,过滤掉其他用户的feed
        setFeedFilter(new Filter<FeedItem>() {

            @Override
            public List<FeedItem> doFilte(List<FeedItem> originItems) {
                Iterator<FeedItem> myIterator = originItems.iterator();
                while (myIterator.hasNext()) {
                    final FeedItem feedItem = myIterator.next();
                    // id等于当前用户的id或者列表中已经包含该feed,那么移除该feed
                    if (!feedItem.creator.id.equals(mUser.id)
                            || mFeedLvAdapter.getDataSource().contains(feedItem)) {
                        myIterator.remove();
                    }
                }
                return originItems;
            }
        });

        super.initAdapter();
    }

    @Override
    protected boolean isMyPage(FeedItem feedItem) {
        if (feedItem == null
                || !feedItem.creator.id.equals(mUser.id)) {
            return false;
        }

        return true;
    }

    @Override
    public void onRefreshStart() {
        mRefreshLayout.setProgressViewOffset(false, 0,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources()
                        .getDisplayMetrics()));
        mRefreshLayout.setRefreshing(true);
    }

    /**
     * @param user
     */
    public void setCurrentUser(CommUser user) {
        mUser = user;
    }

    protected void updateAfterDelete(FeedItem feedItem) {
        super.updateAfterDelete(feedItem);
        if (mDeleteListener != null) {
            mDeleteListener.onDelete(feedItem);
        }
    };

    public void setOnDeleteListener(OnDeleteListener listener) {
        mDeleteListener = listener;
    }

    @Override
    protected boolean isMyFeedList() {
        return mUser != null && mUser.id.equals(CommConfig.getConfig().loginedUser.id);
    }

    /**
     * 删除监听器
     * 
     * @author mrsimple
     */
    public static interface OnDeleteListener {
        public void onDelete(BaseBean item);
    }

    public void setOnAnimationResultListener(OnResultListener listener) {
        mListener = listener;
    }

    private OnResultListener mListener = null;
    
    public void executeScrollToTop(){
        if ( mFeedsListView.getFirstVisiblePosition() > 10 ) {
            mFeedsListView.smoothScrollToPosition(0);
        }
    }

}
