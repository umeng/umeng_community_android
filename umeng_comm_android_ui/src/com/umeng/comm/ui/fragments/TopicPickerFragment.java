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

import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.nets.responses.AbsResponse;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.adapters.TopicPickerAdater;
import com.umeng.comm.ui.adapters.viewholders.FriendItemViewHolder;
import com.umeng.comm.ui.mvpview.MvpRecommendTopicView;
import com.umeng.comm.ui.presenter.impl.TopicFgPresenter;
import com.umeng.comm.ui.widgets.RefreshLayout.OnLoadListener;
import com.umeng.comm.ui.widgets.RefreshLvLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 用户发布feed时的话题选择fragment.
 */
public class TopicPickerFragment extends BaseFragment<List<Topic>, TopicFgPresenter> implements
        MvpRecommendTopicView {

    /**
     * 下来刷新布局
     */
    RefreshLvLayout mRefreshLvLayout;
    /**
     * 显示选择话题的ListView
     */
    private ListView mTopicListView;
    /**
     * 选择话题的适配器
     */
    private TopicPickerAdater mAdapter;
    /**
     * 已经选择的话题
     */
    private List<Topic> mSelectedTopics = new ArrayList<Topic>();
    /**
     * 话题被选中后，点击确认的回调
     */
    private ResultListener<Topic> mTopicListener;

    @Override
    protected int getFragmentLayout() {
        return ResFinder.getLayout("umeng_comm_topic_select");
    }

    @Override
    protected TopicFgPresenter createPresenters() {
        return new TopicFgPresenter(this);
    }

    @Override
    protected void initWidgets() {
        int refreshResId = ResFinder.getId("umeng_comm_topic_lv_layout");
        int topicListViewResId = ResFinder.getId("umeng_comm_topic_listview");

        mRefreshLvLayout = (RefreshLvLayout) mRootView.findViewById(refreshResId);
        mRefreshLvLayout.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                mPresenter.loadDataFromServer();
            }
        });

        mRefreshLvLayout.setOnLoadListener(new OnLoadListener() {
            @Override
            public void onLoad() {
                loadMore();
            }
        });

        mTopicListView = mRefreshLvLayout.findRefreshViewById(topicListViewResId);
        // mTopicListView = (ListView)
        // mRootView.findViewById(topicListViewResId);
        mTopicListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 对于原来的值取反
                setItemSelected(view, position);
            }

        });
        mSelectedTopics.clear();
        mAdapter = new TopicPickerAdater(getActivity());
        mRefreshLvLayout.setAdapter(mAdapter);
    }

    /**
     * 根据url加载更多话题</br>
     */
    private void loadMore() {
        List<Topic> topics = mAdapter.getDataSource();
        if (topics.size() <= 0) {
            mRefreshLvLayout.setLoading(false);
            return;
        }
        mPresenter.loadMoreData();
    }

    /**
     * 某个话题被选中，执行回调，更新TextView的显示
     * 
     * @param isSelected
     */
    private void setItemSelected(View itemView, int position) {
        FriendItemViewHolder viewHolder = (FriendItemViewHolder) itemView.getTag();
        if (viewHolder == null) {
            return;
        }

        Topic topicItem = mAdapter.getItem(position);
        // 对上一次是否含有该项进行取反,即原来没有选中的,那么点击该项以后就变为选选中状态了.
        boolean isChecked = !mSelectedTopics.contains(topicItem);
        // viewHolder.mCheckBox.setChecked(isChecked);
        if (isChecked) {
            mSelectedTopics.add(topicItem);
            mTopicListener.onAdd(topicItem);
        } else {
            mSelectedTopics.remove(topicItem);
            mTopicListener.onRemove(topicItem);
        }

    }

    /**
     * 取消选中的话题
     * 
     * @param topic
     */
    public void uncheckTopic(Topic topic) {
        Iterator<Topic> iterator = mSelectedTopics.iterator();
        while (iterator.hasNext()) {
            Topic item = iterator.next();
            if (item.equals(topic)) {
                item.isFocused = false;
                iterator.remove();
                mAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    /**
     * 设置话题被选择后的回调（点击确认按钮执行该回调）
     * 
     * @param listener
     */
    public void addTopicListener(ResultListener<Topic> listener) {
        mTopicListener = listener;
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
        mRefreshLvLayout.setRefreshing(false);
        mRefreshLvLayout.setLoading(false);
    }

    @Override
    public boolean handlerResponse(AbsResponse<?> response) {
        return super.handlerResponse(response);
    }

    public static interface ResultListener<T> {
        public void onAdd(T t);

        public void onRemove(T t);
    }

}
