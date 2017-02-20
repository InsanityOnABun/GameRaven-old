package com.ioabsoftware.gameraven.views.rowview;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.R;
import com.ioabsoftware.gameraven.networking.NetDesc;
import com.ioabsoftware.gameraven.util.Theming;
import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.BaseRowData.ReadStatus;
import com.ioabsoftware.gameraven.views.BaseRowView;
import com.ioabsoftware.gameraven.views.RowType;
import com.ioabsoftware.gameraven.views.rowdata.TopicRowData;

public class TopicRowView extends BaseRowView {

    TextView title;
    TextView tc;
    TextView msgLP;
    TextView lpLink;

    TextView typeIndicator;

    TopicRowData myData;

    private int defaultTitleColor, defaultTCColor, defaultMsgLPColor, defaultLPLinkColor;

    private static float titleTextSize = 0;
    private static float tcTextSize, msgLPTextSize, lpLinkTextSize;

    public TopicRowView(Context context) {
        super(context);
    }

    public TopicRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TopicRowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init(Context context) {
        myType = RowType.TOPIC;
        LayoutInflater.from(context).inflate(R.layout.topicview, this, true);

        title = (TextView) findViewById(R.id.tvTitle);
        tc = (TextView) findViewById(R.id.tvTC);
        msgLP = (TextView) findViewById(R.id.tvMsgCountLastPost);
        lpLink = (TextView) findViewById(R.id.tvLastPostLink);

        typeIndicator = (TextView) findViewById(R.id.tvTypeIndicator);

        defaultTitleColor = title.getCurrentTextColor();
        defaultTCColor = tc.getCurrentTextColor();
        defaultMsgLPColor = msgLP.getCurrentTextColor();
        defaultLPLinkColor = lpLink.getCurrentTextColor();

        if (titleTextSize == 0) {
            titleTextSize = title.getTextSize();
            tcTextSize = tc.getTextSize();
            msgLPTextSize = msgLP.getTextSize();
            lpLinkTextSize = lpLink.getTextSize();
        }

        lpLink.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AllInOneV2.get().enableGoToUrlDefinedPost();
                AllInOneV2.get().getSession().get(NetDesc.TOPIC, myData.getLastPostUrl());
            }
        });

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AllInOneV2.get().getSession().get(NetDesc.TOPIC, myData.getUrl());
            }
        });
    }

    @Override
    protected void retheme() {
        title.setTextSize(PX, titleTextSize * myScale);
        tc.setTextSize(PX, tcTextSize * myScale);
        msgLP.setTextSize(PX, msgLPTextSize * myScale);
        lpLink.setTextSize(PX, lpLinkTextSize * myScale);
    }

    @Override
    public void showView(BaseRowData data) {
        if (data.getRowType() != myType)
            throw new IllegalArgumentException("data RowType does not match myType");

        myData = (TopicRowData) data;

        title.setText(myData.getTitle());
        tc.setText(myData.getTC());
        msgLP.setText(myData.getMCount() + " Msgs, Last: " + myData.getLastPost());

        int hlColor = myData.getHLColor();
        if (myData.getStatus() == ReadStatus.READ) {
            tc.setTextColor(Theming.colorReadTopic());
            title.setTextColor(Theming.colorReadTopic());
            msgLP.setTextColor(Theming.colorReadTopic());
            lpLink.setTextColor(Theming.colorReadTopic());
        } else if (hlColor != 0) {
            tc.setTextColor(hlColor);
            title.setTextColor(hlColor);
            msgLP.setTextColor(hlColor);
            lpLink.setTextColor(hlColor);
        } else {
            tc.setTextColor(defaultTCColor);
            title.setTextColor(defaultTitleColor);
            msgLP.setTextColor(defaultMsgLPColor);
            lpLink.setTextColor(defaultLPLinkColor);
        }

        if (myData.getStatus() == ReadStatus.NEW_POST) {
            tc.setTypeface(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
            title.setTypeface(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
            msgLP.setTypeface(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
            lpLink.setTypeface(Typeface.DEFAULT, Typeface.BOLD_ITALIC);

            lpLink.setText(R.string.last_unread_post);
        } else {
            tc.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
            title.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
            msgLP.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
            lpLink.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);

            lpLink.setText(R.string.last_post);
        }

        switch (myData.getType()) {
            case NORMAL:
                typeIndicator.setVisibility(View.GONE);
                break;
            case POLL:
                setTypeIndicator(Theming.topicStatusIcons()[0]);
                break;
            case LOCKED:
                setTypeIndicator(Theming.topicStatusIcons()[1]);
                break;
            case ARCHIVED:
                setTypeIndicator(Theming.topicStatusIcons()[2]);
                break;
            case PINNED:
                setTypeIndicator(Theming.topicStatusIcons()[3]);
                break;
        }
    }

    private void setTypeIndicator(Drawable icon) {
        typeIndicator.setBackgroundDrawable(icon);
        typeIndicator.setVisibility(View.VISIBLE);
    }
}
