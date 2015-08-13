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

import android.content.Intent;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.listeners.Listeners.LoginOnViewClickListener;
import com.umeng.comm.core.listeners.Listeners.OnResultListener;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.activities.PostFeedActivity;
import com.umeng.comm.ui.activities.SearchActivity;
import com.umeng.comm.ui.presenter.impl.FeedListPresenter;

/**
 * 主页的三个Tab之一的消息流页面,该页面返回用户关注的人、话题的Feed
 */
public class AllFeedsFragment extends PostBtnAnimFragment<FeedListPresenter> {
    private TextView mTipView; // 更新条数提示
    private boolean isShowToast = false; // 只有在显示的fragment是当前fragment时，才显示Toast

    @Override
    protected void initViews() {
        super.initViews();
        mPostBtn.setOnClickListener(new LoginOnViewClickListener() {
            @Override
            protected void doAfterLogin(View v) {
                gotoPostFeedActivity();
            }
        });
    }

    @Override
    protected FeedListPresenter createPresenters() {
        super.createPresenters();
        FeedListPresenter presenter = new FeedListPresenter(this);
        presenter.setOnResultListener(mListener);
        return presenter;
    }

    /**
     * 用户回调显示更新数目
     */
    private OnResultListener mListener = new OnResultListener() {

        @Override
        public void onResult(int nums) {
            if (!isShowToast) {
                return;
            }
            mTipView = (TextView) LayoutInflater.from(getActivity()).inflate(
                    ResFinder.getLayout("umeng_comm_newfeed_tips"), null);
            if (nums <= 0) {
                mTipView.setText(ResFinder.getString("umeng_comm_no_newfeed_tips"));
            } else {
                mTipView.setText(nums + "条新内容");
            }

            int locationY = getLocation();
            if (locationY == 0) {
                return;
            }

            Toast toast = new Toast(getActivity().getApplication());
            toast.setView(mTipView);
            toast.setDuration(2000);
            toast.setGravity(Gravity.TOP | Gravity.CENTER | Gravity.FILL_HORIZONTAL, 0, locationY);
            toast.show();
        }
    };

    /**
     * 获取feed Listview第一项的位置</br>
     * 
     * @return
     */
    private int getLocation() {
        int[] location = new int[2];
        Rect rect = new Rect();
        getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);

        mFeedsListView.getLocationOnScreen(location); // 该中情况实际不会出现。
        return location[1] - rect.top - 1;
    }

    // [注意]主页的Feed来源于好友+话题，取消对好友的关注不能删除该用户的Feed
    @Override
    protected void onCancelFollowUser(CommUser user) {
        // super.onCancelFollowUser(user);
        // // 取消对某个用户的关注，移除其feed
        // List<FeedItem> items = mFeedLvAdapter.getDataSource();
        // List<FeedItem> scrapItems = new ArrayList<FeedItem>();
        // for (FeedItem item : items) {
        // if (item.creator.equals(user)) {
        // scrapItems.add(item);
        // }
        // }
        // if (scrapItems.size() > 0) {
        // mFeedLvAdapter.getDataSource().removeAll(scrapItems);
        // mFeedLvAdapter.notifyDataSetChanged();
        // }
    }

    @Override
    protected void showPostButtonWithAnim() {
        AlphaAnimation showAnim = new AlphaAnimation(0.5f, 1.0f);
        showAnim.setDuration(500);

        if (mPostBtn != null) {
            mPostBtn.setVisibility(View.VISIBLE);
            mPostBtn.startAnimation(showAnim);
        }
    }

    /**
     * 跳转至发送新鲜事页面</br>
     */
    private void gotoPostFeedActivity() {
        Intent postIntent = new Intent(getActivity(), PostFeedActivity.class);
        startActivity(postIntent);
    }

    @Override
    public void initAdapter() {
        // 添加Header
        View headerView = LayoutInflater.from(getActivity()).inflate(
                ResFinder.getLayout("umeng_comm_search_header_view"), null);
        headerView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                getActivity().startActivity(intent);
            }
        });
        mFeedsListView.addHeaderView(headerView);
        super.initAdapter();
    }

    @Override
    protected void showCommentLayout() {
        super.showCommentLayout();
        AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
        alphaAnimation.setDuration(100);
        alphaAnimation.setFillAfter(true);
        mPostBtn.startAnimation(alphaAnimation);
    }

    @Override
    protected void hideCommentLayout() {
        super.hideCommentLayout();
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(500);
        alphaAnimation.setFillAfter(true);
        mPostBtn.startAnimation(alphaAnimation);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        isShowToast = isVisibleToUser;
    }

}
