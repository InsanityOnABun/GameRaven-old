package com.ioabsoftware.gameraven.views;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.R;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BoardView extends LinearLayout {
	
	public static enum BoardViewType {
		NORMAL, SPLIT, LIST
	}
	
	protected String url;
	
	private BoardViewType type;
	
	public BoardView(Context context, String nameIn, String descIn, String lastPostIn, 
					 String tCount, String mCount, String urlIn, BoardViewType typeIn) {
		super(context);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.boardview, this);

    	TextView desc = (TextView) findViewById(R.id.bvDesc);
    	TextView lastPost = (TextView) findViewById(R.id.bvLastPost);
    	TextView tpcMsgDetails = (TextView) findViewById(R.id.bvTpcMsgDetails);
    	TextView name = (TextView) findViewById(R.id.bvName);

    	int px = TypedValue.COMPLEX_UNIT_PX;
    	desc.setTextSize(px, desc.getTextSize() * AllInOneV2.getTextScale());
    	lastPost.setTextSize(px, lastPost.getTextSize() * AllInOneV2.getTextScale());
    	tpcMsgDetails.setTextSize(px, tpcMsgDetails.getTextSize() * AllInOneV2.getTextScale());
    	name.setTextSize(px, name.getTextSize() * AllInOneV2.getTextScale());

    	name.setText(nameIn);
    	
    	if (descIn != null)
    		desc.setText(descIn);
    	else
    		desc.setVisibility(View.INVISIBLE);
    	
    	switch (typeIn) {
		case NORMAL:
            lastPost.setText("Last Post: " + lastPostIn);
            tpcMsgDetails.setText("Tpcs: " + tCount + "; Msgs: " + mCount);
			break;
		case SPLIT:
			lastPost.setText("--Split List--");
            tpcMsgDetails.setVisibility(View.INVISIBLE);
			break;
		case LIST:
            lastPost.setText("--Board List--");
            tpcMsgDetails.setVisibility(View.INVISIBLE);
			break;
    	}
        
        url = urlIn;
        type = typeIn;
        
        findViewById(R.id.bvSep).setBackgroundColor(AllInOneV2.getAccentColor());
        
        setBackgroundDrawable(AllInOneV2.getSelector().getConstantState().newDrawable());
	}
	
	public String getUrl() {
		return url;
	}
	
	public BoardViewType getType() {
		return type;
	}
}
