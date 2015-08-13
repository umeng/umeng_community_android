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
import android.content.Context;
import android.location.Location;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.umeng.comm.core.CommunitySDK;
import com.umeng.comm.core.impl.CommunityFactory;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.adapters.PickerAdapter;
import com.umeng.comm.ui.widgets.RefreshLayout.OnLoadListener;
import com.umeng.comm.ui.widgets.RefreshLvLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 发帖时的@好友和位置选择Dialog的基类
 */
public abstract class PickerDialog<E> extends Dialog implements OnClickListener {

    /**
     * 好友或者地理位置的ListView
     */
    protected ListView mListView;

    /**
     * 标题视图
     */
    protected TextView mTitleTextView;

    /**
     * 地理位置
     */
    protected Location mLocation = null;

    /**
     * 数据选取监听器
     */
    protected FetchListener<E> mDataListener;

    protected PickerAdapter<E> mAdapter = null;

    /**
     * Dialog的Root View
     */
    protected View mRootView;

    /**
     * 已选中的位置索引
     */
    protected List<Integer> mSelectedIndex = new ArrayList<Integer>();

    protected RefreshLvLayout mRefreshLvLayout;
    protected CommunitySDK mSdkImpl;
    protected E mSelectedItem;

    /**
     * @param context
     */
    public PickerDialog(Context context) {
        this(context, 0);
    }

    public PickerDialog(Context context, int theme) {
        super(context, theme);
        mSdkImpl = CommunityFactory.getCommSDK(context);
    }

    /**
     * 设置数据获取监听器, 即在窗口选择了某项数据后回调给调用者
     * 
     * @param listener
     */
    public void setDataListener(FetchListener<E> listener) {
        mDataListener = listener;
    }

    /**
     * Parse the dialog's content view and fill datas, and so on.
     * 
     * @param mContext the context
     * @return the dialog's content view
     */
    protected View createContentView() {
        int layout = ResFinder.getLayout("umeng_comm_at_friends_layout");
        int listViewResId = ResFinder.getId("umeng_comm_friend_listview");
        int searchTvResId = ResFinder.getId("search_tv");
        int searchlasteTvResId = ResFinder.getId("search_lastes_btn");
        int backBtnResId = ResFinder.getId("search_back_btn");
        int okBtnResId = ResFinder.getId("search_ok_btn");
        int refreshLayoutResId = ResFinder.getId("umeng_comm_at_friend_listview");

        mRootView = LayoutInflater.from(getContext()).inflate(
                layout, null, false);

        //
        mRefreshLvLayout = (RefreshLvLayout) mRootView
                .findViewById(refreshLayoutResId);
        mRefreshLvLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                loadDataFromServer();
            }
        });

        mRefreshLvLayout.setOnLoadListener(new OnLoadListener() {

            @Override
            public void onLoad() {
                loadMore();
            }
        });

        // listview
        // mListView = (ListView) mRootView
        // .findViewById(listViewResId);
        mListView = mRefreshLvLayout.findRefreshViewById(listViewResId);
        // mListView.requestFocus();
        // 文本
        mTitleTextView = (TextView) mRootView.findViewById(searchTvResId);
        mRootView.findViewById(searchlasteTvResId).setVisibility(View.GONE);
        setupAdater();
        setupLvOnItemClickListener();
        initSearchEdit();

        mRootView.findViewById(backBtnResId).setOnClickListener(this);
        mRootView.findViewById(okBtnResId).setOnClickListener(this);
        mRootView.findViewById(okBtnResId).setVisibility(View.GONE);

        return mRootView;
    }

    /**
     * 从server端加载数据</br>
     */
    public abstract void loadDataFromServer();

    /**
     * 从server加载更多数据</br>
     */
    public abstract void loadMore();

    /**
     * 
     */
    private void initSearchEdit() {
        int searchEditResId = ResFinder.getId("umeng_comm_search_edittext");
        mRootView.findViewById(
                searchEditResId).setVisibility(View.GONE);
    }

    protected void pickItemAtPosition(int position) {
        if (mDataListener != null) {
            mSelectedItem = mAdapter.getItem(position);
            mDataListener.onComplete(mSelectedItem);
        }

        this.dismiss();
    }

    /**
     * 设置适配器
     */
    protected abstract void setupAdater();

    /**
     * 设置ListView的item 点击事件处理
     */
    protected abstract void setupLvOnItemClickListener();

    /*
     * (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        if (mDataListener != null) {
            mDataListener.onComplete(mSelectedItem);
        }
        this.dismiss();
    }

}
