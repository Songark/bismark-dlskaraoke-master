package com.tbtc.jftv.manage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.karaoke.R;
import com.tbtc.jftv.common.Global;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

public class MarkManager {
	private static MarkManager s_instance = null;
	public static MarkManager getManager() {
		if(s_instance == null) {
			s_instance = new MarkManager();			
		}
		return s_instance;
	}
	
	private List<String> mFanfares = null;
	
	private static final int ANI_TIME = 1500;
	
	private int mTargetMark = 0;
	private ImageView mImg1, mImg2, mImgMarkText;
	public void setImageView(ImageView img1, ImageView img2, ImageView imgText) {
		mImg1 = img1;
		mImg2 = img2;
		mImgMarkText = imgText;
	}
	public MarkManager() {
		mFanfares = new ArrayList<String>();
		mFanfarePlayer = new MediaPlayer();
		mFanfarePlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				mFanfarePlayer.stop();
				mFanfarePlayer.reset();
				if(mParentHandler != null)
					mParentHandler.sendEmptyMessage(6);
			}
		});
		mFanfarePlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			
			@Override
			public void onPrepared(MediaPlayer mp) {
				// TODO Auto-generated method stub
				mFanfarePlayer.setLooping(false);
				mFanfarePlayer.start();
			}
		});
	}
	private Handler mParentHandler = null;
	public void setHandler(Handler handler) {
		mParentHandler = handler;
	}
	
	public int mCurColor = 0;
	public void setColor(int iColor) {
		mCurColor = iColor;
		//mTxtMark.setTextColor(iColor);
	}
	
	private final static int[][] IMG_ID = {
			{
				R.drawable.n0_0, 
				R.drawable.n0_1, 
				R.drawable.n0_2, 
				R.drawable.n0_3, 
				R.drawable.n0_4, 
				R.drawable.n0_5, 
				R.drawable.n0_6, 
				R.drawable.n0_7, 
				R.drawable.n0_8, 
				R.drawable.n0_9
			},
			{
				R.drawable.n1_0, 
				R.drawable.n1_1, 
				R.drawable.n1_2, 
				R.drawable.n1_3, 
				R.drawable.n1_4, 
				R.drawable.n1_5, 
				R.drawable.n1_6, 
				R.drawable.n1_7, 
				R.drawable.n1_8, 
				R.drawable.n1_9
			},
			{
				R.drawable.n2_0, 
				R.drawable.n2_1, 
				R.drawable.n2_2, 
				R.drawable.n2_3, 
				R.drawable.n2_4, 
				R.drawable.n2_5, 
				R.drawable.n2_6, 
				R.drawable.n2_7, 
				R.drawable.n2_8, 
				R.drawable.n2_9
			},
			{
				R.drawable.n3_0, 
				R.drawable.n3_1, 
				R.drawable.n3_2, 
				R.drawable.n3_3, 
				R.drawable.n3_4, 
				R.drawable.n3_5, 
				R.drawable.n3_6, 
				R.drawable.n3_7, 
				R.drawable.n3_8, 
				R.drawable.n3_9
			},
			{
				R.drawable.n4_0, 
				R.drawable.n4_1, 
				R.drawable.n4_2, 
				R.drawable.n4_3, 
				R.drawable.n4_4, 
				R.drawable.n4_5, 
				R.drawable.n4_6, 
				R.drawable.n4_7, 
				R.drawable.n4_8, 
				R.drawable.n4_9
			}
		};
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			int i1 = msg.what / 10;
			int i2 = msg.what % 10;
			mImg1.setImageResource(IMG_ID[mCurColor][i1]);
			mImg2.setImageResource(IMG_ID[mCurColor][i2]);
			//mTxtMark.setText(String.format("%02d", msg.what));
		}		
	};
	
	private Runnable mRunnable = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				int iCurMark = 0;
				int iDelay = ANI_TIME / mTargetMark;
				while(iCurMark <= mTargetMark) {
					mHandler.sendEmptyMessage(iCurMark);
					Thread.sleep(iDelay);
					iCurMark++;
				}
				
				// 점수 카운트 완료 1초후 완료메시지 전송
				mParentHandler.sendEmptyMessageDelayed(6, 3000);
			} catch(Exception e) {}
		}
	};
	private Thread mThread = null;
	private boolean mIsPlayFanfare;
	public void init() {
		if(mImg1 != null && mImg2 != null) {
			mImg1.setImageResource(IMG_ID[mCurColor][0]);
			mImg2.setImageResource(IMG_ID[mCurColor][0]);
		}
	}
	public boolean start(int currentScore, int minScore) {
		stop();
		
		if (currentScore == -1) {
			// From Midi Engine
			mTargetMark = calculateMark();
		} else {
			// From FFMpegMediaPlayer
			mTargetMark = currentScore;
		}
		
		if (mTargetMark >= 100)
			mTargetMark = 99;
		
		if (minScore > mTargetMark) {
			mTargetMark = minScore;
		}
				
		Global.Debug("MarkManager::start --> target mark=" + mTargetMark);
		if(mTargetMark < 1)
			return false;
		
		// 점수아래부분에 설명글 출력부분 추가
		// 축하음 울리는 조건을 SETUP설정에서 가져오던것을 90점이상일때에만 울리도록 고정.
		if(mTargetMark >= 60 && mTargetMark < 70)
			mImgMarkText.setImageResource(R.drawable.score_60);
		else if(mTargetMark >= 70 && mTargetMark < 80)
			mImgMarkText.setImageResource(R.drawable.score_70);
		else if(mTargetMark >= 80 && mTargetMark < 90)
			mImgMarkText.setImageResource(R.drawable.score_80);
		else if(mTargetMark >= 90 && mTargetMark < 100)
			mImgMarkText.setImageResource(R.drawable.score_90);
		else if(mTargetMark >= 100)
			mImgMarkText.setImageResource(R.drawable.score_100);
		 
		// Fanfare 처리
		/*String strFanfare = SetupManager.getManager().getFanfare();
		mIsPlayFanfare = (!strFanfare.equals("OFF") && (
				(strFanfare.equals("60-75-90") && mTargetMark >= 60 && mTargetMark <= 90) ||
				(strFanfare.equals("70-80-90") && mTargetMark >= 70 && mTargetMark <= 90) ||
				(strFanfare.equals("80-90") && mTargetMark >= 80 && mTargetMark <= 90) ||
				(strFanfare.equals("85-90") && mTargetMark >= 85 && mTargetMark <= 90) ||
				(strFanfare.equals("85-95") && mTargetMark >= 85 && mTargetMark <= 95)
				));*/
		mIsPlayFanfare = (mTargetMark >= 90);
		
		if(mIsPlayFanfare)
			playFanfare();
		mThread = new Thread(mRunnable);
		mThread.start();
		
		return true;
	}
	public void stop() {

		mImgMarkText.setImageDrawable(null);
		
		if(mThread != null) {
			mThread.interrupt();
			mThread = null;
		}
	}
	private MediaPlayer mFanfarePlayer;
	public void playFanfare() {	
		stopFanfare();
		
		if(mFanfares.size() < 1) {
			mIsPlayFanfare = false;
			return;
		}
		try {
			mFanfarePlayer.setDataSource(mFanfares.get((int)(Math.random() * mFanfares.size())));
			mFanfarePlayer.prepareAsync();
		} catch(Exception e) {}
	}
	public void stopFanfare() {
		if(mFanfarePlayer.isPlaying()) {
			mFanfarePlayer.stop();
			mFanfarePlayer.reset();
		}
	}

	public boolean loadFanfare() {
		mFanfares.clear();
				
		String strPath = Global.getFanfarePath();
		
		File dirPath = new File(strPath);
		if(!dirPath.exists())
			return false;
		
		String[] arrFiles = dirPath.list();
		if(arrFiles == null)
			return false;
		
		for(int i = 0; i < arrFiles.length; i++) {
			mFanfares.add(strPath + arrFiles[i]);
		}
		
		return true;
	}
	
	private int calculateMark() {
		int iMark = 0;
		int iMinMark = 0;
		try {
			String strScoreDisplay = SetupManager.getManager().getScoreDisplay();
			if(strScoreDisplay.equals("OFF")) {
				return 0;
			} else if(strScoreDisplay.equals("RANDOM")) {
				iMinMark = SetupManager.getManager().getAverageScore();
			} else {
				iMinMark = Integer.parseInt(strScoreDisplay);
			}
			iMark = iMinMark + (int)(Math.random() * (100 - iMinMark));
		} catch(Exception e) {
			return 0;
		}
		
		return iMark;
	}
}
