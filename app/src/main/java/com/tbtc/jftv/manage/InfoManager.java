package com.tbtc.jftv.manage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.tbtc.jftv.common.Global;

import com.sheetmusic.InfoCore;
import com.sheetmusic.LyricsInfo;
import com.sheetmusic.MidiInfo;

public class InfoManager {
	public static InfoManager s_instance = null;
	public static InfoManager getManager() {
		if(s_instance == null) {
			s_instance = new InfoManager();
		}
		
		synchronized (s_instance) {
			return s_instance;
		}
	}
	
	private InfoCore mCore;

	private String mCurrentMidPath = "";
	private String mCurrentTxtPath = "";
	
	private Context mContext;
	public String getCurrentPath() {
		return mCurrentTxtPath;
	}
	
	public InfoManager() {
		mCore = new InfoCore();
	}
	
	public void init(Context context) {
		mContext = context;

	}

	public boolean mergeSoundFont(String strSfPath, String strSfdPath) {
		return mCore.mergeSoundFont(strSfPath, strSfdPath) == 1;
	}
	public boolean restoreSoundFont(String strSfdPath, String strSfPath) {
		return mCore.restoreSoundFont(strSfdPath, strSfPath) == 1;
	}
	public void deleteMidi() {
		File fileDir = mContext.getFilesDir();
		if(!fileDir.exists())
			return;
		
		String[] arrFiles = fileDir.list();
		File file;
		if(arrFiles != null) {
			for(int i = 0; i < arrFiles.length; i++) {
				if(arrFiles[i].contains(".mid") || arrFiles[i].contains(".txt") || arrFiles[i].contains(".mp3")) {
					file = new File(fileDir + "/" + arrFiles[i]);
					file.delete();
				}
			}
		}
		/*File file = new File(mCurrentMidPath);
		if(file.exists())
			file.delete();
		file = new File(mCurrentTxtPath);
		if(file.exists())
			file.delete();*/
	}
	
	private void unzip(String src, String dest)
	{
	    final int BUFFER_SIZE = 4096;
	     
	    BufferedOutputStream bufferedOutputStream = null;
	    FileInputStream fileInputStream;
		try {
			fileInputStream = new FileInputStream(src);
			ZipInputStream zipInputStream = new ZipInputStream(
					new BufferedInputStream(fileInputStream));
			ZipEntry zipEntry;

			while ((zipEntry = zipInputStream.getNextEntry()) != null) {
				String zipEntryName = zipEntry.getName();
				File file = new File(dest + File.separator + zipEntryName);

				if (file.exists()) {

				} else {
					if (zipEntry.isDirectory()) {
						file.mkdirs();
					} else {
						byte buffer[] = new byte[BUFFER_SIZE];
						FileOutputStream fileOutputStream = new FileOutputStream(
								file);
						bufferedOutputStream = new BufferedOutputStream(
								fileOutputStream, BUFFER_SIZE);
						int count;

						while ((count = zipInputStream.read(buffer, 0,
								BUFFER_SIZE)) != -1) {
							bufferedOutputStream.write(buffer, 0, count);
						}

						bufferedOutputStream.flush();
						bufferedOutputStream.close();
					}
				}
			}
			zipInputStream.close();
			//File s = new File(src);
			//s.delete();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
}
