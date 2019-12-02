package com.tbtc.jftv.manage;

import android.app.Activity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.karaoke.R;
import com.karaoke.activity.MidiPlayerActivity;
import com.tbtc.jftv.text.KScrollTextView;

public class GreetManager {
	private MidiPlayerActivity mParent;
	
	private LinearLayout mLnGreet;
	private TextView mTxtGreet;
	
	private int mScreenWidth;
	
	public GreetManager(Activity activity, MidiPlayerActivity parent) {
		mParent = parent;
		
		DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		mScreenWidth = metrics.widthPixels;		
	}
	
	public void init() {
		mLnGreet = (LinearLayout)mParent.findViewById(R.id.lnGreet);
		mTxtGreet = (TextView)mParent.findViewById(R.id.txtGreet);
		mTxtGreet.setWidth(mScreenWidth);
		hide();
	}
	
	public void show(String strMsg) {
		// String strMsg = SetupManager.getManager().getGreetFunction();
		if(TextUtils.isEmpty(strMsg) || strMsg.equals("ENTER"))
			return;
		
		mTxtGreet.setText(strMsg);
		mTxtGreet.setSelected(true);
		mLnGreet.setVisibility(View.VISIBLE);
		mParent.refreshLayout();
	}
	public void hide() {
		mTxtGreet.setText("");
		mTxtGreet.setSelected(false);
		mLnGreet.setVisibility(View.INVISIBLE);
	}
	public void setColor(int iColor) {
		mTxtGreet.setTextColor(iColor);
	}

}
