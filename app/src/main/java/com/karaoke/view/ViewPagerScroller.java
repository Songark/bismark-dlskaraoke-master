package com.karaoke.view;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * Created by ShineStar on 14-6-6.
 */
public class ViewPagerScroller extends Scroller {

	private int mScrollDuration = 700;// 滑动速度

	public ViewPagerScroller(Context context) {
		super(context);
	}

	public ViewPagerScroller(Context context, Interpolator interpolator) {
		super(context, interpolator);
	}

	@Override
	public void startScroll(int startX, int startY, int dx, int dy, int duration) {
		super.startScroll(startX, startY, dx, dy, mScrollDuration);
	}

	@Override
	public void startScroll(int startX, int startY, int dx, int dy) {
		super.startScroll(startX, startY, dx, dy, mScrollDuration);
	}

}