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

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalFocusChangeListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.umeng.comm.core.beans.BaseBean;
import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.CommUser.Gender;
import com.umeng.comm.core.beans.CommUser.Permisson;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.imageloader.ImgDisplayOption;
import com.umeng.comm.core.listeners.Listeners;
import com.umeng.comm.core.listeners.Listeners.LoginOnViewClickListener;
import com.umeng.comm.core.listeners.Listeners.OnResultListener;
import com.umeng.comm.core.sdkmanager.ImageLoaderManager;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.DeviceUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.anim.CustomAnimator;
import com.umeng.comm.ui.anim.UserInfoAnimator;
import com.umeng.comm.ui.dialogs.UserReportDialog;
import com.umeng.comm.ui.fragments.FansFragment;
import com.umeng.comm.ui.fragments.FollowedUserFragment;
import com.umeng.comm.ui.fragments.PostedFeedsFragment;
import com.umeng.comm.ui.fragments.PostedFeedsFragment.OnDeleteListener;
import com.umeng.comm.ui.mvpview.MvpUserInfoView;
import com.umeng.comm.ui.presenter.impl.UserInfoPresenter;
import com.umeng.comm.ui.utils.BroadcastUtils;
import com.umeng.comm.ui.utils.BroadcastUtils.BROADCAST_TYPE;
import com.umeng.comm.ui.utils.BroadcastUtils.DefalutReceiver;
import com.umeng.comm.ui.utils.ViewFinder;
import com.umeng.comm.ui.widgets.CommentEditText;
import com.umeng.comm.ui.widgets.CommentEditText.EditTextBackEventListener;
import com.umeng.comm.ui.widgets.RoundImageView;

/**
 * 用户个人信息页面, 包含已发布的消息、已关注的话题、已关注的人三个fragment, 以及用户的头像、个人基本信息等.
 */
public final class UserInfoActivity extends BaseFragmentActivity implements OnClickListener,
        MvpUserInfoView {

    /**
     * 已发送Feed的Fragment
     */
    private PostedFeedsFragment mPostedFragment = null;

    /**
     * 关注的好友Fragment
     */
    private FollowedUserFragment mFolloweredUserFragment;

    /**
     * 粉丝Fragment
     */
    private FansFragment mFansFragment;

    private TextView mUserNameTv;
    private RoundImageView mHeaderImageView;
    private ImageView mGenderImageView;
    private ToggleButton mFollowToggleButton;
    /** 该用户为传递进来的user，可能是好友、陌生人等身份 */
    private CommUser mUser;

    /**
     * 已经发布的消息标签, 用于切换Fragment
     */
    private TextView mPostedTv;
    /**
     * 已经发布的消息数量标签
     */
    private TextView mPostedCountTv;
    /**
     * 已经关注的用户标签, 用于切换Fragment
     */
    private TextView mFollowedUserTv;

    /**
     * 已经关注的用户数量标签
     */
    private TextView mFollowedUserCountTv;
    /**
     * 我的粉丝标签, 用于切换Fragment
     */
    private TextView mFansTextView;
    /**
     * 我的fans用户数量标签
     */
    private TextView mFansCountTextView;

    private CommentEditText mCommentEditText;

    private View mCommentLayout;

    private int mSelectedColor = Color.BLUE;
    /**
     * 相册TextView
     */
    TextView mAlbumTextView;
    /**
     * 话题TextView
     */
    TextView mTopicTextView;

    /**
     * 视图查找器,避免每次findViewById进行强转
     */
    ViewFinder mViewFinder;
    /**
     * 用户信息的Presenter
     */
    private UserInfoPresenter mPresenter;

    private View mHeaderView;
    private View mTitleView;
    /**
     * 举报用户的Dialog
     */
    UserReportDialog mReportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ResFinder.getLayout("umeng_comm_user_info_layout"));
        mUser = getIntent().getExtras().getParcelable(Constants.TAG_USER);
        if (mUser == null) {
            return;
        }
        mPresenter = new UserInfoPresenter(this, this, mUser);

        mPostedFragment = PostedFeedsFragment.newInstance(mUser);
        mPostedFragment.setOnAnimationResultListener(mListener);
        // 视图查找器
        mViewFinder = new ViewFinder(getWindow().getDecorView());

        mPostedFragment.setCurrentUser(mUser);
        mPostedFragment.setOnDeleteListener(new OnDeleteListener() {

            @Override
            public void onDelete(BaseBean item) {
                mPresenter.decreaseFeedCount(1);
            }
        });
        // 初始化UI
        initUIComponents();
        mPresenter.onCreate(savedInstanceState);
        // 设置用户信息View的显示内容
        setupUserInfo(mUser);
        initHeaderView();
        BroadcastUtils.registerFeedBroadcast(getApplicationContext(), mReceiver);
    }

    private void initCommentView() {
        mCommentEditText = mViewFinder.findViewById(ResFinder
                .getId("umeng_comm_comment_edittext"));
        mCommentLayout = findViewById(ResFinder.getId("umeng_comm_commnet_edit_layout"));

        findViewById(ResFinder.getId("umeng_comm_comment_send_button")).setOnClickListener(this);
        mCommentEditText.setEditTextBackListener(new EditTextBackEventListener() {

            @Override
            public boolean onClickBack() {
                hideCommentLayout();
                return true;
            }
        });
    }

    /**
     * 隐藏评论布局</br>
     */
    private void hideCommentLayout() {
        mCommentLayout.setVisibility(View.GONE);
        hideInputMethod(mCommentEditText);
    }

    @SuppressWarnings("deprecation")
    private void initUIComponents() {
        // 设置Fragment
        addFragment(ResFinder.getId("umeng_comm_user_info_fragment_container"),
                mPostedFragment);

        // 选中的某个tab时的文字颜色
        mSelectedColor = ResFinder.getColor("umeng_comm_text_topic_light_color");

        // 初始化feed、好友、粉丝、back、设置的listener
        findViewById(ResFinder.getId("umeng_comm_posted_layout")).setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_comm_follow_user_layout")).setOnClickListener(
                this);
        findViewById(ResFinder.getId("umeng_comm_my_fans_layout")).setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_comm_setting_back")).setOnClickListener(this);

        // 举报用户的Dialog
        mReportDialog = new UserReportDialog(this);
        mReportDialog.setTargetUid(mUser.id);

        Button settingButton = (Button) findViewById(ResFinder.getId("umeng_comm_save_bt"));
        settingButton.setBackgroundDrawable(ResFinder.getDrawable("umeng_comm_more"));
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) settingButton
                .getLayoutParams();
        params.width = DeviceUtils.dp2px(this, 20);
        params.height = DeviceUtils.dp2px(this, 20);
        params.rightMargin = DeviceUtils.dp2px(getApplicationContext(), 10);
        settingButton.setLayoutParams(params);
        settingButton.setOnClickListener(new LoginOnViewClickListener() {

            @Override
            protected void doAfterLogin(View v) {
                mReportDialog.show();
            }
        });
        // 如果是用户自己，则不显示设置菜单按钮【目前菜单只有举报功能，即自己不能举报自己】
        if (mUser.id.equals(CommConfig.getConfig().loginedUser.id)
                || mUser.permisson == Permisson.SUPPER_ADMIN) {
            settingButton.setVisibility(View.GONE);
        }

        TextView titleTextView = mViewFinder.findViewById(ResFinder
                .getId("umeng_comm_setting_title"));
        titleTextView.setText(ResFinder.getString("umeng_comm_user_center"));
        //
        mPostedTv = mViewFinder.findViewById(ResFinder.getId("umeng_comm_posted_msg_tv"));
        mPostedTv.setTextColor(mSelectedColor);

        //
        mPostedCountTv = mViewFinder.findViewById(ResFinder
                .getId("umeng_comm_posted_count_tv"));
        mPostedCountTv.setTextColor(mSelectedColor);

        mFollowedUserTv = mViewFinder.findViewById(ResFinder.getId(
                "umeng_comm_followed_user_tv"));
        mFollowedUserCountTv = mViewFinder.findViewById(ResFinder.getId(
                "umeng_comm_follow_user_count_tv"));

        mFansTextView = mViewFinder.findViewById(ResFinder.getId("umeng_comm_my_fans_tv"));
        mFansCountTextView = mViewFinder.findViewById(ResFinder.getId(
                "umeng_comm_fans_count_tv"));
        // 昵称
        mUserNameTv = mViewFinder.findViewById(ResFinder.getId("umeng_comm_user_name_tv"));
        mUserNameTv.setText(mUser.name);

        mHeaderImageView = mViewFinder.findViewById(ResFinder.getId(
                "umeng_comm_user_header"));

        ImgDisplayOption option = ImgDisplayOption.getOptionByGender(mUser.gender);
        mHeaderImageView.setImageUrl(mUser.iconUrl, option);

        // 用户性别
        mGenderImageView = mViewFinder.findViewById(ResFinder.getId("umeng_comm_user_gender"));

        // 关注按钮
        mFollowToggleButton = mViewFinder.findViewById(ResFinder.getId(
                "umeng_comm_user_follow"));
        mFollowToggleButton.setOnClickListener(new Listeners.LoginOnViewClickListener() {

            @Override
            protected void doAfterLogin(View v) {
                mFollowToggleButton.setClickable(false);
                // true为选中状态为已关注，此时显示文本为“取消关注”；false代表未关注，此时显示文本为“关注”
                if (mFollowToggleButton.isChecked()) {
                    mPresenter.followUser(mResultListener);
                } else {
                    mPresenter.cancelFollowUser(mResultListener);
                }
            }
        });

        mAlbumTextView = mViewFinder.findViewById(ResFinder.getId("umeng_comm_user_albums_tv"));
        mAlbumTextView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                jumpToActivityWithUid(AlbumActivity.class);
            }
        });
        mTopicTextView = mViewFinder.findViewById(ResFinder.getId("umeng_comm_user_topic_tv"));
        mTopicTextView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                jumpToActivityWithUid(FollowedTopicActivity.class);
            }
        });

        // 用户自己(在未登录的情况下，点击设置跳转到登录，此时传递进来的uid是空的情况)，隐藏关注按钮，显示设置按钮
        // // 如果是超级管理员且已经关注，则隐藏取消关注按钮
        if (isHideFollowStatus()) {
            mFollowToggleButton.setVisibility(View.GONE);
        }
        initCommentView();
    }

    private boolean isHideFollowStatus() {
        if (TextUtils.isEmpty(mUser.id)) {
            return true;
        }
        CommUser loginUser = CommConfig.getConfig().loginedUser;
        if (mUser.id.equals(loginUser.id)) { // 如果是用户自己，则不显示关注/取消关注
            return true;
        }
        // 如果是超级管理员且已经被关注，则显示关注/取消关注
        if (mUser.permisson == Permisson.SUPPER_ADMIN && mUser.isFollowed) {
            return true;
        }
        return false;
    }

    private void initHeaderView() {
        mHeaderView = findViewById(ResFinder.getId("umeng_comm_portrait_layout"));
        mHeaderView.getViewTreeObserver().addOnGlobalFocusChangeListener(mChangeListener);
        mTitleView = findViewById(ResFinder.getId("umeng_comm_title_layout"));
    }

    private CustomAnimator mCustomAnimator = new UserInfoAnimator();
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

    private OnGlobalFocusChangeListener mChangeListener = new OnGlobalFocusChangeListener() {

        @Override
        public void onGlobalFocusChanged(View oldFocus, View newFocus) {
            int pos = mHeaderView.getHeight() - mHeaderView.getPaddingTop()
                    + mTitleView.getHeight() / 2;
            mCustomAnimator.setStartPosition(pos);
            mHeaderView.getViewTreeObserver().removeOnGlobalFocusChangeListener(mChangeListener);
        }
    };

    private void jumpToActivityWithUid(Class<?> activityClass) {
        Intent intent = new Intent(getApplicationContext(), activityClass);
        intent.putExtra(Constants.USER_ID_KEY, mUser.id);
        startActivity(intent);
    }

    /**
     * 避免对此点击，在回调中将状态设置为可点击状态~
     */
    private OnResultListener mResultListener = new OnResultListener() {

        @Override
        public void onResult(int status) {
            mFollowToggleButton.setClickable(true);
        }
    };

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == ResFinder.getId("umeng_comm_posted_layout")) {// 已发消息
            if (mCurrentFragment instanceof PostedFeedsFragment) { // 已经处于当前页面，判断是否需要滚动到起始位置
                mPostedFragment.executeScrollToTop();
            } else {
                showFragment(mPostedFragment);
            }
        } else if (id == ResFinder.getId("umeng_comm_follow_user_layout")) {// 关注用户
            if (mFolloweredUserFragment == null) {
                mFolloweredUserFragment = FollowedUserFragment.newInstance(mUser.id);
                mFolloweredUserFragment.setOnAnimationResultListener(mListener);
                mFolloweredUserFragment.setOnResultListener(mFollowListener);
            }
            if (mCurrentFragment instanceof FollowedUserFragment
                    && !(mCurrentFragment instanceof FansFragment)) {
                mFolloweredUserFragment.executeScrollTop();
            } else {
                showFragment(mFolloweredUserFragment);
            }
        } else if (id == ResFinder.getId("umeng_comm_my_fans_layout")) { // 我的粉丝
            if (mFansFragment == null) {
                mFansFragment = FansFragment.newFansFragment(mUser.id);
                mFansFragment.setOnAnimationResultListener(mListener);
                mFansFragment.setOnResultListener(mFansListener);
            }
            if (mCurrentFragment instanceof FansFragment) {
                mFansFragment.executeScrollTop();
            } else {
                showFragment(mFansFragment);
            }
        } else if (id == ResFinder.getId("umeng_comm_setting_back")) { // 返回
            this.finish();
        }
        changeSelectedText();
    }

    /**
     * 设置用户相关的信息 </br>
     * 
     * @param user
     */
    public void setupUserInfo(CommUser user) {
        if ( !user.id.equals(mUser.id)) {
            return ;
        }
        mUser = user;
        mUserNameTv.setText(user.name);
        if (user.gender == Gender.MALE) {
            mGenderImageView.setImageDrawable(ResFinder.getDrawable("umeng_comm_gender_male"));
        } else if (user.gender == Gender.FEMALE) {
            mGenderImageView.setImageDrawable(ResFinder.getDrawable("umeng_comm_gender_female"));
        }
        ImgDisplayOption option = ImgDisplayOption.getOptionByGender(mUser.gender);
        // 设置用户头像
        mHeaderImageView.setImageUrl(user.iconUrl, option);
        ImageLoaderManager.getInstance().getCurrentSDK().resume();
        if (isHideFollowStatus()) {
            mFollowToggleButton.setVisibility(View.GONE);
        } else {
            mFollowToggleButton.setVisibility(View.VISIBLE);
            mFollowToggleButton.setChecked(mUser.isFollowed);
        }
    }

    /**
     * 修改文本颜色 </br>
     */
    private void changeSelectedText() {
        if ((mCurrentFragment instanceof PostedFeedsFragment)) {
            mFansCountTextView.setTextColor(Color.BLACK);
            changeTextColor(mSelectedColor, Color.BLACK, Color.BLACK);
        } else if ((mCurrentFragment instanceof FansFragment)) {
            changeTextColor(Color.BLACK, Color.BLACK, mSelectedColor);
        } else if ((mCurrentFragment instanceof FollowedUserFragment)) {
            changeTextColor(Color.BLACK, mSelectedColor, Color.BLACK);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mCommentLayout.isShown()) {
            mCommentLayout.setVisibility(View.VISIBLE);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 设置文本颜色</br>
     * 
     * @param postedColor 已发送feed文本颜色
     * @param followColor 关注文本颜色
     * @param fansColor 粉丝文本颜色
     */
    private void changeTextColor(int postedColor, int followColor, int fansColor) {
        mPostedTv.setTextColor(postedColor);
        mPostedCountTv.setTextColor(postedColor);
        mFollowedUserTv.setTextColor(followColor);
        mFollowedUserCountTv.setTextColor(followColor);
        mFansTextView.setTextColor(fansColor);
        mFansCountTextView.setTextColor(fansColor);
    }

    /**
     * 关注用户数的回调函数。在加载缓存或者下拉刷新时，可能需要更新显示的用户数字。
     */
    private OnResultListener mFollowListener = new OnResultListener() {

        @Override
        public void onResult(final int status) {
            if (mPresenter.isUpdateFollowUserCountTextView()) {
                CommonUtils.runOnUIThread(UserInfoActivity.this, new Runnable() {

                    @Override
                    public void run() {
                        mFollowedUserCountTv.setText(String.valueOf(status));
                    }
                });
            }
        }
    };

    /**
     * 粉丝数的回调函数。在加载缓存或者下拉刷新时，可能需要更新显示的用户数字。
     */
    private OnResultListener mFansListener = new OnResultListener() {

        @Override
        public void onResult(final int status) {
            if (mPresenter.isUpdateFansCountTextView()) {
                CommonUtils.runOnUIThread(UserInfoActivity.this, new Runnable() {

                    @Override
                    public void run() {
                        mFansCountTextView.setText(String.valueOf(status));
                    }
                });
            }
        }
    };

    @Override
    public void setToggleButtonStatus(boolean status) {
        mFollowToggleButton.setChecked(status);
    }

    @Override
    public void updateFansTextView(int count) {
        mFansCountTextView.setText(String.valueOf(count));
    }

    @Override
    public void updateFeedTextView(int count) {
        mUser.feedCount = count;
        mPostedCountTv.setText(String.valueOf(count));
    }

    @Override
    public void updateFollowTextView(int count) {
        mFollowedUserCountTv.setText(String.valueOf(count));
    }

    @Override
    protected void onDestroy() {
        mPresenter.onDestroy();
        BroadcastUtils.unRegisterBroadcast(getApplicationContext(), mReceiver);
        super.onDestroy();
    }

    /**
     * 数据同步处理
     */
    protected DefalutReceiver mReceiver = new DefalutReceiver() {

        public void onReceiveFeed(Intent intent) {// 发送or删除时
            FeedItem feedItem = getFeed(intent);
            if (feedItem == null || !CommonUtils.isMyself(mUser)) {
                return;
            }

            BROADCAST_TYPE type = getType(intent);
            if (BROADCAST_TYPE.TYPE_FEED_POST == type) {
                updateFeedTextView(++mUser.feedCount);
            }
        }
    };
}
