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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.beans.ImageItem;
import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.utils.SharePrefUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * feed备忘录,用于在发送feed时保存当前数据，以便于在发送失败时重新发送.
 * 
 */
public class FeedMemento {

    /**
     * 创建备忘录
     * 
     * @param feedItem
     * @return
     */
    public static void createMemento(Context context, FeedItem feedItem) {
        savePostData(context, feedItem);
    }

    /**
     * 保存要发布的数据
     */
    private static void savePostData(Context context, FeedItem feedItem) {
        JSONObject feedJsonObject = new JSONObject();
        try {
            feedJsonObject.put("content", feedItem.text);
            feedJsonObject.put("location_addr", feedItem.locationAddr);
            // 选中的图片
            JSONArray imageJsonArray = new JSONArray();
            for (ImageItem item : feedItem.imageUrls) {
                imageJsonArray.put(item.originImageUrl);
            }
            feedJsonObject.put("images", imageJsonArray);

            // 选中的好友
            JSONArray friendsJsonArray = new JSONArray();
            for (CommUser friend : feedItem.atFriends) {
                //
                JSONObject friendObject = new JSONObject();
                friendObject.put("id", friend.id);
                friendObject.put("name", friend.name);

                friendsJsonArray.put(friendObject);
            }
            feedJsonObject.put("at_friends", friendsJsonArray);

            // 选中的话题
            JSONArray topicsJsonArray = new JSONArray();
            for (Topic topic : feedItem.topics) {

                JSONObject topicObject = new JSONObject();
                topicObject.put("id", topic.id);
                topicObject.put("name", topic.name);
                //
                topicsJsonArray.put(topicObject);
            }
            feedJsonObject.put("topics", topicsJsonArray);

            //
            SharedPreferences.Editor sharePrefEditor = SharePrefUtils.getSharePrefEdit(context,
                    Constants.FEED_SHARE_PREF).edit();
            sharePrefEditor
                    .putString(Constants.FEED_SHARE_PREF_KEY, feedJsonObject.toString())
                    .commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param context
     * @return
     */
    public static FeedItem restoreMemento(Context context) {
        FeedItem feedItem = new FeedItem();
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                Constants.FEED_SHARE_PREF,
                Context.MODE_PRIVATE);
        String saveFeedStr = sharedPreferences.getString(Constants.FEED_SHARE_PREF_KEY, "");
        if (!TextUtils.isEmpty(saveFeedStr)) {
            try {
                JSONObject feedJsonObject = new JSONObject(saveFeedStr);
                feedItem.text = feedJsonObject.optString("content");
                feedItem.locationAddr = feedJsonObject.optString("location_addr");
                // 图片
                parseImages(feedItem, feedJsonObject);
                // 话题
                parseTopics(feedItem, feedJsonObject);
                // 好友
                parseFriends(feedItem, feedJsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } // end if

        return feedItem;
    }

    /**
     * @param feedItem
     * @param feedJsonObject
     * @throws JSONException
     */
    private static void parseImages(FeedItem feedItem, JSONObject feedJsonObject)
            throws JSONException {
        JSONArray imageArray = feedJsonObject.optJSONArray("images");
        if (imageArray != null && imageArray.length() > 0) {
            int length = imageArray.length();
            for (int i = 0; i < length; i++) {
                feedItem.imageUrls.add(new ImageItem("", "", imageArray.getString(i)));
            }
        }
    } //

    /**
     * @param feedItem
     * @param feedJsonObject
     */
    private static void parseTopics(FeedItem feedItem, JSONObject feedJsonObject)
            throws JSONException {
        JSONArray topicsArray = feedJsonObject.optJSONArray("topics");
        if (topicsArray != null && topicsArray.length() > 0) {
            int length = topicsArray.length();
            for (int i = 0; i < length; i++) {
                JSONObject topicJsonObject = topicsArray.getJSONObject(i);
                Topic topic = new Topic();
                topic.id = topicJsonObject.optString("id");
                topic.name = topicJsonObject.optString("name");
                feedItem.topics.add(topic);
            }
        }
    } //

    /**
     * @param feedItem
     * @param feedJsonObject
     */
    private static void parseFriends(FeedItem feedItem, JSONObject feedJsonObject)
            throws JSONException {
        JSONArray friendArray = feedJsonObject.optJSONArray("at_friends");
        if (friendArray != null && friendArray.length() > 0) {
            int length = friendArray.length();
            for (int i = 0; i < length; i++) {
                JSONObject friendJsonObject = friendArray.getJSONObject(i);
                CommUser friend = new CommUser();
                friend.id = friendJsonObject.optString("id");
                friend.name = friendJsonObject.optString("name");
                feedItem.atFriends.add(friend);
            }
        }
    } //

    /**
     * 清空备份的数据
     * 
     * @param context
     */
    public static void clear(Context context) {
        Editor editor = SharePrefUtils.getSharePrefEdit(context,
                Constants.FEED_SHARE_PREF).edit();
        if (editor != null) {
            editor.remove(Constants.FEED_SHARE_PREF).commit();
        }
    }
}
