package com.ioabsoftware.gameraven.views.rowdata;

import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.RowType;

public class PMDetailRowData extends BaseRowData {

	private String sender, title, message;
	public String getSender() {return sender;}
	public String getTitle() {return title;}
	public String getMessage() {return message;}
	
	private boolean isFromInbox;
	public boolean isFromInbox() {return isFromInbox;}
	
	@Override
	public RowType getRowType() {
		return RowType.PM_DETAIL;
	}
	
	public PMDetailRowData(String senderIn, String titleIn, String messageIn, boolean isFromInboxIn) {
		sender = senderIn;
		title = titleIn;
		message = messageIn;
		isFromInbox = isFromInboxIn;
	}
	
	public String getReplyTitle() {
		if (!title.startsWith("Re: "))
			return "Re: " + title;
		else
			return title;
	}

}
