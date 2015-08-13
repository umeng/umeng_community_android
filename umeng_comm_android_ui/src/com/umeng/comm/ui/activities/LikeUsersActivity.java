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

package com.umeng.comm.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.Like;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.constants.HttpProtocol;
import com.umeng.comm.core.listeners.Listeners;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.adapters.LikeUserAdapter;
import com.umeng.comm.ui.mvpview.MvpLikeUserView;
import com.umeng.comm.ui.presenter.impl.LikeUserActivityPresenter;
import com.umeng.comm.ui.widgets.RefreshLayout.OnLoadListener;
import com.umeng.comm.ui.widgets.RefreshLvLayout;

import java.util.List;

/**
 * 点赞用户页面
 */
public class LikeUsersActivity extends BaseFragmentActivity implements MvpLikeUserView {

    private ListView mListView;
    private RefreshLvLayout mRefreshLvLayout;
    private String mFeedId = "";
    private LikeUserAdapter mAdapter;
    private LikeUserActivityPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ResFinder.getLayout("umeng_comm_like_user_activity"));
        List<CommUser> users = getIntent().getParcelableArrayListExtra(Constants.TAG_USERS);
        mRefreshLvLayout = (RefreshLvLayout) findViewById(ResFinder
                .getId("umeng_comm_feed_refresh_layout"));
        mRefreshLvLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                mPresenter.loadLikeUserFromServer();
            }
        });

        mRefreshLvLayout.setOnLoadListener(new OnLoadListener() {

            @Override
            public void onLoad() {
                mPresenter.loadMoreLikeUser();
            }
        });

        mFeedId = getIntent().getStringExtra(HttpProtocol.FEED_ID_KEY);
        // 初始化Presenter
        mPresenter = new LikeUserActivityPresenter(this, mFeedId);
        mPresenter.attach(getApplicationContext());
        // 获取下一页地址
        String nextUrl = getIntent().getStringExtra(HttpProtocol.NAVIGATOR_KEY);
        if (!TextUtils.isEmpty(nextUrl)) {
            mPresenter.setNextPageUrl(nextUrl);
        }
        mListView = (ListView) findViewById(ResFinder.getId("umeng_comm_like_user_listview"));
        // 处理点击返回事件
        findViewById(ResFinder.getId("umeng_comm_title_back_btn")).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
        // 设置标题
        TextView titleTextView = (TextView) findViewById(ResFinder.getId("umeng_comm_title_tv"));
        titleTextView.setText(ResFinder.getString("umeng_comm_like_user_title"));
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        // 隐藏设置按钮
        findViewById(ResFinder.getId("umeng_comm_title_setting_btn")).setVisibility(View.GONE);

        // 添加header
        View header = getLayoutInflater().inflate(
                ResFinder.getLayout("umeng_comm_like_user_header"), null);
        TextView counTextView = (TextView) header.findViewById(ResFinder
                .getId("umeng_comm_like_count"));
        int count = getIntent().getIntExtra(Constants.TAG_COUNT, 0);
        counTextView.setText(String.valueOf(count));
        mListView.addHeaderView(header);

        mAdapter = new LikeUserAdapter(this);
        mAdapter.getDataSource().addAll(users);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new Listeners.OnItemClickLoginListener() {

            @Override
            protected void doAfterLogin(View v, int position) {
                if (position == 0) {
                    return;// 此时点击的是Header
                }
                // 含有header,position需要减去1
                CommUser user = mAdapter.getDataSource().get(position - 1);
                Intent intent = new Intent();
                intent.setClass(LikeUsersActivity.this, UserInfoActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Constants.TAG_USER, user);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onRefreshStart() {
        mRefreshLvLayout.setRefreshing(true);
    }

    @Override
    public void onRefreshEnd() {
        mRefreshLvLayout.setRefreshing(false);
        mRefreshLvLayout.setLoading(false);
    }

    @Override
    public void fetchLikeUsers(List<Like> likes) {
        if (CommonUtils.isListEmpty(likes)) {
            return;
        }
        List<CommUser> dataSource = mAdapter.getDataSource();
        for (Like like : likes) {
            if (!dataSource.contains(like.creator)) {
                dataSource.add(like.creator);
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detach();
    }

}
