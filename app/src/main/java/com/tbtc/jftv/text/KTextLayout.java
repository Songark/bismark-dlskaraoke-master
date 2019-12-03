package com.tbtc.jftv.text;

import java.util.ArrayList;

import com.karaoke.R;
import com.tbtc.jftv.common.Global;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

@SuppressLint("HandlerLeak")
public class KTextLayout extends LinearLayout {	
	public final static int STROKE_WIDTH = 8;
	
	public final static int DRAW_LEFT = 0;
	public final static int DRAW_RIGHT = 1;
	
	ImageView imgUpTxt;
	ImageView imgUpTxtRed;
	ImageView imgDownTxt;
	ImageView imgDownTxtRed;
	
	ImageView imgUpEnTxt;
	ImageView imgUpEnTxtRed;
	ImageView imgDownEnTxt;
	ImageView imgDownEnTxtRed;
	
	RelativeLayout rltUpEn, rltUp;
	RelativeLayout rltDownEn, rltDown;
	
	private KLyricsThread m_lThread = null;
	private KLyricsEnThread m_lThreadEn = null;
		
	private ArrayList<String> lyrics = new ArrayList<String>();
	private ArrayList<String> lyricsEn = null;
	private int lyricsCount  = 0;
	public void setLyrics(ArrayList<String> arrText, ArrayList<String> arrTextEn) {
		lyrics = (ArrayList<String>)arrText.clone();
		lyricsEn = null;
		if(arrTextEn != null && arrTextEn.size() == arrText.size()) {
			lyricsEn = new ArrayList<String>();
			lyricsEn = (ArrayList<String>)arrTextEn.clone();
		}

		int i = 0;
		do {
			if (i < lyrics.size()) {
				if (lyrics.get(i).indexOf("#") >= 0 || lyrics.get(i).indexOf("@") >= 0 || lyrics.get(i).indexOf("->") >= 0 ) {
					lyrics.remove(i);
					if (lyricsEn != null)
						lyricsEn.remove(i);
				}
				else
					i++;
			}
		} while (i < lyrics.size());
		lyricsCount = lyrics.size();
	}
	private Typeface typeface;
	public void setTypeface(Typeface face) {
		typeface = face;
	}
	private int fontsize, fontsizeEn;
	public void setFontsize(int iValue) {
		fontsize = iValue;
		fontsizeEn = iValue * 3 / 4;
	}
	private int redColor = 0xffff0000;
	public void setRedColor(int iColor) {
		redColor = iColor;
	}
	
	private boolean mRubyOnOff = true;
	public boolean getRubyOnOff() {
		return mRubyOnOff;
	}
	public void setRubyOnOff(boolean bIsOn) {
		mRubyOnOff = bIsOn;
		int iStatus = mRubyOnOff ? View.VISIBLE : View.INVISIBLE;
		
		imgUpEnTxt.setVisibility(iStatus);
		imgUpEnTxtRed.setVisibility(iStatus);
		imgDownEnTxt.setVisibility(iStatus);
		imgDownEnTxtRed.setVisibility(iStatus);
	}
	public void setShadow(boolean bIsOn) {
		int iColor = bIsOn ? 0x7f000000 : 0x00000000;
		rltUp.setBackgroundColor(iColor);
		rltUpEn.setBackgroundColor(iColor);
		rltDown.setBackgroundColor(iColor);
		rltDownEn.setBackgroundColor(iColor);
	}

	public KTextLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public KTextLayout(Context context) {
		super(context);
		init();
	}
	
	private void init() {
		LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.layout_ktext, this, true);
		
		imgUpTxt = (ImageView)findViewById(R.id.imgUpTxt);
		imgUpTxtRed = (ImageView)findViewById(R.id.imgUpTxtRed);
		imgDownTxt = (ImageView)findViewById(R.id.imgDownTxt);
		imgDownTxtRed = (ImageView)findViewById(R.id.imgDownTxtRed);

		imgUpEnTxt = (ImageView)findViewById(R.id.imgUpEnTxt);
		imgUpEnTxtRed = (ImageView)findViewById(R.id.imgUpEnTxtRed);
		imgDownEnTxt = (ImageView)findViewById(R.id.imgDownEnTxt);
		imgDownEnTxtRed = (ImageView)findViewById(R.id.imgDownEnTxtRed);
		
		rltUpEn = (RelativeLayout)findViewById(R.id.rltUpEn);
		rltDownEn = (RelativeLayout)findViewById(R.id.rltDownEn);	
		rltUp = (RelativeLayout)findViewById(R.id.rltUp);
		rltDown = (RelativeLayout)findViewById(R.id.rltDown);	
	}
		
	private Handler m_upHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			// Global.Debug("m_upHandler handleMessage: width-" + msg.what);
			setImagViewWidth(imgUpTxtRed, msg.what);
		}		
	};
	private Handler m_downHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			// Global.Debug("m_downHandler handleMessage: width-" + msg.what);
			setImagViewWidth(imgDownTxtRed, msg.what);
		}		
	};
	private Handler m_upEnHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(m_lThreadEn != null && m_lThreadEn.isAniUp) {
				setImagViewWidth(imgUpEnTxtRed, msg.what);
			}
		}		
	};
	private Handler m_downEnHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(m_lThreadEn != null && !m_lThreadEn.isAniUp) {
				setImagViewWidth(imgDownEnTxtRed, msg.what);
			}
		}		
	};
			
	private boolean mIsRunningRuby = false;
	public boolean isRunningRuby() {
		return mIsRunningRuby;
	}

	public void start(int iLang) {
		stop();
		
		// 아랍어인 경우 오른쪽에서 왼쪽으로 글자가 흐르도록 설정
		setDrawDirection((iLang == Global.ARE) ? DRAW_RIGHT : DRAW_LEFT);
		
		m_lThread = new KLyricsThread();
		m_lThread.setSpace(getResources().getDimensionPixelSize(R.dimen.space_small), 1);
		m_lThread.setDrawDirection(mCurrentDraw);
		m_lThread.setFontsize(fontsize, iLang);
		m_lThread.setRedColor(redColor);
		Global.Debug(">>>>>>>>>>KTextLayout>>>>>>>>typeface is null ? " + (typeface == null ? "true" : "false"));
		m_lThread.setTypeface(typeface);
		m_lThread.setLyrics(lyrics);
		m_lThread.setHandler(m_upHandler, m_downHandler);// 너비조절을 위한 조종기설정
		m_lThread.setInitHandler(textUpInitHandler, textDownInitHandler);	// 초기가사그림을 갱신하기 위한 조종기설정
		m_lThread.start();
		mIsRunningRuby = (lyricsEn != null);
				
		if(mIsRunningRuby) {
			m_lThreadEn = new KLyricsEnThread();
			m_lThreadEn.setFontsize(fontsizeEn);
			m_lThreadEn.setSpace(iLang == Global.JPN ? fontsizeEn * 3 / 4 : 5);
			m_lThreadEn.setRedColor(redColor);
			m_lThreadEn.setLyrics(lyricsEn);
			m_lThreadEn.setHandler(m_upEnHandler, m_downEnHandler);
			m_lThreadEn.setInitHandler(textUpEnInitHandler, textDownEnInitHandler);			
			m_lThreadEn.start();			
		}
		m_lThread.setLyricsEnThread(m_lThreadEn);
	}

	public void pause() {
		m_lThread.isPause = true;
	}

	public void resume() {
		m_lThread.isPause = false;
	}

	public void stop() {
		if(m_lThread != null) {
			m_lThread.isExit = true;
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			m_lThread.interrupt();
			m_lThread = null;
		}
		if(m_lThreadEn != null) {
			m_lThreadEn.isExit = true;
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			m_lThreadEn.interrupt();
			m_lThreadEn = null;			
		}
		imgUpTxt.setImageBitmap(null);
		imgUpTxtRed.setImageBitmap(null);
		imgDownTxt.setImageBitmap(null);
		imgDownTxtRed.setImageBitmap(null);
		imgUpEnTxt.setImageBitmap(null);
		imgUpEnTxtRed.setImageBitmap(null);
		imgDownEnTxt.setImageBitmap(null);
		imgDownEnTxtRed.setImageBitmap(null);
	}

	public int getCharCounts() {
		if (m_lThread != null) {
			return m_lThread.aniTimes.size();
		}
		return 0;
	}

	public void nextChar(double fTime, int iCharCount) {
		if(m_lThreadEn != null) {
			m_lThreadEn.aniTimes.add(fTime);
			m_lThreadEn.charCounts.add(iCharCount);
		}
		
		if (m_lThread != null) {
			m_lThread.aniTimes.add(fTime);
			m_lThread.charCounts.add(iCharCount);
		}
	}
	
	public void initLyrics() {
		if (m_lThread != null) {
			m_lThread.initUpDownTxt();
		}
	}

	public void resetThreadChars()
	{
		if(m_lThreadEn != null) {
			m_lThreadEn.aniTimes.clear();
			m_lThreadEn.charCounts.clear();
		}

		if (m_lThread != null) {
			m_lThread.aniTimes.clear();
			m_lThread.charCounts.clear();
		}
	}

	private Handler textUpInitHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(m_lThread == null) return;
			imgUpTxt.setImageBitmap(m_lThread.getBitmap());
			imgUpTxtRed.setImageBitmap(m_lThread.getBitmapRed());
			
			m_lThread.isInitingUpTxt = false;
			m_lThread.isInitedUpTxt = true;
			m_lThread.isProcessedMessage = true;
			// Global.Debug("[KTextLayout::textUpInitHandler] isInitingUpTxt = false, isInitedUpTxt = true");
			setImagViewWidth(imgUpTxtRed, 0);
		}		
	};

	private Handler textDownInitHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(m_lThread == null) return;
			imgDownTxt.setImageBitmap(m_lThread.getBitmap());
			imgDownTxtRed.setImageBitmap(m_lThread.getBitmapRed());
			
			m_lThread.isInitingDownTxt = false;
			m_lThread.isInitedDownTxt = true;
			m_lThread.isProcessedMessage = true;

			// Global.Debug("[KTextLayout::textDownInitHandler] isInitingDownTxt = false, isInitedDownTxt = true");
			setImagViewWidth(imgDownTxtRed, 0);
		}		
	};
	private Handler textUpEnInitHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(m_lThreadEn == null) return;
			imgUpEnTxt.setImageBitmap(m_lThreadEn.getBitmap());
			imgUpEnTxtRed.setImageBitmap(m_lThreadEn.getBitmapRed());
			
			m_lThreadEn.isInitingUpTxt = false;
			setImagViewWidth(imgUpEnTxtRed, 0);
		}		
	};
	private Handler textDownEnInitHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(m_lThreadEn == null) return;
			imgDownEnTxt.setImageBitmap(m_lThreadEn.getBitmap());
			imgDownEnTxtRed.setImageBitmap(m_lThreadEn.getBitmapRed());
			
			m_lThreadEn.isInitingDownTxt = false;
			setImagViewWidth(imgDownEnTxtRed, 0);
		}		
	};
	
	private int mCurrentDraw = DRAW_LEFT;
	private void setDrawDirection(int iDirection) {
		RelativeLayout.LayoutParams lUpParams = (RelativeLayout.LayoutParams)imgUpTxtRed.getLayoutParams();
		RelativeLayout.LayoutParams lDownParams = (RelativeLayout.LayoutParams)imgDownTxtRed.getLayoutParams();
		if(iDirection == DRAW_LEFT) {
			mCurrentDraw = DRAW_LEFT;
			lUpParams.addRule(RelativeLayout.ALIGN_RIGHT, 0);
			imgUpTxtRed.setLayoutParams(lUpParams);
			lDownParams.addRule(RelativeLayout.ALIGN_RIGHT, 0);
			imgDownTxtRed.setLayoutParams(lDownParams);
		} else if(iDirection == DRAW_RIGHT) {
			mCurrentDraw = DRAW_RIGHT;
			lUpParams.addRule(RelativeLayout.ALIGN_RIGHT, R.id.imgUpTxt);
			imgUpTxtRed.setLayoutParams(lUpParams);
			lDownParams.addRule(RelativeLayout.ALIGN_RIGHT, R.id.imgDownTxt);
			imgDownTxtRed.setLayoutParams(lDownParams);
		}
	}

	private void setImagViewWidth(ImageView img, int iWidth) {
		if(iWidth < 0) iWidth = 0;
		ViewGroup.LayoutParams lParams = img.getLayoutParams();
		lParams.width = iWidth;
		img.setLayoutParams(lParams);
	}
}
