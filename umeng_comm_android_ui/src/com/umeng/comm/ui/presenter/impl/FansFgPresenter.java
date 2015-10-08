/**
 * 
 */

package com.umeng.comm.ui.presenter.impl;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.db.ctrl.FansDBAPI;
import com.umeng.comm.core.db.ctrl.impl.DatabaseAPI;
import com.umeng.comm.core.listeners.Listeners.FetchListener;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.nets.responses.FansResponse;
import com.umeng.comm.core.nets.uitls.NetworkUtils;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.ui.mvpview.MvpFollowedUserView;
import com.umeng.comm.ui.utils.BroadcastUtils.BROADCAST_TYPE;

import java.util.List;

/**
 * 
 */
public class FansFgPresenter extends FollowedUserFgPresenter {

    private FansDBAPI mFansDBAPI;

    /**
     * @param followedUserView
     */
    public FansFgPresenter(MvpFollowedUserView followedUserView, String uid) {
        super(followedUserView, uid);
//        isFollowPage = false;
        mFansDBAPI = DatabaseAPI.getInstance().getFansDBAPI();
    }

    @Override
    public void loadDataFromServer() {
        mCommunitySDK.fetchFans(mUid, new FetchListener<FansResponse>() {

            @Override
            public void onStart() {
                mFollowedUserView.onRefreshStart();
            }

            @Override
            public void onComplete(FansResponse response) {
                // 根据response进行Toast
                if (NetworkUtils.handleResponseAll(response)) {
                    mFollowedUserView.onRefreshEnd();
                    return;
                }
                
                final List<CommUser> fans = response.result;
                // 保存到数据库
                mFansDBAPI.saveFansToDB(mUid, fans);

                // 加载完成后，首先更新粉丝的条数,因为可能在下拉刷新的时候有新的粉丝。
                mFollowedUserView.executeCallback(fans.size());

                // 去重操作
                List<CommUser> dataSource = mFollowedUserView.getBindDataSource();
                fans.removeAll(dataSource);
                dataSource.addAll(fans);
                mFollowedUserView.notifyDataSetChanged();
                // 解析下一页地址
                parseNextpageUrl(response, true);
                mFollowedUserView.onRefreshEnd();
            }
        });
    }

    @Override
    public void loadDataFromDB() {
        if (!mUid.equals(CommConfig.getConfig().loginedUser.id)) {
            return;
        }
        // 加载某个用户的粉丝
        mFansDBAPI.loadFansFromDB(mUid, new
                SimpleFetchListener<List<CommUser>>() {

                    @Override
                    public void onComplete(List<CommUser> results) {
                        if (!CommonUtils.isActivityAlive(convertContextToActivity())) {
                            return;
                        }
                        updateFans(results);
                        mFollowedUserView.executeCallback(results.size());
                    }
                });

    }

    /**
     * 更新粉丝的ListView病保存数据到数据库</br>
     * 
     * @param users
     */
    private void updateFans(final List<CommUser> users) {

        if (users == null || users.size() <= 0) {
            return;
        }
        List<CommUser> dataSource = mFollowedUserView.getBindDataSource();
        users.removeAll(dataSource);
        dataSource.addAll(users);
        mFollowedUserView.notifyDataSetChanged();
    }
    
    @Override
    protected void onUserFollowStateChange(CommUser user, BROADCAST_TYPE type) {
    }

}
