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

package com.umeng.comm.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.umeng.comm.core.utils.ResFinder;

/**
 * 操作确认Dialog
 */
public class ConfirmDialog {

    /**
     * 根据msg、回调构造一个Dialog并显示</br>
     * 
     * @param context Context对象
     * @param msg 显示的message
     * @param listener 点击确认按钮时的回调
     */
    public static void showDialog(Context context, String msg,
            DialogInterface.OnClickListener listener) {
        showDialog(context, msg, listener, null);
    }

    public static void showDialog(Context context, String msg,
            DialogInterface.OnClickListener confirmListener,
            DialogInterface.OnClickListener cancelListener) {
        String okStr = ResFinder.getString("umeng_comm_ok");
        String cancelStr = ResFinder.getString("umeng_comm_cancel");
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(msg);
        builder.setPositiveButton(okStr, confirmListener);

        builder.setNegativeButton(cancelStr, cancelListener);
        builder.create().show();
    }
}
