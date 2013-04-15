package com.ioabsoftware.DroidFAQs.Views;

import com.ioabsoftware.DroidFAQs.AllInOneV2;
import com.ioabsoftware.gameraven.R;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PMView extends LinearLayout {

	private String url;
	
	public PMView(Context context, String subjectIn, String senderIn, String timeIn, String urlIn) {
		super(context);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.pmview, this);
        
        ((TextView) findViewById(R.id.pmSubject)).setText(subjectIn);
        ((TextView) findViewById(R.id.pmSender)).setText(senderIn);
        ((TextView) findViewById(R.id.pmTime)).setText(timeIn);
        	
        
        url = urlIn;
        
        findViewById(R.id.pmSep).setBackgroundColor(AllInOneV2.getAccentColor());
        
        setBackgroundDrawable(AllInOneV2.getSelector().getConstantState().newDrawable());
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setOld() {
		((TextView) findViewById(R.id.pmSubject)).setTextColor(Color.GRAY);
	}
}
