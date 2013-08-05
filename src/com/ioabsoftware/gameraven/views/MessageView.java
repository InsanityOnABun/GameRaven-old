package com.ioabsoftware.gameraven.views;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Parcel;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.QuoteSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import android.text.util.Linkify;
import android.util.StateSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.MessageLinkSpan;
import com.ioabsoftware.gameraven.R;
import com.ioabsoftware.gameraven.RichTextUtils;
import com.ioabsoftware.gameraven.networking.HandlesNetworkResult.NetDesc;
import com.ioabsoftware.gameraven.networking.Session;

public class MessageView extends LinearLayout implements View.OnClickListener {

	private String username, userTitles, postTime, messageID, boardID, topicID;
	private Element messageContent, messageContentNoPoll;
	private ClickableLinksTextView message;
	private AllInOneV2 aio;
	
	private static int quoteBackColor = Color.argb(255, 100, 100, 100);
	
	public String getUser() {
		return username;
	}
	
	public String getMessageID() {
		return messageID;
	}
	
	public String getTopicID() {
		return topicID;
	}
	
	public String getBoardID() {
		return boardID;
	}
	
	public String getMessageDetailLink() {
		return Session.ROOT + "/boards/" + boardID + "/" + topicID + "/" + messageID;
	}
	
	public String getUserDetailLink() {
		return Session.ROOT + "/users/" + username.replace(' ', '+') + "/boards";
	}
	
	/**
	 * @return selected text, or null if no text is selected
	 */
	public String getSelection() {
		int start = message.getSelectionStart();
		int end = message.getSelectionEnd();
		if (start != end)
			return message.getText().subSequence(start, end).toString();
		else
			return null;
	}
	
	public boolean isEdited() {
		if (userTitles != null && userTitles.contains("(edited)"))
			return true;
		else
			return false;
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
			
			Time now  = new Time(id);
			Time then = new Time(id);
			
			then.set(date.getTime());
			then.normalize(true);
			
			now.setToNow();
			now.normalize(true);
			
			if ((now.toMillis(false) - then.toMillis(false)) / ((float) DateUtils.HOUR_IN_MILLIS) < 1f)
				return true;
			else
				return false;
			
		} catch (ParseException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 
	 * @param aioIn 
	 * @param userIn 
	 * @param userTitlesIn nullable
	 * @param postNum nullable
	 * @param postTimeIn
	 * @param messageIn
	 * @param BID
	 * @param TID
	 * @param MID
	 * @param hlColor
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public MessageView(final AllInOneV2 aioIn, String userIn, String userTitlesIn, String postNum,
					   String postTimeIn, Element messageIn, String BID, String TID, String MID, int hlColor) {
		super(aioIn);
		
		aioIn.wtl("starting mv creation");
		
		aio = aioIn;
        
        username = userIn;
        userTitles = userTitlesIn;
        postTime = postTimeIn.replace('\u00A0', ' ');
        messageContent = messageIn;
        messageID = MID;
        topicID = TID;
        boardID = BID;

		aio.wtl("stored vals");
		
		LayoutInflater inflater = (LayoutInflater) aio.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.msgview, this);

		aio.wtl("inflated layout");
        
		message = (ClickableLinksTextView) findViewById(R.id.mvMessage);
		
        ((TextView) findViewById(R.id.mvUser)).setText((userTitles != null ? username + userTitles : username));
        ((TextView) findViewById(R.id.mvUser)).setTextColor(AllInOneV2.getAccentTextColor());
        ((TextView) findViewById(R.id.mvPostNumber)).setText((postNum != null ? "#" + postNum + ", " + postTime : postTime));
        ((TextView) findViewById(R.id.mvPostNumber)).setTextColor(AllInOneV2.getAccentTextColor());

		aio.wtl("set text and color for user and post number");
        
        if (messageContent.getElementsByClass("board_poll").isEmpty()) {
    		aio.wtl("no poll");
    		messageContentNoPoll = messageContent.clone();
		}
        else {
    		aio.wtl("there is a poll");
    		messageContentNoPoll = messageContent.clone();
    		messageContentNoPoll.getElementsByClass("board_poll").first().remove();
        	
        	Element pollElem = messageContent.getElementsByClass("board_poll").first();

    		LinearLayout pollWrapper = (LinearLayout) findViewById(R.id.mvPollWrapper);
    		LinearLayout innerPollWrapper = new LinearLayout(aio);
    		
    		ShapeDrawable s = new ShapeDrawable();
    		Paint p = s.getPaint();
    		p.setStyle(Paint.Style.STROKE);
    		p.setStrokeWidth(10);
    		p.setColor(Color.parseColor(ColorPickerPreference.convertToARGB(AllInOneV2.getAccentColor())));
    		
    		pollWrapper.setBackgroundDrawable(s);
			pollWrapper.addView(new HeaderView(aio, pollElem.getElementsByClass("poll_head").first().text()));
			pollWrapper.addView(innerPollWrapper);

    		innerPollWrapper.setPadding(15, 0, 15, 15);
    		innerPollWrapper.setOrientation(VERTICAL);
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
        				votedFor.setSpan(new ForegroundColorSpan(AllInOneV2.getAccentColor()), 0, text.length(), 0);
        				t.setText(votedFor);
        			}
        			else
            			t.setText(text);
        			
        			innerPollWrapper.addView(t);
        		}
        		
        		String foot = pollElem.getElementsByClass("poll_foot_left").text();
        		if (foot.length() > 0) {
        			t = new TextView(aio);
        			t.setText(foot);
        			innerPollWrapper.addView(t);
        		}
        		
        	}
        	else {
        		// poll has not been voted in
            	final String action = "/boards/" + boardID + "/" + topicID;
    			String key = pollElem.getElementsByAttributeValue("name", "key").attr("value");
    			
    			int x = 0;
    			for (Element e : pollElem.getElementsByAttributeValue("name", "poll_vote")) {
    				x++;
    				Button b = new Button(aio);
    				b.setText(e.nextElementSibling().text());
    				final HashMap<String, String> data = new HashMap<String, String>();
    				data.put("key", key);
    				data.put("poll_vote", Integer.toString(x));
    				data.put("submit", "Vote");
    				b.setOnClickListener(new OnClickListener() {
    					@Override
    					public void onClick(View v) {
    						aio.getSession().post(NetDesc.TOPIC, action, data);
    					}
    				});
    				innerPollWrapper.addView(b);
    			}
    			
    			Button b = new Button(aio);
    			b.setText("View Results");
    			b.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						aio.getSession().get(NetDesc.TOPIC, action + "?results=1", null);
					}
				});
    			innerPollWrapper.addView(b);
    			
    			innerPollWrapper.setVisibility(View.VISIBLE);
        	}
        }

		aio.wtl("html var set");
        
//        URLImageParser p = new URLImageParser(message, aio);
//        Spanned htmlSpan = Html.fromHtml(html, p, null);
//        message.setText(htmlSpan);
//        message.setText(Html.fromHtml(html, null, null));
        
        SpannableStringBuilder ssb = new SpannableStringBuilder(processContent(false));
        
        addSpan(ssb, "<b>", "</b>", new StyleSpan(Typeface.BOLD));
        addSpan(ssb, "<i>", "</i>", new StyleSpan(Typeface.ITALIC));
        addSpan(ssb, "<code>", "</code>", new TypefaceSpan("monospace"));
        addSpan(ssb, "<cite>", "</cite>", new UnderlineSpan(), new StyleSpan(Typeface.ITALIC));
        
        // quotes don't use CharacterStyles, so do it manually
        while (ssb.toString().contains("<blockquote>")) {
        	int start = ssb.toString().indexOf("<blockquote>");
        	ssb.replace(start, start + "<blockquote>".length(), "\n");
        	start++;
        	
        	int stackCount = 1;
        	int closer;
        	int opener;
        	int innerStartPoint = start;
        	do {
        		opener = ssb.toString().indexOf("<blockquote>", innerStartPoint + 1);
        		closer = ssb.toString().indexOf("</blockquote>", innerStartPoint + 1);
        		if (opener != -1 && opener < closer) {
        			// found a nested quote
        			stackCount++;
        			innerStartPoint = opener;
        		}
        		else {
        			// this closer is the right one
        			stackCount--;
        			innerStartPoint = closer;
        		}
        	} while (stackCount > 0);
        	
        	
        	ssb.replace(closer, closer + "</blockquote>".length(), "\n");
        	ssb.setSpan(new GRQuoteSpan(), start, closer, 0);
        }
        
        
        final int defTextColor = message.getTextColors().getDefaultColor();
        final int color;
		if (AllInOneV2.getUsingLightTheme())
			color = Color.WHITE;
		else
			color = Color.BLACK;
		
		// do spoiler tags manually instead of in the method, as the clickablespan needs
		// to know the start and end points
        while (ssb.toString().contains("<spoiler>")) {
        	final int start = ssb.toString().indexOf("<spoiler>");
        	ssb.delete(start, start + 9);
        	final int end = ssb.toString().indexOf("</spoiler>", start);
        	ssb.delete(end, end + 10);
        	ssb.setSpan(new BackgroundColorSpan(defTextColor), start, end, 0);
        	ssb.setSpan(new ClickableSpan() {
				@Override
				public void onClick(View widget) {
					((Spannable) message.getText()).setSpan(new BackgroundColorSpan(color), start, end, 0);
				}
				
				@Override
				public void updateDrawState(TextPaint ds) {
					ds.setColor(defTextColor);
					ds.setUnderlineText(false);
				}
			}, start, end, 0);
        	
        }
        
        ssb.append('\n');
        message.setText(ssb);
        
        Linkify.addLinks(message, Linkify.WEB_URLS);
        
        aio.wtl("converting links to MessageLinkSpans");
        message.setText(RichTextUtils.replaceAll((Spanned) message.getText(), URLSpan.class, new URLSpanConverter()));
        aio.wtl("finished converting links to MessageLinkSpans");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        	message.setMovementMethod(ArrowKeyMovementMethod.getInstance());
        	message.setTextIsSelectable(true);
            // the autoLink attribute must be removed, if you hasn't set it then ok, otherwise call textView.setAutoLink(0);
        }
        
        message.setLinkTextColor(AllInOneV2.getAccentColor());
        aio.registerForContextMenu(message);

		aio.wtl("set message text, color");
        
        findViewById(R.id.mvTopWrapper).setOnClickListener(this);
        if (hlColor == 0)
        	findViewById(R.id.mvTopWrapper).setBackgroundDrawable(AllInOneV2.getMsgHeadSelector().getConstantState().newDrawable());
        else {
        	float[] hsv = new float[3];
    		Color.colorToHSV(hlColor, hsv);
        	if (AllInOneV2.getSettingsPref().getBoolean("useWhiteAccentText", false)) {
    			// color is probably dark
    			if (hsv[2] > 0)
    				hsv[2] *= 1.2f;
    			else
    				hsv[2] = 0.2f;
    		}
    		else {
    			// color is probably bright
    			hsv[2] *= 0.8f;
    		}
    		
    		int msgSelectorColor = Color.HSVToColor(hsv);
    		
    		StateListDrawable hlSelector = new StateListDrawable();
    		hlSelector.addState(new int[] {android.R.attr.state_focused}, new ColorDrawable(msgSelectorColor));
    		hlSelector.addState(new int[] {android.R.attr.state_pressed}, new ColorDrawable(msgSelectorColor));
    		hlSelector.addState(StateSet.WILD_CARD, new ColorDrawable(hlColor));
    		
    		findViewById(R.id.mvTopWrapper).setBackgroundDrawable(hlSelector.getConstantState().newDrawable());
        }

		aio.wtl("set click listener and drawable for top wrapper");
        
        if (AllInOneV2.isAccentLight())
        	((ImageView) findViewById(R.id.mvMessageMenuIcon)).setImageResource(R.drawable.ic_info_light);
        

		aio.wtl("finishing mv creation");
	}
	
	private void addSpan(SpannableStringBuilder ssb, String tag, String endTag, CharacterStyle... cs) {
		while (ssb.toString().contains(tag)) {
        	int start = ssb.toString().indexOf(tag);
        	ssb.delete(start, start + tag.length());
        	int end = ssb.toString().indexOf(endTag, start);
        	ssb.delete(end, end + endTag.length());
        	for (CharacterStyle c : cs)
        		ssb.setSpan(CharacterStyle.wrap(c), start, end, 0);
        }
	}

	@Override
	public void onClick(View v) {
		aio.messageMenuClicked(MessageView.this);
	}
	
	public String getMessageForQuoting() {
		return processContent(true);
	}
	
	public String getMessageForEditing() {
		return processContent(false);
	}

	private String processContent(boolean removeSig) {
		String finalBody = messageContentNoPoll.html();
		
		finalBody = finalBody.replace("<span class=\"fspoiler\">", "<spoiler>").replace("</span>", "</spoiler>");
		
		while (finalBody.contains("<a href")) {
			int start = finalBody.indexOf("<a href");
			int end = finalBody.indexOf(">", start) + 1;
			finalBody = finalBody.replace(finalBody.substring(start, end),
					"");
		}
		
		finalBody = finalBody.replace("</a>", "");
		
		if (finalBody.endsWith("<br />"))
			finalBody = finalBody.substring(0, finalBody.length() - 6);
		finalBody = finalBody.replace("\n", "");
		finalBody = finalBody.replace("<br />", "\n");

		if (removeSig) {
			int sigStart = finalBody.lastIndexOf("\n---\n");
			if (sigStart != -1)
				finalBody = finalBody.substring(0, sigStart);
		}
		
		finalBody = StringEscapeUtils.unescapeHtml4(finalBody);
		return finalBody;
	}
	
	public void disableTopClick() {
		findViewById(R.id.mvTopWrapper).setClickable(false);
		findViewById(R.id.mvMessageMenuIcon).setVisibility(View.INVISIBLE);
	}
	

	
	class URLSpanConverter implements RichTextUtils.SpanConverter<URLSpan, MessageLinkSpan> {
		@Override
		public MessageLinkSpan convert(URLSpan span) {
			return(new MessageLinkSpan(span.getURL(), aio));
		}
	}
	
	private class GRQuoteSpan extends QuoteSpan {
		
		private static final int WIDTH = 4;
		private static final int GAP = 4;
		private final int COLOR = AllInOneV2.getAccentColor();
		
		public void writeToParcel(Parcel dest, int flags) {
	        dest.writeInt(COLOR);
	    }

	    public int getColor() {
	        return COLOR;
	    }
	    
	    public int getLeadingMargin(boolean first) {
	        return WIDTH + GAP;
	    }
		
		@Override
		public void drawLeadingMargin(Canvas c, Paint p, int x, int dir,
                int top, int baseline, int bottom,
                CharSequence text, int start, int end,
                boolean first, Layout layout) {
			Paint.Style style = p.getStyle();
			int color = p.getColor();

			p.setStyle(Paint.Style.FILL);
			p.setColor(COLOR);

			c.drawRect(x, top, x + dir * WIDTH, bottom, p);
			
			p.setStyle(style);
			p.setColor(color);
		}
		
	}
}
