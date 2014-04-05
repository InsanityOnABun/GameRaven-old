package com.ioabsoftware.gameraven.views.rowdata;

import android.webkit.WebView;

import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.RowType;

public class AdGFAQsRowData extends BaseRowData {

    private WebView web;

    public WebView getWebView() {
        return web;
    }

    @Override
    public RowType getRowType() {
        return RowType.GFAQS_AD;
    }

    public AdGFAQsRowData(WebView webIn) {
        web = webIn;
    }

}
