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
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.beans.ImageItem;
import com.umeng.comm.core.beans.ShareContent;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.db.ctrl.impl.DatabaseAPI;
import com.umeng.comm.core.imageloader.ImgDisplayOption;
import com.umeng.comm.core.listeners.Listeners;
import com.umeng.comm.core.listeners.Listeners.LoginOnViewClickListener;
import com.umeng.comm.core.listeners.Listeners.OnItemViewClickListener;
import com.umeng.comm.core.listeners.Listeners.OnResultListener;
import com.umeng.comm.core.sdkmanager.ShareSDKManager;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ResFinder.ResType;
import com.umeng.comm.core.utils.TimeUtils;
import com.umeng.comm.ui.activities.LocationFeedActivity;
import com.umeng.comm.ui.adapters.FeedImageAdapter;
import com.umeng.comm.ui.emoji.EmojiTextView;
import com.umeng.comm.ui.mvpview.MvpLikeView;
import com.umeng.comm.ui.presenter.impl.FeedContentPresenter;
import com.umeng.comm.ui.presenter.impl.LikePresenter;
import com.umeng.comm.ui.utils.FeedViewRender;
import com.umeng.comm.ui.utils.ViewFinder;
import com.umeng.comm.ui.widgets.NetworkImageView;
import com.umeng.comm.ui.widgets.RoundImageView;
import com.umeng.comm.ui.widgets.WrapperGridView;

/**
 * ListView的Feed Item View解析器. ( 将试图的显示和解析解耦, 便于测试, 也便于复用. )
 */
public class FeedItemViewHolder extends ViewHolder implements MvpLikeView {
    private static final String M = "m";

    public ImageView mFeedTypeImgView;
    public RoundImageView mProfileImgView;
    public TextView mShareBtn;
    public TextView mUserNameTv;
    public EmojiTextView mFeedTextTv;
    public ImageView mLocationImgView;
    public TextView mLocationTv;
    public RelativeLayout mForwardLayout;
    public TextView mForwardTextTv;
    public ViewStub mImageGvViewStub;
    public WrapperGridView mImageGv;
    public LinearLayout mButtomLayout;
    public TextView mTimeTv;
    public TextView mLikeCountTextView;
    public TextView mForwardCountTextView;
    public TextView mCommentCountTextView;
    public TextView mDistanceTextView;

    protected FeedItem mFeedItem;

    FeedContentPresenter mPresenter;
    LikePresenter mLikePresenter;

    private String mContainerClzName;

    private boolean mIsShowDistance = false;

    OnItemViewClickListener<FeedItem> mItemViewClickListener;

    public FeedItemViewHolder() {
    }

    public FeedItemViewHolder(Context context, View rootView) {
        mContext = context;
        itemView = rootView;
        mViewFinder = new ViewFinder(rootView);
        initWidgets();
    }

    @Override
    protected int getItemLayout() {
        return ResFinder.getLayout("umeng_comm_feed_lv_item");
    }

    @Override
    protected void initWidgets() {
        inflateFromXML();
        initEventHandle();
        initPresenters();
    }

    protected void initPresenters() {
        mPresenter = new FeedContentPresenter();
        mPresenter.attach(mContext);
        mLikePresenter = new LikePresenter(this);
        mLikePresenter.attach(mContext);
        mLikePresenter.setFeedItem(mFeedItem);
    }

    public void setContainerClass(String clz) {
        mContainerClzName = clz;
    }

    private void setupImageGridView() {
        if (mFeedItem.getImages() != null && mFeedItem.getImages().size() > 0) {
            showImageGridView();
        } else {
            hideImageGridView();
        }
    }

    public void showImageGridView() {
        // 显示转发的布局
        mForwardLayout.setVisibility(View.VISIBLE);
        if (mImageGvViewStub.getVisibility() == View.GONE) {
            mImageGvViewStub.setVisibility(View.VISIBLE);
            int imageGvResId = ResFinder.getId("umeng_comm_msg_gridview");
            mImageGv = (WrapperGridView) findViewById(imageGvResId);
            mImageGv.hasScrollBar = true;
        }

        mImageGv.setBackgroundColor(Color.TRANSPARENT);
        mImageGv.setVisibility(View.VISIBLE);
        // adapter
        FeedImageAdapter gridviewAdapter = new FeedImageAdapter(mContext);
        gridviewAdapter.addDatasOnly(mFeedItem.getImages());
        // 设置图片
        mImageGv.setAdapter(gridviewAdapter);
        // 计算列数
        mImageGv.updateColumns(3);

        // 图片GridView
        mImageGv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                mPresenter.jumpToImageBrowser(mFeedItem.getImages(), pos);
            }
        });
    }

    private void hideImageGridView() {
        if (mImageGv != null) {
            mImageGv.setAdapter(new FeedImageAdapter(mContext));
            mImageGv.setVisibility(View.GONE);
        }
    }

    protected void inflateFromXML() {
        int feedTypeResId = ResFinder.getId("feed_type_img_btn");
        int userIconResId = ResFinder.getId("user_portrait_img_btn");
        int userNameResId = ResFinder.getId("umeng_comm_msg_user_name");
        int textResId = ResFinder.getId("umeng_comm_msg_text");
        int timeResId = ResFinder.getId("umeng_comm_msg_time_tv");
        int locResId = ResFinder.getId("umeng_comm_msg_location");
        int locTextResId = ResFinder.getId("umeng_comm_msg_location_text");
        int gvStubResId = ResFinder.getId("umeng_comm_msg_images_gv_viewstub");
        int forwardgvResId = ResFinder.getId("forward_image_gv_layout");
        int forwardTextResId = ResFinder.getId("umeng_comm_forard_text_tv");
        int distanceTextResId = ResFinder.getId("umeng_comm_distance");

        // 公告或者好友feed的图标
        mFeedTypeImgView = findViewById(feedTypeResId);
        // 用户头像
        mProfileImgView = findViewById(userIconResId);

        // 发布该消息的昵称
        mUserNameTv = findViewById(userNameResId);
        // 文本内容
        mFeedTextTv = findViewById(textResId);

        // feed底部的time、赞、转发、评论的父视图
        mButtomLayout = findViewById(ResFinder.getId("umeng_comm_feed_action_layout"));
        // 更新时间
        mTimeTv = findViewById(timeResId);

        // 位置图标
        mLocationImgView = findViewById(locResId);
        // 地理位置
        mLocationTv = findViewById(locTextResId);
        /**
         * 九宫格图片的View Stub
         */
        mImageGvViewStub = findViewById(gvStubResId);
        // 操作栏, 点击时出现一个包含转发、评论、赞功能的popupwindow
        // mOpetationBtn = findViewById(commentBtnResId);

        // 转发时候的text和图片gv
        mForwardLayout = findViewById(forwardgvResId);
        // 转发文本内容
        mForwardTextTv = findViewById(forwardTextResId);
        // 弹出举报、删除feed对话框的按钮
        mShareBtn = findViewById(ResFinder.getId("umeng_comm_dialog_btn"));

        mLikeCountTextView = findViewById(ResFinder.getId("umeng_comm_like_tv"));
        mForwardCountTextView = findViewById(ResFinder.getId("umeng_comm_forward_tv"));
        mCommentCountTextView = findViewById(ResFinder.getId("umeng_comm_comment_tv"));
        mDistanceTextView = findViewById(distanceTextResId);
    }

    protected void initEventHandle() {
        mLikeCountTextView.setOnClickListener(new LoginOnViewClickListener() {
            @Override
            protected void doAfterLogin(View v) {
                clickAnima(mLikeCountTextView);
                mLikePresenter.setFeedItem(mFeedItem);
                if (mFeedItem.isLiked) {
                    mLikePresenter.postUnlike(mFeedItem.id);
                } else {
                    mLikePresenter.postLike(mFeedItem.id);
                }
            }
        });

        // 转发按钮
        mForwardCountTextView.setOnClickListener(new LoginOnViewClickListener() {

            @Override
            protected void doAfterLogin(View v) {
                clickAnima(mForwardCountTextView);
                mPresenter.gotoForwardActivity(mFeedItem);
            }
        });

        mLocationImgView.setOnClickListener(mLocationClickListener);
        mLocationTv.setOnClickListener(mLocationClickListener);

    }

    /**
     * 点击地理位置的回调，跳转至LocationFeedActivity页面
     */
    private OnClickListener mLocationClickListener = new Listeners.LoginOnViewClickListener() {

        @Override
        protected void doAfterLogin(View v) {
            Intent intent = new Intent(mContext, LocationFeedActivity.class);
            intent.putExtra(Constants.FEED, mFeedItem);
            mContext.startActivity(intent);
        }
    };

    private void clickAnima(View targetView) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.1f, 1.0f, 1.1f);
        scaleAnimation.setDuration(100);
        targetView.startAnimation(scaleAnimation);
    }

    public void setShareActivity(final Activity activity) {
        mShareBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                shareToSns(activity);
            }
        });
    }

    private void shareToSns(Activity activity) {
        ShareContent shareItem = new ShareContent();
        shareItem.mText = mFeedItem.text;
        List<ImageItem> imageItems = mFeedItem.imageUrls;
        if (mFeedItem.sourceFeed != null) {
            imageItems = mFeedItem.sourceFeed.imageUrls;
        }
        if (imageItems.size() > 0) {
            shareItem.mImageItem = imageItems.get(0);
        }
        shareItem.mTargetUrl = mFeedItem.shareLink;
        if (TextUtils.isEmpty(shareItem.mTargetUrl) && mFeedItem.sourceFeed != null) {
            shareItem.mTargetUrl = mFeedItem.sourceFeed.shareLink;
        }
        shareItem.mFeedId = mFeedItem.id;
        shareItem.mTitle = mFeedItem.text;
        ShareSDKManager.getInstance().getCurrentSDK().share(activity, shareItem);
    }

    /**
     * 填充消息流ListView每项的数据
     * 
     * @param viewHolder
     * @param item
     */
    protected void bindFeedItemData() {

        if (TextUtils.isEmpty(mFeedItem.id)) {
            return;
        }
        // 设置基础信息
        setBaseFeeditemInfo();
        // 设置图片
        setupImageGridView();
        // 设置feed图片
        // 转发的feed
        if (mFeedItem.sourceFeed != null) {
            // 转发视图
            setForwardViewVisibility(mFeedItem);
            // 设置转发视图的数据
            setForwardItemData(mFeedItem);
        } else {
            // 设置普通类型feed的item view的可见性
            setCommFeedViewVisibility(mFeedItem);
        }

        mLikeCountTextView.setText("" + mFeedItem.likeCount);
        mCommentCountTextView.setText("" + mFeedItem.commentCount);
        mForwardCountTextView.setText("" + mFeedItem.forwardCount);
        like(mFeedItem.isLiked);
    }

    /**
     * 设置普通feed视图的可见性
     * 
     * @param viewHolder
     */
    private void setCommFeedViewVisibility(FeedItem item) {
        // 修改转发视图的背景为透明
        mForwardLayout.setBackgroundColor(Color.TRANSPARENT);
        // mForwardLayout.setVisibility(View.GONE);
        mForwardLayout.setPadding(0, 0, 0, 0);
        if (mImageGv != null) {
            mImageGv.setPadding(0, 0, 0, 0);
        }
        // 隐藏转发视图
        mForwardTextTv.setVisibility(View.GONE);

        // 显示时间视图
        mTimeTv.setVisibility(View.VISIBLE);
        // 昵称
        mUserNameTv.setVisibility(View.VISIBLE);
        // 加载头像视图设置为可见
        mProfileImgView.setVisibility(View.VISIBLE);
    }

    /**
     * 设置转发的数据
     * 
     * @param viewHolder 视图Holder
     * @param item 转发的feed item
     */
    private void setForwardItemData(FeedItem item) {
        // @原始feed的创建者
        atOriginFeedCreator(item.sourceFeed);
        // 大于等于2表示该feed已经被删除
        if (item.sourceFeed.status >= FeedItem.STATUS_SPAM || isDeleted(item.sourceFeed)) {
            mForwardTextTv.setGravity(Gravity.CENTER);
            mForwardTextTv.setText(ResFinder.getString("umeng_comm_feed_deleted"));
            if (mImageGv != null) {
                mImageGv.setVisibility(View.GONE);
            }
            // 如果该feed是收藏，且转发feed、原feed都被删除，则不显示原feed的状态
            if (item.status >= FeedItem.STATUS_SPAM
                    && item.sourceFeed.status >= FeedItem.STATUS_SPAM) {
                mForwardLayout.setVisibility(View.GONE);
            } else {
                // 删除被转发的feed
                deleteInvalidateFeed(item.sourceFeed);
            }
        } else {
            mForwardTextTv.setGravity(Gravity.LEFT | Gravity.CENTER);
            // 解析被转发的@和话题
            FeedViewRender.parseTopicsAndFriends(mForwardTextTv, item.sourceFeed);
            if (mImageGv != null) {
                mImageGv.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * @param feedItem
     */
    protected void deleteInvalidateFeed(FeedItem feedItem) {
        DatabaseAPI.getInstance().getFeedDBAPI().deleteFeedFromDB(feedItem.id);
    }

    /**
     * 被转发的原始feed的创建者在转发时会被@,因此将其名字设置到文本中,然后将其添加到@的好友中.
     * 
     * @param feedItem
     */
    protected void atOriginFeedCreator(FeedItem feedItem) {
        String contextText = feedItem.text;
        // @前缀
        final String atPrefix = "@" + feedItem.creator.name + ": ";
        if (!contextText.contains(atPrefix)) {
            feedItem.text = atPrefix + contextText;
            feedItem.atFriends.add(feedItem.creator);
        }
    }

    /**
     * 判断该feed是否被删除，本地[目前暂时按照从方法判断]</br>
     * 
     * @param item
     * @return
     */
    private boolean isDeleted(FeedItem item) {
        if (TextUtils.isEmpty(item.publishTime)) {
            return true;
        }
        return false;
    }

    /**
     * 设置转发feed的视图的可见性
     */
    @SuppressWarnings("deprecation")
    private void setForwardViewVisibility(FeedItem item) {
        // 显示转发视图
        mForwardLayout.setVisibility(View.VISIBLE);
        mForwardLayout.setPadding(10, 10, 10, 10);
        mForwardLayout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mPresenter.clickOriginFeedItem(mFeedItem);
            }
        });
        if (mImageGv != null) {
            mImageGv.setPadding(10, 2, 10, 10);
        }

        // 转发视图的背景
        mForwardLayout.setBackgroundDrawable(ResFinder
                .getDrawable("umeng_comm_forward_bg"));
        // 被转发的文本
        mForwardTextTv.setVisibility(View.VISIBLE);
        mForwardTextTv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mPresenter.clickOriginFeedItem(mFeedItem);
            }
        });

        // 隐藏位置图标
        mLocationImgView.setVisibility(View.GONE);
        mLocationTv.setVisibility(View.GONE);
    }

    /**
     * 设置feedItem的基本信息（头像，昵称，内容、位置）</br>
     * 
     * @param viewHolder
     * @param feedItem
     */
    protected void setBaseFeeditemInfo() {
        // 设置feed类型图标
        setTypeIcon();
        // 用户头像
        setupUserIcon(mProfileImgView, mFeedItem.creator);
        // 昵称
        mUserNameTv.setText(mFeedItem.creator.name);
        // 更新时间
        Date date = new Date(Long.parseLong(mFeedItem.publishTime));
        mTimeTv.setText(TimeUtils.format(date));
        // feed的文本内容
        FeedViewRender.parseTopicsAndFriends(mFeedTextTv, mFeedItem);

        // 地理位置信息
        if (TextUtils.isEmpty(mFeedItem.locationAddr)) {
            mLocationTv.setVisibility(View.GONE);
            mLocationImgView.setVisibility(View.GONE);
        } else {
            mLocationTv.setVisibility(View.VISIBLE);
            mLocationImgView.setVisibility(View.VISIBLE);
            mLocationTv.setText(mFeedItem.locationAddr);
        }

        // 内容为空时Text隐藏布局,这种情况出现在转发时没有文本的情况
        if (TextUtils.isEmpty(mFeedItem.text)) {
            mFeedTextTv.setVisibility(View.GONE);
        } else {
            mFeedTextTv.setVisibility(View.VISIBLE);
        }
        mFeedTextTv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mPresenter.clickFeedItem();
            }
        });
        if (mIsShowDistance) {
            mDistanceTextView.setText(mFeedItem.distance + M);
        }
    }

    /**
     * 设置feed 类型的icon</br>
     * 
     * @param viewHolder
     * @param feedItem
     */
    private void setTypeIcon() {
        Drawable drawable = null;
        if (mFeedItem.type == FeedItem.ANNOUNCEMENT_FEED
                && mFeedItem.isTop == FeedItem.TOP_FEED) {
            // 设置feed类型图标
            drawable = ResFinder.getDrawable("umeng_comm_top_announce");
        } else if (mFeedItem.type == FeedItem.ANNOUNCEMENT_FEED) {
            // 设置feed类型图标
            drawable = ResFinder.getDrawable("umeng_comm_announce");
        } else if (mFeedItem.isTop == FeedItem.TOP_FEED) {
            // feed 置顶图标
            drawable = ResFinder.getDrawable("umeng_comm_top");
            mFeedTypeImgView.setVisibility(View.VISIBLE);
            mFeedTypeImgView.setImageDrawable(drawable);
        } else {
            // 设置feed类型图标 [ 目前只标识公告类型 ]
            mFeedTypeImgView.setVisibility(View.INVISIBLE);
            return;
        }

        mFeedTypeImgView.setVisibility(View.VISIBLE);
        mFeedTypeImgView.setImageDrawable(drawable);

    }

    /**
     * 设置用户头像
     * 
     * @param userIconImageView 用户头像的SquareImageView
     * @param iconUrl 用户头像的url
     */
    private void setupUserIcon(final NetworkImageView userIconImageView,
            final CommUser user) {
        if (user == null || userIconImageView == null) {
            return;
        }

        ImgDisplayOption option = ImgDisplayOption.getOptionByGender(user.gender);
        userIconImageView.setImageUrl(user.iconUrl, option);
        userIconImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 跳转用户中心前检查是否登录
                mPresenter.gotoUserInfoActivity(user, mContainerClzName);
            }
        });
    }

    public void setFeedItem(FeedItem feedItem) {
        mFeedItem = feedItem;
        mPresenter.setFeedItem(mFeedItem);
        bindFeedItemData();
    }

    public FeedItem getFeedItem() {
        return mFeedItem;
    }

    /**
     * 在feed详情页面隐藏赞、评论、转发三个按钮
     */
    public void hideActionButtons() {
        mLikeCountTextView.setVisibility(View.GONE);
        mCommentCountTextView.setVisibility(View.GONE);
        mForwardCountTextView.setVisibility(View.GONE);
    }

    public void setOnItemViewClickListener(final int position,final OnItemViewClickListener<FeedItem> listener) {
        mItemViewClickListener = listener;
        mCommentCountTextView.setOnClickListener(new LoginOnViewClickListener() {

            @Override
            protected void doAfterLogin(View v) {
                clickAnima(mCommentCountTextView);
                if (mItemViewClickListener != null) {
                    mItemViewClickListener.onItemClick(position, mFeedItem);
                }
            }
        });
    }

    public void setOnUpdateListener(OnResultListener listener) {
        mLikePresenter.setResultListener(listener);
    }

    @Override
    public void like(boolean isLiked) {
        mFeedItem.isLiked = isLiked;
        if (mFeedItem.isLiked) {
            mLikeCountTextView.setCompoundDrawablesWithIntrinsicBounds(
                    ResFinder.getResourceId(ResType.DRAWABLE, "umeng_comm_like_pressed"), 0, 0,
                    0);
        } else {
            mLikeCountTextView.setCompoundDrawablesWithIntrinsicBounds(
                    ResFinder.getResourceId(ResType.DRAWABLE, "umeng_comm_like_normal"), 0, 0,
                    0);
        }
    }

    @Override
    public void updateLikeView(String nextUrl) {
    }

    public void setShowDistance() {
        mIsShowDistance = true;
        mDistanceTextView.setVisibility(View.VISIBLE);
    }

}
