package com.ioabsoftware.gameraven.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.ioabsoftware.gameraven.R;


public abstract class BaseRowView extends LinearLayout {
	
	protected RowType myType = null;
	
	public BaseRowView(Context context) {
		super(context);
		init(context);
	}

	public BaseRowView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public BaseRowView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	protected Drawable getSelector() {
		StateDrawable s = new StateDrawable(new Drawable[] {getResources().getDrawable(R.drawable.selector)});
		s.setMyColor(Color.TRANSPARENT);
		return s;
	}
	
	protected abstract void init(Context context);
	
	public abstract void showView(BaseRowData data);
	
}
