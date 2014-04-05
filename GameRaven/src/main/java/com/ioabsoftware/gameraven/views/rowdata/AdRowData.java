package com.ioabsoftware.gameraven.views.rowdata;

import android.webkit.WebView;

import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.RowType;

public class AdRowData extends BaseRowData {

    private WebView web;

    public WebView getWebView() {
        return web;
    }

    @Override
    public RowType getRowType() {
        return RowType.AD;
    }

    public AdRowData(WebView webIn) {
        web = webIn;
    }

}
