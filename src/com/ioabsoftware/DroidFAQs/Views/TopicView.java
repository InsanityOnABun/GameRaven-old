package com.ioabsoftware.DroidFAQs.Views;

import com.ioabsoftware.DroidFAQs.AllInOneV2;
import com.ioabsoftware.gameraven.R;

import android.content.Context;
import android.util.AttributeSet;
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
	
	public TopicView(Context context, String titleIn, String tcIn, String lastPostIn, String mCount, String urlIn, TopicViewType type) {
		super(context);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.topicview, this);
        
        ((TextView) findViewById(R.id.tvTitle)).setText(titleIn);
        ((TextView) findViewById(R.id.tvTC)).setText(tcIn);
        ((TextView) findViewById(R.id.tvLastPost)).setText(lastPostIn);
        ((TextView) findViewById(R.id.tvMsgCount)).setText(mCount);
        
        url = urlIn;

        findViewById(R.id.tvSep).setBackgroundColor(AllInOneV2.getAccentColor());
        findViewById(R.id.tvLPSep).setBackgroundColor(AllInOneV2.getAccentColor());
        
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
