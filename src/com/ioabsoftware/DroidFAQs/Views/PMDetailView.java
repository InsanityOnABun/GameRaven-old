package com.ioabsoftware.DroidFAQs.Views;

import com.ioabsoftware.gameraven.R;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PMDetailView extends LinearLayout {

	private String sender, title;
	
	public PMDetailView(Context context, String senderIn, String titleIn, String messageIn) {
		super(context);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.pmdetailview, this);
        
        TextView messageView = (TextView) findViewById(R.id.pmdMessage);
        
        messageView.setText(Html.fromHtml(messageIn, null, null));
        Linkify.addLinks(messageView, Linkify.WEB_URLS);
        	
        
        sender = senderIn;
        title = titleIn;
        
        setBackgroundResource(R.drawable.selector);
	}
	
	public String getSender() {
		return sender;
	}
	
	public String getTitle() {
		return title;
	}
}
