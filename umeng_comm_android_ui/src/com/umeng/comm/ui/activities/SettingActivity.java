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
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.dialogs.ClipImageDialog;
import com.umeng.comm.ui.dialogs.ClipImageDialog.OnClickSaveListener;
import com.umeng.comm.ui.fragments.PushSettingFragment;
import com.umeng.comm.ui.fragments.SettingFragment;
import com.umeng.comm.ui.fragments.UserSettingFragment;

/**
 * 设置页面 注意：此Activity的名字不能修改，数据层需要回调此Activity
 */
public class SettingActivity extends BaseFragmentActivity implements OnClickListener {

    private TextView mTitleTextView;
    private SettingFragment mSettingFragment = new SettingFragment();
    private UserSettingFragment mUserSettingFragment;
    private PushSettingFragment mPushSettingFragment;
    private Button mSaveButton;
    private Bundle mExtra;
    private ClipImageDialog mClipImageDialog;
    public boolean isRegisterUserNameInvalid = false;//注册时，昵称是否无效
    // 由于开发者可能直接使用Fragment，在退出登录的时候，我们需要回到该Activity
    private String mContainerClass = null;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(ResFinder.getLayout("umeng_comm_setting_activity"));
        mContainerClass = getIntent().getExtras().getString(Constants.TYPE_CLASS);
        mSettingFragment.setContainerClass(mContainerClass);
        initViews();
        initFragment();
        switchHeaderView();
    }

    /**
     * 初始化相关View</br>
     */
    private void initViews() {
        findViewById(ResFinder.getId("umeng_comm_setting_back")).setOnClickListener(this);

        mTitleTextView = (TextView) findViewById(ResFinder.getId("umeng_comm_setting_title"));
        mSaveButton = (Button) findViewById(ResFinder.getId("umeng_comm_save_bt"));
        mSaveButton.setText(ResFinder.getString("umeng_comm_save"));
        mSaveButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        mSaveButton.setOnClickListener(this);
    }

    CommUser loginUser;

    /**
     * 初始化Fragment。显示用户设置Fragment or SettingFragment</br>
     */
    private void initFragment() {
        setFragmentContainerId(ResFinder.getId("umeng_comm_setting_content"));
        mExtra = getIntent().getExtras();
        if (mExtra != null && mExtra.containsKey(Constants.USER_SETTING)) {
            loginUser = mExtra.getParcelable(Constants.USER);
            isRegisterUserNameInvalid = mExtra.getBoolean(Constants.REGISTER_USERNAME_INVALID);
            showUserSettingFrgm(true, loginUser);
            mSettingFragment.setFrom(true);
        } else {
            showFragment(mSettingFragment);
        }
    }

    /**
     * 显示用户设置页面</br>
     */
    private void showUserSettingFrgm(boolean isFirstSetting, CommUser user) {
        if (mUserSettingFragment == null) {
            mUserSettingFragment = UserSettingFragment.getUserSettingFragment();
        }
        mUserSettingFragment.setFirstSetting(isFirstSetting);
        mUserSettingFragment.setUser(user);
        mUserSettingFragment.isRegisterUserNameInvalid = isRegisterUserNameInvalid;
        showFragment(mUserSettingFragment);
        isFirstSetting = false;
    }

    /**
     * 显示Push通知设置页面</br>
     */
    private void showPushSettingFrgm() {
        if (mPushSettingFragment == null) {
            mPushSettingFragment = PushSettingFragment.getPushSettingFragment();
        }
        showFragment(mPushSettingFragment);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == ResFinder.getId("umeng_comm_setting_back")) { // 返回事件
            dealBackLogic();
        } else if (id == ResFinder.getId("umeng_comm_save_bt")) { // 保存事件
            dealSaveLogic();
        } else if (id == ResFinder.getId("umeng_comm_user_setting")) { // 在SettingFragment中的按钮
            showUserSettingFrgm(false, loginUser);
        } else if (id == ResFinder.getId("umeng_comm_msg_setting")) { // 在SettingFragment中的按钮
            showPushSettingFrgm();
        }
        switchHeaderView();
    }

    /**
     * 处理保存事件的逻辑。如果当前fragment是用户设置页面，则执行更新用户接口</br>
     */
    private void dealSaveLogic() {
        if (mCurrentFragment instanceof UserSettingFragment) {
            final UserSettingFragment userSettingFragment = (UserSettingFragment) mCurrentFragment;
            userSettingFragment.registerOrUpdateUserInfo();
        }
        // showFragment(mSettingFragment);
    }

    /**
     * 处理back事件的逻辑。如果当前页面已经是设置页面，则直接返回(finish);否则回退到设置页面</br>
     */
    private void dealBackLogic() {
        if (mCurrentFragment == mSettingFragment) {
            finish();
        } else { // 如果是在用户设置页面，此时可能需要关闭软键盘
            if (mCurrentFragment instanceof UserSettingFragment) {
                UserSettingFragment settingFragment = (UserSettingFragment) mCurrentFragment;
                settingFragment.hideInputMethod();
                // 昵称不合法则需要修改昵称并且保持之后才可以退出.
                if (isRegisterUserNameInvalid) {
//                    ToastMsg.showShortMsg(this, ResFinder.getString("umeng_comm_user_name_tips"));
                    finish();
                    return;
                }
            }
            showFragment(mSettingFragment);
        }
    }

    /**
     * 根据不同的页面隐藏、显示、修改title的内容</br>
     */
    public void switchHeaderView() {
        String title = "";
        int visiable = View.VISIBLE;
        if (mCurrentFragment instanceof SettingFragment) {
            title = ResFinder.getString("umeng_comm_setting");
            visiable = View.GONE;
        } else if (mCurrentFragment instanceof UserSettingFragment) {
            title = ResFinder.getString("umeng_comm_setting_user");
            visiable = View.VISIBLE;
        } else if (mCurrentFragment instanceof PushSettingFragment) {
            title = ResFinder.getString("umeng_comm_setting_msg");
            visiable = View.GONE;
        }
        mTitleTextView.setText(title);
        mSaveButton.setVisibility(visiable);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 防止在选择图片的时候按返回键
        if (data == null) {
            return;
        }
        // 从相册中选择图片
        if (requestCode == Constants.PIC_SELECT) {
            int style = ResFinder.getStyle("umeng_comm_dialog_fullscreen");
            // 显示剪切图片的Dialog
            mClipImageDialog = new ClipImageDialog(this, data.getData(), style);
            mClipImageDialog.setOnClickSaveListener(mOnSaveListener);
            mClipImageDialog.show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mCurrentFragment != mSettingFragment
                && mExtra == null) {
            showFragment(mSettingFragment);
            switchHeaderView();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 剪切图片dialog页面，点击保存时的回调，更新UI</br>
     */
    private OnClickSaveListener mOnSaveListener = new OnClickSaveListener() {

        @Override
        public void onClickSave(Bitmap bitmap) {
            if (mCurrentFragment instanceof UserSettingFragment) {
                UserSettingFragment userSettingFragment = (UserSettingFragment) mCurrentFragment;
                userSettingFragment.showClipedBitmap(bitmap);
            }
        }
    };
}
