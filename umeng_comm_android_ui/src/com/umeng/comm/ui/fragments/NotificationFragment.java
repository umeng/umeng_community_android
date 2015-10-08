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

import java.util.List;

import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.widget.ListView;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.Notification;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.adapters.NotifyAdapter;
import com.umeng.comm.ui.mvpview.MvpNotifyView;
import com.umeng.comm.ui.presenter.impl.NotificationPresenter;
import com.umeng.comm.ui.widgets.RefreshLayout.OnLoadListener;
import com.umeng.comm.ui.widgets.RefreshLvLayout;

/**
 * 消息通知Fragment
 */
public class NotificationFragment extends BaseFragment<List<Notification>, NotificationPresenter>
        implements MvpNotifyView {

    ListView mListView;
    NotifyAdapter mAdapter;
    RefreshLvLayout mRefreshLayout;

    @Override
    protected int getFragmentLayout() {
        return ResFinder.getLayout("umeng_comm_notify_fragment");
    }

    @Override
    protected NotificationPresenter createPresenters() {
        return new NotificationPresenter(this);
    }

    @Override
    protected void initWidgets() {

        mRefreshLayout = findViewById(ResFinder.getId("umeng_comm_swipe_layout"));
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                mPresenter.loadDataFromServer();
            }
        });
        mRefreshLayout.setOnLoadListener(new OnLoadListener() {
            @Override
            public void onLoad() {
                mPresenter.loadMoreData();
            }
        });
        // 添加footer
        mRefreshLayout.setDefaultFooterView();
        mListView = findViewById(ResFinder.getId("umeng_comm_notify_listview"));

        mAdapter = new NotifyAdapter(getActivity());
        mListView.setAdapter(mAdapter);
    }

    @Override
    public void onRefreshEnd() {
        mRefreshLayout.setRefreshing(false);
        mRefreshLayout.setLoading(false);
    }

    @Override
    public List<Notification> getBindDataSource() {
        return mAdapter.getDataSource();
    }

    @Override
    public void notifyDataSetChange() {
        mAdapter.notifyDataSetChanged();
        int total = CommConfig.getConfig().mMessageCount.unReadTotal;
        int unReadNotice = CommConfig.getConfig().mMessageCount.unReadNotice;
        CommConfig.getConfig().mMessageCount.unReadTotal = total - unReadNotice;
        CommConfig.getConfig().mMessageCount.unReadNotice = 0;
    }

    @Override
    public void onRefreshStart() {
    }

}
