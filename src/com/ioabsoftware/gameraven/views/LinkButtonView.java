package com.ioabsoftware.gameraven.views;

import com.ioabsoftware.gameraven.AllInOneV2;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.TextView;

public class LinkButtonView extends TextView {

	public static enum Type {LAST_POST, STOP_TRACK};
	private Type type;
	
	private String url;
	
	public LinkButtonView(Context context) {
		super(context);
		if (!isInEditMode())
			setBackgroundDrawable(AllInOneV2.getSelector().getConstantState().newDrawable());
	}
	
	public LinkButtonView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (!isInEditMode())
			setBackgroundDrawable(AllInOneV2.getSelector().getConstantState().newDrawable());
	}
	
	public LinkButtonView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (!isInEditMode())
			setBackgroundDrawable(AllInOneV2.getSelector().getConstantState().newDrawable());
	}
	
	public String getUrl() {
		return url;
	}
	
	public Type getType() {
		return type;
	}
	
	public void setUrlAndType(String urlIn, Type typeIn) {
		url = urlIn;
		type = typeIn;
		setTextSize(TypedValue.COMPLEX_UNIT_PX, getTextSize() * AllInOneV2.getTextScale());
	}

}
