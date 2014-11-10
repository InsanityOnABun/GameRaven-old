package com.ioabsoftware.gameraven.views.rowview;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.ioabsoftware.gameraven.R;
import com.ioabsoftware.gameraven.util.Theming;
import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.BaseRowView;
import com.ioabsoftware.gameraven.views.RowType;
import com.ioabsoftware.gameraven.views.rowdata.HeaderRowData;

public class HeaderRowView extends BaseRowView {

    private TextView tView;

    private static float tSize = 0;

    private Drawable back;

    public HeaderRowView(Context context) {
        super(context);
    }

    public HeaderRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeaderRowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void init(Context context) {
        myType = RowType.HEADER;
        LayoutInflater.from(context).inflate(R.layout.headerview, this, true);
        tView = (TextView) findViewById(R.id.hdrText);

        back = tView.getBackground();

        if (tSize == 0)
            tSize = tView.getTextSize();
    }

    @Override
    protected void retheme() {
        tView.setTextSize(PX, tSize * myScale);
        tView.setTextColor(Theming.accentTextColor());

        back.setColorFilter(myColor, PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public void showView(BaseRowData data) {
        if (data.getRowType() != myType)
            throw new IllegalArgumentException("data RowType does not match myType");

        tView.setText(((HeaderRowData) data).getHeaderText());
    }

}
