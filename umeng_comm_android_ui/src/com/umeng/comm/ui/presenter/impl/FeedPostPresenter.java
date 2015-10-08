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

import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.CommUser.SubPermission;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.beans.ImageItem;
import com.umeng.comm.core.beans.LocationItem;
import com.umeng.comm.core.constants.ErrorCode;
import com.umeng.comm.core.image.ImageUploader;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.FeedItemResponse;
import com.umeng.comm.core.nets.responses.LocationResponse;
import com.umeng.comm.core.nets.uitls.NetworkUtils;
import com.umeng.comm.core.sdkmanager.ImageUploaderManager;
import com.umeng.comm.core.sdkmanager.LocationSDKManager;
import com.umeng.comm.core.utils.DeviceUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.dialogs.ConfirmDialog;
import com.umeng.comm.ui.mvpview.MvpPostFeedActivityView;
import com.umeng.comm.ui.notifycation.PostNotifycation;
import com.umeng.comm.ui.presenter.BasePresenter;
import com.umeng.comm.ui.utils.BroadcastUtils;
import com.umeng.comm.ui.utils.ContentChecker;
import com.umeng.comm.ui.utils.FeedMemento;

import java.util.ArrayList;
import java.util.List;

public class FeedPostPresenter extends BasePresenter {

    /**
     * 内存检测,发布feed时自己输入的字符数量需要大于5个
     */
    ContentChecker mChecker;
    /**
     * 地理位置信息
     */
    Location mLocation;
    /**
     * 获取到的位置信息
     */
    List<LocationItem> mLocationItems = new ArrayList<LocationItem>();
    /**
     * 是否是转发类型
     */
    boolean isForwardFeed = false;
    /**
     * 是否是重新发送
     */
    boolean isRepost = false;
    /**
     * 对应的Mvp View
     */
    MvpPostFeedActivityView mActivityView;

    public FeedPostPresenter(MvpPostFeedActivityView activityView, ContentChecker checker) {
        mChecker = checker;
        mActivityView = activityView;
    }

    @Override
    public void attach(Context context) {
        super.attach(context);
        getMyLocation();
    }

    public void postNewFeed(final FeedItem feedItem) {
        if (!DeviceUtils.isNetworkAvailable(mContext)) {
            ToastMsg.showShortMsgByResName("umeng_comm_not_network");
            mActivityView.canNotPostFeed();
            return;
        }
        if (!hasContent(feedItem)) {
            ToastMsg.showShortMsgByResName("umeng_comm_not_network");
            mActivityView.canNotPostFeed();
            return;
        }

        if (!isTextValid(feedItem)) {
            ToastMsg.showShortMsgByResName("umeng_comm_content_short_tips");
            mActivityView.canNotPostFeed();
            return;
        }

        CommUser loginUser = CommConfig.getConfig().loginedUser;
        if (!isForwardFeed && loginUser.subPermissions.contains(SubPermission.BULLETIN)) { // 当前用户有公告权限，此时需要确认是否发送为公告
            String msg = ResFinder.getString("umeng_comm_bulletin_tips");
            DialogInterface.OnClickListener confirmListener = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    executeRealPostFeed(feedItem, true);
                }
            };
            DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    executeRealPostFeed(feedItem, false);
                }
            };

            ConfirmDialog.showDialog(mContext, msg, confirmListener, cancelListener);
        } else { // 普通feed
            executeRealPostFeed(feedItem, false);
        }
    }

    public void setRepost(boolean isRepost) {
        this.isRepost = isRepost;
        if (isRepost && mActivityView != null) {
            mActivityView.restoreFeedItem(FeedMemento.restoreMemento(mContext));
        }
    }

    public void setForwardFeed(boolean isForwardFeed) {
        this.isForwardFeed = isForwardFeed;
    }

    /**
     * 执行整整的发送请求</br>
     */
    private void executeRealPostFeed(FeedItem feedItem, boolean isbulletin) {
        executePostFeed(feedItem, isbulletin);
        if (isRepost) {
            mCommunitySDK.openCommunity(mContext);
        }
        mActivityView.startPostFeed();
    }

    /**
     * 发布新的feed</br>
     */
    private void executePostFeed(final FeedItem feedItem, boolean isbulletin) {
        feedItem.type = isbulletin ? 1 : 0;
        new PostFeedTask(feedItem).execute();
    }

    protected void postFeedResponse(FeedItemResponse response, FeedItem feedItem) {
        if (mActivityView != null
                && NetworkUtils.handleResponseComm(response)) {
            PostNotifycation.showPostNotifycation(mContext, ResFinder.getString(
                    "umeng_comm_send_failed"),
                    feedItem.text);
            return;
        }

        if (response.errCode == ErrorCode.NO_ERROR) {
            ToastMsg.showShortMsgByResName("umeng_comm_send_success");
            PostNotifycation.clearPostNotifycation(mContext);
            FeedMemento.clear(mContext);
            // 发送广播
            BroadcastUtils.sendFeedPostBroadcast(mContext, feedItem);
        }
    }

    /**
     * 检查除了好友、话题之外字符数是否大于等于5,对于转发无这个要求
     * 
     * @return
     */
    protected boolean isTextValid(FeedItem feedItem) {
        return mChecker.isValidText(feedItem.text);
    }

    /**
     * 检查分享内容是否有效</br>
     * 
     * @return
     */
    protected boolean hasContent(FeedItem feedItem) {
        return feedItem.text.trim().length() > 0;
    }

    public void getMyLocation() {
        LocationSDKManager.getInstance().getCurrentSDK()
                .requestLocation(mContext, new SimpleFetchListener<Location>() {

                    @Override
                    public void onComplete(Location result) {
                        mLocation = result;
                        if (mLocation != null) {
                            // 获取详细的信息
                            getLocationDetailAddr();
                        } else {
                            // 修改位置信息的状态
                            mActivityView.changeLocLayoutState(mLocation, mLocationItems);
                        }
                    }
                });
    }

    /**
     * 获取地理位置详细信息</br>
     */
    private void getLocationDetailAddr() {
        mCommunitySDK.getLocationAddr(mLocation,
                new SimpleFetchListener<LocationResponse>() {

                    @Override
                    public void onComplete(LocationResponse response) {
                        mLocationItems.clear();
                        mLocationItems.addAll(response.result);
                        mActivityView.changeLocLayoutState(mLocation, mLocationItems);
                    }
                });
    }

    /**
     * 转发feed
     * 
     * @param feedItem 发布的新feed
     * @param forwardFeedItem 被转发的feed
     */
    public void forwardFeed(FeedItem feedItem, final FeedItem forwardFeedItem) {
        if (!DeviceUtils.isNetworkAvailable(mContext)) {
            ToastMsg.showShortMsgByResName("umeng_comm_not_network");
            return;
        }

        mActivityView.startPostFeed();
        feedItem.type = 0;// 转发的Feed都不能mark为公告，避免数据同步的影响
        mCommunitySDK.forward(feedItem,
                new SimpleFetchListener<FeedItemResponse>() {

                    @Override
                    public void onComplete(FeedItemResponse response) {
                        if (NetworkUtils.handleResponseComm(response)) {
                            Log.w("", "forward error . code = " + response.errCode);
                            return;
                        }
                        // 被转发的原始feed被删除
                        if (response.errCode == ErrorCode.ORIGIN_FEED_DELETE_ERR_CODE
                                || response.errCode == ErrorCode.ERR_CODE_FEED_UNAVAILABLE) {
                            ToastMsg.showShortMsgByResName("umeng_comm_origin_feed_delete");
                            return;
                        }
                        FeedItem feedItem = response.result;
                        // 原始feed不是当前被转发的feed,也就是被转发的feed也是转发feed类型的情况
                        if (!feedItem.sourceFeedId.equals(feedItem.sourceFeed.id)) {
                            // 原始feed 转发数量加1
                            BroadcastUtils.sendFeedUpdateBroadcast(mContext, feedItem.sourceFeed);
                            // 被转发的feed数量加1
                            BroadcastUtils.sendFeedUpdateBroadcast(mContext, forwardFeedItem);
                        }
                        postFeedResponse(response, feedItem);
                    }
                });
    }

    public void handleBackKeyPressed() {
        if (isRepost) {
            mCommunitySDK.openCommunity(mContext);
        }
    }

    /**
     * 存储feed，以便发送失败时进行重发
     * 
     * @param feedItem
     */
    private void saveFeedItem(FeedItem feedItem) {
        // 保存这次要提交的数据，用于发送失败时的重新发送
        FeedMemento.createMemento(mContext, feedItem);
        // 清除状态
        mActivityView.clearState();
        final String title = ResFinder.getString("umeng_comm_send_ing");
        PostNotifycation.showPostNotifycation(mContext, title, feedItem.text);
    }

    /**
     * 发布Feed的异步任务,先上传每张图片,图片传递成功之后再将图片url设置为Feed参数,最后发布feed
     * 
     * @author mrsimple
     */
    class PostFeedTask extends AsyncTask<Void, Void, Boolean> {
        FeedItem mFeedItem;
        // 图片上传组件
        ImageUploader mImageUploader = ImageUploaderManager.getInstance().getCurrentSDK();
        List<ImageItem> uploadedImageItems = new ArrayList<ImageItem>();

        public PostFeedTask(FeedItem feedItem) {
            mFeedItem = feedItem;
        }

        @Override
        protected void onPreExecute() {
            saveFeedItem(mFeedItem);
        }

        private List<String> getImagePathList(List<ImageItem> imageItems) {
            List<String> imagesList = new ArrayList<String>(imageItems.size());
            for (ImageItem item : imageItems) {
                imagesList.add(Uri.parse(item.originImageUrl).getPath());
            }
            return imagesList;
        }

        private boolean uploadFeedImages(List<ImageItem> imageItems) {
            if (!DeviceUtils.isNetworkAvailable(mContext)) {
                return false;
            }
            final List<String> imageUrls = getImagePathList(imageItems);
            uploadedImageItems = mImageUploader.upload(imageUrls);
            // 要上传的数量与传递成功的数量是否相同,即所有图片是否全部上传成功
            return imageItems.size() == uploadedImageItems.size();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return uploadFeedImages(mFeedItem.imageUrls);
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                mFeedItem.imageUrls.clear();
                // 使用上传得到的图片Url更新Feed,因为传递给服务器端的是上传成功后的url
                mFeedItem.imageUrls.addAll(uploadedImageItems);
                mCommunitySDK.postFeed(mFeedItem, postFeedListener);
            } else {
                PostNotifycation.showPostNotifycation(mContext, ResFinder.getString(
                        "umeng_comm_send_failed"), mFeedItem.text);
            }
        }

        SimpleFetchListener<FeedItemResponse> postFeedListener = new SimpleFetchListener<FeedItemResponse>() {

            @Override
            public void onComplete(FeedItemResponse response) {
                postFeedResponse(response, response.result);
            }
        };
    }
}
