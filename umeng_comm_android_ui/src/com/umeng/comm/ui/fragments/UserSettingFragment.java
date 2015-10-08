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

package com.umeng.comm.ui.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.Selection;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.umeng.comm.core.beans.CommConfig;
import com.umeng.comm.core.beans.CommUser;
import com.umeng.comm.core.beans.CommUser.Gender;
import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.imageloader.ImgDisplayOption;
import com.umeng.comm.core.imageloader.UMImageLoader;
import com.umeng.comm.core.sdkmanager.ImageLoaderManager;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ResFinder.ResType;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.activities.BaseFragmentActivity;
import com.umeng.comm.ui.mvpview.MvpUserProfileSettingView;
import com.umeng.comm.ui.presenter.impl.UserSettingPresenter;
import com.umeng.comm.ui.utils.ViewFinder;
import com.umeng.comm.ui.widgets.SquareImageView;

/**
 * 用户设置Fragment
 */
public class UserSettingFragment extends BaseFragment<CommUser, UserSettingPresenter> implements
        OnClickListener, MvpUserProfileSettingView {

    /**
     * ImageLoader
     */
    protected UMImageLoader mImageLoader = ImageLoaderManager.getInstance().getCurrentSDK();
    private EditText mNickNameEtv;
    private TextView mGendertTextView;
    private SquareImageView mIconImg;
    private CommUser mUser;
    private Dialog mDialog;
    private Gender mGender;
    private boolean isFirstSetting = false;// 是否第一次登录跳转到设置页面
    public boolean isRegisterUserNameInvalid = false;
    private ProgressDialog mProgressDialog;

    /**
     * 获取用户设置页面的Fragment</br>
     * 
     * @return
     */
    public static UserSettingFragment getUserSettingFragment() {
        return new UserSettingFragment();
    }

    @Override
    protected int getFragmentLayout() {
        return ResFinder.getLayout("umeng_comm_account_setting");
    }

    @Override
    protected UserSettingPresenter createPresenters() {
        return new UserSettingPresenter(getActivity(), this);
    }

    @Override
    protected void initWidgets() {
        mProgressDialog = new ProgressDialog(getActivity());
        if (mUser == null || TextUtils.isEmpty(mUser.id)) {
            mUser = CommConfig.getConfig().loginedUser;
        }
        mGender = mUser.gender;
        initViews(mRootView);
        mPresenter.setFirstSetting(isFirstSetting);
    }

    /**
     * 初始化相关视图控件
     */
    private void initViews(View rootView) {
        mViewFinder = new ViewFinder(rootView);

        int userIconResId = ResFinder.getId("umeng_comm_user_icon");
        int nameEditResId = ResFinder.getId("umeng_comm_nickname_edt");
        int genderTextResId = ResFinder.getId("umeng_comm_gender_textview");
        mIconImg = mViewFinder.findViewById(userIconResId);
        mIconImg.setOnClickListener(this);

        // 初始化昵称
        mNickNameEtv = mViewFinder.findViewById(nameEditResId);
        if (!TextUtils.isEmpty(mUser.name)) {
            mNickNameEtv.setText(mUser.name);
            Selection.setSelection(mNickNameEtv.getText(), mNickNameEtv.length());
        }

        // 初始化性别
        mGendertTextView = mViewFinder.findViewById(genderTextResId);
        String genderStr = ResFinder.getString("umeng_comm_male");
        if (mUser.gender == Gender.FEMALE) {
            genderStr = ResFinder.getString("umeng_comm_female");
            changeDefaultIcon(Gender.FEMALE);
        }

        if (!TextUtils.isEmpty(mUser.iconUrl)) {
//            mImageLoader.reset();
            mImageLoader.displayImage(mUser.iconUrl, mIconImg, getDisplayOption(mGender));
            mImageLoader.resume();
        }

        mGendertTextView.setText(genderStr);
        mGendertTextView.setOnClickListener(this);

    }

    /**
     * 通过外部设置用户信息
     * 
     * @param user
     */
    public void setUser(CommUser user) {
        mUser = user;
    }

    public void setFirstSetting(boolean isFirstSetting) {
        this.isFirstSetting = isFirstSetting;
    }

    /**
     * @param bmp
     */
    public void showClipedBitmap(Bitmap bmp) {
        if (bmp != null) {
            mIconImg.setImageBitmap(bmp);
        }
    }

    /**
     * 检查检查昵称、年龄数据是否正确</br>
     * 
     * @return
     */
    private boolean checkData() {
        String name = mNickNameEtv.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            ToastMsg.showShortMsgByResName("umeng_comm_user_center_no_name");
            return false;
        }

        boolean result = CommonUtils.isUserNameValid(name);
        if (!result) {
            ToastMsg.showShortMsgByResName("umeng_comm_user_name_tips");
        }
        return result;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        int userIconResId = ResFinder.getId("umeng_comm_user_icon");
        int genderTextViewResId = ResFinder.getId("umeng_comm_gender_textview");
        int maleViewResId = ResFinder.getId("umeng_comm_gender_textview_male");
        int femalViewResId = ResFinder.getId("umeng_comm_gender_textview_femal");
        if (id == userIconResId) {
            if (isRegisterUserNameInvalid) {
                ToastMsg.showShortMsgByResName("umeng_comm_before_save");
            } else {
                selectProfile();
            }
        } else if (id == genderTextViewResId) {
            // 显示选择性别的dialog
            showGenderDialog();
        } else if (id == maleViewResId) {
            String maleStr = ResFinder.getString("umeng_comm_male");
            mGendertTextView.setText(maleStr);
            closeDialog();
            changeDefaultIcon(Gender.MALE);
        } else if (id == femalViewResId) {
            String femalStr = ResFinder.getString("umeng_comm_female");
            mGendertTextView.setText(femalStr);
            closeDialog();
            changeDefaultIcon(Gender.FEMALE);
        }
    }

    /**
     * 根据性别切换用户默认头像。该行为仅仅发生在用户没有头像的情况下</br>
     * 
     * @param gender 用户性别
     */
    private void changeDefaultIcon(Gender gender) {
        mGender = gender;
        // 头像为空的情况下才设置
        if (TextUtils.isEmpty(mUser.iconUrl)) {
            int resId = 0;
            if (gender == Gender.MALE) {
                resId = ResFinder.getResourceId(ResType.DRAWABLE, "umeng_comm_male");
            } else {
                resId = ResFinder.getResourceId(ResType.DRAWABLE, "umeng_comm_female");
            }
            mIconImg.setImageResource(resId);
        }
    }

    private ImgDisplayOption getDisplayOption(Gender gender) {
        ImgDisplayOption displayOption = new ImgDisplayOption();
        int resId = ResFinder.getResourceId(ResType.DRAWABLE, "umeng_comm_male");
        if (gender == Gender.FEMALE) {
            resId = ResFinder.getResourceId(ResType.DRAWABLE, "umeng_comm_female");
        }
        displayOption.setLoadingResId(resId).setLoadFailedResId(resId);
        return displayOption;
    }

    /**
     * 从相册中选择头像</br>
     */
    private void selectProfile() {
        Intent pickImageIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageIntent.setType("image/png;image/jpeg");
        getActivity().startActivityForResult(pickImageIntent, Constants.PIC_SELECT);
    }

    /**
     * 显示选择性别的Dialog</br>
     */
    private void showGenderDialog() {
        int style = ResFinder.getStyle("customDialog");
        int layout = ResFinder.getLayout("umeng_comm_gender_select");
        int femalResId = ResFinder.getId("umeng_comm_gender_textview_femal");
        int maleResId = ResFinder.getId("umeng_comm_gender_textview_male");
        mDialog = new Dialog(getActivity(), style);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(getActivity()).inflate(layout,
                null, false);
        mDialog.setContentView(view);
        mDialog.setCanceledOnTouchOutside(true);
        view.findViewById(femalResId).setOnClickListener(this);
        view.findViewById(maleResId).setOnClickListener(this);
        mDialog.show();
    }

    /**
     * 注册或者更新用户信息
     */
    public void registerOrUpdateUserInfo() {
        boolean flag = checkData();
        if (!flag) {
            return;
        }

        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setMessage(ResFinder.getString("umeng_comm_update_user_info"));
        if (isRegisterUserNameInvalid) {
            register(mProgressDialog);
        } else {
            updateUserInfo(mProgressDialog);
        }
    }

    private void register(final ProgressDialog dialog) {
        mUser.name = mNickNameEtv.getText().toString().trim();
        mUser.gender = mGender;
        // mCommunitySDK.register(mUser, new FetchListener<LoginResponse>() {
        //
        // @Override
        // public void onStart() {
        // dialog.show();
        // }
        //
        // @Override
        // public void onComplete(LoginResponse response) {
        // dialog.dismiss();
        // if (response.errCode == 0) {
        // Source source = mUser.source;
        // mUser = response.result;
        // LoginHelper.loginSuccess(getActivity(), mUser, source);
        // Intent intent = new Intent(getActivity(), GuideActivity.class);
        // getActivity().startActivity(intent);
        // getActivity().finish();
        // }
        //
        // showResponseToast(response);
        // }
        // });

        mPresenter.register(mUser);
    }

    /**
     * 更新用户信息</br>
     */
    private void updateUserInfo(final ProgressDialog progressDialog) {

        final CommUser newUser = CommConfig.getConfig().loginedUser;
        newUser.name = mNickNameEtv.getText().toString();
        newUser.gender = mGender;

        mPresenter.updateUserProfile(newUser);

    }

    /**
     * 检查昵称的合法性,确保在第一次登录时昵称不合法导致的问题.
     * 
     * @return
     */
    public boolean checkUserName() {
        String name = mNickNameEtv.getText().toString().trim();
        return CommonUtils.isUserNameValid(name);
    }

    /**
     * 关闭Dialog</br>
     */
    private void closeDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    /**
     * 隐藏输入法</br>
     */
    public void hideInputMethod() {
        ((BaseFragmentActivity) getActivity()).hideInputMethod(mNickNameEtv);
    }

    @Override
    public void showLoading(boolean isShow) {
        if (isShow) {
            mProgressDialog.show();
        } else {
            mProgressDialog.dismiss();
        }
    }
}
