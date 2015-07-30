package com.example.satoutakayuki.androidtraining;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

//        final int pgaeNum = 12;

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

		mPager.setAdapter(adapter);
		// pager.setAdapter(new MyPagerAdapter());

//        ActionBar actionBar = getActionBar();
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//        for (int i = 0; i < pgaeNum; i++) {
//            actionBar.addTab(actionBar.newTab().setText(i + "").setTabListener(this));
//        }

		findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
//                adapter.addPage();
//                adapter.notifyDataSetChanged();
				mPager.setCurrentItem((mPager.getCurrentItem() - 1 + imageUrls.size()) % imageUrls.size(), true);
			}
		});

		findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
//                adapter.removePage();
//                adapter.notifyDataSetChanged();
				mPager.setCurrentItem((mPager.getCurrentItem() + 1) % imageUrls.size(), true);
			}
		});
	}

	class MyPagerAdapter extends PagerAdapter {

		public MyPagerAdapter() {
			super();
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			TextView tv = new TextView(container.getContext());
			tv.setTextSize(100);
			tv.setText(position + "");
			container.addView(tv);
			return tv;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			View v = (View) object;
			container.removeView(v);
		}

		@Override
		public int getCount() {
			return 5;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == ((View) object);
		}
	}

	class MainPhotoViewPager extends android.support.v4.view.PagerAdapter {

		// 同時に表示するページ数鵜
		private static final int DISP_PAGE_COUNT = 3;
		private int mVirtualCount = 0;
		private int mRealCount = 0;

		public MainPhotoViewPager(List<String> imageUrls) {
			super();
			if (imageUrls == null) {
				// コンストラクタの引数はすべて必須のため１つでもnullの引数があった場合は例外を発行
				throw new IllegalArgumentException("MainPhotoViewPager null in the argument");
			}
			if (imageUrls.size() > DISP_PAGE_COUNT) {
				// 表示ページ数より多い場合
				mVirtualCount = imageUrls.size() + DISP_PAGE_COUNT * 2;
			} else {
				// 表示ページ数以下の場合
				mVirtualCount = imageUrls.size();
			}
			mRealCount = imageUrls.size();
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
//            return super.instantiateItem(container, position);
			return SimpleFragment.newInstance(position);
		}

//		@Override
//		public void destroyItem(ViewGroup container, int position, Object object) {
//			View v = (View) object;
//			container.removeView(v);
//		}

		@Override
		public int getCount() {
			return mVirtualCount;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object; // false
		}

	}

	public static class SimpleFragment extends Fragment {

		public static SimpleFragment newInstance(int position) {
			Bundle args = new Bundle();
			args.putInt("position", position);
			SimpleFragment sf = new SimpleFragment();
			sf.setArguments(args);
			return sf;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

			int position = getArguments().getInt("position");

			TextView tv = new TextView(inflater.getContext());
			tv.setTextSize(100);
			tv.setText(position + "");
			return tv;
		}
	}

}
