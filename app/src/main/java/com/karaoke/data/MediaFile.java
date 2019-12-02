package com.karaoke.data;

import java.io.File;

import android.text.TextUtils;

public class MediaFile {

	private String title;
	private String filePath;
	private int index;
	private String artist;
	private boolean isToPlay;
	private boolean isSelected;
	private SongInfo curSongInfo = null;

	// Only used for history
	private long time;
	private int cntPlayed;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath, SongInfo songInfo) {
		this.filePath = filePath;
		this.curSongInfo = songInfo;
	}
	
	public String getFileName() {
		if (TextUtils.isEmpty(filePath))
			return null;
		File physicalFile = new File(filePath);
		return physicalFile.getName();
	}

	public boolean containsKeyword(String keyword) {
		if (TextUtils.isEmpty(keyword))
			return true;

		keyword = keyword.toLowerCase();
		String fileName = getFileName().toLowerCase();
		String fileTitle = getTitle().toLowerCase();
		return fileName.contains(keyword) || fileTitle.contains(keyword);
	}

	public String getArtist() {
		if (TextUtils.isEmpty(artist))
			return "";
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public boolean isExist() {
		return new File(filePath).exists();
	}

	public boolean equals(MediaFile otherFile) {
		if (otherFile == null)
			return false;

		if (TextUtils.isEmpty(filePath) || TextUtils.isEmpty(otherFile.getFilePath()))
			return false;

		if (filePath.equals(otherFile.getFilePath()))
			return true;
		return false;
	}

	public boolean isMidiFile() {
		String ext = filePath.substring(filePath.length() - 3).toLowerCase();
		if (ext.equals("mid")) {
			return true;
		}

		return false;
	}

	public String getLyricsFilePath() {
		if (curSongInfo != null && filePath.length() > 0) {
			if (curSongInfo.songType == SongInfo.TYPE_MAGICSING)
				return filePath.substring(0, filePath.length() - 4) + ".lyr";
			else if (curSongInfo.songType == SongInfo.TYPE_KUMYONG)
				return filePath.substring(0, filePath.length() - 4) + ".sok";
		}
		return "";
	}

	public boolean isCountryOf(String countryName) {
		if (TextUtils.isEmpty(countryName))
			return false;

		String prefix = "Karaoke" + File.separator + "Songs" + File.separator + countryName;
		prefix = prefix.toLowerCase().trim();
		if (filePath.toLowerCase().contains(prefix)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isArtistOf(String singer) {
		if (TextUtils.isEmpty(artist))
			return false;
		
		return artist.equals(singer);
	}
}
