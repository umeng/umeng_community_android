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

package com.umeng.comm.ui.presenter.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.CommUser.Permisson;
import com.umeng.comm.core.beans.CommUser.SubPermission;
import com.umeng.comm.core.beans.Comment;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.constants.ErrorCode;
import com.umeng.comm.core.listeners.Listeners.CommListener;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.nets.Response;
import com.umeng.comm.core.nets.responses.SimpleResponse;
import com.umeng.comm.core.nets.uitls.NetworkUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.TimeUtils;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.dialogs.ConfirmDialog;
import com.umeng.comm.ui.mvpview.MvpCommentView;
import com.umeng.comm.ui.presenter.BasePresenter;
import com.umeng.comm.ui.utils.BroadcastUtils;

public class CommentPresenter extends BasePresenter {
    MvpCommentView mCommentView;
    FeedItem mFeedItem;

    public CommentPresenter(MvpCommentView commentView, FeedItem feedItem) {
        mCommentView = commentView;
        mFeedItem = feedItem;
    }

    public void setFeedItem(FeedItem feedItem) {
        this.mFeedItem = feedItem;
    }

    /**
     * 根据评论的内容、评论者等信息创建一条评论
     * 
     * @param commentText 评论的内容
     * @return
     */
    private Comment createComment(String commentText, CommUser replyUser, String replyCommentId) {
        final Comment comment = new Comment();
        comment.feedId = mFeedItem.id;
        comment.text = commentText;
        comment.creator = CommConfig.getConfig().loginedUser;
        comment.replyUser = replyUser;
        if (!TextUtils.isEmpty(replyCommentId)) {
            comment.replyCommentId = replyCommentId;
        }
        return comment;
    }

    /**
     * 发送评论
     */
    public void postComment(String text, final CommUser replyUser, String replyCommentId) {
        final Comment comment = createComment(text, replyUser, replyCommentId);
        FetchListener<SimpleResponse> wrapListener = new FetchListener<SimpleResponse>() {

            @Override
            public void onStart() {
            }

            @Override
            public void onComplete(SimpleResponse response) {
                if (NetworkUtils.handleResponseComm(response)) {
                    return;
                }
                if (response.errCode != ErrorCode.NO_ERROR) {
                    ToastMsg.showShortMsgByResName("umeng_comm_post_comment_failed");
                    return;
                }
                ToastMsg.showShortMsgByResName("umeng_comm_post_comment_success");
                mFeedItem.commentCount++;
                comment.createTime = TimeUtils.format(formatDate());
                comment.id = response.id;
                mFeedItem.comments.add(0, comment);
                BroadcastUtils.sendFeedUpdateBroadcast(mContext, mFeedItem);
                if (mCommentView != null) {
                    mCommentView.postCommentSuccess(comment, replyUser);
                }
            }
        };
        // 发布评论
        mCommunitySDK.postComment(comment, wrapListener);
    }

    private void spamComment(String commentId) {
        mCommunitySDK.spamComment(commentId, new CommListener() {

            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(Response response) {
                if (NetworkUtils.handleResponseComm(response)) {
                    return;
                }
                if (response.errCode == ErrorCode.NO_ERROR) {
                    ToastMsg.showShortMsgByResName("umeng_comm_text_spammer_success");
                } else if (response.errCode == ErrorCode.SPAMMERED_CODE) {
                    ToastMsg.showShortMsgByResName("umeng_comm_comment_text_spammered");
                } else {
                    ToastMsg.showShortMsgByResName("umeng_comm_text_spammer_failed");
                }
            }
        });
    }

    @SuppressLint("SimpleDateFormat")
    private String formatDate() {
        // 转换为24小时制式的字串
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = Calendar.getInstance().getTime();
        return df.format(date);
    }

    private void showConfirmDialog(final Comment comment, String tips,
            DialogInterface.OnClickListener listener) {
        ConfirmDialog.showDialog(mContext, tips,
                listener);
    }

    private void deleteComment(final Comment comment) {
        mCommunitySDK.deleteComment(mFeedItem.id, comment.id, new CommListener() {

            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(Response response) {
                if ( NetworkUtils.handleResponseComm(response) ) {
                    return ;
                }
                if (response.errCode == ErrorCode.NO_ERROR) {
                    mFeedItem.comments.remove(comment);
                    mFeedItem.commentCount--;
                    mCommentView.onCommentDeleted(comment);
                    mDatabaseAPI.getCommentAPI().deleteCommentsFromDB(comment.id);
                } else {
                    ToastMsg.showShortMsgByResName("umeng_comm_delete_comment_failed");
                }
            }
        });
    }

    /**
     * 点击详情页面的评论item,如果是自己创建的评论,那么自己可以删除;如果是在自己发布的feed页面,那么自己可以删除、或者回复他人的评论
     * 
     * @param comment
     */
    public void clickCommentItem(int position, final Comment comment) {
        CommUser loginUser = CommConfig.getConfig().loginedUser;
        // 自己创建的评论可以删除
        if (comment.creator.id.equals(loginUser.id)) {
            showConfirmDialog(comment, ResFinder.getString("umeng_comm_delete_comment"),
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteComment(comment);
                        }
                    });
        } else if (isMyFeed(comment, loginUser.id)) { // 在自己发布的feed页面可以删除或者回复他人的评论
            showSelectDialog(position, comment, true);
        } else {
            boolean hasDeletePermission = (loginUser.permisson == Permisson.ADMIN && loginUser.subPermissions
                    .contains(SubPermission.DELETE_CONTENT));
            showSelectDialog(position, comment, hasDeletePermission);
        }
    }

    /**
     * 是否可以删除评论。可删除的条件是：自己的评论</br>
     * 
     * @param comment
     * @return
     */
    public boolean isMyFeed(Comment comment, String loginUserId) {
        // 如果该feed是自己创建，则可以删除该feed下的所有评论
        if (loginUserId.equals(mFeedItem.creator.id)) {
            return true;
        }
        return comment != null
                && comment.creator.id.equals(loginUserId);
    }

    /**
     * 弹出举报或者回复对话框
     */
    private void showSelectDialog(final int position, final Comment comment, final boolean delete) {
        final Dialog selectDialog = new Dialog(mContext,
                ResFinder.getStyle("umeng_comm_action_dialog_fullscreen"));
        selectDialog.setCanceledOnTouchOutside(true);
        selectDialog.setContentView(ResFinder.getLayout("umeng_comm_report_reply_comment_dialog"));
        TextView reportTextView = (TextView) selectDialog.findViewById(ResFinder
                .getId("umeng_comm_report_comment_tv"));
        if (delete) {
            reportTextView.setText(ResFinder.getString("umeng_comm_delete_feed_tips"));
        }
        reportTextView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                selectDialog.dismiss();
                String tips = delete ? ResFinder.getString("umeng_comm_delete_comment") : ResFinder
                        .getString("umeng_comm_confirm_spam");
                showConfirmDialog(comment, tips, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (delete) {
                            deleteComment(comment);
                        } else {
                            spamComment(comment.id);
                        }
                    }
                });
            }
        });
        TextView replyTextView = (TextView) selectDialog.findViewById(ResFinder
                .getId("umeng_comm_reply_comment_tv"));
        replyTextView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                selectDialog.dismiss();
                mCommentView.showCommentLayout(position, comment);
            }
        });
        selectDialog.show();
    }
}
