package com.tbtc.jftv.text;

import java.nio.charset.Charset;
import java.util.ArrayList;

import com.tbtc.jftv.common.Global;
import com.sheetmusic.MidiEvent;
import com.sheetmusic.MidiPlayer;

import jp.bismark.bssynth.sample.MainActivity;

import static com.sheetmusic.MidiPlayer.PLAYER_PLAYING;

public class KTextThread extends Thread {
	public void setDeltaTime(double fTime) {
		Global.Debug("KTextThread::setDeltaTime --> time = " + fTime + ", cur time=" + curMSec + " msec, cur tick=" + curTick);
		startMSec = curMSec;
	}
	public void setDeltaTime(double fTime, int iCurTime, int iCurTick) {
		Global.Debug("KTextThread::setDeltaTime1 --> time = " + fTime + ", cur time=" + curMSec + " msec, cur tick=" + curTick);
		curMSec = iCurTime;
		curTick = iCurTick + division / 16;
		startMSec = curMSec;
	}
	private int readyLyricsTick = 0;
	private ArrayList<MidiEvent> events;
	private int eventSize;
	private int interludeStartTick;
	private int interludeEndTick;
	private int interludeStartTick1;
	private int interludeEndTick1;
	private int interludeStatus; // 0: Not started, 1: playing, 2: ended
	public Boolean bIsSkip;

	private static final int INTERLUDE_NOTSTARTED = 0;
	private static final int INTERLUDE_PLAYING = 1;
	private static final int INTERLUDE_ENDED = 2;
	private static final int INTERLUDE_PLAYING1 = 3;
	private static final int INTERLUDE_ENDED1 = 4;
	
	public int getInterludeStatus() {
		return interludeStatus;
	}
	public int getInterludeEndTick() {
		return interludeEndTick;
	}
	
	public void setEvents(ArrayList<MidiEvent> arrEvent) {
		events = arrEvent;
		eventSize = events.size();			
	}
	public void setInfo(int iReadyLyricsTick, int iInterludeStart, int iInterludeEnd, int iInterludeStart1, int iInterludeEnd1, boolean isSkip) {
		readyLyricsTick = iReadyLyricsTick;
		interludeStartTick = iInterludeStart;
		interludeEndTick = iInterludeEnd;
		interludeStartTick1 = iInterludeStart1;
		interludeEndTick1 = iInterludeEnd1;
		if(interludeStartTick < 0 || interludeEndTick < 0) {
			interludeStatus = INTERLUDE_ENDED1;
		}
		bIsSkip = isSkip;
	}
	
	private Boolean isLoop;
	public void setLoop(Boolean bLoop) {
		isLoop = bLoop;
	}
	
	private int status = 0;
	private static final int STATUS_PAUSE = 1;
	private static final int STATUS_PLAY = 2;
	private static final int STATUS_JUMPINTERLUDE = 3;

	public void pause() {
	    status = STATUS_PAUSE;
	}

	public void play() {
		status = STATUS_PLAY;
	}
	
	private int division;

	public KTextThread() {
		super();
		// TODO Auto-generated constructor stub
		division = MidiPlayer.getPlayer().getDivision();
		currentIdx = 0;
		isLoop = true;
		interludeStartTick = interludeEndTick = 0;
		interludeStartTick1 = interludeEndTick1 = 0;
		interludeStatus = INTERLUDE_NOTSTARTED;
	}

	public int currentIdx;
	public long startMSec = 0;
	public int curMSec = 0;
	public int curTick = 0;
	public int markTickCount = 4;

	public void jumpInterlude() {
		if(interludeStatus >= INTERLUDE_PLAYING)
			return;
		Global.Debug("jump interlude!!!------------------");
		status = STATUS_JUMPINTERLUDE;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		MidiEvent event;
		Boolean bIsEnd = false;
		int skippingTicks = MidiPlayer.getPlayer().skippingTicks;
		Global.Debug("KTextThread Begin");


		while (isLoop) {
			try {
				curTick = MidiPlayer.getPlayer().getCurrentTicks();

				if(status == STATUS_PAUSE) {
					Thread.sleep(Global.THREAD_MINDELAY);
					continue;
				}

				if (curTick > skippingTicks)
					processInterludes(curTick);

				// Process Lyric Sync Information
				for(;;) {
					if(currentIdx >= eventSize)  {
						bIsEnd = true;
						break;
					}

					if (!isLoop)
						break;

					event = events.get(currentIdx);
					if (Global.isLyricsEvent(event)) {
						if(event.StartTime >= curTick) {
							break;
						}
						if (bIsSkip && event.StartTime < skippingTicks) {
							processInterludes(event.StartTime);
							sendMidiEvent(event, (currentIdx < eventSize - 1) ? events.get(currentIdx + 1) : null, true);
						}
						else {
							sendMidiEvent(event, (currentIdx < eventSize - 1) ? events.get(currentIdx + 1) : null, false);
							bIsSkip = false;
						}
					}
					currentIdx++;
				}

				if(bIsEnd) {
					Global.Debug("bIsEnd True");
					break;
				}

				Thread.sleep(Global.THREAD_MINDELAY);
				
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		Global.Debug("KTextThread End: isLoop-" + isLoop + ", currentIdx-" + currentIdx + ", eventSize-" + eventSize);
	}

	private void processInterludes(int curTick)
	{
		int tickdelay = MidiPlayer.getPlayer().getTickDelay(curTick);

		// Check Interlude Status
		if(interludeStatus == INTERLUDE_NOTSTARTED && curTick >= interludeStartTick) {
		interludeStatus = INTERLUDE_PLAYING;
		Global.Debug("Interlude start! ------------------");
		MidiPlayer.getPlayer().playInterlude();
	}
		if(interludeStatus == INTERLUDE_PLAYING && curTick >= interludeEndTick - 480) {
			interludeStatus = INTERLUDE_ENDED;
			Global.Debug("Interlude end! ------------------");
			MidiPlayer.getPlayer().endInterlude();
			markTickCount = 9;
		}
		if(interludeStatus == INTERLUDE_ENDED && curTick >= interludeStartTick1 && interludeStartTick1 > 0) {
			interludeStatus = INTERLUDE_PLAYING1;
			Global.Debug("Interlude start again! ------------------");
			MidiPlayer.getPlayer().playInterlude();
		}
		if(interludeStatus == INTERLUDE_PLAYING1 && curTick >= interludeEndTick1 - 480 && interludeEndTick1 > 0) {
			interludeStatus = INTERLUDE_ENDED1;
			Global.Debug("Interlude end again! ------------------");
			MidiPlayer.getPlayer().endInterlude();
			markTickCount = 14;
		}

		if(markTickCount >= 0) {
			if(markTickCount == 4 && curTick >= readyLyricsTick) {
				MidiPlayer.getPlayer().playLyrics();
				markTickCount--;
			} else if(markTickCount == 3 && curTick >= readyLyricsTick + tickdelay * 1 * MidiPlayer.getPlayer().getTempo()) {
				MidiPlayer.getPlayer().playTick();
				markTickCount--;
			} else if(markTickCount == 2 && curTick >= readyLyricsTick + tickdelay * 2 * MidiPlayer.getPlayer().getTempo()) {
				MidiPlayer.getPlayer().playTick();
				markTickCount--;
			} else if(markTickCount == 1 && curTick >= readyLyricsTick + tickdelay * 3 * MidiPlayer.getPlayer().getTempo()) {
				MidiPlayer.getPlayer().playTick();
				markTickCount--;
			} else if(markTickCount == 0 && curTick >= readyLyricsTick + tickdelay * 4 * MidiPlayer.getPlayer().getTempo()) {
				MidiPlayer.getPlayer().playTick();
				markTickCount = 9;
			}
		}

		if(markTickCount >= 5) {
			if(markTickCount == 9 && curTick >= interludeEndTick) {
				MidiPlayer.getPlayer().playLyrics();
				markTickCount--;
			} else if(markTickCount == 8 && curTick >= interludeEndTick + tickdelay * 1 * MidiPlayer.getPlayer().getTempo()) {
				MidiPlayer.getPlayer().playTick();
				markTickCount--;
			} else if(markTickCount == 7 && curTick >= interludeEndTick + tickdelay * 2 * MidiPlayer.getPlayer().getTempo()) {
				MidiPlayer.getPlayer().playTick();
				markTickCount--;
			} else if(markTickCount == 6 && curTick >= interludeEndTick + tickdelay * 3 * MidiPlayer.getPlayer().getTempo()) {
				MidiPlayer.getPlayer().playTick();
				markTickCount--;
			} else if(markTickCount == 5 && curTick >= interludeEndTick + tickdelay * 4 * MidiPlayer.getPlayer().getTempo()) {
				MidiPlayer.getPlayer().playTick();
				markTickCount = 14;
			}
		}

		if(markTickCount >= 10) {
			if(markTickCount == 14 && curTick >= interludeEndTick1) {
				MidiPlayer.getPlayer().playLyrics();
				markTickCount--;
			} else if(markTickCount == 13 && curTick >= interludeEndTick1 + tickdelay * 1 * MidiPlayer.getPlayer().getTempo()) {
				MidiPlayer.getPlayer().playTick();
				markTickCount--;
			} else if(markTickCount == 12 && curTick >= interludeEndTick1 + tickdelay * 2 * MidiPlayer.getPlayer().getTempo()) {
				MidiPlayer.getPlayer().playTick();
				markTickCount--;
			} else if(markTickCount == 11 && curTick >= interludeEndTick1 + tickdelay * 3 * MidiPlayer.getPlayer().getTempo()) {
				MidiPlayer.getPlayer().playTick();
				markTickCount--;
			} else if(markTickCount == 10 && curTick >= interludeEndTick1 + tickdelay * 4 * MidiPlayer.getPlayer().getTempo()) {
				MidiPlayer.getPlayer().playTick();
				markTickCount = -1;
			}
		}
	}

	private void sendMidiEvent(MidiEvent curEvent, MidiEvent nextEvent, boolean isSkip) {
		if(Global.isLyricsEvent(curEvent)) {
			double fTxtTime = 1.0f;
			if (nextEvent != null) {
				if (isSkip) {
					fTxtTime = 0.0f;
				}
				else {
					double dblTempo = MidiPlayer.getPlayer().getTempo();
					if (curEvent.StartTime < interludeStartTick && nextEvent.StartTime > interludeStartTick) {
						fTxtTime = MidiPlayer.getPlayer().getTicks2Millseconds(interludeStartTick) - MidiPlayer.getPlayer().getTicks2Millseconds(curEvent.StartTime);
					}
					else if (curEvent.StartTime < interludeStartTick1 && nextEvent.StartTime > interludeStartTick1) {
						fTxtTime = MidiPlayer.getPlayer().getTicks2Millseconds(interludeStartTick1) - MidiPlayer.getPlayer().getTicks2Millseconds(curEvent.StartTime);
					}
					else {
						fTxtTime = MidiPlayer.getPlayer().getTicks2Millseconds(nextEvent.StartTime) - MidiPlayer.getPlayer().getTicks2Millseconds(curEvent.StartTime);
					}
					fTxtTime *= dblTempo;
				}
			}
			Global.Debug("[KTextThread::sendMidiEvent] fTxtTime-" + fTxtTime + ", eventStarttime-" + curEvent.StartTime +
					", count-" + MidiPlayer.getPlayer().getLengthOfAnimChars());
			MidiPlayer.getPlayer().nextChar(fTxtTime);
		}
	}

}
