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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.MessageCount;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.listeners.Listeners.OnResultListener;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.fragments.FavoritesFragment;
import com.umeng.comm.ui.fragments.FriendsFragment;
import com.umeng.comm.ui.fragments.NearbyFeedFragment;
import com.umeng.comm.ui.fragments.RealTimeFeedFragment;
import com.umeng.comm.ui.fragments.RecommendTopicFragment;
import com.umeng.comm.ui.fragments.RecommendUserFragment;

/**
 * 发现的Activity
 */
public class FindActivity extends BaseFragmentActivity implements OnClickListener {

    private CommUser mUser;
    private String mContainerClass;
    private RecommendTopicFragment mRecommendTopicFragment;
    private RecommendUserFragment mRecommendUserFragment;
    private FriendsFragment mFriendsFragment;
    private NearbyFeedFragment mNearbyFeedFragment;
    private FavoritesFragment mFavoritesFragment;
    private RealTimeFeedFragment mRealTimeFeedFragment;
    private MessageCount mUnReadMsg;
    private View mMsgBadgeView;
    private View mNotifyBadgeView;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(ResFinder.getLayout("umeng_comm_find_layout"));
        findViewById(ResFinder.getId("umeng_comm_title_back_btn")).setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_comm_topic_recommend")).setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_comm_user_recommend")).setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_comm_usercenter_recommend")).setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_comm_setting_recommend")).setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_comm_friends")).setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_comm_nearby_recommend")).setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_comm_favortes")).setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_comm_notification")).setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_comm_realtime")).setOnClickListener(this);
        // 右上角的通知
        findViewById(ResFinder.getId("umeng_comm_title_notify_btn")).setOnClickListener(this);
        // 未读消息红点
        mMsgBadgeView = findViewById(ResFinder.getId("umeng_comm_notify_badge_view"));
        mMsgBadgeView.setVisibility(View.GONE);

        // 未读系统通知的红点
        mNotifyBadgeView = findViewById(ResFinder.getId("umeng_comm_badge_view"));

        TextView textView = (TextView) findViewById(ResFinder.getId("umeng_comm_title_tv"));
        textView.setText(ResFinder.getString("umeng_comm_find"));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

        parseIntentData();
        setupUnreadFeedMsgBadge();
        setupUnReadNotifyBadge();
        
        registerInitSuccessBroadcast();
    }

    private void parseIntentData() {
        mUser = getIntent().getExtras().getParcelable(Constants.TAG_USER);
        mContainerClass = getIntent().getExtras().getString(Constants.TYPE_CLASS);
        mUnReadMsg = CommConfig.getConfig().mMessageCount;
    }

    /**
     * 设置通知红点</br>
     */
    private void setupUnReadNotifyBadge() {
        if (mUnReadMsg.unReadNotice > 0) {
            mNotifyBadgeView.setVisibility(View.VISIBLE);
        } else {
            mNotifyBadgeView.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 设置消息数红点</br>
     */
    private void setupUnreadFeedMsgBadge() {
        if (mUnReadMsg.unReadTotal - mUnReadMsg.unReadNotice > 0) {
            mMsgBadgeView.setVisibility(View.VISIBLE);
        } else {
            mMsgBadgeView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == ResFinder.getId("umeng_comm_title_back_btn")) { // 返回事件
            finish();
        } else if (id == ResFinder.getId("umeng_comm_friends")) {
            showFriendsFragment();
        } else if (id == ResFinder.getId("umeng_comm_topic_recommend")) { // 话题推荐
            showRecommendTopic();
        } else if (id == ResFinder.getId("umeng_comm_user_recommend")) { // 用户推荐
            showRecommendUserFragment();
        } else if (id == ResFinder.getId("umeng_comm_usercenter_recommend")) { // 个人中心
            gotoUserInfoActivity();
        } else if (id == ResFinder.getId("umeng_comm_nearby_recommend")) {
            showNearbyFeed();
        } else if (id == ResFinder.getId("umeng_comm_favortes")) {
            // 显示收藏的fragment
            showFavoritesFeed();
        } else if (id == ResFinder.getId("umeng_comm_notification")) {
            // 跳转到通知的Activity
            gotoFeedNewMsgActivity();
        } else if (id == ResFinder.getId("umeng_comm_setting_recommend")) {// 设置页面
            Intent setting = new Intent(this, SettingActivity.class);
            setting.putExtra(Constants.TYPE_CLASS, mContainerClass);
            startActivity(setting);
        } else if (id == ResFinder.getId("umeng_comm_title_notify_btn")) { // 点击右上角的通知
            gotoNotificationActivity();
        } else if (id == ResFinder.getId("umeng_comm_realtime")) { // 实时内容
            showRealTimeFeed();
        }
    }

    private void gotoNotificationActivity() {
        Intent intent = new Intent(FindActivity.this, NotificationActivity.class);
        intent.putExtra(Constants.USER, mUser);
        startActivity(intent);
    }

    private void gotoFeedNewMsgActivity() {
        Intent intent = new Intent(FindActivity.this, NewMsgActivity.class);
        intent.putExtra(Constants.USER, mUser);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupUnReadNotifyBadge();
        setupUnreadFeedMsgBadge();
    }

    /**
     * 跳转到用户中心Activity</br>
     */
    private void gotoUserInfoActivity() {
        Intent intent = new Intent(FindActivity.this, UserInfoActivity.class);
        if (mUser == null || TextUtils.isEmpty(mUser.id)) {// 来自开发者外部调用的情况
            intent.putExtra(Constants.TAG_USER, CommConfig.getConfig().loginedUser);
        } else {
            intent.putExtra(Constants.TAG_USER, mUser);
        }
        // intent.putExtra(Constants.TYPE_CLASS, mContainerClass); //
        // 设置页面需要此参数，由于个人中心设置被移到此页面，暂时不传递该参数
        startActivity(intent);
    }

    /**
     * 显示附件推荐Feed</br>
     */
    private void showNearbyFeed() {
        if (mNearbyFeedFragment == null) {
            mNearbyFeedFragment = NearbyFeedFragment.newNearbyFeedRecommend();
            mNearbyFeedFragment.setOnResultListener(new OnResultListener() {

                @Override
                public void onResult(int status) {
                    showFindPage();
                }
            });
        }
        showCommFragment(mNearbyFeedFragment);
    }

    /**
     * 显示实时内容的Fragment</br>
     */
    private void showRealTimeFeed() {
        if (mRealTimeFeedFragment == null) {
            mRealTimeFeedFragment = RealTimeFeedFragment.newRealTimeFeedRecommend();
            mRealTimeFeedFragment.setOnResultListener(new OnResultListener() {

                @Override
                public void onResult(int status) {
                    showFindPage();
                }
            });
        }
        showCommFragment(mRealTimeFeedFragment);
    }

    /**
     * 显示收藏Feed</br>
     */
    private void showFavoritesFeed() {
        if (mFavoritesFragment == null) {
            mFavoritesFragment = FavoritesFragment.newFavoritesFragment();
            mFavoritesFragment.setOnResultListener(new OnResultListener() {

                @Override
                public void onResult(int status) {
                    showFindPage();
                }
            });
        }
        showCommFragment(mFavoritesFragment);
    }

    /**
     * 显示推荐话题的Dialog</br>
     */
    private void showRecommendTopic() {
        if (mRecommendTopicFragment == null) {
            mRecommendTopicFragment = RecommendTopicFragment.newRecommendTopicFragment();
            mRecommendTopicFragment.setSaveButtonInVisiable();
            mRecommendTopicFragment.setOnDismissListener(new OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    showFindPage();
                }
            });
        }
        showCommFragment(mRecommendTopicFragment);
    }

    /**
     * 隐藏发现页面，显示fragment</br>
     * 
     * @param fragment
     */
    private void showCommFragment(Fragment fragment) {
        findViewById(ResFinder.getId("umeng_comm_find_baset")).setVisibility(View.GONE);
        int container = ResFinder.getId("container");
        findViewById(container).setVisibility(View.VISIBLE);
        setFragmentContainerId(container);
        showFragmentInContainer(container, fragment);
    }

    /**
     * 隐藏fragment，显示发现页面</br>
     */
    private void showFindPage() {
        findViewById(ResFinder.getId("umeng_comm_find_baset")).setVisibility(
                View.VISIBLE);
        findViewById(ResFinder.getId("container")).setVisibility(View.GONE);
    }

    /**
     * 显示朋友圈Fragment</br>
     */
    private void showFriendsFragment() {
        if (mFriendsFragment == null) {
            mFriendsFragment = FriendsFragment.newFriendsFragment();
            mFriendsFragment.setOnResultListener(new OnResultListener() {

                @Override
                public void onResult(int status) {
                    showFindPage();
                }
            });
        }
        showCommFragment(mFriendsFragment);
    }

    /**
     * 显示推荐用户fragment</br>
     */
    private void showRecommendUserFragment() {
        if (mRecommendUserFragment == null) {
            mRecommendUserFragment = new RecommendUserFragment();
            mRecommendUserFragment.setSaveButtonInvisiable();
            mRecommendUserFragment.setOnResultListener(new OnResultListener() {

                @Override
                public void onResult(int status) {
                    showFindPage();
                }
            });
        }
        showCommFragment(mRecommendUserFragment);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && findViewById(ResFinder.getId("container")).getVisibility() == View.VISIBLE) {
            findViewById(ResFinder.getId("umeng_comm_find_baset")).setVisibility(View.VISIBLE);
            findViewById(ResFinder.getId("container")).setVisibility(View.GONE);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    /**
     * 注册登录成功时的广播</br>
     */
    private void registerInitSuccessBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_INIT_SUCCESS);
        LocalBroadcastManager.getInstance(this).registerReceiver(mInitConfigReceiver,
                filter);
    }

    private BroadcastReceiver mInitConfigReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            mUnReadMsg = CommConfig.getConfig().mMessageCount;
            setupUnReadNotifyBadge();
            setupUnreadFeedMsgBadge();
        }
    };

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mInitConfigReceiver);
        super.onDestroy();
    }
    
}
