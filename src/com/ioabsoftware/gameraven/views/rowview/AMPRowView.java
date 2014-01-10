package com.ioabsoftware.gameraven.views.rowview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.networking.HandlesNetworkResult.NetDesc;
import com.ioabsoftware.gameraven.views.RowType;
import com.ioabsoftware.gameraven.views.rowdata.AMPRowData;

public class AMPRowView extends TopicRowView {

	public AMPRowView(Context context) {
		super(context);
	}

	public AMPRowView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public AMPRowView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void init(Context context) {
		super.init(context);
		myType = RowType.AMP_TOPIC;
		
		setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				String url = myData.getUrl().substring(0, myData.getUrl().lastIndexOf('/'));
				AllInOneV2.get().getSession().get(NetDesc.BOARD, url, null);
				return true;
			}
		});
		
		lpLink.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				AllInOneV2.get().enableGoToUrlDefinedPost();
				AllInOneV2.get().getSession().get(NetDesc.TOPIC, ((AMPRowData) myData).getYLPUrl(), null);
				return true;
			}
		});
	}
}
