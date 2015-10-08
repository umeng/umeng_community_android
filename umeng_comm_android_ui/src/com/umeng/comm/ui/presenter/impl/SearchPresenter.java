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

package com.umeng.comm.ui.presenter.impl;

import java.util.List;

import android.text.TextUtils;

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.FeedsResponse;
import com.umeng.comm.core.nets.responses.UsersResponse;
import com.umeng.comm.core.nets.uitls.NetworkUtils;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.mvpview.MvpSearchFgView;

/**
 * 
 */
public class SearchPresenter extends FeedListPresenter {

    private String mUserNextPage = "";
    private MvpSearchFgView mSearchView;
    private boolean executeLoading = false;

    /**
     * @param context
     * @param feedViewInterface
     */
    public SearchPresenter(MvpSearchFgView searchFgView) {
        super(searchFgView);
        this.mSearchView = searchFgView;
    }

    /**
     * 加载更多的关注用户</br>
     */
    public void loadMoreUser() {
        if (TextUtils.isEmpty(mUserNextPage) || executeLoading) {
            ToastMsg.showShortMsgByResName("umeng_comm_text_load_over");
            mSearchView.onRefreshEnd();
            return;
        }
        executeLoading = true;
        mCommunitySDK.fetchNextPageData(mNextPageUrl, UsersResponse.class,
                new SimpleFetchListener<UsersResponse>() {

                    @Override
                    public void onComplete(UsersResponse response) {
                        mSearchView.onRefreshEnd();
                        mUserNextPage = response.nextPageUrl;
                        if (NetworkUtils.handleResponseAll(response)) {
                            return;
                        }
                        mSearchView.getUserDataSource().addAll(response.result);
                        mSearchView.notifyDataSetChanged();
                        executeLoading = false;
                    }
                });
    }

    public void executeSearch(String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            ToastMsg.showShortMsgByResName("umeng_comm_topic_search_no_keyword");
            return;
        }
        mSearchView.hideInputMethod();
        // 搜索Feed
        mCommunitySDK.searchFeed(keyword,
                new SimpleFetchListener<FeedsResponse>() {

                    @Override
                    public void onComplete(FeedsResponse response) {
                        List<FeedItem> feedItems = response.result;
                        mSearchView.getBindDataSource().clear();
                        if (feedItems.size() == 0) {
                            mSearchView.showFeedEmptyView();
                            mSearchView.notifyDataSetChanged();
                        } else {
                            mSearchView.getBindDataSource().addAll(feedItems);
                            mSearchView.notifyDataSetChanged();
                            mSearchView.hideFeedEmptyView();
                        }
                        mNextPageUrl = response.nextPageUrl;
                    }
                });

        // 搜索用户
        mCommunitySDK.searchUser(keyword,
                new SimpleFetchListener<UsersResponse>() {

                    @Override
                    public void onComplete(UsersResponse response) {
                        List<CommUser> users = response.result;
                        mSearchView.getUserDataSource().clear();
                        if (users.size() == 0) {
                            mSearchView.showUserEmptyView();
                        } else {
                            mSearchView.hideUserEmptyView();
                        }
                        mSearchView.showRelativeUserView(users);
                    }
                });
    }

    public String getUserNextUrl() {
        return mUserNextPage;
    }

    @Override
    public void loadDataFromDB() {
    }

    @Override
    public void loadDataFromServer() {
    }

}
