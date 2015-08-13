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

public class NotifyMsgViewHolder extends ViewHolder {
    public RoundImageView userImageView;
    public TextView userNameTextView;
    public TextView notifyTextView;
    public TextView timeTextView;
    
    @Override
    protected int getItemLayout() {
        return ResFinder.getLayout("umeng_comm_notify_item");
    }
    
    @Override
    protected void initWidgets() {
        userImageView = findViewById(ResFinder
                .getId("umeng_comm_notify_user_imageview"));
        timeTextView = findViewById(ResFinder
                .getId("umeng_comm_notify_time_tv"));
        userNameTextView = findViewById(ResFinder
                .getId("umeng_comm_notify_name_tv"));
        notifyTextView = findViewById(ResFinder
                .getId("umeng_comm_notify_desc_tv"));
    }

}
