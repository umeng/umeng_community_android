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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;

import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.adapters.viewholders.ImageSelectedViewHolder;

/**
 * 发布消息时选中的图片预览的GridView适配器, 用户可以删除选中的图片.
 */
public class ImageSelectedAdapter extends CommonAdapter<String, ImageSelectedViewHolder> {

    // /**
    // * 当移除某张图片的时候，更新发送消息界面的图片Gridview信息
    // */
    // private FetchListener<List<String>> mListener;


    public ImageSelectedAdapter(Context context) {
        super(context);
        // this.mListener = listener;
    }

    @Override
    protected ImageSelectedViewHolder createViewHolder() {
        return new ImageSelectedViewHolder();
    }

    /**
     * 显示删除某项的Dialog </br>
     * 
     * @param adapter
     * @param position
     */
    protected void showDeleteItemDialog(final int position) {
        String msg = ResFinder.getString("umeng_comm_delete_photo");
        String confirmText = ResFinder.getString("umeng_comm_text_confirm");
        String cancelText = ResFinder.getString("umeng_comm_text_cancel");

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(msg).setPositiveButton(confirmText,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getDataSource().remove(position);
                        // 添加add图标
                        if (!mDataSet.contains(Constants.ADD_IMAGE_PATH_SAMPLE)) {
                            mDataSet.add(Constants.ADD_IMAGE_PATH_SAMPLE);
                        }
                        notifyDataSetChanged();
                    }
                });

        builder.setNegativeButton(cancelText, null);
        builder.create().show();
    }

    @Override
    protected void setItemData(final int position, ImageSelectedViewHolder holder, View rootView) {
        final String path = getItem(position);
        holder.imageView.setTag(path);

        if (!path.equals(Constants.ADD_IMAGE_PATH_SAMPLE)) {
            this.displayImage(holder, position, path);
        } else {
            Drawable drawable = ResFinder.getDrawable("umeng_comm_add_image");
            this.showDeleteImage(holder, drawable);
        }
    }

    private void displayImage(ImageSelectedViewHolder viewHolder, final int position, String path) {
        viewHolder.deleteImageView.setVisibility(View.VISIBLE);
        // 加载图片
        viewHolder.imageView.setImageUrl(path);
        // 删除按钮
        viewHolder.deleteImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showDeleteItemDialog(position);
            }
        });
    }

    private void showDeleteImage(ImageSelectedViewHolder viewHolder, Drawable drawable) {
        viewHolder.imageView.setImageDrawable(drawable);
        viewHolder.deleteImageView.setVisibility(View.GONE);
    }

}
