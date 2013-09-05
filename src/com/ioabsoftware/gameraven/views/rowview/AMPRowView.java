package com.ioabsoftware.gameraven.views.rowview;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.networking.HandlesNetworkResult.NetDesc;
import com.ioabsoftware.gameraven.views.RowType;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

public class AMPRowView extends TopicRowView {

	public AMPRowView(Context context) {
		super(context);
	}

	public AMPRowView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
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
	}
}
