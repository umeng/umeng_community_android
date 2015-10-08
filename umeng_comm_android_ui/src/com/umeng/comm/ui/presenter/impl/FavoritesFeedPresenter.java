/**
 * 
 */

package com.umeng.comm.ui.presenter.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.beans.FeedItem.CATEGORY;
import com.umeng.comm.core.nets.responses.FeedsResponse;
import com.umeng.comm.ui.mvpview.MvpFeedView;

/**
 * 
 */
public class FavoritesFeedPresenter extends FriendFeedPresenter {

    /**
     * @param feedViewInterface
     */
    public FavoritesFeedPresenter(MvpFeedView feedViewInterface) {
        super(feedViewInterface);
    }

    @Override
    public void loadDataFromServer() {
        mCommunitySDK.fetchFavoritesFeed(mRefreshListener);
    }

    @Override
    protected void beforeDeliveryFeeds(FeedsResponse response) {
        isNeedRemoveOldFeeds.set(false);
        for (FeedItem item : response.result) {
            item.category = CATEGORY.FAVORITES;
        }
    }

    @Override
    public void loadDataFromDB() {
        mDatabaseAPI.getFeedDBAPI().loadFavoritesFeed(mDbFetchListener);
    }

    /**
     * 更新feed 收藏/ 取消收藏</br>
     * 
     * @param feedId 需要被更新的feedid
     * @param category
     */
    public void updateFeedFavourites(FeedItem item, CATEGORY category) {
        List<FeedItem> items = mFeedView.getBindDataSource();
        if (category == CATEGORY.FAVORITES) {
            items.add(item);
            mFeedView.notifyDataSetChanged();
            return;
        }
        Iterator<FeedItem> iterator = items.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().id.equals(item.id)) {
                iterator.remove();
                break;
            }
        }
        mFeedView.notifyDataSetChanged();
    }

    public void addFavoutite(FeedItem feedItem) {
        mFeedView.getBindDataSource().add(feedItem);
        sortFeedItems(mFeedView.getBindDataSource());
        mFeedView.notifyDataSetChanged();
    }

    @Override
    protected int addFeedItemsToHeader(List<FeedItem> feedItems) {
        List<FeedItem> olds = mFeedView.getBindDataSource();
        int size = olds.size();
        olds.removeAll(feedItems);
        olds.addAll(0, feedItems);
        int news = olds.size() - size;
        sortFeedItems(olds);
        mFeedView.notifyDataSetChanged();
        return news;
    }

    @Override
    public void sortFeedItems(List<FeedItem> items) {
        Collections.sort(items, mComparator);
    }

    /**
     * 收藏feed按照添加时间排序
     */
    private Comparator<FeedItem> mComparator = new Comparator<FeedItem>() {

        @Override
        public int compare(FeedItem lhs, FeedItem rhs) {
            return rhs.addTime.compareTo(lhs.addTime);
        }
    };

}
