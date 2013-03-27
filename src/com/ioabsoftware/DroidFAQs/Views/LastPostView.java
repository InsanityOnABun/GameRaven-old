package com.ioabsoftware.DroidFAQs.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

public class LastPostView extends Button {

	private String url;
	
	public LastPostView(Context context) {
		super(context);
	}
	
	public LastPostView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public LastPostView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String urlIn) {
		url = urlIn;
	}

}
