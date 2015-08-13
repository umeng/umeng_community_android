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

package com.umeng.comm.ui.fragments;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.Comment;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.beans.FeedItem.CATEGORY;
import com.umeng.comm.core.beans.Like;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.imageloader.UMImageLoader;
import com.umeng.comm.core.listeners.Listeners.OnItemViewClickListener;
import com.umeng.comm.core.nets.responses.AbsResponse;
import com.umeng.comm.core.sdkmanager.ImageLoaderManager;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.DeviceUtils;
import com.umeng.comm.core.utils.Log;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.SharePrefUtils;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.activities.FeedDetailActivity;
import com.umeng.comm.ui.adapters.FeedAdapter;
import com.umeng.comm.ui.mvpview.MvpFeedView;
import com.umeng.comm.ui.presenter.impl.FeedListPresenter;
import com.umeng.comm.ui.utils.BroadcastUtils;
import com.umeng.comm.ui.utils.BroadcastUtils.BROADCAST_TYPE;
import com.umeng.comm.ui.utils.BroadcastUtils.DefalutReceiver;
import com.umeng.comm.ui.utils.Filter;
import com.umeng.comm.ui.widgets.RefreshLayout.OnLoadListener;
import com.umeng.comm.ui.widgets.RefreshLvLayout;

/**
 * 这是Feed流列表页面，包含当前最新的消息列表.从该页面可以跳转到话题搜索页面、消息发布页面，可以浏览消息流中的图片、评论某项消息、进入某个好友的主页等.
 */
public abstract class FeedListFragment<P extends FeedListPresenter> extends
        CommentEditFragment<List<FeedItem>, P> implements MvpFeedView {
    /**
     * ImageLoader
     */
    protected UMImageLoader mImageLoader = ImageLoaderManager.getInstance().getCurrentSDK();
    /**
     * 下拉刷新, 上拉加载的布局, 包裹了Feeds ListView
     */
    protected RefreshLvLayout mRefreshLayout;
    /**
     * feeds ListView
     */
    protected ListView mFeedsListView;
    /**
     * 消息流适配器
     */
    protected FeedAdapter mFeedLvAdapter;
    /**
     * title的文本TextView
     */
    protected TextView mTitleTextView;

    /**
     * ListView的footers
     */
    protected List<View> mFooterViews = new ArrayList<View>();

    /**
     * 过滤掉某些关键字的filter
     */
    protected Filter<FeedItem> mFeedFilter;

    /**
     * 布局改变时的回调。主要用于监测输入法是否已经打开，并做相关的逻辑处理（评论中某项的具体滚动距离）
     */
    private OnGlobalLayoutListener mOnGlobalLayoutListener;
    /**
     * 当前登录的用户
     */
    protected CommUser mUser = CommConfig.getConfig().loginedUser;
    /**
     * 发表feed的button
     */
    protected ImageView mPostBtn;

    List<String> mTabTitls = new ArrayList<String>();

    @Override
    protected int getFragmentLayout() {
        return ResFinder.getLayout("umeng_comm_feeds_frgm_layout");
    }

    @Override
    protected void initWidgets() {
        super.initWidgets();
        // 初始化视图
        initViews();
        // 初始化Feed Adapter
        initAdapter();
        // 请求中的状态
        mRefreshLayout.setRefreshing(true);
        registerBroadcast();
    }

    /**
     * 初始化feed流 页面显示相关View
     */
    protected void initViews() {
        // 初始化刷新相关View跟事件
        initRefreshView();
        mPostBtn = mViewFinder.findViewById(ResFinder.getId("umeng_comm_new_post_btn"));
    }

    /**
     * 初始化下拉刷新试图, listview
     */
    protected void initRefreshView() {
        // 下拉刷新, 上拉加载的布局
        mRefreshLayout = mViewFinder.findViewById(ResFinder.getId("umeng_comm_swipe_layout"));
        // 下拉刷新时执行的回调
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                // 加载最新的feed
                mPresenter.loadDataFromServer();
            }
        });

        // 上拉加载更多
        mRefreshLayout.setOnLoadListener(new OnLoadListener() {

            @Override
            public void onLoad() {
                loadMoreFeed();
            }
        });

        // 滚动监听器, 滚动停止时才加载图片
        mRefreshLayout.addOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                    mImageLoader.resume();
                } else {
                    mImageLoader.pause();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                    int totalItemCount) {

            }
        });

        int feedListViewResId = ResFinder.getId("umeng_comm_feed_listview");
        // feed列表 listview
        mFeedsListView = mRefreshLayout.findRefreshViewById(feedListViewResId);
        // 添加footer
        mRefreshLayout.setDefaultFooterView();
        // 关闭动画缓存
        mFeedsListView.setAnimationCacheEnabled(false);
        // 开启smooth scrool bar
        mFeedsListView.setSmoothScrollbarEnabled(true);

        mFeedsListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mCommentLayout.getVisibility() == View.VISIBLE) {
                    hideCommentLayout();
                    return;
                }
                final int realPosition = position - mFeedsListView.getHeaderViewsCount();
                final FeedItem feedItem = mFeedLvAdapter.getItem(realPosition < 0 ? 0
                        : realPosition);
                if (feedItem != null && feedItem.status >= FeedItem.STATUS_SPAM
                        && feedItem.category == CATEGORY.FAVORITES) {
                    ToastMsg.showShortMsg(getActivity().getApplicationContext(),
                            getToastText(feedItem.status));
                    return;
                }
                Intent intent = new Intent(getActivity(), FeedDetailActivity.class);
                intent.putExtra(Constants.TAG_FEED, feedItem);
                startActivity(intent);
            }
        });
    }

    private String getToastText(int status) {
        String text = ResFinder.getString("umeng_comm_feed_spam_deleted");
        // switch (status) {
        // case FeedItem.STATUS_SPAM:
        // text = ResFinder.getString("umeng_comm_feed_spam_shield");
        // break;
        // case FeedItem.STATUS_DELETE:
        // text = ResFinder.getString("umeng_comm_feed_spam_deleted");
        // break;
        // case FeedItem.STATUS_SNOW:
        // text = ResFinder.getString("umeng_comm_feed_spam_deleted_admin");
        // break;
        // case FeedItem.STATUS_SENSITIVE:
        // text = ResFinder.getString("umeng_comm_feed_spam_sensitive");
        // break;
        // default:
        // text = "该收藏feed已经被删除";
        // break;
        // }
        return text;
    }

    /**
     * 隐藏评论的布局跟软键盘</br>
     */
    public void hideCommentLayoutAndInputMethod() {
        resetCommentLayout();
        hideInputMethod();
        showPostButtonWithAnim();
    }

    /**
     * 关闭输入法</br>
     */
    private void hideInputMethod() {
        if (CommonUtils.isActivityAlive(getActivity())) {
            sendInputMethodMessage(Constants.INPUT_METHOD_DISAPPEAR, mCommentEditText);
            mRootView.getRootView().getViewTreeObserver().removeGlobalOnLayoutListener(
                    mOnGlobalLayoutListener);
        }
    }

    /**
     * 
     */
    protected void showPostButtonWithAnim() {
    }

    protected void deleteInvalidateFeed(FeedItem feedItem) {
        // 将无效的feed从listview中删除
        mFeedLvAdapter.getDataSource().remove(feedItem);
        mFeedLvAdapter.notifyDataSetChanged();
    }

    protected void updateAfterDelete(FeedItem feedItem) {
        mFeedLvAdapter.getDataSource().remove(feedItem);
        mFeedLvAdapter.notifyDataSetChanged();
        // 发送删除广播
        BroadcastUtils.sendFeedDeleteBroadcast(getActivity(), feedItem);
    }

    /**
     * 加载更多数据</br>
     */
    protected void loadMoreFeed() {
        if (mPresenter == null) {
            return;
        }
        // 没有网络的情况下从数据库加载
        if (!DeviceUtils.isNetworkAvailable(getActivity())) {
            mPresenter.loadDataFromDB();
            mRefreshLayout.setLoading(false);
            return;
        }
        mPresenter.fetchNextPageData();
    }

    protected FeedAdapter createListViewAdapter() {
        return new FeedAdapter(getActivity());
    }

    /**
     * 初始化适配器
     */
    protected void initAdapter() {
        if (mFeedLvAdapter == null) {
            mFeedLvAdapter = createListViewAdapter();
            mFeedLvAdapter.setCommentClickListener(new OnItemViewClickListener<FeedItem>() {

                @Override
                public void onItemClick(int position, FeedItem item) {
                    // 如果评论数>0 ，则跳转到详情页面评论；否则直接显示输入法评论
                    if (item.commentCount > 0) {
                        Intent intent = new Intent(getActivity(), FeedDetailActivity.class);
                        intent.putExtra(Constants.TAG_FEED, item);
                        intent.putExtra(Constants.TAG_IS_SCROLL, true);
                        getActivity().startActivity(intent);
                        return;
                    }
                    mFeedsListView.setSelection(position);
                    mFeedItem = item;
                    if (mCommentPresenter != null) {
                        mCommentPresenter.setFeedItem(item);
                    }
                    showCommentLayout();
                }
            });

        }
        mRefreshLayout.setAdapter(mFeedLvAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        onBaseResumeDeal();
    }

    /**
     * 基本的 OnResume处理逻辑</br>
     */
    protected void onBaseResumeDeal() {
        mFeedsListView.postDelayed(new Runnable() {

            @Override
            public void run() {
                hideInputMethod();
                if (mImageLoader != null) {
                    // 启动加载数据
                    mImageLoader.resume();
                }
            }
        }, 300);
        removeDeletedFeeds();
    }

    public void onStop() {
        resetCommentLayout();
        super.onStop();
    }

    protected boolean isMyPage(FeedItem feedItem) {
        return feedItem != null;
    }

    protected void onCancelFollowUser(CommUser user) {
        Log.d(getTag(), "### cancel follow user");
    }

    /**
     * 数据同步处理
     */
    protected DefalutReceiver mReceiver = new DefalutReceiver() {
        public void onReceiveUser(Intent intent) {
            BROADCAST_TYPE type = getType(intent);
            CommUser user = getUser(intent);
            if (type == BROADCAST_TYPE.TYPE_USER_UPDATE) {// 更新用户信息
                updatedUserInfo(user);
                return;
            }
            if (!CommonUtils.isMyself(mUser)) {// 如果不是登录用户，则不remove feed
                return;
            }

            if (type == BROADCAST_TYPE.TYPE_USER_CANCEL_FOLLOW) {
                // 预留一个hook函数,当取消对某个用户的关注，移除主页上该用户的feed。其他页面不进行操作
                onCancelFollowUser(user);
            }
        }

        public void onReceiveFeed(Intent intent) {// 发送or删除时
            FeedItem feedItem = getFeed(intent);
            if (feedItem == null) {
                return;
            }
            BROADCAST_TYPE type = getType(intent);
            if (BROADCAST_TYPE.TYPE_FEED_POST == type && isMyFeedList()) {
                postFeedComplete(feedItem);
            } else if (BROADCAST_TYPE.TYPE_FEED_DELETE == type) {
                deleteFeedComplete(feedItem);
            } else if (BROADCAST_TYPE.TYPE_FEED_FAVOURITE == type) {
                dealFavourite(feedItem);
            }

            mFeedsListView.invalidate();
        }

        // 更新Feed的相关数据。包括like、comment、forward数量修改
        public void onReceiveUpdateFeed(Intent intent) {
            FeedItem item = getFeed(intent);
            List<FeedItem> items = mFeedLvAdapter.getDataSource();
            for (FeedItem feed : items) {
                if (feed.id.equals(item.id)) {
                    // feed = item;
                    feed.isLiked = item.isLiked;
                    feed.likeCount = item.likeCount;
                    feed.likes = item.likes;
                    feed.commentCount = item.commentCount;
                    feed.comments = item.comments;
                    feed.forwardCount = item.forwardCount;
                    feed.category = item.category;
                    break;
                }
            }
            // 此处不可直接调用adapter.notifyDataSetChanged，其他地方在notifyDataSetChanged（）方法中又逻辑处理
            notifyDataSetChanged();
        }
    };

    /**
     * 在触发收藏操作时，需要收藏feed同步</br>
     */
    protected void dealFavourite(FeedItem feedItem) {
    }

    protected void postFeedComplete(FeedItem feedItem) {
        // mFeedLvAdapter.addToFirst(feedItem);
        // 此时需要排序，确保置顶的feed放在最前面
        mFeedLvAdapter.getDataSource().add(feedItem);
        mPresenter.sortFeedItems(mFeedLvAdapter.getDataSource());
        mFeedLvAdapter.notifyDataSetChanged();
        mFeedsListView.setSelection(0);
        updateForwardCount(feedItem, 1);
    }

    protected void deleteFeedComplete(FeedItem feedItem) {
        mFeedLvAdapter.getDataSource().remove(feedItem);
        mFeedLvAdapter.notifyDataSetChanged();
        updateForwardCount(feedItem, -1);
        Log.d(getTag(), "### 删除feed");
    }

    protected boolean isMyFeedList() {
        return true;
    }

    /**
     * 更新转发数</br>
     * 
     * @param item
     */
    protected void updateForwardCount(FeedItem item, int count) {
        if (TextUtils.isEmpty(item.sourceFeedId)) {
            return;
        }
        List<FeedItem> items = mFeedLvAdapter.getDataSource();
        for (FeedItem feedItem : items) {
            if (feedItem.id.equals(item.sourceFeedId)) {
                feedItem.forwardCount = feedItem.forwardCount + count;
                mFeedLvAdapter.notifyDataSetChanged();
                break;
            }
        }

    }

    /**
     * 判断该Feed是否来源于特定用户</br>
     * 
     * @param feedItem
     * @return
     */
    protected boolean isMyFeed(FeedItem feedItem) {
        CommUser user = CommConfig.getConfig().loginedUser;
        if (user == null || TextUtils.isEmpty(user.id)) {
            return false;
        }
        return feedItem.creator.id.equals(user.id);
    }

    /**
     * 用户信息修改以后更新feed的用户信息
     * 
     * @param user
     */
    public void updatedUserInfo(CommUser user) {
        mUser = user;
        List<FeedItem> feedItems = mFeedLvAdapter.getDataSource();
        for (FeedItem feed : feedItems) {
            updateFeedContent(feed, user);
        }
        mFeedLvAdapter.notifyDataSetChanged();
    }

    private void updateFeedContent(FeedItem feed, CommUser user) {
        if (isMyFeed(feed)) {
            feed.creator = user;
        }

        // 更新like的创建者信息
        updateLikeCreator(feed.likes, user);
        // 更新评论信息
        updateCommentCreator(feed.comments, user);
        // 更新at好友的creator
        updateAtFriendCreator(feed.atFriends, user);
        // 转发类型的feed
        if (feed.sourceFeed != null) {
            updateFeedContent(feed.sourceFeed, user);
        }
    }

    private void updateLikeCreator(List<Like> likes, CommUser user) {
        for (Like likeItem : likes) {
            if (likeItem.creator.id.equals(user.id)) {
                likeItem.creator = user;
            }
        }
    }

    private void updateCommentCreator(List<Comment> comments, CommUser user) {
        for (Comment commentItem : comments) {
            if (commentItem.creator.id.equals(user.id)) {
                commentItem.creator = user;
            }
        }
    }

    private void updateAtFriendCreator(List<CommUser> friends, CommUser user) {
        for (CommUser item : friends) {
            if (item.id.equals(user.id)) {
                item = user;
            }
        }
    }

    @Override
    public void postCommentSuccess(Comment comment, CommUser replyUser) {
        super.postCommentSuccess(comment, replyUser);
        mFeedLvAdapter.notifyDataSetChanged();// 刷新ListVIew，更新评论数字
        mCommentEditText.setText(""); // 清除评论内容
    }

    /**
     * 用户在个人中心删除feed后,在Feed流页面的ListView中要对应的删除.已删除的feed通过SharedPreferences来存储.
     */
    private void removeDeletedFeeds() {
        SharedPreferences deletedSharedPref = SharePrefUtils.getSharePrefEdit(getActivity(),
                Constants.DELETED_FEEDS_PREF);
        // all deleted feeds iterator.
        Iterator<String> deletedIterator = deletedSharedPref.getAll().keySet().iterator();
        // 遍历移除所有已经删除的feed
        while (deletedIterator.hasNext()) {
            String feedId = deletedIterator.next();
            //
            Iterator<FeedItem> feedIterator = mFeedLvAdapter.getDataSource().iterator();
            // find the target feed
            while (feedIterator.hasNext()) {
                FeedItem feedItem = feedIterator.next();
                if (feedItem.id.equals(feedId)) {
                    feedIterator.remove();
                    break;
                }
            } // end of second while
        } // first while

        mFeedLvAdapter.notifyDataSetChanged();
        deletedSharedPref.edit().clear();
    }

    /**
     * 设置feed的过滤器</br>
     * 
     * @param filter
     */
    public void setFeedFilter(Filter<FeedItem> filter) {
        mFeedFilter = filter;
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
            if (item.status > 1) {
                iterator.remove();
            }
        }
        return destList;
    }

    private void resetCommentLayout() {
        if (mCommentLayout != null) {
            mCommentLayout.setVisibility(View.INVISIBLE);
        }
        if (mCommentEditText != null) {
            mCommentEditText.setText("");
        }
    }

    protected void registerBroadcast() {
        // 注册广播接收器
        BroadcastUtils.registerUserBroadcast(getActivity(), mReceiver);
        BroadcastUtils.registerFeedBroadcast(getActivity(), mReceiver);
        BroadcastUtils.registerFeedUpdateBroadcast(getActivity(), mReceiver);
    }

    @Override
    public void onDestroy() {
        BroadcastUtils.unRegisterBroadcast(getActivity(), mReceiver);
        super.onDestroy();
    }

    public boolean handleResponse(AbsResponse<?> response) {
        return super.handlerResponse(response);
    }

    @Override
    public void clearListView() {
        if (mFeedLvAdapter != null) {
            mFeedLvAdapter.getDataSource().clear();
            mFeedLvAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void updateCommentView() {
    }

    @Override
    public void onRefreshStart() {
        mRefreshLayout.setProgressViewOffset(false, 0,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources()
                        .getDisplayMetrics()));
        mRefreshLayout.setRefreshing(true);
    }

    @Override
    public void onRefreshEnd() {
        mRefreshLayout.setRefreshing(false);
        mRefreshLayout.setLoading(false);
    }

    @Override
    public List<FeedItem> getAdapterDataSet() {
        return mFeedLvAdapter.getDataSource();
    }

    @Override
    public void notifyDataSetChanged() {
        mFeedLvAdapter.notifyDataSetChanged();
    }
}
