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
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.ImageView.ScaleType;

import com.umeng.comm.core.beans.ImageItem;
import com.umeng.comm.core.imageloader.ImgDisplayOption;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ResFinder.ResType;
import com.umeng.comm.ui.adapters.viewholders.NullViewParser;
import com.umeng.comm.ui.widgets.SquareImageView;

/**
 * 消息流九宫格图片的Adapter
 */
public class FeedImageAdapter extends CommonAdapter<ImageItem, NullViewParser> {

    private ImgDisplayOption mDisplayOption = new ImgDisplayOption();

    /**
     * 构造函数
     * 
     * @param context
     * @param data
     */
    public FeedImageAdapter(Context context) {
        super(context);
        initDisplayOption();
    }

    @Override
    protected NullViewParser createViewHolder() {
        return new NullViewParser();
    }

    /**
     * 初始化显示的图片跟配置</br>
     */
    private void initDisplayOption() {
        mDisplayOption.mLoadingResId = ResFinder.getResourceId(ResType.DRAWABLE,
                "umeng_comm_not_found");
        mDisplayOption.mLoadFailedResId = ResFinder.getResourceId(ResType.DRAWABLE,
                "umeng_comm_not_found");
    }

    @Override
    public int getCount() {
        int count = super.getCount();
        return count > 9 ? 9 : count;
    }

    // 此处不用ViewParser的方式，主要是只有ImageView且都需要设置Tag（ViewHolder跟图片的url地址，导致冲突）
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        SquareImageView imageView;
        if (view == null) {
            LayoutParams mImageViewLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.MATCH_PARENT);
            imageView = new SquareImageView(mContext);
            imageView.setScaleType(ScaleType.CENTER_CROP);
            imageView.setLayoutParams(mImageViewLayoutParams);
        } else {
            imageView = (SquareImageView) view;
        }
        imageView.setImageUrl(getItem(position).thumbnail, mDisplayOption);
        return imageView;
    }

    @Override
    protected void setItemData(int position, NullViewParser holder, View rootView) {
    }
}
