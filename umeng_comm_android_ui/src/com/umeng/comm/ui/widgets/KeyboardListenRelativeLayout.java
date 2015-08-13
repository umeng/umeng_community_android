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

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * 带有键盘事件相应的RelativeLayout
 */
public class KeyboardListenRelativeLayout extends RelativeLayout {
	
	public static final byte KEYBOARD_STATE_SHOW = -3;
	public static final byte KEYBOARD_STATE_HIDE = -2;
	public static final byte KEYBOARD_STATE_INIT = -1;
	
	private boolean mHasInit = false;
	private boolean mHasKeyboard = false;
	private int mHeight;
	
	private IOnKeyboardStateChangedListener mOnKeyboardStateChangedListener;
	
	public KeyboardListenRelativeLayout(Context context) {
		super(context);
	}
	public KeyboardListenRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public KeyboardListenRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void setOnKeyboardStateChangedListener(IOnKeyboardStateChangedListener onKeyboardStateChangedListener) {
		this.mOnKeyboardStateChangedListener = onKeyboardStateChangedListener;
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if(!mHasInit) {
			mHasInit = true;
			mHeight = b;
			if(mOnKeyboardStateChangedListener != null) {
				mOnKeyboardStateChangedListener.onKeyboardStateChanged(KEYBOARD_STATE_INIT);
			}
		} else {
			mHeight = mHeight < b ? b : mHeight;
		}
		
		if(mHasInit && mHeight > b) {
			mHasKeyboard = true;
			if(mOnKeyboardStateChangedListener != null) {
				mOnKeyboardStateChangedListener.onKeyboardStateChanged(KEYBOARD_STATE_SHOW);
			}
		}
		if(mHasInit && mHasKeyboard && mHeight == b) {
			mHasKeyboard = false;
			if(mOnKeyboardStateChangedListener != null) {
				mOnKeyboardStateChangedListener.onKeyboardStateChanged(KEYBOARD_STATE_HIDE);
			}
		}
	}
	
	/**
	 * <p>@ClassName: IOnKeyboardStateChangedListener</p>
	 * <p>@Description:
	 *
	 * </p> 
	 *
	 */
	public interface IOnKeyboardStateChangedListener {
		public void onKeyboardStateChanged(int state);
	}
}
