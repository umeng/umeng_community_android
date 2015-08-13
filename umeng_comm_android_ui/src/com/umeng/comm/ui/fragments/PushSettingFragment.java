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

import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.presenter.impl.NullPresenter;
import com.umeng.comm.ui.widgets.SwitchButton;

/**
 * 推送设置页面
 */
public class PushSettingFragment extends BaseFragment<Void, NullPresenter> {

    private SwitchButton mSwitchButton;

    private CommConfig mSDKConfig = CommConfig.getConfig();

    @Override
    protected int getFragmentLayout() {
        return ResFinder.getLayout("umeng_comm_push_setting");
    }

    @Override
    protected void initWidgets() {
        int switchButtonResId = ResFinder.getId(
                "umeng_common_switch_button");
        mSwitchButton = (SwitchButton) mRootView
                .findViewById(switchButtonResId);
        mSwitchButton.setChecked(mSDKConfig.isPushEnable(getActivity()));
        mSwitchButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 保存配置
                CommConfig.getConfig().setSDKPushable(getActivity(), isChecked);
            }
        });
    }

    /**
     * 获取PushSettingFragment的对象</br>
     * 
     * @return
     */
    public static PushSettingFragment getPushSettingFragment() {
        return new PushSettingFragment();
    }
}
