package com.ioabsoftware.gameraven.views.rowdata;

import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.RowType;

public class PMDetailRowData extends BaseRowData {

    private String sender, title, message;

    public String getSender() {
        return sender;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public RowType getRowType() {
        return RowType.PM_DETAIL;
    }

    public PMDetailRowData(String senderIn, String titleIn, String messageIn) {
        sender = senderIn;
        title = titleIn;
        message = messageIn;
    }

}
