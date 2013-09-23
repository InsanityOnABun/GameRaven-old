package com.ioabsoftware.gameraven.views.rowview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.TextView;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.R;
import com.ioabsoftware.gameraven.networking.HandlesNetworkResult.NetDesc;
import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.BaseRowView;
import com.ioabsoftware.gameraven.views.RowType;
import com.ioabsoftware.gameraven.views.rowdata.TrackedTopicRowData;

public class TrackedTopicRowView extends BaseRowView {

	TextView boardName;
    TextView title;
    TextView msgLP;
    
    TrackedTopicRowData myData;
    
	public TrackedTopicRowView(Context context) {
		super(context);
	}

	public TrackedTopicRowView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public TrackedTopicRowView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void init(Context context) {
		myType = RowType.TRACKED_TOPIC;
		setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.trackedtopicview, this, true);
        
        boardName = (TextView) findViewById(R.id.ttBoardName);
        title = (TextView) findViewById(R.id.ttTitle);
        msgLP = (TextView) findViewById(R.id.ttMessageCountLastPost);
        
        boardName.setTextSize(TypedValue.COMPLEX_UNIT_PX, boardName.getTextSize() * AllInOneV2.getTextScale());
        title.setTextSize(TypedValue.COMPLEX_UNIT_PX, title.getTextSize() * AllInOneV2.getTextScale());
        msgLP.setTextSize(TypedValue.COMPLEX_UNIT_PX, msgLP.getTextSize() * AllInOneV2.getTextScale());
        
        findViewById(R.id.ttSep).setBackgroundColor(AllInOneV2.getAccentColor());
        findViewById(R.id.ttSTSep).setBackgroundColor(AllInOneV2.getAccentColor());
        findViewById(R.id.ttLPSep).setBackgroundColor(AllInOneV2.getAccentColor());
        
        setBackgroundDrawable(AllInOneV2.getSelector());
        
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
        
        TextView lPostLink = (TextView) findViewById(R.id.ttLastPostLink);
        lPostLink.setBackgroundDrawable(AllInOneV2.getSelector());
        lPostLink.setTextSize(TypedValue.COMPLEX_UNIT_PX, lPostLink.getTextSize() * AllInOneV2.getTextScale());
        lPostLink.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AllInOneV2.get().enableGoToLastPost();
				AllInOneV2.get().getSession().get(NetDesc.TOPIC, myData.getLastPostUrl(), null);
			}
		});
        
        TextView removeLink = (TextView) findViewById(R.id.ttStopTracking);
        removeLink.setBackgroundDrawable(AllInOneV2.getSelector());
        removeLink.setTextSize(TypedValue.COMPLEX_UNIT_PX, removeLink.getTextSize() * AllInOneV2.getTextScale());
        removeLink.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AllInOneV2.get().getSession().get(NetDesc.TRACKED_TOPICS, myData.getRemoveUrl(), null);
			}
		});
	}

	@Override
	public void showView(BaseRowData data) {
		if (data.getRowType() != myType)
			throw new IllegalArgumentException("data RowType does not match myType");
		
		myData = (TrackedTopicRowData) data;
		
		boardName.setText(myData.getBoard());
        title.setText(myData.getTitle());
        msgLP.setText(myData.getMsgs() + " Msgs, Last: " + myData.getLastPost());
	}

}
