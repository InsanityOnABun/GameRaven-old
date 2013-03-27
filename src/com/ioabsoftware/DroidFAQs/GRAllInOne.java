package com.ioabsoftware.DroidFAQs;

import java.io.File;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebView.PictureListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshWebView;
import com.ioabsoftware.DroidFAQs.HandlesNetworkResult.NetDesc;
import com.ioabsoftware.DroidFAQs.Networking.Session;

public class GRAllInOne extends SherlockActivity implements HandlesNetworkResult, OnNavigationListener {
	
	private static boolean needToCheckForUpdate = true;
	private static boolean needToBuildCSS = true;
	
	public static final int CHANGE_LOGGED_IN_DIALOG = 0;
	public static final int NEW_VERSION_DIALOG = 1;
	public static final int SEND_PM_DIALOG = 2;
	
	private static final String ACCOUNTS_PREFNAME = "com.ioabsoftware.DroidFAQs.Accounts";
	private static final String SALT = "RIP Man fan died at the room shot up to 97 degrees";
	
	public static String defaultSig;
	
	/** current session */
	private Session session = null;
	public Session getCurrSession()
	{return session;}
	
	private String boardID;
	public String getBoardID()
	{return boardID;}
	private String topicID;
	public String getTopicID()
	{return topicID;}
	private String messageID;
	public String getMessageID()
	{return messageID;}
	private String postPostUrl;
	public String getPostPostUrl()
	{return postPostUrl;}
	
	private String savedPostBody;
	public String getSavedPostBody()
	{return savedPostBody;}
	private String savedPostTitle;
	public String getSavedPostTitle()
	{return savedPostTitle;}
	
	/** preference object for global settings */
	private static SharedPreferences settingsPref = null;
	public static SharedPreferences getSettingsPref()
	{return settingsPref;}
	
	/** list of accounts (username, password) */
	private static AccountPreferences accounts = null;
	public static AccountPreferences getAccounts()
	{return accounts;}
	
	private LinearLayout titleWrapper;
	private EditText postTitle;
	private EditText postBody;
	
	private TextView titleCounter;
	private TextView bodyCounter;
	
	private Button postButton;
	private Button cancelButton;
	
	private LinearLayout postWrapper;
	
	private PullToRefreshWebView contentPTR;
	private WebView content;
	
	private ActionBar aBar;
	private MenuItem refreshIcon;
    private String[] navList;
    private TextView abTitle;
	
	private static String source = "no source - you called this before any page could even load!";
	public static String getSource() 
	{return source;}
	
	public int getScrollerVertLoc() {
		return content.getScrollY();}
	
	private LinkedList<String> quoteUsers = new LinkedList<String>();
	private LinkedList<String> quoteBodies = new LinkedList<String>();
	
	
	/**********************************************
	 * START METHODS
	 **********************************************/
	

	
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
    	
        setContentView(R.layout.allinone);
        
        getSherlock().setProgressBarIndeterminateVisibility(false);
        
        settingsPref = PreferenceManager.getDefaultSharedPreferences(this);
        
        String nSlashA = settingsPref.getString("defaultAccount", "N/A");
        settingsPref.edit().putString("defaultAccount", nSlashA).commit();
        
        boolean enableJS = settingsPref.getBoolean("enableJavascript", true);
        settingsPref.edit().putBoolean("enableJavascript", enableJS).commit();
        
        boolean enablePTR = settingsPref.getBoolean("enablePTR", false);
        settingsPref.edit().putBoolean("enablePTR", enablePTR).commit();
        
        aBar = getSupportActionBar();
//        aBar.setDisplayShowHomeEnabled(false);
        aBar.setDisplayShowTitleEnabled(false);
        
    	wtl("logging started from onCreate");
    	
    	wtl("getting accounts");
        accounts = new AccountPreferences(getApplicationContext(), ACCOUNTS_PREFNAME, SALT, false);

    	wtl("getting all the views");
        
        contentPTR = (PullToRefreshWebView) findViewById(R.id.aioContent);
        contentPTR.setOnRefreshListener(new OnRefreshListener<WebView>() {
			@Override
			public void onRefresh(PullToRefreshBase<WebView> refreshView) {
				refreshClicked(contentPTR);
			}
		});
        
        content = contentPTR.getRefreshableView();
        
        titleWrapper  = (LinearLayout) findViewById(R.id.aioTitleWrapper);
        postTitle = (EditText) findViewById(R.id.aioPostTitle);
        postBody = (EditText) findViewById(R.id.aioPostBody);
        titleCounter = (TextView) findViewById(R.id.aioTitleCounter);
        bodyCounter = (TextView) findViewById(R.id.aioBodyCounter);
        
        abTitle = (TextView) findViewById(R.id.aioTitle);
        abTitle.setSelected(true);
        
        postTitle.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				String escapedTitle = StringEscapeUtils.escapeHtml4(postTitle.getText().toString());
				titleCounter.setText(escapedTitle.length() + "/80");
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
	    });
        
        postBody.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				String escapedBody = StringEscapeUtils.escapeHtml4(postBody.getText().toString());
				// GFAQs adds 13(!) characters onto bodies when they have a sig, apparently.
				int length = escapedBody.length() + getSig().length() + 13;
				bodyCounter.setText(length + "/4096 (includes sig)");
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
	    });
        
        postButton = (Button) findViewById(R.id.aioPostDo);
        cancelButton = (Button) findViewById(R.id.aioPostCancel);
        
        postWrapper = (LinearLayout) findViewById(R.id.aioPostWrapper);

    	wtl("setting webviewclient and picturelistener for content");
        content.setWebViewClient(new GRWebClient(this));
        content.setPictureListener(new PictureListener() {
			@Override
			public void onNewPicture(WebView view,
					android.graphics.Picture picture) {
				if (GRWebClient.doPictureListener()) {
					if (Session.applySavedScroll) {
						view.scrollTo(0, Session.savedScrollVal);
						Session.applySavedScroll = false;
					}

					GRWebClient.disablePictureListener();
				}
			}
		});

    	wtl("creating default sig");
		defaultSig = "This post made using GameRaven *grver*\n<i>Insanity On A Bun Software - Pushing the bounds of software sanity</i>";

    	wtl("getting css directory");
		File cssDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/gameraven");
    	if (!cssDirectory.exists()) {
        	wtl("css directory does not exist, creating");
    		cssDirectory.mkdir();
    	}

		wtl("setting build css flag");
    	setNeedToBuildCSS();
    	
    	wtl("setting check for update flag");
    	needToCheckForUpdate = true;
		
		wtl("onCreate finishing");
    }

	public void setNavList(boolean isLoggedIn) {
		if (isLoggedIn)
			navList = getResources().getStringArray(R.array.navListLoggedIn);
		else
			navList = getResources().getStringArray(R.array.navListLoggedOut);

        Context context = aBar.getThemedContext();
        ArrayAdapter<CharSequence> list;
        
        if (isLoggedIn)
        	list = ArrayAdapter.createFromResource(context, R.array.navListLoggedIn, R.layout.sherlock_spinner_item);
        else
        	list = ArrayAdapter.createFromResource(context, R.array.navListLoggedOut, R.layout.sherlock_spinner_item);
        
        list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

        aBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        aBar.setListNavigationCallbacks(list, this);
	}
    
    @Override
    protected void onNewIntent(Intent intent) {
    	wtl("onNewIntent fired");
    	if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
    	      String query = intent.getStringExtra(SearchManager.QUERY);
    	      wtl("new intent is a search query, query: " + query);
    	      HashMap<String, String> data = new HashMap<String, String>();
    	      
    	      if (session.getLastDesc() == NetDesc.BOARD) {
    	    	  wtl("searching topics for query");
    	    	  data.put("search", query);
        	      session.get(NetDesc.BOARD, session.getLastPathWithoutData(), data);
    	      }
    	      else if (session.getLastDesc() == NetDesc.BOARD_JUMPER || session.getLastDesc() == NetDesc.GAME_SEARCH) {
    	    	  wtl("searching for games");
    	    	  data.put("game", query);
        	      session.get(NetDesc.GAME_SEARCH, "/search/index.html", data);
    	      }
    	    }

    	wtl("onNewIntent finishing");
    }
    
	@SuppressLint("SetJavaScriptEnabled")
	@Override
    public void onResume() {
    	wtl("onResume fired");
    	super.onResume();
    	
        content.getSettings().setJavaScriptEnabled(settingsPref.getBoolean("enableJavascript", true));
    	
		if (needToBuildCSS) {
			wtl("building CSS");
			buildCSS();
			needToBuildCSS = false;
		}
		
		if (settingsPref.getBoolean("enablePTR", false))
			contentPTR.setMode(Mode.BOTH);
		else
	        contentPTR.setMode(Mode.DISABLED);
		
		if (session != null && settingsPref.getBoolean("reloadOnResume", false)) {
    		wtl("session exists, reload on resume is true, refreshing page");
    		session.refresh();
    	}
    	
    	if (session == null) {
    		wtl("creating a new session");
    		String defaultAccount = settingsPref.getString("defaultAccount", "");
    		if (accounts.containsKey(defaultAccount))
    			session = new Session(this, defaultAccount, accounts.getString(defaultAccount));
    		else
    			session = new Session(this);
    	}
    	
    	wtl("setting content webview's default font size");
		content.getSettings().setDefaultFontSize(settingsPref.getInt("fontSize", 12));
		
		if (needToCheckForUpdate) {
			needToCheckForUpdate = false;
			wtl("starting update check");
			session.devCheckForUpdate();
		}
        
		wtl("onResume finishing");
    }
	
	@Override
	public void handleNetworkResult(Response res, NetDesc desc) {
		wtl("GRAIO hNR fired, desc: " + desc.name());
		
		try {
			if (res != null) {
				wtl("res is not null");
				
				if (desc == NetDesc.DEV_UPDATE_CHECK) {
					wtl("GRAIO hNR determined this is update check response");
					
					String version = res.parse().getElementById("contentsinner").ownText();
					version = version.trim();
					wtl("latest version is " + version);
					if (!version.equals(this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName)) {
						wtl("version does not match current version, showing update dialog");
						showDialog(NEW_VERSION_DIALOG);
					}
					
					wtl("GRAIO hNR finishing via return, desc: " + desc.name());
					return;
				}

				wtl("parsing res");
				Document pRes = res.parse();

				wtl("setting board, topic, message id to null");
				boardID = null;
				topicID = null;
				messageID = null;
				
				pageJumper = null;
				favHtml = null;
				
				savedTo = null;
				savedSubject = null;
				savedMessage = null;
				
				if (!quoteUsers.isEmpty()) {
					wtl("emptying quote users and bodies lists");
					quoteUsers = new LinkedList<String>();
					quoteBodies = new LinkedList<String>();
				}
				
				// data head
				wtl("creating data stringbuilder, appending head");
				StringBuilder data = new StringBuilder();
				data.append("<html><head><style>");
				
				wtl("appending css");
				data.append(CSS);
				
				data.append("</style></head><body>");
				
				Element pmInboxLink = pRes.select("a[href=/pm/]").first();
				if (pmInboxLink != null) {
					if (!pmInboxLink.text().equals("Inbox")) {
						data.append("<table class=\"gameraven_table\"><tr><td>");
						data.append("You have one or more unread <a href=\"http://m.gamefaqs.com/pm/\">Private Messages.</a>");
						data.append("</td></tr></table>");
					}
				}
				
				switch (desc) {
				case BOARD_JUMPER:
				case LOGIN_S2:
					wtl("GRAIO hNR determined this is a home response");

					//aBar.setTitle("Board Jumper");
					abTitle.setText("Board Jumper");
					
					data.append(generateTable("?[_~g][_~g_~l]"));
					
					Elements homeTables = pRes.getElementsByTag("table");

					wtl("appending first table (favorites)");
					data.append(homeTables.first().outerHtml());

					wtl("getting, modding, appending second table (board browser)");
					Element browserTable = homeTables.get(1);
					browserTable.getElementsByTag("tr").get(1).remove();
					data.append(browserTable.outerHtml());

					data.append(generateTable("?[_~g][_~l_~g]"));
					
					wtl("home response block finished");
					break;
					
				case PM_INBOX:
					Element body = pRes.select("div.body").first();
					
					pageJumper = body.select("div.foot").first();
					
					Elements checkboxes = body.select("input[type=checkbox]");
					for (Element e : checkboxes)
						e.parent().remove();
					
					body.getElementsByTag("col").get(4).remove();
					body.getElementsByTag("th").get(4).remove();

					//aBar.setTitle(session.getUser() + "'s PM Inbox");
					abTitle.setText(session.getUser() + "'s PM Inbox");
					
					data.append(generateTable("_~w_~p"));
					data.append(body.outerHtml());
					data.append(generateTable("_~p_~w"));
					break;
					
				case READ_PM:
					Element title = pRes.select("h2.title").first();
					Element message = pRes.select("div.message").first();
					Element foot = pRes.select("div.foot").first();
					
					foot.getElementsByTag("div").get(1).remove();
					
					savedSubject = title.text();
					if (!savedSubject.startsWith("Re: "))
						savedSubject = "Re: " + savedSubject;
					
					//Sent by Barihhl to Corrupt_Power
					String footText = foot.text();
					savedTo = footText.substring(8, footText.toLowerCase(Locale.US).indexOf(" to " + session.getUser().toLowerCase(Locale.US)));

					//aBar.setTitle(title.text());
					abTitle.setText(title.text());
					
					data.append(generateTable("_~r"));
					data.append(message.outerHtml());
					data.append(foot.outerHtml());
					data.append(generateTable("_~r"));
					break;
					
				case AMP_LIST:
					wtl("GRAIO hNR determined this is an amp response");
					
					if (!pRes.getElementsByClass("pages").isEmpty())
						pageJumper = pRes.getElementsByClass("pages").first();

					//aBar.setTitle(session.getUser() + "'s Active Messages");
					abTitle.setText(session.getUser() + "'s Active Messages");
					
					data.append(generateTable("_~p"));
					
					pRes.getElementsByClass("lastpost").get(3).removeAttr("style");
					data.append(pRes.getElementsByTag("table").first().outerHtml());

					data.append(generateTable("_~p"));
					
					wtl("amp response block finished");
					break;
					
				case BOARD_LIST:
					wtl("GRAIO hNR determined this is a board list response");

					//aBar.setTitle(pRes.getElementsByTag("th").get(4).text());
					abTitle.setText(pRes.getElementsByTag("th").get(4).text());

					data.append(generateTable("_~g"));
					
					data.append(pRes.getElementsByTag("table").first().outerHtml());

					data.append(generateTable("_~g"));
					
					wtl("board list response block finished");
					break;
					
				case BOARD:
					wtl("GRAIO hNR determined this is a board response");
					
					wtl("setting board id");
					getBoardID(res.url().toString());
					
					if (!pRes.getElementsByClass("body_bot").isEmpty())
						pageJumper = pRes.getElementsByClass("body_bot").first().getElementsByClass("user").first();
					
					boolean isSplitList = false;
					if (pRes.getElementsByTag("th").first() != null) {
						if (pRes.getElementsByTag("th").first().text().equals("Board Title")) {
							wtl("is actually a split board list");

							//aBar.setTitle(pRes.getElementsByClass("head").first().text());
							abTitle.setText(pRes.getElementsByClass("head").first().text());
							
							data.append(pRes.getElementsByTag("table").first().outerHtml());
							
							isSplitList = true;
						}
					}
					
					if (!isSplitList) {
						String url = res.url().toString();
						String searchQuery = null;
						if (url.contains("search=")) {
							wtl("board search url: " + url);
							searchQuery = url.substring(url.indexOf("search=") + 7);
							int i = searchQuery.indexOf('&');
							if (i != -1)
								searchQuery.replace(searchQuery.substring(i), "");
							
							searchQuery = URLDecoder.decode(searchQuery);
						}
						if (searchQuery == null) {
							//aBar.setTitle(pRes.getElementsByClass("head").first().text());
							abTitle.setText(pRes.getElementsByClass("head").first().text());
						}
						else {
							//aBar.setTitle(pRes.getElementsByClass("head").first().text() + " (search: " + searchQuery + ")");
							abTitle.setText(pRes.getElementsByClass("head").first().text() + " (search: " + searchQuery + ")");
						}
						
						if (session.getUser() != null) {
							Element addFav = pRes.getElementsMatchingOwnText("Add to Favorites").first();
							Element removeFav = pRes.getElementsMatchingOwnText("Remove Favorite").first();
							if (addFav != null)
								favHtml = addFav.outerHtml();
							else if (removeFav != null)
								favHtml = removeFav.outerHtml();
						}
						
						data.append(generateTable("?[_-s-f_~b][_~s]_~p"));

						wtl("updating user level");
						updateUserLevel(pRes);
						
						Element table = pRes.getElementsByTag("table").first();
						if (table != null) {
							
							table.getElementsByTag("col").get(2).remove();
							table.getElementsByTag("th").get(2).remove();
							table.getElementsByTag("col").get(0).remove();
							table.getElementsByTag("th").get(0).remove();
							
							wtl("board row parsing start");
							boolean skipFirst = true;
							for (Element row : table.getElementsByTag("tr")) {
								if (!skipFirst) {
									Elements cells = row.getElementsByTag("td");
									// cells = [image] [title] [author] [post count] [last post]
									Element imgCell = cells.get(0);
									Element img = imgCell.getAllElements().get(1);
									Element titleCell = cells.get(1);
									Element authorCell = cells.get(2);

									img.addClass("gameraven_topic_img");
									img.attr("width", "18px");
									img.attr("height", "19px");
									
									String titleContent = "<div class=\"gameraven_topic_title\">" + titleCell.html() + "</div>";
									String authorContent = "<span class=\"gameraventopicauthor\">" + authorCell.text() + "</span>";
									
									String imgTitleWrapper = "<div class=\"gameraven_img_title_wrapper\">" + img.outerHtml() + titleContent + "</div>";
									
									titleCell.html(authorContent + imgTitleWrapper);
									authorCell.remove();
									imgCell.remove();
								}
								else
									skipFirst = false;
							}
							wtl("board row parsing end");
							
							data.append(pRes.getElementsByTag("table").first().outerHtml());
						}
						else {
							wtl("board is empty, or search returned nothing");
							data.append("<table class=\"gameraven_table\"><tr><td>No topics found.</td></tr></table>");
						}
						
						data.append(generateTable("_~p?[_~b_-s-f][_~s]"));
					}
					
					wtl("board response block finished");
					break;
					
				case TOPIC:
					wtl("timer:topic start");
					wtl("timer:getting board and topic id start");
					getBoardID(res.url().toString());
					getTopicID(res.url().toString());
					wtl("timer:getting board and topic id end");
					
					wtl("timer:pagejumper get start");
					if (!pRes.getElementsByClass("body_bot").isEmpty())
						pageJumper = pRes.getElementsByClass("body_bot").first().getElementsByClass("user").first();
					wtl("timer:pagejumper get end");

					//aBar.setTitle(pRes.getElementsByClass("title").first().text());
					abTitle.setText(pRes.getElementsByClass("title").first().text());
					
					wtl("timer:generate top table start");
					data.append(generateTable("?[_-t-m][_~t]_~p"));
					wtl("timer:generate top table end");
					
					wtl("timer:updating user level start");
					updateUserLevel(pRes);
					wtl("timer:updating user level end");
					
					wtl("timer:getting users for quoting start");
					Elements qUsers = pRes.getElementsByAttributeValueContaining("href", "/users/");
					qUsers.remove(0);
					wtl("timer:getting users for quoting end");

					wtl("timer:getting links for quoting start");
					Elements qLinks = pRes.getElementsByClass("qq");
					if (qLinks.isEmpty()) 
						qLinks = pRes.getElementsByAttributeValueContaining("href", "quote");
					wtl("timer:getting links for quoting end");

					wtl("timer:getting bodies for quoting start");
					Elements qBodies = pRes.getElementsByClass("msg_body");
					wtl("timer:getting bodies for quoting end");

					wtl("timer:adding users to quote user list, unlinkifying usernames start");
					for (Element user : qUsers) {
						quoteUsers.add(user.ownText());
						user.unwrap().wrap("<span class=\"gameraven_username\">");
					}
					wtl("timer:adding users to quote user list, unlinkifying usernames end");
					
					wtl("timer:processing message detail links start");
					for (Element detailLink : pRes.getElementsContainingOwnText("message detail")) {
						if (detailLink.hasAttr("href")) {
							detailLink.attr("href", detailLink.attr("href") + "GAMERAVEN_MESSAGE_DETAIL");
						}
					}
					wtl("timer:processing message detail links end");
					
					int x = 0;
					wtl("timer:replacing quote links with GR quote links start");
					for (Element link : qLinks) {
						link.attr("href", "GAMERAVEN_QUOTE-" + x);
						x++;
					}
					wtl("timer:replacing quote links GR quote links end");

					wtl("timer:checking for a poll in this topic start");
					Element pollDiv = pRes.getElementsByClass("board_poll").first();
					if (pollDiv != null) {
						wtl("there is a poll in this topic");
						
						if (!pollDiv.getElementsByTag("form").isEmpty()) {
							wtl("poll has not been voted in");
							
							Elements inputs = pollDiv.getElementsByTag("input");
							// first is key, last is submit, all others are radio boxes
							
							String key = inputs.first().attr("value");

							wtl("parsing inputs");
							for (int i = 1; i < inputs.size() - 1; i++) {
								Element option = inputs.get(i);
								String id = option.attr("id");
								String value = option.attr("value");
								String name = option.attr("name");

								Element newDiv = new Element(Tag.valueOf("DIV"), option.baseUri());
								newDiv.addClass("gameraven_poll_option");
								
								Element newLink = new Element(Tag.valueOf("A"), option.baseUri());
								newLink.text("Vote");
								newLink.attr("id", id);
								newLink.attr("href", session.getLastPathWithoutData()
										+ "GAMERAVEN_VOTE_TOPICPOLL?name=" + name
										+ "&value=" + value + "&key=" + key);
								newLink.addClass("gameraven_vote_link");
								
								Element label = pollDiv.getElementsByAttributeValue("for", id).first();
								
								newDiv.appendChild(newLink).appendChild(label.clone());
								label.remove();

								inputs.get(i).replaceWith(newDiv);
							}
							
							pollDiv.getElementsByClass("poll_foot_left").first().remove();
						}
					}
					wtl("timer:checking for a poll in this topic end");
					
					// TODO: linkify and TTI-ify stuff in this for loop
					// TODO: make quote formatting much, much better
					wtl("timer:processing msg bodies for quoting start");
					for (Element postBody : qBodies) {
						wtl("timer:msg body processing start");
						wtl("cloning body");
						Element clonedBody = postBody.clone();
						wtl("body cloned finished, checking for poll to remove");
						if (!clonedBody.getElementsByClass("board_poll").isEmpty()) {
							wtl("post has a poll, removing");
							clonedBody.getElementsByClass("board_poll").first().remove();
						}

						wtl("poll check finished, getting html");
						String finalBody = clonedBody.html();

						wtl("get html finished, finding sig seperator");
						int sigStart = finalBody.lastIndexOf("---");
						if (sigStart != -1)
							finalBody = finalBody.substring(0, sigStart);
						
						finalBody = finalBody.replace("<span class=\"fspoiler\">", "<spoiler>").replace("</span>", "</spoiler>");
						
						while (finalBody.contains("<a href")) {
							int start = finalBody.indexOf("<a href");
							int end = finalBody.indexOf(">", start) + 1;
							finalBody = finalBody.replace(finalBody.substring(start, end), "");
						}
						finalBody = finalBody.replace("</a>", "");
						if (finalBody.endsWith("<br />"))
							finalBody = finalBody.substring(0, finalBody.length() - 6);
						finalBody = finalBody.replace("\n", "");
						finalBody = finalBody.replace("<br />", "\n");
						
						finalBody = StringEscapeUtils.unescapeHtml4(finalBody);
						
						quoteBodies.add(finalBody);
						wtl("timer:msg body processing end");
					}
					wtl("timer:processing msg bodies for quoting end");
					
					wtl("timer:appending main table to data start");
					data.append(pRes.getElementsByTag("table").first().outerHtml());
					wtl("timer:appending main table to data end");

					wtl("timer:generate bottom table start");
					data.append(generateTable("_~p?[_-t-m][_~t]"));
					wtl("timer:generate bottom table end");
					
					wtl("timer:topic end");
					break;
					
				case MSG_DETAIL:
					//aBar.setTitle("Message Detail");
					abTitle.setText("Message Detail");
					
					getBoardID(res.url().toString());
					getTopicID(res.url().toString());
					getMessageID(res.url().toString());
					
					String key = pRes.getElementsByAttributeValue("name", "key").first().attr("value");
					
					data.append(pRes.getElementsByClass("body").get(1).outerHtml());
					
					data.append("<table class=\"gameraven_table\">");
					
					if (!pRes.select("h2:containsOwn(Edit this Message)").isEmpty()) {
						data.append("<tr><td><p>You can edit your message within one hour of posting to fix any errors or add additional " +
								"information.</p><a href=\"GAMERAVEN_EDIT_POST\">Edit post</a></td></tr>");
					}

					if (!pRes.select("h2:containsOwn(Delete this Message)").isEmpty()) {
						data.append("<tr><td><p>You can delete your message from the boards, however, bear in mind that a record of your " +
								"deletion will be kept in the database. You can either delete your entire topic, if yours is the first and " +
								"only post, or simply overwrite your message with a note saying that you chose to delete the message.</p>" +
								"<a href=\"GAMERAVEN_DELETE_POST" + key + "\">Delete post</a></td></tr>");
					}

					if (!pRes.select("h2:containsOwn(Close this Topic)").isEmpty()) {
						data.append("<tr><td><p>As nobody has posted in this topic for the past 5 minutes, you have the option of closing " +
								"it, making it impossible to post any additional messages.</p><a href=\"GAMERAVEN_CLOSE_TOPIC" + key + 
								"\">Close topic</a></td></tr>");
					}
					
					if (!pRes.getElementsContainingText(
							"Report This Message to the Moderators").isEmpty()) {
						data.append("<tr><th>You have the ability to report messages to the moderators as abusive. This function allows you " + 
								"to report Terms of Use violations and Board Etiquette issues to the moderators, who then will take " +
								"appropriate action on it as they reach this message in the queue. The reporting function is anonymous; " +
								"only the moderators will see who has marked this message, and that information is deleted when the " +
								"moderator takes action. Please note that abuse of this form could result in karma loss or termination " +
								"of your account.");
						data.append("</th></tr><tr><td>");
						data.append("<a href=\"GAMERAVEN_MARK_POST" + "1?" + key + 
								"\">Report Offensive: Sexually explicit, racism, threats, pornography</a>");
						data.append("</td></tr><tr><td>");
						data.append("<a href=\"GAMERAVEN_MARK_POST" + "5?" + key +
								"\">Report Advertising: Spam, \"Make Money Fast\", referrer codes</a>");
						data.append("</td></tr><tr><td>");
						data.append("<a href=\"GAMERAVEN_MARK_POST" + "6?" + key +
								"\">Report Illegal Activities: Copyright violations, online game cheats</a>");
						data.append("</td></tr><tr><td>");
						data.append("<a href=\"GAMERAVEN_MARK_POST" + "18?" + key +
								"\">Report Spoiler with no Warning: Revealing critical plot details with no warning</a>");
						data.append("</td></tr><tr><td>");
						data.append("<a href=\"GAMERAVEN_MARK_POST" + "22?" + key +
								"\">Report Harassment/Privacy: Posting personal information, repeated harassment and bullying</a>");
						data.append("</td></tr><tr><td>");
						data.append("<a href=\"GAMERAVEN_MARK_POST" + "2?" + key +
								"\">Report Censor Bypassing: Not properly obscuring offensive/banned words</a>");
						data.append("</td></tr><tr><td>");
						data.append("<a href=\"GAMERAVEN_MARK_POST" + "3?" + key +
								"\">Report Trolling: Provoking other users to respond inappropriately</a>");
						data.append("</td></tr><tr><td>");
						data.append("<a href=\"GAMERAVEN_MARK_POST" + "4?" + key +
								"\">Report Flaming: Insulting other board users</a>");
						data.append("</td></tr><tr><td>");
						data.append("<a href=\"GAMERAVEN_MARK_POST" + "20?" + key +
								"\">Report Disruptive Posting: ALL CAPS, large blank posts, hard-to-read posts, mass bumping</a>");
						data.append("</td></tr></table>");
					}
					
					Element postBody = pRes.getElementsByClass("author").get(1).nextElementSibling();
					
					wtl("cloning body");
					Element clonedBody = postBody.clone();
					wtl("body cloned finished, checking for poll to remove");
					if (!clonedBody.getElementsByClass("board_poll").isEmpty()) {
						wtl("post has a poll, removing");
						clonedBody.getElementsByClass("board_poll").first().remove();
					}

					wtl("poll check finished, getting html");
					String finalBody = clonedBody.html();

					wtl("get html finished, finding sig seperator");
					finalBody = finalBody.replace("<span class=\"fspoiler\">", "<spoiler>").replace("</span>", "</spoiler>");
					
					while (finalBody.contains("<a href")) {
						int start = finalBody.indexOf("<a href");
						int end = finalBody.indexOf(">", start) + 1;
						finalBody = finalBody.replace(finalBody.substring(start, end), "");
					}
					finalBody = finalBody.replace("</a>", "");
					if (finalBody.endsWith("<br />"))
						finalBody = finalBody.substring(0, finalBody.length() - 6);
					finalBody = finalBody.replace("\n", "");
					finalBody = finalBody.replace("<br />", "\n");
					
					finalBody = StringEscapeUtils.unescapeHtml4(finalBody);
					
					quoteBodies.add(finalBody);
					
					break;
					
				case GAME_SEARCH:
					wtl("GRAIO hNR determined this is a game search response");
					Element nextPage = null;
					Element previousPage = null;
					
					if (!pRes.getElementsContainingOwnText("Next Page").isEmpty())
						nextPage = pRes.getElementsContainingOwnText("Next Page").first();
					if (!pRes.getElementsContainingOwnText("Previous Page").isEmpty())
						previousPage = pRes.getElementsContainingOwnText("Previous Page").first();
					
					if (nextPage != null) {
						if (previousPage != null) {
							pageJumper = new Element(org.jsoup.parser.Tag.valueOf("SPAN"), res.url().toString());
							pageJumper.append(previousPage.outerHtml());
							pageJumper.append(" | " + nextPage.outerHtml());
						}
						else {
							pageJumper = new Element(org.jsoup.parser.Tag.valueOf("SPAN"), res.url().toString());
							pageJumper.append(nextPage.outerHtml());
						}
					}
					else if (previousPage != null) {
						pageJumper = new Element(org.jsoup.parser.Tag.valueOf("SPAN"), res.url().toString());
						pageJumper.append(previousPage.outerHtml());
					}
					
					String url = res.url().toString();
					wtl("game search url: " + url);
					String searchQuery = null;
					
					searchQuery = url.substring(url.indexOf("game=") + 5);
					int i = searchQuery.indexOf("&");
					if (i != -1)
						searchQuery = searchQuery.replace(searchQuery.substring(i), "");
					
					searchQuery = URLDecoder.decode(searchQuery);
					//aBar.setTitle("Searching games: " + searchQuery + "");
					abTitle.setText("Searching games: " + searchQuery + "");
					
					data.append(generateTable("_~g_~p"));
					
					Elements gameSearchTables = pRes.getElementsByTag("table");
					if (!gameSearchTables.isEmpty()) {
						for (Element table : gameSearchTables) {
						
							Elements colgroup = table.getElementsByTag("col");
							colgroup.get(9).remove();
							colgroup.get(8).remove();
							colgroup.get(7).remove();
							colgroup.get(6).remove();
							colgroup.get(5).remove();
							colgroup.get(4).remove();
							colgroup.get(3).remove();
							colgroup.get(2).remove();
							
							wtl("board row parsing start");
							for (Element row : table.getElementsByTag("tr")) {
								Elements cells = row.getElementsByTag("td");
								// cells = [platform] [title] [faqs] [codes] [saves] [revs] [mygames] [q&a] [pics] [vids] [board]
								Element titleCell = cells.get(1);
								
								titleCell.html(titleCell.text());
								
								cells.get(9).remove();
								cells.get(8).remove();
								cells.get(7).remove();
								cells.get(6).remove();
								cells.get(5).remove();
								cells.get(4).remove();
								cells.get(3).remove();
								cells.get(2).remove();
							}
							wtl("board row parsing end");
							
							data.append(table.outerHtml());
						}
					}
					else
						data.append("<table class=\"gameraven_table\"><tr><td>No games found.</td></tr></table>");

					data.append(generateTable("_~p_~g"));
					
					wtl("game search response block finished");
					break;
					
				default:
					wtl("GRAIO hNR determined response type is unhandled");
					//aBar.setTitle("Page unhandled - " + res.url().toString());
					abTitle.setText("Page unhandled - " + res.url().toString());
					data.append(res.url() + "<br /><br />");
					data.append(pRes.outerHtml());
					break;
				}
				
				wtl("timer:appending ads start");
				for (Element e : pRes.getElementsByClass("ad"))
					if (!e.childNodes().isEmpty()) {
						e.removeClass("ad").addClass("gameraven_ad");
						data.append(e.outerHtml());
					}
				wtl("timer:appending ads end");
				
				data.append("</body></html>");
				
				wtl("loading data into content webview");
				if (Session.applySavedScroll)
					wtl("saved scroll val will be applied: " + Session.savedScrollVal);
				
				wtl("timer:output data to string and load into webview start");
				source = data.toString();
				content.loadDataWithBaseURL(session.getLastPath(), source, "text/html", "iso-8859-1", null);
				wtl("timer:output data to string and load into webview end");
				
			}
			else {
				wtl("res is null");
				content.loadData("There was an error loading the page.", "text", null);
			}
		}
		catch (Exception e) {
			tryCaught(ExceptionUtils.getStackTrace(e));
			session.goBack(false);
		}
		catch (StackOverflowError e) {
			tryCaught(ExceptionUtils.getStackTrace(e));
			session.goBack(false);
		}
		wtl("GRAIO hNR finishing");
	}
	
	private Element pageJumper = null;
	private String favHtml = null;
	private int tableGeneratorDepth = 0;
	private boolean tableGeneratorFirstRow = true;
	private boolean tableGeneratorFirstCell = true;
	private String generateTable(String code) {
		/*
		 * _ = new row
		 * - = new gameraven_2cells cell
		 * ~ = new colspan2 cell
		 * t = topic list
		 * f = add/remove board from favorites
		 * b = post on board link
		 * m = post in topic link
		 * l = log in message
		 * g = game search
		 * s = topic search
		 * p = page jumper
		 * w = send pm (w for 'whisper')
		 * r = reply to pm
		 * ?[][] = dependent on login status - the first following bracketed block will be applied if logged in, the second if not
		 * n = nothing
		 */
		
		tableGeneratorDepth++;
		wtl("generateTable code: " + code);
		
		StringBuilder table = new StringBuilder();
		
		if (tableGeneratorDepth < 2)
			table.append("<table class=\"gameraven_table\">");
		
		for (int x = 0; x < code.length(); x++) {
			char c = code.charAt(x);
			switch (c) {
			case '_': // new row
				if (!tableGeneratorFirstRow)
					table.append("</tr>");
				
				tableGeneratorFirstRow = false;
				tableGeneratorFirstCell = true;
				table.append("<tr class=\"even\">");
				break;

			case '-': // new gameraven_2cells cell
				if (!tableGeneratorFirstCell)
					table.append("</td>");
				
				tableGeneratorFirstCell = false;
				table.append("<td class=\"gameraven_2cells\">");
				break;

			case '~': // new colspan2 cell
				if (!tableGeneratorFirstCell)
					table.append("</td>");
				
				tableGeneratorFirstCell = false;
				table.append("<td colspan=\"2\">");
				break;
				
			case '?':
				int isLoggedStart = code.indexOf('[', x);
				int isLoggedEnd = code.indexOf(']', isLoggedStart);
				int notLoggedStart = code.indexOf('[', isLoggedEnd);
				int notLoggedEnd = code.indexOf(']', notLoggedStart);
				if (session.getUser() != null) {
					table.append(generateTable(code.substring(isLoggedStart + 1, isLoggedEnd)));
				}
				else {
					table.append(generateTable(code.substring(notLoggedStart + 1, notLoggedEnd)));
				}
				
				x += notLoggedEnd - x;
				wtl("generateTable remaining code: " + code.substring(x + 1));
				break;

			default:
				table.append(decodeChar(c, pageJumper, favHtml));
				
			}
		}
		
		if (tableGeneratorDepth < 2) {
			table.append("</td></tr>");
			table.append("</table>");
		}
		
		String tableData = table.toString();
		
		wtl("generateTable data: " + tableData);
		tableGeneratorDepth--;
		return tableData;
	}
	
	private String decodeChar(char c, Element pageJumper, String favHtml) {
		String linkText = null;
		switch (c) {
		case 't': // topic list link
			return "<a href=\"http://m.gamefaqs.com/boards/" + boardID + "\">Topic List</a>";
			
		case 'f': // add/remove board from favorites
			return favHtml;

		case 'b': // post new topic
			return "<a href='GAMERAVEN_POST_ON_BOARD'>Post New Topic</a>";

		case 'm': // post in topic
			return "<a href='GAMERAVEN_POST_ON_TOPIC'>Post New Message</a>";

		case 'l': // log in message
			return "You should log in! Press the menu key and either change your logged in account, or go into the settings and add an existing account!";

		case 'g': // game search
			return "<a href=\"GAMERAVEN_SEARCH_GAMES\">Search Games</a>";

		case 's': // topic search
			return "<a href=\"GAMERAVEN_SEARCH_BOARD\">Search Topics</a>";

		case 'p': // page jumper
			if (pageJumper != null) {
				pageJumper.select("a[href]:containsOwn(First)").addClass("gameraven_first_page");
				pageJumper.select("a[href]:containsOwn(Last)").addClass("gameraven_last_page");
				return pageJumper.outerHtml();
			}
			else
				return "";
			
		case 'r':
			linkText = "Reply to PM";
		case 'w': // send pm
			if (linkText == null)
				linkText = "Send PM";
			return "<a href=\"GAMERAVEN_SEND_PM" + savedTo + "~-_" + savedSubject + "~-_" + savedMessage + "~-_\">" + linkText + "</a>";

		case 'n': // nothing
			return "";
			
		default:
			Toast.makeText(this, "char not recognized in table gen code, skipping: " + c, Toast.LENGTH_SHORT).show();
			return "";
			
		}
	}

	@Override
	public void preExecuteSetup(NetDesc desc) {
		wtl("GRAIO dPreES fired --NEL, desc: " + desc.name());
		getSherlock().setProgressBarIndeterminateVisibility(true);
		if (refreshIcon != null)
			refreshIcon.setVisible(false);
		if (desc != NetDesc.POSTMSG_S1 && desc != NetDesc.POSTTPC_S1 &&
			desc != NetDesc.QPOSTMSG_S1 && desc != NetDesc.QPOSTTPC_S1)
			postCleanup();
	}
	
	public void timeoutCleanup(NetDesc desc) {
		if (desc != NetDesc.DEV_UPDATE_CHECK) {
			getSherlock().setProgressBarIndeterminateVisibility(false);
			if (refreshIcon != null)
				refreshIcon.setVisible(true);
			if (postWrapper.getVisibility() == View.VISIBLE) {
				postButton.setEnabled(true);
				cancelButton.setEnabled(true);
			}
			String msg = "timeout msg unset";
			switch (desc) {
			case LOGIN_S1:
			case LOGIN_S2:
				msg = "Login timed out, refresh to try again.";
				break;
			case POSTMSG_S1:
			case POSTMSG_S2:
			case POSTMSG_S3:
			case POSTTPC_S1:
			case POSTTPC_S2:
			case POSTTPC_S3:
			case QPOSTMSG_S1:
			case QPOSTMSG_S3:
			case QPOSTTPC_S1:
			case QPOSTTPC_S3:
				msg = "Post timed out. After refreshing, check to see if your post made it through before attempting to post again.";
				break;
			default:
				msg = "Connection timed out, refresh to try again.";
				break;

			}
			AlertDialog.Builder b = new AlertDialog.Builder(this);
			b.setTitle("Timeout");
			b.setMessage(msg);
			b.setPositiveButton("Refresh", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					refreshClicked(new View(GRAllInOne.this));
				}
			});
			b.setNegativeButton("Dismiss", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			b.create().show();
		}
	}

	@Override
	public void postExecuteCleanup(NetDesc desc) {
		wtl("GRAIO dPostEC --NEL, desc: " + desc.name());
		getSherlock().setProgressBarIndeterminateVisibility(false);
		if (refreshIcon != null)
			refreshIcon.setVisible(true);
		if (desc == NetDesc.BOARD || desc == NetDesc.TOPIC)
			postCleanup();
	}
	
	public void editPostSetup() {
		postBody.setText(quoteBodies.get(0));
		postOnTopicSetup(true);
	}
	
	public void quoteSetup(int id) {
		wtl("quoteSetup fired");
		String cite = "<cite>" + quoteUsers.get(id) + " posted...</cite>\n";
		String body = "<quote>" + quoteBodies.get(id) + "</quote>\n\n";
		
		wtl("cite: " + cite);
		wtl("body: " + body);
		
		if (postWrapper.getVisibility() != View.VISIBLE) {
			postBody.setText(cite + body);
			postOnTopicSetup(false);
		}
		else {
			postBody.append(cite + body);
			postBody.setSelection(postBody.getText().length());
		}
		wtl("quoteSetup finishing");
	}
	
	public void postOnTopicSetup(boolean isEditingPost) {
		wtl("postOnTopicSetup fired --NEL");
		titleWrapper.setVisibility(View.GONE);
		postWrapper.setVisibility(View.VISIBLE);
		postButton.setEnabled(true);
		cancelButton.setEnabled(true);
		postBody.requestFocus();
		postBody.setSelection(postBody.getText().length());
		postPostUrl = session.getLastPath();
		if (postPostUrl.contains("#"))
			postPostUrl = postPostUrl.substring(0, postPostUrl.indexOf('#'));
		if (isEditingPost) {
			// if this is the case, then we are editing a post. Modify postPostUrl to point
			// to the topic the post is in instead of the post detail page
			int msgSep = postPostUrl.lastIndexOf('/');
			postPostUrl = postPostUrl.substring(0, msgSep);
		}
	}
	
	public void postOnBoardSetup() {
		wtl("postOnBoardSetup fired --NEL");
		titleWrapper.setVisibility(View.VISIBLE);
		postWrapper.setVisibility(View.VISIBLE);
		postButton.setEnabled(true);
		cancelButton.setEnabled(true);
		postTitle.requestFocus();
		postPostUrl = session.getLastPath();
		if (postPostUrl.contains("#"))
			postPostUrl = postPostUrl.substring(0, postPostUrl.indexOf('#'));
	}
	
	public void postCleanup() {
		if (postWrapper.getVisibility() == View.VISIBLE) {
			wtl("postCleanup fired --NEL");
			postWrapper.setVisibility(View.GONE);
			postBody.setText(null);
			postTitle.setText(null);
			postPostUrl = null;
			content.requestFocus();
		}
	}
	
	public void postDo(View view) {
		int escapedTitleLength = StringEscapeUtils.escapeHtml4(postTitle.getText().toString()).length();
		int escapedBodyLength = StringEscapeUtils.escapeHtml4(postBody.getText().toString()).length();
		
		wtl("postDo fired");
		if (titleWrapper.getVisibility() == View.VISIBLE) {
			wtl("posting on a board");
			// posting on a board
			if (postTitle.length() > 4) {
				if (escapedTitleLength < 81) {
					if (postBody.length() > 0) {
						if (escapedBodyLength < 4097) {
							String path = "http://m.gamefaqs.com/boards/post.php?board=" + boardID;
							int i = path.indexOf('-');
							path = path.substring(0, i);
							wtl("post path: " + path);
							savedPostBody = postBody.getText().toString();
							wtl("saved post body: " + savedPostBody);
							savedPostTitle = postTitle.getText().toString();
							wtl("saved post title: " + savedPostTitle);
							wtl("sending topic");
							postButton.setEnabled(false);
							cancelButton.setEnabled(false);
							if (userLevel < 30)
								session.get(NetDesc.POSTTPC_S1, path, null);
							else
								session.get(NetDesc.QPOSTTPC_S1, path, null);
						}
						else {
							wtl("post body is too long");
							Toast.makeText(this, "Post body is too long.", Toast.LENGTH_SHORT).show();
						}
					}
					else {
						wtl("post body is empty");
						Toast.makeText(this, "Post body can't be empty.", Toast.LENGTH_SHORT).show();
					}
				}
				else {
					wtl("post title is too long");
					Toast.makeText(this, "Topic title length must be less than 80 characters.", Toast.LENGTH_SHORT).show();
				}
			}
			else {
				wtl("post title is too short");
				Toast.makeText(this, "Topic title length must be greater than 4 displayed characters.", Toast.LENGTH_SHORT).show();
			}
		}
		
		else {
			// posting on a topic
			wtl("posting on a topic");
			if (postBody.length() > 0) {
				if (escapedBodyLength < 4097) {
					String path = "http://m.gamefaqs.com/boards/post.php?board=" + boardID + "&topic=" + topicID;
					if (messageID != null)
						path += "&message=" + messageID;
					
					wtl("post path: " + path);
					savedPostBody = postBody.getText().toString();
					wtl("saved post body: " + savedPostBody);
					wtl("sending post");
					postButton.setEnabled(false);
					cancelButton.setEnabled(false);
					if (messageID != null)
						session.get(NetDesc.QEDIT_MSG, path, null);
					else if (userLevel < 30)
						session.get(NetDesc.POSTMSG_S1, path, null);
					else
						session.get(NetDesc.QPOSTMSG_S1, path, null);
				}
				else {
					wtl("post body is too long");
					Toast.makeText(this, "Post body is too long.", Toast.LENGTH_SHORT).show();
				}
			}
			else {
				wtl("post body is empty");
				Toast.makeText(this, "Post body can't be empty.", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	public void postCancel(View view) {
		wtl("postCancel fired --NEL");
		postCleanup();
	}
	
	public void postError(String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(msg);
		builder.setTitle("There was a problem with your post...");
		builder.setPositiveButton("Ok", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
		postButton.setEnabled(true);
		cancelButton.setEnabled(true);
	}
	
	public void fourOhError(int errorNum, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		msg = msg.replace("..", ".");
		builder.setMessage(msg);
		builder.setTitle(errorNum + " Error");
		builder.setPositiveButton("Ok", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}
	
	public void refreshClicked(View view) {
		wtl("refreshClicked fired --NEL");
		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		if (session.getLastPath() == null) {
			if (session.getUser() != null)
				session = new Session(this, session.getUser(), accounts.getString(session.getUser()));
	    	else
	    		session = new Session(this);
		}
		else
			session.refresh();
	}
	
	private void getBoardID(String url) {
		wtl("getBoardID fired");
		// board example: http://m.gamefaqs.com/boards/400-current-events
		String boardUrl = url.substring(29);
		
		int i = boardUrl.indexOf('/');
		if (i != -1) {
			String replacer = boardUrl.substring(i);
			boardUrl = boardUrl.replace(replacer, "");
		}
		
		i = boardUrl.indexOf('?');
		if (i != -1) {
			String replacer = boardUrl.substring(i);
			boardUrl = boardUrl.replace(replacer, "");
		}
		i = boardUrl.indexOf('#');
		if (i != -1) {
			String replacer = boardUrl.substring(i);
			boardUrl = boardUrl.replace(replacer, "");
		}
		
		boardID = boardUrl;
		wtl("boardID: " + boardID);
		wtl("getBoardID finishing");
	}
	
	private void getTopicID(String url) {
		wtl("getTopicID fired");
		// topic example: http://m.gamefaqs.com/boards/400-current-events/64300205
		String topicUrl = url.substring(url.indexOf('/', 29) + 1);
		int i = topicUrl.indexOf('/');
		if (i != -1) {
			String replacer = topicUrl.substring(i);
			topicUrl = topicUrl.replace(replacer, "");
		}
		i = topicUrl.indexOf('?');
		if (i != -1) {
			String replacer = topicUrl.substring(i);
			topicUrl = topicUrl.replace(replacer, "");
		}
		i = topicUrl.indexOf('#');
		if (i != -1) {
			String replacer = topicUrl.substring(i);
			topicUrl = topicUrl.replace(replacer, "");
		}
		topicID = topicUrl;
		wtl("topicID: " + topicID);
		wtl("getTopicID finishing");
	}
	
	private void getMessageID(String url) {
		wtl("getMessageID fired");
		// topic example: http://m.gamefaqs.com/boards/400-current-events/64300205
		String messageUrl = url.substring(url.indexOf('/', 29) + 1);
		messageUrl = messageUrl.substring(messageUrl.indexOf('/') + 1);
		int i = messageUrl.indexOf('?');
		if (i != -1) {
			String replacer = messageUrl.substring(i);
			messageUrl = messageUrl.replace(replacer, "");
		}
		i = messageUrl.indexOf('#');
		if (i != -1) {
			String replacer = messageUrl.substring(i);
			messageUrl = messageUrl.replace(replacer, "");
		}
		messageID = messageUrl;
		wtl("messageID: " + messageID);
		wtl("getMessageID finishing");
	}
	
	@Override
	public void onBackPressed() {
		postCleanup();
	    goBack();
	}
	
	public void backClicked(View view) {
		view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
		postCleanup();
		goBack();
	}

	private void goBack() {
		if (session.canGoBack()) {
			wtl("back pressed, history exists, going back");
			session.goBack(false);
		}
		else {
			wtl("back pressed, no history, exiting app");
			session = null;
		    this.finish();
		}
	}
	
	/** Adds menu items */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        refreshIcon = menu.getItem(0);
        return true;
    }
    
    /** fires when a menu option is selected */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.refresh:
        	if (session.getLastPath() == null) {
    			if (session.getUser() != null)
    				session = new Session(this, session.getUser(), accounts.getString(session.getUser()));
    	    	else
    	    		session = new Session(this);
    		}
    		else
    			session.refresh();
        	return true;
        	
        case R.id.changeAccount:
        	showDialog(CHANGE_LOGGED_IN_DIALOG);
        	return true;
        	
        case R.id.changeSettings:
        	startActivity(new Intent(this, GRSettings.class));
        	return true;
        	
        case R.id.openInBrowser:
        	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(session.getLastPath()));
			startActivity(browserIntent);
        	return true;
        	
        case R.id.exit:
        	finish();
        	return true;
        	
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private Button pmSendButton, pmCancelButton;
    private ProgressBar pmProgress;
    /** creates dialogs */
    @Override
    protected Dialog onCreateDialog(int id) {
    	Dialog dialog = null;
    	
    	switch (id) {
    	case CHANGE_LOGGED_IN_DIALOG:
    		AlertDialog.Builder accountChanger = new AlertDialog.Builder(this);
    		
    		String[] keys = accounts.getKeys();
    		
    		final String[] usernames = new String[keys.length + 1];
    		usernames[0] = "Log Out";
    		for (int i = 1; i < usernames.length; i++)
    			usernames[i] = keys[i - 1].toString();
    		
    		final String currUser = session.getUser();
    		int selected = 0;
    		
    		for (int x = 1; x < usernames.length; x++)
    		{
    			if (usernames[x].equals(currUser)) 
    				selected = x;
    		}
    		
    		accountChanger.setTitle("Pick an Account");
    		accountChanger.setSingleChoiceItems(usernames, selected, new DialogInterface.OnClickListener() {
    		    public void onClick(DialogInterface dialog, int item) {
    		    	if (item == 0)
    		    		session = new Session(GRAllInOne.this);
    		    	
    		    	else
    		    	{
        		        String selUser = usernames[item].toString();
        		    	if (!selUser.equals(currUser))
        		        {
        		        	session = new Session(GRAllInOne.this, selUser, accounts.getString(selUser));
        		        }
        		    }

    		    	dismissDialog(CHANGE_LOGGED_IN_DIALOG);
    		    }
    		});
    		
    		
    		dialog = accountChanger.create();
    		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				public void onDismiss(DialogInterface dialog) {
					removeDialog(CHANGE_LOGGED_IN_DIALOG);
				}
			});
    		break;
    		
    	case NEW_VERSION_DIALOG:
    		AlertDialog.Builder newVersion = new AlertDialog.Builder(this);
    		newVersion.setTitle("New Version of GameRaven found!");
    		newVersion.setMessage("Would you like to go to the download page for the new version?");
    		newVersion.setPositiveButton("Yes", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.evernote.com/shard/s252/sh/b680bb2b-64a1-426d-a98d-6cbfb846a883/75eebb4db64c6e1769dd2d0ace487a88"));
					startActivity(browserIntent);
				}
    		});
    		newVersion.setNegativeButton("No", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dismissDialog(NEW_VERSION_DIALOG);
				}
    		});
    		dialog = newVersion.create();
    		break;
    		
    	case SEND_PM_DIALOG:
    		dialog = new Dialog(this);
    		dialog.setContentView(R.layout.sendpm);
    		dialog.setTitle("Send Private Message");
    		dialog.setCancelable(false);
    		
    		final EditText to = (EditText) dialog.findViewById(R.id.spTo);
    		final EditText subject = (EditText) dialog.findViewById(R.id.spSubject);
    		final EditText message = (EditText) dialog.findViewById(R.id.spMessage);
    		final Button send = (Button) dialog.findViewById(R.id.spSend);
    		final Button cancel = (Button) dialog.findViewById(R.id.spCancel);
    		final ProgressBar progress = (ProgressBar) dialog.findViewById(R.id.spProgress);
    		
    		pmSendButton = send;
    		pmCancelButton = cancel;
    		pmProgress = progress;
    		
    		to.setText(savedTo);
    		subject.setText(savedSubject);
    		message.setText(savedMessage);
    		
    		send.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String toContent = to.getText().toString();
					String subjectContent = subject.getText().toString();
					String messageContent = message.getText().toString();
					
					if (!toContent.equals("")) {
						if (!subjectContent.equals("")) {
							if (!messageContent.equals("")) {
								savedTo = toContent;
								savedSubject = subjectContent;
								savedMessage = messageContent;
								
								send.setEnabled(false);
								cancel.setEnabled(false);
								progress.setVisibility(View.VISIBLE);
								
								session.get(NetDesc.SEND_PM_S1, "http://m.gamefaqs.com/pm/new", null);
								
							}
							else
								Toast.makeText(GRAllInOne.this, "The message can't be empty.", Toast.LENGTH_SHORT).show();
						}
						else
							Toast.makeText(GRAllInOne.this, "The subject can't be empty.", Toast.LENGTH_SHORT).show();
					}
					else
						Toast.makeText(GRAllInOne.this, "The recepient can't be empty.", Toast.LENGTH_SHORT).show();
				}
			});
    		
    		cancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismissDialog(SEND_PM_DIALOG);
				}
			});
    	
    		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				public void onDismiss(DialogInterface dialog) {
					pmSendButton = null;
					pmCancelButton = null;
					pmProgress = null;
					removeDialog(SEND_PM_DIALOG);
				}
			});
    		
    	}
    	
    	return dialog;
    }
    
    public String savedTo, savedSubject, savedMessage;
    public void pmSetup(String toIn, String subjectIn, String messageIn) {
    	if (!toIn.equals("null"))
    		savedTo = toIn;
    	else
    		savedTo = "";
    	
    	if (!subjectIn.equals("null"))
    		savedSubject = subjectIn;
    	else
    		savedSubject = "";
    	
    	if (!messageIn.equals("null"))
    		savedMessage = messageIn;
    	else
    		savedMessage = "";
    	
    	savedTo = URLDecoder.decode(savedTo);
    	savedSubject = URLDecoder.decode(savedSubject);
    	savedMessage = URLDecoder.decode(savedMessage);
    	
    	showDialog(SEND_PM_DIALOG);
    }
    
    public void pmCleanup(boolean wasSuccessful, String error) {
    	if (wasSuccessful) {
    		Toast.makeText(this, "PM sent.", Toast.LENGTH_SHORT).show();
        	dismissDialog(SEND_PM_DIALOG);
    	}
    	else {
    		Toast.makeText(this, error, Toast.LENGTH_LONG).show();
    		pmProgress.setVisibility(View.INVISIBLE);
    		pmSendButton.setEnabled(true);
    		pmCancelButton.setEnabled(true);
    	}
    }
    
    private String CSS = null;
    private void buildCSS() {
		wtl("buildCSS fired --NEL");
		
    	StringBuilder sbCSS = new StringBuilder();
    	sbCSS.append("table {width:100%; margin:0;}\n");
    	sbCSS.append("td {padding-top:8px; padding-bottom:8px;}\n");
    	sbCSS.append("tr.even {background:#bbb;}\n");
    	sbCSS.append("cite {font-style:italic; font-weight:bold;}\n");
    	sbCSS.append(".gameraven_ad {padding:0; margin:0; overflow:hidden;}\n");
    	sbCSS.append(".gameraven_img_title_wrapper {position:relative; margin:5px;}\n");
    	sbCSS.append(".gameraven_topic_img {vertical-align:center; float:left; position:absolute; top:50%; margin-top:-9.5px;}\n");
    	sbCSS.append(".gameraven_topic_title {padding-left:5px; margin-left:18px;}\n");
    	sbCSS.append(".gameraven_table {text-align:center;}\n");
    	sbCSS.append(".gameraven_table .gameraven_2cells {width:50%;}\n");
    	sbCSS.append(".gameraven_first_page {float:left; padding-left:15px;}\n");
    	sbCSS.append(".gameraven_last_page {float:right; padding-right:15px;}\n");
    	sbCSS.append(".gameraven_poll_option {margin-top:5px; margin-bottom:5px;}\n");
    	sbCSS.append(".gameraven_username {font-weight:bold;}\n");
    	sbCSS.append("blockquote {margin-right:0px; padding:2px 0px 2px 6px; border-left:1px solid black;}\n");
    	sbCSS.append(".fspoiler {background-color:#333; color:#333;}\n");
    	sbCSS.append(".fspoiler:hover {color:#fff;}\n");
    	sbCSS.append(".fspoiler:active {color:#fff;}\n");
    	sbCSS.append(".board_poll {margin-bottom:15px;}\n");
    	sbCSS.append(".poll_bar {background-color:blue; float:left; text-align:right; height:9px;}\n");
    	sbCSS.append(".poll_head {margin-bottom:5px;}\n");
    	sbCSS.append(".poll_foot_right {margin-top:5px;}\n");
    	sbCSS.append(".msg_body {overflow-x:hidden;}\n");
    	
    	sbCSS.append(settingsPref.getString("cssContent", ""));
    	
    	CSS = sbCSS.toString();
    }
    
    public static void setNeedToBuildCSS() {
    	needToBuildCSS = true;
    }

	private static SimpleDateFormat timingFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss.SSS z", Locale.US);
    public void wtl(String msg) {
    	msg = msg.replaceAll("\\\\n", "(nl)");
		msg = timingFormat.format(new Date()) + "// " + msg;
		Log.d("logger", msg);
    }
    
    @Override
    public boolean onSearchRequested() {
    	switch (session.getLastDesc()) {
    	case BOARD:
    	case BOARD_JUMPER:
    	case GAME_SEARCH:
    		return super.onSearchRequested();
    		
    	default:
    		return false;
    	}
    }
    
    private int userLevel = 0;
    private void updateUserLevel(Document doc) {
    	try {
    		String userString = doc.getElementsByClass("user").first().getAllElements().get(1).text();
        	int open = userString.indexOf('(');
        	int close = userString.indexOf(')');
			userLevel = Integer.parseInt(userString.substring(open + 1, close));
		} catch (Exception e) {
			userLevel = 0;
			try {
	    		String userString = doc.getElementsByClass("user_panel").first().getElementsByTag("a").first().text();
	        	int open = userString.indexOf('(');
	        	int close = userString.indexOf(')');
				userLevel = Integer.parseInt(userString.substring(open + 7, close));
			} catch (Exception ex) {
				userLevel = 0;
			}
		}
		wtl("user level: " + userLevel);
    }
    
    public String getSig() {
    	String sig = "";
    	
    	if (session != null) {
			if (session.getUser() != null)
				sig = settingsPref.getString("customSig" + session.getUser(), "");
		}
    	
    	if (sig.equals(""))
    		sig = settingsPref.getString("customSig", "");
    	
		if (sig.equals(""))
    		sig = defaultSig;
		
		try {
			sig = sig.replace("*grver*", this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			sig = sig.replace("*grver*", "");
			e.printStackTrace();
		}
		
		return sig;
    }
    
    public void tryCaught(final String stacktrace) {
    	AlertDialog.Builder b = new AlertDialog.Builder(this);
    	b.setTitle("Error");
    	b.setMessage("You've run into a bug! Would you like to email the stack trace to the developer? If so, please " +
    			     "include in the email what it was you were doing when the error occurred.");
    	b.setNegativeButton("Cancel", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
    	b.setPositiveButton("Email to dev", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("message/rfc822");
				i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"ioabsoftware@gmail.com"});
				i.putExtra(Intent.EXTRA_SUBJECT, "GameRaven Error Report");
				i.putExtra(Intent.EXTRA_TEXT   , stacktrace);
				try {
				    startActivity(Intent.createChooser(i, "Send mail..."));
				} catch (android.content.ActivityNotFoundException ex) {
				    Toast.makeText(GRAllInOne.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
				}
			}
		});
    	b.create().show();
    }

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		String jumpingTo = navList[itemPosition];
		if (jumpingTo.equals("Board Jumper"))
			session.get(NetDesc.BOARD_JUMPER, Session.ROOT + "/boards", null);
		else if (jumpingTo.equals("AMP List"))
			session.get(NetDesc.AMP_LIST, GRWebClient.buildAMPLink(), null);
		else if (jumpingTo.equals("PM Inbox"))
			session.get(NetDesc.PM_INBOX, "http://m.gamefaqs.com/pm/", null);
		else if (jumpingTo.equals("Jump To…"))
			wtl("resetting nav jumper");
		else
			Toast.makeText(this, "jump id not recognized: " + jumpingTo, Toast.LENGTH_SHORT).show();
		
		aBar.setSelectedNavigationItem(0);
		
		return true;
	}
	
}
