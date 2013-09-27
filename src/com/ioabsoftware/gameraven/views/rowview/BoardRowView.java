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
import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.BaseRowView;
import com.ioabsoftware.gameraven.views.RowType;
import com.ioabsoftware.gameraven.views.rowdata.BoardRowData;
import com.ioabsoftware.gameraven.views.rowdata.BoardRowData.BoardType;

public class BoardRowView extends BaseRowView {

	private TextView desc, lastPost, tpcMsgDetails, name;
	
	BoardRowData myData;

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
	protected void init(Context context) {
		myType = RowType.BOARD;
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
    	
    	setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (myData.getBoardType() == BoardType.LIST) {
					AllInOneV2.get().getSession().get(NetDesc.BOARD_LIST, myData.getUrl(), null);
				}
				else {
					AllInOneV2.get().getSession().get(NetDesc.BOARD, myData.getUrl(), null);
				}
			}
		});
        
        setBackgroundDrawable(getSelector());
	}

	@Override
	public void showView(BaseRowData data) {
		if (data.getRowType() != myType)
			throw new IllegalArgumentException("data RowType does not match myType");
		
		myData = (BoardRowData) data;
		
		name.setText(myData.getName());
		
		String descText = myData.getDesc();
		if (descText != null) {
			desc.setVisibility(View.VISIBLE);
    		desc.setText(descText);
		}
    	else
    		desc.setVisibility(View.INVISIBLE);
    	
    	switch (myData.getBoardType()) {
		case NORMAL:
            tpcMsgDetails.setVisibility(View.VISIBLE);
            lastPost.setText("Last Post: " + myData.getLastPost());
            tpcMsgDetails.setText("Tpcs: " + myData.getTCount() + "; Msgs: " + myData.getMCount());
			break;
		case SPLIT:
			lastPost.setText("--Split List--");
            tpcMsgDetails.setVisibility(View.INVISIBLE);
			break;
		case LIST:
            lastPost.setText("--Board List--");
            tpcMsgDetails.setVisibility(View.INVISIBLE);
			break;
    	}
	}

}
