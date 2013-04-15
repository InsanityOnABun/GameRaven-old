package com.ioabsoftware.DroidFAQs.Views;

import com.ioabsoftware.DroidFAQs.AllInOneV2;
import com.ioabsoftware.gameraven.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HeaderView extends LinearLayout {

	public HeaderView(Context context, String text) {
		super(context);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.headerview, this);
        findViewById(R.id.hdrWrapper).setBackgroundColor(AllInOneV2.getAccentColor());
        
        ((TextView) findViewById(R.id.hdrText)).setText(text);
        ((TextView) findViewById(R.id.hdrText)).setTextColor(AllInOneV2.getAccentTextColor());
	}
	
}
