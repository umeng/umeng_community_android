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

package com.umeng.comm.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.MessageCount;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.listeners.Listeners.LoginOnViewClickListener;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ResFinder.ResType;
import com.umeng.comm.ui.activities.FindActivity;
import com.umeng.comm.ui.mvpview.MvpUnReadMsgView;
import com.umeng.comm.ui.presenter.impl.NullPresenter;
import com.umeng.comm.ui.widgets.SegmentView;
import com.umeng.comm.ui.widgets.SegmentView.OnItemCheckedListener;

/**
 * 社区首页，包含关注、推荐、话题三个tab的页面，通过ViewPager管理页面之间的切换.
 */
public class CommunityMainFragment extends BaseFragment<Void, NullPresenter> implements
        OnClickListener, MvpUnReadMsgView {

    private ViewPager mViewPager;
    private String[] mTitles;
    private Fragment mCurrentFragment;
    /**
     * Feed流页面
     */
    private AllFeedsFragment mMainFeedFragment;
    /**
     * 推荐Feed页面
     */
    private RecommendFeedFragment mRecommendFragment;
    /**
     * 话题页面
     */
    private TopicFragment mTopicFragment;

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
    /**
     * 右上角的个人信息Button
     */
    private ImageView mProfileBtn;

    private String mContainerClass;
    /**
     * tab视图
     */
    private SegmentView mSegmentView;
    /**
     * 未读消息的数量
     */
    private MessageCount mUnreadMsg = CommConfig.getConfig().mMessageCount;
    /**
     * 含有未读消息时的红点视图
     */
    private View mBadgeView;

    @Override
    protected int getFragmentLayout() {
        return ResFinder.getLayout("umeng_comm_community_frag_layout");
    }

    protected void initWidgets() {
        mContainerClass = getActivity().getClass().getName();
        initTitle(mRootView);
        initFragment();
        initViewPager(mRootView);
        registerInitSuccessBroadcast();
    }

    /**
     * 初始化title</br>
     * 
     * @param context
     */
    private void initTitle(View rootView) {
        mTitles = getResources().getStringArray(
                ResFinder.getResourceId(ResType.ARRAY, "umeng_comm_feed_titles"));
        int titleLayoutResId = ResFinder.getId("topic_action_bar");
        mTitleLayout = rootView.findViewById(titleLayoutResId);
        mTitleLayout.setVisibility(View.GONE);

        int backButtonResId = ResFinder.getId("umeng_comm_back_btn");
        rootView.findViewById(backButtonResId).setOnClickListener(this);

        if (mBackButtonVisible != View.VISIBLE) {
            rootView.findViewById(backButtonResId).setVisibility(mBackButtonVisible);
        }

        mTitleLayout.setVisibility(mTitleVisible);

        mBadgeView = findViewById(ResFinder.getId("umeng_comm_badge_view"));
        mBadgeView.setVisibility(View.INVISIBLE);
        //
        mProfileBtn = (ImageView) rootView
                .findViewById(ResFinder.getId("umeng_comm_user_info_btn"));
        mProfileBtn.setOnClickListener(new LoginOnViewClickListener() {
            @Override
            protected void doAfterLogin(View v) {
                if (mBadgeView != null) {
                    mBadgeView.setVisibility(View.INVISIBLE);
                }
                gotoFindActivity(CommConfig.getConfig().loginedUser);
            }
        });

        mSegmentView = (SegmentView) rootView.findViewById(ResFinder
                .getId("umeng_comm_segment_view"));
        // 设置tabs
        mSegmentView.setTabs(mTitles);
        mSegmentView.selectItemIndex(0);
        // 设置点击事件
        mSegmentView.setOnItemCheckedListener(new OnItemCheckedListener() {

            @Override
            public void onCheck(RadioButton button, int position, String title) {
                mViewPager.setCurrentItem(position, true);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mUnreadMsg.unReadTotal > 0) {
            mBadgeView.setVisibility(View.VISIBLE);
        } else {
            mBadgeView.setVisibility(View.INVISIBLE);
        }
    }

    public ViewPager getViewPager() {
        return mViewPager;
    }

    /**
     * 跳转到发现Activity</br>
     * 
     * @param user
     */
    public void gotoFindActivity(CommUser user) {
        Intent intent = new Intent(getActivity(), FindActivity.class);
        if (user == null) {// 来自开发者外部调用的情况
            intent.putExtra(Constants.TAG_USER, CommConfig.getConfig().loginedUser);
        } else {
            intent.putExtra(Constants.TAG_USER, user);
        }
        intent.putExtra(Constants.TYPE_CLASS, mContainerClass);
        getActivity().startActivity(intent);
    }

    /**
     * 设置回退按钮的可见性
     * 
     * @param visible
     */
    public void setBackButtonVisibility(int visible) {
        if (visible == View.VISIBLE || visible == View.INVISIBLE || visible == View.GONE) {
            this.mBackButtonVisible = visible;
        }
    }

    /**
     * 设置Title区域的可见性
     * 
     * @param visible {@see View#VISIBLE},{@see View#INVISIBLE},{@see View#GONE}
     */
    public void setNavTitleVisibility(int visible) {
        if (visible == View.VISIBLE || visible == View.INVISIBLE || visible == View.GONE) {
            mTitleVisible = visible;
        }
    }

    /**
     * 初始化ViewPager VIew</br>
     * 
     * @param rootView
     */
    private void initViewPager(View rootView) {
        mViewPager = (ViewPager) rootView.findViewById(ResFinder.getId("viewPager"));
        mViewPager.setOffscreenPageLimit(mTitles.length);
        CommFragmentPageAdapter adapter = new CommFragmentPageAdapter(getChildFragmentManager());
        mViewPager.setAdapter(adapter);
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int page) {
                mCurrentFragment = getFragment(page);
                // mTitleTextView.selectItemWithIndex(page);
                mSegmentView.selectItemIndex(page);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
    }

    class CommFragmentPageAdapter extends FragmentPagerAdapter {

        public CommFragmentPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            return getFragment(pos);
        }

        @Override
        public int getCount() {
            return mTitles.length;
        }

    }

    /**
     * 初始化Fragment</br>
     */
    private void initFragment() {
        mMainFeedFragment = new AllFeedsFragment();
        mRecommendFragment = new RecommendFeedFragment();
        mTopicFragment = TopicFragment.newTopicFragment();
        mCurrentFragment = mMainFeedFragment;// 默认是MainFeedFragment
    }

    /**
     * 获取当前页面被选中的Fragment</br>
     * 
     * @return
     */
    public Fragment getCurrentFragment() {
        return mCurrentFragment;
    }

    /**
     * </br>
     * 
     * @param pos
     * @return
     */
    private Fragment getFragment(int pos) {
        Fragment fragment = null;
        if (pos == 0) {
            fragment = mMainFeedFragment;
        } else if (pos == 1) {
            fragment = mRecommendFragment;
        } else if (pos == 2) {
            fragment = mTopicFragment;
        }
        return fragment;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == ResFinder.getId("umeng_comm_back_btn")) {
            getActivity().finish();
        }
    }

    /**
     * 隐藏MianFeedFragment的输入法，当退出fragment or activity的时候</br>
     */
    public void hideCommentLayoutAndInputMethod() {
        if (mMainFeedFragment != null) {
            mMainFeedFragment.hideCommentLayoutAndInputMethod();
        }
    }

    /**
     * clean sub fragment data</br>
     */
    public void cleanAdapterData() {
        if (mMainFeedFragment != null) {
            mMainFeedFragment.clearListView();
        }
        if (mRecommendFragment != null) {
            mRecommendFragment.cleanAdapterData();
        }
    }

    @Override
    public void onFetchUnReadMsg(MessageCount unreadMsg) {
        this.mUnreadMsg = unreadMsg;
        if (mUnreadMsg.unReadTotal > 0) {
            mBadgeView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 主动调用加载数据。 【注意】该接口仅仅在退出登录时，跳转到FeedsActivity清理数据后重新刷新数据</br>
     */
    public void repeatLoadDataFromServer() {
        if (mMainFeedFragment != null) {
            mMainFeedFragment.loadFeedFromServer();
        }
        if (mRecommendFragment != null) {
            mRecommendFragment.loadDataFromServer();
        }
    }

    /**
     * 注册登录成功时的广播</br>
     */
    private void registerInitSuccessBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_INIT_SUCCESS);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mInitConfigReceiver,
                filter);
    }

    private BroadcastReceiver mInitConfigReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            onFetchUnReadMsg(CommConfig.getConfig().mMessageCount);
        }
    };

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mInitConfigReceiver);
        super.onDestroy();
    }

}
