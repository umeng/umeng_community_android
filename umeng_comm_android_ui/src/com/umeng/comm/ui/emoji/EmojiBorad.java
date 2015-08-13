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

package com.umeng.comm.ui.emoji;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.umeng.comm.core.utils.DeviceUtils;
import com.umeng.comm.core.utils.ResFinder;

/**
 * Emoji borad
 */
public class EmojiBorad extends LinearLayout implements OnPageChangeListener {

    private static final int PAGE_SIZE = 20; // size of page
    public static final String DELETE_KEY = "delete";
    private static int BOARD_HEIGHT = 400; // height of emoji board
    private EmojiPagerAdapter mAdapter = null;
    private List<ImageView> mIndicators = new ArrayList<ImageView>();

    private int mLastSelectViewPos = 0; // 上次选中的页面位置
    private String mNormalIcon = "umeng_comm_emojipoint_normal"; // 指示器未选中时的图片icon
    private String mSelectIcon = "umeng_comm_emojipoint_pressed";// 指示器选中时的图片icon

    /**
     * @param context
     */
    public EmojiBorad(Context context) {
        super(context);
        init();
    }

    /**
     * @param context
     * @param attrs
     */
    public EmojiBorad(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public EmojiBorad(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void computeBoardHeight() {
        BOARD_HEIGHT = DeviceUtils.dp2px(getContext(), 140);
    }

    /**
     * init. Set params and add show emoji views</br>
     */
    private void init() {
        computeBoardHeight(); 
        setOrientation(LinearLayout.VERTICAL);
        
        setBackgroundColor(Color.parseColor("#f4f4f6"));
        ViewPager viewPager = createVIewpager();
        addView(viewPager);
        ViewGroup container = createPointLinearlayout();
        List<EmojiView> datas = new ArrayList<EmojiView>();
        int lens = People.DATA.length;
        int pages = lens / PAGE_SIZE; // 总共pages个页面
        for (int i = 0; i < pages; i++) {
            EmojiBean[] blocks = new EmojiBean[PAGE_SIZE + 1];
            System.arraycopy(People.DATA, i * PAGE_SIZE, blocks, 0, blocks.length - 1);
            blocks[PAGE_SIZE] = EmojiBean.fromChars(DELETE_KEY);
            datas.add(new EmojiView(getContext(), blocks)); // the last is
                                                            // delete icon
        }
        // add remain emoji view
        if (pages * PAGE_SIZE < lens) {
            EmojiBean[] blocks = new EmojiBean[lens - pages * PAGE_SIZE];
            System.arraycopy(People.DATA, pages * PAGE_SIZE, blocks, 0, blocks.length);
            datas.add(new EmojiView(getContext(), blocks));
        }

        // add indicator
        for (int i = 0; i < datas.size(); i++) {
            ImageView indicatorView = createIndicator();
            mIndicators.add(indicatorView);
            container.addView(indicatorView);
        }
        addView(container);

        // set cache view count
        viewPager.setOffscreenPageLimit(datas.size());
        // 默认选中第一项
        mIndicators.get(mLastSelectViewPos).setImageDrawable(ResFinder.getDrawable(mSelectIcon));
        mAdapter = new EmojiPagerAdapter(getContext(), datas);
        viewPager.setAdapter(mAdapter);
        viewPager.setOnPageChangeListener(this);
    }

    public void setOnEmojiItemClickListener(OnEmojiItemClickListener listener) {
        List<EmojiView> views = mAdapter.getDataSource();
        for (EmojiView view : views) {
            view.setOnItemClickListener(listener);
        }
    }

    /**
     * 创建选中某页的指示器</br>
     * 
     * @return
     */
    private ImageView createIndicator() {
        ImageView imageView = new ImageView(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(30, 30);
        params.gravity = Gravity.CENTER;
        imageView.setLayoutParams(params);
        imageView.setImageDrawable(ResFinder.getDrawable(mNormalIcon));
        imageView.setPadding(0, 0, 10, 0);
        return imageView;
    }

    /**
     * </br>
     * 
     * @return
     */
    private ViewGroup createPointLinearlayout() {
        LinearLayout pointContainerLayout = new LinearLayout(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        pointContainerLayout.setLayoutParams(params);
        pointContainerLayout.setOrientation(LinearLayout.HORIZONTAL);
        pointContainerLayout.setGravity(Gravity.CENTER);
        params.gravity = Gravity.CENTER;
        params.topMargin = DeviceUtils.dp2px(getContext(), 15);
        params.bottomMargin = DeviceUtils.dp2px(getContext(), 15);
        return pointContainerLayout;
    }

    /**
     * create ViewPager view</br>
     * 
     * @return
     */
    private ViewPager createVIewpager() {
        ViewPager viewPager = new ViewPager(getContext());
        ViewPager.LayoutParams params = new ViewPager.LayoutParams();
        params.width = android.support.v4.view.ViewPager.LayoutParams.MATCH_PARENT;
        params.height = BOARD_HEIGHT;
        viewPager.setLayoutParams(params);
        viewPager.setPadding(0, 15, 0, 0);
        return viewPager;
    }

    /**
     * ViewPager Adapter
     */
    private class EmojiPagerAdapter extends PagerAdapter {

        private List<EmojiView> mViews = new ArrayList<EmojiView>();

        public EmojiPagerAdapter(Context context, List<EmojiView> views) {
            mViews.addAll(views);
        }

        @Override
        public int getCount() {
            return mViews.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mViews.get(position), position);
            return mViews.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
        }

        public List<EmojiView> getDataSource() {
            return mViews;
        }
    }

    /**
     * click emoji item callback function
     */
    public static interface OnEmojiItemClickListener {
        /**
         * invoked when click emiji board item</br>
         * 
         * @param emojiBean clicked EmiojiBean
         */
        public void onItemClick(EmojiBean emojiBean);
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }

    @Override
    public void onPageSelected(int pos) {
        mIndicators.get(mLastSelectViewPos).setImageDrawable(ResFinder.getDrawable(mNormalIcon));
        mIndicators.get(pos).setImageDrawable(ResFinder.getDrawable(mSelectIcon));
        mLastSelectViewPos = pos;
    }

}
