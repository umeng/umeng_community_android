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

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.Topic;

import java.util.ArrayList;
import java.util.List;

public final class ContentChecker {

    /**
     * 已选择的话题
     */
    protected List<Topic> mSelecteTopics = new ArrayList<Topic>();

    /**
     * 保存已经@的好友
     */
    protected List<CommUser> mSelectFriends = new ArrayList<CommUser>();

    public ContentChecker(List<Topic> topics, List<CommUser> users) {
        mSelecteTopics = topics;
        mSelectFriends = users;
    }

    /**
     * 内容是否太短: 新增 Feed 字符控制：发 Feed 时，除去话题，空格，文本中至少需要超过5个（含）字符，转发除外
     * 如果不满足条件提示：发布的内容太少啦，再多写点内容。
     * 
     * @return
     */
    public boolean isValidText(String fullString) {
        String noSpace = fullString.replace(" ", "");
        StringBuilder stringBuilder = new StringBuilder(noSpace);
        for (Topic topic : mSelecteTopics) {
            deleteString(stringBuilder, topic.name);
        }
        for (CommUser user : mSelectFriends) {
            deleteString(stringBuilder, "@" + user.name);
        }
        return stringBuilder.length() >= 5;
    }

    private void deleteString(StringBuilder stringBuilder, String name) {
        int start = stringBuilder.indexOf(name);
        if (start >= 0) {
            stringBuilder.delete(start, start + name.length());
        }
    }
}
