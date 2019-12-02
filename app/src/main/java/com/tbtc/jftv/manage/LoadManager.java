package com.tbtc.jftv.manage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.karaoke.R;

import com.karaoke.data.SongInfo;
import com.tbtc.jftv.common.Global;
import com.sheetmusic.MidiPlayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import jp.bismark.bssynth.sample.MainActivity;

@SuppressLint("HandlerLeak")
public class LoadManager {
	private final static int MSG_COMPLETE = 0;
	private final static int MSG_CHK_RAM = 1;
	private final static int MSG_CHK_INTERNET = 2;
	private final static int MSG_CHK_APPVERSION = 3;
	private final static int MSG_INIT_SETUP = 4;
	private final static int MSG_CHK_FOLDER = 5;
	private final static int MSG_SET_FONT = 6;
	private final static int MSG_LOAD_FANFARE = 7;
	private final static int MSG_INIT_INFO = 8;
	private final static int MSG_INIT_BGV = 9;
	private final static int MSG_INIT_PLAYER = 10;
	private final static int MSG_CHK_BETA = 14;
	private final static int MSG_UPDATE = 15;
	private final static int MSG_CHK_STORAGE = 16;

	public final static int MSG_PROGRESS = 11;
	public final static int MSG_PROGRESS_SHOW = 12;
	public final static int MSG_PROGRESS_HIDE = 13;

	private final static int ERROR = 25;

	private Context mContext;
	private TextView mTxtView;
	private ProgressBar mProgBar;
	private LoadHandler mHandler;
	private LoadThread mThread;
	private Handler mParentHandler;
	private View mRlMain;

	public LoadManager(Context context, View rlMain, TextView txtView, ProgressBar prog, Handler handler) {
		mContext = context;
		mTxtView = txtView;
		mProgBar = prog;
		mParentHandler = handler;
		mRlMain = rlMain;

		mProgBar.setVisibility(View.INVISIBLE);

		mThread = new LoadThread();
		mHandler = new LoadHandler();
	}

	public void start() {
		mThread.start();
	}

	public void stop() {
		mThread.interrupt();
	}

	class LoadHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			int iErrMsg = R.string.err_unknown_error;
			switch (msg.what) {
			case MSG_CHK_STORAGE:
				mTxtView.setText(R.string.msg_chk_storage);
				iErrMsg = R.string.err_chk_storage;
				break;
			case MSG_CHK_RAM:
				mTxtView.setText(R.string.msg_chk_ram);
				iErrMsg = R.string.err_chk_ram;
				break;
			case MSG_CHK_INTERNET:
				mTxtView.setText(R.string.msg_chk_internet);
				iErrMsg = R.string.err_chk_internet;
				break;
			case MSG_CHK_APPVERSION:
				mTxtView.setText(R.string.msg_chk_appver);
				iErrMsg = R.string.err_chk_appver;
				break;
			case MSG_INIT_SETUP:
				mTxtView.setText(R.string.msg_init_setup);
				iErrMsg = R.string.err_init_setup;
				break;
			case MSG_CHK_FOLDER:
				mTxtView.setText(R.string.msg_chk_folder);
				iErrMsg = R.string.err_chk_folder;
				break;
			case MSG_SET_FONT:
				mTxtView.setText(R.string.msg_set_font);
				iErrMsg = R.string.err_set_font;
				break;
			case MSG_LOAD_FANFARE:
				mTxtView.setText(R.string.msg_load_fanfare);
				iErrMsg = R.string.err_load_fanfare;
				break;
			case MSG_INIT_INFO:
				mTxtView.setText(R.string.msg_init_info);
				iErrMsg = R.string.err_init_info;
				break;
			case MSG_INIT_BGV:
				mTxtView.setText(R.string.msg_init_bgv);
				iErrMsg = R.string.err_init_bgv;
				break;
			case MSG_INIT_PLAYER:
				mTxtView.setText(R.string.msg_init_player);
				iErrMsg = R.string.err_init_player;
				break;
			case MSG_CHK_BETA:
				iErrMsg = R.string.err_chk_beta;
				break;
			case MSG_PROGRESS_SHOW:
				mProgBar.setVisibility(View.VISIBLE);
				mProgBar.setMax(msg.arg1);
				break;
			case MSG_PROGRESS_HIDE:
				mProgBar.setVisibility(View.INVISIBLE);
				break;
			case MSG_PROGRESS:
				mProgBar.setProgress(msg.arg1);
				break;
			case MSG_COMPLETE:
				mTxtView.setText("");
				mParentHandler.sendEmptyMessage(0);
				break;
			case MSG_UPDATE:
				mParentHandler.sendEmptyMessage(1);
				break;
			}
			
			mRlMain.requestLayout();

			if (msg.arg2 == ERROR) {
				mParentHandler.sendMessage(getMessage(-1, iErrMsg, 0));
			}
		}

	}

	class LoadThread extends Thread {
		@Override
		public void run() {
			try {
				// RAM check
				mHandler.sendEmptyMessage(MSG_CHK_RAM);
				if (Global.getTotalRAM() < Global.MIN_RAMSIZE * 1024) {
					mHandler.sendMessage(getMessage(MSG_CHK_RAM, 0, ERROR));
					return;
				}

				mHandler.sendEmptyMessage(MSG_CHK_STORAGE);

				// get real root path
				Global.realRootPath = Global.getExternalStoragePath();

				// init setup
				mHandler.sendEmptyMessage(MSG_INIT_SETUP);

				// check folder
				mHandler.sendEmptyMessage(MSG_CHK_FOLDER);
				if (!checkDirectories()) {
					mHandler.sendMessage(getMessage(MSG_CHK_FOLDER, 0, ERROR));
					return;
				}

				// set font
				mHandler.sendEmptyMessage(MSG_SET_FONT);
				if (Global.mTypeface == null) {
					// Global.mTypeface =
					// Typeface.createFromAsset(mContext.getAssets(),
					// "KhmerOS.ttf");
					Global.mTypeface = Typeface.MONOSPACE;
				}

				// mHandler.sendEmptyMessage(MSG_LOAD_FANFARE);

				// copy load fanfare
				String strPath = Global.getFanfarePath();
				Global.copyFileFromAsset(mContext, "fanfare/f1.mp3", strPath + "1.mp3", false);
				Global.copyFileFromAsset(mContext, "fanfare/f2.mp3", strPath + "2.mp3", false);
				Global.copyFileFromAsset(mContext, "fanfare/f3.mp3", strPath + "3.mp3", false);
				Global.copyFileFromAsset(mContext, "fanfare/f4.mp3", strPath + "4.mp3", false);
				Global.copyFileFromAsset(mContext, "fanfare/f5.mp3", strPath + "5.mp3", false);
				MarkManager.getManager().loadFanfare();

				// mHandler.sendEmptyMessage(MSG_INIT_PLAYER);

				// copy midi & lyric files
				String strRootSongsPath = Global.getRootSongsPath();
				AssetManager assets = mContext.getResources().getAssets();
				try {
					File folder = new File( strRootSongsPath);
					boolean success = true;
					if (!folder.exists()) {
						success = folder.mkdirs();
					}
					if (success) {
						String[] files = assets.list("midi");
						for (String path: files) {
							if (path.endsWith(".mid") || path.endsWith(".lyr") || path.endsWith(".sok")) {
								Global.copyFileFromAsset(mContext, "midi/" + path, folder.getPath() + File.separator + path, false);
							}
						}
					}
				}
				catch (IOException e) {
				}

				
				// copy soundfont files
				String soundFontFile = "";
				for (int i = 0; i < Global.SOUND_FONTS.length; i++) {
					String assetsFilePath = "soundfont/" + Global.SOUND_FONTS[i];
					String destFilePath = Global.getRootPath() + Global.SOUND_FONTS[i];
					Global.copyFileFromAsset(mContext, assetsFilePath, destFilePath, false);

					if (new File(destFilePath).exists() && soundFontFile.isEmpty()) {
						soundFontFile = destFilePath;
					}
				}

				if (!MidiPlayer.getPlayer().initialize(mContext, null, mHandler)) {
					// mHandler.sendMessage(getMessage(MSG_INIT_PLAYER, 0, ERROR));
					return;
				}

				// load soundfont
				String sf2File = "";
				String sfdFile = "";
				if (soundFontFile.isEmpty() == false) {
					if (soundFontFile.endsWith(".dlgse") || soundFontFile.endsWith(".DLGSE")) {
						sf2File = soundFontFile;
					} else {
						sfdFile = soundFontFile;
						String sfPath = sfdFile.substring(0, sfdFile.length() - 3) + "dlgse";
						if (InfoManager.getManager().restoreSoundFont(sfdFile, sfPath)) {
							Global.Debug("Decrypt sound font error!");
							sf2File = sfPath;
						} else {
							sfdFile = "";
						}
					}
				}
				Global.Debug("Soundfont: " + sf2File);

				// copy background files
				File folder = new File( Global.getRootBackgroundsPath());
				boolean success = true;
				if (!folder.exists()) {
					success = folder.mkdirs();
				}
				if (success) {
					try {
						String[] files = assets.list("bg");
						for (String path: files) {
							if (path.endsWith(".jpg") || path.endsWith(".mp4")) {
								Global.copyFileFromAsset(mContext, "bg/" + path, folder.getPath() + File.separator + path, false);
							}
						}
					}
					catch (IOException e) {
					}
				}

				int nResult = MainActivity.getPlayer().Initialize(44100, 512, sf2File);
				if (nResult != 0) {
					// mHandler.sendMessage(getMessage(MSG_INIT_PLAYER, 0, ERROR));
					return;
				}

				if (!TextUtils.isEmpty(sfdFile)) {
					InfoManager.getManager().mergeSoundFont(sf2File, sfdFile);
				}

				mHandler.sendEmptyMessage(MSG_COMPLETE);
			} catch (Exception e) {
				Global.Debug("LoadManager::LoadThread----->error! detail=" + e.getMessage());
				mHandler.sendMessage(getMessage(-1, 0, ERROR));
			}
		}
	}

	private static boolean checkDirectories() {
		// check root path
		if (!createDirectory(Global.getRootPath()))
			return false;

		return true;
	}

	private static boolean createDirectory(String strPath) {
		File fileChk = new File(strPath);
		if (!fileChk.exists()) {
			if (!fileChk.mkdir())
				return false;
		}
		return true;
	}


	@SuppressWarnings("unused")
	private boolean isOnline() {
		try {
			ConnectivityManager conMan = (ConnectivityManager) mContext
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			State wifi = conMan.getNetworkInfo(1).getState();
			if (wifi == NetworkInfo.State.CONNECTED) {
				return true;
			}
			State mobile = conMan.getNetworkInfo(0).getState();
			if (mobile == NetworkInfo.State.CONNECTED) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private static Message getMessage(int what, int arg1, int arg2) {
		Message msg = new Message();
		msg.what = what;
		msg.arg1 = arg1;
		msg.arg2 = arg2;
		return msg;
	}
}
