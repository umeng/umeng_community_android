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

import java.util.concurrent.atomic.AtomicBoolean;

import android.text.TextUtils;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.constants.ErrorCode;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.LikeMeResponse;
import com.umeng.comm.core.nets.uitls.NetworkUtils;
import com.umeng.comm.ui.mvpview.MvpFeedView;

/**
 * 消息通知页面的Like Fragment对应的Presenter，用于获取别人对我的Like记录
 */
public class LikeMePresenter extends FeedListPresenter {

    private volatile AtomicBoolean mUpdateNextPageUrl = new AtomicBoolean(true);
    
    public LikeMePresenter(MvpFeedView feedViewInterface) {
        super(feedViewInterface);
    }

    @Override
    public void loadDataFromServer() {

        mCommunitySDK.fetchLikedRecords(CommConfig.getConfig().loginedUser.id,
                new SimpleFetchListener<LikeMeResponse>() {

                    @Override
                    public void onStart() {
                        mFeedView.onRefreshStart();
                    }

                    @Override
                    public void onComplete(LikeMeResponse response) {
                        if(NetworkUtils.handleResponseAll(response) ){
                            mFeedView.onRefreshEnd();
                            return ;
                        }
                        if ( TextUtils.isEmpty(mNextPageUrl) && mUpdateNextPageUrl.get() ) {
                            mNextPageUrl = response.nextPageUrl;
                            mUpdateNextPageUrl.set(false);
                        }
                        addFeedItemsToHeader(response.result);
                        mFeedView.onRefreshEnd();
                    }
                });
    }

    @Override
    public void fetchNextPageData() {
        if (TextUtils.isEmpty(mNextPageUrl)) {
            mFeedView.onRefreshEnd();
            return;
        }
        mCommunitySDK.fetchNextPageData(mNextPageUrl, LikeMeResponse.class,
                new SimpleFetchListener<LikeMeResponse>() {
                    @Override
                    public void onComplete(LikeMeResponse response) {
                        if (NetworkUtils.handleResponseAll(response)) {
                            if ( response.errCode == ErrorCode.NO_ERROR ) {
                                mNextPageUrl = "";
                            }
                            mFeedView.onRefreshEnd();
                            return;
                        }
                        mNextPageUrl = response.nextPageUrl;
                        appendFeedItems(response.result,false);
                        mFeedView.onRefreshEnd();
                    }
                });
    }

    @Override
    public void loadDataFromDB() {
    }

    @Override
    public void loadFeedsFromDB(String uid) {

    }
}
