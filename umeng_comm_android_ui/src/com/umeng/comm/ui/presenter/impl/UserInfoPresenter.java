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

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.umeng.comm.core.CommunitySDK;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.constants.ErrorCode;
import com.umeng.comm.core.db.ctrl.impl.DatabaseAPI;
import com.umeng.comm.core.impl.CommunityFactory;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.listeners.Listeners.OnResultListener;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.Response;
import com.umeng.comm.core.nets.responses.ProfileResponse;
import com.umeng.comm.core.nets.uitls.NetworkUtils;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.Log;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.mvpview.MvpUserInfoView;
import com.umeng.comm.ui.presenter.BaseActivityPresenter;
import com.umeng.comm.ui.utils.BroadcastUtils;
import com.umeng.comm.ui.utils.BroadcastUtils.BROADCAST_TYPE;
import com.umeng.comm.ui.utils.BroadcastUtils.DefalutReceiver;

/**
 * 用户信息页面的Presenter
 */
public class UserInfoPresenter implements BaseActivityPresenter {

    private MvpUserInfoView mUserInfoView;
    private Activity mActivity;
    private CommunitySDK mSdkImpl;
    private List<Topic> mFollowTopics = new ArrayList<Topic>();
    private CommUser mUser;

    private int mFeedsCount;
    private int mFollowUserCount;
    private int mFansCount;

    public UserInfoPresenter(Activity activity, MvpUserInfoView userInfoView, CommUser user) {
        this.mActivity = activity;
        this.mUserInfoView = userInfoView;
        this.mUser = user;
        this.mSdkImpl = CommunityFactory.getCommSDK(activity);
    }

    @Override
    public void onCreate(Bundle bundle) {
        registerBroadcast();
        loadTopicFromDB();
        initUserInfoFromSharePref();
//        loadTopicsFromServer();
        fetchUserProfile();
        findFollowedByMe();
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroy() {
        BroadcastUtils.unRegisterBroadcast(mActivity, mReceiver);
    }

    public void loadTopicFromDB() {
        DatabaseAPI.getInstance().getTopicDBAPI()
                .loadTopicsFromDB(mUser.id, new SimpleFetchListener<List<Topic>>() {
                    @Override
                    public void onComplete(List<Topic> result) {
                        if (CommonUtils.isActivityAlive(mActivity)) {
                            mFollowTopics.clear();
                            mFollowTopics.addAll(result);
//                            mUserInfoView.cleanAndUpdateTopicView(result);
                        }
                    }
                });

    }

    private DefalutReceiver mReceiver = new DefalutReceiver() {
        public void onReceiveTopic(Intent intent) {
            if (!CommonUtils.isMyself(mUser)) {
                return;
            }
            Topic topic = getTopic(intent);
            BROADCAST_TYPE type = getType(intent);
            if (type == BROADCAST_TYPE.TYPE_TOPIC_FOLLOW) {
                mFollowTopics.add(topic);
            } else if (type == BROADCAST_TYPE.TYPE_TOPIC_CANCEL_FOLLOW) {
                mFollowTopics.remove(topic);
            }
        }

        public void onReceiveUser(Intent intent) {
            BROADCAST_TYPE type = getType(intent);
            if (type == BROADCAST_TYPE.TYPE_USER_UPDATE) {
                CommUser user = getUser(intent);
                if (user != null) {
                    mUserInfoView.setupUserInfo(user);
                    mUserInfoView.updateFansTextView(mFansCount);
                    mUserInfoView.updateFeedTextView(mFeedsCount);
                    mUserInfoView.updateFollowTextView(mFollowUserCount);
                }
            }
        }

        public void onReceiveCount(Intent intent) {
            if (!CommonUtils.isMyself(mUser)) {
                return;
            }

            BROADCAST_TYPE type = getType(intent);
            int count = getCount(intent);
            if (type == BROADCAST_TYPE.TYPE_COUNT_USER) {
                if (Math.abs(count) <= 1) {// follow or unFollow 情况
                    mFollowUserCount += count;
                    mUserInfoView.updateFollowTextView(mFollowUserCount);
                } else if (mFollowUserCount < 1) { // 从DB重加载的情况，可能加载速度慢于网络
                    mFollowUserCount = count;
                    mUserInfoView.updateFollowTextView(mFollowUserCount);
                }
            } else if (type == BROADCAST_TYPE.TYPE_COUNT_FEED) {
                if (Math.abs(count) <= 1) { // post or delete feed
                    mFeedsCount += count;
                    mUserInfoView.updateFeedTextView(mFeedsCount);
                } else if (mFeedsCount < 1) { // 从DB重加载的情况，可能加载速度慢于网络
                    mFeedsCount = count;
                    mUserInfoView.updateFeedTextView(count);
                }
            } else if (type == BROADCAST_TYPE.TYPE_COUNT_FANS) {
                if (Math.abs(count) <= 1) {
                    mFansCount += count;
                    mUserInfoView.updateFansTextView(count);
                } else if (mFansCount < 1) {
                    mFansCount = count;
                    mUserInfoView.updateFansTextView(count);
                }
            }
        }
    };

    private void registerBroadcast() {
        BroadcastUtils.registerTopicBroadcast(mActivity, mReceiver);
        BroadcastUtils.registerUserBroadcast(mActivity, mReceiver);
        BroadcastUtils.registerCountBroadcast(mActivity, mReceiver);
    }

    /**
     * 在删除feed的时候，需要将该数字-1</br>
     * 
     * @param count
     */
    public void decreaseFeedCount(int count) {
        --mFeedsCount;
    }

    private void initUserInfoFromSharePref() {
        // 此处分三个查询。
        DatabaseAPI.getInstance().getFansDBAPI()
                .queryFansCount(mUser.id, new SimpleFetchListener<Integer>() {

                    @Override
                    public void onComplete(Integer count) {
                        if (mFansCount == 0 && count > 0) {
                            mFansCount = count;
                            mUserInfoView.updateFansTextView(mFansCount);
                        }
                    }
                });

        DatabaseAPI.getInstance().getFollowDBAPI()
                .queryFollowCount(mUser.id, new SimpleFetchListener<Integer>() {

                    @Override
                    public void onComplete(Integer count) {
                        if (mFollowUserCount == 0 && count > 0) {
                            mFollowUserCount = count;
                            mUserInfoView.updateFollowTextView(mFollowUserCount);
                        }
                    }
                });

        DatabaseAPI.getInstance().getFeedDBAPI()
                .queryFeedCount(mUser.id, new SimpleFetchListener<Integer>() {

                    @Override
                    public void onComplete(Integer count) {
                        if (mFeedsCount == 0 && count > 0) {
                            mFeedsCount = count;
                            mUserInfoView.updateFeedTextView(mFeedsCount);
                        }
                    }
                });
    }

    /**
     * 获取用户信息并设置</br>
     */
    private void fetchUserProfile() {
        mSdkImpl.fetchUserProfile(mUser.id, new FetchListener<ProfileResponse>() {

            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(ProfileResponse response) {
                if (NetworkUtils.handleResponseAll(response)) {
                    return;
                }

                mUserInfoView.setToggleButtonStatus(response.hasFollowed);
                CommUser user = response.result;
                Log.d("", "### 用户信息 : " + response.toString());
                if (!TextUtils.isEmpty(user.id)) {
                    // feeds, fans, follow user个数
                    mFeedsCount = response.mFeedsCount;
                    mFollowUserCount = response.mFollowedUserCount;
                    mFansCount = response.mFansCount;
                    // 更新相关的现实VIew
                    mUserInfoView.setupUserInfo(user);
                    mUserInfoView.updateFansTextView(mFansCount);
                    mUserInfoView.updateFeedTextView(mFeedsCount);
                    mUserInfoView.updateFollowTextView(mFollowUserCount);
                }
            }
        });
    }

    /**
     * 关注某个用户</br>
     * 
     * @param uid 被关注用户的id
     */
    public void followUser(final OnResultListener listener) {
        mSdkImpl.followUser(mUser, new SimpleFetchListener<Response>() {

            @Override
            public void onComplete(Response response) {
                if ( NetworkUtils.handleResponseComm(response) ) {
                    return ;
                }
                if (response.errCode == ErrorCode.NO_ERROR) {
                    ToastMsg.showShortMsgByResName("umeng_comm_follow_user_success");
                    mUserInfoView.setToggleButtonStatus(true);
                    DatabaseAPI.getInstance().getFollowDBAPI().follow(mUser);
                    BroadcastUtils.sendUserFollowBroadcast(mActivity, mUser);
                    BroadcastUtils.sendCountUserBroadcast(mActivity, 1);
                } else if ( response.errCode == ErrorCode.ERROR_USER_FOCUSED) {
                    mUserInfoView.setToggleButtonStatus(true);
                    ToastMsg.showShortMsgByResName("umeng_comm_user_has_focused");
                } else {
                    ToastMsg.showShortMsgByResName("umeng_comm_follow_user_failed");
                    mUserInfoView.setToggleButtonStatus(false);
                }
                listener.onResult(0);
            }
        });
    }

    /**
     * 取消关注某个用户</br>
     * 
     * @param uid 需要取消关注的用户的id
     */
    public void cancelFollowUser(final OnResultListener listener) {
        mSdkImpl.cancelFollowUser(mUser, new SimpleFetchListener<Response>() {

            @Override
            public void onComplete(Response response) {
                if ( NetworkUtils.handleResponseComm(response) ) {
                    return ;
                }
                if (response.errCode == ErrorCode.NO_ERROR) {
                    ToastMsg.showShortMsgByResName("umeng_comm_follow_cancel_success");
                    mUserInfoView.setToggleButtonStatus(false);
                    DatabaseAPI.getInstance().getFollowDBAPI().unfollow(mUser);
                    // 发送取消关注的广播
                    BroadcastUtils.sendUserCancelFollowBroadcast(mActivity, mUser);
                    BroadcastUtils.sendCountUserBroadcast(mActivity, -1);
                    DatabaseAPI.getInstance().getFeedDBAPI().deleteFriendFeed(mUser.id);
                }else if (response.errCode == ErrorCode.ERROR_USER_NOT_FOCUSED) {
                    mUserInfoView.setToggleButtonStatus(false);
                    ToastMsg.showShortMsgByResName("umeng_comm_user_has_not_focused");
                } else {
                    ToastMsg.showShortMsgByResName("umeng_comm_follow_user_failed");
                    mUserInfoView.setToggleButtonStatus(true);
                }
                listener.onResult(0);
            }
        });
    }

    /**
     * 检查该用户是否是当前登录用户的好友 [ 关注 ]
     */
    public void findFollowedByMe() {
        DatabaseAPI.getInstance().getFollowDBAPI().isFollowed(mUser.id, new
                SimpleFetchListener<List<CommUser>>() {
                    @Override
                    public void onComplete(List<CommUser> results) {
                        // 确保activity没有被销毁
                        if (!CommonUtils.isActivityAlive(mActivity)) {
                            return;
                        }

                        if (!CommonUtils.isListEmpty(results)) {
                            mUserInfoView.setToggleButtonStatus(true);
                        } else {
                            mUserInfoView.setToggleButtonStatus(false);
                        }
                    }
                });

    }

    /**
     * 是否更新关注用户文本.在获取缓存数据的时候调用</br>
     * 
     * @return
     */
    public boolean isUpdateFollowUserCountTextView() {
        return mFollowUserCount == 0;
    }

    /**
     * 是否更新粉丝文本.在获取缓存数据的时候调用</br>
     * 
     * @return
     */
    public boolean isUpdateFansCountTextView() {
        return mFansCount == 0;
    }

}
