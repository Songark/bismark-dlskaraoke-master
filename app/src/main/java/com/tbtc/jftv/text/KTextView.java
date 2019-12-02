package com.tbtc.jftv.text;


import com.karaoke.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.widget.TextView;

@SuppressLint("AppCompatCustomView")
public class KTextView extends TextView {
	private boolean stroke = false;
	private float strokeWidth = 0.0f;
	private int strokeColor;

	public KTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context, attrs);
	}

	public KTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context, attrs);
	}

	public KTextView(Context context) {
		super(context);
	}
	
	private void initView(Context context, AttributeSet attrs) {
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StrockTextView);
		stroke = a.getBoolean(R.styleable.StrockTextView_textStroke, false);
		strokeWidth = a.getFloat(R.styleable.StrockTextView_textStrokeWidth, 0.0f);
		strokeColor = a.getColor(R.styleable.StrockTextView_textStrokeColor, 0xffffffff);
		a.recycle();
		
		if(stroke) {
			setPadding((int)strokeWidth, 0, (int)strokeWidth, 0);
			setShadowLayer(strokeWidth, 1, 1, 0x00000000);
		}
	}
	public void setStroke(boolean bStroke, float fWidth, int iColor) {
		stroke = bStroke;
		strokeWidth = fWidth;
		strokeColor = iColor;
		
		if(stroke) {
			setPadding((int)strokeWidth, 0, (int)strokeWidth, 0);
			setShadowLayer(strokeWidth, 1, 1, 0x00000000);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if(stroke) {
			ColorStateList states = getTextColors();
			getPaint().setStyle(Style.STROKE);
			getPaint().setStrokeWidth(strokeWidth);
			setTextColor(strokeColor);
			super.onDraw(canvas);
			
			getPaint().setStyle(Style.FILL);
			setTextColor(states);
		}
		super.onDraw(canvas);
	}	
}
