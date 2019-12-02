package com.karaoke.activity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.karaoke.R;
import com.karaoke.data.SongInfo;
import com.sheetmusic.MidiPlayer;

public class MainActivity extends BaseActivity implements OnMenuItemClickListener {

	// private AlertDialog.Builder alert;
	private LocalActivityManager mLocalActivityManager;

	private TabHost tabHost;

	static MainActivity me;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		me = this;
		mainActivity = this;

		// Set Top Title and Actions
		TextView topTitleTv = (TextView) findViewById(R.id.topTitleTv);
		topTitleTv.setTypeface(getQuicksandBoldFont());

		// Menu
		findViewById(R.id.btnMenu).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Setting();
				PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
				popupMenu.setOnMenuItemClickListener(MainActivity.this);
				popupMenu.inflate(R.menu.home_menu);
				Object menuHelper;
				Class[] argTypes;
				try {
					Field fMenuHelper = PopupMenu.class.getDeclaredField("mPopup");
					fMenuHelper.setAccessible(true);
					menuHelper = fMenuHelper.get(popupMenu);
					argTypes = new Class[] { boolean.class };
					menuHelper.getClass().getDeclaredMethod("setForceShowIcon", argTypes)
							.invoke(menuHelper, true);
				} catch (Exception e) {
					popupMenu.show();
					return;
				}
				popupMenu.show();

			}
		});

		// Tab Settings
		tabHost = (TabHost) findViewById(R.id.tabHost);
		mLocalActivityManager = new LocalActivityManager(this, false);
		mLocalActivityManager.dispatchCreate(savedInstanceState);
		tabHost.setup(mLocalActivityManager);

		// Add Recommend Tab
		TabSpec tabRecommend = tabHost.newTabSpec(getString(R.string.tab_recommend));
		tabRecommend.setIndicator(createTabView(tabHost.getContext(), getString(R.string.tab_recommend),
				R.drawable.ic_tab_recommend));
		tabRecommend.setContent(new Intent(this, MidiRecommendActivity.class)
				.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
		tabHost.addTab(tabRecommend);

		// Add New Tab
		TabSpec tabNew = tabHost.newTabSpec(getString(R.string.tab_new));
		tabNew.setIndicator(createTabView(tabHost.getContext(), getString(R.string.tab_new),
				R.drawable.ic_tab_new));
		tabNew.setContent(new Intent(this, MidiNewActivity.class)
				.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
		tabHost.addTab(tabNew);

		// Add Search Tab
		TabSpec tabSearch = tabHost.newTabSpec(getString(R.string.tab_search));
		tabSearch.setIndicator(createTabView(tabHost.getContext(), getString(R.string.tab_search),
				R.drawable.ic_tab_search));
		tabSearch.setContent(new Intent(this, MidiSearchActivity.class));
		tabHost.addTab(tabSearch);


		// Add Favourites Tab
		TabSpec tabFavor = tabHost.newTabSpec(getString(R.string.tab_favorites));
		tabFavor.setIndicator(createTabView(tabHost.getContext(), getString(R.string.tab_favorites),
				R.drawable.ic_tab_favorite));
		tabFavor.setContent(new Intent(this, MidiFavoriteActivity.class));
		tabHost.addTab(tabFavor);

		// Add Reserve Tab
		TabSpec tabReserve = tabHost.newTabSpec(getString(R.string.tab_reserve));
		tabReserve.setIndicator(createTabView(tabHost.getContext(), getString(R.string.tab_reserve),
				R.drawable.ic_tab_reserve));
		tabReserve.setContent(new Intent(this, MidiReserveActivity.class));
		tabHost.addTab(tabReserve);

		TabWidget tabWidget = tabHost.getTabWidget();
		for (int i = 0; i < tabWidget.getTabCount(); i++) {
			tabWidget.getChildAt(i).setTag(i);
			tabWidget.getChildAt(i).setOnTouchListener(new View.OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					int action = event.getAction();

					int nextTabId = (int) v.getTag();
					if (action == MotionEvent.ACTION_UP) {
						if (nextTabId != 0 ) {

							//return true; // Prevents from clicking
						}
					}
					return false;
				}
			});
		}

		tabHost.setCurrentTab(0);
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {

			@Override
			public void onTabChanged(String tabId) {
				initTabHost();
			}
		});

		initTabHost();

		// Media Play Action
		findViewById(R.id.ivPlay).setOnClickListener(new View.OnClickListener()  {
			@Override
			public void onClick(View v) {
				playMidi();
			}
		});
	}

	private void playMidi() {
		ArrayList<SongInfo> selectedSongArray = MidiPlayer.getPlayer().getSelectedSongs();
		if (MidiPlayer.getPlayer().getSelectedSongs() == null || selectedSongArray.size() <= 0) {
			Toast.makeText(me, R.string.select_midi, Toast.LENGTH_SHORT).show();
		} else {
			Intent newIntent = new Intent(MainActivity.this, MidiPlayerActivity.class);
			startActivity(newIntent);
		}
	}

	@SuppressLint("InflateParams")
	private View createTabView(Context context, String tag, int icon) {
		View view = LayoutInflater.from(context).inflate(R.layout.tab_item, null);
		TextView tabTitle = (TextView) view.findViewById(R.id.tabTitle);
		tabTitle.setText(tag);
		tabTitle.setTypeface(getQuicksandRegularFont());
		return view;
	}

	private void initTabHost() {
		int[] aryIconOff = new int[] { R.drawable.ic_tab_recommend, R.drawable.ic_tab_new,
				R.drawable.ic_tab_search, R.drawable.ic_tab_favorite, R.drawable.ic_tab_reserve};
		int[] aryIconOn = new int[] { R.drawable.ic_tab_recommend_sel, R.drawable.ic_tab_new_sel,
				R.drawable.ic_tab_search_sel, R.drawable.ic_tab_favorite_sel, R.drawable.ic_tab_reserve_sel };

		for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
			tabHost.getTabWidget().getChildAt(i).getLayoutParams().height = (int) (60 * this
					.getResources().getDisplayMetrics().density);
			ImageView tabIcon = (ImageView) tabHost.getTabWidget().getChildAt(i)
					.findViewById(R.id.tabIcon);
			tabIcon.setImageResource(aryIconOff[i]);

			tabHost.getTabWidget().getChildAt(i).setBackgroundColor(Color.parseColor("#515151"));

			TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i)
					.findViewById(R.id.tabTitle); // Unselected Tabs
			tv.setTextColor(Color.parseColor("#ffffff"));
			// tv.setTypeface(tv.getTypeface(), Typeface.NORMAL);
		}

		int currentIdx = tabHost.getCurrentTab();

		ImageView tabIcon = (ImageView) tabHost.getCurrentTabView().findViewById(R.id.tabIcon);
		tabIcon.setImageResource(aryIconOn[currentIdx]);

		// tabHost.getTabWidget().getChildAt(currentIdx).setBackgroundColor(Color.parseColor("#FFEAAA"));
		TextView tv = (TextView) tabHost.getCurrentTabView().findViewById(R.id.tabTitle);
		tv.setTextColor(Color.parseColor("#00ccff"));
		// tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
	}

	@Override
	protected void onStart() {
		super.onStart();

		// Plesse add Admob, Flurry, RateThisApp Actions
	}

	@Override
	protected void onResume() {
		super.onResume();
		mLocalActivityManager.dispatchResume();

		// Call startAppAd.OnResume in the future
	}

	@Override
	protected void onPause() {
		super.onPause();
		mLocalActivityManager.dispatchPause(isFinishing());

		// pause startAppAd
	}

	@Override
	protected void onStop() {
		super.onStop();
		// Flurry Agent onEndSession(context) here
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
			if (!isFinish) {
				Toast.makeText(cntxt, getString(R.string.finish_message), Toast.LENGTH_SHORT)
						.show();
				FinishTimer timer = new FinishTimer(2000, 1);
				timer.start();
			} else {
				finish();
				return true;
			}
		}
		return false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		me = null;

		updateBottomBannerAds();
	}

	boolean isFinish = false;

	class FinishTimer extends CountDownTimer {
		public FinishTimer(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			isFinish = true;
		}

		@Override
		public void onTick(long millisUntilFinished) {
		}

		@Override
		public void onFinish() {
			isFinish = false;
		}
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_setting:
			// Please add later in your mind:)

			/*Intent settingsIntent = new Intent(cntxt, SettingsActivity.class);
			startActivity(settingsIntent);*/
			return true;
		case R.id.item_introduction:
			// Please add later in your mind:)

			/*Intent introductionIntent = new Intent(cntxt, GuideActivity.class);
			introductionIntent.putExtra("only_guide", true);
			startActivity(introductionIntent);*/
			return true;
		case R.id.item_share:
			try {
				// Uri imageUri = Uri.fromFile(imageFile);
				Intent shareIntent = new Intent();
				shareIntent.setAction(Intent.ACTION_SEND);
				shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
				shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_body));
				// shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
				shareIntent.setType("text/plain");
				shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				String chooserTitle = getResources().getString(R.string.menu_share);
				startActivity(Intent.createChooser(shareIntent, chooserTitle));
			} catch (Exception e) {

			}
			return true;
		}
		return true;
	}

}
