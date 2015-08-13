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

package com.umeng.comm.core.db.ctrl.impl;

import com.umeng.comm.core.db.ctrl.CommentDBAPI;
import com.umeng.comm.core.db.ctrl.FansDBAPI;
import com.umeng.comm.core.db.ctrl.FeedDBAPI;
import com.umeng.comm.core.db.ctrl.FollowDBAPI;
import com.umeng.comm.core.db.ctrl.LikeDBAPI;
import com.umeng.comm.core.db.ctrl.TopicDBAPI;
import com.umeng.comm.core.db.ctrl.UserDBAPI;

/**
 * 数据库API对象抽象工厂实现类,创建各个数据库实现操作类
 */
public class DatabaseFactory implements DbAPIFactory {

    @Override
    public UserDBAPI createUserDBAPI() {
        return new UserDBAPIImpl();
    }

    @Override
    public TopicDBAPI createTopicDBAPI() {
        return new TopicDBAPIImpl();
    }

    @Override
    public FeedDBAPI createFeedDBAPI() {
        return new FeedDBAPIImpl();
    }

    @Override
    public FansDBAPI createFansDBAPI() {
        return new FansDBAPIImpl();
    }

    @Override
    public LikeDBAPI createLikeDBAPI() {
        return new LikeDBAPIImpl();
    }

    @Override
    public FollowDBAPI createFollowDBAPI() {
        return new FollowDBAPIImpl();
    }

    @Override
    public CommentDBAPI createCommentDBAPI() {
        return new CommentDBAPIImpl();
    }

}
