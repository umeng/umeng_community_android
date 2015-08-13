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

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;

import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.fragments.RecommendTopicFragment;
import com.umeng.comm.ui.fragments.RecommendUserFragment;

/**
 * 用户首次注册成功并修改用户信息后，将进行话题跟活跃用户的引导
 */
public class GuideActivity extends BaseFragmentActivity {

    private int mContainer = 0;
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(ResFinder.getLayout("umeng_comm_guide_activity"));
        mContainer = ResFinder.getId("umeng_comm_guide_container");
        showTopicFragment();
    }

    /**
     * 显示话题引导页面</br>
     */
    private void showTopicFragment() {
        RecommendTopicFragment topicRecommendDialog =RecommendTopicFragment.newRecommendTopicFragment();
        topicRecommendDialog.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                showRecommendUserFragment();
            }
        });
        addFragment(mContainer, topicRecommendDialog);
    }

    private void showRecommendUserFragment() {
        setFragmentContainerId(mContainer);
        RecommendUserFragment recommendUserFragment = new RecommendUserFragment();
        replaceFragment(mContainer, recommendUserFragment);
    }

}
