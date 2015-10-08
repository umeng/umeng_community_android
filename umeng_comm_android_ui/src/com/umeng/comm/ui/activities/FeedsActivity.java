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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.db.ctrl.impl.DatabaseAPI;
import com.umeng.comm.core.utils.ResFinder;

/**
 * 社区主界面, 页面包含消息流主页面、话题选择页面、消息发布页面, 默认为消息流主界面.
 * 注意：此Activity的名字不能修改，数据层需要回调此Activity
 */
public class FeedsActivity extends BaseFragmentActivity implements OnClickListener {

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(ResFinder.getLayout("umeng_comm_feeds_activity"));

        // 设置fragment的container id
        initFragment(ResFinder.getId("umeng_comm_main_container"));
        addLoginPlatforms();
    }

    /**
     * 该代码仅仅在“一建生成apk”情况下被调用</br>
     */
    private void addLoginPlatforms() {
        boolean isFromGenerateApk = getApplication().getClass().getSuperclass()
                .equals(Application.class);
        if (isFromGenerateApk) {
            try {
                Method method = getApplication().getClass().getMethod("addLoginPlatforms",
                        Activity.class);
                method.invoke(null, this);
            } catch (NoSuchMethodException e) {
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == ResFinder.getId("umeng_comm_back_btn")) {// 点击返回按钮的情况
            this.finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        dealLogoutLoginc();
    }

    /**
     * 处理退出登录后，回到FeedsActivity时的逻辑</br>
     */
    private void dealLogoutLoginc() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            boolean fromLogout = bundle.getBoolean(Constants.FROM_COMMUNITY_LOGOUT);
            if (fromLogout) {
                // 此时需要将intent的数据更新，避免下次操作将数据带过来~
                getIntent().putExtra(Constants.FROM_COMMUNITY_LOGOUT, false);
                mFeedsFragment.cleanAdapterData();
                mFeedsFragment.repeatLoadDataFromServer();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mFeedsFragment != null) {
            mFeedsFragment.hideCommentLayoutAndInputMethod();
        }
        DatabaseAPI.getInstance().getFeedDBAPI().resetOffset();
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

}
