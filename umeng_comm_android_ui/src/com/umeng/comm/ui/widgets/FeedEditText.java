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

package com.umeng.comm.ui.widgets;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.utils.Log;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.fragments.TopicPickerFragment.ResultListener;
import com.umeng.comm.ui.utils.textspan.TextWrapperClickSpan;

@SuppressLint("UseSparseArrays")
/**
 * 发布Feed时的编辑视图
 * TODO : 可以使用扫描法来简化插入话题、好友等操作.
 *
 */
public class FeedEditText extends EditText {

    /**
     * @好友的map
     */
    public Map<Integer, CommUser> mAtMap = new ConcurrentHashMap<Integer, CommUser>();

    /**
     * 话题map 
     */
    public Map<Integer, Topic> mTopicMap = new ConcurrentHashMap<Integer, Topic>();

    private ResultListener<Topic> mTopicListener;

    boolean isMyOp = false;

    int lastTextLength = 0;
    int curTextLength = 0;
    public int mCursorIndex = 0;

    /**
     * @param context
     */
    public FeedEditText(Context context) {
        this(context, null);
    }

    public FeedEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public FeedEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // 添加监听器
        this.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(VIEW_LOG_TAG, "@@@ onTextChanged : start = " + start + ", before = " + before
                        + ", count = " + count + ", text = " + s);

                if (isMyOp) {
                    isMyOp = false;
                    return;
                }
                // 最新的文本内容
                curTextLength = s.length();

                int charOffset = curTextLength - lastTextLength;
                if (charOffset > 0) {
                    Log.d(VIEW_LOG_TAG, "### 插入 " + charOffset + " 个字符");
                } else {
                    Log.d(VIEW_LOG_TAG, "### 删除 " + charOffset + " 个字符");
                    // 删除字符, 最后一个字符
                    deleteFriendOrTopic(start + before - count);
                }

                if (charOffset != 0 && !isDecorating && isNeedUpdateIndex()) {
                    updateMapIndex(start, charOffset);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(VIEW_LOG_TAG, "@@@ beforeTextChanged : start = " +
                        start + ", after = "
                        + after
                        + ", count = " + count + ", text = " + s);
                mCursorIndex = getSelectionStart() + 1;
                lastTextLength = s.length();
            }

            @Override
            public void afterTextChanged(Editable text) {
                Log.d(VIEW_LOG_TAG, "### text : " + text);
                Log.d(VIEW_LOG_TAG, "### 字符数 : " + getText().toString().length());
                checkChars();
            }
        });
    }

    private void checkChars() {
        // int currentChars =
        // CommonUtils.getCharacterNums(getText().toString());
        int MAX_CHARS = CommConfig.getConfig().mFeedLen;
        int currentChars = getText().toString().length();
        int temp = currentChars - MAX_CHARS;
        if (temp > 0) {
            setText(getText().delete(MAX_CHARS, currentChars));
            setSelection(MAX_CHARS);
            ToastMsg.showShortMsgByResName("umeng_comm_overflow_tips");
        }

    }

    // 覆盖该方法的目的：避免用户在话题或者@好友文本中间插入自己输入的内容。
    // 目前的处理策略是：在话题名or好友名中间插入文本，则光标自动移动到该话题名or好友名的末尾
    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        if (selStart == selEnd && selStart != 0) {
            boolean isMiddle = isTopicOrFriendMiddle(getSelectionStart());
            if (isMiddle) {
                int newPos = getNextCursorPos(getSelectionStart());
                if (newPos + 1 > getText().length()) {
                    newPos = getText().length() - 1;
                }
                setSelection(newPos + 1);
                super.onSelectionChanged(newPos, newPos + (selEnd - selStart) + 1);
            }
        } else {
            super.onSelectionChanged(selStart, selEnd);
        }
    }

    /**
     * 获取当前cursor的最佳insert位置。最佳位置如下：<li>1：如果当前位置未在话题名or好友名的中间，则cursorStart为最佳位置；
     * <li>2：如果当前位置在话题名or好友名的中间，则最佳位置为该话题名or好友名的末尾</br>
     * 
     * @param cursorStart 光标的当前位置
     * @return 最佳的光标insert位置
     */
    private int getNextCursorPos(int cursorStart) {
        // 检查插入的字符是否在话题中间
        Set<Entry<Integer, Topic>> entries = mTopicMap.entrySet();
        for (Entry<Integer, Topic> entry : entries) {
            int start = entry.getKey();
            int offset = entry.getValue().name.length();
            if (cursorStart > start && cursorStart < start + offset) {
                return start + offset;
            }
        }
        // 检查插入的字符是否在@好友中间
        Set<Entry<Integer, CommUser>> friendsEntries = mAtMap.entrySet();
        for (Entry<Integer, CommUser> entry : friendsEntries) {
            int start = entry.getKey();
            int offset = entry.getValue().name.length();
            if (cursorStart > start && cursorStart < start + offset) {
                return start + offset;
            }
        }
        return cursorStart;
    }

    /**
     * 当前的位置{@link textStart}是否在某个话题名or好友名的中间</br>
     * 
     * @param textStart 当前光标位置
     * @return true，当前光标位置在话题名or好友名的中间；否则返回false
     */
    private boolean isTopicOrFriendMiddle(int textStart) {
        // 检查插入的字符是否在话题中间
        Set<Entry<Integer, Topic>> entries = mTopicMap.entrySet();
        for (Entry<Integer, Topic> entry : entries) {
            int start = entry.getKey();
            int offset = entry.getValue().name.length();
            if (textStart > start && textStart < start + offset) {
                return true;
            }
        }
        // 检查插入的字符是否在@好友中间
        Set<Entry<Integer, CommUser>> friendsEntries = mAtMap.entrySet();
        for (Entry<Integer, CommUser> entry : friendsEntries) {
            int start = entry.getKey();
            int offset = entry.getValue().name.length();
            if (textStart > start && textStart < start + offset) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param start
     * @param text
     */
    public void deleteElement(int start, String text) {
        Editable editableText = getText();
        // +1表示每个话题或者好友name后的一个空格
        final int deleteLength = text.length() + 1;
        if (start + deleteLength > this.length()) {
            Log.d(VIEW_LOG_TAG, "### 删除的文字超过了原来的长度");
            return;
        }

        isDecorating = true;

        // 删除这个区域的文本
        Editable newEditable = editableText.delete(start,
                start + deleteLength);
        setText(newEditable);

        // 更新索引
        updateMapIndex(start, -deleteLength);
        isDecorating = false;
        mCursorIndex = newEditable.length();
        setSelection(mCursorIndex);
    }

    /**
     * @param topic
     */
    public void removeTopic(final Topic topic) {
        Iterator<Integer> iterator = mTopicMap.keySet().iterator();
        while (iterator.hasNext()) {
            Integer key = iterator.next();
            Topic curTopic = mTopicMap.get(key);
            if (topic != null && curTopic != null && curTopic.equals(topic)) {
                iterator.remove();
                //
                deleteElement(key, topic.name);
                break;
            }
        }
        // mTopicMap.values().remove(topic);
    }

    /**
     * @param start
     * @param charOffset 大于0代表插入数据, 后面的索引向后移动; 小于0,代表删掉字符,后面的索引向前移动.
     */
    private void updateAtMap(int start, int charOffset) {
        // @好友
        Object[] keysStrings = mAtMap.keySet().toArray();
        // 对于key排序，升序排列,这样便于删除某个@时更新后面的数据索引
        Arrays.sort(keysStrings, new Comparator<Object>() {

            @Override
            public int compare(Object lhs, Object rhs) {
                return ((Integer) lhs) - ((Integer) rhs);
            }
        });
        int myStart = start;

        // 迭代索引
        for (int i = 0; i < keysStrings.length; i++) {
            // key
            int keyIndex = (Integer) keysStrings[i];
            // value
            CommUser item = mAtMap.get(keyIndex);
            // 后移
            if (keyIndex >= start && (keyIndex + charOffset) <= getText().length()) {
                mAtMap.put(keyIndex + charOffset, item);
                mAtMap.remove(keyIndex);
            }

            // log
            Log.d(VIEW_LOG_TAG, "### updateAtMap的item, keyIndex = " + keyIndex + ", item = " +
                    item
                    + ", myStart = " + myStart + ",  charOffset = " + charOffset);
        } // end for

        Log.d(VIEW_LOG_TAG, "@@@ @好友的map : " + mAtMap);
    }

    /**
     * @param start
     * @param before
     * @param count
     */
    private void updateTopicMap(int start, int charOffset) {
        // 话题
        Object[] keysStrings = mTopicMap.keySet().toArray();
        Arrays.sort(keysStrings);

        int myStart = start;

        // 迭代索引
        for (int i = 0; i < keysStrings.length; i++) {
            // key
            int keyIndex = (Integer) keysStrings[i];
            // value
            Topic item = mTopicMap.get(keyIndex);

            int newIndex = keyIndex + charOffset;
            // 更新索引
            if (keyIndex >= start && newIndex >= 0) {
                mTopicMap.remove(keyIndex);
                mTopicMap.put(newIndex, item);
            }

            // log
            Log.d(VIEW_LOG_TAG, "### updateTopicMap的item, keyIndex = " + keyIndex + ", item = " +
                    item
                    + ", myStart = " + myStart + ",  charOffset = " + charOffset);
        } // end for

        Log.d(VIEW_LOG_TAG, "@@@ 话题的map : " + mTopicMap);
    }

    /**
     * @param start
     * @param charOffser
     */
    private void updateMapIndex(int start, int charOffset) {
        try {
            updateAtMap(start, charOffset);
            updateTopicMap(start, charOffset);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @param start
     * @param end
     */
    private void deleteText(int start, int end) {
        Editable originText = getText();
        int originLength = originText.length();

        if (originLength >= end - 1) {
            originText.delete(start, end - 1);
            setText(originText);
            setSelection(start);
        }
    }

    /**
     * @param last
     */
    private void deleteFriendOrTopic(int last) {
        Log.d(VIEW_LOG_TAG, "### 删除字符 last " + last);
        Set<Entry<Integer, CommUser>> atKeys = mAtMap.entrySet();
        //
        Iterator<Entry<Integer, CommUser>> iterator = atKeys.iterator();
        while (iterator.hasNext()) {
            Map.Entry<java.lang.Integer, CommUser> entry = iterator
                    .next();

            int temp = entry.getKey() + entry.getValue().name.length();

            Log.d(VIEW_LOG_TAG, "### index = " + temp
                    + ", edit last = " + last);

            // 找到以后,删除好友名字
            if (temp + 1 == last) {
                iterator.remove();
                isMyOp = true;
                // 删除文本
                deleteText(entry.getKey(), last);
                // updateTopicMap(start, charOffset);
                mFriendsListener.onRemove(entry.getValue());
                return;
            }
        }

        Log.d(VIEW_LOG_TAG, "### topic map : " + mTopicMap);
        // topic
        Set<Entry<Integer, Topic>> topicKeys = mTopicMap.entrySet();
        //
        Iterator<Entry<Integer, Topic>> topicIterator = topicKeys.iterator();
        //
        while (topicIterator.hasNext()) {
            Map.Entry<java.lang.Integer, Topic> entry = topicIterator
                    .next();

            int temp = entry.getKey() + entry.getValue().name.length();

            Log.d(VIEW_LOG_TAG, "### topic index = " + temp
                    + ", edit last = " + last);
            // 找到以后,删除话题
            if (temp == last) {
                topicIterator.remove();
                isMyOp = true;
                // 删除文本
                deleteText(entry.getKey(), last);

                if (mTopicListener != null) {
                    mTopicListener.onRemove(entry.getValue());
                }
                return;
            }
        }

    }

    /**
     * 是否需要更新索引。由于insert话题时有一个空格，此处在检查时做-1处理
     * 
     * @return
     */
    private boolean isNeedUpdateIndex() {
        int start = getSelectionStart();
        Log.d(VIEW_LOG_TAG, "#### isNeedUpdateIndex, start = " + start);
        Set<Integer> atKeys = mAtMap.keySet();
        for (Integer integer : atKeys) {
            Log.d(VIEW_LOG_TAG, "#### isNeedUpdateIndex,at  index = " + integer);
            if (integer >= start - 1) {
                return true;
            }
        }

        Set<Integer> topicKeys = mTopicMap.keySet();
        for (Integer integer : topicKeys) {
            Log.d(VIEW_LOG_TAG, "#### isNeedUpdateIndex,topic  index = " + integer);
            if (integer >= start - 1) {
                return true;
            }
        }

        return false;
    }

    boolean isDecorating = false;

    /**
     * 
     */
    private void decorateText() {

        isDecorating = true;

        //
        SpannableStringBuilder ssb = new SpannableStringBuilder(getText());
        // 已经存在的话题先包装
        Set<Integer> topicKeySet = mTopicMap.keySet();
        for (Integer atIndex : topicKeySet) {
            Topic topic = mTopicMap.get(atIndex);
            if (topic != null) {
                // ViewUtils.wrapString(ssb, atIndex, topic.name);
                ssb.setSpan(new TextWrapperClickSpan(), atIndex, atIndex + topic.name.length(), 0);
            }
        }

        // @好友的wrap
        Set<Integer> keySet = mAtMap.keySet();
        for (Integer atIndex : keySet) {
            CommUser atUser = mAtMap.get(atIndex);
            if (atUser != null && !TextUtils.isEmpty(atUser.name)) {
                // ViewUtils.wrapString(ssb, atIndex, "@" + atUser.name + " ");
                String atName = "@" + atUser.name + " ";
                ssb.setSpan(new TextWrapperClickSpan(), atIndex, atIndex + atName.length(), 0);
            }
        }

        setText(ssb);
    }

    /**
     * 封装赞的TextView
     * 
     * @param str
     * @return
     */
    public void atFriends(List<CommUser> friends) {

        if (friends == null) {
            return;
        }
        // removeNonexistFriend(friends);
        // 光标的位置
        int editSection = getCursorPos();
        Log.d("", "### atFriends, start = " + editSection);
        SpannableStringBuilder ssb = new SpannableStringBuilder(getText());

        int count = friends.size();
        for (int i = 0; i < count; i++) {
            final CommUser user = friends.get(i);
            // 如果已经有了该好友,则直接返回
            if (mAtMap.containsValue(user)) {
                continue;
            }
            isDecorating = true;
            final String name = "@" + user.name + " ";
            ssb.insert(editSection, name);
            setText(ssb);
            // 更新map索引
            updateMapIndex(editSection, name.length());
            // 将所有at的位置存储在map中
            mAtMap.put(editSection, user);
            // 更新起始点
            editSection += name.length();
        }

        // 包装文本
        decorateText();
        isDecorating = false;
        // 此时将光标移动到文本末尾
        setSelection(getText().length());
        mCursorIndex = editSection;
    } // end of atFriends

    /**
     * 获取光标位置</br>
     * 
     * @return
     */
    private int getCursorPos() {
        int editSection = getSelectionStart();
        // 判断当前光标的位置是否在一个好友name中间。如果处于name中间，则将光标移动至name的末尾再insert好友name
        Set<Entry<Integer, CommUser>> entries = mAtMap.entrySet();
        for (Entry<Integer, CommUser> entry : entries) {
            int start = entry.getKey();
            int offset = entry.getValue().name.length() + 1;
            if (editSection > start && editSection < start + offset) {
                editSection = start + offset;
                break;
            }
        }
        return editSection;
    }

    /**
     * 移除不存在的好友。在用户选择@好友后，再次选择时删除了某些好友的情况</br>
     * 
     * @param newFriends
     */
    // private void removeNonexistFriend( List<CommUser> newFriends){
    // Collection<CommUser> oldUsers = mAtMap.values();
    // Iterator<CommUser> iterator = oldUsers.iterator();
    // while (iterator.hasNext() ) {
    // CommUser user = iterator.next();
    // String context = getText().toString();
    // if ( !newFriends.contains(user) ) {
    // String name = "@" + user.name ;
    // int start = context.indexOf(name);
    // if ( start >= 0 ) {
    // iterator.remove();
    // deleteElement(start, name);
    // }
    // }
    // }
    // }

    /**
     * 封topic的TextView
     * 
     * @param str
     * @return
     */
    public void insertTopics(List<Topic> topics) {

        if (topics == null || topics.size() == 0) {
            return;
        }
        // 光标的位置
        int editSection = getSelectionStart();

        Log.d("", "### insertTopicText, start = " + editSection);
        SpannableStringBuilder ssb = new SpannableStringBuilder(getText());

        int count = topics.size();
        for (int i = 0; i < count; i++) {
            final Topic topicItem = topics.get(i);
            final String name = topicItem.name + " ";
            // 如果已经有了该好友,则直接返回
            if (mTopicMap.containsValue(topicItem)) {
                continue;
            }
            isDecorating = true;
            int ssbLength = ssb.length();
            if (editSection > ssbLength) {
                editSection = ssbLength;
            }
            ssb.insert(editSection, name);
            setText(ssb);
            // 更新map索引
            updateMapIndex(editSection, name.length());
            Log.d("#####", "#####put start : " + editSection);
            // 将所有at的位置存储在map中
            mTopicMap.put(editSection, topicItem);
            // 更新起始点
            editSection += name.length();
        }

        // 包装文本
        decorateText();
        isDecorating = false;
        setSelection(editSection);
        mCursorIndex = editSection;
    }

    /**
     * @param listener
     */
    public void setTopicListener(ResultListener<Topic> listener) {
        mTopicListener = listener;
    }

    public ResultListener<CommUser> mFriendsListener;

    public void setResultListener(ResultListener<CommUser> listener) {
        mFriendsListener = listener;
    }
}
