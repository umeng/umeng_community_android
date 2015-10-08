
package com.umeng.comm.ui.imagepicker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.comm.core.constants.Constants;
import com.umeng.comm.core.sdkmanager.ImageLoaderManager;
import com.umeng.comm.core.utils.ResFinder;
import com.umeng.comm.core.utils.ResFinder.ResType;
import com.umeng.comm.core.utils.ToastMsg;
import com.umeng.comm.ui.imagepicker.adapters.AlbumAdapter;
import com.umeng.comm.ui.imagepicker.adapters.PhotoAdapter;
import com.umeng.comm.ui.imagepicker.domain.PhotoSelectorDomain;
import com.umeng.comm.ui.imagepicker.model.AlbumModel;
import com.umeng.comm.ui.imagepicker.model.PhotoConstants;
import com.umeng.comm.ui.imagepicker.model.PhotoModel;
import com.umeng.comm.ui.imagepicker.util.AnimationUtil;
import com.umeng.comm.ui.imagepicker.util.ImagePickerUtils;
import com.umeng.comm.ui.imagepicker.widgets.PhotoItemViewHolder;
import com.umeng.comm.ui.imagepicker.widgets.PhotoItemViewHolder.onItemClickListener;
import com.umeng.comm.ui.imagepicker.widgets.PhotoItemViewHolder.onPhotoItemCheckedListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片选择Activity
 */
public class PhotoSelectorActivity extends Activity implements
        onItemClickListener, onPhotoItemCheckedListener, OnItemClickListener,
        OnClickListener {

    public static final int SINGLE_IMAGE = 1;
    public static final String KEY_MAX = "key_max";
    public static final String SELECTED = "selected";
    public static final String ADD_PHOTO_PATH = "add_image_path_sample";
    private int MAX_IMAGE;

    public static final int REQUEST_PHOTO = 0;
    private static final int REQUEST_CAMERA = 1;

    public static String RECCENT_PHOTO = null;

    private GridView mPhotosGridView;
    private ListView mAblumListView;
    private Button btnOk;
    private TextView tvAlbum, tvPreview;
    private PhotoSelectorDomain photoSelectorDomain;
    private PhotoAdapter mPhotoAdapter;
    private AlbumAdapter albumAdapter;
    private RelativeLayout layoutAlbum;
    private ArrayList<PhotoModel> mSelectedPhotos;
    private TextView tvNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RECCENT_PHOTO = ResFinder.getString("umeng_comm_recent_photos");
        requestWindowFeature(Window.FEATURE_NO_TITLE);//
        setContentView(ResFinder.getLayout("umeng_comm_imagepicker_photoselector"));

        parseIntentExtra(getIntent());
        initWidgets();
        initPhotoGridView();
        initAlbumListView();

        photoSelectorDomain = new PhotoSelectorDomain(getApplicationContext());
        photoSelectorDomain.getReccent(reccentListener);
        photoSelectorDomain.updateAlbum(albumListener);
    }

    private void initWidgets() {
        // tvTitle = (TextView)
        // findViewById(ResFinder.getId("umeng_comm_tv_title_lh"));
        mPhotosGridView = (GridView) findViewById(ResFinder.getId("umeng_comm_gv_photos_ar"));
        mAblumListView = (ListView) findViewById(ResFinder.getId("umeng_comm_lv_ablum_ar"));
        btnOk = (Button) findViewById(ResFinder.getId("umeng_comm_btn_right_lh"));
        tvAlbum = (TextView) findViewById(ResFinder.getId("umeng_comm_tv_album_ar"));
        tvPreview = (TextView) findViewById(ResFinder.getId("umeng_comm_tv_preview_ar"));
        layoutAlbum = (RelativeLayout) findViewById(ResFinder.getId("umeng_comm_layout_album_ar"));
        tvNumber = (TextView) findViewById(ResFinder.getId("umeng_comm_tv_number"));
        updateSelectePhotoSize();

        btnOk.setOnClickListener(this);
        tvAlbum.setOnClickListener(this);
        tvPreview.setOnClickListener(this);

        findViewById(ResFinder.getId("umeng_comm_bv_back_lh")).setOnClickListener(this); // 返回按钮
    }

    private void initPhotoGridView() {
        mPhotoAdapter = new PhotoAdapter(getApplicationContext(),
                new ArrayList<PhotoModel>(), ImagePickerUtils.getWidthPixels(this),
                this, this);
        mPhotosGridView.setAdapter(mPhotoAdapter);
    }

    private void initAlbumListView() {
        albumAdapter = new AlbumAdapter(getApplicationContext(),
                new ArrayList<AlbumModel>());
        mAblumListView.setAdapter(albumAdapter);
        mAblumListView.setOnItemClickListener(this);
    }

    private void parseIntentExtra(Intent intent) {
        if (getIntent().getExtras() != null) {
            MAX_IMAGE = getIntent().getIntExtra(KEY_MAX, 9);
            // 初始化mSelectedPhotos
            mSelectedPhotos = new ArrayList<PhotoModel>();
            // 获取从外部传递进来的已选列表
            initSelectedPhotoModels(intent.getStringArrayListExtra(Constants.PICKED_IMAGES));
        } else {
            // 选中的图片
            mSelectedPhotos = new ArrayList<PhotoModel>();
        }
    }

    private void initSelectedPhotoModels(List<String> selectedList) {
        if (selectedList != null) {
            for (String path : selectedList) {
                if (!path.equals(ADD_PHOTO_PATH)) {
                    mSelectedPhotos.add(new PhotoModel(path, true));
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == ResFinder.getId("umeng_comm_btn_right_lh"))
            pickedImageDone();
        else if (v.getId() == ResFinder.getId("umeng_comm_tv_album_ar"))
            album();
        else if (v.getId() == ResFinder.getId("umeng_comm_tv_preview_ar"))
            preview();
        else if (v.getId() == ResFinder.getId("umeng_comm_bv_back_lh"))
            finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            PhotoModel photoModel = new PhotoModel(ImagePickerUtils.query(
                    getApplicationContext(), data.getData()));
            // ///////////////////////////////////////////////////////////////////////////////////////////
            if (mSelectedPhotos.size() >= MAX_IMAGE) {
                String toastMsg = String.format(
                        ResFinder.getString("umeng_comm_max_img_limit_reached"),
                        MAX_IMAGE);
                Toast.makeText(
                        this, toastMsg, Toast.LENGTH_SHORT).show();
                photoModel.setChecked(false);
                mPhotoAdapter.notifyDataSetChanged();
            } else {
                if (!mSelectedPhotos.contains(photoModel)) {
                    mSelectedPhotos.add(photoModel);
                }
            }
            pickedImageDone();
        }
    }

    private void pickedImageDone() {
        if (mSelectedPhotos.isEmpty()) {
            setResult(RESULT_CANCELED);
        } else {
            Intent data = new Intent();
            Bundle bundle = new Bundle();
            bundle.putSerializable(Constants.FEED_IMAGES, mSelectedPhotos);
            data.putExtras(bundle);
            setResult(RESULT_OK, data);
        }
        finish();
    }

    private void preview() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(PhotoConstants.PHOTO_PRVIEW_PHOTO, mSelectedPhotos);
        ImagePickerUtils.launchActivity(this, PhotoPreviewActivity.class, bundle);
    }

    private void album() {
        if (layoutAlbum.getVisibility() == View.GONE) {
            popAlbum();
        } else {
            hideAlbum();
        }
    }

    private void popAlbum() {
        layoutAlbum.setVisibility(View.VISIBLE);
        new AnimationUtil(getApplicationContext(), ResFinder.getResourceId(ResType.ANIM,
                "umeng_comm_translate_up_current"))
                .setLinearInterpolator().startAnimation(layoutAlbum);
    }

    private void hideAlbum() {
        new AnimationUtil(getApplicationContext(), ResFinder.getResourceId(ResType.ANIM,
                "umeng_comm_translate_down"))
                .setLinearInterpolator().startAnimation(layoutAlbum);
        layoutAlbum.setVisibility(View.GONE);
    }

    /** 点击查看照片 */
    @Override
    public void onItemClick(int position) {
        Bundle bundle = new Bundle();
        // if (tvAlbum.getText().toString().equals(RECCENT_PHOTO)) {
        // bundle.putInt("position", position - 1);
        // }
        // else {
        // bundle.putInt("position", position);
        // }
        bundle.putInt("position", position);
        bundle.putString("album", tvAlbum.getText().toString());
        ImagePickerUtils.launchActivity(this, PhotoPreviewActivity.class, bundle);
    }

    @Override
    public void onCheckedChanged(PhotoItemViewHolder photoItem, PhotoModel photoModel,
            boolean isChecked) {
        // 不能超过最大数量
        if (mSelectedPhotos.size() == MAX_IMAGE && isChecked) {
            ToastMsg.showShortMsgByResName("umeng_comm_image_overflow");
            photoItem.updatePhotoItemState(false);
            return;
        }

        if (isChecked) {
            if (!mSelectedPhotos.contains(photoModel)) {
                mSelectedPhotos.add(photoModel);
            }
            tvPreview.setEnabled(true);
        } else {
            mSelectedPhotos.remove(photoModel);
        }
        // 更新选中状态
        photoItem.updatePhotoItemState(isChecked);

        updateSelectePhotoSize();

        if (mSelectedPhotos.isEmpty()) {
            tvPreview.setEnabled(false);
            tvPreview.setText(ResFinder.getString("umeng_comm_preview"));
        }
    }

    private void updateSelectePhotoSize() {
        tvNumber.setText("(" + mSelectedPhotos.size() + ")");
    }

    @Override
    public void onBackPressed() {
        if (layoutAlbum.getVisibility() == View.VISIBLE) {
            hideAlbum();
        } else
            super.onBackPressed();
    }

    /** 相册列表点击事件 */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        AlbumModel current = (AlbumModel) parent.getItemAtPosition(position);
        for (int i = 0; i < parent.getCount(); i++) {
            AlbumModel album = (AlbumModel) parent.getItemAtPosition(i);
            if (i == position) {
                album.setCheck(true);
            }
            else {
                album.setCheck(false);
            }
        }
        albumAdapter.notifyDataSetChanged();
        hideAlbum();
        tvAlbum.setText(current.getName());

        if (current.getName().equals(RECCENT_PHOTO)) {
            photoSelectorDomain.getReccent(reccentListener);
        } else {
            photoSelectorDomain.getAlbum(current.getName(), reccentListener); // 获取选中相册的照片
        }
    }

    private OnLocalAlbumListener albumListener = new OnLocalAlbumListener() {
        @Override
        public void onAlbumLoaded(List<AlbumModel> albums) {
            albumAdapter.update(albums);
        }
    };

    private OnLocalReccentListener reccentListener = new OnLocalReccentListener() {
        @Override
        public void onPhotoLoaded(List<PhotoModel> photos) {
            for (PhotoModel model : photos) {
                if (mSelectedPhotos.contains(model)) {
                    model.setChecked(true);
                }
            }
            mPhotoAdapter.update(photos);
            mPhotosGridView.setSelection(0); // 修改图片选择目录之后滚动到顶部
        }
    };

    /** 获取本地图库照片回调 */
    public static interface OnLocalReccentListener {
        public void onPhotoLoaded(List<PhotoModel> photos);
    }

    /** 获取本地相册信息回调 */
    public static interface OnLocalAlbumListener {
        public void onAlbumLoaded(List<AlbumModel> albums);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ImageLoaderManager.getInstance().getCurrentSDK().reset();
        ImageLoaderManager.getInstance().getCurrentSDK().resume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ImageLoaderManager.getInstance().getCurrentSDK().reset();
    }
}
