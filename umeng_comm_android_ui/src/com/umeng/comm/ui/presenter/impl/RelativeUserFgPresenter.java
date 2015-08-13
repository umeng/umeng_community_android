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
import com.umeng.comm.core.nets.responses.AbsResponse;
import com.umeng.comm.core.nets.responses.UsersResponse;
import com.umeng.comm.core.nets.uitls.NetworkUtils;
import com.umeng.comm.ui.mvpview.MvpActiveUserFgView;

/**
 * 
 */
public class RelativeUserFgPresenter extends RecommendUserFgPresenter {

    /**
     * @param activeUserFgView
     */
    public RelativeUserFgPresenter(MvpActiveUserFgView activeUserFgView) {
        super(activeUserFgView);
    }

    /**
     * 数据直接传递过来，不需要再次从server获取
     */
    @Override
    public void loadDataFromServer() {
    }

    @Override
    public void loadMoreData() {
        if (TextUtils.isEmpty(mNextPageUrl)) {
            mActiveUserFgView.onRefreshEnd();
            return;
        }
        mCommunitySDK.fetchNextPageData(mNextPageUrl, UsersResponse.class,
                new SimpleFetchListener<UsersResponse>() {

                    @Override
                    public void onComplete(UsersResponse response) {
                        mActiveUserFgView.onRefreshEnd();
                        mNextPageUrl = response.nextPageUrl;
                        if (handleResponse(response)) {
                            return;
                        }
                        mActiveUserFgView.getBindDataSource().addAll(response.result);
                        mActiveUserFgView.notifyDataSetChanged();
                    }
                });
    }

    /**
     * 根据response做不同的Toast提示。【所有跟网络请求相关的Toast都应该经过此方法的判断】</br>
     * 
     * @param response
     * @return 如果进行了Toast，则返回true；否则返回false
     */
    protected boolean handleResponse(AbsResponse<?> response) {
        return NetworkUtils.handleResponse(mContext, response);
    }

}
