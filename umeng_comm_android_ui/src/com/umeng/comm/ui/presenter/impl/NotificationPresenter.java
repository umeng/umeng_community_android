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

import com.umeng.comm.core.beans.Notification;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.NotificationResponse;
import com.umeng.comm.core.nets.uitls.NetworkUtils;
import com.umeng.comm.ui.mvpview.MvpNotifyView;
import com.umeng.comm.ui.presenter.BaseFragmentPresenter;

/**
 * 用户获取系统消息通知的Presenter
 */
public class NotificationPresenter extends BaseFragmentPresenter<List<Notification>> {

    MvpNotifyView mNotifyView;
    String nextPage;

    public NotificationPresenter(MvpNotifyView view) {
        mNotifyView = view;
    }

    private List<Notification> removeExsitItems(List<Notification> newItems) {
        newItems.removeAll(mNotifyView.getBindDataSource());
        return newItems;
    }

    @Override
    public void loadDataFromServer() {

        mCommunitySDK.fetchNotifications(new SimpleFetchListener<NotificationResponse>() {

            @Override
            public void onComplete(NotificationResponse response) {
                deliveryResponse(response, false);
            }
        });
    }

    @Override
    public void loadMoreData() {
        if (TextUtils.isEmpty(nextPage)) {
            mNotifyView.onRefreshEnd();
            return;
        }

        mCommunitySDK.fetchNextPageData(nextPage, NotificationResponse.class,
                new SimpleFetchListener<NotificationResponse>() {

                    @Override
                    public void onComplete(NotificationResponse response) {
                        deliveryResponse(response, true);
                    }
                });
    }

    private void deliveryResponse(NotificationResponse response, boolean append) {
        mNotifyView.onRefreshEnd();
        if (NetworkUtils.handleResponseAll(response)) {
            return;
        }
        nextPage = response.nextPageUrl;
        if (append) {
            mNotifyView.getBindDataSource().addAll(removeExsitItems(response.result));
        } else {
            mNotifyView.getBindDataSource().addAll(0, removeExsitItems(response.result));
        }
        mNotifyView.notifyDataSetChange();
    }
}
