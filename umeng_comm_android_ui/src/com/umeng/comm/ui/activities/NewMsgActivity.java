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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.MessageCount;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ResFinder.ResType;
import com.umeng.comm.ui.fragments.AtMeFeedFragment;
import com.umeng.comm.ui.fragments.CommentTabFragment;
import com.umeng.comm.ui.fragments.LikedMeFragment;
import com.umeng.comm.ui.widgets.SegmentView;
import com.umeng.comm.ui.widgets.SegmentView.OnItemCheckedListener;

public class NewMsgActivity extends BaseFragmentActivity implements OnClickListener {

    private String[] mTitles;

    /**
     * 回退按钮的可见性
     */
    private int mBackButtonVisible = View.VISIBLE;
    /**
     * 跳转到话题搜索按钮的可见性
     */
    private int mTitleVisible = View.VISIBLE;
    /**
     * title的根布局
     */
    private View mTitleLayout;

    SegmentView mSegmentView;

    AtMeFeedFragment mAtFragment;
    CommentTabFragment mCommentFragment;
    LikedMeFragment mLikeMeFragment;
    MessageCount mUnreadMsg = CommConfig.getConfig().mMessageCount;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(ResFinder.getLayout("umeng_comm_my_msg_layout"));
        initTitle();
        attachFragment();
    }

    /**
     * 初始化title</br>
     * 
     * @param context
     */
    private void initTitle() {
        mTitles = getResources().getStringArray(
                ResFinder.getResourceId(ResType.ARRAY, "umeng_comm_new_msg_titles"));
        int titleLayoutResId = ResFinder.getId("topic_action_bar");
        mTitles[0] = "@" + mTitles[0];
        mTitleLayout = findViewById(titleLayoutResId);
        mTitleLayout.setVisibility(View.GONE);

        int backButtonResId = ResFinder.getId("umeng_comm_back_btn");
        findViewById(backButtonResId).setOnClickListener(this);

        if (mBackButtonVisible != View.VISIBLE) {
            findViewById(backButtonResId).setVisibility(mBackButtonVisible);
        }

        // 隐藏右边的查找按钮
        findViewById(ResFinder.getId("umeng_comm_user_info_btn")).setVisibility(View.GONE);

        mTitleLayout.setVisibility(mTitleVisible);

        // 分割视图
        mSegmentView = (SegmentView) findViewById(ResFinder
                .getId("umeng_comm_segment_view"));
        // 设置tabs
        mSegmentView.setTabs(mTitles);
        mSegmentView.selectItemIndex(0);
        resetUnreadMsgCount(0);
        // 设置点击事件
        mSegmentView.setOnItemCheckedListener(new OnItemCheckedListener() {

            @Override
            public void onCheck(RadioButton button, int position, String title) {
                changeFragment(position);
                resetUnreadMsgCount(position);
            }
        });

        // 设置未读消息的小红点
        setupSegmentViewBadge();
    }

    private void setupSegmentViewBadge() {
        if (mUnreadMsg != null) {
            if (mUnreadMsg.unReadCommentsCount > 0) {
                mSegmentView.getRadioButton(1).setShowBadge(true);
            }

            if (mUnreadMsg.unReadLikesCount > 0) {
                mSegmentView.getRadioButton(2).setShowBadge(true);
            }
        }
    }

    private void resetUnreadMsgCount(int index) {
        if (index == 0) {
            mUnreadMsg.unReadAtCount = 0;
        } else if (index == 1) {
            mUnreadMsg.unReadCommentsCount = 0;
        } else if (index == 2) {
            mUnreadMsg.unReadLikesCount = 0;
        }
        mUnreadMsg.unReadTotal = mUnreadMsg.unReadAtCount + mUnreadMsg.unReadCommentsCount
                + mUnreadMsg.unReadLikesCount + mUnreadMsg.unReadNotice;
    }

    private void attachFragment() {
        mAtFragment = new AtMeFeedFragment();
        addFragment(ResFinder.getId("umeng_comm_my_msg_fragment"), mAtFragment);
    }

    private void changeFragment(int position) {
        if (position == 0) {
            showFragment(mAtFragment);
        } else if (position == 1) {
            if (mCommentFragment == null) {
                mCommentFragment = new CommentTabFragment();
            }
            showFragment(mCommentFragment);
        } else {
            if (mLikeMeFragment == null) {
                mLikeMeFragment = new LikedMeFragment();
            }
            showFragment(mLikeMeFragment);
        }
    }

    @Override
    public void onClick(View v) {
        finish();
    }
}
