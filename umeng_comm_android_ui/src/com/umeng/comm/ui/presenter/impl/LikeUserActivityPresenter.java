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

import android.text.TextUtils;

import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.LikesResponse;
import com.umeng.comm.core.nets.uitls.NetworkUtils;
import com.umeng.comm.ui.mvpview.MvpLikeUserView;
import com.umeng.comm.ui.presenter.BasePresenter;

public class LikeUserActivityPresenter extends BasePresenter {

    MvpLikeUserView mView;
    String mFeedId;
    String mNextPageUrl;

    public LikeUserActivityPresenter(MvpLikeUserView view, String feedId) {
        mView = view;
        mFeedId = feedId;
    }

    public void loadLikeUserFromServer() {
        mCommunitySDK.fetchFeedLikes(mFeedId, mFetchListener);
    }

    public void loadMoreLikeUser() {
        if (TextUtils.isEmpty(mNextPageUrl)) {
            mView.onRefreshEnd();
            return;
        }
        mCommunitySDK.fetchNextPageData(mNextPageUrl, LikesResponse.class, mFetchListener);
    }
    
    public void setNextPageUrl(String url) {
        mNextPageUrl = url ;
    }

    private SimpleFetchListener<LikesResponse> mFetchListener = new SimpleFetchListener<LikesResponse>() {

        @Override
        public void onComplete(LikesResponse response) {
            mView.onRefreshEnd();
            if (NetworkUtils.handleResponse(mContext, response)) {
                return;
            }
            mNextPageUrl = response.nextPageUrl;
            mView.fetchLikeUsers(response.result);
        }
    };
}
