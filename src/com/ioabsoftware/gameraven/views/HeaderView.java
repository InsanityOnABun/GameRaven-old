package com.ioabsoftware.gameraven.views;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.R;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HeaderView extends LinearLayout {

	public HeaderView(Context context, String text) {
		super(context);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.headerview, this);
        findViewById(R.id.hdrWrapper).setBackgroundColor(AllInOneV2.getAccentColor());
        
        TextView tView = (TextView) findViewById(R.id.hdrText);
        tView.setTextSize(TypedValue.COMPLEX_UNIT_PX, tView.getTextSize() * AllInOneV2.getTextScale());
        tView.setTextColor(AllInOneV2.getAccentTextColor());
        tView.setText(text);
	}
	
}
