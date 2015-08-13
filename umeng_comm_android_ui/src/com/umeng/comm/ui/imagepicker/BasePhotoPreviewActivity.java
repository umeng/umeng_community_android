
package com.umeng.comm.ui.imagepicker;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ResFinder.ResType;
import com.umeng.comm.ui.imagepicker.model.PhotoModel;
import com.umeng.comm.ui.imagepicker.util.AnimationUtil;
import com.umeng.comm.ui.imagepicker.widgets.PhotoPreview;

import java.util.List;

public class BasePhotoPreviewActivity extends Activity implements OnPageChangeListener,
        OnClickListener {

    private ViewPager mViewPager;
    private RelativeLayout layoutTop;
    private ImageView btnBack;
    private TextView tvPercent;
    protected List<PhotoModel> photos;
    protected int current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        setContentView(ResFinder.getLayout("umeng_comm_imagepicker_photopreview"));
        layoutTop = (RelativeLayout) findViewById(ResFinder.getId("umeng_comm_layout_top_app"));
        btnBack = (ImageView) findViewById(ResFinder.getId("umeng_comm_btn_back_app"));
        tvPercent = (TextView) findViewById(ResFinder.getId("umeng_comm_tv_percent_app"));
        mViewPager = (ViewPager) findViewById(ResFinder.getId("umeng_comm_vp_base_app"));

        btnBack.setOnClickListener(this);
        mViewPager.setOnPageChangeListener(this);

        overridePendingTransition(
                ResFinder.getResourceId(ResType.ANIM, "umeng_comm_activity_alpha_action_in"), 0); // 渐入效果

    }

    /** 绑定数据，更新界面 */
    protected void bindData() {
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(current);
    }

    private PagerAdapter mPagerAdapter = new PagerAdapter() {

        @Override
        public int getCount() {
            if (photos == null) {
                return 0;
            } else {
                return photos.size();
            }
        }

        @Override
        public View instantiateItem(final ViewGroup container, final int position) {
            PhotoPreview photoPreview = new PhotoPreview(getApplicationContext());
            ((ViewPager) container).addView(photoPreview);
            photoPreview.loadImage(photos.get(position));
            photoPreview.setOnClickListener(photoItemClickListener);
            return photoPreview;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    };
    protected boolean isUp;

    @Override
    public void onClick(View v) {
        if (v.getId() == ResFinder.getId("umeng_comm_btn_back_app"))
            finish();
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }

    @Override
    public void onPageSelected(int arg0) {
        current = arg0;
        updatePercent();
    }

    protected void updatePercent() {
        tvPercent.setText((current + 1) + "/" + photos.size());
    }

    /** 图片点击事件回调 */
    private OnClickListener photoItemClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!isUp) {
                new AnimationUtil(getApplicationContext(), ResFinder.getResourceId(ResType.ANIM,
                        "umeng_comm_translate_up"))
                        .setInterpolator(new LinearInterpolator()).setFillAfter(true)
                        .startAnimation(layoutTop);
                isUp = true;
            } else {
                new AnimationUtil(getApplicationContext(), ResFinder.getResourceId(ResType.ANIM,
                        "umeng_comm_translate_down_current"))
                        .setInterpolator(new LinearInterpolator()).setFillAfter(true)
                        .startAnimation(layoutTop);
                isUp = false;
            }
        }
    };
}
