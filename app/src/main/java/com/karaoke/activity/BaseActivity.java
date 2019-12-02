package com.karaoke.activity;

import com.karaoke.MyApplication;
import com.karaoke.R;
import com.karaoke.utils.Config;
import com.karaoke.utils.Utility;
import com.tbtc.jftv.common.Global;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class BaseActivity extends FragmentActivity {

	protected MyApplication myApp;
	protected MainActivity mainActivity;
	protected Utility util;
	protected Context cntxt;
	protected Config cfg;
	protected ProgressDialog progress;
	protected ProgressDialog pdBuff;

	protected boolean hideProgress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (Build.VERSION_CODES.FROYO < Build.VERSION.SDK_INT) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll()
					.build();
			StrictMode.setThreadPolicy(policy);
		}

		super.onCreate(savedInstanceState);

		myApp = (MyApplication) getApplication();
		util = Utility.getInstance(this);
		cntxt = this;
		cfg = new Config(this);

		// Progress Dialog
		progress = new ProgressDialog(cntxt);
		progress.setMessage(getString(R.string.loading));
		progress.setCancelable(false);
		progress.setCanceledOnTouchOutside(false);
		progress.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				Utility.log("RequestThread.cancel()");
			}
		});

		// Buffering Dialog
		pdBuff = new ProgressDialog(cntxt, AlertDialog.THEME_HOLO_LIGHT);
		pdBuff.setMessage("Buffering Content");
		pdBuff.setCancelable(true);
		pdBuff.setCanceledOnTouchOutside(false);
		pdBuff.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
			}
		});

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	// Remove EditText Keyboard
	public void hideKeyboard(EditText et) {
		if (et != null) {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public Config getConfig() {
		return cfg;
	}

	public ProgressDialog getProgressDialog() {
		return progress;
	}

	public void showProgressDialog() {
//		try {
//			if (progress.isShowing() == false) {
//				progress.show();
//				progress.setContentView(R.layout.dialog_loading);
//			}
//		}
//		catch (Exception ex)
//		{
//			Global.Debug("[BaseActivity::showProgressDialog] error");
//		}
	}

	public void showSavingDialog() {
		if (progress.isShowing())
			return;

		progress.show();
		progress.setContentView(R.layout.dialog_saving);
	}

	public void hideProgressDialog() {
//		try {
//			if (progress.isShowing())
//				progress.dismiss();
//		}
//		catch (Exception ex)
//		{
//			Global.Debug("[BaseActivity::hideProgressDialog] error");
//		}
	}

	public void showBufferingDialog() {
		if (pdBuff.isShowing())
			return;
		pdBuff.show();
	}

	public void hideBufferingDialog() {
		if (pdBuff.isShowing())
			pdBuff.dismiss();
	}

	public void msg(int resId) {
		String msg = getString(resId);
		AlertDialog.Builder alert = new AlertDialog.Builder(cntxt);
		alert.setMessage(msg);
		alert.setPositiveButton(getString(R.string.alert_close),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		alert.show();
	}

	public void msg(String msg) {
		AlertDialog.Builder alert = new AlertDialog.Builder(cntxt);
		alert.setMessage(msg);
		alert.setPositiveButton(getString(R.string.alert_close),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		AlertDialog alertDialog = alert.show();

		// Apply custom font
		TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
		Button button = (Button) alertDialog.findViewById(android.R.id.button1);
		Typeface msgFont = getQuicksandRegularFont();
		if (textView != null) {
			textView.setTypeface(msgFont);
		}

		if (button != null) {
			button.setTypeface(msgFont);
		}
	}

	public void closeMsg(int msgId) {
		closeMsg(getString(msgId));
	}

	public void closeMsg(String msg) {
		AlertDialog.Builder alert = new AlertDialog.Builder(cntxt);
		alert.setMessage(msg);
		alert.setPositiveButton(getString(R.string.alert_close),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});
		AlertDialog alertDialog = alert.show();

		// Apply custom font
		TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
		Button button = (Button) alertDialog.findViewById(android.R.id.button1);
		Typeface msgFont = getQuicksandRegularFont();
		if (textView != null) {
			textView.setTypeface(msgFont);
		}

		if (button != null) {
			button.setTypeface(msgFont);
		}
	}

	public void showMessage(String msg) {
		if (TextUtils.isEmpty(msg))
			return;
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}

	public void showMessage(int resId) {
		String msg = getString(resId);
		if (TextUtils.isEmpty(msg))
			return;
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}

	protected void openLink(String link) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
		startActivity(browserIntent);
	}

	// Get some font about ViewGroup
	public void setGlobalFont(View view, Typeface face) {

		if (view != null) {
			if (view instanceof ViewGroup) {
				ViewGroup vg = (ViewGroup) view;
				int vgCnt = vg.getChildCount();
				for (int i = 0; i < vgCnt; i++) {
					View v = vg.getChildAt(i);
					if (v instanceof TextView) {
						TextView txt = (TextView) v;
						txt.setTypeface(face);
						// txt.setText(txt.getText(),
						// TextView.BufferType.SPANNABLE);
					} else if (v instanceof EditText) {
						((EditText) v).setTypeface(face);
					} else if (v instanceof Button) {
						((Button) v).setTypeface(face);
					} else if (v instanceof RadioButton) {
						((RadioButton) v).setTypeface(face);
					} else if (v instanceof CheckBox) {
						((CheckBox) v).setTypeface(face);
					} else {
						setGlobalFont(v, face);
					}
				}
			}
		}
	}

	// ---------------------- Application Fonts --------------------------
	// Ltcn Font
	public Typeface getQuicksandRegularFont() {
		return myApp.getQuicksandRegularFont();
	}

	// LtcnO Font
	public Typeface getQuicksandLightFont() {
		return myApp.getQuicksandLightFont();
	}

	// Mdcn Font
	public Typeface getQuicksandBoldFont() {
		return myApp.getQuicksandBoldFont();
	}

	// Lato Font
	public Typeface getLatoRegularFont() {
		return myApp.getLatoRegularFont();
	}

	// Mdcn Font
	public Typeface getSimheiFont() {
		return myApp.getSimheiFont();
	}

	protected void onBannerClicked() {
		
	}
	
	protected void updateBottomBannerAds() {
		int bannerId = cfg.getIntEnt("banner_index", 0) % 3;
		cfg.set("banner_index", (bannerId + 1) % 3);
	}

	protected static final int MY_PERMISSIONS_REQUEST = 100;

	// This will be used in Android6.0(Marshmallow) or above
	protected boolean checkPermissions(String[] permissions, boolean showHintMessage, int requestCode) {

		if (permissions == null || permissions.length == 0)
			return true;

		boolean allPermissionSetted = true;
		for (String permission : permissions) {
			if (ContextCompat.checkSelfPermission(cntxt, permission) != PackageManager.PERMISSION_GRANTED) {
				allPermissionSetted = false;
				break;
			}
		}

		if (allPermissionSetted)
			return true;

		// Should we show an explanation?
		boolean shouldShowRequestPermissionRationale = false;
		for (String permission : permissions) {
			if (ActivityCompat.shouldShowRequestPermissionRationale(BaseActivity.this, permission)) {
				shouldShowRequestPermissionRationale = true;
				break;
			}
		}

		if (showHintMessage && shouldShowRequestPermissionRationale) {
			// Show an expanation to the user *asynchronously* -- don't
			// block
			// this thread waiting for the user's response! After the
			// user
			// sees the explanation, try again to request the
			// permission.
			showMessage(R.string.request_permission_hint);
		}

		ActivityCompat.requestPermissions((Activity) cntxt, permissions, requestCode);

		return false;
	}

	public boolean checkPermission() {
		int result = ContextCompat.checkSelfPermission(cntxt, Manifest.permission.WRITE_EXTERNAL_STORAGE);
		if (result == PackageManager.PERMISSION_GRANTED) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}
}
