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

package com.umeng.comm.ui.anim;

import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * 
 */
public class UserInfoAnimator extends CustomAnimator {

    @Override
    protected void initAnimation(final boolean show) {
        mAnimation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime < 0) { // 预处理加速器可能出现负数
                    return;
                }
                MarginLayoutParams params = (MarginLayoutParams) mView.getLayoutParams();
                policyMargin(interpolatedTime, params, show);
                mView.setLayoutParams(params);
            }
        };
        mAnimation.setDuration(500);
        mAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
    }

    @Override
    protected void policyMargin(float interpolatedTime, MarginLayoutParams params, boolean show) {
        if (show) {
            params.topMargin = -(int) (mSourcePostion * (1 - interpolatedTime));
        } else {
            params.topMargin = -(int) (mSourcePostion * (interpolatedTime));
        }
    }
}
