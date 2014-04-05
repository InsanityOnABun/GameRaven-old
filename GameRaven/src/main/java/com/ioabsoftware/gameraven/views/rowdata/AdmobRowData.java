package com.ioabsoftware.gameraven.views.rowdata;

import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.RowType;

public class AdmobRowData extends BaseRowData {

    public AdmobRowData() {
    }

    @Override
    public RowType getRowType() {
        return RowType.ADMOB_AD;
    }
}
