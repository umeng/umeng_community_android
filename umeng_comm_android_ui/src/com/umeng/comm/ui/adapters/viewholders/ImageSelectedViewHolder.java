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

import android.view.View;
import android.widget.ImageView;

import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.widgets.SquareImageView;

/**
 * 发布Feed时已选中图片的GridView ViewHolder
 */
public class ImageSelectedViewHolder extends ViewHolder {

    public SquareImageView imageView;// 选中的图片
    public ImageView deleteImageView;// 选中图片右上方的删除图标

    @Override
    protected void initWidgets() {
        int selectImageViewResId = ResFinder.getId("umeng_comm_image_selected");
        int deleteImageViewResId = ResFinder.getId("umeng_comm_image_delete");

        imageView = findViewById(selectImageViewResId);
        deleteImageView = findViewById(deleteImageViewResId);
        deleteImageView.setVisibility(View.VISIBLE);
    }

    @Override
    protected int getItemLayout() {
        return ResFinder.getLayout("umeng_comm_image_selected_item");
    }

}
