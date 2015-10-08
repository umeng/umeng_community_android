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

package com.umeng.comm.ui.presenter.impl;

import android.app.Activity;
import android.content.Intent;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.constants.ErrorCode;
import com.umeng.comm.core.db.ctrl.impl.DatabaseAPI;
import com.umeng.comm.core.impl.CommunityFactory;
import com.umeng.comm.core.listeners.Listeners.CommListener;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.nets.Response;
import com.umeng.comm.core.nets.responses.LoginResponse;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.LoginHelper;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.activities.GuideActivity;
import com.umeng.comm.ui.mvpview.MvpUserProfileSettingView;
import com.umeng.comm.ui.presenter.BaseFragmentPresenter;
import com.umeng.comm.ui.utils.BroadcastUtils;

public class UserSettingPresenter extends BaseFragmentPresenter<CommUser> {
    Activity mActivity;
    MvpUserProfileSettingView mProfileSettingView;
    private boolean isFirstSetting = false;// 是否第一次登录跳转到设置页面

    public UserSettingPresenter(Activity context, MvpUserProfileSettingView view) {
        mActivity = context;
        mCommunitySDK = CommunityFactory.getCommSDK(context);
        mProfileSettingView = view;
    }

    public void register(final CommUser user) {
        mCommunitySDK.register(user, new FetchListener<LoginResponse>() {

            @Override
            public void onStart() {
                mProfileSettingView.showLoading(true);
            }

            @Override
            public void onComplete(LoginResponse response) {
                mProfileSettingView.showLoading(false);
                if (response.errCode == ErrorCode.NO_ERROR) {
                    LoginHelper.loginSuccess(mActivity, response.result, user.source);
                    Intent intent = new Intent(mActivity, GuideActivity.class);
                    mActivity.startActivity(intent);
                    mActivity.finish();
                }

                showResponseToast(response);
            }
        });
    }

    public void updateUserProfile(final CommUser user) {
        mCommunitySDK.updateUserProfile(user, new CommListener() {

            @Override
            public void onStart() {
                mProfileSettingView.showLoading(true);
            }

            @Override
            public void onComplete(Response response) {
                mProfileSettingView.showLoading(false);
                showResponseToast(response);
                if (response.errCode == ErrorCode.NO_ERROR) {
                    CommConfig.getConfig().loginedUser = user;
                    saveUserInfo(response, user);
                    BroadcastUtils.sendUserUpdateBroadcast(mActivity, user);
                } else {
                    showResponseToast(response);
                }
            }
        });
    }

    /**
     * 保存用户信息</br>
     * 
     * @param data
     * @param tmpUser
     */
    private void saveUserInfo(Response data, CommUser newUser) {
        DatabaseAPI.getInstance().getUserDBAPI().saveUserInfoToDB(newUser);
        CommonUtils.saveLoginUserInfo(mActivity, newUser);
        if (isFirstSetting) {
            isFirstSetting = false;
            Intent intent = new Intent(mActivity, GuideActivity.class);
            mActivity.startActivity(intent);
            mActivity.finish();
        }
    }

    public void setFirstSetting(boolean isFirstSetting) {
        this.isFirstSetting = isFirstSetting;
    }

    /**
     * 根据错误码Toast操作</br>
     * 
     * @param data
     */
    private void showResponseToast(Response data) {
        if (data.errCode == ErrorCode.NO_ERROR) {
            ToastMsg.showShortMsgByResName("umeng_comm_update_info_success");
        } else if (data.errCode == ErrorCode.SENSITIVE_ERR_CODE) { // 昵称含有敏感词
            ToastMsg.showShortMsgByResName("umeng_comm_username_sensitive");
        } else if (data.errCode == ErrorCode.ERR_CODE_USER_NAME_DUPLICATE) { // 昵称重复
            ToastMsg.showShortMsgByResName("umeng_comm_duplicate_name");
        } else if (data.errCode == ErrorCode.ERR_CODE_USER_NAME_ILLEGAL_CHAR) {
            ToastMsg.showShortMsgByResName("umeng_comm_user_name_illegal_char");
        }else {
            ToastMsg.showShortMsgByResName("umeng_comm_update_userinfo_failed");
        }
    }
}
