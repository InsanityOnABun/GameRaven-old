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
import com.ioabsoftware.gameraven.views.rowdata.BaseRowData;
import com.ioabsoftware.gameraven.views.rowdata.HeaderRowData;
import com.ioabsoftware.gameraven.views.rowdata.RowType;

public class HeaderRowView extends BaseRowView {
	
	private TextView tView;

	public HeaderRowView(Context context) {
		super(context);
	}

	public HeaderRowView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public HeaderRowView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void init(Context context) {
		myType = RowType.HEADER;
		setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.headerview, this, true);
        tView = (TextView) findViewById(R.id.hdrText);
        
        tView.setTextSize(TypedValue.COMPLEX_UNIT_PX, tView.getTextSize() * AllInOneV2.getTextScale());
        tView.setTextColor(AllInOneV2.getAccentTextColor());
        
        setBackgroundColor(AllInOneV2.getAccentColor());
	}

	@Override
	public void showView(BaseRowData data) {
		if (data.getRowType() != myType)
			throw new IllegalArgumentException("data RowType does not match myType");
		
		tView.setText(((HeaderRowData) data).getHeaderText());
	}

}
