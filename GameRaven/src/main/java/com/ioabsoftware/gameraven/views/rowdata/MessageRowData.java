package com.ioabsoftware.gameraven.views.rowdata;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
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
import com.ioabsoftware.gameraven.util.RichTextUtils;
import com.ioabsoftware.gameraven.util.Theming;
import com.ioabsoftware.gameraven.util.UrlSpanConverter;
import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.GRQuoteSpan;
import com.ioabsoftware.gameraven.views.RowType;
import com.ioabsoftware.gameraven.views.SpoilerSpan;
import com.ioabsoftware.gameraven.views.rowview.HeaderRowView;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MessageRowData extends BaseRowData {

    private String username, userTitles, postNum, postTime, messageID, boardID, topicID;
    private final String unprocessedMessageText;

    private LinearLayout poll = null;

    private Spannable spannedMessage;

    private int hlColor;

    private boolean topClickable = true;

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

    public String getUser() {
        return username;
    }

    public String getUserTitles() {
        return userTitles;
    }

    public boolean hasTitles() {
        return userTitles != null;
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
        return Session.ROOT + "/users/" + username.replace(' ', '+') + "/boards";
    }

    private static AllInOneV2 aio = null;

    @Override
    public RowType getRowType() {
        return RowType.MESSAGE;
    }

    public MessageRowData(String userIn, String userTitlesIn, String postNumIn, String postTimeIn,
                          Element messageIn, String BID, String TID, String MID, int hlColorIn) {

        if (aio == null || aio != AllInOneV2.get())
            aio = AllInOneV2.get();

        if (BuildConfig.DEBUG) AllInOneV2.wtl("setting values");
        username = userIn;
        userTitles = userTitlesIn;
        postNum = postNumIn;
        postTime = postTimeIn.replace('\u00A0', ' ');
        boardID = BID;
        topicID = TID;
        messageID = MID;
        hlColor = hlColorIn;

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
            s.setColorFilter(Theming.accentColor(), PorterDuff.Mode.SRC_ATOP);
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
                        votedFor.setSpan(new ForegroundColorSpan(Theming.accentColor()), 0, text.length(), 0);
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

                int x = 0;
                for (Element e : pollElem.getElementsByAttributeValue("name", "poll_vote")) {
                    x++;
                    Button b = new Button(aio);
                    b.setText(e.nextElementSibling().text());
                    final HashMap<String, List<String>> data = new HashMap<String, List<String>>();
                    data.put("key", Arrays.asList(key));
                    data.put("poll_vote", Arrays.asList(Integer.toString(x)));
                    data.put("submit", Arrays.asList("Vote"));

                    b.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            aio.getSession().post(NetDesc.TOPIC, action, data);
                        }
                    });
                    pollInnerWrapper.addView(b);
                }

                Button b = new Button(aio);
                b.setText("View Results");
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

        unprocessedMessageText = messageIn.html();

        if (BuildConfig.DEBUG) AllInOneV2.wtl("creating ssb");
        SpannableStringBuilder ssb = new SpannableStringBuilder(processContent(false));

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

        if (BuildConfig.DEBUG) AllInOneV2.wtl("getting text colors for spoilers");
        if (Theming.usingLightTheme()) {
            revealedSpoilerColor = Color.WHITE;
            hiddenSpoilerColor = Color.BLACK;
        } else {
            revealedSpoilerColor = Color.BLACK;
            hiddenSpoilerColor = Color.WHITE;
        }

        ssb.append('\n');

        if (BuildConfig.DEBUG) AllInOneV2.wtl("linkifying");
        Linkify.addLinks(ssb, Linkify.WEB_URLS);

        if (BuildConfig.DEBUG) AllInOneV2.wtl("adding spoiler spans");
        addSpoilerSpans(ssb);

        if (BuildConfig.DEBUG) AllInOneV2.wtl("setting spannedMessage");
        spannedMessage = RichTextUtils.replaceAll(ssb, URLSpan.class, new UrlSpanConverter());
    }

    public boolean isEdited() {
        return userTitles != null && userTitles.contains("(edited)");
    }

    public boolean isEditable() {
        // Posted 8/1/2013 1:40:38 AM
        // Posted 8/1/2013 1:02:05 AM
        // Posted 3/18/2007 11:42:33 PM
        try {
            String id = AllInOneV2.getSettingsPref().getString("timezone", TimeZone.getDefault().getID());

            SimpleDateFormat sdf = new SimpleDateFormat("'Posted 'M/d/yyyy h:mm:ss a", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone(id));
            Date date = sdf.parse(postTime);

            Time now = new Time(id);
            Time then = new Time(id);

            then.set(date.getTime());
            then.normalize(true);

            now.setToNow();
            now.normalize(true);

            return (now.toMillis(false) - then.toMillis(false)) / ((float) DateUtils.HOUR_IN_MILLIS) < 1f;

        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
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
    private int hiddenSpoilerColor, revealedSpoilerColor;
    private boolean spoilersAreRevealed = false;
    private ArrayList<SpoilerSpan> spoilers = new ArrayList<SpoilerSpan>();

    public void revealSpoilers(Spannable s) {
        for (SpoilerSpan spoiler : spoilers)
            spoiler.reveal(revealedSpoilerColor, hiddenSpoilerColor, Theming.accentColor());

        s.setSpan(new StyleSpan(Typeface.BOLD), 0, 0, 0);

        spoilersAreRevealed = true;
    }

    public boolean hasSpoilers() {
        return !spoilers.isEmpty();
    }

    public boolean spoilersAreRevealed() {
        return spoilersAreRevealed;
    }

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
            SpoilerSpan spoiler = new SpoilerSpan(hiddenSpoilerColor, hiddenSpoilerColor, hiddenSpoilerColor);
            ssb.setSpan(spoiler, startEnd[0], startEnd[1], 0);
            spoilers.add(spoiler);

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
        return processContent(true);
    }

    public String getMessageForEditing() {
        return processContent(true);
    }

    private String processContent(boolean removeSig) {
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

        if (BuildConfig.DEBUG) AllInOneV2.wtl("unescaping finalbody html");
        finalBody = StringEscapeUtils.unescapeHtml4(finalBody);

        if (BuildConfig.DEBUG) AllInOneV2.wtl("returning finalbody");
        return finalBody;
    }

}
