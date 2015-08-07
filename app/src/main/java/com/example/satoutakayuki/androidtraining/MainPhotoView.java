package com.example.satoutakayuki.androidtraining;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * メインフォト用レイアウト（メインフォト＋サムネイル）
 */
public class MainPhotoView extends LinearLayout {

	// ----------------------------------------
	// 定数値
	// ----------------------------------------
	/** メインフォト切り替え時のクロスフェードアニメーション時間（ミリ秒） */
	private static final int CROSS_FADE_ANIMATION_DURATION_MILLIS = 300;
	/** 一度に見えるサムネイル数 */
	private static final int DISP_THUMBNAIL_COUNT = 3; //TODO:偶数だとうまく動かない・・・とりあえず３で最適化

	// ----------------------------------------
	// MainPhotoView独自属性用の変数
	// ----------------------------------------
	/** サムネイルの自動ページ送りのON/OFF */
	private boolean mThumbnailAutoPagingOn = true;
	/** サムネイルの自動ページ送りの周期(ミリ秒) */
	private long mThumbnailAutoPagingDuration = 5000L;
	/** サムネイルの自動ページ送りのスクロール速度(ミリ秒) */
	private int mThumbnailAutoPagingScrollSpeed = 200;
	/** サムネイルのフォーカス色（アルファ値まで指定しないと塗りつぶしてしまう） */
	private int mThumbnailFocusedColor = Color.parseColor("#66000000");
	/** サムネイルの非フォーカス色 */
	private int mThumbnailUnFocusedColor = Color.parseColor("#00000000");

	// ----------------------------------------
	// Views
	// ----------------------------------------
	/** メインフォト表示用ImageView */
	private ImageView mMainPhotoImageView;
	/** サムネイル表示用ViewPager */
	private ThumbnailViewPager mThumbnailViewPager;

	// ----------------------------------------
	// Field
	// ----------------------------------------
	private int lbtnWidth = 50;
	private int rbtnWidth = 50;
	private int mPageMargin = 0;
	private List<String> mMainPhotoUrlList = new ArrayList<String>();

	/**
	 * コンストラクタ
	 * @param context
	 */
	public MainPhotoView(final Context context) {
		super(context);
		initLayout(context, null);
	}

	/**
	 * コンストラクタ
	 * @param context
	 * @param attrs
	 */
	public MainPhotoView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		initLayout(context, attrs);
	}

	/**
	 * レイアウトの初期化
	 * @param context
	 * @param attrs
	 */
	private void initLayout(final Context context, final AttributeSet attrs) {
		if (attrs != null) {
			final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MainPhotoView);

			mThumbnailAutoPagingOn = ta.getBoolean(R.styleable.MainPhotoView_thumbnail_auto_paging_on, mThumbnailAutoPagingOn);
			mThumbnailAutoPagingDuration = (long) ta.getInteger(R.styleable.MainPhotoView_thumbnail_auto_paging_duration, Integer.valueOf(Long.toString(mThumbnailAutoPagingDuration)));
			mThumbnailAutoPagingScrollSpeed = ta.getInteger(R.styleable.MainPhotoView_thumbnail_auto_paging_scroll_speed, mThumbnailAutoPagingScrollSpeed);
			mThumbnailFocusedColor = ta.getColor(R.styleable.MainPhotoView_thumbnail_focused_color, mThumbnailFocusedColor);
			mThumbnailUnFocusedColor = ta.getColor(R.styleable.MainPhotoView_thumbnail_unfocused_color, mThumbnailUnFocusedColor);
		}

		// レイアウト定義をインフレート
		final View rootLayout = LayoutInflater.from(context).inflate(R.layout.layout_main_photo_view, this);
		rootLayout.setVisibility(View.GONE);

//		WindowManager wm = (WindowManager) getContext().getSystemService(Activity.WINDOW_SERVICE);
//		final int dispWidth = wm.getDefaultDisplay().getWidth();

		// メインフォト表示用ImageViewのインスタンスを取得
		mMainPhotoImageView = (ImageView) this.findViewById(R.id.main_photo_image_view);
		// サムネイル表示用ViewPagerのインスタンスを取得
		mThumbnailViewPager = (ThumbnailViewPager) this.findViewById(R.id.thumbnail_view_pager);
//		mThumbnailViewPager.setAdapter(null);
		// カスタムViewの幅が確定してから子Viewの初期化をする
		getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			/** 初期化済みフラグ */
			private boolean isInitalized = false;

			@Override
			public void onGlobalLayout() {
				final int mainPhotoWidth = mMainPhotoImageView.getWidth();
//				final int thumbnailWidth = mThumbnailViewPager.getWidth();
				if (!isInitalized && mainPhotoWidth != 0/*&& thumbnailWidth != 0 */) {
					// サムネイル表示用ViewPagerのマージンを計算
					mPageMargin = (mainPhotoWidth - lbtnWidth - rbtnWidth) / DISP_THUMBNAIL_COUNT;
//					mPageMargin = thumbnailWidth / DISP_THUMBNAIL_COUNT;

					// メインフォト表示用ImageViewの初期化
					mMainPhotoImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
					mMainPhotoImageView.getLayoutParams().width = mainPhotoWidth;
					mMainPhotoImageView.getLayoutParams().height = mainPhotoWidth * 3 / 4;

					// サムネイル表示用ViewPagerの初期化
					mThumbnailViewPager.setScrollSpeed(mThumbnailAutoPagingScrollSpeed);
					mThumbnailViewPager.setPageMargin(-mPageMargin * (DISP_THUMBNAIL_COUNT - 1));
					mThumbnailViewPager.requestLayout();
					Log.d("Log", "------------------------------");
					Log.d("Log", "onGlobalLayout");
					Log.d("Log", "mainPhotoWidth = " + mainPhotoWidth);
//					Log.d("Log", "thumbnailWidth = " + thumbnailWidth);
					Log.d("Log", "lbtnWidth = " + lbtnWidth);
					Log.d("Log", "rbtnWidth = " + rbtnWidth);
					Log.d("Log", "pageMargin = " + mPageMargin);
					Log.d("Log", "getRight() = " + getRight());
					Log.d("Log", "getLeft() = " + getLeft());
					Log.d("Log", "getWidth(2) = " + getWidth());
					Log.d("Log", "------------------------------");

					isInitalized = true;
				}
			}
		});

	}

	/**
	 * メインフォトの画像URLのリストをもとにadapterを生成し、ViewPagerに設定
	 * @param mainPhotoUrlList メインフォトの画像URLのリスト
	 * @param activity {@link Activity}
	 */
	public void setMainPhotoUrlList(final List<String> mainPhotoUrlList, final Activity activity) {
		if (mainPhotoUrlList == null || activity == null) {
			throw new IllegalArgumentException();
		}

		// メインフォトの画像URLのリストを再設定
		mMainPhotoUrlList.clear();
		mMainPhotoUrlList.addAll(mainPhotoUrlList);

		final ThumbnailAdapter adapter = new ThumbnailAdapter(mMainPhotoUrlList, mPageMargin, DISP_THUMBNAIL_COUNT, activity);
		mThumbnailViewPager.setAdapter(adapter);
		if (mMainPhotoUrlList.size() > DISP_THUMBNAIL_COUNT) {
			mThumbnailViewPager.setOffscreenPageLimit(mMainPhotoUrlList.size() + DISP_THUMBNAIL_COUNT * 2);//TODO: 適正値を模索中
			mThumbnailViewPager.setCurrentItem(DISP_THUMBNAIL_COUNT + (DISP_THUMBNAIL_COUNT / 2), false);
			mThumbnailViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
				@Override
				public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
					int virtualPosition = (position - DISP_THUMBNAIL_COUNT + mMainPhotoUrlList.size()) % mMainPhotoUrlList.size();
					Log.d("Log", "------------------------------");
					Log.d("Log", "onPageScrolled");
					Log.d("Log", "virtualPosition = " + virtualPosition);
					Log.d("Log", "position = " + position);
					Log.d("Log", "positionOffset = " + positionOffset);
					Log.d("Log", "positionOffsetPixels = " + positionOffsetPixels);
					Log.d("Log", "------------------------------");
					int realPosition = position;
					if (positionOffsetPixels == 0) {
						if (position == (DISP_THUMBNAIL_COUNT / 2)) {
							realPosition = mThumbnailViewPager.getAdapter().getCount() - DISP_THUMBNAIL_COUNT * 2 + (DISP_THUMBNAIL_COUNT / 2);
							mThumbnailViewPager.setCurrentItem(realPosition, false);
						}
					}
					if (positionOffsetPixels >= mPageMargin - 5) {
						if (position == mThumbnailViewPager.getAdapter().getCount() - DISP_THUMBNAIL_COUNT + (DISP_THUMBNAIL_COUNT / 2) - 1) {
							realPosition = DISP_THUMBNAIL_COUNT + (DISP_THUMBNAIL_COUNT / 2);
							mThumbnailViewPager.setCurrentItem(realPosition, false);
						}
					}
					if (positionOffsetPixels == 0 || positionOffsetPixels >= mPageMargin - 5) {
						final TextView tv = (TextView) findViewWithTag(realPosition).findViewWithTag("TextView");
						if (position == mThumbnailViewPager.getCurrentItem()) {
//							tv.setTextColor(Color.parseColor("#FF0000"));
						} else {
//							tv.setTextColor(Color.parseColor("#000000"));
						}
					}
				}

				@Override
				public void onPageSelected(int position) {
					int virtualPosition = (position - DISP_THUMBNAIL_COUNT + mMainPhotoUrlList.size()) % mMainPhotoUrlList.size();
					Log.d("Log", "------------------------------");
					Log.d("Log", "onPageSelected");
					Log.d("Log", "virtualPosition = " + virtualPosition);
					Log.d("Log", "position = " + position);
					Log.d("Log", "------------------------------");
//					final TextView tv = (TextView) mPager.findViewWithTag(position).findViewWithTag("TextView");
//					tv.setBackgroundColor(Color.parseColor("#000000"));
				}

				@Override
				public void onPageScrollStateChanged(int state) {
					Log.d("Log", "------------------------------");
					Log.d("Log", "onPageScrollStateChanged");
					Log.d("Log", "state = " + state);
					Log.d("Log", "------------------------------");
					if (state == ViewPager.SCROLL_STATE_IDLE) {
						// 無操作状態
						findViewById(R.id.button_preview).setEnabled(true);
						findViewById(R.id.button_next).setEnabled(true);
						adapter.removeAutoPagingHandler();
					} else {
						//
						findViewById(R.id.button_preview).setEnabled(false);
						findViewById(R.id.button_next).setEnabled(false);
						adapter.setAutoPagingHandler();
					}
				}
			});
			// 前のページに移動
			findViewById(R.id.button_preview).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mThumbnailViewPager.setCurrentItem(mThumbnailViewPager.getCurrentItem() - 1, true);
				}
			});

			// 次のページに移動
			findViewById(R.id.button_next).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mThumbnailViewPager.setCurrentItem(mThumbnailViewPager.getCurrentItem() + 1, true);
				}
			});
		} else {
			mThumbnailViewPager.setOffscreenPageLimit(DISP_THUMBNAIL_COUNT);
			mThumbnailViewPager.setCurrentItem(2);
			mThumbnailViewPager.setSwipeEnabled(false);
			// 前のページに移動
			findViewById(R.id.button_preview).setVisibility(View.INVISIBLE);
			// 次のページに移動
			findViewById(R.id.button_next).setVisibility(View.INVISIBLE);
		}

		adapter.setAutoPagingHandler();
		this.setVisibility(View.VISIBLE);
	}

	private void refreshViewPager() {
		final int currentPosition = mThumbnailViewPager.getCurrentItem();
		FrameLayout[] rootViews = new FrameLayout[DISP_THUMBNAIL_COUNT * 2];
		for (int i = 0 ; i <= DISP_THUMBNAIL_COUNT * 2 - 1 ; i++) {
			rootViews[i] = (FrameLayout) mThumbnailViewPager.findViewWithTag(currentPosition + i - DISP_THUMBNAIL_COUNT);
		}

		final ThumbnailAdapter adapter = (ThumbnailAdapter) mThumbnailViewPager.getAdapter();
		for (int i = 0 ; i <= DISP_THUMBNAIL_COUNT * 2 - 1 ; i++) {
			if (rootViews[i] != null) {
				int position = currentPosition + i - DISP_THUMBNAIL_COUNT;
				int virtualPosition = (position - DISP_THUMBNAIL_COUNT + mMainPhotoUrlList.size()) % mMainPhotoUrlList.size();
				final TextView tv = (TextView) rootViews[i].findViewWithTag("TextView");
				if (adapter.getFocusedPosition() == virtualPosition) {
					tv.setBackgroundColor(mThumbnailFocusedColor);
				} else {
					tv.setBackgroundColor(mThumbnailUnFocusedColor);
				}
				// TODO:デバッグコード　カレントページの文字色を変更
				if (BuildConfig.DEBUG) {
					if (currentPosition == position) {
						tv.setTextColor(Color.parseColor("#0000FF"));
					} else {
						tv.setTextColor(Color.parseColor("#000000"));
					}
				}
			}
		}
		adapter.setAutoPagingHandler();
	}

	public void stopImageHandler() {
		((ThumbnailAdapter) mThumbnailViewPager.getAdapter()).stopImageHandler();
	}

	// ----------------------------------------
	// インナークラス
	// ----------------------------------------
	/**
	 * サムネイル用ViewPager（スワイプ無効化制御用）
	 */
	public static class ThumbnailViewPager extends ViewPager {

		/** スワイプの有効・無効の設定 */
		private boolean mSwipeEnabled = true;

		public ThumbnailViewPager(Context context) {
			super(context);
		}

		public ThumbnailViewPager(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		/**
		 * 自動スクロール時間を設定
		 * @param scrollSpeed スクロール時間（ミリ秒）
		 */
		public void setScrollSpeed(final int scrollSpeed) {
			if (scrollSpeed <= 0) {
				return;
			}
			try {
				Field mScroller;
				mScroller = ViewPager.class.getDeclaredField("mScroller");
				mScroller.setAccessible(true);
				Interpolator sInterpolator = new AccelerateInterpolator();
				FixedSpeedScroller scroller = new FixedSpeedScroller(this.getContext(), sInterpolator, scrollSpeed);
				mScroller.set(this, scroller);
			} catch (final Exception ignored) {
				Log.w(ignored.getClass().getCanonicalName(), ignored.getMessage(), ignored);
			}
		}

		@Override
		public boolean onInterceptTouchEvent(final MotionEvent event) {
			return mSwipeEnabled;
		}

		@Override
		public boolean onTouchEvent(final MotionEvent event) {
			return mSwipeEnabled;
		}

		/**
		 * スワイプの有効・無効の設定
		 * @param enabled
		 */
		public void setSwipeEnabled(final boolean enabled) {
			mSwipeEnabled = enabled;
		}
	}

	/**
	 * サムネイル用PagerAdapter（無限ループ可能）
	 */
	private class ThumbnailAdapter extends PagerAdapter {

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

		private int mFocusedPosition = 0;
//		private CrossFadeHandler mCrossFadeHandler;
		// ヘッダー・フッターの再表示用のメッセージコード
		private static final int HANDLER_CROSS_FADE = 1;
		// スクロール停止時に呼ばれるハンドラーに渡されるメッセージ
		private Message mMessage = null;
		// 自動ページ送り用のハンドラー
		private final Handler mAutoPagingHandler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(final Message msg) {
				if (msg == null) {
					return false;
				}

				if (msg.what == HANDLER_CROSS_FADE) {
					final Drawable[] drawables = new Drawable[2];
					int nextFocusedPosition = (mFocusedPosition + 1) % mMainPhotoUrlList.size();
					drawables[0] = mDrawerbleMap.get(mFocusedPosition);
					drawables[1] = mDrawerbleMap.get(nextFocusedPosition);

					if (drawables[0] != null && drawables[1] != null) {
						final TransitionDrawable td = new TransitionDrawable(drawables);
						mMainPhotoImageView.setImageDrawable(td);
						td.startTransition(CROSS_FADE_ANIMATION_DURATION_MILLIS);
					} else if (drawables[1] != null) {
						mMainPhotoImageView.setImageDrawable(drawables[1]);
					}
					//------------------------------------------
					int virtualCurrentPosition = (mThumbnailViewPager.getCurrentItem() + 1 - DISP_THUMBNAIL_COUNT + mMainPhotoUrlList.size()) % mMainPhotoUrlList.size();
					if (mFocusedPosition == virtualCurrentPosition) {
						mThumbnailViewPager.setCurrentItem(mThumbnailViewPager.getCurrentItem() + 1, true);
					}
					mFocusedPosition = nextFocusedPosition;
					refreshViewPager();
				}
				return false;
			}
		});

		/**
		 * サムネイルの自動ページ送り用ハンドラの設定（起動）
		 */
		public void setAutoPagingHandler() {
			if (mThumbnailAutoPagingOn) {
				// 自動ページ送りが有効の場合
				removeAutoPagingHandler();
				/*
				 * Note) Callbackが設定されているとhandleMessage()に通知されない。
				 * 参照：Handler.dispatchMessage()
				 */
				// メッセージを取得
				mMessage = mAutoPagingHandler.obtainMessage(HANDLER_CROSS_FADE, this);

				// mAutoPagingDurationミリ秒の遅延でメッセージを送信
				if (!mAutoPagingHandler.sendMessageDelayed(mMessage, mThumbnailAutoPagingDuration)) {
					Log.d("Log", " - Failed send message.");
				} else {
					Log.d("Log", " - Success send message.");
				}
			}
		}

		/**
		 * サムネイルの自動ページ送り用ハンドラからメッセージ削除
		 */
		public void removeAutoPagingHandler() {
			if (mMessage != null && mAutoPagingHandler.hasMessages(HANDLER_CROSS_FADE, this)) {
				// 既にメッセージが追加されている場合はメッセージを削除
				mAutoPagingHandler.removeMessages(HANDLER_CROSS_FADE, this);
			}
		}

		/**
		 * コンストラクタ
		 *
		 * @param imageUrlList
		 * @param pageMargin
		 * @param dispPageCount 一度に表示されるページ数
		 * @param activity {@link Activity}
		 */
		public ThumbnailAdapter(final List<String> imageUrlList, final int pageMargin, final int dispPageCount, final Activity activity) {
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
				mVirtualCount = mDispPageCount;
			}
			mDrawerbleMap = new HashMap<Integer, Drawable>();
			mImageGetExecutor = Executors.newFixedThreadPool(mRealCount);
			mActivityHolder = new WeakReference<Activity>(activity);
		}

		@Override
		public Object instantiateItem(final ViewGroup container, final int position) {
			FrameLayout rootView;
			rootView = (FrameLayout) container.findViewWithTag(position);
			if (rootView == null) {
				int h = (mPageMargin - 5 * 2) * 3 / 4;
				rootView = new FrameLayout(container.getContext());
				rootView.setTag(position);
				rootView.setPadding(
						mPageMargin + 5,
						0,
						mPageMargin + 5,
						0);
//				rootView.setOrientation(LinearLayout.VERTICAL);


				final TextView tv = new TextView(rootView.getContext());
				tv.setTag("TextView");
				tv.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h));
				tv.setGravity(Gravity.CENTER);
				final int virtualPosition;
				if (mVirtualCount == mRealCount) {
					// ページングなし
					virtualPosition = position;
//					tv.setBackgroundColor(Color.parseColor("#66FFFFFF"));
				} else {
					virtualPosition = (position - mDispPageCount + mRealCount) % mRealCount;
					if (0 <= position && position <= mDispPageCount - (mDispPageCount / 2)) {
						// 左側の仮ページ
//						tv.setBackgroundColor(Color.parseColor("#66FF0000"));
					} else if (mVirtualCount - mDispPageCount + (mDispPageCount / 2) - 1 <= position && position <= mVirtualCount - (mDispPageCount / 2)) {
						// 右側の仮ページ
//						tv.setBackgroundColor(Color.parseColor("#66FFFF00"));
					} else {
						// 中央の実ページ
//						tv.setBackgroundColor(Color.parseColor("#66FFFFFF"));
					}
				}
				tv.setTextSize(30);
				tv.setText(virtualPosition + "");

				final ImageView iv = new ImageView(rootView.getContext());
				iv.setTag("ImageView");
				iv.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h));
				iv.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
//						((TextView) container.findViewWithTag(position).findViewWithTag("TextView")).setTextColor(Color.parseColor("#FF0000"));
						if (mDrawerbleMap.containsKey(virtualPosition) && mFocusedPosition != virtualPosition) {
							//------------------------------------------
							final Drawable[] drawables = new Drawable[2];
							drawables[0] = mDrawerbleMap.get(mFocusedPosition);
							drawables[1] = mDrawerbleMap.get(virtualPosition);

							if (drawables[0] != null && drawables[1] != null) {
								final TransitionDrawable td = new TransitionDrawable(drawables);
								mMainPhotoImageView.setImageDrawable(td);
								td.startTransition(CROSS_FADE_ANIMATION_DURATION_MILLIS);
							} else if (drawables[1] != null) {
								mMainPhotoImageView.setImageDrawable(drawables[1]);
							}
							//------------------------------------------
						}
						mFocusedPosition = virtualPosition;
						refreshViewPager();
					}
				});
//				iv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));

				mImageGetExecutor.execute(new ImageGetHandler(virtualPosition, iv));

				rootView.addView(iv);
				rootView.addView(tv);

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
		public void destroyItem(final ViewGroup container, final int position, final Object object) {
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
		public boolean isViewFromObject(final View view, final Object object) {
			return view == object; // false
		}

		@Override
		public void startUpdate(ViewGroup container) {
			super.startUpdate(container);
			Log.d("Log", "==============================");
			Log.d("Log", "startUpdate");
			Log.d("Log", "==============================");
		}

		@Override
		public void finishUpdate(ViewGroup container) {
			super.finishUpdate(container);
			Log.d("Log", "==============================");
			Log.d("Log", "finishUpdate");
			Log.d("Log", "==============================");
			refreshViewPager();
		}

		/**
		 * 画像取得ハンドラの停止
		 */
		void stopImageHandler() {
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

		public int getFocusedPosition() {
			return mFocusedPosition;
		}

		/**
		 * 画像取得ハンドラ
		 */
		private class ImageGetHandler implements Runnable {
			/** メインフォトのサムネイルを表示するImageView */
			private WeakReference<ImageView> mImageViewHolder = null;

			private final int mImageIndex;

			/**
			 * コンストラクタ
			 * @param index 画像のインデックス
			 * @param imageView メインフォトのサムネイルを表示するImageView
			 */
			public ImageGetHandler(final int index, final ImageView imageView) {
				mImageIndex = index;
				mImageViewHolder = new WeakReference<ImageView>(imageView);
			}

			public void run() {
				try {
//					if (mImageViewHolder.get().getDrawable() == null) {
//						mImageViewHolder.get().setImageResource(0);
//					}
					// 未取得の画像だった場合、WEBから画像を取得
					if (!mDrawerbleMap.containsKey(mImageIndex)) {
						final InputStream is = new URL(mImageUrlList.get(mImageIndex)).openStream();
						mDrawerbleMap.put(mImageIndex, Drawable.createFromStream(is, ""));
						is.close();
					}
					if (!mActivityHolder.get().isFinishing()) {
						mActivityHolder.get().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								mImageViewHolder.get().setImageDrawable(mDrawerbleMap.get(mImageIndex));
								if (mMainPhotoImageView.getDrawable() == null && mImageIndex == 0) {
									mMainPhotoImageView.setImageDrawable(mDrawerbleMap.get(mImageIndex));
								}
							}
						});
					}
				} catch (final Exception ignored) {
					Log.w(ignored.getClass().getCanonicalName(), ignored.getMessage(), ignored);
				}
			}
		}
	}

	/**
	 * サムネイルの自動ページ送りのスクロール速度を変更するScroller
	 */
	private static class FixedSpeedScroller extends Scroller {

		private int mDuration;

		public FixedSpeedScroller(Context context, Interpolator interpolator, int duration) {
			super(context, interpolator);
			mDuration = duration;
		}

		@Override
		public void startScroll(int startX, int startY, int dx, int dy, int duration) {
			super.startScroll(startX, startY, dx, dy, mDuration);
		}

		@Override
		public void startScroll(int startX, int startY, int dx, int dy) {
			super.startScroll(startX, startY, dx, dy, mDuration);
		}
	}
}
