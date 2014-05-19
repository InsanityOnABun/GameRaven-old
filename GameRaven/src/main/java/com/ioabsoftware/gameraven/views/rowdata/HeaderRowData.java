package com.ioabsoftware.gameraven.views.rowdata;

import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.RowType;

public class HeaderRowData extends BaseRowData {

    private String headerText;

    public String getHeaderText() {
        return headerText;
    }

    @Override
    public RowType getRowType() {
        return RowType.HEADER;
    }

    public HeaderRowData(String text) {
        headerText = text;
    }

    @Override
    public String toString() {
        return "headerText: " + headerText;
    }

}
