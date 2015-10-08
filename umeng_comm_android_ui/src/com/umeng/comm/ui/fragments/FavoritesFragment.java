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

import java.util.Iterator;

import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.beans.FeedItem.CATEGORY;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.adapters.FavouriteFeedAdapter;
import com.umeng.comm.ui.adapters.FeedAdapter;
import com.umeng.comm.ui.presenter.impl.FavoritesFeedPresenter;
import com.umeng.comm.ui.presenter.impl.FriendFeedPresenter;

/**
 * 收藏页面
 */
public class FavoritesFragment extends FriendsFragment {

    @Override
    protected FriendFeedPresenter createPresenters() {
        return new FavoritesFeedPresenter(this);
    }

    @Override
    protected void initWidgets() {
        super.initWidgets();
        mPostBtn.setVisibility(View.GONE); // 收藏页面不显示发送feed按钮
        // 处理返回事件，显示发现页面
        mRootView.findViewById(ResFinder.getId("umeng_comm_title_back_btn")).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mListener.onResult(0);
                    }
                });

        // 将标题改为收藏列表
        TextView titleTextView = (TextView) mRootView.findViewById(ResFinder
                .getId("umeng_comm_title_tv"));
        titleTextView.setText(ResFinder.getString("umeng_comm_favoriets_list"));
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        mBaseView.setEmptyViewText(ResFinder.getString("umeng_comm_no_favourite_feed"));
    }

    public static FavoritesFragment newFavoritesFragment() {
        return new FavoritesFragment();
    }

    @Override
    protected FeedAdapter createListViewAdapter() {
        FavouriteFeedAdapter adapter = new FavouriteFeedAdapter(getActivity());
        return adapter;
    }

    /**
     * 收藏页面在通知的时候，可能混入非收藏的feed，此时需要检查
     */
    public void notifyDataSetChanged() {
        Iterator<FeedItem> iterator = mFeedLvAdapter.getDataSource().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().category != CATEGORY.FAVORITES) {
                iterator.remove();
            }
        }
        mFeedLvAdapter.notifyDataSetChanged();
        checkListViewData();
    }

    @Override
    protected void dealFavourite(FeedItem feedItem) {
        ((FavoritesFeedPresenter) mPresenter).addFavoutite(feedItem);
    }
    
    /**
     * 取消关注某个人时，收藏中的feed不需要做相关操作
     */
    @Override
    protected void onCancelFollowUser(CommUser user) {
    }
}
