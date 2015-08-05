package com.example.satoutakayuki.androidtraining;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {

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

		// 画面幅を取得
		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		final int dispWidth = wm.getDefaultDisplay().getWidth();
		final int lbtnWidth = 50;
		final int rbtnWidth = 50;

		final int DISP_PAGE_COUNT = 3; //TODO: 一度に見えるページ数が偶数だとうまく動かない

		final int pageMargin = (dispWidth - lbtnWidth - rbtnWidth) / DISP_PAGE_COUNT;

		Log.d("Log", "------------------------------");
		Log.d("Log", "dispWidth = " + dispWidth);
		Log.d("Log", "lbtnWidth = " + lbtnWidth);
		Log.d("Log", "rbtnWidth = " + rbtnWidth);
		Log.d("Log", "pageMargin = " + pageMargin);
		Log.d("Log", "------------------------------");


		final MainPhotoAdapter adapter = new MainPhotoAdapter(imageUrls, pageMargin, DISP_PAGE_COUNT, this);
		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(adapter);
		mPager.setPageMargin(- pageMargin * (DISP_PAGE_COUNT - 1));
		if (imageUrls.size() > DISP_PAGE_COUNT) {
			mPager.setOffscreenPageLimit(imageUrls.size() + DISP_PAGE_COUNT * 2);
			mPager.setCurrentItem(DISP_PAGE_COUNT + (DISP_PAGE_COUNT / 2), false);
			mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
				@Override
				public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
					Log.d("Log", "------------------------------");
					Log.d("Log", "onPageScrolled");
					Log.d("Log", "position = " + position);
					Log.d("Log", "positionOffset = " + positionOffset);
					Log.d("Log", "positionOffsetPixels = " + positionOffsetPixels);
					Log.d("Log", "------------------------------");
					int realPosition = position;
					if (positionOffsetPixels == 0) {
						if (position == (DISP_PAGE_COUNT / 2)) {
							realPosition = mPager.getAdapter().getCount() - DISP_PAGE_COUNT * 2 + (DISP_PAGE_COUNT / 2);
							mPager.setCurrentItem(realPosition, false);
						}
					}
					if (positionOffsetPixels >= pageMargin - 1) {
						if (position == mPager.getAdapter().getCount() - DISP_PAGE_COUNT + (DISP_PAGE_COUNT / 2) - 1) {
							realPosition = DISP_PAGE_COUNT + (DISP_PAGE_COUNT / 2);
							mPager.setCurrentItem(realPosition, false);
						}
					}
					if (positionOffsetPixels == 0 || positionOffsetPixels >= pageMargin - 1) {
						final TextView tv = (TextView) mPager.findViewWithTag(realPosition).findViewWithTag("TextView");
						if (position == mPager.getCurrentItem()) {
							tv.setTextColor(Color.parseColor("#FF0000"));
						} else {
							tv.setTextColor(Color.parseColor("#000000"));
						}
					}
				}

				@Override
				public void onPageSelected(int position) {
					Log.d("Log", "------------------------------");
					Log.d("Log", "onPageSelected");
					Log.d("Log", "position = " + position);
					Log.d("Log", "------------------------------");
//					final TextView tv = (TextView) mPager.findViewWithTag(position).findViewWithTag("TextView");
//					tv.setBackgroundColor(Color.parseColor("#000000"));
				}

				@Override
				public void onPageScrollStateChanged(int state) {
					Log.d("Log", "------------------------------");
					Log.d("Log", "onPageScrollStateChanged");
					Log.d("Log", "position = " + state);
					Log.d("Log", "------------------------------");
				}
			});
		} else {
			mPager.setOffscreenPageLimit(DISP_PAGE_COUNT);
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

	@Override
	protected void onDestroy() {
		((MainPhotoAdapter) mPager.getAdapter()).stopImageHandler();
		super.onDestroy();
	}

	class MainPhotoAdapter extends android.support.v4.view.PagerAdapter {
		private WeakReference<Activity> mActivityHolder = null;

		private final int mPageMargin;
		private final int mDispPageCount;
		private final int mVirtualCount;
		private final int mRealCount;
		private final List<String> mImageUrlList;
		/** WEB画像（Drawable）を保持するマップ */
		private final HashMap<Integer, Drawable> mDrawerbleMap;

		/** 画像取得用のExecutorService */
		private ExecutorService mImageGetExecutor;

		/**
		 * コンストラクタ
		 *
		 * @param imageUrlList
		 * @param pageMargin
		 * @param dispPageCount
		 * @param activity
		 */
		public MainPhotoAdapter(final List<String> imageUrlList, final int pageMargin, final int dispPageCount, final Activity activity) {
			super();
			if (imageUrlList == null || activity == null) {
				// コンストラクタの引数はすべて必須のため１つでもnullの引数があった場合は例外を発行
				throw new IllegalArgumentException("MainPhotoViewPager null in the argument");
			}
			mImageUrlList = imageUrlList;
			mPageMargin = pageMargin;
			mDispPageCount = dispPageCount;
			mRealCount = mImageUrlList.size();
			if (mRealCount > mDispPageCount) {
				// 表示ページ数より多い場合
				mVirtualCount = mRealCount + mDispPageCount * 2;
			} else {
				// 表示ページ数以下の場合
				mVirtualCount = mRealCount;
			}
			mDrawerbleMap = new HashMap<Integer, Drawable>();
			mImageGetExecutor = Executors.newFixedThreadPool(mRealCount);
			mActivityHolder = new WeakReference<Activity>(activity);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			LinearLayout rootView;
			rootView = (LinearLayout) container.findViewWithTag(position);
			if (rootView == null) {
				rootView = new LinearLayout(container.getContext());
				rootView.setTag(position);
				rootView.setPadding(
						mPageMargin + 5,
						0,
						mPageMargin + 5,
						0);
				rootView.setOrientation(LinearLayout.VERTICAL);

				final TextView tv = new TextView(rootView.getContext());
				tv.setTag("TextView");
				tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				tv.setGravity(Gravity.CENTER);
				final int virtualPosition;
				if (mVirtualCount == mRealCount) {
					// ページングなし
					virtualPosition = position;
					tv.setBackgroundColor(Color.parseColor("#66FFFFFF"));
				} else {
					virtualPosition = (position - mDispPageCount + mRealCount) % mRealCount;
					if (0 <= position && position <= mDispPageCount - (mDispPageCount / 2)) {
						// 左側の仮ページ
						tv.setBackgroundColor(Color.parseColor("#66FF0000"));
					} else if (mVirtualCount - mDispPageCount + (mDispPageCount / 2) - 1 <= position && position <= mVirtualCount - (mDispPageCount / 2)) {
						// 右側の仮ページ
						tv.setBackgroundColor(Color.parseColor("#66FFFF00"));
					} else {
						// 中央の実ページ
						tv.setBackgroundColor(Color.parseColor("#66FFFFFF"));
					}
				}
				tv.setTextSize(30);
				tv.setText(virtualPosition + "");

				final ImageView iv = new ImageView(rootView.getContext());
//				iv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));

				mImageGetExecutor.execute(new ImageHandler(virtualPosition, iv));

				rootView.addView(tv);
				rootView.addView(iv);

				container.addView(rootView);
				Log.d("Log", "==============================");
				Log.d("Log", "instantiateItem(" + position + ")");
				Log.d("Log", "mDispPageCount = " + mDispPageCount);
				Log.d("Log", "mVirtualCount = " + mVirtualCount);
				Log.d("Log", "mRealCount = " + mRealCount);
				Log.d("Log", "position = " + position);
				Log.d("Log", "virtualPosition = " + virtualPosition);
				Log.d("Log", "==============================");
			} else {
				Log.d("Log", "==============================");
				Log.d("Log", "instantiateItem(" + position + ")");
				Log.d("Log", "==============================");
			}

			return rootView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			Log.d("Log", "==============================");
			Log.d("Log", "destroyItem(" + position + ")");
			Log.d("Log", "==============================");
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

		public void stopImageHandler() {
			mImageGetExecutor.shutdown();
			try {
				// 既存タスクの終了を待つ
				if (!mImageGetExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
					// 現在実行中のタスクをキャンセル
					mImageGetExecutor.shutdownNow();
					// キャンセル処理に反応するために待つ
					if (!mImageGetExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
						Log.w("Log", "shutdownAndAwaitTermination Pool did not terminate");
					}
				}
			} catch (final InterruptedException ie) {
				// 現在のスレッドを遮るならタスクをキャンセル
				mImageGetExecutor.shutdownNow();
				// 現在のスレッドの割り込みを維持
				Thread.currentThread().interrupt();
			}
			mImageGetExecutor = null;
		}

		/**
		 * 画像取得ハンドラ
		 */
		private class ImageHandler implements Runnable {
			/** サロンヘッダーを表示するImageView */
			private WeakReference<ImageView> mImageViewHolder = null;

			private final int mImageIndex;

			/**
			 * コンストラクタ
			 * @param index 画像のインデックス
			 */
			public ImageHandler(final int index, final ImageView imageView) {
				mImageIndex = index;
				mImageViewHolder = new WeakReference<ImageView>(imageView);
			}

			public void run() {
				try {
					// WEBから画像を取得
					if (!mDrawerbleMap.containsKey(mImageIndex)) {
						// 未取得の画像だった場合取得を実行する
						final InputStream is = new URL(mImageUrlList.get(mImageIndex)).openStream();
						mDrawerbleMap.put(mImageIndex, Drawable.createFromStream(is, ""));
						is.close();
					}

					if (!mActivityHolder.get().isFinishing()) {
						// 画像が未設定の場合、最初に取得できた画像を設定
//						mIndex = mImageIndex;
						mActivityHolder.get().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								mImageViewHolder.get().setImageDrawable(mDrawerbleMap.get(mImageIndex));
							}
						});
					}
				} catch (final Exception ignored) {
					Log.w(ignored.getClass().getCanonicalName(), ignored.getMessage(), ignored);
				}
			}
		}

	}

}
