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

import java.util.List;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.constants.ErrorCode;
import com.umeng.comm.core.listeners.Listeners.OnResultListener;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.LoginResponse;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.adapters.ActiveUserAdapter;
import com.umeng.comm.ui.adapters.RecommendTopicAdapter.FollowListener;
import com.umeng.comm.ui.mvpview.MvpActiveUserFgView;
import com.umeng.comm.ui.presenter.impl.ActiveUserFgPresenter;
import com.umeng.comm.ui.widgets.RefreshLvLayout;

/**
 * 活跃用户的Fragment,用户可以关注、取消关注这些活跃用户
 */
public class ActiveUserFragment extends BaseFragment<List<CommUser>, ActiveUserFgPresenter>
        implements MvpActiveUserFgView {

    protected RefreshLvLayout mRefreshLvLayout;
    protected ActiveUserAdapter mAdapter;
    private OnResultListener mAnimationListener = null;

    @Override
    protected int getFragmentLayout() {
        return ResFinder.getLayout("umeng_comm_active_user_layout");
    }

    @Override
    protected void initWidgets() {
        mRefreshLvLayout = (RefreshLvLayout) mRootView.findViewById(ResFinder
                .getId("umeng_comm_swipe_layout"));
        mAdapter = new ActiveUserAdapter(getActivity());
        mListView = mRefreshLvLayout.findRefreshViewById(ResFinder
                .getId("umeng_comm_active_user_listview"));
        mRefreshLvLayout.setAdapter(mAdapter);
        mRefreshLvLayout.setEnabled(false);
        mAdapter.setFromFindPage(true);
        mAdapter.setFollowListener(mListener);
        if (mAnimationListener != null) {
            mRefreshLvLayout.setOnScrollDirectionListener(new OnResultListener() {

                @Override
                public void onResult(int status) {
                    if (!isScrollEffective || mListView == null) {
                        return;
                    }
                    // 1:向上滑动且第一项显示 (up)
                    // 0:向下且大于第一项 (down)
                    int firstVisible = mListView.getFirstVisiblePosition();
                    int headerCount = mListView.getHeaderViewsCount();
                    if ((status == 1 && firstVisible >= headerCount)
                            || (status == 0 && firstVisible == headerCount)) {
                        mAnimationListener.onResult(status);
                    }
                }
            });
        }
    }

    @Override
    protected ActiveUserFgPresenter createPresenters() {
        Bundle bundle = getArguments();
        Topic topic = bundle != null ? (Topic) bundle.getParcelable(Constants.TAG_TOPIC)
                : new Topic();
        mPresenter = new ActiveUserFgPresenter(this, topic);
        return mPresenter;
    }

    public static ActiveUserFragment newActiveUserFragment(Topic topic) {
        ActiveUserFragment activeUserFragment = new ActiveUserFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.TAG_TOPIC, topic);
        activeUserFragment.setArguments(bundle);
        return activeUserFragment;
    }

    private FollowListener<CommUser> mListener = new FollowListener<CommUser>() {

        @Override
        public void onFollowOrUnFollow(final CommUser user, final ToggleButton toggleButton,
                final boolean isFollow) {
            CommonUtils.checkLoginAndFireCallback(getActivity(),
                    new SimpleFetchListener<LoginResponse>() {

                        @Override
                        public void onComplete(LoginResponse response) {
                            if (response.errCode != ErrorCode.NO_ERROR) {
                                toggleButton.setChecked(!toggleButton.isChecked());
                                return;
                            }
                            if (isFollow) {
                                mPresenter.followUser(user, toggleButton);
                            } else {
                                mPresenter.cancelFollowUser(user, toggleButton);
                            }
                        }
                    });
        }
    };

    @Override
    public void onRefreshStart() {
        mRefreshLvLayout.setRefreshing(true);
    }

    @Override
    public void onRefreshEnd() {
        mRefreshLvLayout.setRefreshing(false);
        mRefreshLvLayout.setLoading(false);
    }

    @Override
    public List<CommUser> getBindDataSource() {
        return mAdapter.getDataSource();
    }

    @Override
    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void hideEmptyView() {

    }

    @Override
    public void showEmptyView() {

    }

    public void setOnAnimationListener(final OnResultListener listener) {
        mAnimationListener = listener;
    }

    private ListView mListView = null;
    private boolean isScrollEffective = false;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        isScrollEffective = isVisibleToUser;
    }

}
