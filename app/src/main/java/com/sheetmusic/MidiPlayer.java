package com.sheetmusic;

import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Locale;

import com.karaoke.data.SongInfo;
import com.sheetmusic.MidiFile;
import com.sheetmusic.MidiFileException;
import com.tbtc.jftv.common.Global;
import com.tbtc.jftv.equalizer.EqualizerInfo;
import com.tbtc.jftv.manage.SetupManager;
import com.tbtc.jftv.text.KTextLayout;
import com.tbtc.jftv.text.KTextThread;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import jp.bismark.bssynth.sample.MainActivity;

public class MidiPlayer {
	private final static String[] MELODY_NAMES = { "melody", "melodie", "melodia", "vocal", "chant",
			"voice", "lead", "leadvocal", "canto"};

	public final static int PLAYER_READY = 0;
	public final static int PLAYER_PLAYING = 1;
	public final static int PLAYER_PAUSE = 2;
	public final static int PLAYER_SKIPPING = 3;
	public final static int PLAYER_DONE = 4;

	private static MidiPlayer _instance = null;

	public static MidiPlayer getPlayer() {
		if (_instance == null) {
			_instance = new MidiPlayer();
		}

		synchronized (_instance) {
			return _instance;
		}
	}
	
	private int playState = PLAYER_READY;
	public int getPlayState() {
		return playState;
	}
	public void setPlayState(int iValue) {
		playState = iValue;
	}

	public MidiFile m_midifile = null;          /* The midi file to play */
	public int skippingTicks = 0;
	private int mFadeoutTime;
	private double mDeltaTime = 0;
	private Context mContext = null;
	private KTextLayout kTxtLayout = null;
	private KTextThread kTxtThread = null;
	private OnMidiPlayListener mParent = null;
	private ArrayList<SongInfo> arrSelectedSongs = new ArrayList<SongInfo>();
	public int selectedSongIndex = 0;

	public KTextThread getTextThread() {
		return kTxtThread;
	}

	public void setSelectedSongArray(ArrayList<SongInfo> arrSongs)
	{
		arrSelectedSongs = arrSongs;
	}

	public void addSelectedSong(SongInfo selectedSong)
	{
		arrSelectedSongs.add(selectedSong);
	}

	public void removeSelectedSong(int index)
	{
		if (index >= 0 && index < arrSelectedSongs.size())
			arrSelectedSongs.remove(index);
	}

	public ArrayList<SongInfo> getSelectedSongs()
	{
		return arrSelectedSongs;
	}

	public void clearSelectedSongs()
	{
		arrSelectedSongs.clear();
	}

	public int getSelectedSongIndex(SongInfo songInfo)
	{
		for (int index = 0; index < arrSelectedSongs.size(); index++) {
			if (songInfo.songMidiname == arrSelectedSongs.get(index).songMidiname)
				return index;
		}
		return -1;
	}

	public int getCountofSelectedSongs()
	{
		return arrSelectedSongs.size();
	}

	public void setParent(OnMidiPlayListener pAct) {
		mParent = pAct;
	}

	public void setDeltaTime(double fTime, int iCurTime, int iCurTick) {
		mDeltaTime = fTime;
		kTxtThread.setDeltaTime(fTime, iCurTime, iCurTick);
	}

	public void setDeltaTime(double fTime) {
		mDeltaTime = fTime;
		if (kTxtThread != null)
			kTxtThread.setDeltaTime(fTime);
	}

	public void setKTextLayout(KTextLayout txtLayout) {
		kTxtLayout = txtLayout;
	}

	public MidiPlayer() {
	}

	public void init() {
	}

	public boolean initialize(Context context, KTextLayout txtlayout, Handler handler) {
		mContext = context;
		if (mContext == null)
			return false;

		kTxtLayout = txtlayout;

		return true;
	}

	private String sfPath;

	public String getSfPath() {
		return sfPath;
	}

	public void setSfPath(String strSfPath) {
		sfPath = strSfPath;
	}

	private SongInfo curSongInfo = null;
	private String midiPath;
	private ArrayList<MidiTrack> tracks = null;
	private MidiTrack melody = null;
	private MidiTrack lyrics = null;
	private int tickdelay[] = new int[3];
	private int division = 0;
	private int measure = 0;
	private double tempo = 1;
	private LyricsInfo lyricsInfo;

	private int readyLyricsTick;
	private int interludeStartTick, interludeEndTick;
	private int interludeStartTick1, interludeEndTick1;
	private int interludeLyricsIdx = 0;
	private int interludeLyricsIdx1 = 0;

	public int getInterludeLyricsIndex(int index) {
		if (index == 0)
			return interludeLyricsIdx;
		return interludeLyricsIdx1;
	}

	private ArrayList<Integer> mLyricsCharCnt = new ArrayList<Integer>();
	private ArrayList<Integer> mLyricsCharTicks = new ArrayList<Integer>();
	private int mLyricsCharIdx = 0;

	public void setLyricsInfo(LyricsInfo lInfo) {
		lyricsInfo = lInfo;
		if (kTxtLayout != null) {
			kTxtLayout.setLyrics(lyricsInfo.getLyrics(), lyricsInfo.getLyricsEn());
		}

		readyLyricsTick = -1;						// 전주 시작위치
		interludeStartTick = -1;					// 간주 시작위치
		interludeEndTick = -1;						// 간주 끝위치
		interludeStartTick1 = -1;					// 간주 시작위치
		interludeEndTick1 = -1;						// 간주 끝위치

		MidiEvent event;
		int i = 0;
		int iSum = 0;

		if (curSongInfo.songType == SongInfo.TYPE_MAGICSING) {
			// 4,3,2,1 시작하는 시점 찾기
			ArrayList<MidiEvent> _lyrics = lyrics.getLyrics();
			if (_lyrics == null || _lyrics.size() == 0)
				return;

			int iEventSize = _lyrics.size();
			while (true) {
				if (i >= iEventSize)
					break;
				event = _lyrics.get(i);
				iSum = event.StartTime;
				i++;
				if (Global.isLyricsEvent(event)) {
					break;
				}
			}
			readyLyricsTick = iSum - division * 4;		// 전주 카운터시작위치
			tickdelay[0] = division;

			// 간주 시작/끝 시점 찾기, 매 글자 이벤트에서 글자 개수 얻기
			int iLyricsIdx = 0;
			ArrayList<String> arrLyrics = lyricsInfo.getLyrics();
			mLyricsCharCnt.clear();
			mLyricsCharTicks.clear();
			String strCountry = lyricsInfo.getLanguage();
			String strEncode = "US-ASCII";
			if (strCountry.equals(Global.LANGCODE[Global.CHN])) {
				strEncode = "GB18030";
			} else if (strCountry.equals(Global.LANGCODE[Global.ESP])
					|| strCountry.equals(Global.LANGCODE[Global.ITA])
					|| strCountry.equals(Global.LANGCODE[Global.POR])
					|| strCountry.equals(Global.LANGCODE[Global.VIT])) {
				strEncode = "ISO-8859-15";
			} else if (strCountry.equals(Global.LANGCODE[Global.JPN])) {
				strEncode = "EUC-JP";
			} else if (strCountry.equals(Global.LANGCODE[Global.KOR])) {
				strEncode = "EUC-KR";
			} else if (strCountry.equals(Global.LANGCODE[Global.MON])
					|| strCountry.equals(Global.LANGCODE[Global.RUS])) {
				strEncode = "ISO-8859-5";
			}
			int iByteLength = 0;
			String strLyrics = "";
			int iCharCount = 0;
			int iCharIdx = 0;
			String strChar = "";
			strLyrics = arrLyrics.get(iLyricsIdx).replace(" ", "").toLowerCase();

			int nFindPos = 0;
			while (i < iEventSize && i > 0) {
				event = _lyrics.get(i - 1);

				String oneLyric = new String(event.Value, Charset.forName(strEncode)).toLowerCase();
				nFindPos = strLyrics.indexOf(oneLyric, nFindPos);
				if (nFindPos >= 0) {
					nFindPos += oneLyric.length();
				}
				else {
					do {
						if (strLyrics.indexOf("#") >= 0) {
							if (interludeStartTick == -1) {
								interludeStartTick = _lyrics.get(i - 2).StartTime + division * 4;	// 간주 시작위치
								interludeEndTick = _lyrics.get(i - 1).StartTime - division * 4;		// 간주 카운터시작위치
								tickdelay[1] = division;
							}
							else if (interludeStartTick1 == -1) {
								interludeStartTick1 = _lyrics.get(i - 2).StartTime + division * 4;	// 간주 시작위치
								interludeEndTick1 = _lyrics.get(i - 1).StartTime - division * 4;	// 간주 카운터시작위치
								tickdelay[2] = division;
							}
						}

						iLyricsIdx++;
						strLyrics = arrLyrics.get(iLyricsIdx).replace(" ", "").toLowerCase();
						nFindPos = strLyrics.indexOf(oneLyric, 0);
					} while (nFindPos < 0);
				}

				if (Global.isLyricsEvent(event)) {
					iByteLength = oneLyric.length();
					mLyricsCharCnt.add(iByteLength);
					mLyricsCharTicks.add(event.StartTime);
				}

				event = _lyrics.get(i);
				iSum = event.StartTime;
				i++;
			}

			Global.Debug("Timming Info: readyLyricsTick-" + readyLyricsTick + ", interludeStartTick-" + interludeStartTick + ", interludeEndTick-" + interludeEndTick);
			Global.Debug("Timming Info: interludeStartTick1-" + interludeStartTick1 + ", interludeEndTick1-" + interludeEndTick1);

			if (interludeStartTick == -1 || interludeEndTick == -1) {
				Global.Debug("No interlude music!");
				interludeStartTick = interludeEndTick = -1;
				interludeStartTick1 = interludeEndTick1 = -1;
				interludeLyricsIdx = interludeLyricsIdx1 = -1;
			} else {
				// 간주시작전까지 가사위치 찾기
				i = 0;
				int iLyricsSize = arrLyrics.size();
				int iWordIdx = 0;
				iLyricsIdx = 0;
				iSum = 0;
				int iCountIdx = 0;
				strLyrics = arrLyrics.get(iLyricsIdx).replace(" ", "").toLowerCase();
				while (i < iEventSize || iLyricsIdx < iLyricsSize) {
					event = _lyrics.get(i);

					if (Global.isLyricsEvent(event)) {
						// iWordIdx++;
						if (iCountIdx < mLyricsCharCnt.size()) {
							iWordIdx += mLyricsCharCnt.get(iCountIdx);
							iCountIdx++;
						} else {
							iWordIdx++;
						}
						if (iWordIdx >= strLyrics.length()) {
							iLyricsIdx++;
							if (iLyricsIdx < arrLyrics.size()) {
								strLyrics = arrLyrics.get(iLyricsIdx).replace(" ", "").toLowerCase();
								if (strLyrics.contains("#")) {
									iLyricsIdx++;
									strLyrics = arrLyrics.get(iLyricsIdx).replace(" ", "").toLowerCase();
								}
							}
							iWordIdx = 0;
						}
					}
					iSum = event.StartTime;
					if (iSum >= interludeStartTick) {
						interludeLyricsIdx = iLyricsIdx;
						if (interludeStartTick1 > 0) {
							if (iSum >= interludeStartTick1) {
								interludeLyricsIdx1 = iLyricsIdx;
								break;
							}
						}
						else
							break;
					}
					i++;
				}

				Global.Debug("InterludelyricsIdx = " + interludeLyricsIdx + ", " + interludeLyricsIdx1);
			}
		}
		else if (curSongInfo.songType == SongInfo.TYPE_KUMYONG) {
			// 4,3,2,1 시작하는 시점 찾기
			ArrayList<MidiEvent> events = lyrics.getEvents();
			int iEventSize = events.size();
			while (true) {
				if (i >= iEventSize)
					break;
				event = events.get(i);
				iSum = event.StartTime;
				i++;
				if (Global.isLyricsStartEvent(event)) {
					readyLyricsTick = iSum;		// 전주 카운터시작위치
					tickdelay[0] = (events.get(i).StartTime - readyLyricsTick) / 4;
					break;
				}
			}

			// 간주 시작/끝 시점 찾기, 매 글자 이벤트에서 글자 개수 얻기
			int iLyricsIdx = 0;
			ArrayList<String> arrLyrics = lyricsInfo.getLyrics();
			mLyricsCharCnt.clear();
			mLyricsCharTicks.clear();
			String strCountry = lyricsInfo.getLanguage();
			String strEncode = "US-ASCII";
			if (strCountry.equals(Global.LANGCODE[Global.CHN])) {
				strEncode = "GB18030";
			} else if (strCountry.equals(Global.LANGCODE[Global.ESP])
					|| strCountry.equals(Global.LANGCODE[Global.ITA])
					|| strCountry.equals(Global.LANGCODE[Global.POR])
					|| strCountry.equals(Global.LANGCODE[Global.VIT])) {
				strEncode = "ISO-8859-15";
			} else if (strCountry.equals(Global.LANGCODE[Global.JPN])) {
				strEncode = "EUC-JP";
			} else if (strCountry.equals(Global.LANGCODE[Global.KOR])) {
				strEncode = "EUC-KR";
			} else if (strCountry.equals(Global.LANGCODE[Global.MON])
					|| strCountry.equals(Global.LANGCODE[Global.RUS])) {
				strEncode = "ISO-8859-5";
			}
			int iByteLength = 0;
			String strLyrics = "";
			int iCharCount = 0;
			int iCharIdx = 0;
			int iStartIdx = 0;
			String strChar = "";
			strLyrics = arrLyrics.get(iLyricsIdx).trim();

			// 원래 제일 시간이 긴 구간을 찾아서 그것을 간주구간으로 판정하던것을
			// 미디에 있는 가사트랙에서 간주표식을 찾도록 수정함.
			// 도중에 이벤트 타입이 192이고 param1과 param2이 다 0이면 간주구간이다. 2014-11-24
			// int iMaxDTime = 0;
			while (i < iEventSize) {
				event = events.get(i - 1);

				if (Global.isLyricsMiddleEvent(event)) {
					if (interludeStartTick == -1) {
						interludeStartTick = iSum + division * 4;
						interludeEndTick = events.get(i).StartTime;
						if (i < iEventSize - 1)
							tickdelay[1] = (events.get(i + 1).StartTime - interludeEndTick) / 4;

						if (interludeStartTick > interludeEndTick) {
                            interludeStartTick = -1;
                            interludeEndTick = -1;
                        }
					}
					else if (interludeStartTick1 == -1) {
						interludeStartTick1 = iSum + division * 4;
						interludeEndTick1 = events.get(i).StartTime;
						if (i < iEventSize - 1)
							tickdelay[2] = (events.get(i + 1).StartTime - interludeEndTick1) / 4;

						if (interludeStartTick1 > interludeEndTick1) {
							interludeStartTick1 = -1;
							interludeEndTick1 = -1;
						}
					}
				}

				iCharCount = 0;
				if (Global.isLyricsEvent(event)) {
					// iByteLength = event.getParam1();
					iByteLength = 2;				// 조문한글자길이
					try {
						while (iByteLength > iCharCount) {
							if (iCharIdx >= strLyrics.length()) {
								strLyrics = arrLyrics.get(++iLyricsIdx).trim();
								iCharIdx = 0;
								iStartIdx = 0;
							}
							strChar = strLyrics.substring(iCharIdx, iCharIdx + 1);
							if (strChar.indexOf(" ") >= 0) {
								iCharIdx++;
								iStartIdx++;
								continue;
							}
							else if (strChar.matches("[A-Za-z]+")) {
								do {
									iCharIdx += 1;
									if (iCharIdx >= strLyrics.length())
										break;
									strChar = strLyrics.substring(iCharIdx, iCharIdx + 1);
								} while (strChar.matches("[A-Za-z]+"));
							}
							else {
								iCharIdx += 1;
							}
							iCharCount += 2;
						}
					} catch (Exception e) {
						iCharCount = 0;
					}
					mLyricsCharCnt.add(iCharIdx - iStartIdx);
					mLyricsCharTicks.add(event.StartTime);
					iStartIdx = iCharIdx;
				}

				event = events.get(i);
				iSum = event.StartTime;
				i++;
			}
			Global.Debug("Timming Info: readyLyricsTick-" + readyLyricsTick + ", interludeStartTick-" + interludeStartTick + ", interludeEndTick-" + interludeEndTick);
			Global.Debug("Timming Info: interludeStartTick1-" + interludeStartTick1 + ", interludeEndTick1-" + interludeEndTick1);

			if (interludeStartTick == -1 || interludeEndTick == -1) {
				Global.Debug("No interlude music!");
				interludeStartTick = interludeEndTick = -1;
				interludeLyricsIdx = -1;
				interludeStartTick1 = interludeEndTick1 = -1;
				interludeLyricsIdx1 = -1;
			} else {
				// 간주시작전까지 가사위치 찾기
				i = 0;
				int iLyricsSize = arrLyrics.size();
				int iWordIdx = 0;
				iLyricsIdx = 0;
				iSum = 0;
				int iCountIdx = 0;
				String strText = arrLyrics.get(iLyricsIdx).replace(" ", "");
				while (i < iEventSize || iLyricsIdx < iLyricsSize) {
					event = events.get(i);

					if (Global.isLyricsEvent(event)) {
						// iWordIdx++;
						if (iCountIdx < mLyricsCharCnt.size()) {
							iWordIdx += mLyricsCharCnt.get(iCountIdx);
							iCountIdx++;
						} else {
							iWordIdx++;
						}
						if (iWordIdx >= strText.length()) {
							iLyricsIdx++;
							if (iLyricsIdx < arrLyrics.size())
								strText = arrLyrics.get(iLyricsIdx).replace(" ", "");
							iWordIdx = 0;
						}
					}
					iSum = event.StartTime;
					if (iSum >= interludeStartTick) {
						interludeLyricsIdx = iLyricsIdx;
						if (interludeStartTick1 > 0) {
							if (iSum >= interludeStartTick1) {
								interludeLyricsIdx1 = iLyricsIdx;
								break;
							}
						}
						else
							break;
					}
					i++;
				}
				Global.Debug("InterludelyricsIdx = " + interludeLyricsIdx + ", " + interludeLyricsIdx1);
			}
		}

		mLyricsCharIdx = 0;
	}

	public String getMidiPath() {
		return midiPath;
	}

	public int getTrackCount() {
		if (tracks == null)
			return 0;
		return tracks.size();
	}

	public MidiTrack getMelody() {
		return melody;
	}

	public MidiTrack getLyrics() {
		return lyrics;
	}

	public int getDivision() {
		return division;
	}

	public int getTickDelay(int nticks) {
		int result = tickdelay[0];
		if (nticks >= readyLyricsTick && (interludeStartTick == -1 || nticks < interludeStartTick))
			result = tickdelay[0];
		else if (interludeStartTick != -1) {
			if (nticks >= interludeStartTick && (interludeStartTick1 == -1 || nticks < interludeStartTick1))
				result = tickdelay[1];
			else if (interludeStartTick1 != -1) {
				if (nticks >= interludeStartTick1)
					result = tickdelay[2];
			}
		}

		return result;
	}

	public int getMeasure() {
		return measure;
	}

	public double getTempo() {
		return tempo;
	}

	public void setTempo(double value) {
		tempo = value;
	}

	public double getTicks2Millseconds(int ticks) {
		double fRetMs = 0;
		int nTempo = m_midifile.tempovalues.get(0);
		int nCurTick = 0, nPrevTick = 0, nSumTick = 0;
		int nIndex = 0;

		for (nIndex = 0; nIndex < m_midifile.tempovalues.size(); nIndex++) {
			nCurTick = m_midifile.tempoticks.get(nIndex);
			if (nIndex > 0) {
				nPrevTick = m_midifile.tempoticks.get(nIndex - 1);
				nTempo = m_midifile.tempovalues.get(nIndex - 1);
			}

			double tempo = (double)nTempo;
			double ppqn = (double)division;
			double bpm = 60000000.0f / tempo;
			double ftick2ms = 1000.0f * (60.0f / (bpm * ppqn));
			if (ticks < nCurTick) {
				fRetMs += (float)(ticks - nPrevTick) * ftick2ms;
				break;
			}
			else {
				fRetMs += (float)(nCurTick - nPrevTick) * ftick2ms;
			}
		}

		if (ticks > nCurTick && nIndex > 0) {
			nTempo = m_midifile.tempovalues.get(nIndex - 1);
			double tempo = (float)nTempo;
			double ppqn = (float)division;
			double bpm = 60000000.0f / tempo;
			double ftick2ms = 1000.0f * (60.0f / (bpm * ppqn));
			fRetMs += (double)(ticks - nCurTick) * ftick2ms;
		}
		return (int)fRetMs;
	}

	public Boolean IsPlaying() {
		return MainActivity.getPlayer().IsPlaying() == 1 ? true : false;
	}

	public int getTotalTicks()
	{
		return MainActivity.getPlayer().GetTotalClocks() * 5;
	}

	public int getPortSelection()
	{
		return MainActivity.getPlayer().GetPortSelectionMethod();
	}

	public int setPortSelection( int method)
	{
		return MainActivity.getPlayer().SetPortSelectionMethod(method);
	}

	public int getCurrentTicks()
	{
		return MainActivity.getPlayer().GetCurrentClocks() * 5;
	}

	public void seekByTicks(int ticks)
	{
		Global.Debug("[MidiPlayer::seekByTicks] begin");
		playState = PLAYER_SKIPPING;
		stop();
		skippingTicks = ticks;
		MainActivity.getPlayer().Seek(skippingTicks / 5);
		skipPlay(_iLang);
		Global.Debug("[MidiPlayer::seekByTicks] end");
	}

	public int seekByTick(int ticks)
	{
		return MainActivity.getPlayer().Seek(ticks);
	}

	public void keyReset()
	{
		MainActivity.getPlayer().SetKeyControl(0);
	}

	public int keyPlus()
	{
		int key = MainActivity.getPlayer().GetKeyControl();
		if (key < 8)
			key++;
		MainActivity.getPlayer().SetKeyControl(key);
		return key;
	}

	public int keyMinus()
	{
		int key = MainActivity.getPlayer().GetKeyControl();
		if (key > -8)
			key--;
		MainActivity.getPlayer().SetKeyControl(key);
		return key;
	}

	public void speedReset()
	{
		MainActivity.getPlayer().SetSpeedControl(0);
	}

	public int speedChange(boolean isPlus)
	{
		int speed = MainActivity.getPlayer().GetSpeedControl();
		if (isPlus && speed < 8)
			speed += 2;
		if (!isPlus && speed > -8)
			speed -= 2;
		tempo = 1 + speed / 16;
		MainActivity.getPlayer().SetSpeedControl(speed);
		return speed / 2;
	}

	public LyricsInfo getLyricsInfo() {
		return lyricsInfo;
	}

	public int loadMidiFile(String spath, byte[] cdata, SongInfo songInformation) {
		midiPath = spath;
		curSongInfo = songInformation;

		Uri uri = Uri.parse("file://" + spath);
		String title = uri.getLastPathSegment();
		try {
			m_midifile = new MidiFile(cdata, title);
			tracks = m_midifile.getTracks();
			division = m_midifile.getQuarternote();
			measure = m_midifile.getTime().getMeasure();
		}
		catch (MidiFileException e)
		{
			Log.e("midi", "Failed to load midi file!");
			return -1;
		}

		Global.Debug("*** Division : " + division);

		melody = null;
		lyrics = null;
		int iMp3Track1 = -1, iMp3Track2 = -1;

		MidiTrack track = null;
		String strName = null;
		MidiEvent event = null;
		for (int i = 0; i < tracks.size(); i++) {
			track = tracks.get(i);
			if (track != null && track.trackName() != null) {
				strName = track.trackName().toLowerCase(Locale.ENGLISH).trim();

				if (curSongInfo.songType == SongInfo.TYPE_MAGICSING && strName.contains("lyric") && lyrics == null) {
					lyrics = track;
				} else if (curSongInfo.songType == SongInfo.TYPE_KUMYONG && strName.contains("$gs") && lyrics == null) {
					lyrics = track;
				} else if (strName.equals("wma singer")) {
					iMp3Track1 = track.trackNumber();
				} else if (strName.equals("wma chorus")) {
					iMp3Track2 = track.trackNumber();
				} else if (melody == null) {
					for (int j = 0; j < MELODY_NAMES.length; j++) {
						if (strName.equals(MELODY_NAMES[j])) {
							melody = track;
							break;
						}
					}
				}
			}
		}
		if (melody != null) {
			int iMelodyChannel = melody.getEvents().get(0).Channel;
			Global.Debug("MidiPlayer::loadMidi --> melody channel = " + iMelodyChannel);
		}
		return 1;
	}

	private EqualizerInfo mEquInfo = null;

	public EqualizerInfo getEqualizerInfo() {
		return mEquInfo;
	}

	public void playLyrics() {
		mParent.onPlayLyrics();
	}

	public boolean isPlayLyrics() {
		return mParent.isPlayLyrics();
	}

	public void showLyrics() {
		mParent.onSetVisibleLyrics(false);
	}

	public void hideLyrics() {
		mParent.onSetVisibleLyrics(true);
	}

	public void playTick() {
		mParent.onPlayTick();
	}

	public void playInterlude() {
		mParent.onPlayInterlude();
		changeLyricLine();
	}

	public void endInterlude() {
		mParent.onEndInterlude();
	}

	public void endLyrics() {
		mParent.onEndLyrics();
	}

	private int mReadyTime = 1000;
	private int mDeviceDelayTime = 0;

	private class OutputThread extends Thread {
		@Override
		public void run() {
			try {
				android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
				// Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
				long lStartTime = System.currentTimeMillis();
				mReadyTime = (int) (System.currentTimeMillis() - lStartTime) + mDeviceDelayTime;
				Global.Debug(">>>>>>>>>>>>>ready to play midi time is " + mReadyTime + "ms");
				// Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
			} catch (Exception e) {
				// Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
			}
		}
	}

	private OutputThread m_outThread;

	public void setLyricCharIdx(int nNewCharIdx) { mLyricsCharIdx = nNewCharIdx; }

	private int _iLang = 0;
	public int skipPlay(int iLang) {
		int iRet = 1;
		Global.Debug("[MidiPlayer::skipPlay] begin");

		kTxtThread = new KTextThread();
		if (lyrics != null) {
			if (curSongInfo.songType == SongInfo.TYPE_MAGICSING)
				kTxtThread.setEvents(lyrics.getLyrics());
			else if (curSongInfo.songType == SongInfo.TYPE_KUMYONG)
				kTxtThread.setEvents(lyrics.getEvents());
			kTxtThread.setInfo(readyLyricsTick, interludeStartTick, interludeEndTick, interludeStartTick1, interludeEndTick1, true);
		}
		kTxtThread.setLoop(true);
		kTxtThread.start();
		kTxtLayout.start(iLang);

		Global.Debug("[MidiPlayer::skipPlay] end");
		return iRet;
	}

	public int getLengthOfAnimChars()
	{
		return kTxtLayout.getCharCounts();
	}



	public int play(int iLang) {
		int iRet = 1;
		_iLang = iLang;

		if (playState == PLAYER_PAUSE) {
			return resume();
		}

		skippingTicks = 0;
		MainActivity.getPlayer().Start();
		long lStartTime = System.currentTimeMillis();
		kTxtThread = new KTextThread();
		if (lyrics != null) {
			if (curSongInfo.songType == SongInfo.TYPE_MAGICSING)
				kTxtThread.setEvents(lyrics.getLyrics());
			else if (curSongInfo.songType == SongInfo.TYPE_KUMYONG)
				kTxtThread.setEvents(lyrics.getEvents());
			Global.Debug(String.format("[Lyrics Info]: tickDelay-%d, readyLyricsTick-%d",
					tickdelay[0], readyLyricsTick));
			Global.Debug(String.format("[Lyrics Info]: tickDelay-%d, interludeStartTick-%d, interludeEndTick-%d",
					tickdelay[1], interludeStartTick, interludeEndTick));
			Global.Debug(String.format("[Lyrics Info]: tickDelay-%d, interludeStartTick1-%d, interludeEndTick1-%d",
					tickdelay[2], interludeStartTick1, interludeEndTick1));

			kTxtThread.setInfo(readyLyricsTick, interludeStartTick, interludeEndTick, interludeStartTick1, interludeEndTick1, false);
		}
		kTxtThread.setLoop(true);
		kTxtThread.start();
		kTxtLayout.start(iLang);
		Global.Debug("Playing State -> PLAYER_PLAYING");
		playState = PLAYER_PLAYING;

		return iRet;
	}

	public int pause() {
		MainActivity.getPlayer().Stop();
		while (IsPlaying()) {
			try {
				Thread.sleep(50);
			}
			catch (Exception ex) {
			}
		}
		for (int i = 0; i < mp3Players.size(); i++) {
			mp3Players.get(i).pause();
		}
		playState = PLAYER_PAUSE;
		kTxtThread.pause();
		kTxtLayout.pause();
		return 1;
	}

	public int resume() {
		MainActivity.getPlayer().Start();
		for (int i = 0; i < mp3Players.size(); i++) {
			mp3Players.get(i).start();
		}
		playState = PLAYER_PLAYING;
		kTxtThread.play();
		kTxtLayout.resume();
		return 1;
	}

	public int stop() {
		int result = -1;

		if (IsPlaying() && playState != PLAYER_SKIPPING)
			MainActivity.getPlayer().Stop();

		for (int i = 0; i < mp3Players.size(); i++) {
			mp3Players.get(i).stop();
		}
		mp3Players.clear();
		try {
			if (playState == PLAYER_PLAYING || playState == PLAYER_PAUSE) {
				playState = PLAYER_READY;
				kTxtLayout.stop();
				setLyricCharIdx(0);
				if (kTxtThread != null) {
					kTxtThread.setLoop(false);
					Thread.sleep(100);
					kTxtThread.interrupt();
					kTxtThread = null;
				}
				result = 1;
			}
			else if (playState == PLAYER_SKIPPING) {
				kTxtLayout.stop();
				setLyricCharIdx(0);
				if (kTxtThread != null) {
					kTxtThread.setLoop(false);
					Thread.sleep(100);
					kTxtThread.interrupt();
					kTxtThread = null;
				}
				result = 1;
			}
			while (IsPlaying() && playState != PLAYER_SKIPPING) {
				Thread.sleep(1);
			}
		} catch(InterruptedException e){
			e.printStackTrace();
		}
		return result;
	}

	public void nextChar(double fTime) {
		try {
			kTxtLayout.nextChar(fTime, mLyricsCharCnt.get(mLyricsCharIdx));
			mLyricsCharIdx++;
		} catch (Exception e) {
			kTxtLayout.nextChar(fTime, 1);
		}
	}

	public void changeLyricLine() {
		try {
			kTxtLayout.nextChar(-1, -1);
		} catch (Exception e) {
		}
	}

	ArrayList<MediaPlayer> mp3Players = new ArrayList<MediaPlayer>();

	MediaPlayer.OnCompletionListener mp3CompleteListener = new MediaPlayer.OnCompletionListener() {

		@Override
		public void onCompletion(MediaPlayer mp) {
			// TODO Auto-generated method stub
			Global.Debug("mp3 play complete!");
			mp3Players.remove(mp);
		}
	};
	private boolean mIsPlayVocal = false;

	public boolean isPlayVocal() {
		return mIsPlayVocal;
	}

	public void switchPlayVocal() {
		mIsPlayVocal = !mIsPlayVocal;
		float fVolume = mIsPlayVocal ? 1.0f : 0.0f;
		Global.Debug("MidiPlayer switch play vocal......volume is " + fVolume);
		for (int i = 0; i < mp3Players.size(); i++) {
			mp3Players.get(i).setVolume(fVolume, fVolume);
		}
	}

	@SuppressLint({ "HandlerLeak", "DefaultLocale" })
	Handler mMp3Handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			try {
				String strPath = String.format("%s/%sw%02d.mp3", mContext.getFilesDir(),
						lyricsInfo.getNum(), msg.what + 1);
				Global.Debug("MidiPlayer play mp3 path=" + strPath);
				MediaPlayer player = new MediaPlayer();
				player.setDataSource(strPath);
				player.setOnCompletionListener(mp3CompleteListener);
				player.setLooping(false);
				if (mIsPlayVocal) {
					Global.Debug("MidiPlayer play mp3 NO MUTE!");
					player.setVolume(1.0f, 1.0f);
				} else {
					Global.Debug("MidiPlayer play mp3 MUTE!");
					player.setVolume(0.0f, 0.0f);
				}
				player.prepare();
				player.start();
				mp3Players.add(player);
			} catch (Exception e) {
				Global.Debug("MidiPlayer play mp3 error! ex=" + e.getMessage());
			}
		}

	};

	public void playMP3(int iNum, int iType) {
		if (iType == 1 && !SetupManager.getManager().getSinger().equals("ON") || iType == 2
				&& !SetupManager.getManager().getChorus().equals("ON"))
			return;

		mMp3Handler.sendEmptyMessageDelayed(iNum, mReadyTime);
	}

	public void completed() {
		Global.Debug("complete music!");
		mParent.onCompleted();
	}

	public interface OnMidiPlayListener {

		public void onCompleted();

		public void onEndInterlude();

		public void onEndLyrics();

		public void onPlayInterlude();

		public void onPlayLyrics();

		public boolean isPlayLyrics();

		public void onPlayTick();

		public void onSetVisibleLyrics(boolean isHide);

	}
}
