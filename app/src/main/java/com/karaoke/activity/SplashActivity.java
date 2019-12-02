package com.karaoke.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.karaoke.R;
import com.tbtc.jftv.common.Global;
import com.tbtc.jftv.manage.LoadManager;
import com.tbtc.jftv.manage.MarkManager;
import com.sheetmusic.MidiPlayer;
import com.tbtc.jftv.net.NetworkManager;

public class SplashActivity extends BaseActivity {

	View panelLoading;

	private final static long DELAY_TIME = 1000;

	private static String[] PERMISSIONS_STORAGE = {
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.MODIFY_AUDIO_SETTINGS
	};

	LoadManager mLoadManager;
	boolean bIsRunningApp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		bIsRunningApp = true;
		panelLoading = findViewById(R.id.panelLoading);

		// Set Font
		setGlobalFont(panelLoading, getQuicksandRegularFont());

		if (verityPermission()) {
			mChkServiceHandler.sendEmptyMessageDelayed(0, DELAY_TIME);
		}
	}

	private boolean verityPermission() {
		return checkPermissions(PERMISSIONS_STORAGE, true, MY_PERMISSIONS_REQUEST);
	}

	@Override
	protected void onStop() {
		super.onStop();

	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	private Handler mChkServiceHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (msg.what == NetworkManager.RESULT_OK) {

				MarkManager.getManager().init();
				MidiPlayer.getPlayer().init();

				mLoadManager = new LoadManager(cntxt, (LinearLayout) findViewById(R.id.panelLoading),
						(TextView) findViewById(R.id.txtStatus),
						(ProgressBar) findViewById(R.id.proDownload), mHandler);
				mLoadManager.start();
			}
			super.handleMessage(msg);
		}
	};

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				Global.Debug("IntroActivity::Handler -- Recv completed!");
				if (bIsRunningApp) {
					Intent intent = new Intent(SplashActivity.this, MainActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);

					finish();
				}
			} else if (msg.what == -1) {
				if (!bIsRunningApp)
					if (msg.arg1 == R.string.err_chk_ram)
						Toast.makeText(
								cntxt,
								String.format(cntxt.getResources().getString(R.string.err_chk_ram),
										Global.MIN_RAMSIZE), Toast.LENGTH_LONG).show();
					else
						Toast.makeText(cntxt, msg.arg1, Toast.LENGTH_LONG).show();
				finish();
			}
		}
	};

	@Override
	public void onBackPressed() {
		if (mLoadManager != null) {
			mLoadManager.stop();
		}

		bIsRunningApp = false;
		super.onBackPressed();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		// Check All Permission was granted
		boolean bAllGranted = true;
		for(int grant : grantResults) {
			if (grant != PackageManager.PERMISSION_GRANTED) {
				bAllGranted = false;
				break;
			}
		}

		if (bAllGranted) {
			// Do next actions
			mChkServiceHandler.sendEmptyMessageDelayed(0, 0);
		} else {
			// Show message and finish app
			closeMsg(R.string.request_permission);
		}
	}
}
