
package com.umeng.comm.ui.imagepicker.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.umeng.comm.core.sdkmanager.ImageLoaderManager;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.imagepicker.model.PhotoModel;
import com.umeng.comm.ui.imagepicker.polites.GestureImageView;

/**
 * 图片浏览视图
 * 
 */
public class PhotoPreview extends LinearLayout implements OnClickListener {

    private GestureImageView mImageView;
    private OnClickListener mClickListener;

    public PhotoPreview(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(
                ResFinder.getLayout("umeng_comm_imagepicker_view_photopreview"), this, true);

        mImageView = (GestureImageView) findViewById(ResFinder.getId("umeng_comm_iv_content_vpp"));
        mImageView.setOnClickListener(this);
    }

    public PhotoPreview(Context context, AttributeSet attrs, int defStyle) {
        this(context);
    }

    public PhotoPreview(Context context, AttributeSet attrs) {
        this(context);
    }

    public void loadImage(PhotoModel photoModel) {
        loadImage("file://" + photoModel.getOriginalPath());
    }

    private void loadImage(String path) {
        ImageLoaderManager.getInstance().getCurrentSDK().displayImage(path, mImageView);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        this.mClickListener = l;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == ResFinder.getId("umeng_comm_iv_content_vpp")
                && mClickListener != null) {
            mClickListener.onClick(mImageView);
        }
    }

}
