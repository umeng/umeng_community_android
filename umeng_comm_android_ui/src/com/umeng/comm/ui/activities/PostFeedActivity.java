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

package com.umeng.comm.ui.activities;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.CommUser.Gender;
import com.umeng.comm.core.beans.CommUser.Permisson;
import com.umeng.comm.core.beans.FeedItem;
import com.umeng.comm.core.beans.ImageItem;
import com.umeng.comm.core.beans.LocationItem;
import com.umeng.comm.core.beans.Topic;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.listeners.Listeners.SimpleFetchListener;
import com.umeng.comm.core.sdkmanager.ImagePickerManager;
import com.umeng.comm.core.utils.DeviceUtils;
import com.umeng.comm.core.utils.Log;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.adapters.ImageSelectedAdapter;
import com.umeng.comm.ui.dialogs.AtFriendDialog;
import com.umeng.comm.ui.dialogs.LocationPickerDlg;
import com.umeng.comm.ui.fragments.TopicPickerFragment;
import com.umeng.comm.ui.fragments.TopicPickerFragment.ResultListener;
import com.umeng.comm.ui.mvpview.MvpPostFeedActivityView;
import com.umeng.comm.ui.presenter.impl.FeedPostPresenter;
import com.umeng.comm.ui.presenter.impl.TakePhotoPresenter;
import com.umeng.comm.ui.utils.ContentChecker;
import com.umeng.comm.ui.utils.FeedViewRender;
import com.umeng.comm.ui.widgets.FeedEditText;
import com.umeng.comm.ui.widgets.TopicTipView;

/**
 * 发布feed的Activity
 */
public class PostFeedActivity extends BaseFragmentActivity implements OnClickListener,
        MvpPostFeedActivityView {

    /**
     * 内容编辑框，最多300字
     */
    protected FeedEditText mEditText;

    FrameLayout mFragmentLatout;

    /**
     * 选择的图片的GridView
     */
    protected GridView mGridView;
    /**
     * 保存已经选择的图片的路径
     */
    // protected List<String> mImageSelectedAdapter.getDataSource() = new
    // ArrayList<String>();
    /**
     * 显示已经选择的图片的Adapter
     */
    private ImageSelectedAdapter mImageSelectedAdapter;
    /**
     * 位置
     */
    protected Location mLocation;
    /**
     * 通过拍照获取到的图片地址
     */
    // private String mNewImagePath;
    /**
     * 已选择的话题
     */
    protected List<Topic> mSelecteTopics = new ArrayList<Topic>();

    /**
     * 保存已经@的好友
     */
    protected List<CommUser> mSelectFriends = new ArrayList<CommUser>();
    /**
     * 我的位置TextView
     */
    protected TextView mLocationTv;
    /**
     * 选择好友的dialog
     */
    private AtFriendDialog mAtFriendDlg;
    /**
     * 地理位置选择dialog
     */
    private LocationPickerDlg mLocationPickerDlg;
    /**
     * 保存地理位置的list
     */
    protected List<LocationItem> mLocationItems = new ArrayList<LocationItem>();
    /**
     * 选择话题的Fragment
     */
    private TopicPickerFragment mTopicFragment;
    /**
     * 选择话题的ToggleButton
     */
    private ToggleButton mTopicButton;
    /**
     * 选择图片的ImageButton
     */
    private ImageButton mPhotoButton;
    /**
     * 地理位置的icon
     */
    private ImageView mLocIcon;

    private View mLocationLayout;
    /**
     * 加载地理位置时的Progress
     */
    private ProgressBar mLocProgressBar;

    protected String TAG = PostFeedActivity.class.getSimpleName();

    /**
     * 是否是发布失败重新发布
     */
    private boolean isRepost = false;

    private static final String CHAR_WELL = "#";
    private static final String CHAR_AT = "@";
    protected boolean isForwardFeed = false;

    protected TopicTipView mTopicTipView;

    private static final String EDIT_CONTENT_KEY = "edit_content_key";

    FeedPostPresenter mPostPresenter;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        setContentView(ResFinder.getLayout("umeng_comm_post_feed_layout"));
        setFragmentContainerId(ResFinder.getId("umeng_comm_select_layout"));
        initViews();
        initLocationLayout();
        initPresenter();
        Bundle extraBundle = getIntent().getExtras();
        if (extraBundle == null) {
            return;
        }
        isRepost = extraBundle.getBoolean(Constants.POST_FAILED, false);
        // 设置是否是重新发送
        mPostPresenter.setRepost(isRepost);
        // 从话题详情页面进入到发送新鲜事页面
        Topic mTopic = extraBundle.getParcelable(Constants.TAG_TOPIC);
        if (mTopic != null) {
            mSelecteTopics.add(mTopic);
            if (CommConfig.getConfig().isDisplayTopicOnPostFeedPage()) {// 检查是否在编辑框添加此话题
                mEditText.insertTopics(mSelecteTopics);
            }
            startFadeOutAnimForTopicTipView();
        }
    }

    private void initPresenter() {
        mPostPresenter = new FeedPostPresenter(this, new ContentChecker(mSelecteTopics,
                mSelectFriends));
        mPostPresenter.attach(this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String content = savedInstanceState.getString(EDIT_CONTENT_KEY);
        if (!TextUtils.isEmpty(content)) {
            mEditText.setText(content);
        }
    }

    private boolean isCharsOverflow(String extraText) {
        int extraTextLen = 0;
        if (!TextUtils.isEmpty(extraText)) {
            extraTextLen = extraText.length();
        }
        int len = mEditText.getText().length();
        return len + extraTextLen >= CommConfig.getConfig().mFeedLen;
    }

    /**
     * 初始化相关View
     */
    protected void initViews() {
        // 发送和回退按钮
        findViewById(ResFinder.getId("umeng_comm_post_ok_btn")).setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_comm_post_back_btn")).setOnClickListener(this);

        mLocProgressBar = (ProgressBar) findViewById(ResFinder.getId(
                "umeng_comm_post_loc_progressbar"));

        mLocIcon = (ImageView) findViewById(ResFinder.getId("umeng_comm_post_loc_icon"));
        mLocIcon.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                initLocationLayout();
            }
        });
        mLocationTv = (TextView) findViewById(ResFinder.getId("umeng_comm_location_text"));
        mLocationLayout = findViewById(
                ResFinder.getId("umeng_community_loc_layout"));

        initEditView();
        // 以下四个按钮分别是选择话题、添加图片、选择位置、@好友
        findViewById(ResFinder.getId("umeng_comm_take_photo_btn")).setOnClickListener(this);
        findViewById(ResFinder.getId("umeng_comm_select_location_btn")).setOnClickListener(
                this);
        findViewById(ResFinder.getId("umeng_comm_at_friend_btn")).setOnClickListener(this);

        mPhotoButton = (ImageButton) findViewById(ResFinder
                .getId("umeng_comm_add_image_btn"));
        mPhotoButton.setOnClickListener(this);
        mTopicButton = (ToggleButton) findViewById(ResFinder.getId(
                "umeng_comm_pick_topic_btn"));
        mTopicButton.setOnClickListener(this);

        mFragmentLatout = (FrameLayout)
                findViewById(ResFinder.getId("umeng_comm_select_layout"));
        mGridView = (GridView) findViewById(ResFinder.getId("umeng_comm_prev_images_gv"));
        initSelectedImageAdapter();
        mTopicTipView = (TopicTipView) findViewById(ResFinder.getId("umeng_comm_topic_tip"));
        if (CommConfig.getConfig().loginedUser.gender == Gender.FEMALE) {// 根据性别做不同的提示
            mTopicTipView.setText(ResFinder.getString("umeng_comm_topic_tip_female"));
        }
        if (!isForwardFeed) {
            startAnimationForTopicTipView();
        }
    }

    /**
     * 为话题提示VIew绑定动画</br>
     */
    private void startAnimationForTopicTipView() {
        int timePiece = 500;
        int repeatCount = 4;
        int startDeny = 50;
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 10, 0);
        translateAnimation.setRepeatMode(Animation.REVERSE);
        // translateAnimation.setStartOffset(startDeny * repeatCount+timePiece);
        translateAnimation.setRepeatCount(Integer.MAX_VALUE);
        translateAnimation.setDuration(timePiece);

        AlphaAnimation alphaAnimationIn = new AlphaAnimation(0, 1.0f);
        alphaAnimationIn.setDuration(timePiece);
        alphaAnimationIn.setStartOffset(startDeny * repeatCount);

        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(alphaAnimationIn);
        animationSet.addAnimation(translateAnimation);
        // animationSet.addAnimation(alphaAnimationOut);
        // animationSet.setFillAfter(true);
        mTopicTipView.startAnimation(animationSet);
    }

    /**
     * 启动淡出动画</br>
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void startFadeOutAnimForTopicTipView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (mTopicTipView.getAlpha() < 0.1f) {
                return;
            }
        }

        AlphaAnimation alphaAnimationOut = new AlphaAnimation(1.0f, 0);
        alphaAnimationOut.setDuration(300);
        alphaAnimationOut.setFillAfter(true);
        mTopicTipView.startAnimation(alphaAnimationOut);
    }

    /**
     * 位置是否已经初始化</br>
     * 
     * @return
     */
    private boolean isLocationInited() {
        return mLocation != null && mLocationItems.size() > 1;
    }

    /**
     * 获取地理位置信息。
     */
    protected void initLocationLayout() {
        // 如果地理位置信息已经初始化，则不再重复获取
        if (isLocationInited()) {
            return;
        }
        mLocProgressBar.setVisibility(View.VISIBLE);
        mLocIcon.setVisibility(View.GONE);
        mLocationTv.setText(ResFinder.getString("umeng_comm_fetching_loc"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        showInputMethod(mEditText);
        mImageLoader.resume();
        mTakePhotoPresenter.attach(this);
    }

    private void showKeyboard() {
        mFragmentLatout.setVisibility(View.GONE);
        showInputMethod(mEditText);
    }

    /**
     * 初始化EditView并设置回调</br>
     */
    private void initEditView() {

        mEditText = (FeedEditText) findViewById(ResFinder.getId(
                "umeng_comm_post_msg_edittext"));
        mEditText.setFocusableInTouchMode(true);
        mEditText.requestFocus();
        mEditText.setMinimumHeight(DeviceUtils.dp2px(this, 150));

        mEditText.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mEditText.mCursorIndex = mEditText.getSelectionStart();
                mFragmentLatout.setVisibility(View.GONE);
                mTopicButton.setChecked(false);
            }
        });

        mEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 1) {
                    String newChar = s.subSequence(start, start + count).toString();
                    // 转发时不显示话题
                    if (CHAR_WELL.equals(newChar) && !isForwardFeed) {
                        showTopicFragment();
                    } else if (CHAR_AT.equals(newChar)) {
                        showAtFriendsDialog();
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    /**
     * 从TextView获取位置text。该值是在获取地理位置信息时设置。</br>
     * 
     * @return
     */
    protected String getLocationAddr() {
        String locString = mLocationTv.getText().toString().trim();
        String fetchFailed = ResFinder.getString("umeng_comm_fetching_loc_failed");
        String fetching = ResFinder.getString("umeng_comm_fetching_loc");
        String dontShowLoc = ResFinder.getString("umeng_comm_text_dont_show_location");
        // 该判断需要重构
        if (locString.equals(fetchFailed)
                || locString.equals(fetching)
                || locString.equals("")
                || locString.equals(dontShowLoc)
                || mLocationLayout.getVisibility() == View.INVISIBLE) {
            return "";
        }
        return locString;
    }

    /**
     * 准备feed数据</br>
     */
    protected FeedItem prepareFeed() {
        FeedItem mNewFeed = new FeedItem();
        mNewFeed.text = mEditText.getText().toString().trim();
        mNewFeed.locationAddr = getLocationAddr();
        mNewFeed.location = mLocation;

        // 移除添加图标
        mImageSelectedAdapter.getDataSource().remove(Constants.ADD_IMAGE_PATH_SAMPLE);

        for (String url : mImageSelectedAdapter.getDataSource()) {
            // 图片地址
            mNewFeed.imageUrls.add(new ImageItem("", "", url));
        }

        // 话题
        mNewFeed.topics.addAll(mSelecteTopics);
        // @好友
        mNewFeed.atFriends.addAll(mSelectFriends);
        // 发表的用户
        mNewFeed.creator = CommConfig.getConfig().loginedUser;
        mNewFeed.type = mNewFeed.creator.permisson == Permisson.ADMIN ? 1 : 0;
        Log.d(TAG, " @@@ my new Feed = " + mNewFeed);
        return mNewFeed;
    }

    /**
     * 清除状态</br>
     */
    @Override
    public void clearState() {
        mEditText.setText("");
        mEditText.mAtMap.clear();
        mEditText.mTopicMap.clear();
        mImageSelectedAdapter.getDataSource().clear();
        mTopicButton.setChecked(false);
        // mPhotoButton.setChecked(false);
    }

    @Override
    public void restoreFeedItem(FeedItem feedItem) {
        mEditText.setText(feedItem.text);
        mLocationTv.setText(feedItem.locationAddr);
        mImageSelectedAdapter.getDataSource().clear();

        int count = feedItem.imageUrls.size();
        for (int i = 0; i < count; i++) {
            // 图片
            mImageSelectedAdapter.getDataSource().add(feedItem.imageUrls.get(i).originImageUrl);
        }
        // 图片
        if (mImageSelectedAdapter.getDataSource().size() < 9) {
            mImageSelectedAdapter.getDataSource().add(Constants.ADD_IMAGE_PATH_SAMPLE);
        }
        mImageSelectedAdapter.notifyDataSetChanged();
        // 好友
        mSelectFriends.addAll(feedItem.atFriends);
        // 话题
        mSelecteTopics.addAll(feedItem.topics);
        FeedViewRender.parseTopicsAndFriends(mEditText, feedItem);
        // 设置光标位置
        mEditText.setSelection(mEditText.getText().length());
    }

    @Override
    public void changeLocLayoutState(Location location, List<LocationItem> locationItems) {
        mLocation = location;
        mLocationItems = locationItems;

        mLocProgressBar.setVisibility(View.GONE);
        mLocIcon.setVisibility(View.VISIBLE);
        // 设置我的位置,我的位置放在第1个索引的位置
        if (locationItems.size() > 0 && location != null) {
            mLocationTv.setText(locationItems.get(0).description);
        } else {
            mLocationTv.setText(ResFinder.getString("umeng_comm_fetching_loc_failed"));
        }
    }

    /**
     * 用户点击back按钮。</br>
     */
    private void dealBackLogic() {
        mFragmentLatout.setVisibility(View.GONE);
        hideInputMethod(mEditText);
        mPostPresenter.handleBackKeyPressed();
        this.finish();
    }

    TakePhotoPresenter mTakePhotoPresenter = new TakePhotoPresenter();

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (ResFinder.getId("umeng_comm_post_ok_btn") == id) { // 点击发送按钮
            postFeed(prepareFeed());
        } else if (ResFinder.getId("umeng_comm_post_back_btn") == id) { // 点击back按钮
            dealBackLogic();
        } else if (ResFinder.getId("umeng_comm_take_photo_btn") == id) { // 拍照按钮
            mTakePhotoPresenter.takePhoto();
            changeButtonStatus(false, false);
        } else if (ResFinder.getId("umeng_comm_select_location_btn") == id) { // 选择位置
            showLocPickerDlg();
            changeButtonStatus(false, false);
        } else if (ResFinder.getId("umeng_comm_add_image_btn") == id) { // 添加图片
            pickImages();
            changeButtonStatus(true, false);
        } else if (ResFinder.getId("umeng_comm_at_friend_btn") == id) { // @好友
            showAtFriendsDialog();
            changeButtonStatus(false, false);
        } else if (ResFinder.getId("umeng_comm_pick_topic_btn") == id) { // 选择话题
            showTopicFragment();
            changeButtonStatus(false, true);
        }
    }

    private void pickImages() {
        ImagePickerManager
                .getInstance()
                .getCurrentSDK()
                .jumpToPickImagesPage(this,
                        (ArrayList<String>) mImageSelectedAdapter.getDataSource());
    }

    protected void postFeed(FeedItem feedItem) {
        mPostPresenter.postNewFeed(feedItem);
    }

    @Override
    public void startPostFeed() {
        hideInputMethod(mEditText);
        finish();
    }

    /**
     * 设置PhotoButton跟TopicButton的选中状态</br>
     * 
     * @param photoSelected
     * @param topicSelected
     */
    private void changeButtonStatus(boolean photoSelected, boolean topicSelected) {
        // mPhotoButton.setChecked(photoSelected);
        mTopicButton.setChecked(topicSelected);
    }

    /**
     * 显示选择话题的Fragment</br>
     */
    private void showTopicFragment() {
        mFragmentLatout.setVisibility(View.VISIBLE);
        hideInputMethod(mEditText);

        if (mTopicFragment == null) {
            mTopicFragment = new TopicPickerFragment();
        }
        showFragment(mTopicFragment);
        // 新增话题的回调
        mTopicFragment.addTopicListener(new ResultListener<Topic>() {

            @Override
            public void onRemove(Topic topic) {
                mEditText.removeTopic(topic);
            }

            @Override
            public void onAdd(Topic topic) {
                Log.d(TAG, "### topic = " + topic);
                if (isCharsOverflow(topic.name)) {
                    ToastMsg.showShortMsgByResName("umeng_comm_overflow_tips");
                    return;
                }
                if (!mEditText.mTopicMap.containsValue(topic)) {
                    removeChar('#');
                    List<Topic> topics = new ArrayList<Topic>();
                    topics.add(topic);
                    mEditText.insertTopics(topics);
                    mSelecteTopics.add(topic);
                    startFadeOutAnimForTopicTipView();
                }

                showKeyboard();
            }
        });

        // 删除话题时的回调
        mEditText.setTopicListener(new ResultListener<Topic>() {

            @Override
            public void onRemove(Topic topic) {
                mTopicFragment.uncheckTopic(topic);
                if (mEditText.mTopicMap.size() == 0 && !isForwardFeed) {
                    startAnimationForTopicTipView();
                }
            }

            @Override
            public void onAdd(Topic topic) {

            }
        });

    }

    // 隐藏Fragment的回调
    OnClickListener hideFragmentClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            mFragmentLatout.setVisibility(View.GONE);
        }
    };

    /**
     * 已选择图片显示的Adapter
     * 
     * @param listener 删除某张图片的回调
     */
    private void initSelectedImageAdapter() {
        mImageSelectedAdapter = new ImageSelectedAdapter(PostFeedActivity.this);
        mImageSelectedAdapter.getDataSource().add(Constants.ADD_IMAGE_PATH_SAMPLE);
        // 设置选择item时得背景为透明
        mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mGridView.setAdapter(mImageSelectedAdapter);
        mGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {
                String clickImageUrl = mImageSelectedAdapter.getItem(position);
                boolean isAddImage = Constants.ADD_IMAGE_PATH_SAMPLE.equals(clickImageUrl);
                if (isAddImage) { // 如果触发的是添加图片事件，则显示选择图片的Fragment
                    pickImages();
                }
            }
        });

    }

    /**
     * 显示选择地理位置的Dialog</br>
     */
    private void showLocPickerDlg() {

        if (mLocationPickerDlg == null) {
            mLocationPickerDlg = new LocationPickerDlg(this, ResFinder.getStyle(
                    "umeng_comm_dialog_fullscreen"));
        }
        mLocationPickerDlg.setOwnerActivity(PostFeedActivity.this);
        mLocationPickerDlg.setupMyLocation(mLocation, mLocationItems);
        // 数据获取监听器
        mLocationPickerDlg.setDataListener(new SimpleFetchListener<LocationItem>() {

            @Override
            public void onComplete(LocationItem data) {
                if (data != null && !TextUtils.isEmpty(data.description)) {
                    mLocationLayout.setVisibility(View.VISIBLE);
                    // 地理位置数据
                    mLocationTv.setText(data.description);
                } else {
                    mLocationLayout.setVisibility(View.INVISIBLE);
                }

                // 显示输入框
                showKeyboard();
            }
        });
        mLocationPickerDlg.show();
    }

    /**
     * 显示@好友列表的Dialog</br>
     */
    private void showAtFriendsDialog() {

        if (mAtFriendDlg == null) {
            mAtFriendDlg = new AtFriendDialog(PostFeedActivity.this, ResFinder.getStyle(
                    "umeng_comm_dialog_fullscreen"));
        }
        mAtFriendDlg.setOwnerActivity(PostFeedActivity.this);
        // 数据获取监听器
        mAtFriendDlg.setDataListener(new SimpleFetchListener<CommUser>() {

            @Override
            public void onComplete(CommUser user) {
                if (user != null) {
                    if (isCharsOverflow(user.name)) {
                        ToastMsg.showShortMsgByResName("umeng_comm_overflow_tips");
                        return;
                    }
                    removeChar('@');
                    mSelectFriends.add(user);
                    // 插入数据
                    mEditText.atFriends(mSelectFriends);
                }
                // // 显示输入框
                showKeyboard();
            }
        });

        mEditText.setResultListener(new ResultListener<CommUser>() {

            @Override
            public void onAdd(CommUser t) {

            }

            @Override
            public void onRemove(CommUser friend) {
                mSelectFriends.remove(friend);
            }
        });
        mAtFriendDlg.show();
    }

    /**
     * 移除字符</br>
     * 
     * @param c
     */
    private void removeChar(char c) {
        Editable editable = mEditText.getText();
        if (editable.length() <= 0) {
            return;
        }
        if (editable.charAt(editable.length() - 1) == c) {
            editable.delete(editable.length() - 1, editable.length());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mFragmentLatout.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTakePhotoPresenter.detach();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String content = mEditText.getText().toString();
        if (!TextUtils.isEmpty(content)) {
            outState.putString(EDIT_CONTENT_KEY, content);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != RESULT_OK) {
            return;
        }

        // 处理图片选择
        onPickedImages(requestCode, data);
        // 将拍照得到的图片添加到gallery中, 并且显示到GridView中
        onTakedPhoto(requestCode);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void onPickedImages(int requestCode, Intent data) {
        if (requestCode == Constants.PICK_IMAGE_REQ_CODE) {// selected image
            if (data != null && data.getExtras() != null) {
                mImageSelectedAdapter.getDataSource().clear();
                // 获取选中的图片
                List<String> selectedList = ImagePickerManager.getInstance().getCurrentSDK()
                        .parsePickedImageList(data);
                updateImageList(selectedList);
            }
        }
    }

    private void updateImageList(List<String> selectedList) {
        appendAddImageIfLessThanNine(selectedList);
        mImageSelectedAdapter.updateListViewData(selectedList);
    }

    /**
     * Add the picture to the photo gallery. Must be called on all camera images
     * or they will disappear once taken.
     */
    protected void onTakedPhoto(int requestCode) {

        if (requestCode != TakePhotoPresenter.REQUEST_IMAGE_CAPTURE) {
            return;
        }

        String imgUri = mTakePhotoPresenter.updateImageToMediaLibrary();

        List<String> selectedList = mImageSelectedAdapter.getDataSource();
        // 更新媒体库
        selectedList.remove(Constants.ADD_IMAGE_PATH_SAMPLE);
        if (selectedList.size() < 9) {
            selectedList.add(imgUri);
            appendAddImageIfLessThanNine(selectedList);
        } else {
            ToastMsg.showShortMsgByResName("umeng_comm_image_overflow");
        }
        mImageSelectedAdapter.notifyDataSetChanged();
    }

    /**
     * 如果选中的图片小于九张那么在最后添加一个"添加"图片
     * 
     * @param selectedList
     */
    private void appendAddImageIfLessThanNine(List<String> selectedList) {
        if (selectedList.size() < 9) {
            selectedList.add(Constants.ADD_IMAGE_PATH_SAMPLE);
        }
    }

    @Override
    public void canNotPostFeed() {
        if (mImageSelectedAdapter.getCount() < 9) {
            mImageSelectedAdapter.addData(Constants.ADD_IMAGE_PATH_SAMPLE);
        }
    }

}
