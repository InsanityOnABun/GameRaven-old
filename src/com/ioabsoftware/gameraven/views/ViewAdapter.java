package com.ioabsoftware.gameraven.views;

import java.util.ArrayList;

import com.ioabsoftware.gameraven.views.rowdata.BaseRowData;
import com.ioabsoftware.gameraven.views.rowdata.RowType;
import com.ioabsoftware.gameraven.views.rowview.BaseRowView;
import com.ioabsoftware.gameraven.views.rowview.BoardRowView;
import com.ioabsoftware.gameraven.views.rowview.GameSearchRowView;
import com.ioabsoftware.gameraven.views.rowview.HeaderRowView;
import com.ioabsoftware.gameraven.views.rowview.MessageRowView;
import com.ioabsoftware.gameraven.views.rowview.PMRowView;
import com.ioabsoftware.gameraven.views.rowview.TopicRowView;

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
			case BOARD:
				view = new BoardRowView(context);
				break;
			case GAME_SEARCH:
				view = new GameSearchRowView(context);
				break;
			case MESSAGE:
				view = new MessageRowView(context);
				break;
			case PM:
				view = new PMRowView(context);
				break;
			case TOPIC:
				view = new TopicRowView(context);
				break;
			default:
				throw new IllegalArgumentException();
			
			}
		}
		
		view.showView(data);
		
		return view;
	}

}
