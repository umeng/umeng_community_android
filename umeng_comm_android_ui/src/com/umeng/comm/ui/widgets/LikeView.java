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

package com.umeng.comm.ui.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.beans.Like;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.constants.HttpProtocol;
import com.umeng.comm.core.imageloader.ImgDisplayOption;
import com.umeng.comm.core.listeners.Listeners.LoginOnViewClickListener;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.DeviceUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.activities.LikeUsersActivity;
import com.umeng.comm.ui.activities.UserInfoActivity;
import java.util.ArrayList;
import java.util.List;

/**
 * 显示Like的用户头像View，包括更多按钮
 */
public class LikeView extends LinearLayout {

    private List<Like> mLikes = new ArrayList<Like>();
    private int mItemPadding;
    private int mItemWidth;
    private boolean isLayoutFinish = false;
    private FeedItem mFeedItem;
    private String mNexturl;

    /**
     * @param context
     */
    public LikeView(Context context) {
        super(context);
        init();
    }

    /**
     * @param context
     * @param attrs
     */
    public LikeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public LikeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * 初始化item的宽跟padding值</br>
     */
    private void init() {
        mItemPadding = DeviceUtils.dp2px(getContext(), 6);
        mItemWidth = DeviceUtils.dp2px(getContext(), 25);
    }

    public void addLikeUsers(FeedItem item, String nextUrl) {
        this.mFeedItem = item;
        this.mNexturl = nextUrl;
        isLayoutFinish = false;
        mLikes.clear();
        removeAllViews();
        if (CommonUtils.isListEmpty(item.likes)) {
            invalidate();
            return;
        }

        mLikes.addAll(item.likes);
        View view = createView(item.likes.get(0).creator);
        addView(view);
        invalidate();
    }

    /**
     * 计算每个item的宽度，并计算能够容纳多少item</br>
     */
    private void calculateAndAddView() {
        if (mLikes.size() <= 0 || isLayoutFinish) {
            return;
        }

        View view = getChildAt(0);
        int itemWidth = view.getMeasuredWidth();
        int parentWidth = getMeasuredWidth();
        int count = parentWidth / (itemWidth + mItemPadding);
        boolean isShowMore = false;
        if (mLikes.size() > count) { // 如果此时超过显示的个数，则显示加载更多按钮
            count = count - 1;// 留一个位置给“更多”view
            isShowMore = true;
        }
        count = Math.min(count, mLikes.size());
        for (int i = 1; i < count; i++) {
            view = createView(mLikes.get(i).creator);
            addView(view);
        }
        if (isShowMore) {
            view = createMoreView();
            addView(view);
        }
        isLayoutFinish = true;
    }

    /**
     * 创建加载更多的按钮View</br>
     * 
     * @return
     */
    private View createMoreView() {
        RoundImageView userImageView = new RoundImageView(getContext());
        userImageView.setScaleType(ScaleType.FIT_XY);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mItemWidth, mItemWidth);
        params.setMargins(0, 0, mItemPadding, 0);
        params.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        userImageView.setLayoutParams(params);
        userImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getContext(), LikeUsersActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Constants.TAG_COUNT, mFeedItem.likeCount);
                intent.putExtra(HttpProtocol.NAVIGATOR_KEY, mNexturl);
                intent.putExtra(HttpProtocol.FEED_ID_KEY, mFeedItem.id);
                intent.putParcelableArrayListExtra(Constants.TAG_USERS, prepareUsers());
                getContext().startActivity(intent);
            }
        });
        // int drawableRes = ResFinder.getResourceId(ResType.DRAWABLE,
        // "umeng_comm_like_more_user");
        // userImageView.setImageResource(drawableRes);
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[] {
                android.R.attr.state_pressed
        },
                ResFinder.getDrawable("umeng_comm_like_more_user_pressed"));
        states.addState(new int[] {
                android.R.attr.state_focused
        },
                ResFinder.getDrawable("umeng_comm_like_more_user_pressed"));
        states.addState(new int[] {},
                ResFinder.getDrawable("umeng_comm_like_more_user_normal"));

        userImageView.setClickable(true);
        userImageView.setEnabled(true);
        userImageView.setImageDrawable(states);
        return userImageView;
    }

    /**
     * 将like的create独立出来</br>
     * 
     * @return
     */
    private ArrayList<CommUser> prepareUsers() {
        ArrayList<CommUser> users = new ArrayList<CommUser>();
        for (Like like : mLikes) {
            users.add(like.creator);
        }
        return users;
    }

    /**
     * 创建每个子View</br>
     * 
     * @param user
     * @return
     */
    private View createView(final CommUser user) {
        RoundImageView userImageView = new RoundImageView(getContext());
        userImageView.setScaleType(ScaleType.FIT_XY);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mItemWidth, mItemWidth);
        params.setMargins(0, 0, mItemPadding, 0);
        params.gravity = Gravity.CENTER_VERTICAL;
        userImageView.setLayoutParams(params);
        userImageView.setOnClickListener(new LoginOnViewClickListener() {

            @Override
            protected void doAfterLogin(View v) {
                Intent intent = new Intent();
                intent.setClass(getContext(), UserInfoActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Constants.TAG_USER, user);
                getContext().startActivity(intent);
            }
        });
        userImageView.setImageUrl(user.iconUrl,
                ImgDisplayOption.getOptionByGender(user.gender));
        return userImageView;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        calculateAndAddView();
        requestLayout();
    }

}
