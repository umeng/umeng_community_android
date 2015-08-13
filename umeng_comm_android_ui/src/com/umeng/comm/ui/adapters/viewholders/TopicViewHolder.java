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
import android.widget.ToggleButton;

import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.widgets.RoundImageView;

/**
 * 话题ViewHolder
 */
public class TopicViewHolder extends ViewHolder {

    public RoundImageView mTopicIcon;
    public TextView mTopicTv;
    public TextView mDescTv;
    public ToggleButton mFollowedBtn;

    @Override
    protected void initWidgets() {
        mTopicTv = findViewById(ResFinder
                .getId("umeng_comm_topic_tv"));
        mDescTv = findViewById(ResFinder
                .getId("umeng_comm_topic_desc_tv"));
        mFollowedBtn = findViewById(ResFinder
                .getId("umeng_comm_topic_togglebutton"));
        mTopicIcon = findViewById(ResFinder
                .getId("umeng_comm_topic_icon"));
    }

    @Override
    protected int getItemLayout() {
        return ResFinder.getLayout("umeng_comm_followed_topic_lv_item");
    }

}
