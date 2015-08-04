package com.example.satoutakayuki.androidtraining;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

	//    LoopViewPager mPager;
	ViewPager mPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// ダミーデータ作成
		final List<String> imageUrls = new ArrayList<String>();
		imageUrls.add("http://imgbp.hotp.jp/CSP/IMG_SRC/77/44/B002927744/B002927744_349-262.jpg");
		imageUrls.add("http://imgbp.hotp.jp/CSP/IMG_SRC/77/34/B002927734/B002927734_349-262.jpg");
		imageUrls.add("http://imgbp.hotp.jp/CSP/IMG_SRC/77/43/B002927743/B002927743_349-262.jpg");
		imageUrls.add("http://imgbp.hotp.jp/CSP/IMG_SRC/57/82/B003145782/B003145782_349-262.jpg");
		imageUrls.add("http://imgbp.hotp.jp/CSP/IMG_SRC/45/06/B008094506/B008094506_349-262.jpg");
		imageUrls.add("http://imgbp.hotp.jp/CSP/IMG_SRC/45/13/B008094513/B008094513_349-262.jpg");
		imageUrls.add("http://imgbp.hotp.jp/CSP/IMG_SRC/16/41/B005811641/B005811641_349-262.jpg");
		imageUrls.add("http://imgbp.hotp.jp/CSP/IMG_SRC/31/29/B005793129/B005793129_349-262.jpg");
		imageUrls.add("http://imgbp.hotp.jp/CSP/IMG_SRC/31/70/B005793170/B005793170_349-262.jpg");
		imageUrls.add("http://imgbp.hotp.jp/CSP/IMG_SRC/02/87/B007700287/B007700287_349-262.jpg");
		imageUrls.add("http://imgbp.hotp.jp/CSP/IMG_SRC/69/30/B007846930/B007846930_349-262.jpg");
		imageUrls.add("http://imgbp.hotp.jp/CSP/IMG_SRC/49/17/B007734917/B007734917_349-262.jpg");

		mPager = (ViewPager) findViewById(R.id.pager);
		final MainPhotoViewPager adapter = new MainPhotoViewPager(imageUrls);
//		WindowManager wm = (WindowManager) getSystemService("window");
//		int pageWidth = wm.getDefaultDisplay().getWidth() / 3;
//		mPager.getLayoutParams().width = pageWidth;

		mPager.setAdapter(adapter);
		if (imageUrls.size() > 3) {
			mPager.setCurrentItem(3, false);
//			WindowManager wm = (WindowManager) getSystemService("window");
//			mPager.setPageMargin(-1 * wm.getDefaultDisplay().getWidth() / 3);
			mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
				@Override
				public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
					Log.d("Log", "------------------------------");
					Log.d("Log", "onPageScrolled");
					Log.d("Log", "position = " + position);
					Log.d("Log", "positionOffset = " + positionOffset);
					Log.d("Log", "positionOffsetPixels = " + positionOffsetPixels);
					Log.d("Log", "------------------------------");
					if (positionOffsetPixels == 0) {
						if (position == 0) {
							mPager.setCurrentItem(mPager.getAdapter().getCount() - 3 * 2, false);
						} else if (position == mPager.getAdapter().getCount() - 1) {
							mPager.setCurrentItem(3 * 2 - 1, false);
						}
					}
				}

				@Override
				public void onPageSelected(int position) {
					Log.d("Log", "------------------------------");
					Log.d("Log", "onPageSelected");
					Log.d("Log", "position = " + position);
					Log.d("Log", "------------------------------");
				}

				@Override
				public void onPageScrollStateChanged(int state) {
					Log.d("Log", "------------------------------");
					Log.d("Log", "onPageScrollStateChanged");
					Log.d("Log", "position = " + state);
					Log.d("Log", "------------------------------");
				}
			});
		}

		findViewById(R.id.btnLeft).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mPager.setCurrentItem(mPager.getCurrentItem() - 1, true);
			}
		});

		findViewById(R.id.btnRight).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mPager.setCurrentItem(mPager.getCurrentItem() + 1, true);
			}
		});
	}

	class MainPhotoViewPager extends android.support.v4.view.PagerAdapter {

		// 同時に表示するページ数鵜
		private static final int DISP_PAGE_COUNT = 3;
		private int mVirtualCount = 0;
		private int mRealCount = 0;
		private List<String> mImageUrls;

		public MainPhotoViewPager(List<String> imageUrls) {
			super();
			if (imageUrls == null) {
				// コンストラクタの引数はすべて必須のため１つでもnullの引数があった場合は例外を発行
				throw new IllegalArgumentException("MainPhotoViewPager null in the argument");
			}
			mImageUrls = imageUrls;
			mRealCount = mImageUrls.size();
			if (mRealCount > DISP_PAGE_COUNT) {
				// 表示ページ数より多い場合
				mVirtualCount = mRealCount + DISP_PAGE_COUNT * 2;
			} else {
				// 表示ページ数以下の場合
				mVirtualCount = mRealCount;
			}
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			TextView tv = new TextView(container.getContext());
			WindowManager wm = (WindowManager) getSystemService("window");
			tv.setWidth(wm.getDefaultDisplay().getWidth() / 3);
//			tv.setLayoutDirection((int) wm.getDefaultDisplay().getWidth() / 3);
			tv.setPadding(
					-wm.getDefaultDisplay().getWidth() / 3,
					0,
					-wm.getDefaultDisplay().getWidth() / 3,
					0);
			tv.setGravity(Gravity.CENTER);
			int virtualPosition;
			if (mVirtualCount == mRealCount) {
				// ページングなし
				virtualPosition = position;
				tv.setBackgroundColor(Color.parseColor("#FFFFFF"));
			} else {
				virtualPosition = (position - DISP_PAGE_COUNT + mRealCount) % mRealCount;
				if (0 <= position && position <= DISP_PAGE_COUNT - 1) {
					// 左側の仮ページ
					tv.setBackgroundColor(Color.parseColor("#FF0000"));
				} else if (mVirtualCount - DISP_PAGE_COUNT <= position && position <= mVirtualCount - 1) {
					// 左側の仮ページ
					tv.setBackgroundColor(Color.parseColor("#FFFF00"));
				} else {
					// 中央の実ページ
					tv.setBackgroundColor(Color.parseColor("#FFFFFF"));
				}
			}
			tv.setTextSize(100);
			tv.setText(virtualPosition + "");
			container.addView(tv);
			Log.d("Log", "------------------------------");
			Log.d("Log", "DISP_PAGE_COUNT = " + DISP_PAGE_COUNT);
			Log.d("Log", "mVirtualCount = " + mVirtualCount);
			Log.d("Log", "mRealCount = " + mRealCount);
			Log.d("Log", "position = " + position);
			Log.d("Log", "virtualPosition = " + virtualPosition);
			Log.d("Log", "------------------------------");
			return tv;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			View v = (View) object;
			container.removeView(v);
		}

		@Override
		public int getCount() {
			return mVirtualCount;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object; // false
		}

	}

}
