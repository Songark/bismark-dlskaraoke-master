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

	Typeface latoReguarFont;
	Typeface simheiFont;
	
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
			quicksandBoldFont = Typeface.createFromAsset(getAssets(), "fonts/Quicksand-Bold.ttf");
		}
		return quicksandBoldFont;
	}
	
	// Bold Font
	public Typeface getLatoRegularFont() {
		if (latoReguarFont == null) {
			latoReguarFont = Typeface.createFromAsset(getAssets(), "fonts/Lato-Regular.ttf");
		}
		return latoReguarFont;
	}

	// Simhei Font
	public Typeface getSimheiFont() {
		if (simheiFont == null) {
			simheiFont = Typeface.createFromAsset(getAssets(), "fonts/simhei.ttf");
		}
		return simheiFont;
	}
}
