package com.tbtc.jftv.text;



import com.karaoke.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

@SuppressLint("AppCompatCustomView")
public class KImageView extends ImageView {
	public static final int NO_SCALE_TYPE = 0;
	public static final int LEFT_CROP = 1;
	public static final int TOP_CROP = 2;
	public static final int RIGHT_CROP = 4;
	public static final int BOTTOM_CROP = 8;

	private int mScaleType;
	private ScaleType mScaleTypeOrigin = ScaleType.MATRIX; //ScaleType.FIT_CENTER;

	public KImageView(Context context) {
		this(context, null);
	}

	public KImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public KImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.KImageView, defStyle, 0);

		setScaleType(styledAttrs.getInt(R.styleable.KImageView_scaleType, NO_SCALE_TYPE));

		styledAttrs.recycle();
	}

	public void setScaleType(int scaleType) {
		if (scaleType == NO_SCALE_TYPE) {
			super.setScaleType(mScaleTypeOrigin);
		} else {
			super.setScaleType(ScaleType.MATRIX);
		}
		mScaleType = scaleType;
	}

	@Override
	public void setScaleType(ScaleType scaleType) {
		if (mScaleType == NO_SCALE_TYPE) {
			super.setScaleType(scaleType);
		}
		mScaleTypeOrigin = scaleType;
	}

	@Override
	protected boolean setFrame(int left, int top, int right, int bottom) {
		boolean leftCrop = (mScaleType & LEFT_CROP) == LEFT_CROP;
		boolean topCrop = (mScaleType & TOP_CROP) == TOP_CROP;
		boolean rightCrop = (mScaleType & RIGHT_CROP) == RIGHT_CROP;
		boolean bottomCrop = (mScaleType & BOTTOM_CROP) == BOTTOM_CROP;
		if (leftCrop || topCrop || rightCrop || bottomCrop) {
			Drawable drawable = getDrawable();
			if (drawable != null) {
				int dwidth = drawable.getIntrinsicWidth();
				int dheight = drawable.getIntrinsicHeight();
				// int vwidth = getWidth() - getPaddingLeft() - getPaddingRight();
				// int vheight = getHeight() - getPaddingTop() - getPaddingBottom();
				int vwidth = right - left - getPaddingLeft() - getPaddingRight();
				int vheight = bottom - top - getPaddingTop() - getPaddingBottom();
				if (dwidth > 0 && dheight > 0) {
					float scale;
					float dx = 0, dy = 0;
					if (dwidth * vheight > vwidth * dheight) {
						scale = (float) vheight / (float) dheight;
						dx = (vwidth - dwidth * scale) * ((leftCrop && !rightCrop) ? 0 : (!leftCrop && rightCrop) ? 1 : 0.5f);
					} else {
						scale = (float) vwidth / (float) dwidth;
						dy = (vheight - dheight * scale) * ((topCrop && !bottomCrop) ? 0 : (!topCrop && bottomCrop) ? 1 : 0.5f);
					}
					// Matrix matrix = new Matrix();
					Matrix matrix = getImageMatrix();
					matrix.setScale(scale, scale);
					//matrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
					matrix.postTranslate((int) (dx), (int) (dy));
					setImageMatrix(matrix);
				}
			}
		}
		return super.setFrame(left, top, right, bottom);
	}
}
