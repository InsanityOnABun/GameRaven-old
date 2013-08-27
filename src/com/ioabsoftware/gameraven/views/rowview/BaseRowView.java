package com.ioabsoftware.gameraven.views.rowview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.ioabsoftware.gameraven.views.rowdata.BaseRowData;
import com.ioabsoftware.gameraven.views.rowdata.RowType;

public abstract class BaseRowView extends LinearLayout {
	
	protected RowType myType = null;
	
	public BaseRowView(Context context) {
		super(context);
		init(context);
	}

	public BaseRowView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public BaseRowView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	abstract void init(Context context);
	
	public abstract void showView(BaseRowData data);
	
}
