package com.example.satoutakayuki.androidtraining;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

	private MainPhotoView mPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mPager = (MainPhotoView) findViewById(R.id.pager);

		// ダミーデータ作成
		final List<String> imageUrls = new ArrayList<String>();
		imageUrls.add("http://imgbp.hotp.jp/CSP/IMG_SRC/77/44/B002927744/B002927744_349-262.jpg");
		imageUrls.add("http://imgbp.hotp.jp/CSP/IMG_SRC/77/34/B002927734/B002927734_349-262.jpg");
		imageUrls.add("http://imgbp.hotp.jp/CSP/IMG_SRC/77/43/B002927743/B002927743_349-262.jpg");
		imageUrls.add("http://imgbp.hotp.jp/CSP/IMG_SRC/57/82/B003145782/B003145782_349-262.jpg");
//		imageUrls.add("http://imgbp.hotp.jp/CSP/IMG_SRC/45/06/B008094506/B008094506_349-262.jpg");
//		imageUrls.add("http://imgbp.hotp.jp/CSP/IMG_SRC/45/13/B008094513/B008094513_349-262.jpg");
//		imageUrls.add("http://imgbp.hotp.jp/CSP/IMG_SRC/16/41/B005811641/B005811641_349-262.jpg");
//		imageUrls.add("http://imgbp.hotp.jp/CSP/IMG_SRC/31/29/B005793129/B005793129_349-262.jpg");
//		imageUrls.add("http://imgbp.hotp.jp/CSP/IMG_SRC/31/70/B005793170/B005793170_349-262.jpg");
//		imageUrls.add("http://imgbp.hotp.jp/CSP/IMG_SRC/02/87/B007700287/B007700287_349-262.jpg");
//		imageUrls.add("http://imgbp.hotp.jp/CSP/IMG_SRC/69/30/B007846930/B007846930_349-262.jpg");
//		imageUrls.add("http://imgbp.hotp.jp/CSP/IMG_SRC/49/17/B007734917/B007734917_349-262.jpg");

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				mPager.setMainPhotoUrlList(imageUrls, MainActivity.this);
			}
		}, 1500);

	}

	@Override
	protected void onDestroy() {
		mPager.stopImageHandler();
		super.onDestroy();
	}

}
