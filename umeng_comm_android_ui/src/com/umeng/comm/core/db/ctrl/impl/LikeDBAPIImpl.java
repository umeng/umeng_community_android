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

import android.text.TextUtils;

import com.activeandroid.query.Delete;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.beans.Like;
import com.umeng.comm.core.beans.relation.DBRelationOP;
import com.umeng.comm.core.beans.relation.EntityRelationFactory;
import com.umeng.comm.core.db.ctrl.LikeDBAPI;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;

import java.util.ArrayList;
import java.util.List;

class LikeDBAPIImpl extends AbsDbAPI<List<Like>> implements LikeDBAPI {

    @Override
    public void loadLikesFromDB(final FeedItem feedItem,
            final SimpleFetchListener<List<Like>> listener) {
        submit(new DbCommand() {

            @Override
            protected void execute() {
                DBRelationOP<List<Like>> relation = EntityRelationFactory.createFeedLike();
                List<Like> result = relation.queryById(feedItem.id);
                deliverResult(listener, result);
            }
        });
    }

    @Override
    public void saveLikesToDB(final FeedItem feedItem) {

        submit(new DbCommand() {

            @Override
            protected void execute() {
                final List<Like> likes = new ArrayList<Like>(feedItem.likes);
                // 存储实体类本身
                for (Like like : likes) {
                    like.saveEntity();
                    // 存储关系
                    DBRelationOP<?> relation = EntityRelationFactory.createFeedLike(feedItem,
                            like);
                    relation.saveEntity();
                }
            }
        });

    }

    @Override
    public void deleteLikesFromDB(final String feedId, final String likeId) {
        if (TextUtils.isEmpty(feedId) || TextUtils.isEmpty(likeId)) {
            return;
        }

        submit(new DbCommand() {

            @Override
            protected void execute() {
                new Delete().from(Like.class).where("_id=?", likeId).execute();
                DBRelationOP<?> relation = EntityRelationFactory.createFeedLike(feedId);
                relation.deleteById(likeId);
            }
        });
    }

}
