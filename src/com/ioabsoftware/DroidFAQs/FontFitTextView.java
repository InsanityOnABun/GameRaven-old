package com.ioabsoftware.DroidFAQs;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;
import com.ioabsoftware.gameraven.R;

public class FontFitTextView extends TextView {

	public FontFitTextView(Context context) {
		super(context);
		initialise();
	}

	public FontFitTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialise();
	}

	private void initialise() {
		mTestPaint = new Paint();
		mTestPaint.set(this.getPaint());
		// max size defaults to the initially specified text size unless it is
		// too small
	}

	/*
	 * Re size the font so the specified text fits in the text box assuming the
	 * text box is the specified width.
	 */
	private void refitText(String text, int textWidth, int textHeight) {
		if (textWidth <= 0)
			return;
		int targetWidth = textWidth - this.getPaddingLeft()
				- this.getPaddingRight();
		int targetHeight = textHeight - this.getPaddingTop()
				- this.getPaddingBottom();
		float hi = 40;
		float lo = 2;
		final float threshold = 1f; // How close we have to be

		mTestPaint.set(this.getPaint());

		while ((hi - lo) > threshold) {
			float size = (hi + lo) / 2;
			mTestPaint.setTextSize(size);
			Rect rect = new Rect();
			mTestPaint.getTextBounds(text, 0, text.length(), rect);
			if (Math.abs(rect.width()) >= targetWidth
					|| Math.abs(rect.height()) >= targetHeight) {
				hi = size; // too big
			} else {
				lo = size; // too small
			}
		}
		
		float scale;
        
        if (lo > 100)
        	scale = 0.3f;
        else if (lo > 50)
        	scale = 0.6f;
        else if (lo > 25)
        	scale = 0.85f;
        else
        	scale = 1f;
        
        // Use lo so that we undershoot rather than overshoot
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, lo * scale);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
		int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
		refitText(this.getText().toString(), parentWidth, parentHeight);
		this.setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
	}

	@Override
	protected void onTextChanged(final CharSequence text, final int start,
			final int before, final int after) {
		refitText(text.toString(), this.getWidth(), this.getHeight());
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (w != oldw || h != oldh) {
			refitText(this.getText().toString(), w, h);
		}
	}

	// Attributes
	private Paint mTestPaint;
}