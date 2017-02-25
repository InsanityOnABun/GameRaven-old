package com.ioabsoftware.gameraven.networking;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.ioabsoftware.gameraven.AllInOneV2;
import com.ioabsoftware.gameraven.BuildConfig;
import com.ioabsoftware.gameraven.db.History;
import com.ioabsoftware.gameraven.db.HistoryDBAdapter;
import com.ioabsoftware.gameraven.util.DocumentParser;
import com.ioabsoftware.gameraven.util.FinalDoc;
import com.ioabsoftware.gameraven.util.Theming;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.ConnectionClosedException;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;

import de.keyboardsurfer.android.widget.crouton.Crouton;

/**
 * Session is used to establish and maintain GFAQs sessions, and to send GET and POST requests.
 *
 * @author Charles Rosaaen, Insanity On A Bun Software
 */
public class Session implements FutureCallback<Response<FinalDoc>> {

    /**
     * The root of GFAQs.
     */
    public static final String ROOT = "https://www.gamefaqs.com";

    private String lastAttemptedPath = "not set";

    public String getLastAttemptedPath() {
        return lastAttemptedPath;
    }

    private NetDesc lastAttemptedDesc = NetDesc.UNSPECIFIED;

    public NetDesc getLastAttemptedDesc() {
        return lastAttemptedDesc;
    }

    /**
     * The latest page, with excess get data.
     */
    private String lastPath = null;

    /**
     * Get's the path of the latest page.
     */
    public String getLastPath() {
        return lastPath;
    }

    /**
     * Get's the path of the latest page, stripped of any GET data.
     */
    public String getLastPathWithoutData() {
        if (lastPath.contains("?"))
            return lastPath.substring(0, lastPath.indexOf('?'));
        else
            return lastPath;
    }

    /**
     * The latest Response body as an array of bytes.
     */
    private byte[] lastResBodyAsBytes = null;

    /**
     * The latest description.
     */
    private NetDesc lastDesc = null;

    /**
     * Get's the description of the latest page.
     */
    public NetDesc getLastDesc() {
        return lastDesc;
    }

    /**
     * The name of the user for this session.
     */
    private static String user = null;

    /**
     * Get's the name of the session user.
     */
    public static String getUser() {
        return user;
    }

    public static boolean isLoggedIn() {
        return user != null;
    }

    private static int userLevel = 0;

    public static boolean userCanDeleteClose() {
        return userLevel > 13;
    }

    public static boolean userCanViewAMP() {
        return userLevel > 14;
    }

    public static boolean userCanMarkMsgs() {
        return userLevel > 19;
    }

    public static boolean userCanEditMsgs() {
        return userLevel > 19;
    }

    /**
     * Quickpost and create poll topics
     */
    public static boolean userHasAdvancedPosting() {
        return userLevel > 29;
    }

    public static boolean applySavedScroll;
    public static int[] savedScrollVal;

    /**
     * The password of the user for this session.
     */
    private String password = null;

    private String sessionKey;
    public String getSessionKey() {return sessionKey;}

    /**
     * The current activity.
     */
    private AllInOneV2 aio;

    private boolean addToHistory = true;

    public void forceNoHistoryAddition() {
        if (BuildConfig.DEBUG) AllInOneV2.wtl("forcing history addition off");
        addToHistory = false;
    }

    private HistoryDBAdapter hAdapter;

    public static String RESUME_INIT_URL = "RESUME-SESSION";
    private String initUrl = null;
    private NetDesc initDesc = null;


    /**********************************************
     * START METHODS
     **********************************************/

    /**
     * Create a new session with no user logged in
     * that starts at the homepage.
     */
    public Session(AllInOneV2 aioIn) {
        this(aioIn, null, null);
    }

    /**
     * Construct a new session for the specified user,
     * using the specified password, that finishes on
     * the GFAQs homepage.
     *
     * @param userIn     The user for this session.
     * @param passwordIn The password for this session.
     */
    public Session(AllInOneV2 aioIn, String userIn, String passwordIn) {
        this(aioIn, userIn, passwordIn, null, null);
    }

    /**
     * Construct a new session for the specified user,
     * using the specified password, that finishes on
     * initUrlIn using initDescIn. Passing null for
     * initUrlIn will finish on the GFAQs homepage.
     *
     * @param userIn     The user for this session.
     * @param passwordIn The password for this session.
     * @param initUrlIn  The URL to load once successfully logged in.
     * @param initDescIn The desc to use once successfully logged in.
     */
    public Session(AllInOneV2 aioIn, String userIn, String passwordIn, String initUrlIn, NetDesc initDescIn) {
        initUrl = initUrlIn;
        initDesc = initDescIn;
        finalConstructor(aioIn, userIn, passwordIn);
    }

    /**
     * Final construction method.
     *
     * @param aioIn      The current activity
     * @param userIn     Username, or null if no user
     * @param passwordIn Password, or null if no user
     */
    private void finalConstructor(AllInOneV2 aioIn, String userIn, String passwordIn) {
        aio = aioIn;
        if (BuildConfig.DEBUG) AllInOneV2.wtl("NEW SESSION");
        aio.navDrawerReset();

        netManager = (ConnectivityManager) aio.getSystemService(Context.CONNECTIVITY_SERVICE);

        hAdapter = new HistoryDBAdapter();
        openHistoryDB();

        if (initUrl == null || !initUrl.equals(RESUME_INIT_URL))
            hAdapter.clearTable();

        user = userIn;
        password = passwordIn;

        // reset the Session unread PM and TT counters
        AllInOneV2.getSettingsPref().edit().putInt("unreadPMCount", 0).apply();
        AllInOneV2.getSettingsPref().edit().putInt("unreadTTCount", 0).apply();

        // clear out cookies
        Ion.getDefault(aio).getCookieMiddleware().clear();

        if (user == null) {
            if (BuildConfig.DEBUG) AllInOneV2.wtl("session constructor, user is null, starting logged out session");
            get(NetDesc.BOARD_JUMPER, ROOT + "/boards/");
            aio.setLoginName(user);
        } else {
            if (BuildConfig.DEBUG) AllInOneV2.wtl("session constructor, user is not null, starting logged in session");
            get(NetDesc.LOGIN_S1, ROOT + "/boards/");
            aio.setLoginName(user);
            aio.showLoggingInDialog(user);
        }
    }

    /**
     * Builds a URL based on path.
     *
     * @param path The path to build a URL off of. Can
     *             be relative or absolute. If relative, can start
     *             with a forward slash or not.
     * @return The correct absolute URL for the specified
     * path.
     */
    public static String buildURL(String path, NetDesc desc) {
        if (!path.contains("www.gamefaqs.com") && path.contains("gamefaqs.com"))
            path = path.replace("gamefaqs.com", "www.gamefaqs.com");

        if (path.contains("http://www.gamefaqs.com")) {
            path = path.replace("http://www.gamefaqs.com", "https://www.gamefaqs.com");
        }

        if (desc == NetDesc.BOARD && path.matches(".*\\d$")) {
            path += "-";
        }

        // path is absolute, return it
        if (path.startsWith("http"))
            return path;

        // add a forward slash to path if needed
        if (!path.startsWith("/"))
            path = '/' + path;

        // return absolute path
        return ROOT + path;
    }

    private ConnectivityManager netManager;

    public boolean hasNetworkConnection() {
        NetworkInfo netInfo = netManager.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }


    private Future currentNetworkTask;
    /**
     * Sends a GET request to a specified page.
     *
     * @param desc Description of this request, to properly handle the response later.
     * @param path The path to send the request to.
     */
    public void get(NetDesc desc, String path) {
        if (hasNetworkConnection()) {
            if (desc != NetDesc.MODHIST) {
                if (currentNetworkTask != null && !currentNetworkTask.isDone())
                    currentNetworkTask.cancel(true);

                lastAttemptedPath = path;
                lastAttemptedDesc = desc;

                preExecuteSetup(desc);

                currentNetworkTask = Ion.with(aio)
                        .load("GET", buildURL(path, desc))
                        .as(new DocumentParser())
                        .withResponse()
                        .setCallback(this);

            } else
                aio.genError("Page Unsupported", "The moderation history page is currently unsupported in-app. Sorry.", "Ok");

        } else
            aio.noNetworkConnection();
    }

    /**
     * Sends a POST request to a specified page.
     *
     * @param desc Description of this request, to properly handle the response later.
     * @param path The path to send the request to.
     * @param data The extra data to send along.
     */
    public void post(NetDesc desc, String path, Map<String, List<String>> data) {
        if (hasNetworkConnection()) {
            if (desc != NetDesc.MODHIST) {
                if (currentNetworkTask != null && !currentNetworkTask.isDone())
                    currentNetworkTask.cancel(true);

                preExecuteSetup(desc);
                currentNetworkTask = Ion.with(aio)
                        .load("POST", buildURL(path, desc))
                        .setBodyParameters(data)
                        .as(new DocumentParser())
                        .withResponse()
                        .setCallback(this);
            } else
                aio.genError("Page Unsupported", "The moderation history page is currently unsupported in-app. Sorry.", "Ok");

        } else
            aio.noNetworkConnection();
    }

    private NetDesc currentDesc;

    /**
     * onCompleted is called by the Future with the result or exception of the asynchronous operation.
     *
     * @param e      Exception encountered by the operation
     * @param result Result returned from the operation
     */
    @Override
    public void onCompleted(Exception e, Response<FinalDoc> result) {
        if (e != null && e instanceof CancellationException)
            return;

        NetDesc thisDesc = currentDesc;
        handleNetworkResult(e, thisDesc, result);
        postExecuteCleanup(thisDesc);
    }

    private void preExecuteSetup(NetDesc desc) {
        currentDesc = desc;
        switch (desc) {
            case AMP_LIST:
            case TRACKED_TOPICS:
            case BOARD:
            case BOARD_JUMPER:
            case TOPIC:
            case GAME_SEARCH:
            case BOARD_LIST:
            case MESSAGE_DETAIL:
            case USER_DETAIL:
            case USER_TAG:
            case MODHIST:
            case PM_INBOX:
            case PM_INBOX_DETAIL:
            case PM_OUTBOX:
            case PM_OUTBOX_DETAIL:
            case MSG_MARK:
            case TOPIC_CLOSE:
            case MSG_DELETE:
            case LOGIN_S1:
            case EDIT_MSG:
            case MSG_POST_S1:
            case TOPIC_POST_S1:
            case NOTIFS_PAGE:
            case NOTIFS_CLEAR:
            case MENTIONS_PAGE:
            case UNSPECIFIED:
                aio.preExecuteSetup(desc);
                break;

            case LOGIN_S2:
            case MSG_POST_S3:
            case TOPIC_POST_S3:
            case VERIFY_ACCOUNT_S1:
            case VERIFY_ACCOUNT_S2:
            case PM_SEND_S1:
            case PM_SEND_S2:
                break;
        }
    }

    private void handleNetworkResult(Exception e, NetDesc desc, Response<FinalDoc> result) {
        if (BuildConfig.DEBUG) AllInOneV2.wtl("session hNR fired, desc: " + desc.name());
        try {
            if (e != null)
                throw e;

            if (result != null && result.getResult() != null && result.getResult().doc != null) {

                if (lastDesc == NetDesc.LOGIN_S2)
                    aio.dismissLoginDialog();

                result.getResult().doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

                if (BuildConfig.DEBUG) AllInOneV2.wtl("parsing res");
                Document doc = result.getResult().doc;
                String resUrl = result.getRequest().getUri().toString();
                if (BuildConfig.DEBUG) AllInOneV2.wtl("resUrl: " + resUrl);

                if (BuildConfig.DEBUG) AllInOneV2.wtl("checking if res does not start with root");
                if (!resUrl.startsWith(ROOT)) {
                    AlertDialog.Builder b = new AlertDialog.Builder(aio);
                    b.setTitle("Redirected");
                    b.setMessage("The request was redirected somewhere away from GameFAQs. " +
                            "This usually happens if you're connected to a network that requires a login, " +
                            "such as a paid-for wifi service. Click below to open the page in your browser.\n" +
                            "\n" +
                            "Redirect: " + resUrl);

                    final String path = resUrl;
                    b.setPositiveButton("Open Page In Browser", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(path));
                            aio.startActivity(browserIntent);
                            aio.finish();
                        }
                    });

                    b.create().show();
                    return;
                }

                if (BuildConfig.DEBUG) AllInOneV2.wtl("checking if pRes contains captcha");
                if (!doc.select("header.page_header:contains(CAPTCHA)").isEmpty()) {

                    String captcha = doc.select("iframe").outerHtml();
                    final String key = doc.getElementsByAttributeValue("name", "key").attr("value");

                    AlertDialog.Builder b = new AlertDialog.Builder(aio);
                    b.setTitle("CAPTCHA Required");

                    LinearLayout wrapper = new LinearLayout(aio);
                    wrapper.setOrientation(LinearLayout.VERTICAL);

                    WebView web = new WebView(aio);
                    web.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                    web.loadDataWithBaseURL(resUrl,
                            "<p>There have been multiple unsuccessful login attempts!</p>" + captcha,
                            "text/html",
                            null, null);
                    wrapper.addView(web);

                    final EditText form = new EditText(aio);
                    form.setHint("Enter confirmation code (NOT CAPTCHA!!!) here");
                    wrapper.addView(form);

                    b.setView(wrapper);

                    b.setPositiveButton("Login", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            HashMap<String, List<String>> loginData = new HashMap<>();
                            // "EMAILADDR", user, "PASSWORD", password, "path", lastPath, "key", key
                            loginData.put("EMAILADDR", Collections.singletonList(user));
                            loginData.put("PASSWORD", Collections.singletonList(password));
                            loginData.put("path", Collections.singletonList(ROOT));
                            loginData.put("key", Collections.singletonList(key));
                            loginData.put("recaptcha_challenge_field", Collections.singletonList(form.getText().toString()));
                            loginData.put("recaptcha_response_field", Collections.singletonList("manual_challenge"));

                            post(NetDesc.LOGIN_S2, "/user/login_captcha.html", loginData);
                        }
                    });

                    b.create().show();
                    return;
                }

                if (BuildConfig.DEBUG) AllInOneV2.wtl("checking for non-200 http response code");
                int responseCode = result.getHeaders().code();
                if (BuildConfig.DEBUG && responseCode != 200)
                    Crouton.showText(aio, "HTTP Response Code: " + responseCode, Theming.croutonStyle());

                if (responseCode != 200) {
                    if (responseCode == 404) {
                        if (BuildConfig.DEBUG) AllInOneV2.wtl("status code 404");
                        Elements paragraphs = doc.getElementsByTag("p");
                        aio.genError("404 Error", paragraphs.get(1).text() + "\n\n" + paragraphs.get(2).text(), "Ok");
                        return;
                    } else if (responseCode == 403) {
                        if (BuildConfig.DEBUG) AllInOneV2.wtl("status code 403");
                        Elements paragraphs = doc.getElementsByTag("p");
                        aio.genError("403 Error", paragraphs.get(1).text() + "\n\n" + paragraphs.get(2).text(), "Ok");
                        return;
                    } else if (responseCode == 401) {
                        if (BuildConfig.DEBUG) AllInOneV2.wtl("status code 401");
                        if (lastDesc == NetDesc.LOGIN_S2) {
                            forceSkipAIOCleanup();
                            get(NetDesc.BOARD_JUMPER, "/boards");
                        } else {
                            Elements paragraphs = doc.getElementsByTag("p");
                            aio.genError("401 Error", paragraphs.get(1).text() + "\n\n" + paragraphs.get(2).text(), "Ok");
                        }
                        return;
                    }
                }

                if (BuildConfig.DEBUG) AllInOneV2.wtl("checking for 408, 503, GameFAQs is Down pages");
                Element firstHeader = doc.getElementsByTag("h1").first();
                if (firstHeader != null && firstHeader.text().equals("408 Request Time-out")) {
                    if (BuildConfig.DEBUG) AllInOneV2.wtl("status code 408");
                    aio.genError("408 Error", "Your browser didn't send a complete request in time.", "Ok");
                    return;
                }

                if (doc.title().equals("GameFAQs - 503 - Temporarily Unavailable")) {
                    aio.genError("503 Error", "GameFAQs is experiencing some temporary difficulties with " +
                            "the site. Please wait a few seconds before refreshing this page to try again.", "Ok");

                    return;
                } else if (doc.title().equals("GameFAQs is Down")) {
                    aio.genError("GameFAQs is Down", "GameFAQs is experiencing an outage at the moment - " +
                            "the servers are overloaded and unable to serve pages. Hopefully, this is a " +
                            "temporary problem, and will be rectified by the time you refresh this page.", "Ok");

                    return;
                }

                if (BuildConfig.DEBUG) AllInOneV2.wtl("checking for suspended, banned, and new accounts, " +
                        "as well as register.html?miss=1 page");
                if (resUrl.contains("account_suspended.html")) {
                    aio.genError("Account Suspended", "Your account seems to be suspended. Please " +
                            "log in to your account in a web browser for more details.", "Ok");

                    return;
                } else if (resUrl.contains("account_banned.html")) {
                    aio.genError("Account Banned", "Your account seems to be banned. Please " +
                            "log in to your account in a web browser for more details.", "Ok");

                    return;
                } else if (resUrl.contains("welcome.php")) {
                    aio.genError("New Account", "It looks like this is a new account. Welcome to GameFAQs! " +
                            "There are some ground rules you'll have to go over first before you can get " +
                            "access to the message boards. Please log in to your account in a web browser " +
                            "and access the message boards there to view and accept the site terms and rules.", "Ok");

                    return;
                } else if (resUrl.contains("register.html?miss=1")) {
                    aio.genError("Login Required", "You've just tried to access a feature that requires a " +
                            "GameFAQs account. You can manage your accounts and log in through the navigation " +
                            "drawer. If you are currently logged into an account, try removing the account " +
                            "from the app and re-adding it.", "Ok");

                    return;
                }

                updateUserLevel(doc);

                switch (desc) {
                    case AMP_LIST:
                    case TRACKED_TOPICS:
                    case BOARD:
                    case BOARD_JUMPER:
                    case TOPIC:
                    case GAME_SEARCH:
                    case BOARD_LIST:
                    case MESSAGE_DETAIL:
                    case USER_DETAIL:
                    case MODHIST:
                    case PM_INBOX:
                    case PM_INBOX_DETAIL:
                    case PM_OUTBOX:
                    case PM_OUTBOX_DETAIL:
                    case UNSPECIFIED:
                    case LOGIN_S1:
                    case LOGIN_S2:
                    case MSG_DELETE:
                    case EDIT_MSG:
                    case MSG_POST_S1:
                    case MSG_POST_S3:
                    case TOPIC_POST_S1:
                    case TOPIC_POST_S3:
                    case VERIFY_ACCOUNT_S1:
                    case VERIFY_ACCOUNT_S2:
                    case NOTIFS_PAGE:
                    case MENTIONS_PAGE:
                        if (BuildConfig.DEBUG) AllInOneV2.wtl("addToHistory unchanged: " + addToHistory);
                        break;

                    case USER_TAG:
                    case MSG_MARK:
                    case TOPIC_CLOSE:
                    case PM_SEND_S1:
                    case PM_SEND_S2:
                    case NOTIFS_CLEAR:
                        if (BuildConfig.DEBUG) AllInOneV2.wtl("setting addToHistory to false based on current NetDesc");
                        addToHistory = false;
                        break;
                }

                if (addToHistory) {
                    addHistory();
                }

                switch (desc) {
                    case AMP_LIST:
                    case TRACKED_TOPICS:
                    case BOARD:
                    case BOARD_JUMPER:
                    case TOPIC:
                    case GAME_SEARCH:
                    case BOARD_LIST:
                    case MESSAGE_DETAIL:
                    case USER_DETAIL:
                    case MODHIST:
                    case PM_INBOX:
                    case PM_INBOX_DETAIL:
                    case PM_OUTBOX:
                    case PM_OUTBOX_DETAIL:
                    case MSG_DELETE:
                    case UNSPECIFIED:
                    case LOGIN_S1:
                    case LOGIN_S2:
                    case EDIT_MSG:
                    case MSG_POST_S1:
                    case MSG_POST_S3:
                    case TOPIC_POST_S1:
                    case TOPIC_POST_S3:
                    case VERIFY_ACCOUNT_S1:
                    case VERIFY_ACCOUNT_S2:
                    case NOTIFS_PAGE:
                    case MENTIONS_PAGE:
                        if (BuildConfig.DEBUG) AllInOneV2.wtl("beginning lastDesc, lastRes, etc. setting");

                        lastDesc = desc;
                        lastResBodyAsBytes = result.getResult().bytes;
                        lastPath = resUrl;

                        // replace boardaction part of url, don't want it being added to history
                        if (lastPath.contains("/boardaction/"))
                            lastPath = lastPath.replace("/boardaction/", "/boards/");

                        if (BuildConfig.DEBUG) AllInOneV2.wtl("finishing lastDesc, lastRes, etc. setting");
                        break;


                    case USER_TAG:
                    case MSG_MARK:
                    case TOPIC_CLOSE:
                    case PM_SEND_S1:
                    case PM_SEND_S2:
                    case NOTIFS_CLEAR:
                        if (BuildConfig.DEBUG) AllInOneV2.wtl("not setting lastDesc, lastRes, etc.");
                        break;

                }

                // reset history flag
                addToHistory = true;

                Element keyElem = doc.getElementsByAttributeValue("name", "key").first();
                if (keyElem != null)
                    sessionKey = doc.getElementsByAttributeValue("name", "key").first().attr("value");

                switch (desc) {
                    case LOGIN_S1:
                        if (BuildConfig.DEBUG) AllInOneV2.wtl("session hNR determined this is login step 1");
                        String loginKey = doc.getElementsByAttributeValue("name", "key").attr("value");

                        HashMap<String, List<String>> loginData = new HashMap<>();
                        // "EMAILADDR", user, "PASSWORD", password, "path", lastPath, "key", key
                        loginData.put("EMAILADDR", Collections.singletonList(user));
                        loginData.put("PASSWORD", Collections.singletonList(password));
                        loginData.put("path", Collections.singletonList(buildURL("answers", NetDesc.UNSPECIFIED)));
                        loginData.put("key", Collections.singletonList(loginKey));

                        if (BuildConfig.DEBUG) AllInOneV2.wtl("finishing login step 1, sending step 2");
                        post(NetDesc.LOGIN_S2, "/user/login", loginData);
                        break;

                    case LOGIN_S2:
                        if (BuildConfig.DEBUG) AllInOneV2.wtl("session hNR determined this is login step 2");
                        aio.setAMPLinkVisible(userCanViewAMP());

                        if (initUrl != null) {
                            if (BuildConfig.DEBUG) AllInOneV2.wtl("loading previous page");
                            if (initUrl.equals(RESUME_INIT_URL) && canGoBack()) {
                                aio.dismissLoginDialog();
                                goBack(true);
                                aio.setNavDrawerVisibility(isLoggedIn());
                            }
                            else
                                get(initDesc, initUrl);
                        } else if (userCanViewAMP() && AllInOneV2.getSettingsPref().getBoolean("startAtAMP", false)) {
                            if (BuildConfig.DEBUG) AllInOneV2.wtl("loading AMP");
                            get(NetDesc.AMP_LIST, AllInOneV2.buildAMPLink());
                        } else {
                            if (BuildConfig.DEBUG) AllInOneV2.wtl("loading board jumper");
                            get(NetDesc.BOARD_JUMPER, "/boards");
                        }

                        break;

                    case MSG_POST_S1:
                    case EDIT_MSG:
                        if (BuildConfig.DEBUG) AllInOneV2.wtl("session hNR determined this is post message step 1");

                        HashMap<String, List<String>> msg1Data = new HashMap<>();
                        msg1Data.put("messagetext", Collections.singletonList(aio.getSavedPostBody()));
                        msg1Data.put("key", Collections.singletonList(sessionKey));
                        msg1Data.put("post", Collections.singletonList("Post Message"));
                        if (!AllInOneV2.getSettingsPref().getBoolean("useGFAQsSig" + user, false))
                            msg1Data.put("custom_sig", Collections.singletonList(aio.getSig()));

                        post(NetDesc.MSG_POST_S3, lastPath, msg1Data);
                        break;

                    case MSG_POST_S3:
                        if (BuildConfig.DEBUG) AllInOneV2.wtl("session hNR determined this is post message step 3 (if jumping from 1 to 3, then app is quick posting)");

                        Elements msg3AutoFlag = doc.select("b:contains(There are one or more potential issues with your message)");
                        Elements msg3Error = doc.select("b:contains(There was an error posting your message)");
                        if (!msg3Error.isEmpty()) {
                            if (BuildConfig.DEBUG) AllInOneV2.wtl("there was an error in post msg step 3, ending early");
                            aio.postError(((TextNode) msg3Error.first().nextSibling().nextSibling()).text());
                            postErrorDetected = true;
                        } else if (!msg3AutoFlag.isEmpty()) {
                            if (BuildConfig.DEBUG) AllInOneV2.wtl("autoflag got tripped in post msg step 3, getting data and showing autoflag dialog");
                            String msg = ((TextNode) msg3AutoFlag.first().nextSibling().nextSibling()).text();

                            HashMap<String, List<String>> msg3Data = new HashMap<>();
                            msg3Data.put("messagetext", Collections.singletonList(aio.getSavedPostBody()));
                            msg3Data.put("post", Collections.singletonList("Post Message"));
                            msg3Data.put("key", Collections.singletonList(sessionKey));
                            msg3Data.put("override", Collections.singletonList("checked"));
                            if (!AllInOneV2.getSettingsPref().getBoolean("useGFAQsSig" + user, false))
                                msg3Data.put("custom_sig", Collections.singletonList(aio.getSig()));

                            showAutoFlagWarning(lastPath, msg3Data, NetDesc.MSG_POST_S3, msg);
                            postErrorDetected = true;
                        } else {
                            if (BuildConfig.DEBUG) AllInOneV2.wtl("finishing post message step 3, refreshing topic");
                            lastDesc = NetDesc.TOPIC;
                            aio.enableGoToUrlDefinedPost();
                            Crouton.showText(aio, "Message posted.", Theming.croutonStyle());
                            processTopicsAndMessages(doc, resUrl, NetDesc.TOPIC);
                        }
                        break;

                    case TOPIC_POST_S1:
                        if (BuildConfig.DEBUG) AllInOneV2.wtl("session hNR determined this is post topic step 1");

                        HashMap<String, List<String>> tpc1Data = new HashMap<>();
                        tpc1Data.put("topictitle", Collections.singletonList(aio.getSavedPostTitle()));
                        tpc1Data.put("messagetext", Collections.singletonList(aio.getSavedPostBody()));
                        tpc1Data.put("key", Collections.singletonList(sessionKey));
                        tpc1Data.put("post", Collections.singletonList("Post Message"));
                        if (!AllInOneV2.getSettingsPref().getBoolean("useGFAQsSig" + user, false))
                            tpc1Data.put("custom_sig", Collections.singletonList(aio.getSig()));

                        if (aio.isUsingPoll()) {
                            tpc1Data.put("poll_text", Collections.singletonList(aio.getPollTitle()));
                            for (int x = 0; x < 10; x++) {
                                if (aio.getPollOptions()[x].length() != 0)
                                    tpc1Data.put("poll_option_" + (x + 1), Collections.singletonList(aio.getPollOptions()[x]));
                                else
                                    x = 11;
                            }
                            tpc1Data.put("min_level", Collections.singletonList(aio.getPollMinLevel()));
                        }

                        post(NetDesc.TOPIC_POST_S3, lastPath, tpc1Data);
                        break;

                    case TOPIC_POST_S3:
                        if (BuildConfig.DEBUG) AllInOneV2.wtl("session hNR determined this is post topic step 3 (if jumping from 1 to 3, then app is quick posting)");

                        Elements tpc3AutoFlag = doc.select("b:contains(There are one or more potential issues with your message)");
                        Elements tpc3Error = doc.select("b:contains(There was an error posting your message)");
                        if (!tpc3Error.isEmpty()) {
                            if (BuildConfig.DEBUG) AllInOneV2.wtl("there was an error in post topic step 3, ending early");
                            aio.postError(((TextNode) tpc3Error.first().nextSibling().nextSibling()).text());
                            postErrorDetected = true;
                        } else if (!tpc3AutoFlag.isEmpty()) {
                            if (BuildConfig.DEBUG) AllInOneV2.wtl("autoflag got tripped in post msg step 3, getting data and showing autoflag dialog");
                            String msg = ((TextNode) tpc3AutoFlag.first().nextSibling().nextSibling()).text();

                            HashMap<String, List<String>> tpc3Data = new HashMap<>();
                            tpc3Data.put("topictitle", Collections.singletonList(aio.getSavedPostTitle()));
                            tpc3Data.put("messagetext", Collections.singletonList(aio.getSavedPostBody()));
                            tpc3Data.put("post", Collections.singletonList("Post Message"));
                            tpc3Data.put("key", Collections.singletonList(sessionKey));
                            tpc3Data.put("override", Collections.singletonList("checked"));
                            if (!AllInOneV2.getSettingsPref().getBoolean("useGFAQsSig" + user, false))
                                tpc3Data.put("custom_sig", Collections.singletonList(aio.getSig()));

                            showAutoFlagWarning(lastPath, tpc3Data, NetDesc.TOPIC_POST_S3, msg);
                            postErrorDetected = true;
                        } else {
                            if (BuildConfig.DEBUG) AllInOneV2.wtl("finishing post topic step 3, processing new topic");
                            lastDesc = NetDesc.TOPIC;
                            Crouton.showText(aio, "Topic posted.", Theming.croutonStyle());
                            processTopicsAndMessages(doc, resUrl, NetDesc.TOPIC);
                        }
                        break;

                    case MSG_MARK:
                        String response = doc.text();
                        int start = response.indexOf("\":\"") + 3;
                        int end = response.indexOf("\",\"");
                        String markMessage = response.substring(start, end);
                        Crouton.showText(aio, markMessage, Theming.croutonStyle());
                        break;

                    case MSG_DELETE:
                        Crouton.showText(aio, "Message deleted.", Theming.croutonStyle());
                        applySavedScroll = true;
                        savedScrollVal = aio.getScrollerVertLoc();
                        lastDesc = NetDesc.TOPIC;
                        processTopicsAndMessages(doc, resUrl, NetDesc.TOPIC);
                        break;

                    case TOPIC_CLOSE:
                        Crouton.showText(aio, "Topic closed successfully.", Theming.croutonStyle());
                        goBack(true);
                        break;

                    case USER_TAG:
                    case NOTIFS_CLEAR:
                        refresh();
                        break;

                    case PM_SEND_S1:
                        HashMap<String, List<String>> pmData = new HashMap<>();
                        pmData.put("key", Collections.singletonList(sessionKey));
                        pmData.put("to", Collections.singletonList(aio.savedTo));
                        pmData.put("subject", Collections.singletonList(aio.savedSubject));
                        pmData.put("message", Collections.singletonList(aio.savedMessage));
                        pmData.put("submit", Collections.singletonList("Send Message"));

                        post(NetDesc.PM_SEND_S2, "/pm/new", pmData);
                        break;

                    case PM_SEND_S2:
                        if (doc.select("input[name=subject]").isEmpty()) {
                            aio.pmCleanup(true, null);
                        } else {
                            String error = doc.select("form[action=/pm/new]").first().previousElementSibling().text();
                            aio.pmCleanup(false, error);
                        }
                        break;

                    case TOPIC:
                        if (BuildConfig.DEBUG) AllInOneV2.wtl("session hNR determined this is a topic");
                        processTopicsAndMessages(doc, resUrl, NetDesc.TOPIC);
                        break;

                    case MESSAGE_DETAIL:
                        if (BuildConfig.DEBUG) AllInOneV2.wtl("session hNR determined this is a message");
                        processTopicsAndMessages(doc, resUrl, NetDesc.MESSAGE_DETAIL);
                        break;

                    case GAME_SEARCH:
                    case BOARD_LIST:
                    case AMP_LIST:
                    case TRACKED_TOPICS:
                    case BOARD:
                    case BOARD_JUMPER:
                    case UNSPECIFIED:
                    case USER_DETAIL:
                    case MODHIST:
                    case PM_INBOX:
                    case PM_INBOX_DETAIL:
                    case PM_OUTBOX:
                    case PM_OUTBOX_DETAIL:
                    case VERIFY_ACCOUNT_S1:
                    case VERIFY_ACCOUNT_S2:
                    case NOTIFS_PAGE:
                    case MENTIONS_PAGE:
                        if (BuildConfig.DEBUG) AllInOneV2.wtl("session hNR determined this should be handled by AIO");
                        aio.processContent(desc, doc, resUrl);
                        break;
                }
            } else {
                // connection failed for some reason, probably timed out
                if (BuildConfig.DEBUG) AllInOneV2.wtl("res was null in session hNR");
                aio.timeoutCleanup(desc);
            }
        } catch (TimeoutException timeoutEx) {
            aio.timeoutCleanup(desc);
        } catch (UnknownHostException unknownHostEx) {
            aio.genError("Unknown Host Exception", "Couldn't find the address for the specified host. " +
                    "This usually happens due to a DNS lookup error, which is outside of GameRaven's " +
                    "ability to handle. If you continue to receive this error, try resetting your network. " +
                    "If you are on wifi, you can do this by unplugging your router for 30 seconds, then plugging " +
                    "it back in. If on a cellular connection, toggle airplane mode on and off, or restart " +
                    "the phone.", "Ok");
        } catch (ConnectionClosedException connClosedEx) {
            aio.genError("Connection Closed", "The connection was closed before the the response was completed.", "Ok");
        } catch (Throwable ex) {
            ex.printStackTrace();
            String url, body;
            if (result != null) {
                try {
                    url = result.getRequest().getUri().toString();
                } catch (Exception e1) {
                    url = "uri is null";
                }
                try {
                    body = new String(result.getResult().bytes);
                } catch (Exception e1) {
                    body = "result bytes are null";
                }
            } else
                url = body = "res is null";

            aio.tryCaught(url, desc.toString(), ex, body);
        }

        if (BuildConfig.DEBUG) AllInOneV2.wtl("session hNR finishing, desc: " + desc.name());
    }

    private void addHistory() {
        if (lastPath != null) {
            switch (lastDesc) {
                case AMP_LIST:
                case TRACKED_TOPICS:
                case BOARD:
                case BOARD_JUMPER:
                case TOPIC:
                case GAME_SEARCH:
                case BOARD_LIST:
                case MESSAGE_DETAIL:
                case USER_DETAIL:
                case MODHIST:
                case PM_INBOX:
                case PM_INBOX_DETAIL:
                case PM_OUTBOX:
                case PM_OUTBOX_DETAIL:
                case MSG_DELETE:
                case NOTIFS_PAGE:
                case MENTIONS_PAGE:
                case UNSPECIFIED:
                    if (BuildConfig.DEBUG) AllInOneV2.wtl("beginning history addition");
                    int[] vLoc = aio.getScrollerVertLoc();
                    hAdapter.insertHistory(lastPath, lastDesc.name(), lastResBodyAsBytes, vLoc[0], vLoc[1]);
                    if (BuildConfig.DEBUG) AllInOneV2.wtl("finished history addition");
                    break;

                case USER_TAG:
                case MSG_MARK:
                case TOPIC_CLOSE:
                case LOGIN_S1:
                case LOGIN_S2:
                case EDIT_MSG:
                case MSG_POST_S1:
                case MSG_POST_S3:
                case TOPIC_POST_S1:
                case TOPIC_POST_S3:
                case VERIFY_ACCOUNT_S1:
                case VERIFY_ACCOUNT_S2:
                case PM_SEND_S1:
                case PM_SEND_S2:
                case NOTIFS_CLEAR:
                    if (BuildConfig.DEBUG) AllInOneV2.wtl("not adding to history");
                    break;

            }
        }
    }

    private void processTopicsAndMessages(Document doc, String resUrl, NetDesc successDesc) {
        boolean processAsBoard = false;
        if (!doc.select("p:contains(no longer available for viewing)").isEmpty()) {
            if (successDesc == NetDesc.TOPIC)
                Crouton.showText(aio, "The topic you selected is no longer available for viewing.", Theming.croutonStyle());
            else if (successDesc == NetDesc.MESSAGE_DETAIL)
                Crouton.showText(aio, "The message you selected is no longer available for viewing.", Theming.croutonStyle());

            processAsBoard = true;
        } else if (!doc.select("p:contains(Your topic has been deleted)").isEmpty()) {
            Crouton.showText(aio, "Your topic has been deleted.", Theming.croutonStyle());
            processAsBoard = true;
        }

        if (processAsBoard) {
            if (BuildConfig.DEBUG) AllInOneV2.wtl("topic or message is no longer available, treat response as a board");
            aio.processContent(NetDesc.BOARD, doc, resUrl);
        } else {
            if (BuildConfig.DEBUG) AllInOneV2.wtl("handle the topic or message in AIO");
            aio.processContent(successDesc, doc, resUrl);
        }
    }

    private boolean skipAIOCleanup = false;

    public void forceSkipAIOCleanup() {
        if (BuildConfig.DEBUG) AllInOneV2.wtl("forcing AIO cleanup skip");
        skipAIOCleanup = true;
    }

    private boolean postErrorDetected = false;

    private void postExecuteCleanup(NetDesc desc) {
        switch (desc) {
            case AMP_LIST:
            case TRACKED_TOPICS:
            case BOARD:
            case BOARD_JUMPER:
            case TOPIC:
            case MESSAGE_DETAIL:
            case USER_DETAIL:
            case USER_TAG:
            case MODHIST:
            case PM_INBOX:
            case PM_INBOX_DETAIL:
            case PM_OUTBOX:
            case PM_OUTBOX_DETAIL:
            case MSG_MARK:
            case MSG_DELETE:
            case TOPIC_CLOSE:
            case GAME_SEARCH:
            case BOARD_LIST:
            case NOTIFS_PAGE:
            case MENTIONS_PAGE:
            case UNSPECIFIED:
                if (!skipAIOCleanup)
                    aio.postExecuteCleanup(desc);

                break;

            case MSG_POST_S3:
            case TOPIC_POST_S3:
                if (!postErrorDetected)
                    aio.postExecuteCleanup((desc == NetDesc.MSG_POST_S3 ? NetDesc.TOPIC : NetDesc.BOARD));

                break;

            case LOGIN_S1:
            case LOGIN_S2:
            case EDIT_MSG:
            case MSG_POST_S1:
            case TOPIC_POST_S1:
            case VERIFY_ACCOUNT_S1:
            case VERIFY_ACCOUNT_S2:
            case PM_SEND_S1:
            case PM_SEND_S2:
            case NOTIFS_CLEAR:
                break;
        }

        skipAIOCleanup = false;
        postErrorDetected = false;
    }

    public boolean canGoBack() {
        return hAdapter.hasHistory();
    }

    public void popHistory() {
        if (canGoBack())
            hAdapter.pullHistory();
    }

    public void goBack(boolean forceReload) {
        History h = hAdapter.pullHistory();

        applySavedScroll = true;
        savedScrollVal = h.getVertPos();

        if (forceReload || AllInOneV2.getSettingsPref().getBoolean("reloadOnBack", false)) {
            forceNoHistoryAddition();
            if (BuildConfig.DEBUG) AllInOneV2.wtl("going back in history, refreshing: " + h.getDesc().name() + " " + h.getPath());
            get(h.getDesc(), h.getPath());
        } else {
            if (BuildConfig.DEBUG) AllInOneV2.wtl("going back in history: " + h.getDesc().name() + " " + h.getPath());
            lastDesc = h.getDesc();
            lastResBodyAsBytes = h.getResBodyAsBytes();
            lastPath = h.getPath();

            Document d = Jsoup.parse(new String(lastResBodyAsBytes), lastPath);
            d.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
            aio.processContent(lastDesc, d, lastPath);
        }
    }

    public void openHistoryDB() {
        hAdapter.open(aio);
    }

    public void closeHistoryDB() {
        hAdapter.close();
    }

    public void addHistoryBeforeStop() {
        addHistory();
    }

    public void setLastPathAndDesc(String path, NetDesc desc) {
        lastPath = path;
        lastDesc = desc;
    }

    public void refresh() {
        forceNoHistoryAddition();
        if (BuildConfig.DEBUG) AllInOneV2.wtl("refreshing: " + lastDesc.name() + " " + lastPath);
        applySavedScroll = true;
        savedScrollVal = aio.getScrollerVertLoc();

        int i = lastPath.indexOf('#');
        String trimmedPath;
        if (i != -1)
            trimmedPath = lastPath.substring(0, i);
        else
            trimmedPath = lastPath;

        if (lastDesc == NetDesc.AMP_LIST)
            trimmedPath = AllInOneV2.buildAMPLink();

        get(lastDesc, trimmedPath);
    }

    private void showAutoFlagWarning(final String path, final HashMap<String, List<String>> data, final NetDesc desc, String msg) {
        AlertDialog.Builder b = new AlertDialog.Builder(aio);
        b.setTitle("Post Warning");
        b.setMessage(msg);

        b.setPositiveButton("Post anyway", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                post(desc, path, data);
            }
        });

        b.setNegativeButton("Cancel", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                aio.postExecuteCleanup(desc);
            }
        });

        Dialog d = b.create();
        d.setCancelable(false);

        d.show();
    }

    private void updateUserLevel(Document doc) {
        String sc = doc.getElementsByTag("head").first().getElementsByTag("script").html();
        int start = sc.indexOf("UserLevel','") + 12;
        int end = sc.indexOf('\'', start + 1);
        if (end > start)
            userLevel = Integer.parseInt(sc.substring(start, end));

        if (BuildConfig.DEBUG) AllInOneV2.wtl("user level: " + userLevel);
    }

    public static NetDesc determineNetDesc(String url) {
        url = Session.buildURL(url, NetDesc.UNSPECIFIED);

        if (url.startsWith(Session.ROOT)) {

            // check if PM
            if (url.equals(Session.ROOT + "/pm"))
                url += "/";
            if (url.contains("/pm/")) {
                if (url.contains("/pm/sent?id=")) {
                    return NetDesc.PM_OUTBOX_DETAIL;
                } else if (url.contains("/pm/sent")) {
                    return NetDesc.PM_OUTBOX;
                } else if (url.contains("?id=")) {
                    return NetDesc.PM_INBOX_DETAIL;
                } else {
                    return NetDesc.PM_INBOX;
                }
            }
            // check if AMP or tracked topic
            else if (url.contains("/user/")) {
                if (url.contains("/messages")) {
                    return NetDesc.AMP_LIST;
                } else if (url.contains("/notifications")) {
                    return NetDesc.NOTIFS_PAGE;
                } else if (url.contains("/mentions")) {
                    return NetDesc.MENTIONS_PAGE;
                } else if (url.contains("/tracked")) {
                    return NetDesc.TRACKED_TOPICS;
                } else if (url.contains("/moderated")) {
                    return NetDesc.MODHIST;
                }
            }
            // check if this is a board or topic url
            else if (url.contains("/boards")) {
                if (url.contains("/users/")) {
                    return NetDesc.USER_DETAIL;
                } else if (url.contains("boardlist.php")) {
                    return NetDesc.BOARD_LIST;
                } else {
                    String boardUrl = url.substring(url.indexOf("boards"));
                    if (boardUrl.contains("/")) {
                        String checkForTopicSep = boardUrl.substring(boardUrl.indexOf("/") + 1);
                        if (checkForTopicSep.contains("/")) {
                            String checkForMsgSep = checkForTopicSep.substring(checkForTopicSep.indexOf("/") + 1);
                            if (checkForMsgSep.contains("/")) {
                                // should be a message
                                return NetDesc.MESSAGE_DETAIL;
                            } else {
                                // should be a topic
                                return NetDesc.TOPIC;
                            }
                        } else {
                            // should be a board
                            return NetDesc.BOARD;
                        }
                    } else {
                        // should be home
                        return NetDesc.BOARD_JUMPER;
                    }
                }
            }
        }

        return NetDesc.UNSPECIFIED;
    }
}
