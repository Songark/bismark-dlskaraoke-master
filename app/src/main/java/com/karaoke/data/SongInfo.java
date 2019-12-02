package com.karaoke.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.tbtc.jftv.common.Global;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SongInfo implements Parcelable {

	public static final int TYPE_MAGICSING = 1;
	public static final int TYPE_KUMYONG = 2;

	public static final int NATION_KOR = 1;
	public static final int NATION_JPN = 2;
	public static final int NATION_CHN = 3;

	public int songNumber;
	public int songType;
	public int songNation;
	public String songTitle;
	public String songArtist;
	public String songMidiname;
	public String songLyricname;
	public String songGenre;
	public String songKey;
	public String songShift;

	public SongInfo() {}

	public SongInfo(int type, int nation, int number, String title, String artist) {
		songType = type;
		songNation = nation;
		songNumber = number;
		songTitle = title;
		songArtist = artist;

		String typeTailer[] = {"KR", "JP", "CN"};
		if (songNation > 0) {
			songMidiname = String.format("%05d", songNumber) + "_" + typeTailer[songNation - 1] + ".mid";
			songLyricname = String.format("%05d", songNumber) + "_" + typeTailer[songNation - 1] + ".lyr";
		}
	}

	public SongInfo(int type, String lyricsFile) {

		String songFilename = lyricsFile.substring(lyricsFile.lastIndexOf("/") + 1);
		String songName = songFilename.substring(0, songFilename.lastIndexOf("."));
		String songNational = "KR";
		String songNumberStr = "";
		if (songName.lastIndexOf("_") >= 0) {
			songNational = songName.substring(songName.lastIndexOf("_") + 1);
			songNumberStr = songName.substring(0, songName.lastIndexOf("_"));
		}
		else {
			songNumberStr = songName;
		}

		Global.Debug("\t" + songFilename + ", " + songName + ", " + songNational + ", " + songNumberStr);

		try {
			File f = new File(lyricsFile);
			if (!f.exists()) {
				throw new Exception("File not found. path = " + lyricsFile);
			}

			String charsetName = "euc-kr";
			songType = type;
			songNation = SongInfo.NATION_KOR;
			if (songNational.compareTo("CN") == 0) {
				charsetName = "gb2312";
				songNation = SongInfo.NATION_CHN;
			}
			else if (songNational.compareTo("KR") == 0) {
				charsetName = "euc-kr";
				songNation = SongInfo.NATION_KOR;
			}
			else if (songNational.compareTo("JP") == 0) {
				charsetName = "jis";
				songNation = SongInfo.NATION_JPN;
			}

			songNumber = Integer.parseInt(songNumberStr);

			InputStream is = new FileInputStream(f);
			BufferedReader br = new BufferedReader(new InputStreamReader(is, charsetName));
			String strLine = "";

			if (songType == SongInfo.TYPE_MAGICSING) {
				do {
					strLine = br.readLine();
				} while (strLine.substring(0, 1).compareTo("!") == 0);
				songTitle = strLine.trim();		// title
				br.readLine();				//
				br.readLine();				//
				br.readLine();				//
				songArtist = br.readLine().trim();	// artist
				songLyricname = songFilename;
				songMidiname = songFilename.substring(0, songFilename.lastIndexOf(".")) + ".mid";
			}
			else if (songType == SongInfo.TYPE_KUMYONG) {
				br.readLine();				// special
				songTitle = br.readLine().trim();	// title
				br.readLine();				// space
				br.readLine();				// lyric_maker
				br.readLine();				// composer
				songArtist = br.readLine().trim();	// singer
				if (songArtist.indexOf(" ") >= 1) {
					songArtist = songArtist.substring(0, songArtist.indexOf(" "));
				}
				songLyricname = songFilename;
				songMidiname = songFilename.substring(0, songFilename.lastIndexOf(".")) + ".mid";
			}

			Global.Debug("songTitle: " + songTitle + ", songMidiname: " + songMidiname);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(songNumber);
		dest.writeString(songTitle);
		dest.writeString(songArtist);
		dest.writeString(songMidiname);
		dest.writeString(songLyricname);
		dest.writeInt(songType);
		dest.writeInt(songNation);
	}

	private SongInfo(Parcel source) {
		songNumber = source.readInt();
		songTitle = source.readString();
		songArtist = source.readString();
		songMidiname = source.readString();
		songLyricname = source.readString();
		songType = source.readInt();
		songNation = source.readInt();
	}

	public int getLanguage()
	{
		int iLang = Global.KOR;
		switch (songNation)
		{
			case NATION_KOR:
				iLang = Global.KOR;
				break;
			case NATION_CHN:
				iLang = Global.CHN;
				break;
			case NATION_JPN:
				iLang = Global.JPN;
				break;
		}
		return iLang;
	}

	public static final Parcelable.Creator<SongInfo> CREATOR = new Parcelable.Creator<SongInfo>() {
		@Override
		public SongInfo createFromParcel(Parcel source) {
			return new SongInfo(source);
		}

		@Override
		public SongInfo[] newArray(int size) {
			return new SongInfo[size];
		}
	};
}
