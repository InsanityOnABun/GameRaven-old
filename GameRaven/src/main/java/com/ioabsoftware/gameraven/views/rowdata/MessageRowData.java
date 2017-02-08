package com.ioabsoftware.gameraven.views.rowdata;

import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.UnderlineSpan;
import android.text.util.Linkify;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.BuildConfig;
import com.ioabsoftware.gameraven.R;
import com.ioabsoftware.gameraven.networking.NetDesc;
import com.ioabsoftware.gameraven.networking.Session;
import com.ioabsoftware.gameraven.util.MyLinkifier;
import com.ioabsoftware.gameraven.util.Theming;
import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.GRQuoteSpan;
import com.ioabsoftware.gameraven.views.RowType;
import com.ioabsoftware.gameraven.views.SpoilerBackgroundSpan;
import com.ioabsoftware.gameraven.views.SpoilerClickSpan;
import com.ioabsoftware.gameraven.views.rowview.HeaderRowView;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MessageRowData extends BaseRowData {

    private String username, userTitles, avatarUrl, postNum, postTime, messageID, boardID, topicID;
    private final String unprocessedMessageText;

    private LinearLayout poll = null;

    private Spannable spannedMessage;

    private int hlColor;

    private boolean topClickable = true;
    private boolean isDeleted = false;

    private boolean canReport = false, canDelete = false, canEdit = false, canQuote = false;

    @Override
    public String toString() {
        return "username: " + username +
                "\nuserTitles: " + userTitles +
                "\nhlColor: " + hlColor +
                "\npostNum: " + postNum +
                "\npostTime: " + postTime +
                "\nmessageID: " + messageID +
                "\nboardID: " + boardID +
                "\ntopicID: " + topicID +
                "\nhasPoll: " + hasPoll() +
                "\nunprocessedMessageText: " + unprocessedMessageText +
                "\nspannedMessage: " + spannedMessage;
    }

    public void disableTopClick() {
        topClickable = false;
    }

    public boolean topClickable() {
        return topClickable;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public String getUser() {
        return username;
    }

    public String getUserTitles() {
        return userTitles;
    }

    public boolean hasTitles() {
        return userTitles != null;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getPostNum() {
        return postNum;
    }

    public String getPostTime() {
        return postTime;
    }

    public String getMessageID() {
        return messageID;
    }

    public boolean hasMsgID() {
        return messageID != null;
    }

    public String getTopicID() {
        return topicID;
    }

    public String getBoardID() {
        return boardID;
    }

    public boolean canReport() {
        return canReport;
    }

    public boolean canDelete() {
        return canDelete;
    }

    public boolean canEdit() {
        return canEdit;
    }

    public boolean canQuote() {
        return canQuote;
    }

    public String getUnprocessedMessageText() {
        return unprocessedMessageText;
    }

    public LinearLayout getPoll() {
        if (poll.getParent() != null)
            ((ViewGroup) poll.getParent()).removeView(poll);

        return poll;
    }

    public boolean hasPoll() {
        return poll != null;
    }

    public Spannable getSpannedMessage() {
        return spannedMessage;
    }

    public int getHLColor() {
        return hlColor;
    }

    public String getMessageDetailLink() {
        return Session.ROOT + "/boards/" + boardID + "/" + topicID + "/" + messageID;
    }

    public String getUserDetailLink() {
        return Session.ROOT + "/community/" + username.replace(' ', '+') + "/boards";
    }

    private static AllInOneV2 aio = null;

    @Override
    public RowType getRowType() {
        return RowType.MESSAGE;
    }

    public MessageRowData(boolean isDeletedIn, String postNumIn) {
        isDeleted = isDeletedIn;
        postNum = postNumIn;
        unprocessedMessageText = "";
    }

    public MessageRowData(String userIn, String userTitlesIn, String avatarUrlIn, String postNumIn,
                          String postTimeIn, Element messageIn, String BID, String TID, String MID,
                          int hlColorIn, boolean cReport, boolean cDelete, boolean cEdit, boolean cQuote) {

        if (aio == null || aio != AllInOneV2.get())
            aio = AllInOneV2.get();

        if (BuildConfig.DEBUG) AllInOneV2.wtl("setting values");
        username = userIn;
        userTitles = userTitlesIn;
        avatarUrl = avatarUrlIn;
        postNum = postNumIn;
        postTime = postTimeIn.replace('\u00A0', ' ');
        boardID = BID;
        topicID = TID;
        messageID = MID;
        hlColor = hlColorIn;

        canReport = cReport;
        canDelete = cDelete;
        canEdit = cEdit;
        canQuote = cQuote;

        String sigHtml = "";
        Element sig = messageIn.select("div.sig_text").first();
        if (sig != null) {
            sigHtml = "<br />---<br />" + sig.html();
            messageIn.select("div.signature").remove();
        }

        if (!Session.isLoggedIn())
            messageIn.select("div.message_mpu").remove();

        if (BuildConfig.DEBUG) AllInOneV2.wtl("checking for poll");
        if (!messageIn.getElementsByClass("board_poll").isEmpty()) {
            if (BuildConfig.DEBUG) AllInOneV2.wtl("there is a poll");

            Element pollElem = messageIn.getElementsByClass("board_poll").first();

            poll = new LinearLayout(aio);
            poll.setOrientation(LinearLayout.VERTICAL);

            LinearLayout pollInnerWrapper = new LinearLayout(aio);
            pollInnerWrapper.setPadding(15, 0, 15, 15);
            pollInnerWrapper.setOrientation(LinearLayout.VERTICAL);

            Drawable s = aio.getResources().getDrawable(R.drawable.item_background);
            s.setColorFilter(Theming.colorPrimary(), PorterDuff.Mode.SRC_ATOP);
            poll.setBackgroundDrawable(s);

            HeaderRowView h = new HeaderRowView(aio);
            h.showView(new HeaderRowData(pollElem.getElementsByClass("poll_head").first().text()));
            poll.addView(h);

            poll.addView(pollInnerWrapper);

            if (pollElem.getElementsByTag("form").isEmpty()) {
                // poll has been voted in
                // poll_foot_left
                TextView t;
                for (Element e : pollElem.select("div.row")) {
                    Elements c = e.children();
                    t = new TextView(aio);
                    String text = c.get(0).text() + ": " + c.get(1).text();
                    if (!c.get(0).children().isEmpty()) {
                        SpannableStringBuilder votedFor = new SpannableStringBuilder(text);
                        votedFor.setSpan(new StyleSpan(Typeface.BOLD), 0, text.length(), 0);
                        votedFor.setSpan(new ForegroundColorSpan(Theming.colorPrimary()), 0, text.length(), 0);
                        t.setText(votedFor);
                    } else
                        t.setText(text);

                    pollInnerWrapper.addView(t);
                }

                String foot = pollElem.getElementsByClass("poll_foot_left").text();
                if (foot.length() > 0) {
                    t = new TextView(aio);
                    t.setText(foot);
                    pollInnerWrapper.addView(t);
                }

            } else {
                // poll has not been voted in
                final String action = "/boards/" + boardID + "/" + topicID;
                String key = pollElem.getElementsByAttributeValue("name", "key").attr("value");

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, Theming.convertDPtoPX(aio, 1));
                int x = 0;
                for (Element e : pollElem.getElementsByAttributeValue("name", "poll_vote")) {
                    if (x > 0) {
                        View v = new View(aio);
                        v.setLayoutParams(lp);
                        v.setBackgroundColor(Theming.colorPrimary());
                        pollInnerWrapper.addView(v);
                    }
                    x++;
                    Button b = new Button(aio);
                    b.setBackgroundDrawable(Theming.selectableItemBackground());
                    b.setText(e.nextElementSibling().text());
                    final HashMap<String, List<String>> data = new HashMap<String, List<String>>();
                    data.put("key", Collections.singletonList(key));
                    data.put("poll_vote", Collections.singletonList(Integer.toString(x)));
                    data.put("submit", Collections.singletonList("Vote"));

                    b.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            aio.getSession().post(NetDesc.TOPIC, action, data);
                        }
                    });
                    pollInnerWrapper.addView(b);
                }

                View v = new View(aio);
                v.setLayoutParams(lp);
                v.setBackgroundColor(Theming.colorPrimary());
                pollInnerWrapper.addView(v);

                Button b = new Button(aio);
                b.setBackgroundDrawable(Theming.selectableItemBackground());
                b.setText(R.string.view_results);
                b.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        aio.getSession().get(NetDesc.TOPIC, action + "?results=1");
                    }
                });
                pollInnerWrapper.addView(b);
            }

            // remove the poll element so it doesn't get put in unprocessedMessageText
            messageIn.getElementsByClass("board_poll").first().remove();
        }

        unprocessedMessageText = messageIn.html() + sigHtml;

        if (BuildConfig.DEBUG) AllInOneV2.wtl("creating ssb");
        SpannableStringBuilder ssb = new SpannableStringBuilder(processContent(false, true));

        if (BuildConfig.DEBUG) AllInOneV2.wtl("adding bold spans");
        addGenericSpans(ssb, "<b>", "</b>", new StyleSpan(Typeface.BOLD));
        if (BuildConfig.DEBUG) AllInOneV2.wtl("adding italic spans");
        addGenericSpans(ssb, "<i>", "</i>", new StyleSpan(Typeface.ITALIC));
        if (BuildConfig.DEBUG) AllInOneV2.wtl("adding code spans");
        addGenericSpans(ssb, "<code>", "</code>", new TypefaceSpan("monospace"));
        if (BuildConfig.DEBUG) AllInOneV2.wtl("adding cite spans");
        addGenericSpans(ssb, "<cite>", "</cite>", new UnderlineSpan(), new StyleSpan(Typeface.ITALIC));
        if (BuildConfig.DEBUG) AllInOneV2.wtl("adding quote spans");
        addQuoteSpans(ssb);

        ssb.append('\n');

        if (BuildConfig.DEBUG) AllInOneV2.wtl("linkifying");
        MyLinkifier.addLinks(ssb, Linkify.WEB_URLS);

        if (BuildConfig.DEBUG) AllInOneV2.wtl("adding spoiler spans");
        addSpoilerSpans(ssb);

        if (BuildConfig.DEBUG) AllInOneV2.wtl("replacing &lt; with <");
        while (ssb.toString().contains("&lt;")) {
            int start = ssb.toString().indexOf("&lt;");
            ssb.replace(start, start + "&lt;".length(), "<");
        }

        if (BuildConfig.DEBUG) AllInOneV2.wtl("replacing &gt; with >");
        while (ssb.toString().contains("&gt;")) {
            int start = ssb.toString().indexOf("&gt;");
            ssb.replace(start, start + "&gt;".length(), ">");
        }

        if (BuildConfig.DEBUG) AllInOneV2.wtl("setting spannedMessage");
        spannedMessage = ssb;
    }

    public boolean isEdited() {
        return userTitles != null && userTitles.contains("(edited)");
    }

    private static void addGenericSpans(SpannableStringBuilder ssb, String tag, String endTag, CharacterStyle... cs) {
        // initialize array
        int[] startEnd = spanStartAndEnd(ssb.toString(), tag, endTag);

        // while start and end points are found...
        while (!Arrays.equals(startEnd, noStartEndBase)) {
            // remove the start tag
            ssb.delete(startEnd[0], startEnd[0] + tag.length());

            // adjust end point for removed start tag
            startEnd[1] -= tag.length();

            // remove end tag
            ssb.delete(startEnd[1], startEnd[1] + endTag.length());

            // apply styles
            for (CharacterStyle c : cs)
                ssb.setSpan(CharacterStyle.wrap(c), startEnd[0], startEnd[1], 0);

            // get new start and end points
            startEnd = spanStartAndEnd(ssb.toString(), tag, endTag);
        }
    }

    public static final String QUOTE_START = "<blockquote>";
    public static final String QUOTE_END = "</blockquote>";

    private static void addQuoteSpans(SpannableStringBuilder ssb) {
        // initialize array
        int[] startEnd = spanStartAndEnd(ssb.toString(), QUOTE_START, QUOTE_END);

        // while start and end points are found...
        while (!Arrays.equals(startEnd, noStartEndBase)) {
            // replace the start tag
            ssb.replace(startEnd[0], startEnd[0] + QUOTE_START.length(), "\n");
            startEnd[0]++;

            // adjust end point for replaced start tag
            startEnd[1] -= QUOTE_START.length() - 1;

            // remove end tag
            ssb.replace(startEnd[1], startEnd[1] + QUOTE_END.length(), "\n");

            // apply style
            ssb.setSpan(new GRQuoteSpan(), startEnd[0], startEnd[1], 0);

            // get new start and end points
            startEnd = spanStartAndEnd(ssb.toString(), QUOTE_START, QUOTE_END);
        }
    }

    public static final String SPOILER_START = "<s>";
    public static final String SPOILER_END = "</s>";

    private void addSpoilerSpans(SpannableStringBuilder ssb) {
        // initialize array
        int[] startEnd = spanStartAndEnd(ssb.toString(), SPOILER_START, SPOILER_END);

        // while start and end points are found...
        while (!Arrays.equals(startEnd, noStartEndBase)) {
            // remove the start tag
            ssb.delete(startEnd[0], startEnd[0] + SPOILER_START.length());

            // adjust end point for removed start tag
            startEnd[1] -= SPOILER_START.length();

            // remove end tag
            ssb.delete(startEnd[1], startEnd[1] + SPOILER_END.length());

            // apply styles
            SpoilerBackgroundSpan spoiler = new SpoilerBackgroundSpan(Theming.colorHiddenSpoiler(), Theming.colorRevealedSpoiler());
            SpoilerClickSpan spoilerClick = new SpoilerClickSpan(spoiler);
            ssb.setSpan(spoiler, startEnd[0], startEnd[1], 0);
            ssb.setSpan(spoilerClick, startEnd[0], startEnd[1], 0);

            // get new start and end points
            startEnd = spanStartAndEnd(ssb.toString(), SPOILER_START, SPOILER_END);
        }
    }

    private static int[] noStartEndBase = {-1, -1};

    private static int[] spanStartAndEnd(String text, String openTag, String closeTag) {
        int start = -1;
        int end = -1;
        if (text.contains(openTag) && text.contains(closeTag)) {
            start = text.indexOf(openTag);
            end = text.indexOf(closeTag);

            int stackCount = 1;
            int closer;
            int opener;
            int innerStartPoint = start;
            do {
                opener = text.indexOf(openTag, innerStartPoint + 1);
                closer = text.indexOf(closeTag, innerStartPoint + 1);
                if (opener != -1 && opener < closer) {
                    // found a nested tag
                    stackCount++;
                    innerStartPoint = opener;
                } else {
                    // this closer is the right one
                    stackCount--;
                    innerStartPoint = closer;
                }
            } while (stackCount > 0);

            if (closer != -1)
                end = closer;

        }
        return new int[]{start, end};
    }

    public String getMessageForQuoting() {
        return processContent(true, false);
    }

    public String getMessageForEditing() {
        return processContent(true, false);
    }

    private String processContent(boolean removeSig, boolean ignoreLtGt) {
        String finalBody = unprocessedMessageText;

        if (BuildConfig.DEBUG) AllInOneV2.wtl("beginning opening anchor tag removal");
        while (finalBody.contains("<a href")) {
            int start = finalBody.indexOf("<a href");
            int end = finalBody.indexOf(">", start) + 1;
            finalBody = finalBody.replace(finalBody.substring(start, end),
                    "");
        }

        if (BuildConfig.DEBUG) AllInOneV2.wtl("removing closing anchor tags");
        finalBody = finalBody.replace("</a>", "");

        if (BuildConfig.DEBUG)
            AllInOneV2.wtl("removing existing \\n, replacing linebreak tags with \\n");
        if (finalBody.endsWith("<br />"))
            finalBody = finalBody.substring(0, finalBody.length() - 6);
        finalBody = finalBody.replace("\n", "");
        finalBody = finalBody.replace("<br />", "\n");

        if (removeSig) {
            if (BuildConfig.DEBUG) AllInOneV2.wtl("removing sig");
            int sigStart = finalBody.lastIndexOf("\n---\n");
            if (sigStart != -1)
                finalBody = finalBody.substring(0, sigStart);
        }

        if (ignoreLtGt) {
            if (BuildConfig.DEBUG) AllInOneV2.wtl("ignoring &lt; / &gt;, pre-unescape");
            finalBody = finalBody.replace("&lt;", "&gameravenlt;").replace("&gt;", "&gameravengt;");
        }

        if (BuildConfig.DEBUG) AllInOneV2.wtl("unescaping finalbody html");
        finalBody = StringEscapeUtils.unescapeHtml4(finalBody);

        if (ignoreLtGt) {
            if (BuildConfig.DEBUG) AllInOneV2.wtl("ignoring &lt; / &gt;, post-unescape");
            finalBody = finalBody.replace("&gameravenlt;", "&lt;").replace("&gameravengt;", "&gt;");
        }

        if (BuildConfig.DEBUG) AllInOneV2.wtl("returning finalbody");
        return finalBody;
    }

}
