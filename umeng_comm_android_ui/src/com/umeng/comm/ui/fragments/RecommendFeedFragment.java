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

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.utils.DeviceUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.activities.SearchActivity;
import com.umeng.comm.ui.presenter.impl.RecommendFeedPresenter;
import com.umeng.comm.ui.utils.BroadcastUtils;

/**
 * feed推荐页面
 */
public class RecommendFeedFragment extends FeedListFragment<RecommendFeedPresenter> {

    @Override
    protected void initViews() {
        super.initViews();
        mPostBtn.setVisibility(View.GONE);
    }

    @Override
    protected RecommendFeedPresenter createPresenters() {
        RecommendFeedPresenter presenter = new RecommendFeedPresenter(this);
        return presenter;
    }

    @Override
    protected void loadMoreFeed() {
        // 没有网络的情况下从数据库加载
        if (!DeviceUtils.isNetworkAvailable(getActivity())) {
            onRefreshEnd();
            mPresenter.loadDataFromDB();
            return;
        }
        mPresenter.fetchNextPageData();
    }

    public void cleanAdapterData() {
        if (mFeedLvAdapter != null) {
            mFeedLvAdapter.getDataSource().clear();
            mFeedLvAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 推荐页面不接受新发布的feed。【不可删除】
     */
    // @Override
    // protected void registerBroadcast() {
    // BroadcastUtils.registerFeedUpdateBroadcast(getActivity(), mReceiver);
    // }

    @Override
    protected void postFeedComplete(FeedItem feedItem) {
        updateForwardCount(feedItem, 1);
    }

    @Override
    public void initAdapter() {
        // 添加Header
        View headerView = LayoutInflater.from(getActivity()).inflate(
                ResFinder.getLayout("umeng_comm_search_header_view"), null);
        headerView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                getActivity().startActivity(intent);
            }
        });
        mFeedsListView.addHeaderView(headerView);
        super.initAdapter();
    }

    @Override
    public void onDestroy() {
        BroadcastUtils.unRegisterBroadcast(getActivity(), mReceiver);
        super.onDestroy();
    }

    /**
     * 主动调用加载数据。 【注意】该接口仅仅在退出登录时，跳转到FeedsActivity清理数据后重新刷新数据</br>
     */
    public void loadDataFromServer() {
        if (mPresenter != null) {
            mPresenter.loadDataFromServer();
        }
    }

}
