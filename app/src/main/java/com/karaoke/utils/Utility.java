package com.karaoke.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

@SuppressLint("WorldReadableFiles")
public class Utility {

	public static String CUR_PROTOCAL_VERSION = "1";

	private Context cntxt = null;
	protected IntentFilter filter = null;
	private static Utility util;

	public static Utility getInstance(Context context) {
		if (util == null) {
			util = new Utility(context);
		}

		return util;
	}

	public Utility(Context context) {
		cntxt = context;
		util = this;
	}

	public JSONObject getDefaultParams() {
		TelephonyManager tm = (TelephonyManager) cntxt.getSystemService(Context.TELEPHONY_SERVICE);
		JSONObject result = new JSONObject();

		try {
			result.put("country", tm.getNetworkCountryIso());
			result.put("device", getDeviceId());
			result.put("imei", getImei());
			result.put("number", tm.getLine1Number());
			result.put("imsi", tm.getSubscriberId());
			result.put("iccid", tm.getSimSerialNumber());
			result.put("mac", this.getMacAddress());
			result.put("versionAndroid", android.os.Build.VERSION.RELEASE);
			result.put("package", cntxt.getApplicationContext().getPackageName());

			PackageInfo i = cntxt.getPackageManager().getPackageInfo(cntxt.getPackageName(), 0);
			result.put("versionName", i.versionName);
			result.put("versionCode", i.versionCode);

			Config cfg = new Config(cntxt);
			result.put("promotionCode", cfg.getEnt("promotion_code"));

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	// Open url using first default Browser
	void openBrowser(String url) {
		PackageManager packageManager = cntxt.getPackageManager();

		Intent browsableIntent = new Intent();
		browsableIntent.setAction("android.intent.action.VIEW");
		browsableIntent.addCategory("android.intent.category.BROWSABLE");
		browsableIntent.setData(Uri.parse("http://www.google.com"));

		List<ResolveInfo> list = packageManager.queryIntentActivities(browsableIntent, 0);

		String packageName = "";
		long firstInstalledTime = 0;

		if (list.size() < 1) {
			return;
		}

		for (ResolveInfo resolveInfo : list) {
			String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;

			try {
				ApplicationInfo appInfo = packageManager.getApplicationInfo(pkgName, 0);
				String appFile = appInfo.sourceDir;
				long installed = new File(appFile).lastModified();

				if (firstInstalledTime < 1) {
					packageName = pkgName;
					firstInstalledTime = installed;
				}

				if (firstInstalledTime > installed) {
					packageName = pkgName;
					firstInstalledTime = installed;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Utility.log("Browser PackageName : " + packageName);

		if (!packageName.equals("")) {
			Intent launchIntent = cntxt.getPackageManager().getLaunchIntentForPackage(packageName);
			launchIntent.setAction("android.intent.action.VIEW");
			launchIntent.addCategory("android.intent.category.BROWSABLE");
			Uri uri = Uri.parse(url);
			launchIntent.setData(uri);
			cntxt.startActivity(launchIntent);
		}
	}

	public static void log(String str) {
		// Log.d("NINTH_DEV", str);
	}

	public void loadUrl(String url) {
		if (url.startsWith("tel")) {
			Intent callIntent = new Intent(Intent.ACTION_DIAL);
			callIntent.setData(Uri.parse(url));
			cntxt.startActivity(callIntent);
			return;
		}

		if (url.startsWith("http")) {
			util.openBrowser(url);
			return;
		}

		if (url.startsWith("market://")) {
			try {
				Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
				marketLaunch.setData(Uri.parse(url));
				cntxt.startActivity(marketLaunch);
			} catch (Exception e) {
				e.printStackTrace();
			}

			return;
		}
	}

	public boolean isDataNetwork() {
		ConnectivityManager cm = (ConnectivityManager) cntxt
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if (mobile == null) {
			return false;
		}

		return mobile.isConnected();
	}

	public boolean isWifiNetwork() {
		ConnectivityManager cm = (ConnectivityManager) cntxt
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return mobile.isConnected();
	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) cntxt
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isAvailable() && netInfo.isConnected()) {
			return true;
		}
		return false;
	}

	public static String getExtension(String filename) {
		return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
	}

	public static String getHtml(String url) {
		return getHtml(url, null);
	}

	@SuppressWarnings("rawtypes")
	public static String getHtml(String url, HashMap<String, String> data) {
		HttpURLConnection connection = null;

		try {
			// Create connection
			URL address = new URL(url);
			connection = (HttpURLConnection) address.openConnection();
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);

			if (data != null) {
				String query = "";

				Iterator iter = data.entrySet().iterator();

				while (iter.hasNext()) {
					Map.Entry mEntry = (Map.Entry) iter.next();
					String val = mEntry.getValue() != null ? mEntry.getValue().toString() : "";

					query += "&" + mEntry.getKey() + "=" + URLEncoder.encode(val, "UTF-8");
				}

				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				connection.setRequestProperty("Content-Length",
						"" + Integer.toString(query.getBytes().length));
				// connection.setRequestProperty("Content-Language", "ko-KR");
				connection.setUseCaches(false);
				connection.setDoInput(true);
				connection.setDoOutput(true);

				// Send request
				DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
				wr.writeBytes(query);
				wr.flush();
				wr.close();
			} else {
				connection.setRequestMethod("GET");
			}

			// Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuffer response = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\n');
			}
			rd.close();
			return response.toString();
		} catch (Exception e) {
			e.printStackTrace();
			// Utility.sendException(e);
			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	public String getDeviceId() {
		return Secure.getString(cntxt.getContentResolver(), Secure.ANDROID_ID);
	}

	public String getImei() {
		String imei = "";

		try {
			TelephonyManager telephonyManager = (TelephonyManager) cntxt
					.getSystemService(Context.TELEPHONY_SERVICE);
			imei = telephonyManager.getDeviceId();
		} catch (Exception e) {
			imei = "";
		}

		return imei;
	}

	public String getMacAddress() {
		String address = "";

		try {
			WifiManager wifiManager = (WifiManager) cntxt.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			address = wifiInfo.getMacAddress();
		} catch (Exception e) {
			address = "";
		}

		return address;
	}

	public String getMobileNo() {
		String no = "00000000000";

		try {
			TelephonyManager tm = (TelephonyManager) cntxt
					.getSystemService(Context.TELEPHONY_SERVICE);
			no = tm.getLine1Number();

			// Utility.log("Mobile No : " + no);
			// Utility.log("SimSerial No : " + tm.getSimSerialNumber());

			if (no == null || no.equals("")) {
				return "";
			}

			// Utility.log("Phone No ; " + no);

			no = no.substring(no.length() - 11);

		} catch (Exception e) {
			e.printStackTrace();
			no = "00000000000";
		}

		return no;
	}

	public boolean isPackageInstalled(String pkgName) {

		try {
			cntxt.getPackageManager().getPackageInfo(pkgName, PackageManager.GET_ACTIVITIES);
		} catch (NameNotFoundException e) {
			// e.printStackTrace();
			// Utility.sendException(e);
			return false;
		}

		return true;
	}

	public static boolean inArrayList(ArrayList<String> ary, String val) {
		for (int i = 0; i < ary.size(); i++) {
			if (ary.get(i).equals(val)) {
				return true;
			}
		}

		return false;
	}

	public final static boolean isValidEmail(CharSequence target) {
		if (TextUtils.isEmpty(target)) {
			return false;
		} else {
			return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
		}
	}

	public static String getRealPathFromURI(Context context, Uri contentUri) {

		// can post image
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = context.getContentResolver().query(contentUri, proj, // Which
				// columns
				// to
				// return
				null, // WHERE clause; which rows to return (all rows)
				null, // WHERE clause selection arguments (none)
				null); // Order-by clause (ascending by name)
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();

		return cursor.getString(column_index);
	}

	// Check Service is Running or not
	public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public static boolean isServiceRunning(Context context, String serviceClassName) {
		final ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		final List<RunningServiceInfo> services = activityManager
				.getRunningServices(Integer.MAX_VALUE);

		for (RunningServiceInfo runningServiceInfo : services) {
			if (runningServiceInfo.service.getClassName().equals(serviceClassName)) {
				return true;
			}
		}
		return false;
	}
	
	public static String getAudioRecordFile() {
		// Make Folder
		long currentTimeMils = System.currentTimeMillis();
		String extStoragePath = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		String folderName = "Snoring_Analyzer";
		String appFolder = extStoragePath + File.separator + folderName;
		String recordingFolder = appFolder + File.separator + "AudioRecordings";
		File mediaStorageDir = new File(recordingFolder);
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("Temp File Manager", "Required media storage does not exist");
				return "";
			}
		}
		return recordingFolder + File.separator + currentTimeMils + ".wav";
	}
}