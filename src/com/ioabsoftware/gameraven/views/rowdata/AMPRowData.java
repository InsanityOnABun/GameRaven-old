package com.ioabsoftware.gameraven.views.rowdata;

import com.ioabsoftware.gameraven.views.RowType;

public class AMPRowData extends TopicRowData {

	@Override
	public RowType getRowType() {
		return RowType.AMP_TOPIC;
	}
	
	public AMPRowData(String titleIn, String tcIn, String lastPostIn,
			String mCountIn, String urlIn, String lPostUrlIn, TopicType typeIn,
			int hlColorIn) {
		super(titleIn, tcIn, lastPostIn, mCountIn, urlIn, lPostUrlIn, typeIn, hlColorIn);
	}

}
