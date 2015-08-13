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

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.imageloader.ImgDisplayOption;
import com.umeng.comm.core.imageloader.UMImageLoader;
import com.umeng.comm.core.sdkmanager.ImageLoaderManager;
import com.umeng.comm.core.utils.DeviceUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.activities.UserInfoActivity;
import com.umeng.comm.ui.adapters.SearchUsersAdapter.SearchUserViewHolder;
import com.umeng.comm.ui.widgets.RoundImageView;

/**
 * 搜索时的用户RecyclerView Adapter
 */
public class SearchUsersAdapter extends BaseRecyclerAdapter<CommUser, SearchUserViewHolder> {

    private LayoutInflater mInflater;
    private UMImageLoader mImageLoader = ImageLoaderManager.getInstance().getCurrentSDK();
    public static final int MAX_SHOW_NUM = 4;
    private Activity mActivity;

    /**
     * @param context
     */
    public SearchUsersAdapter(Activity activity) {
        super(activity);
        mInflater = LayoutInflater.from(activity);
        this.mActivity = activity;
    }

    @Override
    public SearchUserViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int i) {
        View view = mInflater.inflate(ResFinder.getLayout("umeng_comm_relative_user_gallery_item"),
                viewGroup, false);
        int width = computeWidth();
        LayoutParams params = (LayoutParams) view.getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(width, LayoutParams.WRAP_CONTENT);
        } else {
            params.width = width;
        }
        view.setLayoutParams(params);

        final SearchUserViewHolder viewHolder = new SearchUserViewHolder(view);
        viewHolder.mImg = (RoundImageView) view
                .findViewById(ResFinder.getId("umeng_comm_user_icon"));
        viewHolder.mTxt = (TextView) view
                .findViewById(ResFinder.getId("umeng_comm_user_name"));
        return viewHolder;
    }

    /**
     * 根据项数计算每项的宽度</br>
     * 
     * @param childSize
     * @return
     */
    public int computeWidth() {
        return DeviceUtils.getScreenSize(mActivity).x / MAX_SHOW_NUM;
    }

    @Override
    protected void bindItemData(SearchUserViewHolder viewHolder, final CommUser user, int position) {
        ImgDisplayOption option = ImgDisplayOption.getOptionByGender(user.gender);
        if (TextUtils.isEmpty(user.iconUrl)) {
            viewHolder.mImg.setImageResource(option.mLoadingResId);
        } else {
            mImageLoader.displayImage(user.iconUrl, viewHolder.mImg, option);
        }
        viewHolder.mTxt.setText(user.name);

        viewHolder.itemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, UserInfoActivity.class);
                intent.putExtra(Constants.TAG_USER, user);
                mActivity.startActivity(intent);
            }
        });
    }

    public static interface OnItemClickLitener {
        void onItemClick(View view, int position);
    }

    public static class SearchUserViewHolder extends RecyclerView.ViewHolder {

        RoundImageView mImg;
        TextView mTxt;

        public SearchUserViewHolder(View arg0) {
            super(arg0);
        }

    }

}
