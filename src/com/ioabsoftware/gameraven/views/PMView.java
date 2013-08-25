package com.ioabsoftware.gameraven.views;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.R;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PMView extends LinearLayout {

	private String url;
	
	public PMView(Context context, String subjectIn, String senderIn, String timeIn, String urlIn) {
		super(context);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.pmview, this);
        
        TextView subject = (TextView) findViewById(R.id.pmSubject);
        TextView sender = (TextView) findViewById(R.id.pmSender);
        TextView time = (TextView) findViewById(R.id.pmTime);

        subject.setTextSize(TypedValue.COMPLEX_UNIT_PX, subject.getTextSize() * AllInOneV2.getTextScale());
        sender.setTextSize(TypedValue.COMPLEX_UNIT_PX, sender.getTextSize() * AllInOneV2.getTextScale());
        time.setTextSize(TypedValue.COMPLEX_UNIT_PX, time.getTextSize() * AllInOneV2.getTextScale());
        
        subject.setText(subjectIn);
        sender.setText(senderIn);
        time.setText(timeIn);
        	
        
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
