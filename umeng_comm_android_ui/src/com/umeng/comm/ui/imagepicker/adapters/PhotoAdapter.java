
package com.umeng.comm.ui.imagepicker.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.umeng.comm.ui.imagepicker.model.PhotoModel;
import com.umeng.comm.ui.imagepicker.widgets.PhotoItemViewHolder;
import com.umeng.comm.ui.imagepicker.widgets.PhotoItemViewHolder.onItemClickListener;
import com.umeng.comm.ui.imagepicker.widgets.PhotoItemViewHolder.onPhotoItemCheckedListener;

/**
 * 图片选择Adapter
 */
public class PhotoAdapter extends MBaseAdapter<PhotoModel> {
    private onPhotoItemCheckedListener listener;
    private onItemClickListener mClickListener;

    private PhotoAdapter(Context context, ArrayList<PhotoModel> models) {
        super(context, models);
    }

    public PhotoAdapter(Context context, ArrayList<PhotoModel> models, int screenWidth,
            onPhotoItemCheckedListener listener, onItemClickListener clickListener) {
        this(context, models);
        this.listener = listener;
        this.mClickListener = clickListener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        PhotoItemViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new PhotoItemViewHolder(mContext, parent, listener);
            convertView = viewHolder.getItemView();
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (PhotoItemViewHolder) convertView.getTag();
        }
        final PhotoModel photoModel = getItem(position);
        viewHolder.setPhotoModel(photoModel);
        viewHolder.setSelected(photoModel.isChecked());
        viewHolder.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mClickListener != null) {
                    mClickListener.onItemClick(position);
                }
            }
        });
        return convertView;
    }

    public void setOnItemClickListener(onItemClickListener clickListener) {
        this.mClickListener = clickListener;
    }
}
