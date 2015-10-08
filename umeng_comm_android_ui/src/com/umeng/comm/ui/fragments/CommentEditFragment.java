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

package com.umeng.comm.ui.fragments;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.Comment;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ResFinder.ResType;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.emoji.EmojiBean;
import com.umeng.comm.ui.emoji.EmojiBorad;
import com.umeng.comm.ui.emoji.EmojiBorad.OnEmojiItemClickListener;
import com.umeng.comm.ui.mvpview.MvpCommentView;
import com.umeng.comm.ui.presenter.BaseFragmentPresenter;
import com.umeng.comm.ui.presenter.impl.CommentPresenter;
import com.umeng.comm.ui.widgets.CommentEditText;
import com.umeng.comm.ui.widgets.CommentEditText.EditTextBackEventListener;

/**
 * 含有评论发布页面的Fragment基类
 * 
 * @param <T> 数据类型
 * @param <P> Presenter类型
 */
public abstract class CommentEditFragment<T, P extends BaseFragmentPresenter<T>> extends
        BaseFragment<T, P> implements MvpCommentView {
    /**
     * 评论布局的根视图
     */
    protected View mCommentLayout;
    /**
     * 评论内容编辑框
     */
    protected CommentEditText mCommentEditText;
    /**
     * 发送按钮
     */
    private View mCommentSendView;
    /**
     * 输入法
     */
    protected InputMethodManager mInputMgr;
    /**
     * 当前的Feed
     */
    protected FeedItem mFeedItem;
    /**
     * 评论的Presenter
     */
    protected CommentPresenter mCommentPresenter;
    /**
     * emoji表情触发按钮
     */
    protected ImageView mEmojiImageView;
    /**
     * 对某人进行回复。用在评论的时候显示在EditText中
     */
    protected CommUser mReplyUser;
    /** 对某条评论进行回复 */
    protected String mReplyCommentId = "";
    /**
     * emoji表情面板
     */
    private EmojiBorad mEmojiBoard;

    private int totalTime = 0;
    private boolean isFinish = false;
    private int mKeyboardIconRes;
    private int mEmojiIconRes;
    private BaseInputConnection mInputConnection = null;

    @Override
    protected void initWidgets() {
        mCommentLayout = mViewFinder.findViewById(ResFinder
                .getId("umeng_comm_comment_edit_layout"));
        mCommentLayout.setClickable(true);
        mCommentEditText = mViewFinder.findViewById(ResFinder
                .getId("umeng_comm_comment_edittext"));
        mCommentEditText.setEditTextBackListener(new EditTextBackEventListener() {

            @Override
            public boolean onClickBack() {
                hideCommentLayout();
                mEmojiBoard.setVisibility(View.GONE);
                return true;
            }
        });

        // 点击评论编辑框时，此时将弹出软键盘，需要隐藏掉表情面板
        mCommentEditText.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mEmojiBoard.setVisibility(View.GONE);
            }
        });

        mInputConnection = new BaseInputConnection(mCommentEditText, true);

        mCommentSendView = mViewFinder.findViewById(ResFinder
                .getId("umeng_comm_comment_send_button"));
        mCommentSendView.setClickable(true);
        mCommentSendView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                final String content = mCommentEditText.getText().toString();
                if (!checkCommentData(content)) {
                    return;
                }
                hideCommentLayout();
                postComment(content);
            }
        });

        mEmojiImageView = mViewFinder.findViewById(ResFinder.getId("umeng_comm_emoji"));
        mEmojiBoard = mViewFinder.findViewById(ResFinder.getId("umeng_comm_emojiview"));
        mKeyboardIconRes = ResFinder.getResourceId(ResType.DRAWABLE, "umeng_comm_emoji_keyboard");
        mEmojiIconRes = ResFinder.getResourceId(ResType.DRAWABLE, "umeng_comm_emoji");

        // click emoji ImageView to show emoji board
        mEmojiImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mEmojiBoard.getVisibility() == View.VISIBLE) { // 显示输入法，隐藏表情board
                    mEmojiBoard.setVisibility(View.GONE);
                    mEmojiImageView.setImageResource(mEmojiIconRes);
                    getActivity().getWindow().setSoftInputMode(
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                                    | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    sendInputMethodMessage(Constants.INPUT_METHOD_SHOW, mCommentEditText);
                } else { // 隐藏输入法，显示表情board
                    mEmojiImageView.setImageResource(mKeyboardIconRes);
                    sendInputMethodMessage(Constants.INPUT_METHOD_DISAPPEAR, mCommentEditText);
                    mHandler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            mEmojiBoard.setVisibility(View.VISIBLE);
                        }
                    }, 80);
                }
            }
        });

        // 点击表情的某一项的回调函数
        mEmojiBoard.setOnEmojiItemClickListener(new OnEmojiItemClickListener() {

            @Override
            public void onItemClick(EmojiBean emojiBean) {
                // delete event
                if (EmojiBorad.DELETE_KEY.equals(emojiBean.getEmoji())) {
                    // 对于删除事件，此时模拟一个输入法上的删除事件达到删除的效果
                    //【注意：此处不能调用delete方法，原因是emoji有些是单字符，有的是双字符】
                    mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                            KeyEvent.KEYCODE_DEL));
                    return;
                }
                // 预判断，如果插入超过140字符，则不显示最新的表情
                int emojiLen = emojiBean.isDouble ? 2: 1;
                if ( mCommentEditText.getText().length()+emojiLen > Constants.COMMENT_CHARS ) {
                    ToastMsg.showShortMsgByResName("umeng_comm_comment_text_max");
                    return ;
                }
                int start = mCommentEditText.getSelectionStart();
                int end = mCommentEditText.getSelectionEnd();
                if (start < 0) {
                    mCommentEditText.append(emojiBean.getEmoji());
                } else {
                    mCommentEditText.getText().replace(Math.min(start, end), Math.max(start, end),
                            emojiBean.getEmoji(), 0, emojiBean.getEmoji().length());
                }
            }
        });

        // 此时如果点击其它区域，需要隐藏表情面板
        mCommentEditText.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    mEmojiBoard.setVisibility(View.GONE);
                }
            }
        });

        mInputMgr = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
    }
    

    /**
     * 该Handler主要处理软键盘的弹出跟隐藏
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            View view = (View) msg.obj;
            // 显示软键盘
            if (msg.what == Constants.INPUT_METHOD_SHOW) {
                boolean result = mInputMgr.showSoftInput(view, 0);
                if (!result && totalTime < Constants.LIMIT_TIME) {
                    totalTime += Constants.IDLE;
                    Message message = Message.obtain(msg);
                    mHandler.sendMessageDelayed(message, Constants.IDLE);
                } else if (!isFinish) {
                    totalTime = 0;
                    result = view.requestFocus();
                    isFinish = true;
                }
            } else if (msg.what == Constants.INPUT_METHOD_DISAPPEAR) {
                // 隐藏软键盘
                mInputMgr.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    };

    /**
     * 发送show or hide输入法消息</br>
     * 
     * @param type
     * @param view
     */
    protected void sendInputMethodMessage(int type, View view) {
        Message message = mHandler.obtainMessage(type);
        message.obj = view;
        mHandler.sendMessage(message);
    }

    @Override
    protected P createPresenters() {
        // 初始化评论相关的Presenter
        mCommentPresenter = new CommentPresenter(this, mFeedItem);
        mCommentPresenter.attach(getActivity());
        return null;
    }

    protected void postComment(String text) {
        if (mCommentPresenter == null) {
            mCommentPresenter = new CommentPresenter(this, mFeedItem);
            mCommentPresenter.attach(getActivity());
        }
        mCommentPresenter.postComment(text, mReplyUser, mReplyCommentId);
    }

    protected void showCommentLayout() {
        mCommentLayout.setVisibility(View.VISIBLE);
        mCommentLayout.setClickable(true);
        mCommentEditText.requestFocus();
        mCommentEditText.postDelayed(new Runnable() {

            @Override
            public void run() {
                mInputMgr.showSoftInput(mCommentEditText, 0);
            }
        }, 30);
        
    }
    
    @Override
    public void showCommentLayout(int realPosition, Comment comment) {
        showCommentLayout();
    }

    protected void hideCommentLayout() {
        mCommentLayout.setVisibility(View.GONE);
        mEmojiBoard.setVisibility(View.GONE);
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        mInputMgr.hideSoftInputFromWindow(mCommentEditText.getWindowToken(), 0);
    }

    /**
     * 检查评论是否有效。目前仅仅判空</br>
     * 
     * @param content 评论的内容
     * @return
     */
    private boolean checkCommentData(String content) {
        // 检查评论的内容是否合法
        if (TextUtils.isEmpty(content)) {
            ToastMsg.showShortMsgByResName("umeng_comm_content_invalid");
            return false;
        }
        if (content.length() > 140) {
            ToastMsg.showShortMsgByResName("umeng_comm_comment_text_overflow");
            return false;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        if (mCommentPresenter != null) {
            mCommentPresenter.detach();
        }
        super.onDestroy();
    }

    @Override
    public void postCommentSuccess(Comment comment, CommUser replyUser) {
    }

    @Override
    public void loadMoreComment(List<Comment> comments) {
    }

    @Override
    public void onRefreshEnd() {

    }

    @Override
    public void onCommentDeleted(Comment comment) {

    }

}
