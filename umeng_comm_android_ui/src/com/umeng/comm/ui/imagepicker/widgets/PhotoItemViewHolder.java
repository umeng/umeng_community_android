
package com.umeng.comm.ui.imagepicker.widgets;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;

import com.umeng.comm.core.imageloader.ImgDisplayOption;
import com.umeng.comm.core.sdkmanager.ImageLoaderManager;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.imagepicker.model.PhotoModel;

/**
 * 图片Item ViewHolder
 */
public class PhotoItemViewHolder implements OnCheckedChangeListener,
        OnLongClickListener {

    private ImageView mPhotoImageView;
    private CheckBox mPhotoCheckBox;
    private onPhotoItemCheckedListener listener;
    private PhotoModel mPhotoModel;
    private boolean isCheckAll;
    private onItemClickListener mItemClickListener;
    private int position;

    View mRootView;

    Context mContext;

    private PhotoItemViewHolder(Context context) {
        mContext = context;
    }

    public PhotoItemViewHolder(Context context, ViewGroup parent,
            onPhotoItemCheckedListener listener) {
        this(context);
        mRootView = LayoutInflater.from(context).inflate(
                ResFinder.getLayout("umeng_comm_imagepicker_photo_item"), parent,
                false);
        this.listener = listener;
        mRootView.setDrawingCacheEnabled(false);

        mRootView.setOnLongClickListener(this);

        mPhotoImageView = (ImageView) mRootView.findViewById(ResFinder
                .getId("umeng_comm_iv_photo_lpsi"));
        mPhotoCheckBox = (CheckBox) mRootView.findViewById(ResFinder
                .getId("umeng_comm_cb_photo_lpsi"));

        mPhotoCheckBox.setOnCheckedChangeListener(this); // CheckBox的监听器
    }

    public View getItemView() {
        return mRootView;
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

    public void setPhotoModel(final PhotoModel photo) {
        this.mPhotoModel = photo;
        ImgDisplayOption option = ImgDisplayOption.getCommonDisplayOption();
        option.requestOrigin = false;
        // TODO : 性能优化
        // Debug.startMethodTracing(Environment.getExternalStorageDirectory().getPath()
        // + "/community_trace");
        ImageLoaderManager.getInstance().getCurrentSDK().displayImage(
                "file://" + photo.getOriginalPath(), mPhotoImageView, option);
        // Debug.stopMethodTracing();
    }

    public void setOnClickListener(OnClickListener clickListener) {
        if (mRootView != null) {
            mRootView.setOnClickListener(clickListener);
        }
    }

    private void setDrawingable() {
        mPhotoImageView.setDrawingCacheEnabled(true);
        mPhotoImageView.buildDrawingCache();
    }

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
        public void onCheckedChanged(PhotoItemViewHolder photoItem, PhotoModel photoModel,
                boolean isChecked);
    }

    public interface onItemClickListener {
        public void onItemClick(int position);
    }

    @Override
    public boolean onLongClick(View v) {
        if (mItemClickListener != null) {
            mItemClickListener.onItemClick(position);
        }

        return true;
    }

}
