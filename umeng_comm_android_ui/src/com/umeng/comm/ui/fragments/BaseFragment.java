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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.ui.presenter.BaseFragmentPresenter;
import com.umeng.comm.ui.utils.FontUtils;
import com.umeng.comm.ui.utils.ViewFinder;

/**
 * Fragment基类,在该类中定义了Fragment的初始化流程.Fragment中首先会通过
 * {@link #inflateRootView(LayoutInflater, ViewGroup, Bundle)}
 * 函数加载该页面的视图,然后通过该页面的根视图构造一个ViewFinder,用户可以通过findViewById很方便的查找子视图。
 * 由于友盟微社区采用MVP架构，因此下一步用户需要覆写{@link #createPresenters()}返回该Fragment对应的Presenter;
 * 再下一步是用户通过{@link #initWidgets()} 初始化各个子视图,如果有一些事件处理则通过
 * {@link #initEventHandlers()}处理。
 * 
 * @param <T> 该Fragment加载的数据类型,例如Feed流列表的类型为List<FeedItem>
 * @param <P> 继承自BaseFragmentPresenter基类的Presenter类型
 */
public abstract class BaseFragment<T, P extends BaseFragmentPresenter<T>> extends Fragment {
    /**
     * 视图查找器,避免findViewById时进行强制转换
     */
    protected ViewFinder mViewFinder;
    /**
     * 布局加载LayoutInflater
     */
    protected LayoutInflater mLayoutInflater;
    /**
     * 根视图
     */
    protected View mRootView;
    /**
     * 该页面对应的Presenter
     */
    protected P mPresenter;

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        CommonUtils.saveComponentImpl(getActivity());// 注意此处必须保存登录组件的信息
        mLayoutInflater = inflater;
        mRootView = mLayoutInflater.inflate(getFragmentLayout(), container, false);
        mViewFinder = new ViewFinder(mRootView);
        mPresenter = createPresenters();
        initWidgets();
        initEventHandlers();
        setupOthers();
        FontUtils.changeTypeface(mRootView);
        if (mPresenter != null) {
            mPresenter.attach(getActivity());
        }
        return mRootView;
    }
    
    // protected abstract View inflateRootView(LayoutInflater inflater,
    // ViewGroup container,
    // Bundle savedInstanceState);

    protected abstract int getFragmentLayout();

    /**
     * 初始化子视图
     */
    protected void initWidgets() {

    }

    /**
     * 处理各种点击事件等
     */
    protected void initEventHandlers() {

    }

    /**
     * 创建该Fragment对应的Presenter,其中Presenter可以有多个，但是返回的Presenter只有一个,
     * 返回的Presenter赋值给了mPresenter变量
     * 
     * @return
     */
    protected P createPresenters() {
        return null;
    }

    /**
     * 其他设置,在onCreateView的最后一步
     */
    protected void setupOthers() {

    }

    protected <V extends View> V findViewById(int viewId) {
        return mViewFinder.findViewById(viewId);
    }

    /**
     * 在onResume中修改字体
     */
    public void onResume() {
        super.onResume();
        // 修改字体
        FontUtils.changeTypeface(getView());
    }

    @Override
    public void onDetach() {
        if (mPresenter != null) {
            mPresenter.detach();
        }
        super.onDetach();
    }
}
