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

package com.umeng.comm.ui.adapters.viewholders;

import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint.FontMetrics;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.umeng.comm.core.CommunitySDK;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.beans.FeedItem.CATEGORY;
import com.umeng.comm.core.constants.ErrorCode;
import com.umeng.comm.core.db.ctrl.impl.DatabaseAPI;
import com.umeng.comm.core.impl.CommunityFactory;
import com.umeng.comm.core.listeners.Listeners.LoginOnViewClickListener;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.SimpleResponse;
import com.umeng.comm.core.utils.DeviceUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ResFinder.ResType;
import com.umeng.comm.core.utils.TimeUtils;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.utils.BroadcastUtils;

/**
 * 
 */
public class FavouriteFeedItemViewHolder extends FeedItemViewHolder {

    private CommunitySDK mCommunitySDK;
    public TextView mFavoritestButton;

    // 该构造方法目前只针对FavouriteAdapter调用
    public FavouriteFeedItemViewHolder() {
        isShowFavouriteView = true;
    }

    private boolean isShowFavouriteView = false;
    private boolean isFromFeedDetailePage = false;

    public FavouriteFeedItemViewHolder(Context context, View rootView) {
        super(context, rootView);
    }

    @Override
    protected void bindFeedItemData() {
        super.bindFeedItemData();
        if (!isFromFeedDetailePage) {
            Date date = new Date(Long.parseLong(mFeedItem.addTime));
            mTimeTv.setText(TimeUtils.format(date));
        }
        RelativeLayout.LayoutParams params = (LayoutParams) mFavoritestButton.getLayoutParams();
        // [此时说明在收藏页面，对于被删除的feed，需要隐藏分享、like、转发、评论按钮]
        if (isShowFavouriteView) {
            mShareBtn.setVisibility(View.VISIBLE);
            if (mFeedItem.category == CATEGORY.FAVORITES
                    && mFeedItem.status >= FeedItem.STATUS_SPAM) {
                mShareBtn.setVisibility(View.GONE);
                mLikeCountTextView.setVisibility(View.GONE);
                mForwardCountTextView.setVisibility(View.GONE);
                mCommentCountTextView.setVisibility(View.GONE);
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.rightMargin = DeviceUtils.dp2px(mContext, 13);
                mFavoritestButton.setLayoutParams(params);
            } else {
                mLikeCountTextView.setVisibility(View.VISIBLE);
                mForwardCountTextView.setVisibility(View.VISIBLE);
                mCommentCountTextView.setVisibility(View.VISIBLE);
                // params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                removeRuleFromLayoutParam(params, RelativeLayout.ALIGN_PARENT_RIGHT);
                params.rightMargin = DeviceUtils.dp2px(mContext, 40);
                mFavoritestButton.setLayoutParams(params);
            }
        } else {
            resetInitStatus();
        }
        if (mFeedItem.category == CATEGORY.FAVORITES) {
            mFavoritestButton.setBackgroundResource(ResFinder.getResourceId(ResType.DRAWABLE,
                    "umeng_comm_favorites_pressed"));
            setSpamFeed(mFeedItem);
        } else {
            mFavoritestButton.setBackgroundResource(ResFinder.getResourceId(ResType.DRAWABLE,
                    "umeng_comm_favorites_normal"));
        }

        // 详情页面需要隐藏
        if (isFromFeedDetailePage) {
            mLikeCountTextView.setVisibility(View.GONE);
            mForwardCountTextView.setVisibility(View.GONE);
            mCommentCountTextView.setVisibility(View.GONE);
        }
    }

    private void resetInitStatus() {
        RelativeLayout.LayoutParams params = (LayoutParams) mFavoritestButton.getLayoutParams();
        mShareBtn.setVisibility(View.VISIBLE);
        mLikeCountTextView.setVisibility(View.VISIBLE);
        mForwardCountTextView.setVisibility(View.VISIBLE);
        mCommentCountTextView.setVisibility(View.VISIBLE);
        params.addRule(RelativeLayout.ALIGN_LEFT, mShareBtn.getId());
        // params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        removeRuleFromLayoutParam(params, RelativeLayout.ALIGN_PARENT_RIGHT);
        params.rightMargin = DeviceUtils.dp2px(mContext, 40);
        mFavoritestButton.setLayoutParams(params);
    }

    @SuppressLint("NewApi")
    private void removeRuleFromLayoutParam(RelativeLayout.LayoutParams param, int verb) {
        if (Build.VERSION.SDK_INT >= 17) {
            param.removeRule(verb);
        } else {
            int[] rules = param.getRules();
            rules[RelativeLayout.ALIGN_PARENT_RIGHT] = 0;
        }
    }

    @Override
    protected void inflateFromXML() {
        super.inflateFromXML();
        mFavoritestButton = findViewById(ResFinder.getId("umeng_comm_favorites_textview"));
    }

    @Override
    protected void setBaseFeeditemInfo() {
        super.setBaseFeeditemInfo();
        if (isFromFeedDetailePage) {
            mFeedTextTv.setOnClickListener(null);
        } else {
            mFeedTextTv.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mFeedItem.status >= FeedItem.STATUS_SPAM) {
                        ToastMsg.showShortMsgByResName("umeng_comm_feed_spam_deleted");
                        return;
                    }
                    mPresenter.clickFeedItem();
                }
            });
        }
        mFeedTextTv.setVisibility(View.VISIBLE);
    }

    @Override
    protected void initPresenters() {
        mCommunitySDK = CommunityFactory.getCommSDK(mContext);
        super.initPresenters();
    }

    /**
     * 设置收藏Feed spam的样式+文本内容。【仅仅针对收藏有效】</br>
     * 
     * @param feedItem
     */
    @SuppressWarnings("deprecation")
    private void setSpamFeed(FeedItem feedItem) {
        ViewGroup.LayoutParams params = mFeedTextTv.getLayoutParams();
        if (feedItem.status < FeedItem.STATUS_SPAM) { // 设置成默认的状态，避免复用导致问题
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            mFeedTextTv.setLayoutParams(params);
            mFeedTextTv.setGravity(Gravity.LEFT);
            mFeedTextTv.setBackgroundDrawable(null);
            mFeedTextTv.setText(feedItem.text);
            return;
        }
        mFeedTextTv.setVisibility(View.VISIBLE);
        String text = getToastText(feedItem.status);
        FontMetrics fm = mFeedTextTv.getPaint().getFontMetrics();
        int textHeight = (int) Math.ceil(fm.descent - fm.ascent) + 60;
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        // params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.height = textHeight;
        mFeedTextTv.setLayoutParams(params);
        mFeedTextTv.setGravity(Gravity.CENTER);
        // mFeedTextTv.setPadding(0, 10, 0, 10);
        mFeedTextTv.setBackgroundDrawable(ResFinder.getDrawable("umeng_comm_forward_bg"));
        mFeedTextTv.setText(text);
    }

    private String getToastText(int status) {
        String text = ResFinder.getString("umeng_comm_feed_spam_deleted");
        // switch (status) {
        // case FeedItem.STATUS_SPAM:
        // text = ResFinder.getString("umeng_comm_feed_spam_shield");
        // break;
        // case FeedItem.STATUS_DELETE:
        // text = ResFinder.getString("umeng_comm_feed_spam_deleted");
        // break;
        // case FeedItem.STATUS_SNOW:
        // text = ResFinder.getString("umeng_comm_feed_spam_deleted_admin");
        // break;
        // case FeedItem.STATUS_SENSITIVE:
        // text = ResFinder.getString("umeng_comm_feed_spam_sensitive");
        // break;
        // default:
        // text = ResFinder.getString("umeng_comm_feed_spam_deleted");
        // break;
        // }
        return text;
    }

    /**
     * 收藏Feed</br>
     */
    private void favoritesFeed() {
        mCommunitySDK.favoriteFeed(mFeedItem.id, new SimpleFetchListener<SimpleResponse>() {

            @Override
            public void onComplete(SimpleResponse response) {
                if (response.errCode != ErrorCode.NO_ERROR) {
                    if (response.errCode == ErrorCode.ERR_CODE_FEED_FAVOURITED) {
                        mFavoritestButton.setBackgroundResource(ResFinder.getResourceId(
                                ResType.DRAWABLE, "umeng_comm_favorites_pressed"));
                        ToastMsg.showShortMsgByResName("umeng_comm_has_favorited");
                        mFeedItem.category = CATEGORY.FAVORITES;
                        BroadcastUtils.sendFeedUpdateBroadcast(mContext, mFeedItem);
                    } else if (response.errCode == ErrorCode.ERR_CODE_FAVOURITED_OVER_FLOW) {
                        ToastMsg.showShortMsgByResName("umeng_comm_favorites_overflow");
                    } else if (response.errCode == ErrorCode.USER_FORBIDDEN_ERR_CODE) {
                        ToastMsg.showShortMsgByResName("umeng_comm_user_unusable");
                    } else {
                        ToastMsg.showShortMsgByResName("umeng_comm_favorites_failed");
                    }
                } else {
                    mFavoritestButton.setBackgroundResource(ResFinder.getResourceId(
                            ResType.DRAWABLE,
                            "umeng_comm_favorites_pressed"));
                    ToastMsg.showShortMsgByResName("umeng_comm_favorites_success");
                    mFeedItem.category = CATEGORY.FAVORITES;
                    mFeedItem.addTime = String.valueOf(System.currentTimeMillis());
                    DatabaseAPI.getInstance().getFeedDBAPI().saveFeedToDB(mFeedItem);
                    // 数据同步
                    BroadcastUtils.sendFeedUpdateBroadcast(mContext, mFeedItem);
                    BroadcastUtils.sendFeedFavouritesBroadcast(mContext, mFeedItem);
                }
            }
        });
    }

    @Override
    protected void initEventHandle() {
        super.initEventHandle();
        mFavoritestButton.setVisibility(View.VISIBLE);
        // 收藏按钮
        mFavoritestButton.setOnClickListener(new LoginOnViewClickListener() {

            @Override
            protected void doAfterLogin(View v) {
                if (mFeedItem.category == CATEGORY.FAVORITES) {
                    cancelFavoritesFeed();
                } else {
                    favoritesFeed();
                }
            }
        });
    }

    /**
     * 是否显示收藏按钮。目前该方法仅仅在Feed详情页面调用</br> [修改该方法请慎重]
     * 
     * @param isShow
     */
    public void setShowFavouriteView(boolean isShow) {
        isShowFavouriteView = isShow;
        isFromFeedDetailePage = true;
    }

    /**
     * 取消收藏feed</br>
     */
    private void cancelFavoritesFeed() {
        mCommunitySDK.cancelFavoriteFeed(mFeedItem.id,
                new SimpleFetchListener<SimpleResponse>() {

                    @Override
                    public void onComplete(SimpleResponse response) {
                        if (response.errCode != ErrorCode.NO_ERROR) {
                            if (response.errCode == ErrorCode.ERR_CODE_FEED_NOT_FAVOURITED) {
                                mFavoritestButton.setBackgroundResource(ResFinder
                                        .getResourceId(
                                                ResType.DRAWABLE, "umeng_comm_favorites_normal"));
                                ToastMsg.showShortMsgByResName(
                                        "umeng_comm_not_favorited");
                                mFeedItem.category = CATEGORY.NORMAL;
                                BroadcastUtils.sendFeedUpdateBroadcast(mContext, mFeedItem);
                            } else if (response.errCode == ErrorCode.USER_FORBIDDEN_ERR_CODE) {
                                ToastMsg.showShortMsgByResName("umeng_comm_user_unusable");
                            } else {
                                ToastMsg.showShortMsgByResName(
                                        "umeng_comm_cancel_favorites_failed");
                            }
                        } else {
                            mFavoritestButton.setBackgroundResource(ResFinder.getResourceId(
                                    ResType.DRAWABLE,
                                    "umeng_comm_favorites_normal"));
                            ToastMsg.showShortMsgByResName(
                                    "umeng_comm_cancel_favorites_success");
                            mFeedItem.category = CATEGORY.NORMAL;
                            DatabaseAPI.getInstance().getFeedDBAPI().saveFeedToDB(mFeedItem);
                            // 数据同步
                            BroadcastUtils.sendFeedUpdateBroadcast(mContext, mFeedItem);
                        }
                    }
                });
    }

}
