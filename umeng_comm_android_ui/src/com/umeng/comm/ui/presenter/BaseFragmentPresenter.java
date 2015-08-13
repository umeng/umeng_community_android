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

package com.umeng.comm.ui.presenter;

import android.content.Context;

/**
 * Fragment的Presenter
 */
public abstract class BaseFragmentPresenter<T> extends BasePresenter {

    /**
     * id.该id表示获取某一资源的id。比如获取某个用户（id）的feed
     */
    protected String mId;

    public void setId(String id) {
        this.mId = id;
    }

    @Override
    public void attach(Context context) {
        super.attach(context);
        loadDataFromDB();
        loadDataFromServer();
    }

    /**
     * 从server加载数据</br>
     */
    public void loadDataFromServer() {

    }

    /**
     * 从数据库中加载数据</br>
     */
    public void loadDataFromDB() {

    }

    /**
     * 加载更多数据</br>
     */
    public void loadMoreData() {
    }

    /**
     * 保存数据到DB</br>
     * 
     * @param t
     */
    protected void saveDataToDB(T datas) {

    }

}
