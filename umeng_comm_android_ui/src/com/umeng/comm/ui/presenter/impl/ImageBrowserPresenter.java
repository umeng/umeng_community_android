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
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import com.umeng.comm.core.imageloader.cache.DiskLruCache;
import com.umeng.comm.core.imageloader.cache.ImageCache;
import com.umeng.comm.core.imageloader.utils.Md5Helper;
import com.umeng.comm.core.utils.CommonUtils;
import com.umeng.comm.core.utils.DeviceUtils;
import com.umeng.comm.core.utils.Log;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ToastMsg;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageBrowserPresenter {

    Context mContext;

    public ImageBrowserPresenter(Context context) {
        mContext = context;
    }

    public void saveImage(String url) {
        BufferedOutputStream out = null;
        BufferedInputStream in = null;
        DiskLruCache.Snapshot snapshot = null;
        try {
            String fileName = Md5Helper.toMD5(url);
            File imgFile = new File(getCacheDir() + File.separator
                    + fileName + ".png");
            snapshot = ImageCache.getInstance().getInputStream(fileName);
            if (snapshot == null) {
                return;
            }
            in = new BufferedInputStream(snapshot.getInputStream(0),
                    8 * 1024);
            out = new BufferedOutputStream(new FileOutputStream(imgFile), 8 * 1024);
            byte[] buffer = new byte[4 * 1024];
            int len = 0;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
            galleryAddPic(imgFile);
            String text = ResFinder.getString("umeng_comm_save_pic_success") +imgFile.getAbsolutePath();  
            Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            ToastMsg.showShortMsgByResName("umeng_comm_save_pic_failed");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            ToastMsg.showShortMsgByResName("umeng_comm_save_pic_failed");
        } finally {
            CommonUtils.closeSilently(snapshot);
            CommonUtils.closeSilently(out);
            CommonUtils.closeSilently(in);
        }
    }

    private void galleryAddPic(File savedFile) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(savedFile);
        mediaScanIntent.setData(contentUri);
        mContext.sendBroadcast(mediaScanIntent);
    }

    private String getCacheDir() throws IOException {
        Context context = mContext;
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Log.d("", "### context : " + context + ", dir = " + context.getExternalCacheDir());
            cachePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }

        File cacheFile = new File(cachePath + File.separator + DeviceUtils.getAppName(context));
        if (!cacheFile.exists()) {
            cacheFile.mkdir();
        }
        return cacheFile.getAbsolutePath();
    }
}
