package com.tbtc.jftv.text;

import java.util.ArrayList;

import com.tbtc.jftv.common.Global;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.os.Handler;

public class KLyricsEnThread extends Thread {
	//private final static int SPACE_WORD = 7;
	private final static int SPACE_STEP = 1;
	
	private int mSpaceWord = 7;
	public void setSpace(int iSpace) {
		mSpaceWord = iSpace;
		Global.Debug("LyricsEn -- setSpace -----> font = " + fontsize + ", space=" + mSpaceWord);
	}
	
	Boolean isExit = false;
	public ArrayList<Double> aniTimes;
	public ArrayList<Integer> charCounts;

	private long startTime = 0;
	private double aniTime;
	private int charCount;
	
	private ArrayList<Rect> upRects;
	private ArrayList<Rect> downRects;
	private int upRectIdx = 0;
	private int downRectIdx = 0;
	
	public Boolean isInitingUpTxt = false;
	public Boolean isInitingDownTxt = false;
		
	@SuppressWarnings("unused")
	private Boolean isAniEnd = false;
	
	public volatile Boolean isAniUp = false;
	
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

	private int currentIdx = 0;
	
	private int txtHeight = 70;
	private int fontsize = 50;
	public void setFontsize(int iValue) {
		fontsize = iValue;
		txtHeight = fontsize * 4 / 3;
		paint.setTextSize(fontsize);
		paintStroke.setTextSize(fontsize);
		paintRed.setTextSize(fontsize);
		paintRedStroke.setTextSize(fontsize);
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
		
	public KLyricsEnThread() {
		super();
		
		aniTimes = new ArrayList<Double>();
		charCounts = new ArrayList<Integer>();
		
		paint = new Paint();		
		paint.setTextSize(fontsize);
		paint.setColor(0xffffffff);
		paint.setAntiAlias(true);
		paint.setTypeface(Global.mTypeface);
		
		paintStroke = new Paint();
		paintStroke.setColor(0xff000000);
		paintStroke.setAntiAlias(true);
		paintStroke.setTypeface(Global.mTypeface);
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
		paintRedStroke.setTypeface(Global.mTypeface);
		paintRedStroke.setStyle(Paint.Style.STROKE);
		paintRedStroke.setStrokeWidth(KTextLayout.STROKE_WIDTH);
		
		cvs = new Canvas();
		cvsRed = new Canvas();

		upRects = new ArrayList<Rect>();
		downRects = new ArrayList<Rect>();
	}

	public void initUpTxt(Boolean bIsStart) {
		if(currentIdx >= lyricsCount) {
			isAniEnd = true;
			return;
		}
		String strText = lyrics.get(currentIdx);
		currentIdx++;
		new TextInitThread(strText, true, bIsStart).start();
		upRectIdx = 0;
	}
	public void initDownTxt(Boolean bIsStart) {
		if(currentIdx >= lyricsCount) {
			isAniEnd = true;
			return;
		}
		String strText = lyrics.get(currentIdx);
		currentIdx++;
		new TextInitThread(strText, false, bIsStart).start();
		downRectIdx = 0;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		currentIdx = 0;
		initUpTxt(true);
		initDownTxt(true);
		
		isAniUp = true;
		isAniEnd = false;
		
		double fSpeed;
		int iWidth = 0;
		long delay = 0;
		try {
			while(!isExit) {	
				if(aniTimes.size() < 1 || charCounts.size() < 1) {
					continue;
				}
				aniTime = aniTimes.get(0);
				aniTimes.remove(0);
				charCount = charCounts.get(0);
				charCounts.remove(0);
				startTime = System.currentTimeMillis();
				
				Rect rc;					
				
				if(isAniUp) {
					if(isInitingUpTxt || upRectIdx == upRects.size() || upRects.size() < 1) {
						continue;
					}
						
					upRectIdx += charCount - 1;
					rc = upRects.get(upRectIdx);
					iWidth = rc.left + SPACE_STEP;
					fSpeed = aniTime / rc.width() * SPACE_STEP;
					// 만일 1픽셀 증가 시간이 1.5미리초이하인 경우 단번 색칠
					if(fSpeed < 1.5f) {
						m_upHandler.sendEmptyMessage(rc.right);
						delay = (long)(aniTime - (float)(System.currentTimeMillis() - startTime) - 0.5f);
						if(delay > Global.THREAD_MINDELAY)
							Thread.sleep(delay);
					} else {
						while(iWidth <= rc.right) {
							m_upHandler.sendEmptyMessage(iWidth);
							iWidth+=SPACE_STEP;
							delay = (long)(fSpeed - (float)(System.currentTimeMillis() - startTime) - 0.5f);
							if(delay > Global.THREAD_MINDELAY)
								Thread.sleep(delay);
							startTime = System.currentTimeMillis();
						}
					}
					upRectIdx++;
				} else {
					if(isInitingDownTxt || downRectIdx == downRects.size() || downRects.size() < 1) continue;
					
					downRectIdx += charCount - 1;
					rc = downRects.get(downRectIdx);
					iWidth = rc.left + SPACE_STEP;
					fSpeed = aniTime / rc.width() * SPACE_STEP;
					if(fSpeed < 1.5f) {
						m_downHandler.sendEmptyMessage(rc.right);
						delay = (long)(aniTime - (float)(System.currentTimeMillis() - startTime) - 0.5f);
						if(delay > Global.THREAD_MINDELAY)
							Thread.sleep(delay);
					} else {
						while(iWidth <= rc.right) {
							m_downHandler.sendEmptyMessage(iWidth);
							iWidth+=SPACE_STEP;
							delay = (long)(fSpeed - (float)(System.currentTimeMillis() - startTime) - 0.5f);
							if(delay > Global.THREAD_MINDELAY)
								Thread.sleep(delay);
							startTime = System.currentTimeMillis();
						}
					}
					downRectIdx++;
				}
			}
		} catch(Exception e) {
			Global.Debug(">>>>>>>>>>>Lyrics En Thread Error! ex=" + e.getMessage());
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
		Boolean isUp, isStart;
		public TextInitThread(String strText, Boolean bisUp, Boolean bisStart) {
			super();
			text = strText;
			isUp = bisUp;
			isStart = bisStart;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(isInitingUpTxt || isInitingDownTxt) {
				// Waiting another init task end!
				try {
					sleep(10);
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
			int x = 0;
			Rect bounds = new Rect();
			String[] arrText = text.split("/");
			int iSpaceCnt = 0;
			for(int i = 0; i < arrText.length; i++) {
				strChar = arrText[i].trim();
				if(strChar.equals("")) {
					iSpaceCnt++;
					if(iSpaceCnt >= 2) {
						rcTemp = new Rect(x, 0, x + mSpaceWord, 0);
						if(isUp)
							upRects.add(rcTemp);
						else
							downRects.add(rcTemp);
						iSpaceCnt = 0;
					}
					x += mSpaceWord;
					continue;
				}
				iSpaceCnt = 0;
				
				paint.getTextBounds(strChar, 0, strChar.length(), bounds);
				cvs.drawText(strChar, x, fontsize, paintStroke);
				cvs.drawText(strChar, x, fontsize, paint);

				cvsRed.drawText(strChar, x, fontsize, paintRedStroke);
				cvsRed.drawText(strChar, x, fontsize, paintRed);

				rcTemp = new Rect(x, 0, x + bounds.width() + fontsize / 4, 0);
				if(isUp)
					upRects.add(rcTemp);
				else
					downRects.add(rcTemp);
				
				x += bounds.width() + fontsize / 4;
			}
			
			if(isUp) {
				textUpInitHandler.sendEmptyMessage(0);
			} else {
				textDownInitHandler.sendEmptyMessage(0);
			}
		}
		
	}
	
	private int getTextSize(String strText) {
		Rect bounds = new Rect();
		String strChar = "";
		int iWidth = 0;
		String[] arrText = strText.split("/");
		for(int i = 0; i < arrText.length; i++) {
			strChar = arrText[i].trim();
			
			if(strChar.equals("")) {
				iWidth += mSpaceWord;
				continue;
			}
			paint.getTextBounds(strChar, 0, strChar.length(), bounds);
			iWidth += bounds.width() + fontsize / 4;
		}
		return iWidth + fontsize / 10;
	}

}
