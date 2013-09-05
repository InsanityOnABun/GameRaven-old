package com.ioabsoftware.gameraven.views.rowdata;

import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.RowType;


public class TrackedTopicRowData extends BaseRowData {

	private String board, title, lastPost, msgs, url, removeUrl, lastPostUrl;
	public String getBoard() {return board;}
	public String getTitle() {return title;}
	public String getLastPost() {return lastPost;}
	public String getMsgs() {return msgs;}
	public String getUrl() {return url;}
	public String getRemoveUrl() {return removeUrl;}
	public String getLastPostUrl() {return lastPostUrl;}
	
	@Override
	public RowType getRowType() {
		return RowType.TRACKED_TOPIC;
	}
	
	public TrackedTopicRowData(String boardIn, String titleIn, String lastPostIn, 
			String msgsIn, String urlIn, String removeUrlIn, String lastPostUrlIn) {
		board = boardIn;
		title = titleIn;
		lastPost = lastPostIn;
		msgs = msgsIn;
		url = urlIn;
		removeUrl = removeUrlIn;
		lastPostUrl = lastPostUrlIn;
	}

}
