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

import android.os.Handler;

import com.activeandroid.ActiveAndroid;
import com.umeng.comm.core.db.engine.DatabaseExecutor;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;

/**
 * 数据库操作抽象类,封装数据库操作、回调流程
 * 
 * @param <T> 操作的数据类型,一般为List的数据集合
 */
public abstract class AbsDbAPI<T> {
    DatabaseExecutor mDbDispatcher = DatabaseExecutor.getExecutor();
    Handler mHandler = mDbDispatcher.getUIHandler();

    /**
     * 提交数据库命令
     * 
     * @param runnable
     */
    public void submit(DbCommand cmd) {
        mDbDispatcher.submit(cmd);
    }

    protected void deliverResult(final SimpleFetchListener<T> listener, final T t) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                listener.onComplete(t);
            }
        });
    }

    protected void deliverResultForCount(final SimpleFetchListener<Integer> listener,
            final Integer count) {
        mHandler.post(new Runnable() {

            @Override
            public void run() {
                listener.onComplete(count);
            }
        });
    }

    /**
     * 数据库命令类,封装事务处理,提升效率
     */
    public static abstract class DbCommand implements Runnable {
        @Override
        public final void run() {
            ActiveAndroid.beginTransaction();
            execute();
            ActiveAndroid.setTransactionSuccessful();
            ActiveAndroid.endTransaction();
        }

        protected abstract void execute();
    }
}
