package com.ioabsoftware.gameraven.views.rowview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.R;
import com.ioabsoftware.gameraven.views.rowdata.BaseRowData;
import com.ioabsoftware.gameraven.views.rowdata.GameSearchRowData;
import com.ioabsoftware.gameraven.views.rowdata.PMRowData;
import com.ioabsoftware.gameraven.views.rowdata.RowType;

public class PMRowView extends BaseRowView {
	
	TextView subject;
    TextView sender;
    TextView time;
	
	public PMRowView(Context context) {
		super(context);
	}

	public PMRowView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public PMRowView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	void init(Context context) {
		myType = RowType.PM;
		setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.pmview, this, true);
        
        subject = (TextView) findViewById(R.id.pmSubject);
        sender = (TextView) findViewById(R.id.pmSender);
        time = (TextView) findViewById(R.id.pmTime);
        
        subject.setTextSize(TypedValue.COMPLEX_UNIT_PX, subject.getTextSize() * AllInOneV2.getTextScale());
        sender.setTextSize(TypedValue.COMPLEX_UNIT_PX, sender.getTextSize() * AllInOneV2.getTextScale());
        time.setTextSize(TypedValue.COMPLEX_UNIT_PX, time.getTextSize() * AllInOneV2.getTextScale());
        
        findViewById(R.id.pmSep).setBackgroundColor(AllInOneV2.getAccentColor());
        
        setBackgroundDrawable(AllInOneV2.getSelector().getConstantState().newDrawable());
	}

	@Override
	public void showView(BaseRowData data) {
		if (data.getRowType() != myType)
			throw new IllegalArgumentException("data RowType does not match myType");
		
		PMRowData castData = (PMRowData) data;
		
		subject.setText(castData.getSubject());
        sender.setText(castData.getSender());
        time.setText(castData.getTime());
	}

}
