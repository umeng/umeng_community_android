
package com.umeng.community.example.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.umeng.comm.core.imageloader.ImgDisplayOption;
import com.umeng.comm.core.imageloader.UMImageLoader;
import com.umeng.community.example.R;

/**
 * 自定义的图片加载器
 */
public class UILImageLoader implements UMImageLoader {

    static ImageLoader mImageLoader = ImageLoader.getInstance();

    DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.umeng_comm_not_found)
            .showImageForEmptyUri(R.drawable.umeng_comm_not_found)
            .showImageOnFail(R.drawable.umeng_comm_not_found)
            .cacheInMemory(true)
            .cacheOnDisc(true)
            .considerExifParams(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    public UILImageLoader(Context context) {
        init(context);
    }

    private void init(Context context) {

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCacheSize(10 * 1024 * 1024)
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs() // Remove for release app
                .build();

        mImageLoader.init(config);
    }

    @Override
    public void displayImage(String urlOrPath, ImageView imageView) {
        this.displayImage(urlOrPath, imageView, null);
    }

    @Override
    public void displayImage(final String urlOrPath, final ImageView imageView,
            final ImgDisplayOption option) {

        mImageLoader.displayImage(urlOrPath, imageView, options);
    }

    @Override
    public void resume() {
        mImageLoader.resume();
    }

    @Override
    public void pause() {
        mImageLoader.pause();
    }

    @Override
    public void reset() {
    }

    @Override
    public void displayImage(String imgUri, ImageView imageView, ImgDisplayOption option,
            ImageLoadingListener listener) {

    }
}
