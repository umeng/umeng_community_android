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

import android.content.DialogInterface.OnDismissListener;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.umeng.comm.core.beans.ImageItem;
import com.umeng.comm.core.imageloader.ImgDisplayOption;
import com.umeng.comm.core.imageloader.LocalImageLoader;
import com.umeng.comm.core.imageloader.UMImageLoader;
import com.umeng.comm.core.imageloader.UMImageLoader.ImageLoadingListener;
import com.umeng.comm.core.imageloader.cache.ImageCache;
import com.umeng.comm.core.imageloader.utils.Md5Helper;
import com.umeng.comm.core.sdkmanager.ImageLoaderManager;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.presenter.impl.ImageBrowserPresenter;
import com.umeng.comm.ui.widgets.ScaleImageView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * feed图片浏览的Adapter
 */
public class ImagePagerAdapter extends PagerAdapter {

    private final List<ImageItem> mPaths = new ArrayList<ImageItem>();
    UMImageLoader mImageLoader;
    public boolean isPreView = false;// 是否是预览
    private OnDismissListener mListener;
    ImageBrowserPresenter mPresenter;
    /**
     * 缓存的图片path MD5值,也就是缓存的key
     */
    private List<String> mCachedImagePath = new LinkedList<String>();

    public ImagePagerAdapter() {
        mImageLoader = ImageLoaderManager.getInstance().getCurrentSDK();
    }

    @Override
    public int getCount() {
        return mPaths.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (mPresenter == null) {
            mPresenter = new ImageBrowserPresenter(container.getContext());
        }
        View view = createView(container, mPaths.get(position));
        container.addView(view, LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        return view;
    }

    /**
     * ' 存储缓存在内存中的图片URL的MD5值列表,便于在Dialog销毁时清空这些缓存
     * 
     * @param originPath
     */
    private void storeImageItemMd5(String originPath) {
        String md5 = Md5Helper.toMD5(originPath);
        mCachedImagePath.add(md5);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    /**
     * 确保创建的View被销毁
     */
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public void addImagePaths(List<ImageItem> paths) {
        mPaths.addAll(paths);
        notifyDataSetChanged();
    }

    private void clearImageCache() {
        for (String md5 : mCachedImagePath) {
            ImageCache.getInstance().removeFromMemory(md5);
        }
    }

    public void cleanCache() {
        mPaths.clear();
        notifyDataSetChanged();
        // 清空缓存
        clearImageCache();
        mImageLoader.reset();
    }

    private View createView(final ViewGroup parent, final ImageItem imageItem) {
        // 根视图布局
        int layoutResId = ResFinder.getLayout("umeng_comm_img_browser_item_layout");
        View rootView = LayoutInflater.from(parent.getContext())
                .inflate(layoutResId, parent, false);
        return setupWidgets(rootView, imageItem);
    }

    private View setupWidgets(View rootView, final ImageItem imageItem) {
        // 图片ImageView
        int imageResId = ResFinder.getId("umeng_comm_imagebrowser_view");
        final ScaleImageView imageView = (ScaleImageView) rootView.findViewById(imageResId);
        imageView.setOndismissListener(mListener);

        final String middleImgUrl = imageItem.middleImageUrl;
        // 保存图片
        View saveView = rootView.findViewById(ResFinder.getId("umeng_comm_save_img_tv"));
        if (isPreView) {
            saveView.setVisibility(View.GONE);
            mImageLoader.displayImage(middleImgUrl, imageView);
            return imageView;
        }

        final ProgressBar progressBar = (ProgressBar) rootView.findViewById(ResFinder
                .getId("umeng_comm_image_progressbar"));

        // 保存图片
        saveView.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mPresenter.saveImage(middleImgUrl);
                    }
                });

        // 加载原图
        rootView.findViewById(ResFinder.getId("umeng_comm_origin_img_tv")).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        displayImage(imageView, progressBar, imageItem, true);
                    }
                });
        // 加载图片
        displayImage(imageView, progressBar, imageItem, false);
        return rootView;
    }

    /**
     * 加载图片,默认加载的是ImageItem中的中等质量的图片,当originImage参数为true时则加载原图.
     * 
     * @param imageView 显示图片的ImageView
     * @param progressBar 进度条
     * @param image 图片的ImageItem
     * @param originImage 是否加载原图
     */
    private void displayImage(ImageView imageView, final ProgressBar progressBar,
            final ImageItem image, boolean originImage) {
        String imageUrl = originImage ? image.originImageUrl : image.middleImageUrl;
        // 存储缓存在内存中的图片URL的MD5值列表,便于
        storeImageItemMd5(imageUrl);
        // 设置原图
        Bitmap bitmap = LocalImageLoader.getInstance().loadBitmap(imageUrl, getSize(imageView));
        ImgDisplayOption option = null;
        if (bitmap == null) {
            option = ImgDisplayOption.getCommonDisplayOption();
        } else {
            imageView.setImageBitmap(bitmap);
            option = new ImgDisplayOption();
        }
        // 显示时不缩放图片
        option.requestOrigin = true;
        mImageLoader.displayImage(imageUrl, imageView, option,
                new ImageLoadingListener() {

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        // 显示加载的Dialog
                        progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view) {

                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        // 关闭显示的Dialog
                        progressBar.setVisibility(View.GONE);
                        if (view == null) {
                            return;
                        }
                        ScaleImageView imageView = (ScaleImageView) view;
                        if (isUriEqualsWithImageViewTag(imageView.getTag(), imageUri)
                                && loadedImage != null) {
                            imageView.setImageBitmap(loadedImage);
                            imageView.updateScale();
                        }
                    }
                });
    }

    /**
     * 根据image设置宽高。如果是wrap_content,match_parent则返回宽高250</br>
     * 
     * @param imageView
     * @return
     */
    private Point getSize(ImageView imageView) {
        Point size = new Point();
        if (imageView.getWidth() > 0) {
            size.x = imageView.getWidth();
            size.y = imageView.getHeight();
        } else {
            size.x = size.y = 250;
        }
        return size;
    }

    private boolean isUriEqualsWithImageViewTag(Object tag, String url) {
        return tag != null && !TextUtils.isEmpty(url) && tag.equals(url);
    }

    public void setOnDismissListener(OnDismissListener listener) {
        mListener = listener;
    }
}
