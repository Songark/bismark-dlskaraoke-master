package com.sheetmusic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;

import com.karaoke.data.SongInfo;
import com.tbtc.jftv.common.Global;

public class LyricsInfo {	
	private String num;
	private String version;
	private String language;
	private String languageName;
	private String languageCode;
	private String title;
	private String subTitle;
	private String singer;
	private String subsinger;
	private String composer;
	private String writer;
	private String copyright;
	private String country;
	private String gender;
	private String key;
	private String genre;
	private String filepath;
	private int alphabetIndex;
	private int singerAlphabetIndex;
	
	private ArrayList<String> lyrics;
	private ArrayList<String> lyricsEn;
	
	public LyricsInfo() {
		this.num = "";
		this.version = "";     
		this.language = "";    
		this.languageName = "";
		this.languageCode = "";
		this.title = "";       
		this.subTitle = "";    
		this.singer = "";      
		this.subsinger = "";   
		this.composer = "";    
		this.writer = "";      
		this.copyright = "";   
		this.country = "";     
		this.gender = "";      
		this.key = "";         
		this.genre = "";
		this.filepath = "";
				
		this.lyrics = new ArrayList<String>();
		this.lyricsEn = new ArrayList<String>();
		
		this.alphabetIndex = -1;
		this.singerAlphabetIndex = -1;
	}
	
	public String getNum() {
		return num;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getLanguageName() {
		return languageName;
	}
	public void setLanguageName(String languageName) {
		this.languageName = languageName;
	}
	public String getLanguageCode() {
		return languageCode;
	}
	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSubTitle() {
		return subTitle;
	}
	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}
	public String getViewTitle() {
		if((this.language.equals(Global.LANGCODE[Global.CHN]) || this.language.equals(Global.LANGCODE[Global.JPN])) && !subTitle.equals(""))
			return subTitle;
		
		return title;
	}
	public String getSinger() {
		return singer;
	}
	public void setSinger(String singer) {
		this.singer = Global.removeSungBy(singer);
	}
	public String getSubsinger() {
		return subsinger;
	}
	public void setSubsinger(String subsinger) {
		this.subsinger = Global.removeSungBy(subsinger);
	}
	public String getViewSinger() {
		if((this.language.equals(Global.LANGCODE[Global.CHN]) || this.language.equals(Global.LANGCODE[Global.JPN])) && !subsinger.equals(""))
			return subsinger;
		
		return singer;
	}
	public String getComposer() {
		return composer;
	}
	public void setComposer(String composer) {
		this.composer = composer.replace("Composed by ", "");
	}
	public String getWriter() {
		return writer;
	}
	public void setWriter(String writer) {
		this.writer = writer.replace("Written by ", "");
	}
	public String getCopyright() {
		return copyright;
	}
	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getGenre() {
		return genre;
	}
	public void setGenre(String genre) {
		this.genre = genre;
	}
	public ArrayList<String> getLyrics() {
		return lyrics;
	}
	public void setLyrics(ArrayList<String> lyrics) {
		this.lyrics = lyrics;
	}
	public ArrayList<String> getLyricsEn() {
		return lyricsEn;
	}
	public void setLyricsEn(ArrayList<String> lyricsEn) {
		this.lyricsEn = lyricsEn;
	}	

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
		this.num = filepath.substring(filepath.lastIndexOf("/") + 1);
	}
	
	public int getAlphabetIndex() {
		return alphabetIndex;
	}

	public void setAlphabetIndex(int alphabetIndex) {
		this.alphabetIndex = alphabetIndex;
	}

	public int getSingerAlphabetIndex() {
		return singerAlphabetIndex;
	}

	public void setSingerAlphabetIndex(int singerAlphabetIndex) {
		this.singerAlphabetIndex = singerAlphabetIndex;
	}

	public static String getUpper(String s) {
		return s.toUpperCase(Locale.ENGLISH);
	}

	public static LyricsInfo loadLyricsInfo(String lpath, SongInfo songInformation) {
		LyricsInfo lyricsInfo = null;
		try {
			File f = new File(lpath);
			if(!f.exists()) {
				throw new Exception("File not found. path=" + lpath);				
			}

			lyricsInfo = new LyricsInfo();
			lyricsInfo.setFilepath(lpath.replace(".lyr", ""));
			lyricsInfo.setSubTitle(songInformation.songTitle);
			lyricsInfo.setSinger(songInformation.songArtist);

			String charsetName = "utf-8";
			switch (songInformation.songNation) {
				case SongInfo.NATION_CHN:
					charsetName = "gb2312";
					lyricsInfo.setLanguage(Global.LANGCODE[Global.CHN]);
					break;
				case SongInfo.NATION_KOR:
					charsetName = "euc-kr";
					lyricsInfo.setLanguage(Global.LANGCODE[Global.KOR]);
					break;
				case SongInfo.NATION_JPN:
					charsetName = "jis";
					lyricsInfo.setLanguage(Global.LANGCODE[Global.JPN]);
					break;
			}

			InputStream is = new FileInputStream(f);
			BufferedReader br = new BufferedReader(new InputStreamReader(is, charsetName));
			String str = "";
			String strTemp = "";

			// get lyric details information
			if (songInformation.songType == SongInfo.TYPE_MAGICSING) {
				do {
					strTemp = br.readLine();
				} while (strTemp.length() > 0 && strTemp.charAt(0) == '!');
				for (int headerCnt = 1; headerCnt < 5; headerCnt++) {
					strTemp = br.readLine();
					switch (headerCnt) {
						case 3: {
							if (strTemp.length() > 0 && strTemp.indexOf("/") >= 0) {
								String[] info = strTemp.split("/");
								lyricsInfo.setWriter(info[0].trim());
								lyricsInfo.setComposer(info[1].trim());
							}
							break;
						}
						case 4:
							break;
					}
					if (strTemp.length() > 0 && strTemp.charAt(0) == '@')
						break;
				}
				while((str = br.readLine()) != null && !getUpper(str).equals("@")) {
					if (str.length() == 0) continue;

					// str = str.toLowerCase();
					if(lyricsInfo.getLanguage().equals(Global.LANGCODE[Global.CHN])) {
						str = str.replace(" ", "");
					}
					else if(lyricsInfo.getLanguage().equals(Global.LANGCODE[Global.JPN])) {
						str = str.replace("/", "/ / ");
						str = str.substring(0, str.length() - 1);
					} else if(!lyricsInfo.getLanguage().equals(Global.LANGCODE[Global.KOR])) {
						str = str.replace("/", "/ ");
						str = str.substring(0, str.length() - 1);
					}
					if(!str.trim().equals("")) {
						lyricsInfo.getLyrics().add(str);
					}
				}
			}
			else if (songInformation.songType == SongInfo.TYPE_KUMYONG) {
				strTemp = br.readLine().trim();		// 50-00-0-M/Cm/Fm
				strTemp = br.readLine().trim();		// title
				strTemp = br.readLine();			// space
				strTemp = br.readLine().trim();		// lyric maker
				if (strTemp.indexOf(" ") > 0) {
					strTemp = strTemp.substring(0, strTemp.indexOf(" "));
				}
				lyricsInfo.setWriter(strTemp);
				strTemp = br.readLine().trim();		// composer
				if (strTemp.indexOf(" ") > 0) {
					strTemp = strTemp.substring(0, strTemp.indexOf(" "));
				}
				lyricsInfo.setComposer(strTemp);
				strTemp = br.readLine().trim();		// singer

				while((str = br.readLine()) != null && !getUpper(str).equals("@") && !getUpper(str).equals("->")) {
					str = str.trim();
					if (str.length() == 0) continue;

					// str = str.toLowerCase();
					if(lyricsInfo.getLanguage().equals(Global.LANGCODE[Global.CHN])) {
						str = str.replace(" ", "");
					}
					else if(lyricsInfo.getLanguage().equals(Global.LANGCODE[Global.JPN])) {
						str = str.replace("/", "/ / ");
						str = str.substring(0, str.length() - 1);
					} else if(!lyricsInfo.getLanguage().equals(Global.LANGCODE[Global.KOR])) {
						str = str.replace("/", "/ ");
						str = str.substring(0, str.length() - 1);
					}
					if(!str.trim().equals("")) {
						lyricsInfo.getLyrics().add(str);
					}
				}
			}

			br.close();
			is.close();
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}

		return lyricsInfo;
	}	

	private static String getUpperCase(String strText, int iLang) {
		String strRet = "";
		Locale loc = Locale.US;
		switch(iLang) {
		case Global.ARE:
			loc = new Locale("ar");			
			break;
		case Global.KHM:
			loc =  new Locale("km");
			break;
		case Global.ESP:
			loc = new Locale("es");
			break;
		case Global.MON:
			loc = new Locale("mn");
			break;
		case Global.MMR:
			loc = new Locale("tbq");
			break;
		case Global.RUS:
			loc = new Locale("ru");
			break;
		case Global.LKA:
			loc = new Locale("si");
			break;
		case Global.THA:
			loc = new Locale("th");
			break;
		case Global.TUR:
			loc = new Locale("trk");
			break;
		case Global.VIT:
			loc = new Locale("vi");
			break;
		}
		strRet = strText.toUpperCase(loc);
		
		return strRet;
	}
	
	private static final char[] DOUBLE_CHAR = {'ㄳ', 'ㄵ', 'ㄼ', 'ㄽ', 'ㄾ', 'ㅄ', 'ㄺ', 'ㄻ', 'ㄿ', 'ㄶ', 'ㅀ'};
	private static final String[] RESOLVE_CHAR = {"0;6;", "1;8;", "3;5;", "3;6;", "3;11;", "5;6;", "3;0;", "3;4;", "3;12;", "1;13;", "3;13;"};
	private static String checkDoubleChar(char szChar) {
		String strRet = null;
		for(int i = 0; i < DOUBLE_CHAR.length; i++) {
			if(szChar == DOUBLE_CHAR[i]) {
				strRet = RESOLVE_CHAR[i];
				break;
			}
		}
		return strRet;
	}
	public static String getAllAlpha(String strText, int iLang) {
		String strRet = "", strTemp = "";
		int i = 0;
		for(i = 0; i < strText.length(); i++) {
			strTemp = checkDoubleChar(strText.charAt(i));
			if(strTemp != null) {
				strRet += strTemp; 
			} else {
				strRet += LyricsInfo.getAlphabetIndex(strText.substring(i, i + 1), Global.ALPHABET[iLang], iLang) + ";";
			}
		}
		return strRet;
	}
	
	public static int getAlphabetIndex(String strText, String[] arrAlphabet, int iLang) {
		int iRet = -1;
		
		if(strText == null || strText.equals("") || arrAlphabet == null)
			return -1;
		
		if(iLang == Global.KOR) {
			iRet = getHangulIndex(strText.charAt(0));
		}
		
		String strTemp = getUpperCase(strText.substring(0, 1), iLang);
		if(iRet < 0) {
			for(int i = 0; i < arrAlphabet.length; i++) {
				if(arrAlphabet[i].equals(strTemp) || arrAlphabet[i].equals("ETC")) {
					iRet = i;
					break;
				}				
			} 
		}		
		
		return iRet;
	}
	public static int getHangulIndex(char szChar) {
		int iRet = -1;
		
		if(szChar >= 0xAC00) { //&& szChar <= 0xD79F
			int iCode = (szChar - 0xAC00) / (21 * 28);
			
			switch(iCode) {
			case 0:
			case 1:
				iRet = 0;	//ㄱ
				break;
			case 2:
				iRet = 1;	//ㄴ
				break;
			case 3:
			case 4:
				iRet = 2;	//ㄷ, ㄸ
				break;
			case 5:
				iRet = 3;	//ㄹ
				break;
			case 6:
				iRet = 4;	//ㅁ
				break;
			case 7:
			case 8:
				iRet = 5;	//ㅂ, ㅃ
				break;
			case 9:
			case 10:
				iRet = 6;	//ㅅ, ㅆ
				break;
			case 11:
				iRet = 7;	//ㅇ
				break;
			case 12:
			case 13:
				iRet = 8;	//ㅈ, ㅉ
				break;
			case 14:
				iRet = 9;	//ㅊ
				break;
			case 15:
				iRet = 10;	//ㅋ
				break;
			case 16:
				iRet = 11;	//ㅌ
				break;
			case 17:
				iRet = 12;	//ㅍ
				break;
			case 18:
				iRet = 13;	//ㅎ
				break;
			}
		} else {
			switch(szChar) {
			case 'ㄱ':
			case 'ㄲ':
				iRet = 0;	//ㄱ
				break;
			case 'ㄴ':
				iRet = 1;	//ㄴ
				break;
			case 'ㄷ':
			case 'ㄸ':
				iRet = 2;	//ㄷ, ㄸ
				break;
			case 'ㄹ':
				iRet = 3;	//ㄹ
				break;
			case 'ㅁ':
				iRet = 4;	//ㅁ
				break;
			case 'ㅂ':
			case 'ㅃ':
				iRet = 5;	//ㅂ, ㅃ
				break;
			case 'ㅅ':
			case 'ㅆ':
				iRet = 6;	//ㅅ, ㅆ
				break;
			case 'ㅇ':
				iRet = 7;	//ㅇ
				break;
			case 'ㅈ':
			case 'ㅉ':
				iRet = 8;	//ㅈ, ㅉ
				break;
			case 'ㅊ':
				iRet = 9;	//ㅊ
				break;
			case 'ㅋ':
				iRet = 10;	//ㅋ
				break;
			case 'ㅌ':
				iRet = 11;	//ㅌ
				break;
			case 'ㅍ':
				iRet = 12;	//ㅍ
				break;
			case 'ㅎ':
				iRet = 13;	//ㅎ
				break;
			}
		}
		
		return iRet;
	}
}
