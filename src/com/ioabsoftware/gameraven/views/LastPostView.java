package com.ioabsoftware.gameraven.views;

import com.ioabsoftware.gameraven.AllInOneV2;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.TextView;

public class LastPostView extends TextView {

	private String url;
	
	public LastPostView(Context context) {
		super(context);
		setBackgroundDrawable(AllInOneV2.getSelector().getConstantState().newDrawable());
	}
	
	public LastPostView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setBackgroundDrawable(AllInOneV2.getSelector().getConstantState().newDrawable());
	}
	
	public LastPostView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setBackgroundDrawable(AllInOneV2.getSelector().getConstantState().newDrawable());
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String urlIn) {
		url = urlIn;
	}

}
