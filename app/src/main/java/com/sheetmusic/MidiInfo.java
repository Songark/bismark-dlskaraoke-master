package com.sheetmusic;

import com.tbtc.jftv.common.Global;

public class MidiInfo {
	private int language;
	private String num;
	private String title;
	private String singer;
	private int alphabet;
	private int slphabet;
	private String sorttitle;
	private String sortsinger;
	private String allalpha;
	private String allslpha;
	private int type;
	private int sex;
	private int event;
	private String regdate;
	private boolean record;
	private String file;
	
	public MidiInfo() {		
		record = false;
	}
	public MidiInfo(String strNum, String strTitle, String strSinger, int iType, String strRegDate) {
		this.language = -1;
		this.num = strNum;
		this.title = strTitle;
		this.singer = Global.removeSungBy(strSinger);
		this.alphabet = -1;
		this.slphabet = -1;
		this.sorttitle = strTitle;
		this.sortsinger = strSinger;
		this.allalpha = "";
		this.allslpha = "";
		this.type = iType;
		this.sex = 0;
		this.event = 0;
		this.regdate = strRegDate;
		this.record = true;
	}
	public MidiInfo(int iLang, String strNum, String strTitle, String strSinger, int iAlphabet, int iSlphabet, String strSortTitle, String strSortSinger, String strAllAlpha, String strAllSlpha, int iType, int iSex, int iEvent) {
		this.language = iLang;
		this.num = strNum;
		this.title = strTitle;
		this.singer = Global.removeSungBy(strSinger);
		this.alphabet = iAlphabet;
		this.slphabet = iSlphabet;
		this.sorttitle = strSortTitle;
		this.sortsinger = strSortSinger;
		this.allalpha = strAllAlpha;
		this.allslpha = strAllSlpha;
		this.type = iType;
		this.sex = iSex;
		this.event = iEvent;
		this.regdate = "";
		this.record = false;
	}
	public int getLanguage() {
		return this.language;
	}
	public String getNum() {
		return num;
	}
	public String getTitle() {
		return title;
	}
	public String getSinger() {
		return singer;
	}	
	public int getType() {
		return type;
	}
	public String getRegDate() {
		return regdate;
	}
	public boolean isMTV() {
		return (!record && type == Global.TYPE_MTV) || (record && type == Global.RTYPE_MP4);
	}
	public boolean isMP3() {
		return (!record && type == Global.TYPE_MP3) || (record && type == Global.RTYPE_MP3);
	}
	public boolean isCDG() {
		return type == Global.TYPE_CDG;
	}
	public boolean isMidi() {
		return type == Global.TYPE_MIDI;
	}
	public int getSex() {
		return sex;
	}	
	public boolean hasSinger() {
		return sex > 0;
	}
	public int getEvent() {
		return event;
	}
	public boolean isEvent() {
		return event == 1;
	}
	public boolean isRecord() {
		return record;
	}
	public int getAlphabet() {
		return alphabet;
	}
	public int getSlphabet() {
		return slphabet;
	}
	public String getSorttitle() {
		return sorttitle;
	}
	public String getSortsinger() {
		return sortsinger;
	}
	public String getAllalpha() {
		return allalpha;
	}
	public String getAllslpha() {
		return allslpha;
	}
	public void setFile(String filePath) {
		file = filePath;
	}
	public String getFile() {
		return file;
	}
}
