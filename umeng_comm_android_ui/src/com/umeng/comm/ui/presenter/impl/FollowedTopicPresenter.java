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

import java.util.List;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.db.ctrl.impl.DatabaseAPI;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.TopicResponse;
import com.umeng.comm.core.nets.uitls.NetworkUtils;
import com.umeng.comm.ui.mvpview.MvpRecommendTopicView;

/**
 * 用户关注的话题Presenter
 * 
 * @author mrsimple
 */
public class FollowedTopicPresenter extends TopicFgPresenter {

    String mNextPage;

    String mUid;

    public FollowedTopicPresenter(String uid, MvpRecommendTopicView recommendTopicView) {
        super(recommendTopicView);
        mUid = uid;
    }

    @Override
    public void loadDataFromDB() {
        mDatabaseAPI.getTopicDBAPI().loadTopicsFromDB(mUid, mDbFetchListener);
    }

    @Override
    public void loadDataFromServer() {
        mCommunitySDK.fetchFollowedTopics(mUid,
                new SimpleFetchListener<TopicResponse>() {

                    @Override
                    public void onComplete(final TopicResponse response) {
                        if (NetworkUtils.handleResponseAll(response)) {
                            mRecommendTopicView.onRefreshEnd();
                            return;
                        }

                        List<Topic> results = response.result;
                        List<Topic> mDatas = mRecommendTopicView.getBindDataSource();
                        // 去除重复的
                        mDatas.removeAll(results);
                        mDatas.addAll(results);
                        userTopicPolicy(mUid, results);
                        mRecommendTopicView.notifyDataSetChanged();
                        // 保存话题本身的数据后再保存用户关注的话题到数据库
                        DatabaseAPI.getInstance().getTopicDBAPI()
                                .saveFollowedTopicsToDB(mUid, results);
                        mRecommendTopicView.onRefreshEnd();
                    }

                });
    }

    /**
     * 对用户关注的话题设置已关注flag。在获取用户关注的话题时，server不返回is_focused。
     * 这里仅仅只针用户为当前登录用户时，才改变该值</br>
     * 
     * @param topics 从server端获取到的最新feed
     */
    private void userTopicPolicy(String uid, List<Topic> topics) {
        CommUser loginUser = CommConfig.getConfig().loginedUser;
        if (loginUser.id.equals(uid)) {
            for (Topic topic : topics) {
                topic.isFocused = true;
            }
        }
    }
}
