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

package com.umeng.comm.ui.widgets;

/**
 * 可缩放的ImageView,用于图片浏览
 */
import android.content.Context;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ImageView;

public class ScaleImageView extends ImageView {
    private Context mContext;
    private float MAX_SCALE = 8f;

    private Matrix mMatrix;
    private final float[] mMatrixValues = new float[9];

    // display width height.
    private int mWidth;
    private int mHeight;

    private int mIntrinsicWidth;
    private int mIntrinsicHeight;

    private float mScale;
    private float mMinScale = 0.1f;

    private float mPrevDistance;
    private boolean isScaling;

    private int mPrevMoveX;
    private int mPrevMoveY;

    private int mLastX;
    private int mLastY;

    private GestureDetector mDetector;

    private float mSlop;
    private android.content.DialogInterface.OnDismissListener mListener;
    private float mDefaultScale = 0f;

    public ScaleImageView(Context context, AttributeSet attr) {
        super(context, attr);
        this.mContext = context;
        initialize();
    }

    public ScaleImageView(Context context) {
        super(context);
        this.mContext = context;
        initialize();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        this.initialize();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        this.initialize();
    }

    private void initialize() {
        this.setScaleType(ScaleType.MATRIX);
        this.mMatrix = new Matrix();
        Drawable d = getDrawable();
        if (d != null) {
            mIntrinsicWidth = d.getIntrinsicWidth();
            mIntrinsicHeight = d.getIntrinsicHeight();
        }
        // 双击的情况。首先放大到图片的最大比例8，再双击则显示原图大小
        mDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                maxZoomTo((int) e.getX(), (int) e.getY());
                cutting();
                return super.onDoubleTap(e);
            }
        });
        mSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        Log.e(VIEW_LOG_TAG, "### 图片宽度 : " + mIntrinsicWidth + ", height : " + mIntrinsicHeight);
    }

    public void updateScale() {
        mWidth = getWidth();
        mHeight = getHeight();
        mMatrix.reset();
        mScale = (float) mWidth / (float) mIntrinsicWidth;
        if (mScale * mIntrinsicHeight > mHeight) {
            mScale = (float) mHeight / (float) mIntrinsicHeight;
        }
        zoomTo(mScale, mWidth / 2, mHeight / 2);
        cutting();
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        mWidth = r - l;
        mHeight = b - t;

        mMatrix.reset();
        int r_norm = r - l;
        mScale = (float) r_norm / (float) mIntrinsicWidth;
        if (mScale * mIntrinsicHeight > mHeight) {
            mScale = (float) mHeight / (float) mIntrinsicHeight;
        }
        zoomTo(mScale, mWidth / 2, mHeight / 2);
        cutting();
        return super.setFrame(l, t, r, b);
    }

    protected float getValue(Matrix matrix, int whichValue) {
        matrix.getValues(mMatrixValues);
        return mMatrixValues[whichValue];
    }

    protected float getScale() {
        return getValue(mMatrix, Matrix.MSCALE_X);
    }

    public float getTranslateX() {
        return getValue(mMatrix, Matrix.MTRANS_X);
    }

    protected float getTranslateY() {
        return getValue(mMatrix, Matrix.MTRANS_Y);
    }

    protected void maxZoomTo(int x, int y) {
        float scale = 0;
        if (MAX_SCALE != getScale() && (Math.abs(getScale() - MAX_SCALE)) > 0.1f) {
            scale = MAX_SCALE / getScale();
        } else {
            scale = mMinScale / getScale();
        }
        zoomTo(scale, x, y);
    }

    public void reset() {
        mMatrix.reset();
        mScale = mMinScale;
        setImageMatrix(mMatrix);
    }

    public void zoomTo(float scale, int x, int y) {
        if (getScale() * scale < mMinScale) {
            return;
        }
        if (scale >= 1 && getScale() * scale > MAX_SCALE) {
            return;
        }
        mMatrix.postScale(scale, scale);
        // move to center
        mMatrix.postTranslate(-(mWidth * scale - mWidth) / 2, -(mHeight * scale - mHeight) / 2);

        // move x and y distance
        mMatrix.postTranslate(-(x - (mWidth / 2)) * scale, 0);
        mMatrix.postTranslate(0, -(y - (mHeight / 2)) * scale);
        setImageMatrix(mMatrix);
    }

    public void cutting() {
        int width = (int) (mIntrinsicWidth * getScale());
        int height = (int) (mIntrinsicHeight * getScale());
        if (getTranslateX() < -(width - mWidth)) {
            mMatrix.postTranslate(-(getTranslateX() + width - mWidth), 0);
        }
        if (getTranslateX() > 0) {
            mMatrix.postTranslate(-getTranslateX(), 0);
        }
        if (getTranslateY() < -(height - mHeight)) {
            mMatrix.postTranslate(0, -(getTranslateY() + height - mHeight));
        }
        if (getTranslateY() > 0) {
            mMatrix.postTranslate(0, -getTranslateY());
        }
        if (width < mWidth) {
            mMatrix.postTranslate((mWidth - width) / 2, 0);
        }
        if (height < mHeight) {
            mMatrix.postTranslate(0, (mHeight - height) / 2);
        }
        setImageMatrix(mMatrix);
    }

    private float distance(float x0, float x1, float y0, float y1) {
        float x = x0 - x1;
        float y = y0 - y1;
        return FloatMath.sqrt(x * x + y * y);
    }

    private float dispDistance() {
        return FloatMath.sqrt(mWidth * mWidth + mHeight * mHeight);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (isGingerbread() || event.getAction() != MotionEvent.ACTION_MOVE) {
            return super.dispatchTouchEvent(event);
        }
        if (mDefaultScale == 0f) {
            mDefaultScale = mScale;
        }
        if (getScale() - 1.0f >= 0.01f && !checkEdge(event)) {// 放大且没有移动到边缘，此时事件交给子控件处理
            getParent().requestDisallowInterceptTouchEvent(true);
        } else {
            getParent().requestDisallowInterceptTouchEvent(false);// 交给ViewPager处理
        }
        return super.dispatchTouchEvent(event);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mDetector.onTouchEvent(event)) {
            return true;
        }
        int touchCount = event.getPointerCount();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_1_DOWN:
            case MotionEvent.ACTION_POINTER_2_DOWN:
                if (touchCount >= 2) {
                    float distance = distance(event.getX(0), event.getX(1), event.getY(0),
                            event.getY(1));
                    mPrevDistance = distance;
                    isScaling = true;
                } else {
                    mPrevMoveX = (int) event.getX();
                    mPrevMoveY = (int) event.getY();
                    mLastX = mPrevMoveX;
                    mLastY = mPrevMoveY;
                }
            case MotionEvent.ACTION_MOVE:
                if (touchCount >= 2 && isScaling) {
                    float dist = distance(event.getX(0), event.getX(1), event.getY(0),
                            event.getY(1));
                    // if ( Math.abs(dist - mPrevDistance) < mSlop ) {
                    // Log.d("", "######## move dis " + dist + "   " + mSlop);
                    // return false;
                    // }
                    float scale = (dist - mPrevDistance) / dispDistance();
                    mPrevDistance = dist;
                    scale += 1;
                    scale = scale * scale;
                    zoomTo(scale, mWidth / 2, mHeight / 2);
                    cutting();
                } else if (!isScaling) {
                    int distanceX = mPrevMoveX - (int) event.getX();
                    int distanceY = mPrevMoveY - (int) event.getY();
                    mPrevMoveX = (int) event.getX();
                    mPrevMoveY = (int) event.getY();
                    mMatrix.postTranslate(-distanceX, -distanceY);
                    cutting();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_POINTER_2_UP:
                if (event.getPointerCount() <= 1) {
                    isScaling = false;
                    checkExecuteCallback(event);
                } else {
                    if (mDefaultScale == 0f) {
                        mDefaultScale = mScale;
                    }
                }
                mPrevDistance = 0;
                mPrevMoveX = 0;
                mPrevMoveY = 0;
                mLastX = 0;
                mLastY = 0;
                break;
        }
        return true;
    }

    /**
     * 检查是否执行回调</br>
     * 
     * @param event
     */
    private void checkExecuteCallback(MotionEvent event) {
        int delta = (int) distance(mLastX, event.getX(), mLastY, event.getY());
        if (delta < mSlop && mListener != null) {
            // 此时仅仅是点击，执行回调，dismiss dialog
            mListener.onDismiss(null);
        }
    }

    /**
     * 检查是否滑动到放大的边缘</br>
     * 
     * @param event
     * @return
     */
    private boolean checkEdge(MotionEvent event) {
        if (isGingerbread()) {
            return super.dispatchTouchEvent(event);
        }
        mapDisplayRect();
        // Log.d("", "#############----" +mDisplayRect.left + "   " +
        // mDisplayRect.right + "   " + getWidth() + "   " + mSlop);
        // Log.d("", "#############----" +getScale() + "  " + mDefaultScale);

        // 已经滑动到边缘
        if (Math.abs(mDisplayRect.left) <= mSlop
                ||
                (Math.abs(mDisplayRect.left) > mSlop && getScale() - mDefaultScale <= 0.5f)
                || Math.abs(mDisplayRect.right - getWidth()) < mSlop
                || (Math.abs(mDisplayRect.right - getWidth()) >= mSlop && getScale()
                        - mDefaultScale <= 0.5f)) {
            return true;
        }
        return false;
    }

    private final RectF mDisplayRect = new RectF();

    /**
     * 将图片的尺寸矩阵map成RectF</br>
     * 
     * @return
     */
    private RectF mapDisplayRect() {
        Drawable d = getDrawable();
        if (null != d) {
            mDisplayRect.set(0, 0, d.getIntrinsicWidth(),
                    d.getIntrinsicHeight());
            getImageMatrix().mapRect(mDisplayRect);
            return mDisplayRect;
        }
        return null;
    }

    public void setOndismissListener(OnDismissListener listener) {
        mListener = listener;
    }

    private boolean isGingerbread() {
        if (VERSION.SDK_INT == Build.VERSION_CODES.GINGERBREAD) {
            return true;
        }
        return false;
    }

}
