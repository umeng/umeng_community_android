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

import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.login.LoginListener;
import com.umeng.comm.core.push.NullPushImpl;
import com.umeng.comm.core.push.Pushable;
import com.umeng.comm.core.sdkmanager.LoginSDKManager;
import com.umeng.comm.core.sdkmanager.PushSDKManager;
import com.umeng.comm.core.strategy.logout.InnerLogoutStrategy;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.DeviceUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.dialogs.ConfirmDialog;
import com.umeng.comm.ui.presenter.impl.NullPresenter;

/**
 * 设置页面,是各个子类型设置页面的入口
 */
public class SettingFragment extends BaseFragment<Void, NullPresenter> implements OnClickListener {

    // 由于开发者可能直接使用Fragment，在退出登录的时候，我们需要回到该Activity
    private String mContainerClass = null;
    private boolean mFromRegister = false;

    @Override
    protected int getFragmentLayout() {
        return ResFinder.getLayout("umeng_comm_setting");
    }

    @Override
    protected void initWidgets() {
        int userSettingResId = ResFinder.getId("umeng_comm_user_setting");
        int settingResId = ResFinder.getId("umeng_comm_msg_setting");
        int logoutResId = ResFinder.getId("umeng_comm_logout");
        OnClickListener clickListener = (OnClickListener) getActivity();
        mRootView.findViewById(userSettingResId).setOnClickListener(clickListener);
        mRootView.findViewById(settingResId).setOnClickListener(clickListener);
        // 登出
        mRootView.findViewById(logoutResId).setOnClickListener(this);
        checkConfigPush(mRootView);
    }

    public void setContainerClass(String clz) {
        mContainerClass = clz;
    }

    public void setFrom(boolean from) {
        mFromRegister = from;
    }

    /**
     * 检查是否配置push。如果未配置则不显示push开关</br>
     */
    private void checkConfigPush(View rootView) {
        Pushable pushImpl = PushSDKManager.getInstance().getCurrentSDK();
        // 判断Push是否配置,或者为NullPushImpl实现
        if (pushImpl == null || pushImpl instanceof NullPushImpl) {
            // 没有配置推送，推送设置按钮不可见
            rootView.findViewById(ResFinder.getId("umeng_comm_msg_setting")).setVisibility(
                    View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int logoutResId = ResFinder.getId("umeng_comm_logout");
        if (v.getId() == logoutResId) {
            String title = ResFinder.getString("umeng_comm_setting_logout");
            ConfirmDialog.showDialog(getActivity(),
                    title + "?",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            logout();
                        }
                    });

        }
    }

    /**
     * 注销登录。清除保存的用户信息，并调用开发者的注销逻辑</br>
     */
    private void logout() {
        if (CommonUtils.isActivityAlive(getActivity())
                && !DeviceUtils.isNetworkAvailable(getActivity())) {
            ToastMsg.showShortMsgByResName("umeng_comm_not_network");
            return;
        }
        // 退出登录的情况
        LoginSDKManager.getInstance().getCurrentSDK()
                .logout(getActivity(), new LoginListener() {

                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onComplete(int stCode, CommUser userInfo) {

                        Log.d(getTag(), "### 社区登出 , stCode = " + stCode);

                        if (stCode != 200) {
                            ToastMsg.showShortMsgByResName("umeng_comm_logout_failed");
                            return;
                        }

                        if (mContainerClass == null && !mFromRegister) {
                            Log.e(getTag(), " container class is null...");
                            return;
                        }

                        String uid = CommConfig.getConfig().loginedUser.id;
                        Log.e(getTag(), "### uid = " + uid);
                        // 关闭推送
                        Pushable pushable = PushSDKManager.getInstance().getCurrentSDK();
                        pushable.disable();
                        // 清空未读消息
                        CommConfig.getConfig().mMessageCount.clear();
                        // 清空SDK内部保存的用户信息
                        CommonUtils.logout();
                        // 置空用户信息
                        CommConfig.getConfig().loginedUser = new CommUser();
                        if (mContainerClass == null) {
                            getActivity().finish();
                            return;
                        }
                        Class<?> clz;
                        try {
                            clz = Class.forName(mContainerClass);
                            InnerLogoutStrategy strategy = CommConfig.getConfig()
                                    .getInnerLogoutStrategy();
                            if (strategy != null) {
                                strategy.afterLogout(getActivity(), clz);
                            }
                            // finish activity 也作为策略的一部分
                            // getActivity().finish();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}
