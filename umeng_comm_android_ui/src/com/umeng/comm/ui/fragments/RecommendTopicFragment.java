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

package com.umeng.comm.ui.fragments;

import java.util.List;

import android.content.DialogInterface.OnDismissListener;
import android.graphics.Color;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.adapters.BackupAdapter;
import com.umeng.comm.ui.adapters.RecommendTopicAdapter;
import com.umeng.comm.ui.adapters.RecommendTopicAdapter.FollowListener;
import com.umeng.comm.ui.mvpview.MvpRecommendTopicView;
import com.umeng.comm.ui.presenter.impl.RecommendTopicPresenter;
import com.umeng.comm.ui.utils.FontUtils;
import com.umeng.comm.ui.widgets.BaseView;
import com.umeng.comm.ui.widgets.RefreshLayout.OnLoadListener;
import com.umeng.comm.ui.widgets.RefreshLvLayout;

/**
 * 推荐的话题
 */
public class RecommendTopicFragment extends BaseFragment<List<Topic>, RecommendTopicPresenter>
        implements android.view.View.OnClickListener, MvpRecommendTopicView {

    protected BackupAdapter<Topic, ?> mAdapter;
    protected ListView mTopicListView;
    protected RefreshLvLayout mRefreshLvLayout;
    protected boolean fromRecommedTopic = true;
    private boolean mSaveButtonVisiable = true;
    private OnDismissListener mOnDismissListener;
    protected BaseView mBaseView;

    protected RecommendTopicFragment() {
    }

    public static RecommendTopicFragment newRecommendTopicFragment() {
        return new RecommendTopicFragment();
    }

    @Override
    protected int getFragmentLayout() {
        return ResFinder.getLayout("umeng_comm_topic_recommend");
    }
    
    @Override
    protected void initWidgets() {
        FontUtils.changeTypeface(mRootView);
        initRefreshView(mRootView);
        initTitleView(mRootView);
    }
    
    @Override
    protected RecommendTopicPresenter createPresenters() {
        return new RecommendTopicPresenter(this);
    }


    protected void initTitleView(View rootView) {
        Button button = (Button) rootView.findViewById(ResFinder.getId("umeng_comm_save_bt"));
        button.setOnClickListener(this);
        button.setText(ResFinder.getString("umeng_comm_skip"));
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        button.setTextColor(ResFinder.getColor("umeng_comm_skip_text_color"));
        if (!mSaveButtonVisiable) {
            button.setVisibility(View.GONE);
            rootView.findViewById(ResFinder.getId("umeng_comm_setting_back")).setOnClickListener(
                    this);
        } else {
            rootView.findViewById(ResFinder.getId("umeng_comm_setting_back")).setVisibility(
                    View.GONE);
        }
        TextView textView = (TextView) rootView.findViewById(ResFinder
                .getId("umeng_comm_setting_title"));
        textView.setText(ResFinder.getString("umeng_comm_recommend_topic"));
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        rootView.findViewById(ResFinder.getId("umeng_comm_title_bar_root"))
                .setBackgroundColor(Color.WHITE);
    }

    /**
     * 设置保存按钮魏不可见。在设置页面显示推荐话题时，不需要显示</br>
     */
    public void setSaveButtonInVisiable() {
        // findViewById(ResFinder.getId("umeng_comm_save_bt")).setVisibility(View.INVISIBLE);
        mSaveButtonVisiable = false;
    }

    /**
     * 初始化刷新相关的view跟事件</br>
     * 
     * @param rootView
     */
    protected void initRefreshView(View rootView) {
        int refreshResId = ResFinder.getId("umeng_comm_topic_refersh");
        mRefreshLvLayout = (RefreshLvLayout) rootView.findViewById(refreshResId);

        // 推荐用户页面无加载更多跟下拉刷新
        if (fromRecommedTopic) {
            mRefreshLvLayout.setOnRefreshListener(new OnRefreshListener() {

                @Override
                public void onRefresh() {
                    mPresenter.loadDataFromServer();
                }
            });
            mRefreshLvLayout.setOnLoadListener(new OnLoadListener() {
                @Override
                public void onLoad() {
                    mPresenter.loadMoreData();
                }
            });
        }

        int listViewResId = ResFinder.getId("umeng_comm_topic_listview");
        mTopicListView = mRefreshLvLayout.findRefreshViewById(listViewResId);
        initAdapter();
        if (!mSaveButtonVisiable) {
            // 目前推荐话题不需要刷新跟加载更多，因此暂时设置不可用
//            mRefreshLvLayout.setEnabled(false);
        } else {
            mRefreshLvLayout.setDefaultFooterView();
        }

        mBaseView = (BaseView) rootView.findViewById(ResFinder.getId("umeng_comm_baseview"));
        mBaseView.setEmptyViewText(ResFinder.getString("umeng_comm_no_recommend_topic"));
    }

    protected void initAdapter() {
        RecommendTopicAdapter adapter = new RecommendTopicAdapter(getActivity());
        adapter.setFromFindPage(!mSaveButtonVisiable);
        mAdapter = adapter;
        adapter.setFollowListener(new FollowListener<Topic>() {

            @Override
            public void onFollowOrUnFollow(Topic topic, ToggleButton toggleButton,
                    boolean isFollow) {
                if (isFollow) {
                    mPresenter.followTopic(topic, toggleButton);
                } else {
                    mPresenter.cancelFollowTopic(topic, toggleButton);
                }
            }
        });
        mTopicListView.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == ResFinder.getId("umeng_comm_save_bt")
                || id == ResFinder.getId("umeng_comm_setting_back")) {
            mOnDismissListener.onDismiss(null);
        }
    }

    public void setOnDismissListener(OnDismissListener listener) {
        this.mOnDismissListener = listener;
    }

    @Override
    public List<Topic> getBindDataSource() {
        return mAdapter.getDataSource();
    }

    @Override
    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRefreshStart() {
        mRefreshLvLayout.setRefreshing(true);
    }

    @Override
    public void onRefreshEnd() {
        onRefreshEndNoOP();
        if (mAdapter.getCount() == 0) {
            mBaseView.showEmptyView();
        } else {
            mBaseView.hideEmptyView();
        }
    }

    @Override
    public void onRefreshEndNoOP() {
        mRefreshLvLayout.setRefreshing(false);
        mRefreshLvLayout.setLoading(false);
    }

}
