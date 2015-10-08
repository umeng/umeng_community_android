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

import java.util.Iterator;

import android.text.TextUtils;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.beans.Like;
import com.umeng.comm.core.constants.ErrorCode;
import com.umeng.comm.core.listeners.Listeners.OnResultListener;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.SimpleResponse;
import com.umeng.comm.core.nets.uitls.NetworkUtils;
import com.umeng.comm.ui.mvpview.MvpLikeView;
import com.umeng.comm.ui.presenter.BasePresenter;
import com.umeng.comm.ui.utils.BroadcastUtils;

public class LikePresenter extends BasePresenter {

    MvpLikeView mLikeViewInterface;
    FeedItem mFeedItem;
    private OnResultListener mListener = null;

    public LikePresenter() {
    }

    public LikePresenter(MvpLikeView viewInterface) {
        mLikeViewInterface = viewInterface;
    }

    /**
     * 用户点赞操作</br>
     * 
     * @param feedId 该条feed的id
     */
    public void postLike(final String feedId) {

        SimpleFetchListener<SimpleResponse> listener = new SimpleFetchListener<SimpleResponse>() {

            @Override
            public void onComplete(SimpleResponse response) {
                if (NetworkUtils.handleResponseComm(response)) {
                    return;
                }

                if (!TextUtils.isEmpty(response.id)) {
                    likeSuccess(feedId, response.id);
                } else if (ErrorCode.LIKED_CODE == response.errCode) {
                    mFeedItem.isLiked = true;
                    if (mLikeViewInterface != null) {
                        mLikeViewInterface.like(true);
                        mLikeViewInterface.updateLikeView("");
                    }
                }
            }
        };
        mCommunitySDK.postLike(feedId, listener);

    }

    protected FeedItem findFeedWithId(String feedId) {
        return mFeedItem != null && mFeedItem.id.equals(feedId) ? mFeedItem : new FeedItem();
    }

    public void setFeedItem(FeedItem feedItem) {
        this.mFeedItem = feedItem;
    }

    private void likeSuccess(String feedId, String likeId) {
        Like like = new Like();
        CommUser likeUser = CommConfig.getConfig().loginedUser;
        like.id = likeId;
        like.creator = likeUser;
        mFeedItem.isLiked = true;

        // 通过feed id找到feed
        final FeedItem targetFeedItem = findFeedWithId(feedId);
        targetFeedItem.likes.add(like);
        targetFeedItem.isLiked = true;
        targetFeedItem.likeCount++;

        BroadcastUtils.sendFeedUpdateBroadcast(mContext, targetFeedItem);

        // 保存到数据库
        mDatabaseAPI.getLikeDBAPI().saveLikesToDB(targetFeedItem);
        mDatabaseAPI.getFeedDBAPI().saveFeedToDB(targetFeedItem);

        if (mLikeViewInterface != null) {
            mLikeViewInterface.like(true);
            mLikeViewInterface.updateLikeView("");
        }
        if (mListener != null) {
            mListener.onResult(0);
        }
    }

    public void postUnlike(final String feedId) {
        SimpleFetchListener<SimpleResponse> listener = new SimpleFetchListener<SimpleResponse>() {

            @Override
            public void onComplete(SimpleResponse response) {
                // 判断用户是否被禁言
                if (NetworkUtils.handleResponseComm(response)
                        || NetworkUtils.handResponseWithDefaultCode(response)) {
                    return ;
                }
                mFeedItem.isLiked = false;
                mFeedItem.likeCount--;
                String likeId = getLikeId();
                removeLike();
                BroadcastUtils.sendFeedUpdateBroadcast(mContext, mFeedItem);
                mDatabaseAPI.getFeedDBAPI().saveFeedToDB(mFeedItem);
                mDatabaseAPI.getLikeDBAPI().deleteLikesFromDB(mFeedItem.id, likeId);
                if (mLikeViewInterface != null) {
                    mLikeViewInterface.like(false);
                    mLikeViewInterface.updateLikeView("");
                }
                if (mListener != null) {
                    mListener.onResult(0);
                }
            }
        };
        mCommunitySDK.postUnLike(feedId, listener);
    }

    /**
     * 获取当前用户点赞的likeid</br>
     * 
     * @return
     */
    public String getLikeId() {
        String id = CommConfig.getConfig().loginedUser.id;
        Iterator<Like> iterator = mFeedItem.likes.iterator();
        while (iterator.hasNext()) {
            Like like = iterator.next();
            if (like.creator.id.equals(id)) {
                return like.id;
            }
        }
        return null;
    }

    /**
     * 移除自己点赞数据</br>
     */
    private void removeLike() {
        String id = CommConfig.getConfig().loginedUser.id;
        Iterator<Like> iterator = mFeedItem.likes.iterator();
        while (iterator.hasNext()) {
            Like like = iterator.next();
            if (like.creator.id.equals(id)) {
                iterator.remove();
                break;
            }
        }
    }

    public void setResultListener(OnResultListener listener) {
        this.mListener = listener;
    }

}
