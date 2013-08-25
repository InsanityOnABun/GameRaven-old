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

public class TopicView extends LinearLayout {
	
	public static enum TopicViewType {
		NORMAL, POLL, LOCKED, ARCHIVED, PINNED
	}

	private String url;
	
	public TopicView(Context context, String titleIn, String tcIn, 
			String lastPostIn, String mCount, String urlIn, TopicViewType type, int hlColor) {
		super(context);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.topicview, this);
        
        TextView title = (TextView) findViewById(R.id.tvTitle);
        TextView tc = (TextView) findViewById(R.id.tvTC);
        TextView msgLP = (TextView) findViewById(R.id.tvMsgCountLastPost);

        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, title.getTextSize() * AllInOneV2.getTextScale());
        tc.setTextSize(TypedValue.COMPLEX_UNIT_PX, tc.getTextSize() * AllInOneV2.getTextScale());
        msgLP.setTextSize(TypedValue.COMPLEX_UNIT_PX, msgLP.getTextSize() * AllInOneV2.getTextScale());
        
        title.setText(titleIn);
        tc.setText(tcIn);
        msgLP.setText(mCount + " Msgs, Last: " + lastPostIn);
        
        url = urlIn;

        findViewById(R.id.tvSep).setBackgroundColor(AllInOneV2.getAccentColor());
        findViewById(R.id.tvLPSep).setBackgroundColor(AllInOneV2.getAccentColor());
        
        if (hlColor != 0) {
        	tc.setTextColor(hlColor);
        	title.setTextColor(hlColor);
        }
        
        setBackgroundDrawable(AllInOneV2.getSelector().getConstantState().newDrawable());
        
        switch (type) {
		case NORMAL:
			((ImageView) findViewById(R.id.tvTypeIndicator)).setVisibility(View.GONE);
			break;
		case POLL:
			((ImageView) findViewById(R.id.tvTypeIndicator)).
					setImageResource((AllInOneV2.getUsingLightTheme() ? R.drawable.ic_poll_light : R.drawable.ic_poll));
			break;
		case LOCKED:
			((ImageView) findViewById(R.id.tvTypeIndicator)).
					setImageResource((AllInOneV2.getUsingLightTheme() ? R.drawable.ic_locked_light : R.drawable.ic_locked));
			break;
		case ARCHIVED:
			((ImageView) findViewById(R.id.tvTypeIndicator)).
					setImageResource((AllInOneV2.getUsingLightTheme() ? R.drawable.ic_archived_light : R.drawable.ic_archived));
			break;
		case PINNED:
			((ImageView) findViewById(R.id.tvTypeIndicator)).
					setImageResource((AllInOneV2.getUsingLightTheme() ? R.drawable.ic_pinned_light : R.drawable.ic_pinned));
			break;
        }
	}
	
	public String getUrl() {
		return url;
	}
	
}
