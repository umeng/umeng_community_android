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

import java.util.ArrayList;
import java.util.List;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.ImageItem;
import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.beans.relation.DBRelationOP;
import com.umeng.comm.core.beans.relation.EntityRelationFactory;
import com.umeng.comm.core.db.ctrl.TopicDBAPI;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.utils.CommonUtils;

class TopicDBAPIImpl extends AbsDbAPI<List<Topic>> implements TopicDBAPI {

    @Override
    public void loadTopicsFromDB(final SimpleFetchListener<List<Topic>> listener) {
        submit(new DbCommand() {

            @Override
            protected void execute() {
                List<Topic> topics = new Select().from(Topic.class).execute();
                fillTopicImageItems(topics);
                deliverResult(listener, topics);
            }
        });
    }

    @Override
    public void loadTopicsFromDB(final String uid, final SimpleFetchListener<List<Topic>> listener) {
        submit(new DbCommand() {

            @Override
            protected void execute() {
                DBRelationOP<List<Topic>> relationOP = EntityRelationFactory.createUserTopic();
                List<Topic> topics = relationOP.queryById(uid);
                fillTopicImageItems(topics);
                deliverResult(listener, topics);
            }
        });
    }

    @Override
    public void saveTopicsToDB(final List<Topic> topics) {
        submit(new DbCommand() {

            @Override
            protected void execute() {
                for (Topic topic : topics) {
                    topic.saveEntity();
                }
            }
        });
    }

    @Override
    public void deleteTopicFromDB(final String topicId) {
        submit(new DbCommand() {

            @Override
            protected void execute() {
                CommUser user = CommConfig.getConfig().loginedUser;
                DBRelationOP<?> relationOP = EntityRelationFactory.createUserTopic(user, topicId);
                relationOP.deleteById(user.id);
                deleteTopicImages(topicId);
            }
        });
    }

    @Override
    public void saveFollowedTopicsToDB(final String uid, final List<Topic> topics) {
        submit(new DbCommand() {

            @Override
            protected void execute() {
                for (Topic topic : topics) {
                    DBRelationOP<List<Topic>> relationOP = EntityRelationFactory.createUserTopic(
                            new CommUser(uid),
                            topic);
                    relationOP.saveEntity();
                    topic.saveEntity();
                }
            }
        });
    }

    @Override
    public void saveFollowedTopicToDB(final String uid, Topic topic) {
        List<Topic> topics = new ArrayList<Topic>();
        topics.add(topic);
        saveFollowedTopicsToDB(uid, topics);
    }

    @Override
    public void deleteTopicDataFromDB(final String topicId) {
        deleteTopicFromDB(topicId);
        submit(new DbCommand() {

            @Override
            protected void execute() {
                new Delete().from(Topic.class).where("topic._id=?", topicId).execute();
                deleteTopicImages(topicId);
            }
        });
    }

    @Override
    public void deleteAllTopics() {
        new Delete().from(Topic.class).execute();
    }

    private List<ImageItem> selectImagesForTopic(String topicId) {
        return new Select().from(ImageItem.class).where("feedId=?", topicId).execute();
    }

    private void deleteTopicImages(String topicId) {
        new Delete().from(ImageItem.class).where("imageitem.feedId=?", topicId).execute();
    }

    private void fillTopicImageItems(List<Topic> topics) {
        if (CommonUtils.isListEmpty(topics)) {
            return;
        }
        for (Topic topic : topics) {
            List<ImageItem> cacheTopics = selectImagesForTopic(topic.id);
            if (!CommonUtils.isListEmpty(cacheTopics)) {
                topic.imageItems.addAll(cacheTopics);
            }
        }
    }

}
