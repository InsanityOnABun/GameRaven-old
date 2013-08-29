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
import com.ioabsoftware.gameraven.views.rowdata.GameSearchRowData;
import com.ioabsoftware.gameraven.views.rowdata.RowType;

public class GameSearchRowView extends BaseRowView {
	
	TextView platform, name;

	public GameSearchRowView(Context context) {
		super(context);
	}

	public GameSearchRowView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public GameSearchRowView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	void init(Context context) {
		myType = RowType.GAME_SEARCH;
		setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.gamesearchview, this, true);
        
        platform = (TextView) findViewById(R.id.gsPlatform);
    	name = (TextView) findViewById(R.id.gsName);
    	
    	int px = TypedValue.COMPLEX_UNIT_PX;
        name.setTextSize(px, name.getTextSize() * AllInOneV2.getTextScale());
        platform.setTextSize(px, platform.getTextSize() * AllInOneV2.getTextScale());
        
        findViewById(R.id.gsSep).setBackgroundColor(AllInOneV2.getAccentColor());
        
        setBackgroundDrawable(AllInOneV2.getSelector().getConstantState().newDrawable());
	}

	@Override
	public void showView(BaseRowData data) {
		if (data.getRowType() != myType)
			throw new IllegalArgumentException("data RowType does not match myType");
		
		GameSearchRowData castData = (GameSearchRowData) data;
		
		name.setText(castData.getName());
		platform.setText(castData.getPlatform());
	}

}
