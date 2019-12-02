package com.tbtc.jftv.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.karaoke.EnvironmentApp;
import com.sheetmusic.LyricsInfo;
import com.sheetmusic.MidiEvent;
import com.sheetmusic.MidiFile;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class Global {
	private final static boolean DEBUG_FILE = false;

	private final static boolean D = true;
	private final static String TAG = "karaoke";

	public static long DEBUG_TIME = 0;

	public final static int MIN_RAMSIZE = 256; // Minimum RAM Size : MB

	// public final static String ROOTPATH = "/jftv/";
	public final static String ROOTPATH = "/globalkaraoke/";
	public final static String FANFAREPATH = ROOTPATH + "fanfare/";

	public final static int SEX_NONE = 0;
	public final static int SEX_MALE = 1;
	public final static int SEX_FEMALE = 2;

	public final static int STYPE_COMMON = 0;
	public final static int STYPE_MEDLEY = 1;
	public final static int STYPE_EVENT = 2;
	public final static int STYPE_NEWSONG = 3;
	public final static int STYPE_POPULAR = 4;
	public final static int STYPE_RECORD = 5;
	public final static int STYPE_FAVORITE = 6;

	public final static int TYPE_MIDI = 0;
	public final static int TYPE_MTV = 1;
	public final static int TYPE_CDG = 2;
	public final static int TYPE_MP3 = 3;

	public final static int RTYPE_MP3 = 0;
	public final static int RTYPE_MP4 = 1;

	public final static int MAXRECORDCOUNT = 50;

	public final static int THREAD_MINDELAY = 1;

	public final static String[] SOUND_FONTS = { "realdls.dlgse", "bssynth_nk.dlgse",};

	public final static String[] MEDLEY_TYPES = { "Blues", "Dance", "Techno", "Trot", "Disco" };

	public static Typeface mTypeface = null;

	public static final int[] COLORS = { 0xFF078DFF, 0xFF6CFCE1, 0xFF2361A2, 0xFFFF6B77, 0xFFFA66FA };

	// 곡이 있는 국가들만 담는 변수
	public static List<Integer> gValidLangs = null;
	public final static String[] LANGS = { "Arabic", "Bangladesh", "Cambodian", "Chinese", "English",
			"Espanol", "Indian", "Indonesian", "Italian", "Japanese", "Korean", "Malaysian",
			"Mongolian", "Myanmar", "Philippine", "Portugues", "Russian", "Sinhala", "Thai", "Turkish",
			"Vietnamese" };
	public final static int[] INDONESIA_LANGS = { 3, 4, 6, 7, 9, 10, 11 };
	public final static String[] LANGCODE = { "ARAARACC", "ENGBANCC", "KHMKHMCC", "CHIMANCC",
			"ENGUSACC", "SPAESPCC", "ENGIN2CC", "IDNIDNCC", "SPAITACC", "JPNJP2CC", "KORKORCC",
			"ENGMALCC", "RUSMNGCC", "BURMYMCC", "TGLPHLCC", "SPAPRTCC", "RUSRUSCC", "SINLKACC",
			"THATHACC", "TURTURCC", "VSCVNMCC" };
	public final static String[][] ALPHABET = {
			{ "ا‎", "ب", "ت", "ث", "ج", "ح‎", "خ‎", "د", "ذ", "ر", "ز‎", "س", "ش", "ص", "ض‎", "ط‎", "ظ",
					"ع", "غ‎", "ف", "ق‎", "ك‎", "ل‎", "م", "ن", "ه", "و", "ي", "ء", "A", "B", "C", "D",
					"E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
					"V", "W", "X", "Y", "Z", "ETC" }, // Are
			{ "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
					"S", "T", "U", "V", "W", "X", "Y", "Z", "ETC" }, // Ban
			{ "ក", "ខ", "គ", "ឃ", "ង", "ច", "ឆ", "ជ", "ឈ", "ញ", "ដ", "ឋ", "ឌ", "ឍ", "ណ", "ត", "ថ", "ទ",
					"ធ", "ន", "ប", "ផ", "ព", "ភ", "ម", "យ", "រ", "ល", "វ", "ឝ", "ឞ", "ស", "ហ", "ឡ", "អ",
					"ហ្គ", "ហ្គ៊", "ហ្ន", "ប៉", "ហ្ម", "ហ្ល", "ហ្វ", "ហ្វ៊", "ហ្ស", "ហ្ស៊", "អា", "អិ",
					"អី", "អឹ", "អឺ", "អុ", "អូ", "អួ", "អើ", "អឿ", "អៀ", "អេ", "អែ", "អៃ", "អោ", "អៅ",
					"អុំ", "អំ", "អាំ", "អះ", "អុះ", "អេះ", "អោះ", "ឥ", "ឦ", "ឧ", "ឨ", "ឩ", "ឪ", "ឫ",
					"ឬ", "ឭ", "ឮ", "ឯ", "ឰ", "ឱ", "ឲ", "ឳ", "A", "B", "C", "D", "E", "F", "G", "H", "I",
					"J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
					"ETC" }, // KHM
			{ "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
					"S", "T", "U", "W", "X", "Y", "Z", "ETC" }, // Chn
			{ "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
					"S", "T", "U", "V", "W", "X", "Y", "Z", "ETC" }, // Eng
			{ "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "Ñ", "O", "P", "Q",
					"R", "S", "T", "U", "V", "W", "X", "Y", "Z", "ETC" }, // Esp
			{ "A", "B", "C", "D", "E", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S",
					"T", "U", "V", "W", "Y", "Z", "ETC" }, // Ind
			{ "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
					"S", "T", "U", "V", "W", "X", "Y", "Z", "ETC" }, // Ins
			{ "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "L", "M", "N", "O", "P", "Q", "R", "S",
					"T", "U", "V", "Z", "ETC" }, // Ita
			{ "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
					"S", "T", "U", "V", "W", "X", "Y", "Z", "ETC" }, // Jpn
			{ "가", "나", "다", "라", "마", "바", "사", "아", "자", "차", "카", "타", "파", "하", "A", "B", "C", "D",
					"E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U",
					"V", "W", "X", "Y", "Z", "ETC" }, // Kor
			{ "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "R", "S",
					"T", "U", "W", "Y", "Z", "ETC" }, // Mal
			{ "А", "Б", "В", "Г", "Д", "Е", "Ё", "Ж", "З", "И", "Й", "К", "Л", "М", "Н", "О", "Ө", "П",
					"Р", "С", "Т", "У", "Ү", "Ф", "Х", "Ц", "Ч", "Ш", "Щ", "Ъ", "Ы", "Ь", "Э", "Ю", "Я",
					"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q",
					"R", "S", "T", "U", "V", "W", "X", "Y", "Z", "ETC" }, // Mon
			{ "က", "ခ", "ဂ", "ဃ", "င", "စ", "ဆ", "ဇ", "ဈ", "ဉ", "ဋ", "ဌ", "ဍ", "ဎ", "ဏ", "တ", "ထ", "ဒ",
					"ဓ", "န", "ပ", "ဖ", "ဗ", "ဘ", "မ", "ယ", "ရ", "လ", "ဝ", "သ", "ဟ", "ဠ", "အ", "A", "B",
					"C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S",
					"T", "U", "V", "W", "X", "Y", "Z", "ETC" }, // MMR
			{ "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
					"S", "T", "U", "V", "W", "X", "Y", "Z", "ETC" }, // Phn
			{ "Á", "A", "B", "C", "D", "É", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "Ô", "O",
					"P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "ETC" }, // Por
			{ "А", "Б", "В", "Г", "Д", "Е", "Ё", "Ж", "З", "И", "Й", "К", "Л", "М", "Н", "О", "П", "Р",
					"С", "Т", "У", "Ф", "Х", "Ц", "Ч", "Ш", "Щ", "Ъ", "Ы", "Ь", "Э", "Ю", "Я" }, // Rus
			{ "ක", "ට", "ත", "ප", "ස", "ච", "ම", "ල", "ව", "ණ", "අ", "එ", "ඉ", "ඔ", "උ", "ඇ", "ආ", "ඒ",
					"ඊ", "ඕ", "ඌ", "ඈ", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
					"N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "ETC" }, // Lka
			{ "ก", "ข", "ฃ", "ค", "ฅ", "ฆ", "ง", "จ", "ฉ", "ช", "ซ", "ฌ", "ญ", "ฎ", "ฏ", "ฐ", "ฑ", "ฒ",
					"ณ", "ด", "ต", "ถ", "ท", "ธ", "น", "บ", "ป", "ผ", "ฝ", "พ", "ฟ", "ภ", "ย", "ม", "ร",
					"ล", "ว", "ศ", "ษ", "ส", "ห", "ฬ", "อ", "ฮ", "A", "B", "C", "D", "E", "F", "G", "H",
					"I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y",
					"Z", "ETC" }, // Tha
			{ "A", "B", "C", "Ç", "D", "E", "F", "G", "Ğ", "H", "I", "İ", "J", "K", "L", "M", "N", "O",
					"Ö", "P", "R", "S", "Ş", "T", "U", "Ü", "V", "Y", "Z", "ETC" }, // Tur
			{ "A", "Ă", "Â", "B", "C", "D", "Đ", "E", "Ê", "G", "H", "I", "K", "L", "M", "N", "O", "Ô",
					"Ơ", "P", "Q", "R", "S", "T", "U", "Ư", "V", "X", "Y", "ETC" } // Vit
	};
	public final static int ARE = 0;
	public final static int BAN = 1;
	public final static int KHM = 2;
	public final static int CHN = 3;
	public final static int ENG = 4;
	public final static int ESP = 5;
	public final static int IND = 6;
	public final static int INS = 7;
	public final static int ITA = 8;
	public final static int JPN = 9;
	public final static int KOR = 10;
	public final static int MAL = 11;
	public final static int MON = 12;
	public final static int MMR = 13;
	public final static int PHN = 14;
	public final static int POR = 15;
	public final static int RUS = 16;
	public final static int LKA = 17;
	public final static int THA = 18;
	public final static int TUR = 19;
	public final static int VIT = 20;

	public final static int MAX_LANG = 20;

	public static int getCountryFromNum(int iNum) {
		if (iNum > 10000 && iNum < 80001)
			return KOR;
		else if (iNum > 100000 && iNum < 200001)
			return ENG;
		else if (iNum > 200000 && iNum < 300001)
			return CHN;
		else if (iNum > 300000 && iNum < 450001)
			return JPN;
		else if (iNum > 500000 && iNum < 550001)
			return INS;
		else if (iNum > 550000 && iNum < 580001)
			return IND;
		else if (iNum > 580000 && iNum < 600001)
			return MAL;
		else if (iNum > 600000 && iNum < 650001)
			return RUS;
		else if (iNum > 650000 && iNum < 660001)
			return MON;
		else if (iNum > 660000 && iNum < 680001)
			return BAN;
		else if (iNum > 680000 && iNum < 700001)
			return PHN;
		else if (iNum > 700000 && iNum < 730001)
			return VIT;
		else if (iNum > 730000 && iNum < 780001)
			return ESP;
		else if (iNum > 780000 && iNum < 800001)
			return ITA;
		else if (iNum > 800000 && iNum < 840001)
			return POR;
		else if (iNum > 840000 && iNum < 860001)
			return LKA;
		else if (iNum > 860000 && iNum < 880001)
			return KHM;
		else if (iNum > 880000 && iNum < 900001)
			return MMR;
		else if (iNum > 900000 && iNum < 940001)
			return THA;
		else if (iNum > 940000 && iNum < 960001)
			return TUR;
		else if (iNum > 960000 && iNum < 980001)
			return ARE;

		return -1;
	}

	public static String getExternalStoragePath() {

		String externalState = Environment.getExternalStorageState();
		if (externalState.equals(Environment.MEDIA_MOUNTED)) {
			return Environment.getExternalStorageDirectory().getAbsolutePath();
		}

		return "";
	}

	public static File getDownloadFolder() {
		String externalState = Environment.getExternalStorageState();
		if (externalState.equals(Environment.MEDIA_MOUNTED)) {
			return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		}

		return null;
	}

	public static String realRootPath = "";

	public static String getRootPath() {
		return realRootPath + ROOTPATH;
	}

	public static String getRootSongsPath() {
		return realRootPath + ROOTPATH + "songs/";
	}

	public static String getRootBackgroundsPath() {
		return realRootPath + ROOTPATH + "bg/";
	}

	public static String getFanfarePath() {
		return realRootPath + FANFAREPATH;
	}

	public static boolean copyFileFromAsset(Context context, String strAssetName, String strDesPath,
			boolean bOverwrite) {
		Global.Debug("Starting copy file from asset....name=" + strAssetName + ", path=" + strDesPath);

		File desFile = new File(strDesPath);

		try {

			if (desFile.exists()) {
				if (bOverwrite || desFile.length() == 0)
					desFile.delete();
				else
					return true;
			}

			if (!desFile.createNewFile())
				return false;

			InputStream is = context.getAssets().open(strAssetName);
			OutputStream os = new FileOutputStream(strDesPath);

			BufferedInputStream bis = new BufferedInputStream(is);
			BufferedOutputStream bos = new BufferedOutputStream(os);

			int bytesRead = 0;
			byte[] buffer = new byte[1024];
			while ((bytesRead = bis.read(buffer, 0, 1024)) != -1) {
				bos.write(buffer, 0, bytesRead);
			}

			bos.close();
			bis.close();
			os.close();
			is.close();
		} catch (Exception e) {
			if (desFile.exists())
				desFile.delete();
			Global.Debug("Failed copy file from asset!");
			return false;
		}
		Global.Debug("End copy file from asset");
		return true;
	}

	public static boolean copyFile(String strSrcPath, String strDesPath) {
		FileChannel fcIn = null, fcOut = null;
		FileInputStream fis = null;
		FileOutputStream fos = null;

		try {
			File srcFile = new File(strSrcPath);
			File desFile = new File(strDesPath);
			if (!srcFile.exists())
				return false;

			if (desFile.exists())
				desFile.delete();

			if (!desFile.createNewFile())
				return false;

			fis = new FileInputStream(srcFile);
			fos = new FileOutputStream(desFile);

			fcIn = fis.getChannel();
			fcOut = fos.getChannel();

			long size = fcIn.size();
			fcIn.transferTo(0, size, fcOut);

			fcIn.close();
			fcOut.close();

			fis.close();
			fos.close();
		} catch (Exception e) {
			try {
				if (fcIn != null)
					fcIn.close();
				if (fcOut != null)
					fcOut.close();
				if (fis != null)
					fis.close();
				if (fos != null)
					fos.close();
			} catch (Exception e1) {

			}
			return false;
		}

		return true;
	}

	public static boolean moveFile(String strSrcPath, String strDesPath) {
		if (!copyFile(strSrcPath, strDesPath))
			return false;
		File srcFile = new File(strSrcPath);

		return srcFile.delete();
	}

	/*
	 * get total ram size for KB.
	 */
	public static synchronized double getTotalRAM() {
		double fSize = 0;
		RandomAccessFile reader = null;
		String load = null;
		try {
			reader = new RandomAccessFile("/proc/meminfo", "r");
			load = reader.readLine();
			Pattern p = Pattern.compile("(\\d+)");
			Matcher m = p.matcher(load);
			String value = "";
			while (m.find()) {
				value = m.group(1);
			}
			reader.close();

			fSize = Double.parseDouble(value);
		} catch (Exception e) {
			fSize = 0;
		} finally {
			try {
				reader.close();
			} catch (Exception e1) {
			}
		}
		return fSize;
	}

	public static void setGlobalFont(View view, Typeface face) {
		TextView txt;
		if (view != null) {
			if (view instanceof ViewGroup) {
				ViewGroup vg = (ViewGroup) view;
				int vgCnt = vg.getChildCount();
				for (int i = 0; i < vgCnt; i++) {
					View v = vg.getChildAt(i);
					if (v instanceof TextView) {
						txt = (TextView) v;
						txt.setTypeface(face);
						txt.setText(txt.getText(), TextView.BufferType.SPANNABLE);
					}
					setGlobalFont(v, face);
				}
			}
		}
	}

	public static boolean isRecord = false;

	public static Boolean isLyricsStartEvent(MidiEvent event) {
		return event.EventFlag == MidiFile.EventProgramChange && event.Instrument == 0;
	}

	public static Boolean isLyricsMiddleEvent(MidiEvent event) {
		if (event.Metaevent == MidiFile.MetaEventLyric)
			return false;
		return event.EventFlag == MidiFile.EventProgramChange && event.Instrument == 3;
	}

	public static Boolean isLyricsEvent(MidiEvent event) {
		if (event.Metaevent == MidiFile.MetaEventLyric) {
			return true;
		}
		else if (event.EventFlag == MidiFile.EventProgramChange && event.Instrument > 0) {
			return true;
		}
		return false;
	}

	public static String removeSungBy(String str) {
		return str.replace("Sung by ", "").replace("Sung By", "");
	}

	public static void Debug(String strMsg) {
		if (D) {
			Log.i(TAG, strMsg);
			if (DEBUG_FILE) {
				Global.DebugToFile(strMsg);
			}
		}
	}

	private static void DebugToFile(String strMsg) {
		if (!DEBUG_FILE)
			return;

		try {
			String strPath = realRootPath + ROOTPATH + "/debug.txt";
			File file = new File(strPath);
			if (!file.exists())
				file.createNewFile();
			BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
			bw.write(strMsg + "\n");
			bw.close();
		} catch (Exception e) {
		}
	}
}