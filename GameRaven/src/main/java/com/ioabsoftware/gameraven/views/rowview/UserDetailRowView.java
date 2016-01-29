package com.ioabsoftware.gameraven.views.rowview;

import android.content.Context;
import android.text.Html;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.ioabsoftware.gameraven.R;
import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.BaseRowView;
import com.ioabsoftware.gameraven.views.RowType;
import com.ioabsoftware.gameraven.views.rowdata.UserDetailRowData;

public class UserDetailRowView extends BaseRowView {

    TextView tag, ID, level, creation, lVisit, karma, amp, sig;

    UserDetailRowData myData;

    private static float textSize = 0;

    public UserDetailRowView(Context context) {
        super(context);
    }

    public UserDetailRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UserDetailRowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init(Context context) {
        myType = RowType.USER_DETAIL;
        LayoutInflater.from(context).inflate(R.layout.userdetailview, this, true);

        tag = (TextView) findViewById(R.id.udTag);
        ID = (TextView) findViewById(R.id.udID);
        level = (TextView) findViewById(R.id.udLevel);
        creation = (TextView) findViewById(R.id.udCreation);
        lVisit = (TextView) findViewById(R.id.udLVisit);
        karma = (TextView) findViewById(R.id.udKarma);
        amp = (TextView) findViewById(R.id.udAMP);
        sig = (TextView) findViewById(R.id.udSig);

        if (textSize == 0)
            textSize = ID.getTextSize();
    }

    @Override
    protected void retheme() {
        tag.setTextSize(PX, textSize * myScale);
        ID.setTextSize(PX, textSize * myScale);
        level.setTextSize(PX, textSize * myScale);
        creation.setTextSize(PX, textSize * myScale);
        lVisit.setTextSize(PX, textSize * myScale);
        karma.setTextSize(PX, textSize * myScale);
        amp.setTextSize(PX, textSize * myScale);
        sig.setTextSize(PX, textSize * myScale);

        findViewById(R.id.udTagSep).setBackgroundColor(myColor);
        findViewById(R.id.udIDSep).setBackgroundColor(myColor);
        findViewById(R.id.udLevelSep).setBackgroundColor(myColor);
        findViewById(R.id.udCreationSep).setBackgroundColor(myColor);
        findViewById(R.id.udLVisitSep).setBackgroundColor(myColor);
        findViewById(R.id.udSigSep).setBackgroundColor(myColor);
        findViewById(R.id.udKarmaSep).setBackgroundColor(myColor);
        findViewById(R.id.udAMPSep).setBackgroundColor(myColor);

        sig.setLinkTextColor(myColor);
    }

    @Override
    public void showView(BaseRowData data) {
        if (data.getRowType() != myType)
            throw new IllegalArgumentException("data RowType does not match myType");

        myData = (UserDetailRowData) data;

        if (!myData.getTagText().isEmpty()) {
            findViewById(R.id.udTagWrapper).setVisibility(VISIBLE);
            tag.setText(myData.getTagText());
        } else
            findViewById(R.id.udTagWrapper).setVisibility(GONE);

        ID.setText(myData.getID());
        level.setText(Html.fromHtml(myData.getLevel()));
        creation.setText(myData.getCreation());
        lVisit.setText(myData.getLastVisit());
        karma.setText(myData.getKarma());
        amp.setText(myData.getAMP());

        if (myData.getSig() != null) {
            findViewById(R.id.udSigWrapper).setVisibility(VISIBLE);
            sig.setText(Html.fromHtml(myData.getSig()));
            Linkify.addLinks(sig, Linkify.WEB_URLS);
        } else
            findViewById(R.id.udSigWrapper).setVisibility(GONE);
    }

}
