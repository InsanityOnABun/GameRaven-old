package com.ioabsoftware.gameraven.views.rowdata;

import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.RowType;

public class AdRowData extends BaseRowData {

	private String source, path;
	public String getSource() {return source;}
	public String getPath() {return path;}
	
	@Override
	public RowType getRowType() {
		return RowType.AD;
	}
	
	public AdRowData(String sourceIn, String pathIn) {
		source = sourceIn;
		path = pathIn;
	}

}
