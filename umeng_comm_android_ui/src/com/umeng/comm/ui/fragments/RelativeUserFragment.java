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

import android.view.View;

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.constants.HttpProtocol;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.presenter.impl.ActiveUserFgPresenter;
import com.umeng.comm.ui.presenter.impl.RelativeUserFgPresenter;
import com.umeng.comm.ui.utils.BroadcastUtils;
import com.umeng.comm.ui.utils.BroadcastUtils.BROADCAST_TYPE;
import com.umeng.comm.ui.utils.BroadcastUtils.DefalutReceiver;
import com.umeng.comm.ui.widgets.RefreshLayout.OnLoadListener;

import java.util.List;

/**
 * 相关用户Fragment
 */
public class RelativeUserFragment extends RecommendUserFragment {

    private View mBackView = null;

    @Override
    protected void initWidgets() {
        super.initWidgets();
        List<CommUser> users = getArguments().getParcelableArrayList(Constants.TAG_USERS);
        mAdapter.addData(users);
        mRefreshLvLayout.setRefreshing(true);
        String nextPageUrl = getArguments().getString(HttpProtocol.NAVIGATOR_KEY);
        mPresenter.setNextPageUrl(nextPageUrl);
        mRootView.findViewById(ResFinder.getId("umeng_comm_save_bt")).setVisibility(View.GONE);
        mBackView = mRootView.findViewById(ResFinder.getId("umeng_comm_setting_back"));
        mBackView.setVisibility(View.VISIBLE);
        mBackView.setOnClickListener(this);
        mTitleTextView.setText(ResFinder.getString("umeng_comm_relation_user"));
        mRefreshLvLayout.setOnLoadListener(new OnLoadListener() {

            @Override
            public void onLoad() {
                mPresenter.loadMoreData();
            }
        });
        BroadcastUtils.registerUserBroadcast(getActivity(), mReceiver);
    }

    @Override
    protected ActiveUserFgPresenter createPresenters() {
        return new RelativeUserFgPresenter(this);
    }

    private DefalutReceiver mReceiver = new DefalutReceiver() {
        public void onReceiveUser(android.content.Intent intent) {
            CommUser newUser = getUser(intent);
            BROADCAST_TYPE type = getType(intent);
            boolean follow = true;
            if (type == BROADCAST_TYPE.TYPE_USER_FOLLOW) {
                follow = true;
            } else if (type == BROADCAST_TYPE.TYPE_USER_CANCEL_FOLLOW) {
                follow = false;
            }
            List<CommUser> users = mAdapter.getDataSource();
            for (CommUser user : users) {
                if (user.id.equals(newUser.id)) {
                    user.extraData.putBoolean(Constants.IS_FOCUSED, follow);
                    break;
                }
            }
            mAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onClick(View v) {
        if (v == mBackView) {
            getActivity().finish();
        }
    }

    @Override
    public void onDestroy() {
        BroadcastUtils.unRegisterBroadcast(getActivity(), mReceiver);
        super.onDestroy();
    }

}
