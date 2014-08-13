package com.ioabsoftware.gameraven.views.rowdata;

import com.ioabsoftware.gameraven.views.RowType;

public class AMPRowData extends TopicRowData {

    String lpLongPressLink;

    public String getLPLongPressLink() {
        return lpLongPressLink;
    }

    @Override
    public RowType getRowType() {
        return RowType.AMP_TOPIC;
    }

    public AMPRowData(String titleIn, String tcIn, String lastPostIn, String mCountIn,
                      String urlIn, String lPostUrlIn, String ylpUrlIn, ReadStatus statusIn) {
        super(titleIn, tcIn, lastPostIn, mCountIn, urlIn, lPostUrlIn,
                TopicType.NORMAL, statusIn, 0);

        lpLongPressLink = ylpUrlIn;
    }

    @Override
    public String toString() {
        return super.toString() + "\nlpLongPressLink: " + lpLongPressLink;
    }
}
