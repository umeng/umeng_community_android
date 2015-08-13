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

package com.umeng.comm.ui.presenter.impl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.TopicResponse;
import com.umeng.comm.core.utils.Log;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.mvpview.MvpRecommendTopicView;
import com.umeng.comm.ui.utils.BroadcastUtils;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 
 */
public class TopicFgPresenter extends RecommendTopicPresenter {

    private static final int CHECK_RESULT = 0x01;
    private static final int DELAY = 100;
    private SearchTask mSearchTask = new SearchTask();
    private static boolean isRefreshed = false;

    public TopicFgPresenter(MvpRecommendTopicView recommendTopicView) {
        super(recommendTopicView);
    }

    // 检测消息是否加载完成，防止block
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what != CHECK_RESULT) {
                return;
            }
            boolean done = mSearchTask.isDone();
            if (!done) {
                mHandler.sendEmptyMessageDelayed(CHECK_RESULT, DELAY);
            }
        }
    };

    @Override
    public void attach(Context context) {
        super.attach(context);
        BroadcastUtils.registerTopicBroadcast(context, mReceiver);
    }

    @Override
    public void loadDataFromServer() {
        mCommunitySDK.fetchTopics(new FetchListener<TopicResponse>() {

            @Override
            public void onStart() {
                mRecommendTopicView.onRefreshStart();
            }

            @Override
            public void onComplete(final TopicResponse response) {
                // 根据response进行Toast
                if (mRecommendTopicView.handlerResponse(response)) {
                    mRecommendTopicView.onRefreshEnd();
                    return;
                }

                clearTopicCacheAfterFirstRefresh();
                final List<Topic> results = response.result;
                updateNextPageUrl(results.get(0).nextPage);
                fetchTopicComplete(results, true);
                mRecommendTopicView.onRefreshEnd();
            }
        });
    }

    /**
     * 第一次刷新数据后清空本地数据库缓存
     */
    private void clearTopicCacheAfterFirstRefresh() {
        if (!isRefreshed) {
            isRefreshed = true;
            mDatabaseAPI.getTopicDBAPI().deleteAllTopics();
        }
    }

    @Override
    public void loadDataFromDB() {
        mDatabaseAPI.getTopicDBAPI().loadTopicsFromDB(mDbFetchListener);
    }

    SimpleFetchListener<List<Topic>> mDbFetchListener = new SimpleFetchListener<List<Topic>>() {

        @Override
        public void onComplete(List<Topic> result) {
            if (!isActivityAlive()) {
                return;
            }
            List<Topic> newTopics = setFollowedTag(result);
            // 更新listview数据
            mRecommendTopicView.getBindDataSource().addAll(0, newTopics);
            mRecommendTopicView.notifyDataSetChanged();
        }
    };

    /**
     * 将已经关注的Topic设置标识,数据库中没有保存该字段
     * 
     * @param results
     * @return
     */
    private List<Topic> setFollowedTag(List<Topic> results) {
        // 将过滤后的数据添加到listview中,这些为用户已经关注的话题列表
        List<Topic> newTopics = filterTopics(results);
        for (Topic topic : newTopics) {
            topic.isFocused = true;
        }
        return newTopics;
    }

    /**
     * 移除重复的话题</br>
     * 
     * @param dest 目标话题列表。
     * @return
     */
    private List<Topic> filterTopics(List<Topic> dest) {
        List<Topic> src = mRecommendTopicView.getBindDataSource();
        src.removeAll(dest);
        return dest;
    }

    @Override
    public void loadMoreData() {
        final List<Topic> datas = mRecommendTopicView.getBindDataSource();
        if (datas == null || datas.size() <= 0) {
            mRecommendTopicView.onRefreshEnd();
            return;
        }
        String url = datas.get(datas.size() - 1).nextPage;
        if (TextUtils.isEmpty(url)) {
            // ToastMsg.showShortMsgByResName(mContext,
            // "umeng_comm_load_complete");
            mRecommendTopicView.onRefreshEnd();
            return;
        }
        Log.d("TopicDialog", "加载更多下一页 : " + url);
        mCommunitySDK.fetchNextPageData(url, TopicResponse.class,
                new SimpleFetchListener<TopicResponse>() {

                    @Override
                    public void onComplete(TopicResponse response) {
                        mRecommendTopicView.onRefreshEnd();

                        // 根据response进行Toast
                        if (mRecommendTopicView.handlerResponse(response)) {
                            return;
                        }
                        fetchTopicComplete(response.result, false);
                    }
                });
    }

    /**
     * 话题搜索处理类
     */
    class SearchTask {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future<Void> future = null;

        /**
         * 请求根据关键字搜索话题。对于对个请求，总是以最新的请求为准</br>
         * 
         * @param keyword 话题的关键字
         */
        public void execute(final String keyword) {
            if (TextUtils.isEmpty(keyword)) {
                ToastMsg.showShortMsgByResName(mContext, "umeng_comm_search_keyword_input");
                return;
            }
            // 如果本次搜索未完成，直接取消，搜索新的话题
            cancelTask();
            // 构建Callable任务
            Callable<Void> callable = new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    // 异步搜索
                    mCommunitySDK.searchTopic(keyword,
                            new SimpleFetchListener<TopicResponse>() {

                                @Override
                                public void onComplete(TopicResponse response) {
                                    mRecommendTopicView.onRefreshEnd();
                                    updateResult(response.result);
                                }
                            });
                    return null;
                }
            };
            future = executorService.submit(callable);
            mHandler.sendEmptyMessageDelayed(CHECK_RESULT, DELAY);
        }

        // 取消未完成的搜索任务
        void cancelTask() {
            if (future != null && !future.isDone()) {
                future.cancel(true);
                mHandler.removeMessages(CHECK_RESULT);
            }
        }

        // 检查该搜索任务是否完成
        boolean isDone() {
            if (future != null) {
                return future.isDone();
            }
            return true;
        }

    } // end of Task

    // 获取搜索结果并更新listView
    void updateResult(List<Topic> topics) {
        try {
            if (topics != null && topics.size() > 0) {
                updateTopicFocusable(topics);
                List<Topic> dataSource = mRecommendTopicView.getBindDataSource();
                dataSource.clear();
                dataSource.addAll(topics);
                mRecommendTopicView.notifyDataSetChanged();
            } else if (topics.size() == 0) {
                ToastMsg.showShortMsgByResName(mContext, "umeng_comm_search_topic_failed");
            } else {
                ToastMsg.showShortMsgByResName(mContext, "umeng_comm_search_topic_failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新搜索到的话题的isFocus字段。由于Server比较难处理是否关注（数据量大），
     * 获取话题时不返回是否关注字段。目前暂时跟本地话题对比的方式</br>
     * 
     * @param newTopics
     */
    private void updateTopicFocusable(List<Topic> newTopics) {
        List<Topic> dataSource = mRecommendTopicView.getBindDataSource();
        if (dataSource.size() == 0) {
            return;
        }

        int len = dataSource.size();
        Topic topic = null;
        for (int i = 0; i < len; i++) {
            topic = dataSource.get(i);
            if (newTopics.contains(topic)) {
                int index = newTopics.indexOf(topic);
                newTopics.get(index).isFocused = topic.isFocused;
            }
        }
    }

    /**
     * 搜索话题</br>
     * 
     * @param keyword
     */
    public void executeSearch(String keyword) {
        mSearchTask.execute(keyword);
    }

    @Override
    public void detach() {
        mSearchTask.cancelTask();
        mHandler.removeCallbacks(null);
        super.detach();
    }

}
