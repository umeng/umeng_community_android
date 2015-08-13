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

import android.view.View;

import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.presenter.impl.FollowedTopicPresenter;
import com.umeng.comm.ui.presenter.impl.RecommendTopicPresenter;

/**
 * 用户已经关注的话题Fragment
 */
public class FollowedTopicFragment extends TopicFragment {

    String mUid;

    public static FollowedTopicFragment newFollowedTopicFragment(String uid) {
        FollowedTopicFragment followedTopicFragment = new FollowedTopicFragment();
        followedTopicFragment.mUid = uid;
        return followedTopicFragment;
    }

    @Override
    protected RecommendTopicPresenter createPresenters() {
        return new FollowedTopicPresenter(mUid, this);
    }
    
    @Override
    protected void initSearchView(View rootView) {
        super.initSearchView(rootView);
        mSearchLayout.setVisibility(View.GONE);
    }
    
    protected void initRefreshView(View rootView) {
        super.initRefreshView(rootView);
        mBaseView.setEmptyViewText(ResFinder.getString("umeng_comm_no_focus_topic"));
    }
}
