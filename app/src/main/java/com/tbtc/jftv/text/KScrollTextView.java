package com.tbtc.jftv.text;

import android.content.Context;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

public class KScrollTextView extends KTextView {
	// scrolling feature
    private Scroller mSlr;

    // milliseconds for a round of scrolling
    private int mRndDuration = 10000;

    // the X offset when paused
    private int mXPaused = 0;

    // whether it's being paused
    private boolean mPaused = true;
    
    private int mWidth = 0;
    
    public void setWidth(int iWidth) {
    	mWidth = iWidth;
    }
    
	public KScrollTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public KScrollTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public KScrollTextView(Context context) {
		super(context);
		init();
	}

	private void init() {
		setSingleLine();
	    setEllipsize(null);
	    setVisibility(INVISIBLE);
	}
	
	/**
     * begin to scroll the text from the original position
     */
     public void startScroll() {
	     // begin from the very right side
	     mXPaused = -1 * mWidth;
	     // assume it's paused
	     mPaused = true;
	     resumeScroll();
     }
     
     public void resumeScroll() {
         if (!mPaused)
        	 return;

         // Do not know why it would not scroll sometimes
         // if setHorizontallyScrolling is called in constructor.
         setHorizontallyScrolling(true);

         // use LinearInterpolator for steady scrolling
         mSlr = new Scroller(this.getContext(), new LinearInterpolator());
         setScroller(mSlr);

         int scrollingLen = calculateScrollingLen();
         int distance = scrollingLen - (mWidth + mXPaused);
         int duration = (Double.valueOf(mRndDuration * distance * 1.00000 / scrollingLen)).intValue();

         setVisibility(VISIBLE);
         mSlr.startScroll(mXPaused, 0, distance, 0, duration);
         invalidate();
         mPaused = false;
     }
     
     /**
      * calculate the scrolling length of the text in pixel
      *
      * @return the scrolling length in pixels
      */
      private int calculateScrollingLen() {
	      TextPaint tp = getPaint();
	      Rect rect = new Rect();
	      String strTxt = getText().toString();
	      tp.getTextBounds(strTxt, 0, strTxt.length(), rect);
	      int scrollingLen = rect.width() + mWidth;
	      rect = null;
	      return scrollingLen;
      }

      /**
      * pause scrolling the text
      */
      public void pauseScroll() {
	      if (null == mSlr)
	    	  return;
	
	      if (mPaused)
	    	  return;
	
	      mPaused = true;
	
	      // abortAnimation sets the current X to be the final X,
	      // and sets isFinished to be true
	      // so current position shall be saved
	      mXPaused = mSlr.getCurrX();
	
	      mSlr.abortAnimation();
      }

	@Override
	public void computeScroll() {
		// TODO Auto-generated method stub
		super.computeScroll();
		if (null == mSlr) return;

		if (mSlr.isFinished() && (!mPaused)) {
			this.startScroll();
		}
	}
	
	public int getRndDuration() {
		return mRndDuration;
    }

    public void setRndDuration(int duration) {
    	this.mRndDuration = duration;
    }

    public boolean isPaused() {
    	return mPaused;
    }
      
      
}
