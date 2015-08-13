
package com.umeng.comm.ui.imagepicker.widgets;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.umeng.comm.core.imageloader.ImgDisplayOption;
import com.umeng.comm.core.sdkmanager.ImageLoaderManager;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.imagepicker.model.PhotoModel;

/**
 * 图片Item View
 */
public class PhotoItemView extends LinearLayout implements OnCheckedChangeListener,
        OnLongClickListener {

    private ImageView mPhotoImageView;
    private CheckBox mPhotoCheckBox;
    private onPhotoItemCheckedListener listener;
    private PhotoModel mPhotoModel;
    private boolean isCheckAll;
    private onItemClickListener mItemClickListener;
    private int position;

    private PhotoItemView(Context context) {
        super(context);
    }

    public PhotoItemView(Context context, onPhotoItemCheckedListener listener) {
        this(context);
        LayoutInflater.from(context).inflate(
                ResFinder.getLayout("umeng_comm_imagepicker_photo_item"), this,
                true);
        this.listener = listener;

        setOnLongClickListener(this);

        mPhotoImageView = (ImageView) findViewById(ResFinder.getId("umeng_comm_iv_photo_lpsi"));
        mPhotoCheckBox = (CheckBox) findViewById(ResFinder.getId("umeng_comm_cb_photo_lpsi"));

        mPhotoCheckBox.setOnCheckedChangeListener(this); // CheckBox的监听器
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!isCheckAll) {
            listener.onCheckedChanged(this, mPhotoModel, isChecked);
        }
    }

    public void updatePhotoItemState(boolean isChecked) {
        if (isChecked) {
            setDrawingable();
            mPhotoImageView.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        } else {
            mPhotoImageView.clearColorFilter();
        }
        mPhotoCheckBox.setChecked(isChecked);
        mPhotoModel.setChecked(isChecked);
    }

    public void setImageDrawable(final PhotoModel photo) {
        this.mPhotoModel = photo;
        ImgDisplayOption option = ImgDisplayOption.getCommonDisplayOption() ;
        option.requestOrigin = false ;
        ImageLoaderManager.getInstance().getCurrentSDK().displayImage(
                "file://" + photo.getOriginalPath(), mPhotoImageView, option);
    }

    private void setDrawingable() {
        mPhotoImageView.setDrawingCacheEnabled(true);
        mPhotoImageView.buildDrawingCache();
    }

    @Override
    public void setSelected(boolean selected) {
        if (mPhotoModel == null) {
            return;
        }
        isCheckAll = true;
        mPhotoCheckBox.setChecked(selected);
        isCheckAll = false;
    }

    public void setOnClickListener(onItemClickListener l, int position) {
        this.mItemClickListener = l;
        this.position = position;
    }

    public static interface onPhotoItemCheckedListener {
        public void onCheckedChanged(PhotoItemView photoItem, PhotoModel photoModel,
                boolean isChecked);
    }

    public interface onItemClickListener {
        public void onItemClick(int position);
    }

    @Override
    public boolean onLongClick(View v) {
        if (mItemClickListener != null)
            mItemClickListener.onItemClick(position);
        return true;
    }

}
