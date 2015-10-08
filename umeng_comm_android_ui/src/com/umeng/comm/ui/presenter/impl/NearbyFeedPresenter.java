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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.location.Location;

import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.FeedsResponse;
import com.umeng.comm.core.sdkmanager.LocationSDKManager;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.mvpview.MvpFeedView;

/**
 * 附近的Feed Presenter
 */
public class NearbyFeedPresenter extends FriendFeedPresenter {

    /**
     * @param view
     */
    public NearbyFeedPresenter(MvpFeedView view) {
        super(view);
    }

    @Override
    public void loadDataFromServer() {
        LocationSDKManager.getInstance().getCurrentSDK()
                .requestLocation(mContext, new SimpleFetchListener<Location>() {

                    @Override
                    public void onComplete(Location location) {
                        if (location == null) {
                            ToastMsg.showShortMsgByResName("umeng_comm_request_location_failed");
                            return;
                        }
                        mCommunitySDK.fetchNearByFeed(location, mRefreshListener);
                    }
                });
    }

    @Override
    public void sortFeedItems(List<FeedItem> items) {
        // 所有feed都按照feed的时间降序排列。【该代码避免用户首次登录时，推荐的feed时间较新，但是管理员的帖子较旧的情况】
        Collections.sort(items, new Comparator<FeedItem>() {

            @Override
            public int compare(FeedItem lhs, FeedItem rhs) {
                return lhs.distance - rhs.distance;
            }

        });
    }

    @Override
    public void loadDataFromDB() {
        mDatabaseAPI.getFeedDBAPI().loadNearbyFeed(mDbFetchListener);
    }

    @Override
    protected void beforeDeliveryFeeds(FeedsResponse response) {
        // 第一次请求成功，clean nearby cache data
        if (isNeedRemoveOldFeeds.get()) {
            mDatabaseAPI.getFeedDBAPI().deleteNearbyFeed();
            isNeedRemoveOldFeeds.set(false);
        }
    }

}
