package com.ioabsoftware.gameraven.views.rowview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.BaseRowView;
import com.ioabsoftware.gameraven.views.RowType;
import com.ioabsoftware.gameraven.views.rowdata.AdGFAQsRowData;

public class AdGFAQsRowView extends BaseRowView {

    public AdGFAQsRowView(Context context) {
        super(context);
    }

    public AdGFAQsRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdGFAQsRowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void init(Context context) {
        myType = RowType.GFAQS_AD;
        setOrientation(VERTICAL);
    }

    @Override
    protected void retheme() {
        // Nothing to theme here.
    }

    @Override
    protected void showView(BaseRowData data) {
        if (data.getRowType() != myType)
            throw new IllegalArgumentException("data RowType does not match myType");

        View view = ((AdGFAQsRowData) data).getWebView();

        if (view.getParent() != this) {
            if (view.getParent() != null) {
                        ((ViewGroup) view.getParent()).removeView(view);
            }
            addView(view);
        }
    }

}
