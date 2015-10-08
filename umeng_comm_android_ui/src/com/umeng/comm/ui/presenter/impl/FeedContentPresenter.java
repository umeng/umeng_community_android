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

import android.content.Context;
import android.content.Intent;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.beans.ImageItem;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.constants.ErrorCode;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.LoginResponse;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.activities.FeedDetailActivity;
import com.umeng.comm.ui.activities.UserInfoActivity;
import com.umeng.comm.ui.dialogs.ImageBrowser;

/**
 * @author mrsimple
 */
public class FeedContentPresenter extends BaseFeedPresenter {
    ImageBrowser mImageBrowser;
    FeedItem mFeedItem;

    @Override
    public void attach(Context context) {
        super.attach(context);
        mImageBrowser = new ImageBrowser(context);
    }

    /**
     * 跳转到转发页面</br>
     */
    private void gotoFeedDetailActivity(FeedItem feedItem) {
        Intent intent = new Intent(mContext, FeedDetailActivity.class);
        feedItem.extraData.clear();
        intent.putExtra(Constants.FEED, feedItem);
        mContext.startActivity(intent);
    }

    public void clickFeedItem() {
        gotoFeedDetailActivity(mFeedItem);
    }

    public void clickOriginFeedItem(FeedItem feedItem) {
        mFeedItem = feedItem;
        if (mFeedItem.sourceFeed != null
                && mFeedItem.sourceFeed.status >= FeedItem.STATUS_SPAM) {
            ToastMsg.showShortMsgByResName("umeng_comm_feed_deleted");
            return;
        }
        gotoFeedDetailActivity(getForwardDetailFeed());
    }

    /**
     * 点击转发feed的原始feed时需要从内容中删除原始作者的名字
     * 
     * @return
     */
    private FeedItem getForwardDetailFeed() {
        FeedItem feedItem = mFeedItem.sourceFeed;
        if (feedItem != null) {
            String feedText = feedItem.text;
            // 如果点击是被转发的内容,那么将@原始feed的创建者的用户名从内容中去掉
            int creatorNameIndex = feedText.indexOf(":");
            if (feedText.startsWith("@") && creatorNameIndex >= 0) {
                FeedItem originfeedItem = feedItem.clone();
                int length = feedText.length();
                int start = creatorNameIndex + 1;
                originfeedItem.text = feedText.substring(start, length);
                feedItem = originfeedItem;
            }
        } else {
            feedItem = mFeedItem;
        }
        return feedItem;
    }

    public void setFeedItem(FeedItem feedItem) {
        this.mFeedItem = feedItem;
    }

    protected FeedItem findFeedWithId(String feedId) {
        return mFeedItem.id.equals(feedId) ? mFeedItem : new FeedItem();
    }

    /**
     * 跳转到个人中心。</br>
     * 
     * @param user
     */
    public void gotoUserInfoActivity(final CommUser user, final String containerClassName) {
        CommonUtils.checkLoginAndFireCallback(mContext,
                new SimpleFetchListener<LoginResponse>() {

                    @Override
                    public void onComplete(LoginResponse response) {
                        // 取消登录情况，不做任何提示
                        if (response.errCode == ErrorCode.CANCAL_CODE) {
                            return;
                        }
                        if (response.errCode != ErrorCode.NO_ERROR) {
                            ToastMsg.showShortMsgByResName("umeng_comm_login_failed");
                            return;
                        }

                        Intent intent = new Intent(mContext, UserInfoActivity.class);
                        if (user == null) {// 来自开发者外部调用的情况
                            intent.putExtra(Constants.TAG_USER, CommConfig.getConfig().loginedUser);
                        } else {
                            intent.putExtra(Constants.TAG_USER, user);
                        }
                        intent.putExtra(Constants.TYPE_CLASS,
                                containerClassName);
                        mContext.startActivity(intent);
                    }
                });
    }

    public void jumpToImageBrowser(List<ImageItem> images, int position) {
        mImageBrowser.setImageList(images, position);
        mImageBrowser.show();
    }

    @Override
    public void loadDataFromServer() {
    }

    @Override
    public void loadDataFromDB() {

    }
}
