package com.ioabsoftware.gameraven.views;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.R;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.StateSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TrackedTopicView extends LinearLayout {

	private String url;
	
	public TrackedTopicView(Context context, String board, String title, String lastPost, String msgs, String urlIn) {
		super(context);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.trackedtopicview, this);
        
        url = urlIn;
        
        TextView boardName = (TextView) findViewById(R.id.ttBoardName);
        TextView titleV = (TextView) findViewById(R.id.ttTitle);
        TextView msgLP = (TextView) findViewById(R.id.ttMessageCountLastPost);

        boardName.setTextSize(TypedValue.COMPLEX_UNIT_PX, boardName.getTextSize() * AllInOneV2.getTextScale());
        titleV.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleV.getTextSize() * AllInOneV2.getTextScale());
        msgLP.setTextSize(TypedValue.COMPLEX_UNIT_PX, msgLP.getTextSize() * AllInOneV2.getTextScale());

        boardName.setText(board);
        titleV.setText(title);
        msgLP.setText(msgs + " Msgs, Last: " + lastPost);

        findViewById(R.id.ttSep).setBackgroundColor(AllInOneV2.getAccentColor());
        findViewById(R.id.ttSTSep).setBackgroundColor(AllInOneV2.getAccentColor());
        findViewById(R.id.ttLPSep).setBackgroundColor(AllInOneV2.getAccentColor());
        
        setBackgroundDrawable(AllInOneV2.getSelector().getConstantState().newDrawable());
	}
	
	public String getUrl() {
		return url;
	}
	
}
