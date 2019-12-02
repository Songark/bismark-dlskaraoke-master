package com.karaoke.utils;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

public class Config {

	private final Context cntxt;

	public Config(Context context) {
		cntxt = context;
	}

	public String getMemberKey() {
		return this.getEnt("memberKey");
	}

	public boolean setMemberKey(String key) {
		return this.set("memberKey", key);
	}

	public ArrayList<String> get(String key) {
		ArrayList<String> result = new ArrayList<String>();

		// SQLiteOpenHelper dbHelper = new
		DBAdapter dbAdapter = new DBAdapter(cntxt);
		dbAdapter.open();
		Cursor dt = dbAdapter.select(key);
		// Utility.log("Member getConfig : " + dt.getCount());
		if (dt.getCount() == 0) {
			dbAdapter.close();
			return result;
		}

		if (dt.moveToFirst()) {
			do {
				try {
					if (dt.getString(1) != null) {
						result.add(TripleDes.decrypt(dt.getString(1).toString()));
					}
				} catch (Exception e) {
					e.printStackTrace();
					result.add("");
				}

			} while (dt.moveToNext());
		}
		dbAdapter.close();

		return result;
	}

	public String getEnt(String key) {
		ArrayList<String> result = this.get(key);

		if (result.isEmpty()) {
			return "";
		}

		return result.get(0);
	}

	public int getIntEnt(String key, int initVal) {
		String strEnt = getEnt(key);
		int nVal = initVal;

		if (!TextUtils.isEmpty(strEnt)) {
			try {
				nVal = Integer.parseInt(strEnt);
			} catch (NumberFormatException e) {
			}
		}

		return nVal;
	}

	public boolean set(String key, String val) {
		DBAdapter dbAdapter = new DBAdapter(cntxt);
		dbAdapter.open();

		Cursor dt = dbAdapter.select(key);

		try {
			val = TripleDes.encrypt(val);
		} catch (Exception e) {
			dbAdapter.close();
			e.printStackTrace();
			return false;
		}

		// Utility.log("setConfig("+key+") : " + val);

		if ((dt.getCount() == 0) || !dt.moveToFirst()) {
			boolean result = dbAdapter.insert(key, val) > 0;
			dbAdapter.close();
			return result;
		} else {
			boolean result = dbAdapter.update(key, val) > 0;
			dbAdapter.close();
			return result;
		}
	}

	public boolean set(String key, int val) {
		return set(key, val + "");
	}

	public boolean set(String key, double val) {
		return set(key, val + "");
	}

	public boolean add(String key, String val) {
		DBAdapter dbAdapter = new DBAdapter(cntxt);
		dbAdapter.open();

		try {
			val = TripleDes.encrypt(val);
		} catch (Exception e) {
			dbAdapter.close();
			e.printStackTrace();
			return false;
		}

		boolean result = dbAdapter.insert(key, val) > 0;
		dbAdapter.close();
		return result;
	}

	public boolean del(String key) {
		DBAdapter dbAdapter = new DBAdapter(cntxt);
		dbAdapter.open();

		boolean result = dbAdapter.delete(key);
		dbAdapter.close();
		return result;
	}

}
