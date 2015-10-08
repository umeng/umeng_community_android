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
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.nets.responses.FansResponse;
import com.umeng.comm.core.nets.responses.UsersResponse;
import com.umeng.comm.core.nets.uitls.NetworkUtils;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.ui.mvpview.MvpActiveUserFgView;

/**
 * 
 */
public class RecommendUserFgPresenter extends ActiveUserFgPresenter {

    public RecommendUserFgPresenter(MvpActiveUserFgView activeUserFgView) {
        super(activeUserFgView);
    }

    @Override
    public void loadDataFromServer() {
        mCommunitySDK.fetchRecommendedUsers(new FetchListener<UsersResponse>() {

            @Override
            public void onStart() {
                mActiveUserFgView.onRefreshStart();
            }

            @Override
            public void onComplete(UsersResponse response) {
                mActiveUserFgView.onRefreshEnd();
                dealResult(response, true);
            }
        });
    }

    @Override
    void dealResult(FansResponse response, boolean fromRefresh) {
        if ( NetworkUtils.handleResponseComm(response) ) {
            return ;
        }
        List<CommUser> users = response.result;
        if (CommonUtils.isListEmpty(users)) {
            mActiveUserFgView.showEmptyView();
            return;
        } else {
            mActiveUserFgView.hideEmptyView();
        }
        super.dealResult(response, fromRefresh);
    }
    
    @Override
    public void loadMoreData() {
        if ( TextUtils.isEmpty(mNextPageUrl) ) {
            mActiveUserFgView.onRefreshEnd();
            return ;
        }
        mCommunitySDK.fetchNextPageData(mNextPageUrl, UsersResponse.class, new FetchListener<UsersResponse>() {

            @Override
            public void onStart() {
            }

            @Override
            public void onComplete(UsersResponse response) {
                dealResult(response, false);
            }} );
    }

}
