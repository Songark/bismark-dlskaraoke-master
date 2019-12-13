package com.karaoke;

import com.karaoke.utils.Utility;

import android.app.Application;
import android.content.Intent;
import android.graphics.Typeface;
import android.widget.Toast;

public class MyApplication extends Application {

	Typeface quicksandRegularFont;
	Typeface quicksandLightFont;
	Typeface quicksandBoldFont;

	Typeface mainFont_Kr;
	Typeface mainFont_Cn;
	Typeface layoutFont_Kr;
	Typeface layoutFont_Cn;
	
	@Override
	public void onCreate() {
		super.onCreate();

	}

	// ---------------------- Application Fonts --------------------------
	// Regular Font
	public Typeface getQuicksandRegularFont() {
		if (quicksandRegularFont == null) {
			quicksandRegularFont = Typeface.createFromAsset(getAssets(),
					"fonts/Quicksand-Regular.ttf");
		}
		return quicksandRegularFont;
	}

	// Light Font
	public Typeface getQuicksandLightFont() {
		if (quicksandLightFont == null) {
			quicksandLightFont = Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Light.ttf");
		}
		return quicksandLightFont;
	}

	// Bold Font
	public Typeface getQuicksandBoldFont() {
		if (quicksandBoldFont == null) {
			quicksandBoldFont = Typeface.createFromAsset(getAssets(), "fonts/NanumGothicExtraBold.ttf");
		}
		return quicksandBoldFont;
	}
	
	// Bold Font
	public Typeface getMainFont_Kr() {
		if (mainFont_Kr == null) {
			mainFont_Kr = Typeface.createFromAsset(getAssets(), "fonts/NanumGothicExtraBold.ttf");
		}
		return mainFont_Kr;
	}

	public Typeface getMainFont_Cn() {
		if (mainFont_Cn == null) {
			mainFont_Cn = Typeface.createFromAsset(getAssets(), "fonts/simhei.ttf");
		}
		return mainFont_Cn;
	}


	// Text Layout Font
	public Typeface getLayoutFont_Kr() {
		if (layoutFont_Kr == null) {
			layoutFont_Kr = Typeface.createFromAsset(getAssets(), "fonts/NanumGothicBold.ttf");
		}
		return layoutFont_Kr;
	}

	public Typeface getLayoutFont_Cn() {
		if (layoutFont_Cn == null) {
			layoutFont_Cn = Typeface.createFromAsset(getAssets(), "fonts/simhei.ttf");
		}
		return layoutFont_Cn;
	}
}
