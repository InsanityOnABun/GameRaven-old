package com.ioabsoftware.gameraven.views.rowview;

import android.content.Context;
import android.text.Html;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.R;
import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.BaseRowView;
import com.ioabsoftware.gameraven.views.RowType;
import com.ioabsoftware.gameraven.views.rowdata.UserDetailRowData;

public class UserDetailRowView extends BaseRowView {

    TextView ID, level, creation, lVisit, karma, amp, sig;
    RelativeLayout sigWrapper;
    Button sendPM;

    UserDetailRowData myData;

    private static float idTextSize = 0;
    private static float levelTextSize, creationTextSize, lVisitTextSize, karmaTextSize, ampTextSize, sigTextSize, sendPMTextSize;

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
        setOrientation(VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.userdetailview, this, true);

        ID = (TextView) findViewById(R.id.udID);
        level = (TextView) findViewById(R.id.udLevel);
        creation = (TextView) findViewById(R.id.udCreation);
        lVisit = (TextView) findViewById(R.id.udLVisit);
        karma = (TextView) findViewById(R.id.udKarma);
        amp = (TextView) findViewById(R.id.udAMP);
        sig = (TextView) findViewById(R.id.udSig);

        sendPM = (Button) findViewById(R.id.udSendPM);

        if (idTextSize == 0) {
            idTextSize = ID.getTextSize();
            levelTextSize = level.getTextSize();
            creationTextSize = creation.getTextSize();
            lVisitTextSize = lVisit.getTextSize();
            karmaTextSize = karma.getTextSize();
            ampTextSize = amp.getTextSize();
            sigTextSize = sig.getTextSize();
            sendPMTextSize = sendPM.getTextSize();
        }

        sigWrapper = (RelativeLayout) findViewById(R.id.udSigWrapper);
        sendPM.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AllInOneV2.get().pmSetup(myData.getName(), null, null);
            }
        });
    }

    @Override
    protected void retheme() {
        ID.setTextSize(PX, idTextSize * myScale);
        level.setTextSize(PX, levelTextSize * myScale);
        creation.setTextSize(PX, creationTextSize * myScale);
        lVisit.setTextSize(PX, lVisitTextSize * myScale);
        karma.setTextSize(PX, karmaTextSize * myScale);
        amp.setTextSize(PX, ampTextSize * myScale);
        sig.setTextSize(PX, sigTextSize * myScale);
        sendPM.setTextSize(PX, sendPMTextSize * myScale);

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

        ID.setText(myData.getID());
        level.setText(Html.fromHtml(myData.getLevel()));
        creation.setText(myData.getCreation());
        lVisit.setText(myData.getLastVisit());
        karma.setText(myData.getKarma());
        amp.setText(myData.getAMP());

        if (myData.getSig() != null) {
            sig.setText(Html.fromHtml(myData.getSig()));
            Linkify.addLinks(sig, Linkify.WEB_URLS);
        } else
            sigWrapper.setVisibility(View.GONE);

        sendPM.setText("Send PM to " + myData.getName());
    }

}
