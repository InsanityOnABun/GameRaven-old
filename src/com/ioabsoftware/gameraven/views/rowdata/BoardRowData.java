package com.ioabsoftware.gameraven.views.rowdata;

public class BoardRowData extends BaseRowData {
	
	private String name, desc, lastPost, tCount, mCount, url;
	public String getName() {return name;}
	public String getDesc() {return desc;}
	public String getLastPost() {return lastPost;}
	public String getTCount() {return tCount;}
	public String getMCount() {return mCount;}
	public String getUrl() {return url;}

	public BoardRowData(String nameIn, String descIn, String lastPostIn, 
			 String tCountIn, String mCountIn, String urlIn) {
		name = nameIn;
		desc = descIn;
		lastPost = lastPostIn;
		tCount = tCountIn;
		mCount = mCountIn;
		url = urlIn;
	}
	
	@Override
	public RowType getRowType() {
		return RowType.BOARD;
	}

}
