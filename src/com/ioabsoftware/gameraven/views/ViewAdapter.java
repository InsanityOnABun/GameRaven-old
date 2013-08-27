package com.ioabsoftware.gameraven.views;

import java.util.ArrayList;

import com.ioabsoftware.gameraven.views.rowdata.BaseRowData;
import com.ioabsoftware.gameraven.views.rowdata.RowType;
import com.ioabsoftware.gameraven.views.rowview.BaseRowView;
import com.ioabsoftware.gameraven.views.rowview.HeaderRowView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ViewAdapter extends BaseAdapter {
	
	ArrayList<BaseRowData> rows;
	Context context;

	public ViewAdapter(Context contextIn, ArrayList<BaseRowData> rowsIn) {
		context = contextIn;
		rows = rowsIn;
	}
	
	@Override
	public int getCount() {
		return rows.size();
	}

	@Override
	public BaseRowData getItem(int position) {
		return rows.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public int getViewTypeCount() {
		return RowType.values().length;
	}
	
	@Override
	public int getItemViewType(int position) {
		return rows.get(position).getRowType().ordinal();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		BaseRowView view;
		BaseRowData data = rows.get(position);
		
		if (convertView != null) {
			view = (BaseRowView) convertView;
		}
		else {
			switch (data.getRowType()) {
			case HEADER:
				view = new HeaderRowView(context);
				break;
			default:
				throw new IllegalArgumentException();
			
			}
		}
		
		view.showView(data);
		
		return view;
	}

}
