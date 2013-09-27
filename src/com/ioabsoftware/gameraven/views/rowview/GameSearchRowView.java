package com.ioabsoftware.gameraven.views.rowview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.R;
import com.ioabsoftware.gameraven.networking.HandlesNetworkResult.NetDesc;
import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.BaseRowView;
import com.ioabsoftware.gameraven.views.RowType;
import com.ioabsoftware.gameraven.views.rowdata.BoardRowData;
import com.ioabsoftware.gameraven.views.rowdata.GameSearchRowData;
import com.ioabsoftware.gameraven.views.rowdata.BoardRowData.BoardType;

public class GameSearchRowView extends BaseRowView {
	
	TextView platform, name;
	
	GameSearchRowData myData;

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
	protected void init(Context context) {
		myType = RowType.GAME_SEARCH;
		setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.gamesearchview, this, true);
        
        platform = (TextView) findViewById(R.id.gsPlatform);
    	name = (TextView) findViewById(R.id.gsName);
    	
    	int px = TypedValue.COMPLEX_UNIT_PX;
        name.setTextSize(px, name.getTextSize() * AllInOneV2.getTextScale());
        platform.setTextSize(px, platform.getTextSize() * AllInOneV2.getTextScale());
        
        findViewById(R.id.gsSep).setBackgroundColor(AllInOneV2.getAccentColor());
        
        setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AllInOneV2.get().getSession().get(NetDesc.BOARD, myData.getUrl(), null);
			}
		});
        
        setBackgroundDrawable(getSelector());
	}

	@Override
	public void showView(BaseRowData data) {
		if (data.getRowType() != myType)
			throw new IllegalArgumentException("data RowType does not match myType");
		
		myData = (GameSearchRowData) data;
		
		name.setText(myData.getName());
		platform.setText(myData.getPlatform());
	}

}
