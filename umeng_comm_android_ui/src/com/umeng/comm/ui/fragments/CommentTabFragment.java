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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ResFinder.ResType;
import com.umeng.comm.ui.presenter.impl.NullPresenter;
import com.umeng.comm.ui.widgets.ViewPagerIndicator;

/**
 * 我的消息中评论模块下的两个tab Fragment
 */
public class CommentTabFragment extends BaseFragment<Void, NullPresenter> {

    CommentReceivedFragment mReceiveFragment;
    CommentPostedFragment mPostedFragment;

    private ViewPagerIndicator mIndicator;
    private ViewPager mViewPager;
    private String[] mTitles = null;
    private FragmentPagerAdapter mAdapter;

    @Override
    protected int getFragmentLayout() {
        return ResFinder.getLayout("umeng_comm_comment_main_layout");
    }

    @Override
    protected void initWidgets() {

        mTitles = getResources().getStringArray(
                ResFinder.getResourceId(ResType.ARRAY, "umeng_comm_comments_tabs"));

        mIndicator = (ViewPagerIndicator) findViewById(ResFinder
                .getId("umeng_comm_comment_indicator"));
        mViewPager = (ViewPager) findViewById(ResFinder.getId("umeng_comm_comment_viewPager"));
        mIndicator.setTabItemTitles(mTitles);
        mAdapter = new FragmentPagerAdapter(getFragmentManager()) {

            @Override
            public int getCount() {
                return mTitles.length;
            }

            @Override
            public Fragment getItem(int pos) {
                return getFragment(pos);
            }
        };
        mViewPager.setAdapter(mAdapter);
        // 设置关联的ViewPager
        mIndicator.setViewPager(mViewPager, 0);
    }

    /**
     * 获取对应的Fragment。0：话题聚合 1：活跃用户</br>
     * 
     * @param pos
     * @return
     */
    private Fragment getFragment(int pos) {
        if (pos == 0) {
            if (mReceiveFragment == null) {
                mReceiveFragment = new CommentReceivedFragment();
            }
            return mReceiveFragment;
        } else if (pos == 1) {
            if (mPostedFragment == null) {
                mPostedFragment = new CommentPostedFragment();
            }
            return mPostedFragment;
        }
        return null;
    }

}
