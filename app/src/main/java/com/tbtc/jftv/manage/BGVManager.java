package com.tbtc.jftv.manage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.tbtc.jftv.common.Global;

import android.content.Context;

public class BGVManager {
	public static final String DEFAULT_VIDEO = "m003.mp4";
	
	public static final String[] VIDEOPATHS = {Global.ROOTPATH + "BGV1/", Global.ROOTPATH + "BGV2/", Global.ROOTPATH + "BGV3/", Global.ROOTPATH + "BGV4/", Global.ROOTPATH + "BGV5/"};
	private static final int VIDEOCOUNT = 5;
	private static BGVManager s_instance = null;
	public synchronized static BGVManager getManager() {
		if(s_instance == null) {
			s_instance = new BGVManager();
		}
		return s_instance;
	}
	
	public BGVManager() {		
	}
	
	@SuppressWarnings("unused")
	private Context mContext;
	public void init(Context context) {
		mContext = context;
		mCurrentIdx = SetupManager.getManager().readBGVIndex();
		mArrPath = new String[VIDEOCOUNT][];
	}
	
	public String getDefaultPath() {
		return Global.realRootPath + VIDEOPATHS[0] + DEFAULT_VIDEO;
	}
	public String getDefaultDirectory() {
		return Global.realRootPath + VIDEOPATHS[0];
	}
	
	private String[][] mArrPath;
	private int mCurrentIdx = 0;
	public int nextBGV() {
		int iRet = mCurrentIdx;
		do {
			iRet = (iRet + 1) % VIDEOCOUNT;
		} while(mArrPath[iRet] == null || mArrPath[iRet].length < 1);
		if(iRet == mCurrentIdx) return -1;
		mCurrentIdx = iRet;
		SetupManager.getManager().saveBGVIndex(mCurrentIdx);
		return iRet;
	}
	public boolean checkBGV() {
		String strPath = "";
		File dirFile;
		for(int i = 0; i < VIDEOCOUNT; i++) {
			strPath = Global.realRootPath + VIDEOPATHS[i];
			dirFile = new File(strPath);
			if(!dirFile.exists()) {
				dirFile.mkdir();
				continue;
			}
			String[] arrFiles = dirFile.list();
			for(int j = 0; j < arrFiles.length; j++) {
				if(arrFiles[j].endsWith(".mp4")) {
					return true;
				}
			}
		}
		return false;
	}
	public void load() {
		String strPath = "";
		File dirFile;
		List<String> arrTemp = new ArrayList<String>();
		for(int i = 0; i < VIDEOCOUNT; i++) {
			arrTemp.clear();
			strPath = Global.realRootPath + VIDEOPATHS[i];
			dirFile = new File(strPath);
			if(!dirFile.exists()) {
				dirFile.mkdir();
				continue;
			}
			String[] arrFiles = dirFile.list();
			for(int j = 0; j < arrFiles.length; j++) {
				if(arrFiles[j].endsWith(".mp4")) {
					arrTemp.add(strPath + arrFiles[j]);
				}
			}
			mArrPath[i] = new String[arrTemp.size()];
			for(int j = 0; j < arrTemp.size(); j++) {
				mArrPath[i][j] = arrTemp.get(j);
			}
			Global.Debug("BGVManager::load -- BGV" + (i + 1) + " count=" + arrTemp.size());
		}
	}
	
	public String getCurrentPath() {
		String[] arr = mArrPath[mCurrentIdx];
		if(arr == null || arr.length < 1) {
			mCurrentIdx = 0;
			SetupManager.getManager().saveBGVIndex(mCurrentIdx);
			Global.Debug("BGVManager::getCurrentPath --> No exist current index! Will play default video!");
			return getDefaultPath();
		}
		
		String strPath = arr[(int)(Math.random() * (arr.length - 1))];
		Global.Debug("BGVManager::getCurrentPath --> path=" + strPath);
		return strPath;
	}
}
