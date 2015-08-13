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
import android.support.v4.app.Fragment;

import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.fragments.FollowedTopicFragment;

/**
 * 用户关注的话题列表
 * 
 * @author mrsimple
 */
public class FollowedTopicActivity extends BaseTitleActivity {

    @Override
    protected int getContentView() {
        return ResFinder.getLayout("umeng_comm_followed_topic_layout");
    }

    @Override
    protected void initTitleLayout() {
        super.initTitleLayout();
        mTitleTextView.setText(ResFinder.getString("umeng_comm_topic"));
    }

    @Override
    protected void initFragment() {
        addFragment(ResFinder.getId("umeng_comm_user_followed_container"),
                createFragment(getIntent()));
    }

    private Fragment createFragment(Intent intent) {
        String uid = intent.getStringExtra(Constants.USER_ID_KEY);
        return FollowedTopicFragment.newFollowedTopicFragment(uid);
    }
}
