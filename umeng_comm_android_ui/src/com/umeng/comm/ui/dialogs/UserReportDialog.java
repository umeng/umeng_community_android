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

package com.umeng.comm.ui.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import com.umeng.comm.core.CommunitySDK;
import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.constants.ErrorCode;
import com.umeng.comm.core.impl.CommunityFactory;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.SimpleResponse;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ToastMsg;

/**
 * 举报用户的Dialog
 */
public class UserReportDialog extends ActionDialog {

    UserReportPresenter mPresenter;

    public UserReportDialog(Context context) {
        super(context);
        mPresenter = new UserReportPresenter(context);
    }

    public void setTargetUid(String uid) {
        mPresenter.mUid = uid;
    }

    @Override
    protected void report() {
        String loginedUid = CommConfig.getConfig().loginedUser.id;
        if (mFeedItem != null && mFeedItem.creator.id.equals(loginedUid)) {
            ToastMsg.showShortMsgByResName("umeng_comm_do_not_spam_yourself");
            return;
        }
        mPresenter.showSpamDialog();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void initViewClickListeners() {
        super.initViewClickListeners();
        mDeleteView.setVisibility(View.GONE);
        mCopyView.setVisibility(View.GONE);
        mReportView.setBackgroundDrawable(ResFinder.getDrawable("umeng_comm_radius_bg"));
    }

    class UserReportPresenter {
        Context mContext;
        CommunitySDK mCommunitySDK;
        String mUid;

        public UserReportPresenter(Context context) {
            mContext = context;
            mCommunitySDK = CommunityFactory.getCommSDK(context);
        }

        protected void showSpamDialog() {
            ConfirmDialog.showDialog(mContext, ResFinder.getString("umeng_comm_sure_spam"),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int whitch) {
                            spamUser();
                        }
                    });
        }

        private void spamUser() {
            mCommunitySDK.spamUser(mUid, new SimpleFetchListener<SimpleResponse>() {
                @Override
                public void onComplete(SimpleResponse response) {
                    if (response.errCode == ErrorCode.NO_ERROR) {
                        ToastMsg.showShortMsgByResName("umeng_comm_text_spammer_success");
                    } else if (response.errCode == ErrorCode.SPAMMERED_CODE) {
                        ToastMsg.showShortMsgByResName("umeng_comm_user_spamed");
                    } else {
                        ToastMsg.showShortMsgByResName("umeng_comm_text_spammer_failed");
                    }
                }
            });
        }
    }

}
