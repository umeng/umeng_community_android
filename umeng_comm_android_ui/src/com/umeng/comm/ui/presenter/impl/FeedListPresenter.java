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

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.constants.ErrorCode;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.listeners.Listeners.OnResultListener;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.FeedsResponse;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.ui.mvpview.MvpFeedView;
import com.umeng.comm.ui.utils.Filter;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Feed列表相关的Presenter，该Presenter从网络、数据库中读取Feed，如果数据是从网络上获取的，那么需要将数据存储到数据库中。
 * 在获取数据后会通过MvpFeedView的{@link MvpFeedView#getAdapterDataSet()}
 * 函数获取到列表的数据集,然后对新获取的数据进行去重、排序，再将新获取到的数据添加到列表的数据集合中，最后调用
 * {@link MvpFeedView#notifyDataSetChanged()} 函数更新对应的列表视图。
 * 
 * @author mrsimple
 */
public class FeedListPresenter extends BaseFeedPresenter {
    protected MvpFeedView mFeedView;
    protected String mNextPageUrl;
    private Filter<FeedItem> mFeedFilter;
    private OnResultListener mOnResultListener; // feed更新时回调，用于提示

    /**
     * 是否需要清空数据库缓存的标志位
     */
    protected AtomicBoolean isNeedRemoveOldFeeds = new AtomicBoolean(true);

    public FeedListPresenter(MvpFeedView view) {
        mFeedView = view;
    }

    /**
     * 设置回调</br>
     * 
     * @param listener
     */
    public void setOnResultListener(OnResultListener listener) {
        this.mOnResultListener = listener;
    }

    @Override
    public void loadDataFromServer() {
        mCommunitySDK.fetchLastestFeeds(mRefreshListener);
    }

    /**
     * 下拉数据的Listener
     */
    protected FetchListener<FeedsResponse> mRefreshListener = new SimpleFetchListener<FeedsResponse>() {

        @Override
        public void onStart() {
            mFeedView.onRefreshStart();
        }

        // [注意]：mFeedView.onRefreshEnd方法不可提前统一调用，该方法会被判断是否显示空视图的逻辑
        @Override
        public void onComplete(FeedsResponse response) {
            // 根据response进行Toast
            if (mFeedView.handleResponse(response)) {
                mFeedView.onRefreshEnd();
                return;
            }

            final List<FeedItem> newFeedItems = response.result;
            if (response.errCode == ErrorCode.NO_ERROR) {
                // 对于下拉刷新，仅在下一个地址为空（首次刷新）时设置下一页地址
                if (TextUtils.isEmpty(mNextPageUrl) && isNeedRemoveOldFeeds.get()) { 
                    mNextPageUrl = response.nextPageUrl;
                }
                beforeDeliveryFeeds(response);
                // 更新数据
                int news = addFeedItemsToHeader(newFeedItems);
                if (mOnResultListener != null) {
                    mOnResultListener.onResult(news);
                }
                // 保存加载的数据。如果该数据存在于DB中，则替换成最新的，否则Insert一条新纪录
                saveDataToDB(newFeedItems);
                mFeedView.onRefreshEnd();
            }
        }
    };

    protected void beforeDeliveryFeeds(FeedsResponse response) {
        // 第一次从网络上下拉到数据,那么则清除从数据库中加载进来的数据,避免下一页地址出问题.
        clearFeedDBCache();
    }

    /**
     * 第一次从网上下拉到数据时清空数据库中的缓存,然后会调用{@see saveFeedsToDB}将新的数据存储到缓存中
     * 
     * @param helper
     */
    private void clearFeedDBCache() {
        if (isNeedRemoveOldFeeds.get()) {
            // 清空原来的缓存数据
            // mFeedView.clearListView(); //
            // 此时暂时不清空Adapter数据，确保手机显示的数据是集为：cache+server端数据
            // 清空数据库中的缓存数据
            mDatabaseAPI.getFeedDBAPI().deleteAllFeedsFromDB();
            if ( !TextUtils.isEmpty(CommConfig.getConfig().loginedUser.id) ) {
                isNeedRemoveOldFeeds.set(false);
            }
        }
    }

    public void fetchNextPageData() {
        if (TextUtils.isEmpty(mNextPageUrl)) {
            mFeedView.onRefreshEnd();
            return;
        }

        mCommunitySDK.fetchNextPageData(mNextPageUrl,
                FeedsResponse.class, new SimpleFetchListener<FeedsResponse>() {

                    @Override
                    public void onComplete(FeedsResponse response) {
                        mFeedView.onRefreshEnd();
                        //此时数据加载完毕，需要更新下一页地址
                        if (response.errCode == ErrorCode.NO_ERROR
                                && CommonUtils.isListEmpty(response.result)) {
                            mNextPageUrl = "";
                        }
                        // 根据response进行Toast
                        if (mFeedView.handleResponse(response)) {
                            return;
                        }

                        mNextPageUrl = response.nextPageUrl;
                        // 去掉重复的feed
                        final List<FeedItem> feedItems = getNewFeedItems(response.result);
                        if (feedItems != null && feedItems.size() > 0) {
                            // 追加数据
                            appendFeedItems(feedItems);
                            saveDataToDB(feedItems);
                        }
                    }
                });
    }

    /**
     * 去重并更新adapter的数据,追加到后面。</br>
     * 
     * @param feedItems
     */
    protected List<FeedItem> appendFeedItems(List<FeedItem> feedItems) {
        List<FeedItem> newFeeds = null;
        synchronized (this) {
            newFeeds = getNewFeedItems(feedItems);
            if (newFeeds != null && newFeeds.size() > 0) {
                // 添加到listview中
                mFeedView.getAdapterDataSet().addAll(newFeeds);
                sortFeedItems(mFeedView.getAdapterDataSet());
                mFeedView.notifyDataSetChanged();
            }
        }
        return newFeeds;
    }

    /**
     * 过滤数据</br>
     * 
     * @return
     */
    protected List<FeedItem> filteFeeds(List<FeedItem> list) {
        List<FeedItem> destList = mFeedFilter != null ? mFeedFilter.doFilte(list) : list;
        // 移除status>=2的feed，具体值得的含义参考文档说明
        Iterator<FeedItem> iterator = destList.iterator();
        while (iterator.hasNext()) {
            FeedItem item = iterator.next();
            if (item.status >= FeedItem.STATUS_SPAM) {
                iterator.remove();
            }
        }
        return destList;
    }

    /**
     * 移除存在的feed，获取最新的feed</br>
     * 
     * @param feedItems 最新的feed（待被移除）
     * @return
     */
    private List<FeedItem> getNewFeedItems(List<FeedItem> feedItems) {
        // 去掉在本地已经存在的feeds[此时应该先从adapter中移除数据，确保加载下来的是最新的数据]
        // feedItems.removeAll(mFeedView.getAdapterDataSet());
        mFeedView.getAdapterDataSet().removeAll(feedItems);
        // 过滤数据
        return filteFeeds(feedItems);
    }

    /**
     * 去重并更新adapter的数据,将数据插到前边。</br>
     * 
     * @param feedItems
     */
    protected int addFeedItemsToHeader(List<FeedItem> feedItems) {
        feedItems = filteFeeds(feedItems);
        List<FeedItem> olds = mFeedView.getAdapterDataSet();
        int size = olds.size();
        olds.removeAll(feedItems);
        olds.addAll(0, feedItems);
        int news = olds.size() - size;
        sortFeedItems(olds);
        mFeedView.notifyDataSetChanged();
        return news;
    }

    public void sortFeedItems(List<FeedItem> items) {
        // 所有feed都按照feed的时间降序排列。【该代码避免用户首次登录时，推荐的feed时间较新，但是管理员的帖子较旧的情况】
        Collections.sort(items, mComparator);
    }

    /**
     * 获取所有的feed
     */
    @Override
    public void loadDataFromDB() {
        mDatabaseAPI.getFeedDBAPI().loadFeedsFromDB(mDbFetchListener);
    }

    /**
     * 获取某个用户的feed</br>
     * 
     * @param uid
     */
    // 注意这里不能重构
    public void loadFeedsFromDB(String uid) {
        mDatabaseAPI.getFeedDBAPI().loadFeedsFromDB(uid, mDbFetchListener);
    }

    /**
     * 用于数据库的SimpleFetchListener
     */
    protected SimpleFetchListener<List<FeedItem>> mDbFetchListener =
            new SimpleFetchListener<List<FeedItem>>() {

                @Override
                public void onComplete(List<FeedItem> response) {
                    appendFeedItems(response);
                }
            };

    protected Comparator<FeedItem> mComparator = new Comparator<FeedItem>() {

        @Override
        public int compare(FeedItem lhs, FeedItem rhs) {
            int isTop = rhs.isTop - lhs.isTop;
            return isTop != 0 ? isTop : rhs.publishTime.compareTo(lhs.publishTime);
        }
    };

}
