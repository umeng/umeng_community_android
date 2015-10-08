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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.umeng.comm.core.utils.ToastMsg;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TakePhotoPresenter {

    /**
     * 启动拍照的requestCode
     */
    public static final int REQUEST_IMAGE_CAPTURE = 123;
    private Activity mContext;
    private String mNewImagePath;

    public void attach(Activity activity) {
        mContext = activity;
    }

    public void detach() {
        mContext = null;
    }

    /**
     * 启动系统拍照功能
     */
    public void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        ComponentName componentName = takePictureIntent.
                resolveActivity(mContext.getPackageManager());
        if (componentName == null) { // 无拍照的App
            return;
        }
        File photoFile = null;
        try {
            photoFile = createImageFile();
            Uri fileUri = Uri.fromFile(photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            // 跳转到拍照页面
            mContext.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (IOException ex) {
            // Error occurred while creating the File
            ex.printStackTrace();
            ToastMsg.showShortMsgByResName("umeng_comm_save_photo_failed");
        }
    }

    /**
     * Creates the image file to which the image must be saved.
     * 
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        SimpleDateFormat dateFormat = (SimpleDateFormat) SimpleDateFormat.getInstance();
        dateFormat.applyPattern("yyyyMMdd_HHmmss");
        String timeStamp = dateFormat.format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        // 检测目录是否存在
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        mNewImagePath = image.getAbsolutePath();
        return image;
    }

    /**
     * 保存新的照片,并且返回照片的Uri路径
     * 
     * @return 照片的Uri
     */
    public String updateImageToMediaLibrary() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File imgFile = new File(mNewImagePath);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mNewImagePath, options);
        // 图片的有效性判断
        if (options.outWidth < 10 && options.outHeight < 10) {
            imgFile.delete();
            return "";
        }
        Uri contentUri = Uri.fromFile(imgFile);
        mediaScanIntent.setData(contentUri);

        // 发布广播,更新媒体库
        mContext.sendBroadcast(mediaScanIntent);
        return contentUri.toString();
    }
}
