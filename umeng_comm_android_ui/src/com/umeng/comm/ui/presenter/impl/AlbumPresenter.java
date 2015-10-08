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

package com.umeng.comm.ui.presenter.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.umeng.comm.core.beans.AlbumItem;
import com.umeng.comm.core.beans.ImageItem;
import com.umeng.comm.core.constants.ErrorCode;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.AlbumResponse;
import com.umeng.comm.core.nets.uitls.NetworkUtils;
import com.umeng.comm.ui.mvpview.MvpAlbumView;
import com.umeng.comm.ui.presenter.BaseActivityPresenter;
import com.umeng.comm.ui.presenter.BasePresenter;

public class AlbumPresenter extends BasePresenter implements BaseActivityPresenter {

    String mUid;
    MvpAlbumView mAlbumView;
    String mNextPage;

    private volatile AtomicBoolean mUpdateNextUrl = new AtomicBoolean(true);

    public AlbumPresenter(String uid, MvpAlbumView view) {
        mUid = uid;
        mAlbumView = view;
    }

    @Override
    public void attach(Context context) {
        super.attach(context);
        loadDataFromServer();
    }

    public void loadDataFromServer() {
        mCommunitySDK.fetchAlbums(mUid, new FetchListener<AlbumResponse>() {

            @Override
            public void onStart() {
            }

            @Override
            public void onComplete(AlbumResponse response) {
                if (NetworkUtils.handleResponseAll(response)) {
                    return;
                }
                if (TextUtils.isEmpty(mNextPage) && mUpdateNextUrl.get()) {
                    mNextPage = response.nextPageUrl;
                    mUpdateNextUrl.set(false);
                }
                deliveryImageItems(response);
            }
        });
    }

    private void deliveryImageItems(AlbumResponse response) {
        if (NetworkUtils.handleResponseComm(response)) {
            return;
        }
        // 从相册中解析图片,目前一个相册只有一张图,将结果回调给View
        mAlbumView.fetchedAlbums(parseNewImageItem(response));
    }

    private List<ImageItem> parseNewImageItem(AlbumResponse response) {
        List<ImageItem> newItems = new ArrayList<ImageItem>();
        for (AlbumItem albumItem : response.result) {
            newItems.addAll(albumItem.images);
        }
        // 去除已经存在的数据项
        newItems.removeAll(mAlbumView.getBindDataSource());
        return newItems;
    }

    public void loadMore() {
        if (TextUtils.isEmpty(mNextPage)) {
            return;
        }
        mCommunitySDK.fetchNextPageData(mNextPage, AlbumResponse.class,
                new SimpleFetchListener<AlbumResponse>() {

                    @Override
                    public void onComplete(AlbumResponse response) {
                        if (NetworkUtils.handleResponseAll(response)) {
                            if (response.errCode == ErrorCode.NO_ERROR) {
                                mNextPage = "";
                            }
                            return;
                        }
                        mNextPage = response.nextPageUrl;
                        if (!TextUtils.isEmpty(mNextPage) && "null".equals(mNextPage)) {
                            mNextPage = "";
                        }
                        deliveryImageItems(response);
                    }
                });
    }

    @Override
    public void onCreate(Bundle bundle) {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroy() {
        mAlbumView = null;
    }
}
