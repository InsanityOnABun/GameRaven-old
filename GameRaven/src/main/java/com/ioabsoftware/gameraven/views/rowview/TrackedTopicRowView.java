package com.ioabsoftware.gameraven.views.rowview;

import android.content.Context;
import android.graphics.Color;
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
import com.ioabsoftware.gameraven.views.BaseRowData.ReadStatus;
import com.ioabsoftware.gameraven.views.BaseRowView;
import com.ioabsoftware.gameraven.views.RowType;
import com.ioabsoftware.gameraven.views.rowdata.TrackedTopicRowData;

public class TrackedTopicRowView extends BaseRowView {

    TextView board;
    TextView title;
    TextView msgLP;
    TextView lpLink;
    TextView removeLink;

    TrackedTopicRowData myData;

    private int defaultBoardColor;
    private int defaultTitleColor;
    private int defaultMsgLPColor;
    private int defaultLPLinkColor;
    private int defaultRemoveLinkColor;

    private static float titleTextSize = 0;
    private static float boardTextSize, msgLPTextSize, lpLinkTextSize, removeLinkTextSize;

    public TrackedTopicRowView(Context context) {
        super(context);
    }

    public TrackedTopicRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TrackedTopicRowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init(Context context) {
        myType = RowType.TRACKED_TOPIC;
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.trackedtopicview, this, true);

        board = (TextView) findViewById(R.id.ttBoardName);
        title = (TextView) findViewById(R.id.ttTitle);
        msgLP = (TextView) findViewById(R.id.ttMessageCountLastPost);
        lpLink = (TextView) findViewById(R.id.ttLastPostLink);
        removeLink = (TextView) findViewById(R.id.ttStopTracking);

        defaultBoardColor = board.getCurrentTextColor();
        defaultTitleColor = title.getCurrentTextColor();
        defaultMsgLPColor = msgLP.getCurrentTextColor();
        defaultLPLinkColor = lpLink.getCurrentTextColor();
        defaultRemoveLinkColor = removeLink.getCurrentTextColor();

        if (titleTextSize == 0) {
            titleTextSize = title.getTextSize();
            boardTextSize = board.getTextSize();
            msgLPTextSize = msgLP.getTextSize();
            lpLinkTextSize = lpLink.getTextSize();
            removeLinkTextSize = removeLink.getTextSize();
        }

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AllInOneV2.get().getSession().get(NetDesc.TOPIC, myData.getUrl(), null);
            }
        });

        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String url = myData.getUrl().substring(0, myData.getUrl().lastIndexOf('/'));
                AllInOneV2.get().getSession().get(NetDesc.BOARD, url, null);
                return true;
            }
        });

        lpLink.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AllInOneV2.get().enableGoToUrlDefinedPost();
                AllInOneV2.get().getSession().get(NetDesc.TOPIC, myData.getLastPostUrl(), null);
            }
        });

        removeLink.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AllInOneV2.get().getSession().get(NetDesc.TRACKED_TOPICS, myData.getRemoveUrl(), null);
            }
        });

        retheme(Theming.accentColor(), Theming.textScale());
    }

    @Override
    protected void retheme(int color, float scale) {
        board.setTextSize(PX, boardTextSize * scale);
        title.setTextSize(PX, titleTextSize * scale);
        msgLP.setTextSize(PX, msgLPTextSize * scale);
        lpLink.setTextSize(PX, lpLinkTextSize * scale);
        removeLink.setTextSize(PX, removeLinkTextSize * scale);

        findViewById(R.id.ttSep).setBackgroundColor(color);
        findViewById(R.id.ttSTSep).setBackgroundColor(color);
        findViewById(R.id.ttLPSep).setBackgroundColor(color);

        setBackgroundDrawable(getSelector());
        lpLink.setBackgroundDrawable(getSelector());
        removeLink.setBackgroundDrawable(getSelector());
    }

    @Override
    public void showView(BaseRowData data) {
        if (data.getRowType() != myType)
            throw new IllegalArgumentException("data RowType does not match myType");

        myData = (TrackedTopicRowData) data;

        if (myData.getStatus() == ReadStatus.READ) {
            int readColor = Theming.usingLightTheme() ? Color.LTGRAY : Color.DKGRAY;
            board.setTextColor(readColor);
            title.setTextColor(readColor);
            msgLP.setTextColor(readColor);
            lpLink.setTextColor(readColor);
            removeLink.setTextColor(readColor);
        } else {
            board.setTextColor(defaultBoardColor);
            title.setTextColor(defaultTitleColor);
            msgLP.setTextColor(defaultMsgLPColor);
            lpLink.setTextColor(defaultLPLinkColor);
            removeLink.setTextColor(defaultRemoveLinkColor);
        }

        if (myData.getStatus() == ReadStatus.NEW_POST) {
            board.setTypeface(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
            title.setTypeface(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
            msgLP.setTypeface(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
            lpLink.setTypeface(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
            removeLink.setTypeface(Typeface.DEFAULT, Typeface.BOLD_ITALIC);
        } else {

            board.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
            title.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
            msgLP.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
            lpLink.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
            removeLink.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
        }

        board.setText(myData.getBoard());
        title.setText(myData.getTitle());
        msgLP.setText(myData.getMsgs() + " Msgs, Last: " + myData.getLastPost());
    }

}
