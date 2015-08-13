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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.constants.ErrorCode;
import com.umeng.comm.core.listeners.Listeners.OnResultListener;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.LoginResponse;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ResFinder.ResType;
import com.umeng.comm.ui.anim.CustomAnimator;
import com.umeng.comm.ui.fragments.ActiveUserFragment;
import com.umeng.comm.ui.fragments.TopicFeedFragment;
import com.umeng.comm.ui.mvpview.MvpTopicDetailView;
import com.umeng.comm.ui.presenter.impl.TopicDetailPresenter;
import com.umeng.comm.ui.widgets.ViewPagerIndicator;

/**
 * 话题详情页
 */
public class TopicDetailActivity extends BaseFragmentActivity implements OnClickListener,
        MvpTopicDetailView {

    /**
     * 话题详情的Fragment
     */
    private TopicFeedFragment mDetailFragment;
    private ActiveUserFragment mActiveUserFragment;
    private Topic mTopic;
    private ViewPagerIndicator mIndicator;
    private ViewPager mViewPager;
    private String[] mTitles = null;
    private FragmentPagerAdapter mAdapter;
    private ToggleButton mFollowToggleBtn;
    private TopicDetailPresenter mPresenter;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        mPresenter = new TopicDetailPresenter(this, this);
        setContentView(ResFinder.getLayout("umeng_comm_topic_detail_layout"));
        mTopic = getIntent().getExtras().getParcelable(Constants.TAG_TOPIC);
        if (mTopic == null) {
            finish();
            return;
        }
        mTitles = getResources().getStringArray(
                ResFinder.getResourceId(ResType.ARRAY, "umeng_comm_topic_detail_tabs"));
        // 根据话题的id信息初始化fragment
        initView();
        mPresenter.onCreate(arg0);
    }

    private void initView() {
        mIndicator = (ViewPagerIndicator) findViewById(ResFinder.getId("indicator"));
        mViewPager = (ViewPager) findViewById(ResFinder.getId("viewPager"));
        mIndicator.setTabItemTitles(mTitles);
        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {

            @Override
            public int getCount() {
                return mTitles.length;
            }

            @Override
            public Fragment getItem(int pos) {
                return getFragment(pos);
            }
        };
        mViewPager.setAdapter(mAdapter);
        // 设置关联的ViewPager
        mIndicator.setViewPager(mViewPager, 0);
        // 初始化Header的控件跟数据
        initHeader();
        initTitle();
    }

    /**
     * 初始化标题栏相关控件跟设置数据</br>
     */
    private void initTitle() {
        findViewById(ResFinder.getId("umeng_comm_title_back_btn")).setOnClickListener(this);
        TextView titleTextView = (TextView) findViewById(ResFinder.getId("umeng_comm_title_tv"));
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        titleTextView.setText(mTopic.name);
        findViewById(ResFinder.getId("umeng_comm_title_setting_btn")).setVisibility(View.GONE);
    }

    /**
     * 获取对应的Fragment。0：话题聚合 1：活跃用户</br>
     * 
     * @param pos
     * @return
     */
    private Fragment getFragment(int pos) {
        if (pos == 0) {
            if (mDetailFragment == null) {
                mDetailFragment = TopicFeedFragment.newTopicFeedFrmg(mTopic);
            }
            mDetailFragment.setOnAnimationListener(mListener);
            return mDetailFragment;
        } else if (pos == 1) {
            if (mActiveUserFragment == null) {
                mActiveUserFragment = ActiveUserFragment.newActiveUserFragment(mTopic);
                mActiveUserFragment.setOnAnimationListener(mListener);
            }
            return mActiveUserFragment;
        }
        return null;
    }

    private void initHeader() {
        // 话题描述
        TextView topicDescTv = (TextView) findViewById(ResFinder.getId(
                "umeng_comm_topic_desc_tv"));
        String desc = mTopic.desc;
        String noDescStr = ResFinder.getString("umeng_comm_topic_no_desc");
        boolean hasText = TextUtils.isEmpty(desc) || "null".equals(desc);
        String showText = hasText ? noDescStr : desc;
        topicDescTv.setText(showText);

        mFollowToggleBtn = (ToggleButton)
                findViewById(ResFinder.getId("umeng_comm_topic_toggle_btn"));
        mPresenter.checkIsFollowed(mTopic.id, new OnResultListener() {

            @Override
            public void onResult(int status) {
                mFollowToggleBtn.setChecked(status == 1);
            }
        });

        setTopicStatus();
        mFollowToggleBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                CommonUtils.checkLoginAndFireCallback(TopicDetailActivity.this,
                        new SimpleFetchListener<LoginResponse>() {

                            @Override
                            public void onComplete(LoginResponse response) {
                                mFollowToggleBtn.setChecked(!mFollowToggleBtn.isChecked());
                                if (response.errCode != ErrorCode.NO_ERROR) {
                                    mFollowToggleBtn.setChecked(!mFollowToggleBtn.isChecked());
                                    return;
                                }
                                if (mFollowToggleBtn.isChecked()) {
                                    mPresenter.cancelFollowTopic(mTopic);
                                } else {
                                    mPresenter.followTopic(mTopic);
                                }
                            }
                        });
            }
        });

        mHeaderView = (LinearLayout) findViewById(ResFinder.getId("umeng_comm_topic_header"));
    }

    private LinearLayout mHeaderView = null;
    private CustomAnimator mCustomAnimator = new CustomAnimator();
    private OnResultListener mListener = new OnResultListener() {

        @Override
        public void onResult(int status) {
            if (status == 1) {// dismiss
                mCustomAnimator.startDismissAnimation(mHeaderView);
            } else if (status == 0) { // show
                mCustomAnimator.startShowAnimation(mHeaderView);
            }
        }
    };
    
    /**
     * 检查当前登录用户是否已关注该话题，并设置ToggleButton的状态</br>
     */
    private void setTopicStatus() {
        String loginUserId = CommConfig.getConfig().loginedUser.id;
        if (TextUtils.isEmpty(loginUserId)) {
            Log.d("###", "### user dont login...");
            return;
        }
        mFollowToggleBtn.setChecked(mTopic.isFocused);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == ResFinder.getId("umeng_comm_title_back_btn")) {
            finish();
        }
    }

    @Override
    public void setToggleButtonStatus(boolean status) {
        mFollowToggleBtn.setClickable(true);
        mFollowToggleBtn.setChecked(status);
    }

}
