package com.ioabsoftware.gameraven.views.rowdata;

public class HeaderRowData extends BaseRowData {

	private String headerText;
	public String getHeaderText() {return headerText;}
	
	@Override
	public RowType getRowType() {
		return RowType.HEADER;
	}
	
	public HeaderRowData(String text) {
		headerText = text;
	}

}
