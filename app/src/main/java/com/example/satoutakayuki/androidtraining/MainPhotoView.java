package com.example.satoutakayuki.androidtraining;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
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

	// ----------------------------------------
	// MainPhotoView独自属性用の変数
	// ----------------------------------------
	/** メインフォトのアスペクト比（w） */
	private int mMainPhotoAspectRatioW = 4;
	/** メインフォトのアスペクト比（h） */
	private int mMainPhotoAspectRatioH = 3;
	/** 一度に見えるサムネイル数 */
	private int mThumbnailDispCount = 3;
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
	/** サムネイル間のマージン(px) */
	private int mThumbnailMargin = 0;

	// ----------------------------------------
	// 変数
	// ----------------------------------------
	/** メインフォトのURLリスト */
	private List<String> mMainPhotoUrlList = new ArrayList<String>();

	// ----------------------------------------
	// Views
	// ----------------------------------------
	/** メインフォト表示用ImageView */
	private ImageView mMainPhotoImageView;
	/** サムネイル表示用ViewPager */
	private ThumbnailViewPager mThumbnailViewPager;

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
			// 独自属性の設定
			final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MainPhotoView);
			mMainPhotoAspectRatioW = ta.getInteger(R.styleable.MainPhotoView_main_photo_aspect_ratio_w, mMainPhotoAspectRatioW);
			mMainPhotoAspectRatioH = ta.getInteger(R.styleable.MainPhotoView_main_photo_aspect_ratio_h, mMainPhotoAspectRatioH);
			mThumbnailDispCount = ta.getInteger(R.styleable.MainPhotoView_thumbnail_disp_count, mThumbnailDispCount);
			mThumbnailAutoPagingOn = ta.getBoolean(R.styleable.MainPhotoView_thumbnail_auto_paging_on, mThumbnailAutoPagingOn);
			mThumbnailAutoPagingDuration = (long) ta.getInteger(R.styleable.MainPhotoView_thumbnail_auto_paging_duration, Integer.valueOf(Long.toString(mThumbnailAutoPagingDuration)));
			mThumbnailAutoPagingScrollSpeed = ta.getInteger(R.styleable.MainPhotoView_thumbnail_auto_paging_scroll_speed, mThumbnailAutoPagingScrollSpeed);
			mThumbnailFocusedColor = ta.getColor(R.styleable.MainPhotoView_thumbnail_focused_color, mThumbnailFocusedColor);
			mThumbnailUnFocusedColor = ta.getColor(R.styleable.MainPhotoView_thumbnail_unfocused_color, mThumbnailUnFocusedColor);
			mThumbnailMargin = ta.getDimensionPixelSize(R.styleable.MainPhotoView_thumbnail_margin, mThumbnailMargin) / 2;
		}

		// レイアウトをインフレート
		final View rootLayout = LayoutInflater.from(context).inflate(R.layout.layout_main_photo_view, this);
		rootLayout.setVisibility(View.GONE);

		// メインフォト表示用ImageViewのインスタンスを取得
		mMainPhotoImageView = (ImageView) this.findViewById(R.id.main_photo_image_view);
		// サムネイル表示用ViewPagerのインスタンスを取得
		mThumbnailViewPager = (ThumbnailViewPager) this.findViewById(R.id.thumbnail_view_pager);

		// カスタムViewの幅が確定してから子Viewの高さを初期化
		getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			/** 初期化済みフラグ */
			private boolean isInitalized = false;

			@Override
			public void onGlobalLayout() {
				final int mainPhotoWidth = mMainPhotoImageView.getWidth();
				final int thumbnailWidth = mThumbnailViewPager.getWidth();

				if (!isInitalized && mainPhotoWidth != 0 && thumbnailWidth != 0) {

					// メインフォト表示用ImageViewの初期化
					mMainPhotoImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
					mMainPhotoImageView.getLayoutParams().width = mainPhotoWidth;
					mMainPhotoImageView.getLayoutParams().height = mainPhotoWidth * mMainPhotoAspectRatioH / mMainPhotoAspectRatioW;

					// サムネイル表示用ViewPagerの初期化
					final int h = ((thumbnailWidth / mThumbnailDispCount) - 5 * 2) * mMainPhotoAspectRatioH / mMainPhotoAspectRatioW;
					mThumbnailViewPager.setScrollSpeed(mThumbnailAutoPagingScrollSpeed);
					mThumbnailViewPager.getLayoutParams().height = h;
					mThumbnailViewPager.requestLayout();

					// 「＜」「＞」ボタンの初期化
					findViewById(R.id.button_preview).getLayoutParams().height = h;
					findViewById(R.id.button_next).getLayoutParams().height = h;

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

		final ThumbnailAdapter adapter = new ThumbnailAdapter(mMainPhotoUrlList, activity);
		mThumbnailViewPager.setAdapter(adapter);

		// サムネイル表示用ViewPagerの設定
		if (mMainPhotoUrlList.size() > mThumbnailDispCount) {
			// サムネイル表示枚数より表示データの方が多い場合
			mThumbnailViewPager.setOffscreenPageLimit(mMainPhotoUrlList.size() + mThumbnailDispCount * 2);//TODO: 適正値を模索中
			mThumbnailViewPager.setCurrentItem(mThumbnailDispCount, false);
			mThumbnailViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
				@Override
				public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
					// ignore
//					Log.d("Log", "------------------------------");
//					Log.d("Log", "onPageScrolled(" + position + ")");
//					Log.d("Log", "position = " + position);
//					Log.d("Log", "positionOffset = " + positionOffset);
//					Log.d("Log", "positionOffsetPixels = " + positionOffsetPixels);
//					Log.d("Log", "------------------------------");
				}

				@Override
				public void onPageSelected(int position) {
					// ignore
//					Log.d("Log", "------------------------------");
//					Log.d("Log", "onPageSelected(" + position + ")");
//					Log.d("Log", "------------------------------");
				}

				@Override
				public void onPageScrollStateChanged(int state) {
					int position = mThumbnailViewPager.getCurrentItem();
					int virtualPosition = position;
					if (state == ViewPager.SCROLL_STATE_IDLE) {
						// 無操作状態
						findViewById(R.id.button_preview).setEnabled(true);
						findViewById(R.id.button_next).setEnabled(true);
						adapter.setAutoPagingHandler();

						// ポジションの循環処理
						if (position == 0) {
							// スクロール可能範囲の一番左のページの場合
							// サムネイルの表示枚数分オフセットした右側のポジションへ移動
							virtualPosition = mThumbnailViewPager.getAdapter().getCount() - mThumbnailDispCount * 2;
							mThumbnailViewPager.setCurrentItem(virtualPosition, false);
						} else if (position == mThumbnailViewPager.getAdapter().getCount() - mThumbnailDispCount) {
							// スクロール可能範囲の一番右のページの場合
							// サムネイルの表示枚数分オフセットした左側のポジションへ移動
							virtualPosition = mThumbnailDispCount;
							mThumbnailViewPager.setCurrentItem(virtualPosition, false);
						}
					} else {
						// ページ操作中
						findViewById(R.id.button_preview).setEnabled(false);
						findViewById(R.id.button_next).setEnabled(false);
						adapter.removeAutoPagingHandler();
					}
//					Log.d("Log", "------------------------------");
//					Log.d("Log", "onPageScrollStateChanged");
//					Log.d("Log", "state = " + state);
//					Log.d("Log", "CurrentPosition(Now ) = " + position);
//					Log.d("Log", "CurrentPosition(Next) = " + virtualPosition);
//					Log.d("Log", "------------------------------");
				}
			});
			mThumbnailViewPager.setSwipeEnabled(true);

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
			// サムネイル表示枚数より表示データの方が少ない場合
			mThumbnailViewPager.setOffscreenPageLimit(mThumbnailDispCount);
			mThumbnailViewPager.setCurrentItem(0);
			mThumbnailViewPager.setSwipeEnabled(false);
			// 前のページに移動
			findViewById(R.id.button_preview).setVisibility(View.INVISIBLE);
			// 次のページに移動
			findViewById(R.id.button_next).setVisibility(View.INVISIBLE);
		}

		// 自動ページ送り開始
		adapter.setAutoPagingHandler();
		this.setVisibility(View.VISIBLE);
	}

	/**
	 * Viewの更新
	 */
	private void refreshViewPager() {
		int offsetPos = 0;
		int currentPosition = mThumbnailViewPager.getCurrentItem();
		if (mThumbnailDispCount > mMainPhotoUrlList.size()) {
			offsetPos = 1;
			currentPosition = 0;
		}

		FrameLayout rootView;
		TextView tv;
		final ThumbnailAdapter adapter = (ThumbnailAdapter) mThumbnailViewPager.getAdapter();
		Log.d("Log", "------------------------------");
		Log.d("Log", "refreshViewPager");
		Log.d("Log", "currentPosition = " + currentPosition);
		Log.d("Log", "getFocusedPosition() = " + adapter.getFocusedPosition());
		Log.d("Log", "------------------------------");
		for (int i = -1 + offsetPos ; i < mThumbnailDispCount + 1 - offsetPos ; i++) {
			rootView = (FrameLayout) mThumbnailViewPager.findViewWithTag(currentPosition * (1 - offsetPos) + i);
			if (rootView != null) {
				tv = (TextView) rootView.findViewWithTag("TextView");
				if (tv != null) {
					int position = currentPosition + mThumbnailDispCount * (-1 + offsetPos) + i;
					int virtualPosition = (position + mMainPhotoUrlList.size()) % mMainPhotoUrlList.size();
					if (adapter.getFocusedPosition() == virtualPosition) {
						tv.setBackgroundColor(mThumbnailFocusedColor);
					} else {
						tv.setBackgroundColor(mThumbnailUnFocusedColor);
					}
				}
			}
		}
		adapter.setAutoPagingHandler();
	}

	public void stopHandlers() {
		Log.d("Log", "stopImageGetHandler");
		((ThumbnailAdapter) mThumbnailViewPager.getAdapter()).stopImageGetHandler();
		((ThumbnailAdapter) mThumbnailViewPager.getAdapter()).removeAutoPagingHandler();
	}

	// ----------------------------------------
	// インナークラス
	// ----------------------------------------
	/**
	 * サムネイル表示用ViewPager（スワイプ無効化制御用）
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
				AutoPagingSpeedControlScroller scroller = new AutoPagingSpeedControlScroller(this.getContext(), sInterpolator, scrollSpeed);
				mScroller.set(this, scroller);
				requestLayout();
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
		/** 自動ページ送り用のメッセージコード */
		private static final int HANDLER_AUTO_PAGING = 1;

		/** WEB画像のURLを保持するリスト */
		private final List<String> mImageUrlList;
		/** WEB画像（Drawable）を保持するマップ */
		private final HashMap<Integer, Drawable> mDrawerbleMap;

		/** 循環ページ送りするための仮想ページ数 */
		private final int mVirtualPageCount;
		/** サムネイルの実際のページ数（データ数） */
		private final int mRealPageCount;

		/** 呼び出し元のActivity（弱参照） */
		private WeakReference<Activity> mActivityHolder = null;

		/** WEB画像取得用のExecutorService */
		private ExecutorService mImageGetExecutor;

		/** フォーカスされている画像のポジション */
		private int mFocusedPosition = 0;

		/** 自動ページ送り用のハンドラーに渡されるメッセージ */
		private Message mMessage = null;
		/** 自動ページ送り用のハンドラー */
		private final Handler mAutoPagingHandler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(final Message msg) {
				if (msg == null) {
					return false;
				}

				if (msg.what == HANDLER_AUTO_PAGING) {
					final Drawable[] drawables = new Drawable[2];
					final int nextFocusedPosition = (mFocusedPosition + 1) % mMainPhotoUrlList.size();
					// 表示中の画像
					drawables[0] = mDrawerbleMap.get(mFocusedPosition);
					// 次に表示する画像
					drawables[1] = mDrawerbleMap.get(nextFocusedPosition);

					if (drawables[0] != null && drawables[1] != null) {
						// 両方とも画像取得済みの場合、クロスフェードアニメーションを実行
						final TransitionDrawable td = new TransitionDrawable(drawables);
						mMainPhotoImageView.setImageDrawable(td);
						td.startTransition(CROSS_FADE_ANIMATION_DURATION_MILLIS);
					} else if (drawables[1] != null) {
						// 次に表示する画像のみ取得済みの場合、ImageViewにそのまま設定
						mMainPhotoImageView.setImageDrawable(drawables[1]);
					}

					// 自動ページ送りを実行
					if (mVirtualPageCount <= mThumbnailDispCount) {
						// 循環ページングなし
						mThumbnailViewPager.setCurrentItem(nextFocusedPosition, false);
					} else {
						// 循環ページングあり
						final int virtualPosition = (mThumbnailViewPager.getCurrentItem() - mThumbnailDispCount + mRealPageCount) % mRealPageCount;
						if (mFocusedPosition == (virtualPosition + mThumbnailDispCount - 1) % mRealPageCount) {
							mThumbnailViewPager.setCurrentItem(mThumbnailViewPager.getCurrentItem() + 1, true);
						}
					}
					mFocusedPosition = nextFocusedPosition;
					refreshViewPager();
				}
				return false;
			}
		});

		/**
		 * コンストラクタ
		 *
		 * @param imageUrlList サムネイル画像用URLのリスト
		 * @param activity {@link Activity}
		 */
		public ThumbnailAdapter(final List<String> imageUrlList, final Activity activity) {
			super();

			if (imageUrlList == null || activity == null) {
				// コンストラクタの引数はすべて必須のため１つでもnullの引数があった場合は例外を発行
				throw new IllegalArgumentException("MainPhotoViewPager null in the argument");
			}

			mImageUrlList = imageUrlList;
			mRealPageCount = mImageUrlList.size();
			if (mRealPageCount > mThumbnailDispCount) {
				// 表示ページ数より多い場合
				mVirtualPageCount = mRealPageCount + mThumbnailDispCount * 2;
			} else {
				// 表示ページ数以下の場合
				mVirtualPageCount = mThumbnailDispCount;
			}
			mDrawerbleMap = new HashMap<Integer, Drawable>();
			mImageGetExecutor = Executors.newFixedThreadPool(mRealPageCount);
			mActivityHolder = new WeakReference<Activity>(activity);

		}

		@Override
		public Object instantiateItem(final ViewGroup container, final int position) {
			FrameLayout rootView;
			rootView = (FrameLayout) container.findViewWithTag(position);
			if (rootView == null) {
				rootView = new FrameLayout(container.getContext());
				rootView.setTag(position);
				// サムネイル画像間(左右)のパディングを設定
				rootView.setPadding(mThumbnailMargin, 0, mThumbnailMargin, 0);

				final int virtualPosition;
				if (mVirtualPageCount <= mThumbnailDispCount) {
					// ページングなし
					virtualPosition = position;
				} else {
					virtualPosition = (position - mThumbnailDispCount + mRealPageCount) % mRealPageCount;
				}


				if (virtualPosition < mImageUrlList.size()) {
					final TextView tv = new TextView(rootView.getContext());
					tv.setTag("TextView");
					tv.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
					tv.setGravity(Gravity.CENTER);
//					tv.setTextSize(30);
//					tv.setText(virtualPosition + "");

					final ImageView iv = new ImageView(rootView.getContext());
					iv.setTag("ImageView");
					iv.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
					iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
					rootView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(final View v) {
							if (mDrawerbleMap.containsKey(virtualPosition) && mFocusedPosition != virtualPosition) {
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
							}
							mFocusedPosition = virtualPosition;
							refreshViewPager();
						}
					});

					if (!mDrawerbleMap.containsKey(virtualPosition)) {
						// 画像が未取得の場合、画像取得用のExecutorServiceを実行
						mImageGetExecutor.execute(
								new ImageGetHandler(
										virtualPosition,
										mImageUrlList.get(virtualPosition),
										new ImageGetHandler.OnGetDrawable() {
											@Override
											public void onGetDrawable(final int index, final Drawable drawable) {
												mDrawerbleMap.put(index, drawable);
												if (!mActivityHolder.get().isFinishing()) {
													mActivityHolder.get().runOnUiThread(new Runnable() {
														@Override
														public void run() {
															iv.setImageDrawable(mDrawerbleMap.get(virtualPosition));
															if (mMainPhotoImageView.getDrawable() == null && virtualPosition == 0) {
																mMainPhotoImageView.setImageDrawable(mDrawerbleMap.get(virtualPosition));
															}
														}
													});
												}
											}
										}));
					}
					rootView.addView(iv);
					rootView.addView(tv);
				}

				container.addView(rootView);
				Log.d("Log", "==============================");
				Log.d("Log", "instantiateItem(" + position + ")");
				Log.d("Log", "mThumbnailDispCount = " + mThumbnailDispCount);
				Log.d("Log", "mVirtualPageCount = " + mVirtualPageCount);
				Log.d("Log", "mRealPageCount = " + mRealPageCount);
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
		public float getPageWidth(int position) {
			return 1.0f / mThumbnailDispCount;
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
			return mVirtualPageCount;
		}

		@Override
		public boolean isViewFromObject(final View view, final Object object) {
			return view == object;
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
		 * 画像取得の停止
		 */
		void stopImageGetHandler() {
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
		 * フォーカスされているサムネイルのポジションを取得
		 */
		public int getFocusedPosition() {
			return mFocusedPosition;
		}

		/**
		 * サムネイルの自動ページ送り用ハンドラの開始
		 */
		public void setAutoPagingHandler() {
			if (mThumbnailAutoPagingOn && mRealPageCount > 1) {
				// 自動ページ送りが有効の場合
				removeAutoPagingHandler();
				/*
				 * Note) Callbackが設定されているとhandleMessage()に通知されない。
				 * 参照：Handler.dispatchMessage()
				 */
				// メッセージを取得
				mMessage = mAutoPagingHandler.obtainMessage(HANDLER_AUTO_PAGING, this);

				// mAutoPagingDurationミリ秒の遅延でメッセージを送信
				final boolean successed = mAutoPagingHandler.sendMessageDelayed(mMessage, mThumbnailAutoPagingDuration);

				// TODO:デバッグコード
				if (successed) {
					Log.d("Log", " - Success send message.");
				} else {
					Log.d("Log", " - Failed send message.");
				}
			}
		}

		/**
		 * サムネイルの自動ページ送り用ハンドラの停止（メッセージ削除）
		 */
		public void removeAutoPagingHandler() {
			if (mMessage != null && mAutoPagingHandler.hasMessages(HANDLER_AUTO_PAGING, this)) {
				// 既にメッセージが追加されている場合はメッセージを削除
				mAutoPagingHandler.removeMessages(HANDLER_AUTO_PAGING, this);
			}
		}

	}

	/**
	 * 画像取得ハンドラ
	 */
	private static class ImageGetHandler implements Runnable {
		/** 画像のインデックス */
		private final int mImageIndex;
		/** 画像のURL */
		private final String mImageUrl;
		/** 画像取得のコールバック */
		private final OnGetDrawable mCallbacks;

		/**
		 * コンストラクタ
		 * @param imageIndex 画像のインデックス
		 * @param imageUrl 画像のURL
		 * @param callbacks 画像取得のコールバック
		 */
		/* package private */ ImageGetHandler(final int imageIndex, final String imageUrl, final OnGetDrawable callbacks) {
			mImageIndex = imageIndex;
			mImageUrl  = imageUrl;
			mCallbacks = callbacks;
		}

		public void run() {
			try {
				// WEBから画像を取得
				final InputStream is = new URL(mImageUrl).openStream();
				mCallbacks.onGetDrawable(mImageIndex, Drawable.createFromStream(is, ""));
				is.close();
			} catch (final Exception ignored) {
				Log.w(ignored.getClass().getCanonicalName(), ignored.getMessage(), ignored);
			}
		}

		/** 画像取得のコールバック用インターフェース */
		interface OnGetDrawable {
			void onGetDrawable(final int index, final Drawable drawable);
		}
	}

	/**
	 * サムネイルの自動ページ送りのスクロール速度を変更するScroller
	 */
	private static class AutoPagingSpeedControlScroller extends Scroller {

		private final int mDuration;

		/* package private */ AutoPagingSpeedControlScroller(final Context context, final Interpolator interpolator, final int duration) {
			super(context, interpolator);
			mDuration = duration;
		}

		@Override
		public void startScroll(final int startX, final int startY, final int dx, final int dy, final int duration) {
			super.startScroll(startX, startY, dx, dy, mDuration);
		}

		@Override
		public void startScroll(final int startX, final int startY, final int dx, final int dy) {
			super.startScroll(startX, startY, dx, dy, mDuration);
		}
	}
}
