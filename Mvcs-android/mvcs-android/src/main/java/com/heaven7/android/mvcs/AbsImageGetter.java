package com.heaven7.android.mvcs;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import java.io.File;
import java.lang.ref.WeakReference;
/**
 * 从相册或者摄像头获取图片，支持裁剪
 * @author heaven7
 * @note 内部采用弱引用Activity
 */
public abstract class AbsImageGetter {

	public static final int NONE = 0;
	public static final int PHOTOHRAPH = 1;    // 拍照
	public static final int PHOTOZOOM = 2;     // 缩放
	public static final int PHOTORESOULT = 3;  // 结果

	public static final String IMAGE_UNSPECIFIED = "image/*";
	
	private WeakReference<Activity> mWeakActivity ;
	private String path;
	private boolean finished;
	
	public AbsImageGetter(Activity activity) {
		mWeakActivity = new WeakReference<Activity>(activity);
	}
	
	/**从相册获取图片*/
	public void getFromAlbum(){
		if(finished) return;
		final Activity activity = getActivity();
		Intent intent = new Intent(Intent.ACTION_PICK, null);
		intent.setDataAndType(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				IMAGE_UNSPECIFIED);
		activity.startActivityForResult(intent, PHOTOZOOM);
	}
	
	public void setActivityFinished(boolean finished){
		this.finished = finished;
	}
	
	private Activity getActivity() {
		Activity activity = mWeakActivity.get();
		if(activity==null)
			throw new IllegalStateException("activity is finished?");
		return activity;
	}

	/** path 存储路径_绝对路径*/
	public void camera(String path){
		if(finished) return;
		final Activity activity = getActivity();
		this.path = path;
		//TODO SDKUtil.createFileIfNeed(path);
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(
				//Environment.getExternalStorageDirectory(), "temp.jpg"
				path
				)));
		activity.startActivityForResult(intent, PHOTOHRAPH);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if (resultCode == NONE)
			return;
		// 拍照
		if (requestCode == PHOTOHRAPH) {
			// 设置文件保存路径,这里放在跟目录下
			File picture = new File(
					//Environment.getExternalStorageDirectory()+ "/temp.jpg"
					path
					);
			startPhotoZoom(Uri.fromFile(picture));
		}

		if (data == null)
			return;

		// 读取相册缩放图片
		if (requestCode == PHOTOZOOM) {
			startPhotoZoom(data.getData());
		}
		// 处理结果
		if (requestCode == PHOTORESOULT) {
			Bundle extras = data.getExtras();
			if (extras != null) {
				Bitmap photo = extras.getParcelable("data");
				onGetImageSuccess(photo);
			}
		}
	}
	
	public abstract void onGetImageSuccess(Bitmap photo);
	
	public void startPhotoZoom(Uri uri) {
		if(finished) return;
		final Activity activity = getActivity();
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, IMAGE_UNSPECIFIED);
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 64);
		intent.putExtra("outputY", 64);
		intent.putExtra("return-data", true);
		activity.startActivityForResult(intent, PHOTORESOULT);
	}
}
