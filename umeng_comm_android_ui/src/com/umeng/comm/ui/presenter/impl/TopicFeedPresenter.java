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

import java.util.Iterator;
import java.util.List;

import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.ui.mvpview.MvpFeedView;

/**
 * 获取某个话题下的Feed
 */
public class TopicFeedPresenter extends FeedListPresenter {

    public TopicFeedPresenter(MvpFeedView view) {
        super(view);
    }

    @Override
    public void loadDataFromServer() {
        mCommunitySDK.fetchTopicFeed(mId, mRefreshListener);
    }
    
    @Override
    protected List<FeedItem> appendFeedItems(List<FeedItem> feedItems,boolean fromDB) {
        if ( CommonUtils.isListEmpty(feedItems) || !fromDB) {
            return super.appendFeedItems(feedItems,fromDB);
        }
        // 移除不包含该feed的缓存数据
        Iterator<FeedItem> iterator = feedItems.iterator();
        Topic tempTopic = new Topic();
        tempTopic.id = mId;
        while (iterator.hasNext()) {
            FeedItem item = iterator.next();
            if ( CommonUtils.isListEmpty(item.topics) || !item.topics.contains(tempTopic) ) {
                iterator.remove();
            }
        }
        return super.appendFeedItems(feedItems,fromDB);
    }

}
