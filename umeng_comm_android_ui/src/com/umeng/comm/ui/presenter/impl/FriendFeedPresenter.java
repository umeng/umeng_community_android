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

import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.beans.FeedItem.CATEGORY;
import com.umeng.comm.core.nets.responses.FeedsResponse;
import com.umeng.comm.ui.mvpview.MvpFeedView;

public class FriendFeedPresenter extends FeedListPresenter {

    public FriendFeedPresenter(MvpFeedView feedViewInterface) {
        super(feedViewInterface);
    }

    @Override
    public void loadDataFromServer() {
        mCommunitySDK.fetchFriendsFeed(mRefreshListener);
    }

    @Override
    protected void beforeDeliveryFeeds(FeedsResponse response) {
        isNeedRemoveOldFeeds.set(false);
        for (FeedItem item : response.result) {
            item.category = CATEGORY.FRIENDS;
        }
    }

    @Override
    public void loadDataFromDB() {
        mDatabaseAPI.getFeedDBAPI().loadFriendsFeedsFromDB(mDbFetchListener);
    }
    
    @Override
    public void sortFeedItems(List<FeedItem> items) {
        Collections.sort(items, mComparator);
    }
    
    private Comparator<FeedItem> mComparator = new Comparator<FeedItem>() {

        @Override
        public int compare(FeedItem lhs, FeedItem rhs) {
            return rhs.publishTime.compareTo(lhs.publishTime);
        }
    };

}
