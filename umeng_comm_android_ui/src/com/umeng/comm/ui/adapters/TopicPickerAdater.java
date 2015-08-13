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

package com.umeng.comm.ui.adapters;

import android.content.Context;
import android.view.View;

import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.adapters.viewholders.FriendItemViewHolder;

/**
 * 发布Feed时的话题选择Adapter
 */
public class TopicPickerAdater extends CommonAdapter<Topic, FriendItemViewHolder> {

    private int mTextColor;

    public TopicPickerAdater(Context context) {
        super(context);
        mTextColor = ResFinder.getColor("umeng_comm_text_topic_light_color");
    }

    @Override
    protected FriendItemViewHolder createViewHolder() {
        return new FriendItemViewHolder();
    }

    @Override
    protected void setItemData(int position, FriendItemViewHolder viewHolder, View rootView) {
        final Topic item = getItem(position);
        viewHolder.mImageView.setVisibility(View.GONE);
        viewHolder.mDetailTextView.setVisibility(View.GONE);
        viewHolder.mTextView.setTextColor(mTextColor);
        viewHolder.mTextView.setText(item.name);
        viewHolder.mTextView.setVisibility(View.VISIBLE);
    }

}
