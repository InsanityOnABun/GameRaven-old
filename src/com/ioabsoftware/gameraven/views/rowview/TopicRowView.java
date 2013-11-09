package com.ioabsoftware.gameraven.views.rowview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.R;
import com.ioabsoftware.gameraven.networking.HandlesNetworkResult.NetDesc;
import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.BaseRowView;
import com.ioabsoftware.gameraven.views.RowType;
import com.ioabsoftware.gameraven.views.rowdata.TopicRowData;

public class TopicRowView extends BaseRowView {
	
	TextView title;
    TextView tc;
    TextView msgLP;
    TextView lpLink;
    
    ImageView typeIndicator;
    
    TopicRowData myData;
    
    private int defaultTitleColor;
    private int defaultTCColor;
    private int defaultMsgLPColor;
    private int defaultLPLinkColor;
	
	public TopicRowView(Context context) {
		super(context);
	}

	public TopicRowView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public TopicRowView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void init(Context context) {
		myType = RowType.TOPIC;
		setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.topicview, this, true);
        
        title = (TextView) findViewById(R.id.tvTitle);
        tc = (TextView) findViewById(R.id.tvTC);
        msgLP = (TextView) findViewById(R.id.tvMsgCountLastPost);
        lpLink = (TextView) findViewById(R.id.tvLastPostLink);
        
        typeIndicator = (ImageView) findViewById(R.id.tvTypeIndicator);
        
        defaultTitleColor = title.getCurrentTextColor();
        defaultTCColor = tc.getCurrentTextColor();
        defaultMsgLPColor = msgLP.getCurrentTextColor();
        defaultLPLinkColor = lpLink.getCurrentTextColor();
        
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, title.getTextSize() * AllInOneV2.getTextScale());
        tc.setTextSize(TypedValue.COMPLEX_UNIT_PX, tc.getTextSize() * AllInOneV2.getTextScale());
        msgLP.setTextSize(TypedValue.COMPLEX_UNIT_PX, msgLP.getTextSize() * AllInOneV2.getTextScale());
        
        findViewById(R.id.tvSep).setBackgroundColor(AllInOneV2.getAccentColor());
        findViewById(R.id.tvLPSep).setBackgroundColor(AllInOneV2.getAccentColor());
        
        setBackgroundDrawable(getSelector());
        
        TextView lPostLink = (TextView) findViewById(R.id.tvLastPostLink);
        lPostLink.setBackgroundDrawable(getSelector());
        lPostLink.setTextSize(TypedValue.COMPLEX_UNIT_PX, lPostLink.getTextSize() * AllInOneV2.getTextScale());
        lPostLink.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AllInOneV2.get().enableGoToLastPost();
				AllInOneV2.get().getSession().get(NetDesc.TOPIC, myData.getLastPostUrl(), null);
			}
		});
        
        setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AllInOneV2.get().getSession().get(NetDesc.TOPIC, myData.getUrl(), null);
			}
		});
	}

	@Override
	public void showView(BaseRowData data) {
		if (data.getRowType() != myType)
			throw new IllegalArgumentException("data RowType does not match myType");
		
		myData = (TopicRowData) data;
		
		title.setText(myData.getTitle());
        tc.setText(myData.getTC());
        msgLP.setText(myData.getMCount() + " Msgs, Last: " + myData.getLastPost());
        
        int hlColor = myData.getHLColor();
        if (myData.isRead()) {
        	int readColor = AllInOneV2.getUsingLightTheme() ? Color.LTGRAY : Color.DKGRAY;
        	tc.setTextColor(readColor);
        	title.setTextColor(readColor);
        	msgLP.setTextColor(readColor);
        	lpLink.setTextColor(readColor);
        }
        else if (hlColor != 0) {
        	tc.setTextColor(hlColor);
        	title.setTextColor(hlColor);
        	msgLP.setTextColor(hlColor);
        	lpLink.setTextColor(hlColor);
        }
        else {
        	tc.setTextColor(defaultTCColor);
        	title.setTextColor(defaultTitleColor);
        	msgLP.setTextColor(defaultMsgLPColor);
        	lpLink.setTextColor(defaultLPLinkColor);
        }
        
        switch (myData.getType()) {
		case NORMAL:
			typeIndicator.setVisibility(View.GONE);
			break;
		case POLL:
			setTypeIndicator(AllInOneV2.getUsingLightTheme() ? R.drawable.ic_poll_light : R.drawable.ic_poll);
			break;
		case LOCKED:
			setTypeIndicator(AllInOneV2.getUsingLightTheme() ? R.drawable.ic_locked_light : R.drawable.ic_locked);
			break;
		case ARCHIVED:
			setTypeIndicator(AllInOneV2.getUsingLightTheme() ? R.drawable.ic_archived_light : R.drawable.ic_archived);
			break;
		case PINNED:
			setTypeIndicator(AllInOneV2.getUsingLightTheme() ? R.drawable.ic_pinned_light : R.drawable.ic_pinned);
			break;
        }
	}
    
    private void setTypeIndicator(int resId) {
    	typeIndicator.setImageResource(resId);
		typeIndicator.setVisibility(View.VISIBLE);
    }

}
