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
import com.ioabsoftware.gameraven.views.rowdata.BoardRowData;
import com.ioabsoftware.gameraven.views.rowdata.RowType;

public class BoardRowView extends BaseRowView {

	private TextView desc, lastPost, tpcMsgDetails, name;

	public BoardRowView(Context context) {
		super(context);
	}

	public BoardRowView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public BoardRowView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	void init(Context context) {
		myType = RowType.HEADER;
		setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.boardview, this, true);

    	desc = (TextView) findViewById(R.id.bvDesc);
    	lastPost = (TextView) findViewById(R.id.bvLastPost);
    	tpcMsgDetails = (TextView) findViewById(R.id.bvTpcMsgDetails);
    	name = (TextView) findViewById(R.id.bvName);
    	
    	int px = TypedValue.COMPLEX_UNIT_PX;
    	desc.setTextSize(px, desc.getTextSize() * AllInOneV2.getTextScale());
    	lastPost.setTextSize(px, lastPost.getTextSize() * AllInOneV2.getTextScale());
    	tpcMsgDetails.setTextSize(px, tpcMsgDetails.getTextSize() * AllInOneV2.getTextScale());
    	name.setTextSize(px, name.getTextSize() * AllInOneV2.getTextScale());
    	
    	findViewById(R.id.bvSep).setBackgroundColor(AllInOneV2.getAccentColor());
        
        setBackgroundDrawable(AllInOneV2.getSelector().getConstantState().newDrawable());
	}

	@Override
	public void showView(BaseRowData data) {
		if (data.getRowType() != myType)
			throw new IllegalArgumentException("data RowType does not match myType");
		
		BoardRowData castData = (BoardRowData) data;
		
		name.setText(castData.getName());
		desc.setText(castData.getDesc());
		lastPost.setText("Last Post: " + castData.getLastPost());
		tpcMsgDetails.setText("Tpcs: " + castData.getTCount() + "; Msgs: " + castData.getMCount());
	}

}
