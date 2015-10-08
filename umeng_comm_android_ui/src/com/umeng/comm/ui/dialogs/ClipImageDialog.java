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

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.constants.ErrorCode;
import com.umeng.comm.core.db.ctrl.impl.DatabaseAPI;
import com.umeng.comm.core.imageloader.utils.BitmapDecoder;
import com.umeng.comm.core.impl.CommunityFactory;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.PortraitUploadResponse;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.Log;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.utils.BroadcastUtils;
import com.umeng.comm.ui.widgets.ClipImageLayout;

/**
 * 图片裁剪Dialog
 */
public class ClipImageDialog extends Dialog implements OnClickListener {

    private ClipImageLayout mClipImageLayout;
    private Uri mImgUri = null;
    private OnClickSaveListener mSaveListener;

    /**
     * 构造函数
     * 
     * @param context
     */
    public ClipImageDialog(Context context, Uri uri, int theme) {
        super(context, theme);
        this.mImgUri = uri;
        init();
    }

    /**
     * 初始化相关资源跟设置点击事件</br>
     */
    private void init() {
        int layout = ResFinder.getLayout("umeng_comm_pic_clip");
        int imageLayoutId = ResFinder.getId("umeng_comm_clip_layout");
        setContentView(layout);
        mClipImageLayout = (ClipImageLayout) findViewById(imageLayoutId);
        int backResId = ResFinder.getId("umeng_comm_clip_back");
        int saveResId = ResFinder.getId("umeng_comm_clip_save");
        findViewById(backResId).setOnClickListener(this);
        findViewById(saveResId).setOnClickListener(this);
    }

    @Override
    public void show() {
        super.show();
        loadImage();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        int backResId = ResFinder.getId("umeng_comm_clip_back");
        int saveResId = ResFinder.getId("umeng_comm_clip_save");
        if (id == backResId) {
            dismiss();
        } else if (id == saveResId) {
            clipImage();
        }
    }

    /**
     * 剪切图片并执行回调</br>
     */
    private void clipImage() {
        Bitmap bitmap = mClipImageLayout.clip();
        mSaveListener.onClickSave(bitmap);
        // 更新用户头像
        updateUserPortrait(bitmap);
    }

    /**
     * 更新用户头像
     */
    private void updateUserPortrait(final Bitmap bmp) {

        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage(ResFinder.getString("umeng_comm_update_user_icon"));
        progressDialog.setCanceledOnTouchOutside(false);

        CommunityFactory.getCommSDK(getContext()).updateUserProtrait(bmp,
                new SimpleFetchListener<PortraitUploadResponse>() {

                    @Override
                    public void onStart() {
                        progressDialog.show();
                    }

                    @Override
                    public void onComplete(PortraitUploadResponse response) {
                        progressDialog.dismiss();
                        if (response != null && response.errCode == ErrorCode.NO_ERROR) {
                            Log.d("", "头像更新成功 : " + response.mJsonObject.toString());
                            CommUser user = CommConfig.getConfig().loginedUser;
                            user.iconUrl = response.mIconUrl;

                            Log.d("", "#### 登录用户的头像 : "
                                    + CommConfig.getConfig().loginedUser.iconUrl);
                            // 同步到数据库中
                            syncUserIconUrlToDB(user);
                            CommonUtils.saveLoginUserInfo(getContext(), user);
                            BroadcastUtils.sendUserUpdateBroadcast(getContext(), user);
                            dismiss();
                        } else {
                            ToastMsg.showShortMsgByResName("umeng_comm_update_icon_failed");
                        }
                    }

                });
    }

    private void syncUserIconUrlToDB(CommUser user) {
        DatabaseAPI.getInstance().getUserDBAPI().saveUserInfoToDB(user);
    }

    /**
     * 设置点击保存时的回调</br>
     * 
     * @param listener
     */
    public void setOnClickSaveListener(OnClickSaveListener listener) {
        this.mSaveListener = listener;
    }

    /**
     * 根据uri加载图片</br>
     */
    private void loadImage() {
        BitmapDecoder decoder = getBitmapDecoder();
        mClipImageLayout.setImageDrawable(decoder.decodeBitmap(Constants.SCREEN_WIDTH,
                Constants.SCREEN_HEIGHT));
    }

    private BitmapDecoder getBitmapDecoder() {
        return new BitmapDecoder() {

            @Override
            public Bitmap decodeBitmapWithOption(Options options) {
                final ContentResolver contentResolver = getContext().getContentResolver();
                try {
                    return BitmapFactory.decodeStream(contentResolver
                            .openInputStream(mImgUri), null, options);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    /**
     * 点击保存按钮时的回调
     */
    public interface OnClickSaveListener {
        public void onClickSave(Bitmap bitmap);
    }

}
