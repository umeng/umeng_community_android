/**
 * 
 */

package com.umeng.comm.ui.presenter.impl;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.db.ctrl.FollowDBAPI;
import com.umeng.comm.core.db.ctrl.impl.DatabaseAPI;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.FansResponse;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.ui.mvpview.MvpFollowedUserView;
import com.umeng.comm.ui.presenter.BaseFragmentPresenter;
import com.umeng.comm.ui.utils.BroadcastUtils;
import com.umeng.comm.ui.utils.BroadcastUtils.BROADCAST_TYPE;
import com.umeng.comm.ui.utils.BroadcastUtils.DefalutReceiver;

import java.util.List;

/**
 * 
 */
public class FollowedUserFgPresenter extends BaseFragmentPresenter<List<CommUser>> {

    protected String mUid;
    protected MvpFollowedUserView mFollowedUserView;
    private FollowDBAPI mFollowDBAPI = DatabaseAPI.getInstance().getFollowDBAPI();
    protected String nextPageUrl;
    private boolean hasRefresh = false;
    protected boolean isFollowPage = true;

    public FollowedUserFgPresenter(MvpFollowedUserView followedUserView, String uid) {
        this.mFollowedUserView = followedUserView;
        this.mUid = uid;
    }

    @Override
    public void attach(Context context) {
        super.attach(context);
        BroadcastUtils.registerUserBroadcast(mContext, mReceiver);
    }

    @Override
    public void detach() {
        BroadcastUtils.unRegisterBroadcast(mContext, mReceiver);
    }

    @Override
    public void loadDataFromServer() {
        mCommunitySDK.fetchFollowedUser(mUid, new FetchListener<FansResponse>() {

            @Override
            public void onStart() {
                mFollowedUserView.onRefreshStart();
            }

            @Override
            public void onComplete(FansResponse response) {
                final List<CommUser> followedUsers = response.result;
                // 根据response进行Toast
                if (mFollowedUserView.handleResponse(response)) {
                    mFollowedUserView.onRefreshEnd();
                    return;
                }
                // 保存数据
                if (CommonUtils.isMyself(new CommUser(mUid))) {
                    mFollowDBAPI.follow(followedUsers);
                }
                mFollowedUserView.executeCallback(followedUsers.size());
                // 更新GridView
                List<CommUser> dataSource = mFollowedUserView.getBindDataSource();
                followedUsers.removeAll(dataSource);
                if (followedUsers.size() > 0) {
                    dataSource.addAll(followedUsers);
                    mFollowedUserView.notifyDataSetChanged();
                }
                // 解析下一页地址
                parseNextpageUrl(response, true);
                mFollowedUserView.onRefreshEnd();
            }
        });
    }

    private DefalutReceiver mReceiver = new DefalutReceiver() {
        public void onReceiveUser(Intent intent) {
            if (isFollowPage && mUid.equals(CommConfig.getConfig().loginedUser.id)) {
                CommUser user = getUser(intent);// 取消关注某个用户
                BROADCAST_TYPE type = getType(intent);
                onUserFollowStateChange(user, type);
            }
        }
    };

    protected Activity convertContextToActivity() {
        if (mContext instanceof Activity) {
            return (Activity) mContext;
        }

        return null;
    }

    @Override
    public void loadDataFromDB() {
        if ( !mUid.equals(CommConfig.getConfig().loginedUser.id) ) {
            return ;
        }
        mFollowDBAPI.loadFollowedUsersFromDB(mUid, new
                SimpleFetchListener<List<CommUser>>() {
                    @Override
                    public void onComplete(List<CommUser> fans) {
                        if (hasRefresh) {

                        }
                        if (CommonUtils.isActivityAlive(convertContextToActivity())
                                && !CommonUtils.isListEmpty(fans)) {
                            List<CommUser> dataSource = mFollowedUserView.getBindDataSource();
                            fans.removeAll(dataSource);
                            if (fans.size() > 0) {
                                dataSource.addAll(fans);
                                mFollowedUserView.notifyDataSetChanged();
                            }
                            mFollowedUserView.executeCallback(fans.size());
                        }
                    }
                });
    }

    @Override
    public void loadMoreData() {
        if (TextUtils.isEmpty(nextPageUrl)) {
            mFollowedUserView.onRefreshEnd();
            return;
        }

        mCommunitySDK.fetchNextPageData(nextPageUrl, FansResponse.class,
                new SimpleFetchListener<FansResponse>() {

                    @Override
                    public void onComplete(FansResponse response) {
                        mFollowedUserView.onRefreshEnd();
                        // 根据response进行Toast
                        if (mFollowedUserView.handleResponse(response)) {
                            return;
                        }
                        // 保存到数据库
                        mFollowDBAPI.follow(response.result);
                        appendUsers(response.result);
                        parseNextpageUrl(response, false);
                    }
                });
    }

    /**
     * 追加已关注的用户，并刷新adapter</br>
     * 
     * @param uewUsers 新关注的好友
     */
    protected void appendUsers(List<CommUser> newUsers) {
        List<CommUser> dataSource = mFollowedUserView.getBindDataSource();
        newUsers.removeAll(dataSource);
        dataSource.addAll(newUsers);
        mFollowedUserView.notifyDataSetChanged();
    }

    /**
     * 在其他页面对某个用户进行取消关注、关注之后需要从关注列表中移除或者添加
     * 
     * @param user
     * @param type
     */
    protected void onUserFollowStateChange(CommUser user, BROADCAST_TYPE type) {
        List<CommUser> dataSource = mFollowedUserView.getBindDataSource();
        if (type == BROADCAST_TYPE.TYPE_USER_FOLLOW ) {
            if (!dataSource.contains(user)) {
                dataSource.add(user);
                mFollowDBAPI.follow(user);
            }
        } else {
            dataSource.remove(user);
            // 从DB中移除
            mFollowDBAPI.unfollow(user);
        }
        mFollowedUserView.notifyDataSetChanged();
    }

    protected void parseNextpageUrl(FansResponse response, boolean fromRefersh) {
        if (fromRefersh && TextUtils.isEmpty(nextPageUrl) && !hasRefresh) {
            hasRefresh = true;
            nextPageUrl = response.nextPageUrl;
        } else if (!fromRefersh) {
            nextPageUrl = response.nextPageUrl;
        }
    }
}
