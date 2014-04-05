package com.ioabsoftware.gameraven.views.rowview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;

import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.BaseRowView;
import com.ioabsoftware.gameraven.views.RowType;
import com.ioabsoftware.gameraven.views.rowdata.AdRowData;

public class AdRowView extends BaseRowView {

    public AdRowView(Context context, BaseRowData data) {
        super(context);

        if (data.getRowType() != myType)
            throw new IllegalArgumentException("data RowType does not match myType");

        addView(((AdRowData) data).getWebView());
    }

    public AdRowView(Context context) {
        super(context);
    }

    public AdRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdRowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void init(Context context) {
        myType = RowType.AD;
        setOrientation(VERTICAL);
    }

    @Override
    protected void retheme(int color, float scale) {
        // Nothing to theme here.
    }

    @Override
    protected void showView(BaseRowData data) {
        /*
         * There is only ever one ad in the list, so there is no view updating
		 * needed. Moreover, we need to load the ad into the WebView immediately,
		 * not when it actually comes into view. That way GFAQs can't complain
		 * that their ads aren't being loaded ;)
		 */
    }

}
