package com.sheetmusic;

public class InfoCore {
	public native int restoreMidi(String strDatPath, String strMidiPath, String strTxtPath);
	public native int mergeSoundFont(String strSfPath, String strSfdPath);
	public native int restoreSoundFont(String strSfdPath, String strSfPath);
	
	static {
		System.loadLibrary("midinfo");
	}
}
