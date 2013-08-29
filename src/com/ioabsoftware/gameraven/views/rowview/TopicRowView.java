package com.ioabsoftware.gameraven.views.rowview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.R;
import com.ioabsoftware.gameraven.networking.HandlesNetworkResult.NetDesc;
import com.ioabsoftware.gameraven.views.rowdata.BaseRowData;
import com.ioabsoftware.gameraven.views.rowdata.RowType;
import com.ioabsoftware.gameraven.views.rowdata.TopicRowData;

public class TopicRowView extends BaseRowView {
	
	TextView title;
    TextView tc;
    TextView msgLP;
    
    TopicRowData myData;
	
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
	void init(Context context) {
		myType = RowType.TOPIC;
		setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.topicview, this, true);
        
        title = (TextView) findViewById(R.id.tvTitle);
        tc = (TextView) findViewById(R.id.tvTC);
        msgLP = (TextView) findViewById(R.id.tvMsgCountLastPost);
        
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, title.getTextSize() * AllInOneV2.getTextScale());
        tc.setTextSize(TypedValue.COMPLEX_UNIT_PX, tc.getTextSize() * AllInOneV2.getTextScale());
        msgLP.setTextSize(TypedValue.COMPLEX_UNIT_PX, msgLP.getTextSize() * AllInOneV2.getTextScale());
        
        findViewById(R.id.tvSep).setBackgroundColor(AllInOneV2.getAccentColor());
        findViewById(R.id.tvLPSep).setBackgroundColor(AllInOneV2.getAccentColor());
        
        setBackgroundDrawable(AllInOneV2.getSelector().getConstantState().newDrawable());
        
        TextView lPostLink = (TextView) findViewById(R.id.tvLastPostLink);
        lPostLink.setBackgroundDrawable(AllInOneV2.getSelector().getConstantState().newDrawable());
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
        
        setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				String url = myData.getUrl().substring(0, myData.getUrl().lastIndexOf('/'));
				AllInOneV2.get().getSession().get(NetDesc.BOARD, url, null);
				return true;
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
        if (hlColor != 0) {
        	tc.setTextColor(hlColor);
        	title.setTextColor(hlColor);
        }
	}

}
