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

package com.umeng.comm.ui.adapters.viewholders;

import android.widget.TextView;

import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.widgets.RoundImageView;

/**
 * 包含imageview和textview的ViewHolder
 */
public class FriendItemViewHolder extends ViewHolder {
    public RoundImageView mImageView;
    public TextView mTextView;
    public TextView mDetailTextView;

    @Override
    protected void initWidgets() {
        int iconResId = ResFinder.getId("umeng_comm_friend_picture");
        int nameResId = ResFinder.getId("umeng_comm_friend_name");
        int otherInfoResId = ResFinder.getId("umeng_comm_other_info");

        // 查找views
        mImageView = findViewById(iconResId);
        mTextView = findViewById(nameResId);
        mDetailTextView = findViewById(otherInfoResId);
    }

    @Override
    protected int getItemLayout() {
        return ResFinder.getLayout("umeng_comm_at_friend_lv_item");
    }

}
