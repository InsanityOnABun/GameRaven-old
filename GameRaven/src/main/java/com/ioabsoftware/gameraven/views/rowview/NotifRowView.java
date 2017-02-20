package com.ioabsoftware.gameraven.views.rowview;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.R;
import com.ioabsoftware.gameraven.networking.NetDesc;
import com.ioabsoftware.gameraven.util.Theming;
import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.BaseRowView;
import com.ioabsoftware.gameraven.views.RowType;
import com.ioabsoftware.gameraven.views.rowdata.NotifRowData;
import com.ioabsoftware.gameraven.views.rowdata.PMRowData;

public class NotifRowView extends BaseRowView {

    private static float titleTextSize = 0;
    private static float timeTextSize;
    TextView title;
    TextView time;
    NotifRowData myData;
    private int defaultTitleColor, defaultTimeColor;

    public NotifRowView(Context context) {
        super(context);
    }

    public NotifRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NotifRowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init(Context context) {
        myType = RowType.NOTIF;
        LayoutInflater.from(context).inflate(R.layout.notifview, this, true);

        title = (TextView) findViewById(R.id.notifTitle);
        time = (TextView) findViewById(R.id.notifTime);

        defaultTitleColor = title.getCurrentTextColor();
        defaultTimeColor = time.getCurrentTextColor();

        if (titleTextSize == 0) {
            titleTextSize = title.getTextSize();
            timeTextSize = time.getTextSize();
        }

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AllInOneV2.get().processNotif(myData.getTitle(), myData.getUrl());
            }
        });
    }

    @Override
    protected void retheme() {
        title.setTextSize(PX, titleTextSize * myScale);
        time.setTextSize(PX, timeTextSize * myScale);
    }

    @Override
    public void showView(BaseRowData data) {
        if (data.getRowType() != myType)
            throw new IllegalArgumentException("data RowType does not match myType");

        myData = (NotifRowData) data;

        title.setText(myData.getTitle());
        time.setText(myData.getTime());

        if (myData.isOld()) {
            title.setTextColor(Theming.colorReadTopic());
            time.setTextColor(Theming.colorReadTopic());
            title.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
            time.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
        } else {
            title.setTextColor(defaultTitleColor);
            time.setTextColor(defaultTimeColor);
            title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            time.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
    }


}
