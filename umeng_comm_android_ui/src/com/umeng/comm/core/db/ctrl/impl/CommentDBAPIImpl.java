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

import com.activeandroid.query.Delete;
import com.umeng.comm.core.beans.Comment;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.beans.relation.DBRelationOP;
import com.umeng.comm.core.beans.relation.EntityRelationFactory;
import com.umeng.comm.core.db.ctrl.CommentDBAPI;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 评论的数据库相关操作
 */
class CommentDBAPIImpl extends AbsDbAPI<List<Comment>> implements CommentDBAPI {

    @Override
    public void loadCommentsFromDB(final String feedId,
            final SimpleFetchListener<List<Comment>> listener) {
        submit(new DbCommand() {

            @Override
            protected void execute() {
                DBRelationOP<List<Comment>> feedComment = EntityRelationFactory.createFeedComment();
                List<Comment> comments = feedComment.queryById(feedId);
                deliverResult(listener, comments);
            }
        });
    }

    @Override
    public void saveCommentsToDB(final FeedItem feedItem) {
        final List<Comment> comments = new ArrayList<Comment>(feedItem.comments);
        if (comments == null || comments.size() == 0) {
            return;
        }

        submit(new DbCommand() {

            @Override
            protected void execute() {
                for (Comment comment : comments) {
                    comment.saveEntity();
                    DBRelationOP<?> feedComment = EntityRelationFactory.createFeedComment(feedItem,
                            comment);
                    feedComment.saveEntity();
                }
            }
        });

    }

    @Override
    public void deleteCommentsFromDB(final String commentId) {
        submit(new DbCommand() {

            @Override
            protected void execute() {
                // 这里需要删除评论本身
                new Delete().from(Comment.class).where("_id=?", commentId).execute();
                // 删除评论跟feed的关系表
                DBRelationOP<?> feedComment = EntityRelationFactory.createFeedComment();
                feedComment.deleteById(commentId);
            }
        });
    }

}
