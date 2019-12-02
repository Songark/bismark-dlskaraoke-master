package com.tbtc.jftv.manage;

import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;

import com.tbtc.jftv.common.Global;

public class SetupManager {	
	public static final String[] SETUP_ITEMS = {
		"CUSTOMER LOCK", 
		"FANFARE", 
		"SCORE DISPLAY", 
		"AVERAGE SCORE", 
		"SHADOW", 
		"VOCAL", 
		"COIN/TIME",
		"BUSINESS ROOM",
		"RUBY CONFIG", 
		"DEFALUT SETUP", 
		"GREET FUNCTION", 
		"BLUETOOTH", 
		"B/F", 
		"POWER", 
		"LANGUAGE", 
		"MANAGEMENT TOOL", 
		"LOAD MTV/MP3/CDG", 
		"SINGER", 
		"CHORUS",
		"RECORD MP3/MP4"};
	
	private final static String[] ONOFF = {"ON", "OFF"};
	private final static String[] FANFARE = {"60-75-90", "70-80-90", "80-90", "85-90", "85-95", "OFF"};
	private final static String[] SCOREDISPLAY = {"80", "85", "90", "95", "OFF", "RANDOM", "70", "75"};
	private final static int[] AVERAGESCORE = {70, 75, 80, 85, 90, 95};
	private final static String[] VOCAL = {"MIDDLE", "HIGH MIDDLE", "HIGH", "OFF", "LOW", "LOW MIDDLE"};
	private final static String[] BF = {"BUSSINESS", "FAMILY"};
	private final static String[] RECORD = {"MP3", "MP4"};
	private final static String[] COINTIME = {
		"OFF",
		"1 COIN 1 CREDIT",
		"1 COIN 2 CREDIT",
		"1 COIN 3 CREDIT",
		"1 COIN 5 CREDIT",
		"1 COIN 10 CREDIT",
		"2 COIN 1 CREDIT",
		"3 COIN 1 CREDIT",
		"5 COIN 1 CREDIT",
		"1 COIN 5 MINUTE",
		"1 COIN 10 MINUTE",
		"1 COIN 30 MINUTE",
		"1 COIN 60 MINUTE"
	};
	
	private final static String PREF_KEY = "joyful_setup";
		
	private static SetupManager s_instance = null;
	public static SetupManager getManager() {
		if(s_instance == null) {
			s_instance = new SetupManager();
		}
		synchronized (s_instance) {
			return s_instance;
		}
	}
	
	private int customerLock;
	private int fanfare;
	private int scoreDisplay;
	private int averageScore;
	private int shadow;
	private int vocal;
	private int coinTime;
	private int businessRoom;
	private int rubyConfig;
	private String greetFunction;
	private int bluetooth;
	private int bf;
	private int power;
	private String language;
	private int singer;
	private int chorus;
	private int record;
	
	private Context mContext;
	
	public void setContext(Context context) {
		mContext = context;
	}
	public SetupManager() {	
	}
	
	public void init(Context context) {
		initValues();
		
		mContext = context;
	}
	private void initValues() {
		customerLock = 1; 
		fanfare = 0;
		scoreDisplay = 0; 
		averageScore = 0; 
		shadow = 1;
		vocal = 0;
		coinTime = 0;
		businessRoom = 0;
		rubyConfig = 0;
		greetFunction = "<간 주>";
		bluetooth = 1;
		bf = 0;           
		power = 0;
		
		language = "";
		for(int i = 0; i < Global.LANGS.length; i++) {
			language += i + ",";
		}
		
		singer = 1;
		chorus = 0;
		record = 0;
	}
	public int readBGVIndex() {
		SharedPreferences pref = mContext.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
		return pref.getInt("BGVINDEX", 0);
	}
	public void saveBGVIndex(int iIndex) {
		SharedPreferences pref = mContext.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putInt("BGVINDEX", iIndex);
		editor.commit();
	}
	public int readDBSize() {
		SharedPreferences pref = mContext.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
		return pref.getInt("DBSIZE", 0);
	}
	public void saveDBSize(int iSize) {
		SharedPreferences pref = mContext.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putInt("DBSIZE", iSize);
		editor.commit();
	}
	public int readUpdateVersion() {
		SharedPreferences pref = mContext.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
		return pref.getInt("UPDATEVERSION", 1);
	}
	public void saveUpdateVersion(int iVersion) {
		SharedPreferences pref = mContext.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putInt("UPDATEVERSION", iVersion);
		editor.commit();
	}
	public int readCoin() {
		SharedPreferences pref = mContext.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
		return pref.getInt("JFTVCOIN", 0);
	}
	public int saveCoin(int iValue) {
		SharedPreferences pref = mContext.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putInt("JFTVCOIN", iValue);
		editor.commit();
		return iValue;
	}
	public int readPlayTimes() {
		SharedPreferences pref = mContext.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
		return pref.getInt("JFTVPLAYTIME", 0);
	}
	public void savePlayTimes(int iValue) {
		SharedPreferences pref = mContext.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putInt("JFTVPLAYTIME", iValue);
		editor.commit();
	}
	public void readValues() {
		SharedPreferences pref = mContext.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
		customerLock 	= pref.getInt(SETUP_ITEMS[0], customerLock);
		fanfare 			= pref.getInt(SETUP_ITEMS[1], fanfare);
		scoreDisplay 		= pref.getInt(SETUP_ITEMS[2], scoreDisplay);
		averageScore 	= pref.getInt(SETUP_ITEMS[3], averageScore);
		shadow 			= pref.getInt(SETUP_ITEMS[4], shadow);
		vocal 				= pref.getInt(SETUP_ITEMS[5], vocal);
		coinTime 			= pref.getInt(SETUP_ITEMS[6], coinTime);
		businessRoom	= pref.getInt(SETUP_ITEMS[7], businessRoom);
		rubyConfig 		= pref.getInt(SETUP_ITEMS[8], rubyConfig);
		greetFunction 	= pref.getString(SETUP_ITEMS[10], greetFunction);
		bluetooth 		= pref.getInt(SETUP_ITEMS[11], bluetooth);
		bf 					= pref.getInt(SETUP_ITEMS[12], bf);
		power 			= pref.getInt(SETUP_ITEMS[13], power);

		language 		= pref.getString(SETUP_ITEMS[14], language);
		singer 			= pref.getInt(SETUP_ITEMS[17], singer);
		chorus 			= pref.getInt(SETUP_ITEMS[18], chorus);
		record 			= pref.getInt(SETUP_ITEMS[19], record);
	}
	public void saveValues() {
		SharedPreferences pref = mContext.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putInt(SETUP_ITEMS[0], customerLock);
		editor.putInt(SETUP_ITEMS[1], fanfare);
		editor.putInt(SETUP_ITEMS[2], scoreDisplay);
		editor.putInt(SETUP_ITEMS[3], averageScore);
		editor.putInt(SETUP_ITEMS[4], shadow);
		editor.putInt(SETUP_ITEMS[5], vocal);
		editor.putInt(SETUP_ITEMS[6], coinTime);
		editor.putInt(SETUP_ITEMS[7], businessRoom);
		editor.putInt(SETUP_ITEMS[8], rubyConfig);
		editor.putString(SETUP_ITEMS[10], greetFunction);
		editor.putInt(SETUP_ITEMS[11], bluetooth);
		editor.putInt(SETUP_ITEMS[12], bf);
		editor.putInt(SETUP_ITEMS[13], power);
		editor.putString(SETUP_ITEMS[14], language);
		editor.putInt(SETUP_ITEMS[17], singer);
		editor.putInt(SETUP_ITEMS[18], chorus);
		editor.putInt(SETUP_ITEMS[19], record);
		editor.commit();
	}
	
	public String getValues(int iIndex) {
		String strRet = "";
		
		switch(iIndex) {
		case 0:
			strRet = getCustomerLock();
			break;
		case 1:
			strRet = getFanfare();
			break;
		case 2:
			strRet = getScoreDisplay();
			break;
		case 3:
			strRet = "" + getAverageScore();
			break;
		case 4:
			strRet = getShadow();
			break;
		case 5:
			strRet = getVocal();
			break;
		case 6:
			strRet = getCoinTime();
			break;
		case 7:
			strRet = String.format(Locale.US, "%03d", getBusinessRoom());
			break;
		case 8:
			strRet = getRubyConfig();
			break;
		case 9:
			strRet = "ENTER";
			break;
		case 10:
			strRet = getGreetFunction();
			break;
		case 11:
			strRet = getBluetooth();
			break;
		case 12:
			strRet = getBf();
			break;
		case 13:
			strRet = getPower();
			break;
		case 14:
			strRet = getLanguage();
			break;
		case 15:
		case 16:
			strRet = "ENTER";
			break;
		case 17:
			strRet = getSinger();
			break;
		case 18:
			strRet = getChorus();
			break;
		case 19:
			strRet = getRecord();
			break;
		}
		return strRet;
	}

	public void setValues(int iIndex) {
		switch(iIndex) {
		case 0:
			nextCustomerLock();
			break;
		case 1:
			nextFanfare();
			break;
		case 2:
			nextScoreDisplay();
			break;
		case 3:
			nextAverageScore();
			break;
		case 4:
			nextShadow();
			break;
		case 5:
			nextVocal();
			break;
		case 6:
			nextCoinTime();
			saveCoin(0);
			savePlayTimes(0);
			break;
		case 7:
			break;
		case 8:
			nextRubyConfig();
			break;
		case 9:
			initValues();
			break;
		case 10:
			break;
		case 11:
			nextBluetooth();
			break;
		case 12:
			nextBf();
			break;
		case 13:
			nextPower();
			break;
		case 14:
			break;
		case 15:
			break;
		case 16:
			break;
		case 17:
			nextSinger();
			break;			
		case 18:
			nextChorus();
			break;
		case 19:
			nextRecord();
			break;
		}
		
		saveValues();
	}

	public String getCustomerLock() {
		return ONOFF[customerLock];
	}
	public void nextCustomerLock() {
		customerLock++;
		if(customerLock >= ONOFF.length)
			customerLock = 0;		
	}

	public String getFanfare() {
		return FANFARE[fanfare];
	}
	public void nextFanfare() {
		fanfare++;
		if(fanfare >= FANFARE.length)
			fanfare = 0;
	}

	public String getScoreDisplay() {
		return SCOREDISPLAY[scoreDisplay];
	}
	public void nextScoreDisplay() {
		scoreDisplay++;
		if(scoreDisplay >= SCOREDISPLAY.length)
			scoreDisplay = 0;
	}

	public int getAverageScore() {
		return AVERAGESCORE[averageScore];
	}
	public void nextAverageScore() {
		averageScore++;
		if(averageScore >= AVERAGESCORE.length)
			averageScore = 0;
	}

	public String getShadow() {
		return ONOFF[shadow];
	}
	public void nextShadow() {
		shadow++;
		if(shadow >= ONOFF.length)
			shadow = 0;		
	}

	public String getVocal() {
		return VOCAL[vocal];
	}
	public void nextVocal() {
		vocal++;
		if(vocal >= VOCAL.length)
			vocal = 0;
	}

	public int getTimePerCoin() {
		if(coinTime == 0)
			return -1;
		if(coinTime == 9) 
			return 5;
		else if(coinTime == 10)
			return 10;
		else if(coinTime == 11)
			return 30;
		else if(coinTime == 12)
			return 60;
		
		return 1;
	}
	public String getCoinTime() {
		return COINTIME[coinTime];
	}
	public void nextCoinTime() {
		coinTime = (coinTime + 1) % COINTIME.length;		
	}
	public void setCoinTime(int iIdx) {
		coinTime = iIdx;
	}

	public int getBusinessRoom() {
		return businessRoom;
	}
	public void setBusinessRoom(int iValue) {
		businessRoom = iValue;
	}
	public String getRubyConfig() {
		return ONOFF[rubyConfig];
	}
	public void nextRubyConfig() {
		rubyConfig++;
		if(rubyConfig >= ONOFF.length)
			rubyConfig = 0;		
	}

	public String getGreetFunction() {
		return greetFunction;
	}
	public void setGreetFunction(String strValue) {
		this.greetFunction = strValue;
	}
	
	

	public String getBluetooth() {
		return ONOFF[bluetooth];
	}
	public void nextBluetooth() {
		bluetooth++;
		if(bluetooth >= ONOFF.length)
			bluetooth = 0;		
	}

	public String getBf() {
		return BF[bf];
	}
	public void nextBf() {
		bf++;
		if(bf >= BF.length)
			bf = 0;		
	}

	public String getPower() {
		return ONOFF[power];
	}
	public void nextPower() {
		power++;
		if(power >= ONOFF.length)
			power = 0;		
	}

	public String getLanguage() {
		String[] arrIdx = language.split(",");
		String strRet = "";
		int iIdx;
		for(int i = 0; i < arrIdx.length; i++) {
			if(arrIdx[i].equals("")) continue;			
			iIdx = Integer.parseInt(arrIdx[i]);
			/*if(Global.INDONESIA) {
				boolean bExist = false;
				for(int j = 0; j < Global.INDONESIA_LANGS.length; j++) {
					if(Global.INDONESIA_LANGS[j] == iIdx) {
						bExist = true;
						break;								
					}						
				}
				if(!bExist) continue;
			}*/
			strRet += Global.LANGS[iIdx] + ",";
		}
		
		return strRet.equals("") ? "" : strRet.substring(0, strRet.length() - 1);
	}
	public void setLanguage(String strLang) {
		language = strLang;
	}

	public String getSinger() {
		return ONOFF[singer];
	}
	public void nextSinger() {
		singer = (singer + 1) % ONOFF.length;
	}

	public String getChorus() {
		return ONOFF[chorus];
	}
	public void nextChorus() {
		chorus = (chorus + 1) % ONOFF.length;
	}
	
	public String getRecord() {
		return RECORD[record];
	}
	public void nextRecord() {
		record = (record + 1) % RECORD.length;
	}

	private long playStartTime = 0;
	public void startPlay() {
		if(coinTime < 9) return;
		playStartTime = System.currentTimeMillis();
	}
	public void endPlay() {
		if(playStartTime == 0 || coinTime < 9) return;
		long playedTime = (System.currentTimeMillis() - playStartTime) / 1000;
		savePlayTimes(readPlayTimes() + (int)playedTime);
		checkCoin();
		playStartTime = 0;
	}
	public int insertCoin() {
		return saveCoin(readCoin() + 1);
	}
 	public boolean checkCoin() {
		if(coinTime == 0)
			return true;
		
		int iCoin = readCoin();

		if(iCoin < 1)
			return false;
		
		int iPlayTimes = readPlayTimes();
				
		switch(coinTime) {
		case 1: // 1coin 1 credit
			saveCoin(iCoin - 1);
			break;
		case 2: // 1coin 2 credit
			iPlayTimes++;
			if(iPlayTimes >= 2) {
				saveCoin(iCoin - 1);
				savePlayTimes(iPlayTimes - 2);
			} else {
				savePlayTimes(iPlayTimes);
			}
			break;
		case 3: // 1coin 3 credit
			iPlayTimes++;
			if(iPlayTimes >= 3) {
				saveCoin(iCoin - 1);
				savePlayTimes(iPlayTimes - 3);
			} else {
				savePlayTimes(iPlayTimes);
			}
			break;
		case 4: // 1coin 5 credit
			iPlayTimes++;
			if(iPlayTimes >= 5) {
				saveCoin(iCoin - 1);
				savePlayTimes(iPlayTimes - 5);
			} else {
				savePlayTimes(iPlayTimes);
			}
			break;
		case 5: // 1coin 10 credit
			iPlayTimes++;
			if(iPlayTimes >= 10) {
				saveCoin(iCoin - 1);
				savePlayTimes(iPlayTimes - 10);
			} else {
				savePlayTimes(iPlayTimes);
			}
			break;
		case 6: // 2coin 1 credit
			if(iCoin < 2) {
				return false;
			} else {
				saveCoin(iCoin - 2);
			}
			break;
		case 7: //3coin 1 credit
			if(iCoin < 3) {
				return false;
			} else {
				saveCoin(iCoin - 3);
			}
			break;
		case 8: // 5coin 1 credit
			if(iCoin < 5) {
				return false;
			} else {
				saveCoin(iCoin - 5);
			}			
			break;
		case 9: // 1coin 5 minute
			if(iPlayTimes >= 5 * 60) {
				saveCoin(iCoin - 1);
				savePlayTimes(iPlayTimes - 5 * 60);
			}
			break;
		case 10: // 1coin 10 minute
			if(iPlayTimes >= 10 * 60) {
				saveCoin(iCoin - 1);
				savePlayTimes(iPlayTimes - 10 * 60);
			}
			break;
		case 11: // 1coin 30 minute
			if(iPlayTimes >= 30 * 60) {
				saveCoin(iCoin - 1);
				savePlayTimes(iPlayTimes - 30 * 60);
			}
			break;
		case 12: // 1coin 60 minute
			if(iPlayTimes >= 60 * 60) {
				saveCoin(iCoin - 1);
				savePlayTimes(iPlayTimes - 60 * 60);
			}
			break;
		}
		return true;
	}
}
