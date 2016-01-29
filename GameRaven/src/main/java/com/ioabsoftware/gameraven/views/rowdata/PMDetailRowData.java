package com.ioabsoftware.gameraven.views.rowdata;

import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.URLSpan;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.MessageLinkSpan;
import com.ioabsoftware.gameraven.views.RowType;

public class PMDetailRowData extends BaseRowData {

    private String sender, title;
    private SpannableString message;

    public String getSender() {
        return sender;
    }

    public String getTitle() {
        return title;
    }

    public SpannableString getMessage() {
        return message;
    }

    @Override
    public RowType getRowType() {
        return RowType.PM_DETAIL;
    }

    public PMDetailRowData(String senderIn, String titleIn, String messageIn) {
        sender = senderIn;
        title = titleIn;
        message = linkifyHtml(messageIn);
    }

    @Override
    public String toString() {
        return "title: " + title +
                "\nsender: " + sender +
                "\nmessage: " + message;
    }

    public static SpannableString linkifyHtml(String html) {
        Spanned spanned = Html.fromHtml(html);
        SpannableString text = new SpannableString(spanned);

        URLSpan[] old = text.getSpans(0, text.length(), URLSpan.class);
        for (int i = old.length - 1; i >= 0; i--) {
            int start = text.getSpanStart(old[i]);
            int end = text.getSpanEnd(old[i]);
            String url = old[i].getURL();
            text.removeSpan(old[i]);
            text.setSpan(new MessageLinkSpan(url, AllInOneV2.get()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        }

        return text;
    }
}
