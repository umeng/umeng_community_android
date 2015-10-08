
package com.umeng.comm.ui.imagepicker.domain;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.umeng.comm.ui.imagepicker.PhotoSelectorActivity.OnLocalAlbumListener;
import com.umeng.comm.ui.imagepicker.PhotoSelectorActivity.OnLocalReccentListener;
import com.umeng.comm.ui.imagepicker.controller.AlbumController;
import com.umeng.comm.ui.imagepicker.model.AlbumModel;
import com.umeng.comm.ui.imagepicker.model.PhotoModel;

// TODO Handler 的方式需要优化
@SuppressLint("HandlerLeak")
public class PhotoSelectorDomain {

    private AlbumController albumController;

    public PhotoSelectorDomain(Context context) {
        albumController = new AlbumController(context);
    }

    /**
     * 获取最近的照片
     * 
     * @param listener
     */
    public void getReccent(final OnLocalReccentListener listener) {
        final Handler handler = new Handler() {
            @SuppressWarnings("unchecked")
            @Override
            public void handleMessage(Message msg) {
                listener.onPhotoLoaded((List<PhotoModel>) msg.obj);
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<PhotoModel> photos = albumController.getCurrent();
                Message msg = new Message();
                msg.obj = photos;
                handler.sendMessage(msg);
            }
        }).start();
    }

    /**
     * 更新相册
     * 
     * @param listener
     */
    public void updateAlbum(final OnLocalAlbumListener listener) {
        final Handler handler = new Handler() {
            @SuppressWarnings("unchecked")
            @Override
            public void handleMessage(Message msg) {
                listener.onAlbumLoaded((List<AlbumModel>) msg.obj);
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<AlbumModel> albums = albumController.getAlbums();
                Message msg = new Message();
                msg.obj = albums;
                handler.sendMessage(msg);
            }
        }).start();
    }

    /**
     * 获取某个相册中的图片
     * 
     * @param name
     * @param listener
     */
    public void getAlbum(final String name, final OnLocalReccentListener listener) {
        final Handler handler = new Handler() {
            @SuppressWarnings("unchecked")
            @Override
            public void handleMessage(Message msg) {
                listener.onPhotoLoaded((List<PhotoModel>) msg.obj);
            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<PhotoModel> photos = albumController.getAlbum(name);
                Message msg = new Message();
                msg.obj = photos;
                handler.sendMessage(msg);
            }
        }).start();
    }

}
