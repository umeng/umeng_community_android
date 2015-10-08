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

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.imageloader.UMImageLoader;
import com.umeng.comm.core.sdkmanager.ImageLoaderManager;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.ui.fragments.CommunityMainFragment;
import com.umeng.comm.ui.utils.FontUtils;
import com.umeng.comm.ui.utils.ViewFinder;

/**
 * 继承自FragmentActivity的Activity类型, 封装了FragmentManager和去除了title
 */
public class BaseFragmentActivity extends FragmentActivity {
    /**
     * Fragment管理器
     */
    protected FragmentManager mFragmentManager = null;
    /**
     * Fragment的parent view,即Fragment的容器
     */
    protected int mFragmentContainer;
    /**
     * 当前显示的Fragment
     */
    public Fragment mCurrentFragment;

    protected UMImageLoader mImageLoader = ImageLoaderManager.getInstance().getCurrentSDK();
    private int totalTime = 0;
    private boolean isFinish = false;
    private InputMethodManager mInputMan;
    protected CommunityMainFragment mFeedsFragment = new CommunityMainFragment();
    private ViewFinder mViewFinder;

    /**
     * 该Handler主要处理软键盘的弹出跟隐藏
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            View view = (View) msg.obj;
            // 显示软键盘
            if (msg.what == Constants.INPUT_METHOD_SHOW) {
                boolean result = mInputMan.showSoftInput(view, 0);
                if (!result && totalTime < Constants.LIMIT_TIME) {
                    totalTime += Constants.IDLE;
                    Message message = Message.obtain(msg);
                    mHandler.sendMessageDelayed(message, Constants.IDLE);
                } else if (!isFinish) {
                    totalTime = 0;
                    result = view.requestFocus();
                    isFinish = true;
                }
            } else if (msg.what == Constants.INPUT_METHOD_DISAPPEAR) {
                // 隐藏软键盘
                mInputMan.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

        }
    };

    @Override
    protected void onCreate(Bundle arg0) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(arg0);
        mFragmentManager = getSupportFragmentManager();
        mInputMan = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        mViewFinder = new ViewFinder(getWindow().getDecorView());
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        mViewFinder = new ViewFinder(view);
    }

    @Override
    public void setContentView(View view, LayoutParams params) {
        super.setContentView(view, params);
        mViewFinder = new ViewFinder(view);
    }

    public <T extends View> T findViewByIdWithFinder(int id) {
        return mViewFinder.findViewById(id);
    }

    /*
     * [ 不要删除该函数 ],该函数的空实现修复了FragmentActivity中的bug
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从SharedPreferences中获取用户信息
        CommConfig.getConfig().loginedUser = CommonUtils.getLoginUser(this);
        // 修改字体
        FontUtils.changeTypeface(getWindow().getDecorView());
    }

    public void showMainFeedFragment() {
        showFragment(mFeedsFragment);
    }

    public void initFragment(int container) {
        setFragmentContainerId(container);
        showFragment(mFeedsFragment);
    }

    /**
     * 显示输入法</br>
     * 
     * @param view
     */
    public void showInputMethod(View view) {
        sendInputMethodMessage(Constants.INPUT_METHOD_SHOW, view);
    }

    /**
     * 隐藏输入法</br>
     * 
     * @param view
     */
    public void hideInputMethod(View view) {
        sendInputMethodMessage(Constants.INPUT_METHOD_DISAPPEAR, view);
    }

    /**
     * 发送show or hide输入法消息</br>
     * 
     * @param type
     * @param view
     */
    private void sendInputMethodMessage(int type, View view) {
        Message message = mHandler.obtainMessage(type);
        message.obj = view;
        mHandler.sendMessage(message);
    }

    /**
     * @param container 用于放置fragment的布局id
     * @param fragment 要添加的Fragment
     */
    public void addFragment(int container, Fragment fragment) {

        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        if (!isFragmentAdded(fragment)) {
            fragmentTransaction
                    .add(container, fragment,
                            fragment.getClass().getName()).commitAllowingStateLoss();
            mCurrentFragment = fragment;
        } else {
            fragmentTransaction.show(fragment).commitAllowingStateLoss();
        }

        mFragmentContainer = container;
    }

    /**
     * 需要在调用任何函数前设置
     * 
     * @param container 用于放置fragment的布局id
     */
    public void setFragmentContainerId(int container) {
        mFragmentContainer = container;
    }

    /**
     * 判断一个Fragment是否已经添加
     * 
     * @param fragment 要判断是否已经添加的Fragment
     * @return
     */
    public boolean isFragmentAdded(Fragment fragment) {
        return fragment != null
                && mFragmentManager.findFragmentByTag(fragment.getClass().getName()) != null;
    }

    /**
     * 检查放置fragment的布局id
     */
    private void checkContainer() {
        if (mFragmentContainer <= 0) {
            throw new RuntimeException(
                    "在调用replaceFragment函数之前请调用setFragmentContainerId函数来设置fragment container id");
        }
    }

    /**
     * 显示Fragment，并且把上一个隐藏
     * 
     * @param fragment
     */
    public void showFragment(Fragment fragmentShow) {
        showFragmentInContainer(mFragmentContainer, fragmentShow);
    }

    /**
     * 将fragmentShow显示在一个新的container上,而不覆盖mFragmentContainer。
     * 这种情况适用于Fragment中又嵌套Fragment
     * 
     * @param container
     * @param fragmentShow
     */
    public void showFragmentInContainer(int container, Fragment fragmentShow) {
        checkContainer();

        if (mCurrentFragment != fragmentShow) {
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            if (mCurrentFragment != null) {
                // 首先隐藏原来显示的Fragment
                transaction.hide(mCurrentFragment);
            }
            // 然后再显示传递进来的Fragment
            if (mFragmentManager.findFragmentByTag(fragmentShow.getClass().getName()) == null) {
                transaction
                        .add(container, fragmentShow, fragmentShow.getClass().getName());
            } else {
                transaction.show(fragmentShow);
            }
            transaction.commitAllowingStateLoss();
            mCurrentFragment = fragmentShow;
        }
    }

    /**
     * 移除上一个Fragment，显示传递进来的Fragment
     * 
     * @param fragment
     */
    public void replaceFragment(Fragment fragment) {
        replaceFragment(fragment, false);
    }

    /**
     * @param fragment
     */
    public void replaceFragment(Fragment fragment, boolean isAddToBackStack) {
        checkContainer();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(mFragmentContainer,
                fragment);
        if (isAddToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commitAllowingStateLoss();
        mCurrentFragment = fragment;
    }

    /**
     * @param container
     * @param fragment
     */
    public void replaceFragment(int container, Fragment fragment) {
        checkContainer();
        if (mCurrentFragment != fragment) {
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            transaction.replace(container,
                    fragment, fragment.getClass().getSimpleName());
            transaction.commitAllowingStateLoss();
            mCurrentFragment = fragment;
        }
    }

    /**
     * @param fragment
     */
    public void remove(Fragment fragment) {
        if (null != fragment) {
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            transaction.remove(fragment);
            transaction.commitAllowingStateLoss();
        }
    }

    /**
     * @param fragment
     */
    public void detach(Fragment fragment) {
        if (null != fragment) {
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            transaction.detach(fragment);
            transaction.commit();
        }
    }

    @Override
    protected void onDestroy() {
        // avoid memory leak
        mHandler.removeCallbacksAndMessages(null);
        if (mFeedsFragment != null) {
            mFeedsFragment.hideCommentLayoutAndInputMethod();
        }
        super.onDestroy();
    }

}
