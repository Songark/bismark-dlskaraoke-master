package com.tbtc.jftv.text;

import java.util.ArrayList;

import com.tbtc.jftv.common.Global;
import com.sheetmusic.MidiPlayer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Bitmap.Config;
import android.os.Handler;

public class KLyricsThread extends Thread {	
	private int mSpaceWord = 10;
	private int mSpaceStep = 3;
	public void setSpace(int iWord, int iStep) {
		mSpaceWord = iWord;
		mSpaceStep = iStep;
        Global.Debug("[KLyricsThread::setSpace] fontsize-" + fontsize + ", space=" + mSpaceWord + ", step=" + mSpaceStep);
	}
	
	private KLyricsEnThread mEnThread = null;
	public void setLyricsEnThread(KLyricsEnThread enThread) {
		mEnThread = enThread;
	}

	Boolean isExit = false;
	Boolean isPause = false;

	public ArrayList<Double> aniTimes;
	public ArrayList<Integer> charCounts;

	private long startTime = 0;
	private double aniTime;
	private int charCount;
	
	private ArrayList<Rect> upRects;
	private ArrayList<Rect> downRects;
	private int upRectIdx = 0;
	private int downRectIdx = 0;

	public Boolean isProcessedUpMsg = false;
	public Boolean isProcessedDownMsg = false;
	public Boolean isInitingUpTxt = false;
	public Boolean isInitingDownTxt = false;
	public Boolean isInitedUpTxt = false;
	public Boolean isInitedDownTxt = false;
	private Boolean isAniEnd = false;
	private Boolean isAniUp = false;
	private Handler m_upHandler, m_downHandler;

	public void setHandler(Handler upHandler, Handler downHandler) {
		m_upHandler = upHandler;
		m_downHandler = downHandler;
	}
	private ArrayList<String> lyrics;
	private int lyricsCount  = 0;
	public void setLyrics(ArrayList<String> arrText) {
		lyrics = arrText;
		lyricsCount = arrText.size();
	}
	
	private int mCurrentDraw = 0;

	private int currentIdx = 0;
	
	private int txtHeight = 70;
	private int fontsize = 50;
	public void setFontsize(int iValue, int iLang) {
		fontsize = iValue;
		paint.setTextSize(fontsize);
		paintStroke.setTextSize(fontsize);
		paintRed.setTextSize(fontsize);
		paintRedStroke.setTextSize(fontsize);
		// 원래 폰트사이즈의 1.4배정도로 고정해서 가사 글자 높이를 결정하던것을 국가번호를 받아서 아랍어인 경우 1.7배로 고정함. 2014-11-03
		txtHeight = fontsize * 140 / 100;
		if(iLang == Global.ARE)
			txtHeight = fontsize * 170 / 100;
	}
	
	private Typeface typeface;
	public void setTypeface(Typeface face) {
		typeface = face;
		paint.setTypeface(typeface);
		paintStroke.setTypeface(typeface);
		paintRed.setTypeface(typeface);
		paintRedStroke.setTypeface(typeface);
	}
	
	private int redColor = 0xffff0000;
	public void setRedColor(int iColor) {
		redColor = iColor;
		paintRed.setColor(redColor);
	}

	Paint paint;
	Paint paintStroke;
	Paint paintRed;
	Paint paintRedStroke;
	Bitmap bmp;
	Bitmap bmpRed;
	public Bitmap getBitmap() {
		return bmp;
	}
	public Bitmap getBitmapRed() {
		return bmpRed;
	}
	
	private Canvas cvs = null;
	private Canvas cvsRed = null;
		
	public KLyricsThread() {
		super();
		
		aniTimes = new ArrayList<Double>();
		charCounts = new ArrayList<Integer>();
		
		paint = new Paint();		
		paint.setTextSize(fontsize);
		paint.setColor(0xffffffff);
		paint.setAntiAlias(true);
		paint.setShadowLayer(2.0f, 1.0f, 1.0f, 0xff464646);
		
		paintStroke = new Paint();
		paintStroke.setColor(0xff000000);
		paintStroke.setAntiAlias(true);
		paintStroke.setStyle(Paint.Style.STROKE);
		paintStroke.setStrokeWidth(KTextLayout.STROKE_WIDTH);
		
		paintRed = new Paint();		
		paintRed.setTextSize(fontsize);
		paintRed.setColor(0xffff0000);
		paintRed.setAntiAlias(true);
		paintRed.setTypeface(Global.mTypeface);
		
		paintRedStroke = new Paint();
		paintRedStroke.setColor(0xffffffff);
		paintRedStroke.setAntiAlias(true);
		paintRedStroke.setStyle(Paint.Style.STROKE);
		paintRedStroke.setStrokeWidth(KTextLayout.STROKE_WIDTH);
		
		cvs = new Canvas();
		cvsRed = new Canvas();

		upRects = new ArrayList<Rect>();
		downRects = new ArrayList<Rect>();
	}	

	public void initUpDownTxt() {
		if(isAniUp) {
			initDownTxt(false, false);
			if(mEnThread != null) mEnThread.initDownTxt(false);
			isAniUp = false;
		}
		else {
			initUpTxt(false, false);
			if(mEnThread != null) mEnThread.initUpTxt(false);
			isAniUp = true;
		}
	}

	private void initUpTxt(Boolean bIsStart, Boolean bAsync) {
		Global.Debug("lyrics thread init up...cur idx=" + currentIdx);
		if(currentIdx >= lyricsCount) {
			isAniEnd = true;
			return;
		}
		String strText = lyrics.get(currentIdx);
		currentIdx++;
		isProcessedUpMsg = false;
		TextInitThread txtInitThread = new TextInitThread(strText, true, bIsStart);
		txtInitThread.start();
		while (bAsync) {
			if (txtInitThread.isEnd && isProcessedUpMsg == true)
				break;
			try {
				Thread.sleep(1);
			}
			catch (Exception ex) {
			}
		}
		upRectIdx = 0;
	}

	private void initDownTxt(Boolean bIsStart, Boolean bAsync) {
		Global.Debug("lyrics thread init down...cur idx=" + currentIdx);
		if(currentIdx >= lyricsCount) {
			isAniEnd = true;
			return;
		}
		String strText = lyrics.get(currentIdx);
		currentIdx++;
		isProcessedDownMsg = false;
		TextInitThread txtInitThread = new TextInitThread(strText, false, bIsStart);
		txtInitThread.start();
		while (bAsync) {
			if (txtInitThread.isEnd && isProcessedDownMsg == true)
				break;
			try {
				Thread.sleep(1);
			}
			catch (Exception ex) {
			}
		}
		downRectIdx = 0;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY);
		
		currentIdx = 0;
		initUpTxt(true, true);
		initDownTxt(true, true);
		Global.Debug("[KLyricsThread::run] upRects-" + upRects.size() + ", downRects-" + downRects.size());

		isAniUp = true;
		isAniEnd = false;

		// int iInterludeLyricsIdx = MidiPlayer.getPlayer().getInterludeLyricsIndex();
		int iInterludeLyricsIdx = -1;
		
		double fSpeed;
		int iWidth = 0;
		long delay = 0;
		int iOldUpIdx = 0;
		int iOldDownIdx = 0;
		try {
			while(!isExit) {
				if (isPause) {
					Thread.sleep(Global.THREAD_MINDELAY);
					continue;
				}

				if (aniTimes.size() < 1 || aniTimes.get(0) == null)
					continue;
				if (charCounts.size() < 1 || charCounts.get(0) == null)
					continue;
				if (upRects.size() < 1 || downRects.size() < 1)
					continue;

				aniTime = aniTimes.get(0);
				if (aniTime == -1) {
					currentIdx--;
					initUpTxt(true, true);
					initDownTxt(true, true);
					isAniUp = true;
					isAniEnd = false;
					aniTimes.remove(0);
					charCounts.remove(0);
					Global.Debug("Changed Lyric Lines");
					continue;
				}

				startTime = System.currentTimeMillis();

				Rect rc;		
				if (isAniUp) {
					if(isInitingUpTxt) continue;

					try {
						aniTime = aniTimes.get(0);
						aniTimes.remove(0);
						charCount = charCounts.get(0);
						charCounts.remove(0);
						Global.Debug("Removing count-" + aniTimes.size());
					}
					catch (Exception ex)
					{
						Global.Debug("aniTime & charCount Error");
						continue;
					}

					iOldUpIdx = upRectIdx;
					if(iOldUpIdx == 0 && !isInitedDownTxt && currentIdx != iInterludeLyricsIdx) {
						Global.Debug("Calling initDownTxt: count-" + aniTimes.size());
						initDownTxt(false, aniTime > 0 ? false : true);
						if(mEnThread != null) mEnThread.initDownTxt(false);
					}

					for (int iCharCount = 0; iCharCount < charCount; iCharCount++) {
						if (upRectIdx >= upRects.size())
							break;

						rc = upRects.get(upRectIdx);
						iWidth = rc.left + mSpaceStep;
						int nSteps = rc.width() / mSpaceStep;
						fSpeed = aniTime / charCount / nSteps;

						// 만일 1픽셀 증가 시간이 1.5미리초이하인 경우 단번 색칠
						if(fSpeed < 1.0f) {
							m_upHandler.sendEmptyMessage(rc.right);
							delay = (long)(aniTime / charCount - (float)(System.currentTimeMillis() - startTime) - 0.5f);
							if(delay > Global.THREAD_MINDELAY)
								Thread.sleep(delay);
						} else {
							startTime = System.currentTimeMillis();
							int nStepIndex = 0;
							long nDeltaTime = 0;
							// Global.Debug("Drawing Up Begin: steps-" + nSteps + ", fSpeed-" + fSpeed + ", startTime-" + startTime + ", beginWidth-" + iWidth);
							while(iWidth <= rc.right && !isExit) {
								nDeltaTime = System.currentTimeMillis() - startTime;
								if (nDeltaTime >= nStepIndex * fSpeed) {
									// Global.Debug("\tDrawing : curTime-" + System.currentTimeMillis() + ", setWidth-" + iWidth);
									m_upHandler.sendEmptyMessage(iWidth);
									nStepIndex++;
									iWidth += mSpaceStep;
								}
								Thread.sleep(Global.THREAD_MINDELAY);
							}
							// Global.Debug("Drawing Up End: curTime-" + System.currentTimeMillis() + ", endWidth" + iWidth);
						}
						upRectIdx++;
						upRectIdx = upRectIdx >= upRects.size() ? upRects.size() : upRectIdx;
					}

					if(upRectIdx == upRects.size()) {
						if(isAniEnd) {
							break;						
						}
						isAniUp = false;
						if(mEnThread != null) mEnThread.isAniUp = false;
						isInitedUpTxt = false;
					}
				} else {
					if(isInitingDownTxt) continue;

					try {
						aniTime = aniTimes.get(0);
						aniTimes.remove(0);
						charCount = charCounts.get(0);
						charCounts.remove(0);
						Global.Debug("Removing count-" + aniTimes.size());
					}
					catch (Exception ex)
					{
						Global.Debug("aniTime & charCount Error");
						continue;
					}

					iOldDownIdx = downRectIdx;
					if(iOldDownIdx == 0 && !isInitedUpTxt && currentIdx != iInterludeLyricsIdx) {
						Global.Debug("Calling initUpTxt: count-" + aniTimes.size());
						initUpTxt(false, aniTime > 0 ? false : true);
						if(mEnThread != null) mEnThread.initUpTxt(false);
					}

					for (int iCharCount = 0; iCharCount < charCount; iCharCount++) {
						if (downRectIdx >= downRects.size())
							break;

						rc = downRects.get(downRectIdx);
						iWidth = rc.left + mSpaceStep;
						int nSteps = rc.width() / mSpaceStep;
						fSpeed = aniTime / charCount / nSteps;


						if(fSpeed < 1.0f) {
							m_downHandler.sendEmptyMessage(rc.right);
							delay = (long)(aniTime / charCount - (float)(System.currentTimeMillis() - startTime) - 0.5f);
							if(delay > Global.THREAD_MINDELAY)
								Thread.sleep(delay);
						} else {
							startTime = System.currentTimeMillis();
							int nStepIndex = 0;
							long nDeltaTime = 0;
							// Global.Debug("Drawing Down Begin: steps-" + nSteps + ", fSpeed-" + fSpeed + ", startTime-" + startTime + ", beginWidth-" + iWidth);
							while(iWidth <= rc.right && !isExit) {
								nDeltaTime = System.currentTimeMillis() - startTime;
								if (nDeltaTime >= nStepIndex * fSpeed) {
									// Global.Debug("\tDrawing : curTime-" + System.currentTimeMillis() + ", setWidth-" + iWidth);
									m_downHandler.sendEmptyMessage(iWidth);
									nStepIndex++;
									iWidth += mSpaceStep;
								}
								Thread.sleep(Global.THREAD_MINDELAY);
							}
							// Global.Debug("Drawing Down End: curTime-" + System.currentTimeMillis() + ", endWidth" + iWidth);
						}
						downRectIdx ++;
						downRectIdx = downRectIdx >= downRects.size() ? downRects.size() : downRectIdx;
					}

					if(downRectIdx == downRects.size()) {
						if(isAniEnd) {
							break;
						}
						isAniUp = true;
						if(mEnThread != null) mEnThread.isAniUp = true;
						isInitedDownTxt = false;
					}
				}
			}
		} catch(Exception e) {
			Global.Debug(">>>>>>>>>>>Lyrics Thread Error! ex=" + e.getMessage());
			Global.Debug(">>> position=" + upRectIdx +  ", " + downRectIdx);
			e.printStackTrace();
		}
	}
	
	private Handler textUpInitHandler, textDownInitHandler;
	public void setInitHandler(Handler upHandler, Handler downHandler) {
		textUpInitHandler = upHandler;
		textDownInitHandler = downHandler;
	}
		
	public class TextInitThread extends Thread {
		String text;
		Boolean isUp, isStart, isEnd;
		public TextInitThread(String strText, Boolean bisUp, Boolean bisStart) {
			super();
			text = strText;
			isUp = bisUp;
			isStart = bisStart;
			isEnd = false;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY);

			while(isInitingUpTxt || isInitingDownTxt) {
				// Waiting another init task end!
				try {
					sleep(1);
				} catch(Exception e){}
			}

			isInitingUpTxt = isUp;
			isInitingDownTxt = !isUp;

			if(isUp) {
				upRects.clear();
			} else {
				downRects.clear();
			}
			
			int iWidth = getTextSize(text);
			bmp = Bitmap.createBitmap(iWidth, txtHeight, Config.ARGB_8888);
			bmpRed = Bitmap.createBitmap(iWidth, txtHeight, Config.ARGB_8888);
			cvs.setBitmap(bmp);
			cvsRed.setBitmap(bmpRed);
								
			Rect rcTemp;
			String strChar;
			int x = fontsize / 8;
			Rect bounds = new Rect();
			
			if(mCurrentDraw == KTextLayout.DRAW_LEFT) {
				for(int i = 0; i < text.length(); i++) {
					strChar = text.substring(i, i + 1);
					if(strChar.equals(" ")) {
						x += mSpaceWord;
						continue;
					}
					
					paint.getTextBounds(strChar, 0, strChar.length(), bounds);
					cvs.drawText(strChar, x, fontsize, paintStroke);
					cvs.drawText(strChar, x, fontsize, paint);
					
					cvsRed.drawText(strChar, x, fontsize, paintRedStroke);
					cvsRed.drawText(strChar, x, fontsize, paintRed);
	
					rcTemp = new Rect(x, 0, x + bounds.width() + fontsize / 4, 0);
					if(isUp) {
						upRects.add(rcTemp);
					}
					else {
						downRects.add(rcTemp);
					}
					
					x += bounds.width() + fontsize / 4;
				}
			} else {
				int iCharWidth = 0;
				for(int i = text.length() - 1; i >= 0; i--) {
					strChar = text.substring(i, i + 1);
					if(strChar.equals(" ")) {
						x += mSpaceWord;
						continue;
					}

					paint.getTextBounds(strChar, 0, strChar.length(), bounds);
					iCharWidth = bounds.width() + fontsize / 4;

					cvs.drawText(strChar, iWidth - x - iCharWidth, fontsize, paintStroke);
					cvs.drawText(strChar, iWidth - x - iCharWidth, fontsize, paint);
					
					cvsRed.drawText(strChar, iWidth - x - iCharWidth, fontsize, paintRedStroke);
					cvsRed.drawText(strChar, iWidth - x - iCharWidth, fontsize, paintRed);
	
					rcTemp = new Rect(x, 0, x + iCharWidth, 0);
					if(isUp)
						upRects.add(rcTemp);
					else
						downRects.add(rcTemp);
					
					x += iCharWidth;
				}
			}

			if(isUp) {
				textUpInitHandler.sendEmptyMessage(0);
			} else {
				textDownInitHandler.sendEmptyMessage(0);
			}

			Global.Debug("TextInitThread exit text-" + text);
			isEnd = true;
		}
	}
	
	private int getTextSize(String strText) {
		Rect bounds = new Rect();
		String strChar = "";
		int iWidth = 0;
		for(int i = 0; i < strText.length(); i++) {
			strChar = strText.substring(i, i + 1);
			
			if(strChar.equals(" ")) {
				iWidth += mSpaceWord;
				continue;
			}
			paint.getTextBounds(strChar, 0, strChar.length(), bounds);
			iWidth += bounds.width() + fontsize / 4;
		}
		return iWidth + fontsize / 10;
	}
	public void setDrawDirection(int drawDirection) {
		mCurrentDraw = drawDirection;
	}
}
