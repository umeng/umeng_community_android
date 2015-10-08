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

import android.app.Activity;
import android.os.Bundle;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.constants.ErrorCode;
import com.umeng.comm.core.db.ctrl.impl.DatabaseAPI;
import com.umeng.comm.core.listeners.Listeners.OnResultListener;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.Response;
import com.umeng.comm.core.nets.uitls.NetworkUtils;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.mvpview.MvpTopicDetailView;
import com.umeng.comm.ui.presenter.BaseActivityPresenter;
import com.umeng.comm.ui.presenter.BasePresenter;
import com.umeng.comm.ui.utils.BroadcastUtils;

import java.util.List;

/**
 * 
 */
public class TopicDetailPresenter extends BasePresenter implements BaseActivityPresenter {

    private MvpTopicDetailView mTopicDetailView;
    private Activity mActivity;

    public TopicDetailPresenter(Activity activity, MvpTopicDetailView topicDetailView) {
        this.mActivity = activity;
        this.mTopicDetailView = topicDetailView;
        attach(activity);
    }

    @Override
    public void onCreate(Bundle bundle) {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroy() {
        mActivity = null;
        mTopicDetailView = null;
    }

    public void checkIsFollowed(final String topicId, final OnResultListener listener) {
        String uid = CommConfig.getConfig().loginedUser.id;
        DatabaseAPI.getInstance().getTopicDBAPI()
                .loadTopicsFromDB(uid, new SimpleFetchListener<List<Topic>>() {

                    @Override
                    public void onComplete(List<Topic> topics) {
                        int flag = 0;
                        if (topics.size() > 0) {
                            for (Topic topic : topics) {
                                if (topicId.equals(topic.id)) {
                                    flag = 1;
                                    break;
                                }
                            }
                        }
                        listener.onResult(flag);
                    }
                });
    }

    /**
     * 关注某个话题</br>
     * 
     * @param id 话题的id
     */
    public void followTopic(final Topic topic) {
        // 关注话题
        mCommunitySDK.followTopic(topic,
                new SimpleFetchListener<Response>() {

                    @Override
                    public void onComplete(Response response) {
                        if ( NetworkUtils.handleResponseComm(response) ) {
                            return ;
                        }
                        String resName = "";
                        if (response.errCode == ErrorCode.NO_ERROR) {
                            resName = "umeng_comm_topic_follow_success";
                            mTopicDetailView.setToggleButtonStatus(true);
                            topic.isFocused = true;
                            CommUser user = CommConfig.getConfig().loginedUser;
                            DatabaseAPI.getInstance().getTopicDBAPI()
                                    .saveFollowedTopicToDB(user.id, topic);
                            BroadcastUtils
                                    .sendTopicFollowBroadcast(mActivity, topic);
                        } else if (response.errCode == ErrorCode.ORIGIN_TOPIC_DELETE_ERR_CODE) {
                            // 在数据库中删除该话题并Toast
                            resName = "umeng_comm_topic_has_deleted";
                            DatabaseAPI.getInstance().getTopicDBAPI()
                                    .deleteTopicDataFromDB(topic.id);
                        }else if (response.errCode == ErrorCode.ERROR_TOPIC_FOCUSED) {
                            resName = "umeng_comm_topic_has_focused";
                            mTopicDetailView.setToggleButtonStatus(true);
                        } else {
                            resName = "umeng_comm_topic_follow_failed";
                            mTopicDetailView.setToggleButtonStatus(false);
                        }
                        ToastMsg.showShortMsgByResName(resName);
                    }
                });
    }

    /**
     * 取消关注某个话题</br>
     * 
     * @param id
     */
    public void cancelFollowTopic(final Topic topic) {
        // 取消关注话题
        mCommunitySDK.cancelFollowTopic(topic,
                new SimpleFetchListener<Response>() {

                    @Override
                    public void onComplete(Response response) {
                        if ( NetworkUtils.handleResponseComm(response) ) {
                            return ;
                        }
                        String resName = "";
                        if (response.errCode == ErrorCode.NO_ERROR) {
                            resName = "umeng_comm_topic_cancel_success";
                            topic.isFocused = false;
                            mTopicDetailView.setToggleButtonStatus(false);
                            DatabaseAPI.getInstance().getTopicDBAPI().deleteTopicFromDB(topic.id);
                            BroadcastUtils.sendTopicCancelFollowBroadcast(mActivity,
                                    topic);
                        } else if (response.errCode == ErrorCode.ORIGIN_TOPIC_DELETE_ERR_CODE) {
                            ToastMsg.showShortMsgByResName("umeng_comm__topic_has_deleted");
                            DatabaseAPI.getInstance().getTopicDBAPI()
                                    .deleteTopicDataFromDB(topic.id);
                        } else if (response.errCode == ErrorCode.ERROR_TOPIC_NOT_FOCUSED) {
                            resName = "umeng_comm_topic_has_not_focused";
                            mTopicDetailView.setToggleButtonStatus(false);
                        } else {
                            resName =
                                    "umeng_comm_topic_cancel_failed";
                            mTopicDetailView.setToggleButtonStatus(true);
                        }
                        ToastMsg.showShortMsgByResName(resName);
                    }
                });
    }

}
