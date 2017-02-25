package com.ioabsoftware.gameraven.views.rowview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.R;
import com.ioabsoftware.gameraven.networking.NetDesc;
import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.BaseRowView;
import com.ioabsoftware.gameraven.views.RowType;
import com.ioabsoftware.gameraven.views.rowdata.MentionRowData;

public class MentionRowView extends BaseRowView {

    private static float topicTextSize = 0;
    private static float timeTextSize;
    TextView topic, board, user, time;
    MentionRowData myData;

    public MentionRowView(Context context) {
        super(context);
    }

    public MentionRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MentionRowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init(Context context) {
        myType = RowType.MENTION;
        LayoutInflater.from(context).inflate(R.layout.mentionview, this, true);

        topic = (TextView) findViewById(R.id.mentionTopic);
        board = (TextView) findViewById(R.id.mentionBoard);
        user = (TextView) findViewById(R.id.mentionUser);
        time = (TextView) findViewById(R.id.mentionTime);

        if (topicTextSize == 0) {
            topicTextSize = topic.getTextSize();
            timeTextSize = time.getTextSize();
        }

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AllInOneV2.get().enableGoToUrlDefinedPost();
                AllInOneV2.get().getSession().get(NetDesc.TOPIC, myData.getUrl());
            }
        });
    }

    @Override
    protected void retheme() {
        topic.setTextSize(PX, topicTextSize * myScale);
        time.setTextSize(PX, timeTextSize * myScale);
        board.setTextSize(PX, timeTextSize * myScale);
        user.setTextSize(PX, timeTextSize * myScale);
    }

    @Override
    public void showView(BaseRowData data) {
        if (data.getRowType() != myType)
            throw new IllegalArgumentException("data RowType does not match myType");

        myData = (MentionRowData) data;
        Log.d("asdfasdf", myData.getUrl());

        topic.setText(myData.getTopic());
        board.setText(myData.getBoard());
        user.setText(myData.getUser());
        time.setText(myData.getTime());
    }
}
