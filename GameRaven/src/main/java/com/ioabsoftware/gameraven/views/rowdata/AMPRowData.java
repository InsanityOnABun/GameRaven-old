package com.ioabsoftware.gameraven.views.rowdata;

import com.ioabsoftware.gameraven.views.RowType;

public class AMPRowData extends TopicRowData {

    String ylpUrl;

    public String getYLPUrl() {
        return ylpUrl;
    }

    @Override
    public RowType getRowType() {
        return RowType.AMP_TOPIC;
    }

    public AMPRowData(String titleIn, String tcIn, String lastPostIn,
                      String mCountIn, String urlIn, String lPostUrlIn, String ylpUrlIn) {
        super(titleIn, tcIn, lastPostIn, mCountIn, urlIn, lPostUrlIn,
                TopicType.NORMAL, ReadStatus.UNREAD, 0);

        ylpUrl = ylpUrlIn;
    }

}
