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

package com.umeng.comm.ui.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.CommUser.Permisson;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.utils.DeviceUtils;
import com.umeng.comm.core.utils.Log;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.utils.FeedViewRender;

/**
 * 转发Feed的Activity
 */
public class ForwardActivity extends PostFeedActivity {
    /**
     * 被转发的文本内容
     */
    protected TextView mFeedText;

    /**
     * 被转发的第一个图片
     */
    protected ImageView mFeedIcon;

    /**
     * 底部的话题、拍照、位置等图标的布局
     */
    protected View mBottomTabLayout;
    /**
     * 被转发的FeedItem
     */
    FeedItem mForwardedFeeditem;

    /**
     * 原feed的第一张图片，在转发的时候显示
     */
    private String mForwardImage = "";

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        Bundle extraBundle = getIntent().getExtras();
        Object forwardItem = extraBundle.getParcelable(Constants.FEED);
        if (forwardItem != null && forwardItem instanceof FeedItem) {
            mForwardedFeeditem = (FeedItem) (forwardItem);
            parseForwardData(mForwardedFeeditem);
        } else {
            Log.e(TAG, "### 转发的数据出错");
        }
        // 更新view的显示内容
        updateViewContent();
        isForwardFeed = true;
        // 转发
        mPostPresenter.setForwardFeed(true);
    }

    @Override
    protected void initLocationLayout() {
        Log.d(TAG, "### 转发不需要地理位置");
    }

    /**
     * 解析被转发FeedItem中的第一张图片</br>
     * 
     * @param item 被转发的FeedItem
     */
    private void parseFirstImage(FeedItem item) {
        if (item.imageUrls.size() > 0) {
            mForwardImage = item.imageUrls.get(0).thumbnail;
        }
    }

    /**
     * “解析”被转发的FeedItem并显示
     */
    private void parseForwardData(FeedItem item) {
        //
        if (item.sourceFeed != null) {
            item.text = "//@" + item.creator.name + " : " + item.text;
            CommUser friend = item.creator;
            item.atFriends.add(friend);
            // 原始@好友数据中添加这条feed的创建者
            mSelectFriends.add(friend);

            // 转发类型的内容
            FeedViewRender.parseTopicsAndFriends(mEditText, item);
            // 被转发的原始数据
            FeedViewRender.parseTopicsAndFriends(mFeedText, item.sourceFeed);
            parseFirstImage(item.sourceFeed);
        } else {
            FeedViewRender.parseTopicsAndFriends(mFeedText, item);
            parseFirstImage(item);
        }
        // 调整光标到开始位置
        mEditText.setSelection(0);
    }

    @Override
    protected void initViews() {
        super.initViews();

        // 转发时把输入框的大小稍微设置小一些
        mEditText.setMinimumHeight(DeviceUtils.dp2px(this, 80));
        mEditText.setHint(ResFinder.getString("umeng_comm_write_sth"));
        // 隐藏选择图片的图标
        mGridView.setVisibility(View.GONE);

        // 隐藏话题，地理位置、图片和拍照的图标
        findViewById(ResFinder.getId("umeng_community_loc_layout")).setVisibility(View.GONE);
        findViewById(ResFinder.getId("umeng_comm_loc_layout")).setVisibility(View.GONE);
        findViewById(ResFinder.getId("umeng_comm_topic_layout")).setVisibility(View.GONE);
        findViewById(ResFinder.getId("umeng_comm_take_photo_layout"))
                .setVisibility(View.GONE);
        findViewById(ResFinder.getId("umeng_comm_pick_photo_layout"))
                .setVisibility(View.GONE);
        // 设置转发时，图片跟文本layout可见
        findViewById(ResFinder.getId("umeng_comm_forward_layout"))
                .setVisibility(View.VISIBLE);

        //
        mBottomTabLayout = findViewById(ResFinder.getId("umeng_community_post_tab_bar"));
        int paddingRight = getResources().getDisplayMetrics().widthPixels
                - DeviceUtils.dp2px(this, 80);
        mBottomTabLayout
                .setPadding(mBottomTabLayout.getPaddingLeft(), mBottomTabLayout.getPaddingTop(),
                        paddingRight, mBottomTabLayout.getPaddingBottom());

        // 显示转发文本的TextView
        mFeedText = (TextView) findViewById(ResFinder.getId("umeng_comm_forward_text"));
        mFeedIcon = (ImageView) findViewById(ResFinder.getId("umeng_comm_forward_img"));
        mTopicTipView.setVisibility(View.GONE);// 转发页面不显示提示话题VIew
    }

    @Override
    protected void postFeed(FeedItem feedItem) {
        mPostPresenter.forwardFeed(feedItem, mForwardedFeeditem);
    }

    /**
     * 获取转发Feed的id。
     * 
     * @return 如果该被转发的feed为空，则返回空串; 如果被转发的feed是一条转发feed，则返回该转发feed的id；</br>
     *         否则返回该转发的id
     */
    private String getForwardFeedId() {
        if (mForwardedFeeditem == null) {
            return "";
        }

        return mForwardedFeeditem.id;
    }

    /**
     * 根据内容创建一条转发的feed</br>
     * 
     * @return 一条转发的feed
     */
    @Override
    protected FeedItem prepareFeed() {
        FeedItem newFeed = new FeedItem();

        // 被转发的项
        newFeed.sourceFeed = mForwardedFeeditem;
        newFeed.sourceFeedId = getForwardFeedId();
        String originText = mForwardedFeeditem.text;
        if (!isForwardFeed()) {
            newFeed.sourceFeed.text = "@" + mForwardedFeeditem.creator.name + ": "
                    + originText;
            CommUser friend = mForwardedFeeditem.creator;
            newFeed.sourceFeed.atFriends.add(friend);
        }

        // 转发的内容
        newFeed.text = mEditText.getText().toString().trim();
        // 转发的位置
        newFeed.location = mLocation;
        newFeed.locationAddr = getLocationAddr();
        // 转发@的好友
        newFeed.atFriends.addAll(mSelectFriends);
        // 发表的用户
        newFeed.creator = CommConfig.getConfig().loginedUser;
        // 转发用户的类型，管理员or普通用户
        newFeed.type = newFeed.creator.permisson == Permisson.ADMIN ? 1 : 0;
        return newFeed;
    }

    /**
     * 判断当前feed是否是一条转发</br>
     * 
     * @return
     */
    private boolean isForwardFeed() {
        return (mForwardedFeeditem != null);
    }

    /**
     * 更新View的显示内容</br>
     */
    private void updateViewContent() {
        // 被转发的feed的第一张图片url
        if (!TextUtils.isEmpty(mForwardImage)) {
            final String iconUrl = mForwardImage;
            Log.d("", "### 转发的图片 = " + iconUrl);
            mFeedIcon.setTag(iconUrl);
            mImageLoader.displayImage(iconUrl,
                    mFeedIcon);
        } else if (TextUtils.isEmpty(getForwardFeedId())) {
            mFeedIcon.setImageBitmap(null);
            mFeedText.setText("");
        }
    }
}
