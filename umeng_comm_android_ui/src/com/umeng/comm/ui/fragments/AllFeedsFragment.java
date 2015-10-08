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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.TextView;

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
        FeedListPresenter presenter = new FeedListPresenter(this, true);
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
            if (nums <= 0) {
                mTipView.setText(ResFinder.getString("umeng_comm_no_newfeed_tips"));
            } else {
                mTipView.setText(nums + "条新内容");
            }
          showNewFeedTips();
        }
    };
    
    /**
     * 
     * 显示[更新N条新feed]】的View</br>
     */
    private void showNewFeedTips(){
        mTipView.setVisibility(View.VISIBLE);
        Animation showAnimation = new AlphaAnimation(0.2f, 1);
        showAnimation.setDuration(400);
        showAnimation.setFillAfter(true);
        showAnimation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                dismissNewFeedTips();
            }
        });
        mTipView.startAnimation(showAnimation);
    }
    
    /**
     * 
     * 隐藏[更新N条feed]的View。注意：该方法必须由{@link #showNewFeedTips}的AnimationListener回调中被调用</br>
     */
    private void dismissNewFeedTips(){
        Animation animation = new AlphaAnimation(1, 0);
        animation.setStartOffset(800);
        animation.setDuration(500);
        animation.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mTipView.setVisibility(View.GONE);
            }
        });
        mTipView.startAnimation(animation);
    }

    // [注意]主页的Feed来源于好友+话题，取消对好友的关注不能删除该用户的Feed
    @Override
    protected void onCancelFollowUser(CommUser user) {
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
        mTipView = (TextView) headerView.findViewById(ResFinder.getId("umeng_comm_feeds_tips"));
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

    /**
     * 主动调用加载数据。 【注意】该接口仅仅在退出登录时，跳转到FeedsActivity清理数据后重新刷新数据</br>
     */
    public void loadFeedFromServer() {
        if (mPresenter != null) {
            mPresenter.loadDataFromServer();
        }
    }

}
