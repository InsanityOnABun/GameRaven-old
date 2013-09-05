package com.ioabsoftware.gameraven.views.rowview;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebView;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.BaseRowView;
import com.ioabsoftware.gameraven.views.RowType;
import com.ioabsoftware.gameraven.views.rowdata.AdRowData;

public class AdRowView extends BaseRowView {

	private WebView web;
	
	public AdRowView(Context context, BaseRowData data) {
		super(context);
		
		if (data.getRowType() != myType)
			throw new IllegalArgumentException("data RowType does not match myType");
		
		AdRowData myData = (AdRowData) data;
		
		web.loadDataWithBaseURL(myData.getPath(), myData.getSource(), "text/html", "iso-8859-1", null);
	}
	
	public AdRowView(Context context) {
		super(context);
	}

	public AdRowView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public AdRowView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void init(Context context) {
		myType = RowType.AD;
		setOrientation(VERTICAL);
		
		web = new WebView(getContext());
		web.getSettings().setJavaScriptEnabled(AllInOneV2.getSettingsPref().getBoolean("enableJS", true));
		this.addView(web);
	}

	@Override
	public void showView(BaseRowData data) {
		/*
		 * There is only ever one ad in the list, so there is no view updating
		 * needed. Moreover, we need to load the ad into the WebView immediately,
		 * not when it actually comes into view. That way GFAQs can't complain
		 * that their ads aren't being loaded ;)
		 */
	}

}
