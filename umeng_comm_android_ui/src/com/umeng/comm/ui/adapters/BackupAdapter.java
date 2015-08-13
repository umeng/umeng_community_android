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

package com.umeng.comm.ui.adapters;

import android.content.Context;

import com.umeng.comm.core.utils.Log;
import com.umeng.comm.ui.adapters.viewholders.ViewParser;

import java.util.ArrayList;
import java.util.List;

/**
 * 可备份数据的Adapter,用户搜索时存储原始数据。
 * 
 * @param <T> 数据类型
 * @param <H> ViewHolder类型
 */
public abstract class BackupAdapter<T, H extends ViewParser> extends CommonAdapter<T, H> {

    final List<T> mBackupData = new ArrayList<T>();

    /**
     * @param context
     * @param data
     */
    public BackupAdapter(Context context) {
        super(context);
    }

    /**
     * 保存原始数据列表,用于搜索功能
     */
    public void backupData() {
        if (mBackupData.size() == 0) {
            mBackupData.clear();
            mBackupData.addAll(mDataSet);
            Log.d("", "### backup : " + mBackupData.toString());
        }
    }

    /**
     * 回复原始数据,与backupData配套使用
     */
    public void restoreData() {
        mDataSet.clear();
        mDataSet.addAll(mBackupData);
        mBackupData.clear();
        notifyDataSetChanged();
    }

}
