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
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemClickListener;

import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.ui.utils.FontUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView的Adapter基类
 * 
 * @param <T> 数据类型
 * @param <VH> ViewHolder类型
 */
public abstract class BaseRecyclerAdapter<T, VH extends ViewHolder> extends Adapter<VH> {

    protected static final int HEADER_TYPE = 0;
    protected static final int ITEM_TYPE = 1;
    protected static final int FOOTER_TYPE = 2;

    protected Context mContext;
    protected final List<T> mDataSet = new ArrayList<T>();
    OnItemClickListener mItemClickListener;

    public BaseRecyclerAdapter(Context context) {
        mContext = context;
    }

    @Override
    public final void onBindViewHolder(VH viewHolder, int position) {
        final T item = getItem(position);
        bindItemData(viewHolder, item, position);
        setupOnItemClick(viewHolder, position);
        FontUtils.changeTypeface(viewHolder.itemView);
    }

    protected abstract void bindItemData(VH viewHolder, T data, int position);

    protected void setupOnItemClick(final VH viewHolder, final int position) {
        if (mItemClickListener != null) {
            viewHolder.itemView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mItemClickListener.onItemClick(null, viewHolder.itemView, position, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public T getItem(int position) {
        position = Math.max(0, position - mHeaderCount);
        return mDataSet.get(position);
    }

    public void updateListViewData(List<T> lists) {
        mDataSet.clear();
        if (!CommonUtils.isListEmpty(lists)) {
            mDataSet.addAll(lists);
            notifyDataSetChanged();
        }
    }

    public void addData(T t) {
        if (t != null) {
            mDataSet.add(t);
            notifyDataSetChanged();
        }
    }

    public void addData(List<T> newItems) {
        if (newItems != null) {
            mDataSet.addAll(newItems);
            notifyDataSetChanged();
        }
    }

    public void addToFirst(T item) {
        mDataSet.add(0, item);
        notifyDataSetChanged();
    }

    public void addToFirst(List<T> newItems) {
        mDataSet.addAll(0, newItems);
        notifyDataSetChanged();
    }

    public List<T> getDataSource() {
        return mDataSet;
    }

    int mHeaderCount = 0;
    int mFooterCount = 0;

    public void setOnItemClickListener(OnItemClickListener l) {
        this.mItemClickListener = l;
    }

    View mHeaderView;

    public void addHeaderView(View headerView) {
        mHeaderView = headerView;
        mHeaderCount++;
    }

    public void setHeaderCount(int count) {
        mHeaderCount = count;
    }

    public int getHeaderCount() {
        return mHeaderCount;
    }

    public int getFooterCount() {
        return mFooterCount;
    }

    public void setFooterCount(int mFooterCount) {
        this.mFooterCount = mFooterCount;
    }
}
