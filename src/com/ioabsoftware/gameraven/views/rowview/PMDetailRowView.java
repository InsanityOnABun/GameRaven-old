package com.ioabsoftware.gameraven.views.rowview;

import android.content.Context;
import android.text.Html;
import android.text.method.ArrowKeyMovementMethod;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.R;
import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.BaseRowView;
import com.ioabsoftware.gameraven.views.RowType;
import com.ioabsoftware.gameraven.views.rowdata.PMDetailRowData;

public class PMDetailRowView extends BaseRowView {
	
	TextView messageView;
	
	PMDetailRowData myData;
	
	public PMDetailRowView(Context context) {
		super(context);
	}

	public PMDetailRowView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public PMDetailRowView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void init(Context context) {
		myType = RowType.PM_DETAIL;
		setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.pmdetailview, this, true);

        TextView replyLabel = (TextView) findViewById(R.id.pmdReplyLabel);
        
        messageView = (TextView) findViewById(R.id.pmdMessage);
        
        messageView.setTextSize(TypedValue.COMPLEX_UNIT_PX, messageView.getTextSize() * AllInOneV2.getTextScale());
        replyLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, replyLabel.getTextSize() * AllInOneV2.getTextScale());
        
        messageView.setLinkTextColor(AllInOneV2.getAccentColor());
        
        findViewById(R.id.pmdMidSep).setBackgroundColor(AllInOneV2.getAccentColor());
        findViewById(R.id.pmdBotSep).setBackgroundColor(AllInOneV2.getAccentColor());
        
        replyLabel.setBackgroundDrawable(getSelector());
        
        replyLabel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AllInOneV2.get().pmSetup(myData.getSender(), myData.getTitle(), null);
			}
		});
	}
	
	@Override
	public void showView(BaseRowData data) {
		if (data.getRowType() != myType)
			throw new IllegalArgumentException("data RowType does not match myType");
		
		myData = (PMDetailRowData) data;
		
		messageView.setText(Html.fromHtml(myData.getMessage(), null, null));
        Linkify.addLinks(messageView, Linkify.WEB_URLS);
        
    	messageView.setMovementMethod(ArrowKeyMovementMethod.getInstance());
    	messageView.setTextIsSelectable(true);
        // the autoLink attribute must be removed, if you hasn't set it then ok, otherwise call textView.setAutoLink(0);
	}

}
