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

package com.umeng.comm.ui.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.constants.Constants;

/**
 * 广播send/receive工具类。广播分为用户、话题、feed、计数四中类别，如果需要更细的区分是哪类，可以根据type（
 * {@link BROADCAST_TYPE} ）来作更细粒度的区分。
 * 
 * @author BinGoBinBin
 */
public class BroadcastUtils {

    private static final String ACTION_USER = "com.umeng.comm.user";// 用户Action
    private static final String ACTION_TOPIC = "com.umeng.comm.topic";// 话题Action
    private static final String ACTION_FEED = "com.umeng.comm.feed";// feed
                                                                    // Action
    private static final String ACTION_COUNT = "com.umeng.comm.count"; // 计数Action
    private static final String ACTION_UPDATE = "com.umeng.comm.update"; // 更新Action

    /**
     * 广播类型
     */
    public static enum BROADCAST_TYPE {
        TYPE_USER_UPDATE, // 更新用户信息
        TYPE_USER_FOLLOW, // 关注用户
        TYPE_USER_CANCEL_FOLLOW, // 取消关注用户
        TYPE_TOPIC_FOLLOW, // 关注某个话题
        TYPE_TOPIC_CANCEL_FOLLOW, // 取消关注某个话题
        TYPE_COUNT_FEED, // feed消息条数
        TYPE_COUNT_USER, // user消息条数
        TYPE_COUNT_FANS, // 粉丝消息条数
        TYPE_FEED_POST, // 发送feed
        TYPE_FEED_DELETE, // 删除feed
        TYPE_FEED_UPDATE,// update feed
        TYPE_FEED_FAVOURITE// feed favourite
    }

    /**
     * 注册一个用户级别的广播</br>
     * 
     * @param context
     * @param receiver
     */
    public static void registerUserBroadcast(Context context, DefalutReceiver receiver) {
        registerBroadcast(context, ACTION_USER, receiver);
    }

    /**
     * 注册一个话题级别的广播</br>
     * 
     * @param context
     * @param receiver
     */
    public static void registerTopicBroadcast(Context context, DefalutReceiver receiver) {
        registerBroadcast(context, ACTION_TOPIC, receiver);
    }

    /**
     * 注册一个Feed级别的广播</br>
     * 
     * @param context
     * @param receiver
     */
    public static void registerFeedBroadcast(Context context, DefalutReceiver receiver) {
        registerBroadcast(context, ACTION_FEED, receiver);
    }

    /**
     * 注册一个feed更新级别的广播</br>
     * 
     * @param context
     * @param receiver
     */
    public static void registerFeedUpdateBroadcast(Context context, DefalutReceiver receiver) {
        registerBroadcast(context, ACTION_UPDATE, receiver);
    }

    /**
     * 注册一个更新消息数的广播</br>
     * 
     * @param context
     * @param receiver
     */
    public static void registerCountBroadcast(Context context, DefalutReceiver receiver) {
        registerBroadcast(context, ACTION_COUNT, receiver);
    }

    /**
     * 注册一个广播</br>
     * 
     * @param context
     * @param action
     * @param receiver
     */
    private static void registerBroadcast(Context context, String action, DefalutReceiver receiver) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(action);
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter);
        // context.registerReceiver(receiver, filter);
    }

    /**
     * 注销该context下的所有广播。</br>
     * 
     * @param context
     */
    public static void unRegisterBroadcast(Context context, BroadcastReceiver receiver) {
        if (context != null && receiver != null) {
            try {
                // context.unregisterReceiver(receiver);
                LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
            } catch (IllegalArgumentException e) {
            }

        }
    }

    /**
     * 发送一个关注用户的广播</br>
     * 
     * @param context
     * @param user
     */
    public static void sendUserFollowBroadcast(Context context, CommUser user) {
        Intent intent = buildIntent(BROADCAST_TYPE.TYPE_USER_FOLLOW, ACTION_USER);
        intent.putExtra(Constants.TAG_USER, user);
        sendBroadcast(context, intent);
    }

    /**
     * 发送一个更新用户的广播</br>
     * 
     * @param context
     * @param user
     */
    public static void sendUserUpdateBroadcast(Context context, CommUser user) {
        Intent intent = buildIntent(BROADCAST_TYPE.TYPE_USER_UPDATE, ACTION_USER);
        intent.putExtra(Constants.TAG_USER, user);
        sendBroadcast(context, intent);
    }

    /**
     * 发送一个取消关注某个用户的广播</br>
     * 
     * @param context
     * @param user
     */
    public static void sendUserCancelFollowBroadcast(Context context, CommUser user) {
        Intent intent = buildIntent(BROADCAST_TYPE.TYPE_USER_CANCEL_FOLLOW, ACTION_USER);
        intent.putExtra(Constants.TAG_USER, user);
        sendBroadcast(context, intent);
    }

    /**
     * 发送关注某个话题的广播</br>
     * 
     * @param context
     * @param topic
     */
    public static void sendTopicFollowBroadcast(Context context, Topic topic) {
        Intent intent = buildIntent(BROADCAST_TYPE.TYPE_TOPIC_FOLLOW, ACTION_TOPIC);
        intent.putExtra(Constants.TAG_TOPIC, topic);
        sendBroadcast(context, intent);
    }

    /**
     * 发送取消关注某个话题的广播</br>
     * 
     * @param context
     * @param topic
     */
    public static void sendTopicCancelFollowBroadcast(Context context, Topic topic) {
        Intent intent = buildIntent(BROADCAST_TYPE.TYPE_TOPIC_CANCEL_FOLLOW, ACTION_TOPIC);
        intent.putExtra(Constants.TAG_TOPIC, topic);
        sendBroadcast(context, intent);
    }

    /**
     * 发送取消关注某个话题的广播</br>
     * 
     * @param context
     * @param topic
     */
    public static void sendCountFeedBroadcast(Context context, int count) {
        Intent intent = buildIntent(BROADCAST_TYPE.TYPE_COUNT_FEED, ACTION_COUNT);
        intent.putExtra(Constants.TAG_COUNT, count);
        sendBroadcast(context, intent);
    }

    /**
     * 发送取消关注某个话题的广播</br>
     * 
     * @param context
     * @param topic
     */
    public static void sendCountUserBroadcast(Context context, int count) {
        Intent intent = buildIntent(BROADCAST_TYPE.TYPE_COUNT_USER, ACTION_COUNT);
        intent.putExtra(Constants.TAG_COUNT, count);
        sendBroadcast(context, intent);
    }

    /**
     * 发送粉丝数广播</br>
     * 
     * @param context
     * @param topic
     */
    public static void sendCountFansBroadcast(Context context, int count) {
        Intent intent = buildIntent(BROADCAST_TYPE.TYPE_COUNT_FANS, ACTION_COUNT);
        intent.putExtra(Constants.TAG_COUNT, count);
        sendBroadcast(context, intent);

    }

    /**
     * send“发送feed”的广播</br>
     * 
     * @param context
     * @param topic
     */
    public static void sendFeedPostBroadcast(Context context, FeedItem item) {
        Intent intent = buildIntent(BROADCAST_TYPE.TYPE_FEED_POST, ACTION_FEED);
        intent.putExtra(Constants.TAG_FEED, item);
        sendBroadcast(context, intent);
    }

    /**
     * send“更新feed”的广播</br>
     * 
     * @param context
     * @param topic
     */
    public static void sendFeedUpdateBroadcast(Context context, FeedItem item) {
        Intent intent = buildIntent(BROADCAST_TYPE.TYPE_FEED_UPDATE, ACTION_UPDATE);
        intent.putExtra(Constants.TAG_FEED, item);
        sendBroadcast(context, intent);
    }

    /**
     * 发送删除feed的广播</br>
     * 
     * @param context
     * @param topic
     */
    public static void sendFeedDeleteBroadcast(Context context, FeedItem item) {
        Intent intent = buildIntent(BROADCAST_TYPE.TYPE_FEED_DELETE,
                ACTION_FEED);
        intent.putExtra(Constants.TAG_FEED, item);
        sendBroadcast(context, intent);
    }
    
    /**
     * 发送删除feed的广播</br>
     * 
     * @param context
     * @param topic
     */
    public static void sendFeedFavouritesBroadcast(Context context, FeedItem item) {
        Intent intent = buildIntent(BROADCAST_TYPE.TYPE_FEED_FAVOURITE,
                ACTION_FEED);
        intent.putExtra(Constants.TAG_FEED, item);
        sendBroadcast(context, intent);
    }

    private static void sendBroadcast(Context context, Intent intent) {
        if (context != null) {
            // context.sendBroadcast(intent);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    private static Intent buildIntent(BROADCAST_TYPE type, String action) {
        Intent intent = new Intent();
        intent.putExtra(Constants.TAG_TYPE, type);
        intent.setAction(action);
        return intent;
    }

    /**
     * 默认的广播接收器实现类。具体对哪类（user、feed、topic、count）感兴趣，覆盖相应的方法并处理并处理逻辑
     */
    public static class DefalutReceiver extends BroadcastReceiver {

        public void onReceiveUser(Intent intent) {// 收到用户广播时的回调
        }

        public void onReceiveTopic(Intent intent) {// 收到话题广播时的回调
        }

        public void onReceiveFeed(Intent intent) {// 收到feed广播时的回调
        }

        public void onReceiveCount(Intent intent) {// 收到更新消息数广播时的回调
        }

        public void onReceiveUpdateFeed(Intent intent) {
        }

        protected CommUser getUser(Intent intent) {// 从intent中获取数据
            return intent.getExtras().getParcelable(Constants.TAG_USER);
        }

        protected Topic getTopic(Intent intent) {
            return intent.getExtras().getParcelable(Constants.TAG_TOPIC);// 从intent中获取数据
        }

        protected FeedItem getFeed(Intent intent) {
            return intent.getExtras().getParcelable(Constants.TAG_FEED);// 从intent中获取数据
        }

        protected int getCount(Intent intent) {// 从intent中获取数据
            return intent.getIntExtra(Constants.TAG_COUNT, 0);
        }

        protected BROADCAST_TYPE getType(Intent intent) {// 获取类型
            return (BROADCAST_TYPE) intent.getSerializableExtra(Constants.TAG_TYPE);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USER.equals(action)) {
                onReceiveUser(intent);
            } else if (ACTION_TOPIC.equals(action)) {
                onReceiveTopic(intent);
            } else if (ACTION_COUNT.equals(action)) {
                onReceiveCount(intent);
            } else if (ACTION_FEED.equals(action)) {
                onReceiveFeed(intent);
            } else if (ACTION_UPDATE.equals(action)) {
                onReceiveUpdateFeed(intent);
            }
        }
    }
}
