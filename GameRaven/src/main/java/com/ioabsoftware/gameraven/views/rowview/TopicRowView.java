package com.ioabsoftware.gameraven.views.rowview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
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
import com.ioabsoftware.gameraven.views.SelectorDrawable;
import com.ioabsoftware.gameraven.views.rowdata.TopicRowData;

public class TopicRowView extends BaseRowView {

    TextView title;
    TextView tc;
    TextView msgLP;
    TextView lpLink;

    ImageView typeIndicator;

    TopicRowData myData;

    private int defaultTitleColor;
    private int defaultTCColor;
    private int defaultMsgLPColor;
    private int defaultLPLinkColor;

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
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.topicview, this, true);

        title = (TextView) findViewById(R.id.tvTitle);
        tc = (TextView) findViewById(R.id.tvTC);
        msgLP = (TextView) findViewById(R.id.tvMsgCountLastPost);
        lpLink = (TextView) findViewById(R.id.tvLastPostLink);

        typeIndicator = (ImageView) findViewById(R.id.tvTypeIndicator);

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

        lpLink.setBackgroundDrawable(new SelectorDrawable(context));
        lpLink.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AllInOneV2.get().enableGoToUrlDefinedPost();
                AllInOneV2.get().getSession().get(NetDesc.TOPIC, myData.getLastPostUrl(), null);
            }
        });

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AllInOneV2.get().getSession().get(NetDesc.TOPIC, myData.getUrl(), null);
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
            int readColor = Theming.usingLightTheme() ? Color.LTGRAY : Color.DKGRAY;
            tc.setTextColor(readColor);
            title.setTextColor(readColor);
            msgLP.setTextColor(readColor);
            lpLink.setTextColor(readColor);
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
        } else {

            tc.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
            title.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
            msgLP.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
            lpLink.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
        }

        switch (myData.getType()) {
            case NORMAL:
                typeIndicator.setVisibility(View.GONE);
                break;
            case POLL:
                setTypeIndicator(Theming.usingLightTheme() ? R.drawable.ic_poll_light : R.drawable.ic_poll);
                break;
            case LOCKED:
                setTypeIndicator(Theming.usingLightTheme() ? R.drawable.ic_locked_light : R.drawable.ic_locked);
                break;
            case ARCHIVED:
                setTypeIndicator(Theming.usingLightTheme() ? R.drawable.ic_archived_light : R.drawable.ic_archived);
                break;
            case PINNED:
                setTypeIndicator(Theming.usingLightTheme() ? R.drawable.ic_pinned_light : R.drawable.ic_pinned);
                break;
        }
    }

    private void setTypeIndicator(int resId) {
        typeIndicator.setImageResource(resId);
        typeIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    protected void drawableStateChanged() {
        lpLink.getBackground().setState(this.getDrawableState());
        super.drawableStateChanged();
    }
}
