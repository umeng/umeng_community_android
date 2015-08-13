
package com.umeng.comm.ui.imagepicker.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.umeng.comm.core.sdkmanager.ImageLoaderManager;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.ui.imagepicker.model.AlbumModel;

/**
 * 相册Item视图
 */
public class AlbumItemView extends LinearLayout {

    private ImageView ivAlbum, ivIndex;
    private TextView tvName, tvCount;

    public AlbumItemView(Context context) {
        this(context, null);
    }

    public AlbumItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(ResFinder.getLayout("umeng_comm_imagepicker_album_item"), this, true);

        ivAlbum = (ImageView) findViewById(ResFinder.getId("umeng_comm_iv_album_la"));
        ivIndex = (ImageView) findViewById(ResFinder.getId("umeng_comm_iv_index_la"));
        tvName = (TextView) findViewById(ResFinder.getId("umeng_comm_tv_name_la"));
        tvCount = (TextView) findViewById(ResFinder.getId("umeng_comm_tv_count_la"));
    }

    public AlbumItemView(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public void setAlbumImage(String path) {
        ImageLoaderManager.getInstance().getCurrentSDK().displayImage("file://" + path, ivAlbum);
    }

    public void update(AlbumModel album) {
        setAlbumImage(album.getRecent());
        setName(album.getName());
        setCount(album.getCount());
        isCheck(album.isCheck());
    }

    public void setName(CharSequence title) {
        tvName.setText(title);
    }

    public void setCount(int count) {
        tvCount.setHint(count + ResFinder.getString("umeng_comm_count"));
    }

    public void isCheck(boolean isCheck) {
        if (isCheck) {
            ivIndex.setVisibility(View.VISIBLE);
        }
        else {
            ivIndex.setVisibility(View.GONE);
        }
    }

}
