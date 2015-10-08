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

package com.umeng.comm.ui.dialogs;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.db.ctrl.impl.DatabaseAPI;
import com.umeng.comm.core.imageloader.ImgDisplayOption;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.FansResponse;
import com.umeng.comm.core.nets.uitls.NetworkUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.adapters.PickerAdapter;
import com.umeng.comm.ui.adapters.viewholders.FriendItemViewHolder;

/**
 * 发布Feed时@好友的Dialog
 */
public class AtFriendDialog extends PickerDialog<CommUser> {
    /**
     * 当前已登录的用户
     */
    private CommUser mUser = CommConfig.getConfig().loginedUser;
    /**
     * @ 好友的下一页url地址。每次从server获取好友列表时，都能够拿到该url，因此不cache到DB
     */
    private String mNextPageUrl;
    
    private volatile AtomicBoolean mUpdateNextPageUrl = new AtomicBoolean(true);

    public AtFriendDialog(Context context) {
        this(context, 0);
    }

    public AtFriendDialog(Context context, int theme) {
        super(context, theme);
        setContentView(this.createContentView());
        loadFriendsFromDB(mUser.id);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        loadDataFromServer();
    }

    @Override
    protected void setupAdater() {
        mAdapter = new PickerAdapter<CommUser>(getContext()) {

            @Override
            public void bindData(FriendItemViewHolder viewHolder, CommUser item, int position) {
                viewHolder.mTextView.setText(item.name);
                viewHolder.mDetailTextView.setVisibility(View.GONE);
                ImgDisplayOption option = ImgDisplayOption.getOptionByGender(item.gender);
                viewHolder.mImageView.setImageUrl(item.iconUrl, option);
            }
        };

        mRefreshLvLayout.setAdapter(mAdapter);
        String title = ResFinder.getString("umeng_comm_my_friends");
        mTitleTextView.setText(title);
        mListView.setFooterDividersEnabled(true);
        mListView.setOverscrollFooter(null);
    }

    @Override
    protected void setupLvOnItemClickListener() {
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pickItemAtPosition(position);
            }

        });
    }

    /**
     * 从数据库中加载最近的联系人。</br>
     * 
     * @param uid
     */
    private void loadFriendsFromDB(final String uid) {
        DatabaseAPI.getInstance().getFollowDBAPI()
                .loadFollowedUsersFromDB(uid, new SimpleFetchListener<List<CommUser>>() {

                    @Override
                    public void onComplete(List<CommUser> result) {
                        mAdapter.addData(result);
                    }
                });
    }

    @Override
    public void loadDataFromServer() {
        mSdkImpl.fetchFollowedUser(mUser.id, new FetchListener<FansResponse>() {

            @Override
            public void onStart() {
                mRefreshLvLayout.setRefreshing(true);
            }

            @Override
            public void onComplete(FansResponse response) {
                mRefreshLvLayout.setRefreshing(false);
                if ( NetworkUtils.handleResponseAll(response) ) {
                    return ;
                }
                if ( mUpdateNextPageUrl.get() ) {
                    mNextPageUrl = response.nextPageUrl;
                    mUpdateNextPageUrl.set(false);
                }
                handleResultData(response);
            }
        });
    }

    @Override
    public void loadMore() {
        if (TextUtils.isEmpty(mNextPageUrl)) {
            mRefreshLvLayout.setLoading(false);
            return;
        }
        mSdkImpl.fetchNextPageData(mNextPageUrl, FansResponse.class,
                new FetchListener<FansResponse>() {

                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onComplete(FansResponse response) {
                        mRefreshLvLayout.setLoading(false);
                        if ( NetworkUtils.handleResponseAll(response) ) {
                            return ;
                        }
                        mNextPageUrl = response.nextPageUrl;
                        handleResultData(response);
                    }
                });
    }

    @Override
    protected void pickItemAtPosition(int position) {
        super.pickItemAtPosition(position);
        mSelectedItem = null;
    }

    /**
     * 处理从server加载后返回的数据</br>
     * 
     * @param resp
     */
    private void handleResultData(FansResponse response) {
        List<CommUser> users = response.result;
        List<CommUser> sourceList = mAdapter.getDataSource();
        users.removeAll(sourceList);
        mAdapter.addData(users);

        // 将我关注的好友的owner id 设置为当前用户的id.
        for (CommUser commUser : users) {
            commUser.extraData.putString(Constants.FOLLOWED_USER_ID, mUser.id);
        }
        // 保存关注的用户到DB
        DatabaseAPI.getInstance().getFollowDBAPI().follow(users);
    }

}
