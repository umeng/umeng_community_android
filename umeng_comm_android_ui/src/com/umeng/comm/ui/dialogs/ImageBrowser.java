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
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.widget.TextView;

import com.umeng.comm.core.beans.ImageItem;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.adapters.ImagePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片浏览器,通过ViewPager来进行图片浏览
 */
public class ImageBrowser extends Dialog {

    ProgressDialog mLoadingDialog;
    private ViewPager mViewPager;
    private ImagePagerAdapter mAdapter;
    private List<ImageItem> mImageList;
    private TextView mImagePosTextView;
    private static final String DIVIDER = "/";
    private boolean mPreView = false;

    /**
     * @param context
     */
    public ImageBrowser(Context context) {
        this(context, false);
    }

    public ImageBrowser(Context context, boolean isPreView) {
        super(context, android.R.style.Theme_Black_NoTitleBar);
        mPreView = isPreView;
        initContentView();
        int dialogStyle = ResFinder.getStyle("umeng_comm_dialog_wrap_content");
        mLoadingDialog = new ProgressDialog(context, dialogStyle);
        getWindow().setWindowAnimations(ResFinder.getStyle("umeng_comm_image_browser"));
    }

    /**
     * 初始化内容视图
     */
    private void initContentView() {
        setContentView(ResFinder.getLayout("umeng_comm_img_browser_layout"));
        mImagePosTextView = (TextView) findViewById(ResFinder.getId("umeng_comm_current_pos"));
        initImageViewPager();
    }

    private void initImageViewPager() {
        mViewPager = (ViewPager) findViewById(ResFinder.getId("viewPager"));
        mAdapter = new ImagePagerAdapter();
        mAdapter.setOnDismissListener(new android.content.DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                dismiss();
            }
        });
        mAdapter.isPreView = mPreView;
        mViewPager.setOffscreenPageLimit(3); // 最多cache 3屏数据
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int page) {
                mImagePosTextView.setText((page + 1) + DIVIDER + mImageList.size());
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
    }

    /**
     * @param url 图片url地址
     */
    public void setImageList(List<ImageItem> images, int curPos) {
        mImageList = images;
        mAdapter.addImagePaths(images);
        mViewPager.setCurrentItem(curPos);
        mImagePosTextView.setText((curPos + 1) + DIVIDER + mImageList.size());
    }

    /**
     * @param url 图片url地址
     */
    public void setImageStringList(List<String> images, int curPos) {
        List<ImageItem> imageItems = new ArrayList<ImageItem>();
        for (int i = 0; i < images.size(); i++) {
            String url = images.get(i);
            imageItems.add(new ImageItem(url, url, url));
        }

        setImageList(imageItems, curPos);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mAdapter.cleanCache();
    }

    public static interface OnDismissListener {
        void onDismiss();
    }

}
