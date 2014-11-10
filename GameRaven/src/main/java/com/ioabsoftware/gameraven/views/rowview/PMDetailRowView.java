package com.ioabsoftware.gameraven.views.rowview;

import android.content.Context;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.ArrowKeyMovementMethod;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.ioabsoftware.gameraven.R;
import com.ioabsoftware.gameraven.util.RichTextUtils;
import com.ioabsoftware.gameraven.util.UrlSpanConverter;
import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.BaseRowView;
import com.ioabsoftware.gameraven.views.RowType;
import com.ioabsoftware.gameraven.views.rowdata.PMDetailRowData;

public class PMDetailRowView extends BaseRowView {

    TextView messageView;
    private static float messageTextSize = 0;

    PMDetailRowData myData;

    public PMDetailRowView(Context context) {
        super(context);
    }

    public PMDetailRowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PMDetailRowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init(Context context) {
        myType = RowType.PM_DETAIL;
        LayoutInflater.from(context).inflate(R.layout.pmdetailview, this, true);

        messageView = (TextView) findViewById(R.id.pmdMessage);

        if (messageTextSize == 0)
            messageTextSize = messageView.getTextSize();
    }

    @Override
    protected void retheme() {
        messageView.setTextSize(PX, messageTextSize * myScale);

        messageView.setLinkTextColor(myColor);
    }

    @Override
    public void showView(BaseRowData data) {
        if (data.getRowType() != myType)
            throw new IllegalArgumentException("data RowType does not match myType");

        myData = (PMDetailRowData) data;

        messageView.setText(RichTextUtils.replaceAll(linkifyHtml(myData.getMessage(), Linkify.WEB_URLS), URLSpan.class, new UrlSpanConverter()));

        messageView.setMovementMethod(ArrowKeyMovementMethod.getInstance());
        messageView.setTextIsSelectable(true);
        // the autoLink attribute must be removed, if you hasn't set it then ok, otherwise call textView.setAutoLink(0);
    }

    public static Spannable linkifyHtml(String html, int linkifyMask) {
        Spanned text = Html.fromHtml(html);
        URLSpan[] currentSpans = text.getSpans(0, text.length(), URLSpan.class);

        SpannableString buffer = new SpannableString(text);
        Linkify.addLinks(buffer, linkifyMask);

        for (URLSpan span : currentSpans) {
            int end = text.getSpanEnd(span);
            int start = text.getSpanStart(span);
            buffer.setSpan(span, start, end, 0);
        }
        return buffer;
    }

}
