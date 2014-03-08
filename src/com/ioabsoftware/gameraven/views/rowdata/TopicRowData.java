package com.ioabsoftware.gameraven.views.rowdata;

import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.RowType;


public class TopicRowData extends BaseRowData {

	public static enum TopicType {
		NORMAL, POLL, LOCKED, ARCHIVED, PINNED
	}
	
	String title, tc, lastPost, mCount, url, lPostUrl;
	public String getTitle() {return title;}
	public String getTC() {return tc;}
	public String getLastPost() {return lastPost;}
	public String getMCount() {return mCount;}
	public String getUrl() {return url;}
	public String getLastPostUrl() {return lPostUrl;}
	
	TopicType type;
	public TopicType getType() {return type;}
	
	ReadStatus status;
	public ReadStatus getStatus() {return status;}
	
	int hlColor;
	public int getHLColor() {return hlColor;}
	
	@Override
	public RowType getRowType() {
		return RowType.TOPIC;
	}
	
	public TopicRowData(String titleIn, String tcIn, String lastPostIn, String mCountIn, 
			String urlIn, String lPostUrlIn, TopicType typeIn, ReadStatus statusIn, int hlColorIn) {
		title = titleIn;
		tc = tcIn;
		lastPost = lastPostIn;
		mCount = mCountIn;
		url = urlIn;
		lPostUrl = lPostUrlIn;
		
		type = typeIn;
		
		status = statusIn;
		
		hlColor = hlColorIn;
	}

}
