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

import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.relation.DBRelationOP;
import com.umeng.comm.core.beans.relation.EntityRelationFactory;
import com.umeng.comm.core.db.ctrl.FansDBAPI;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;

import java.util.LinkedList;
import java.util.List;

class FansDBAPIImpl extends AbsDbAPI<List<CommUser>> implements FansDBAPI {

    @Override
    public void loadFansFromDB(final String uid,
            final SimpleFetchListener<List<CommUser>> listener) {

        submit(new DbCommand() {

            @Override
            protected void execute() {
                DBRelationOP<List<CommUser>> fans = EntityRelationFactory.createUserFans(
                        uid, "");
                List<CommUser> result = fans.queryById(uid);
                deliverResult(listener, result);
            }
        });

    }

    @Override
    public void saveFansToDB(final String uid, final CommUser fans) {
        submit(new DbCommand() {

            @Override
            protected void execute() {
                fans.saveEntity();
                DBRelationOP<?> relationOP = EntityRelationFactory.createUserFans(uid, fans.id);
                relationOP.saveEntity();
            }
        });
    }

    @Override
    public void deleteFansFromDB(final String uid) {
        submit(new DbCommand() {

            @Override
            protected void execute() {
                DBRelationOP<?> fansRecord = EntityRelationFactory.createUserFans();
                fansRecord.deleteById(uid);
            }
        });

    }

    @Override
    public void saveFansToDB(final String uid, List<CommUser> fans) {
        final List<CommUser> newFans = new LinkedList<CommUser>(fans);
        submit(new DbCommand() {

            @Override
            protected void execute() {
                for (CommUser f : newFans) {
                    f.saveEntity();
                    DBRelationOP<?> fansRecord = EntityRelationFactory.createUserFans(
                            uid, f.id);
                    fansRecord.saveEntity();
                }
            }
        });
    }

    @Override
    public void queryFansCount(final String uid, final SimpleFetchListener<Integer> listener) {
        submit(new DbCommand() {

            @Override
            protected void execute() {
                DBRelationOP<?> relationOP = EntityRelationFactory.createUserFans();
                deliverResultForCount(listener, relationOP.queryCountById(uid));
            }
        });
    }
}
