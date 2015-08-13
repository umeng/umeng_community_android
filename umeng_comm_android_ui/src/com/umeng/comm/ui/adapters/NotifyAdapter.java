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
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

import com.umeng.comm.core.beans.Notification;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.sdkmanager.ImageLoaderManager;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.activities.UserInfoActivity;
import com.umeng.comm.ui.adapters.viewholders.NotifyMsgViewHolder;

/**
 * 系统通知消息Adapter
 */
public class NotifyAdapter extends CommonAdapter<Notification, NotifyMsgViewHolder> {

    public NotifyAdapter(Context context) {
        super(context);
    }

    @Override
    protected NotifyMsgViewHolder createViewHolder() {
        return new NotifyMsgViewHolder();
    }

    @Override
    protected void setItemData(int position, NotifyMsgViewHolder viewHolder, View rootView) {
        final Notification item = getItem(position);
        ImageLoaderManager.getInstance().getCurrentSDK()
                .displayImage(item.from.iconUrl, viewHolder.userImageView);
        viewHolder.userImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, UserInfoActivity.class);
                intent.putExtra(Constants.TAG_USER, item.from);
                mContext.startActivity(intent);
            }
        });
        viewHolder.userNameTextView.setText(ResFinder.getString("umeng_comm_come_from")
                + item.from.name);
        viewHolder.timeTextView.setText(item.timeStamp);
        viewHolder.notifyTextView.setText(item.msg);
    }
}
