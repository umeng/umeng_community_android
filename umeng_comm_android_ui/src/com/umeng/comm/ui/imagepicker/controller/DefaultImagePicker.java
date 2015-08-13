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

package com.umeng.comm.ui.imagepicker.controller;

import android.app.Activity;
import android.content.Intent;

import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.imagepicker.ImagePicker;
import com.umeng.comm.ui.imagepicker.PhotoSelectorActivity;
import com.umeng.comm.ui.imagepicker.model.PhotoModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认的图片选择器
 * 
 */
public class DefaultImagePicker implements ImagePicker {
    
    @Override
    public void jumpToPickImagesPage(Activity activity, ArrayList<String> selected) {
        Intent intent = new Intent(activity, PhotoSelectorActivity.class);
        intent.putExtra(PhotoSelectorActivity.KEY_MAX, 9);
        // 传递已经选中的图片
        intent.putStringArrayListExtra(Constants.PICKED_IMAGES, selected);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.startActivityForResult(intent, Constants.PICK_IMAGE_REQ_CODE);        
    }
    
    
    @SuppressWarnings("unchecked")
    @Override
    public List<String> parsePickedImageList(Intent intent) {
        List<String> selectedImages = new ArrayList<String>();
        List<PhotoModel> photos = (List<PhotoModel>) intent.getExtras().getSerializable(
                Constants.FEED_IMAGES);
        for (PhotoModel photoModel : photos) {
            selectedImages.add(photoModel.getOriginalPath());
        }

        return selectedImages;
    }
}
