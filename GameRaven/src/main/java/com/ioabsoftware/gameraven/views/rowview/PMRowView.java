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
import com.ioabsoftware.gameraven.views.rowdata.PMRowData;

public class PMRowView extends BaseRowView {

    private static float subjectTextSize = 0;
    private static float senderTextSize, timeTextSize;
    TextView subject;
    TextView sender;
    TextView time;
    PMRowData myData;
    private int defaultSubjectColor, defaultSenderColor, defaultTimeColor;

    public PMRowView(Context context) {
        super(context);
    }

    public PMRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PMRowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init(Context context) {
        myType = RowType.PM;
        LayoutInflater.from(context).inflate(R.layout.pmview, this, true);

        subject = (TextView) findViewById(R.id.pmSubject);
        sender = (TextView) findViewById(R.id.pmSender);
        time = (TextView) findViewById(R.id.pmTime);

        defaultSubjectColor = subject.getCurrentTextColor();
        defaultSenderColor = sender.getCurrentTextColor();
        defaultTimeColor = time.getCurrentTextColor();

        if (subjectTextSize == 0) {
            subjectTextSize = subject.getTextSize();
            senderTextSize = sender.getTextSize();
            timeTextSize = time.getTextSize();
        }

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myData.isFromInbox())
                    AllInOneV2.get().getSession().get(NetDesc.PM_INBOX_DETAIL, myData.getUrl());
                else
                    AllInOneV2.get().getSession().get(NetDesc.PM_OUTBOX_DETAIL, myData.getUrl());
            }
        });
    }

    @Override
    protected void retheme() {
        subject.setTextSize(PX, subjectTextSize * myScale);
        sender.setTextSize(PX, senderTextSize * myScale);
        time.setTextSize(PX, timeTextSize * myScale);
    }

    @Override
    public void showView(BaseRowData data) {
        if (data.getRowType() != myType)
            throw new IllegalArgumentException("data RowType does not match myType");

        myData = (PMRowData) data;

        subject.setText(myData.getSubject());
        sender.setText(myData.getSender());
        time.setText(myData.getTime());

        if (myData.isOld()) {
            subject.setTextColor(Theming.colorReadTopic());
            sender.setTextColor(Theming.colorReadTopic());
            time.setTextColor(Theming.colorReadTopic());
            subject.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
            sender.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
            time.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
        } else {
            subject.setTextColor(defaultSubjectColor);
            sender.setTextColor(defaultSenderColor);
            time.setTextColor(defaultTimeColor);
            subject.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            sender.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            time.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
    }


}
