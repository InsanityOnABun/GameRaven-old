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
import android.util.Log;
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
import org.jsoup.select.Elements;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
    public static final String ROOT = "http://www.gamefaqs.com";

    /**
     * Holds all cookies for the session
     */
    private Map<String, String> cookies = new LinkedHashMap<String, String>();

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
     * Get's the Response body as an array of bytes from the latest page.
     */
    public byte[] getLastResBodyAsBytes() {
        return lastResBodyAsBytes;
    }

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

    //	public static int getUserLevel()
//	{return userLevel;}
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
        return userLevel > 24;
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

    /**
     * The current activity.
     */
    private AllInOneV2 aio;

    private boolean addToHistory = true;

    public void forceNoHistoryAddition() {
        AllInOneV2.wtl("forcing history addition off");
        addToHistory = false;
    }

    private HistoryDBAdapter hAdapter;

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
        AllInOneV2.wtl("NEW SESSION");
        aio.disableNavList();

        netManager = (ConnectivityManager) aio.getSystemService(Context.CONNECTIVITY_SERVICE);

        hAdapter = new HistoryDBAdapter(aio);
        openHistoryDB();

        user = userIn;
        password = passwordIn;

        // reset the Session unread PM and TT counters
        AllInOneV2.getSettingsPref().edit().putInt("unreadPMCount", 0).apply();
        AllInOneV2.getSettingsPref().edit().putInt("unreadTTCount", 0).apply();

        // clear out cookies
        Ion.getDefault(aio).getCookieMiddleware().clear();

        if (BuildConfig.DEBUG)
            Ion.getDefault(aio).configure().setLogging("IonLogs", Log.VERBOSE);

        if (user == null) {
            AllInOneV2.wtl("session constructor, user is null, starting logged out session");
            get(NetDesc.BOARD_JUMPER, ROOT + "/boards");
            aio.setLoginName("Logged Out");
        } else {
            AllInOneV2.wtl("session constructor, user is not null, starting logged in session");
            get(NetDesc.LOGIN_S1, ROOT + "/boards");
            aio.setLoginName(user);
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


    Future currentNetworkTask;
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
                aio.genError("Page Unsupported", "The moderation history page is currently unsupported in-app. Sorry.");

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
                aio.genError("Page Unsupported", "The moderation history page is currently unsupported in-app. Sorry.");

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

    public void preExecuteSetup(NetDesc desc) {
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
            case TAG_USER:
            case MODHIST:
            case PM_INBOX:
            case PM_INBOX_DETAIL:
            case PM_OUTBOX:
            case PM_OUTBOX_DETAIL:
            case MARKMSG_S1:
            case CLOSE_TOPIC:
            case DLTMSG_S1:
            case LOGIN_S1:
            case EDIT_MSG:
            case POSTMSG_S1:
            case POSTTPC_S1:
            case UNSPECIFIED:
                aio.preExecuteSetup(desc);
                break;

            case LOGIN_S2:
            case MARKMSG_S2:
            case DLTMSG_S2:
            case POSTMSG_S2:
            case POSTMSG_S3:
            case POSTTPC_S2:
            case POSTTPC_S3:
            case VERIFY_ACCOUNT_S1:
            case VERIFY_ACCOUNT_S2:
            case SEND_PM_S1:
            case SEND_PM_S2:
                break;
        }
    }

    public void handleNetworkResult(Exception e, NetDesc desc, Response<FinalDoc> result) {
        AllInOneV2.wtl("session hNR fired, desc: " + desc.name());
        try {
            if (e != null)
                throw e;

            if (result != null) {

                AllInOneV2.wtl("parsing res");
                Document doc = result.getResult().doc;
                String resUrl = result.getRequest().getUri().toString();
                AllInOneV2.wtl("resUrl: " + resUrl);

                AllInOneV2.wtl("checking if res does not start with root");
                if (!resUrl.startsWith(ROOT)) {
                    AlertDialog.Builder b = new AlertDialog.Builder(aio);
                    b.setTitle("Redirected");
                    b.setMessage("The request was redirected somewhere away from GameFAQs. " +
                            "This usually happens if you're connected to a network that requires a login, " +
                            "such as a  paid-for wifi service. Click below to open the page in your browser.");

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

                AllInOneV2.wtl("checking if pRes contains captcha");
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
                            HashMap<String, List<String>> loginData = new HashMap<String, List<String>>();
                            // "EMAILADDR", user, "PASSWORD", password, "path", lastPath, "key", key
                            loginData.put("EMAILADDR", Arrays.asList(user));
                            loginData.put("PASSWORD", Arrays.asList(password));
                            loginData.put("path", Arrays.asList(ROOT));
                            loginData.put("key", Arrays.asList(key));
                            loginData.put("recaptcha_challenge_field", Arrays.asList(form.getText().toString()));
                            loginData.put("recaptcha_response_field", Arrays.asList("manual_challenge"));

                            post(NetDesc.LOGIN_S2, "/user/login_captcha.html", loginData);
                        }
                    });

                    b.create().show();
                    return;
                }

                Element gfaqsError = doc.select("h1.page-title").first();
                if (gfaqsError != null && gfaqsError.text().contains("Error")) {
                    if (gfaqsError.text().contains("404 Error")) {
                        AllInOneV2.wtl("status code 404");
                        Elements paragraphs = doc.getElementsByTag("p");
                        aio.genError("404 Error", paragraphs.get(1).text() + "\n\n"
                                + paragraphs.get(2).text());
                        return;
                    } else if (gfaqsError.text().contains("403 Error")) {
                        AllInOneV2.wtl("status code 403");
                        Elements paragraphs = doc.getElementsByTag("p");
                        aio.genError("403 Error", paragraphs.get(1).text() + "\n\n"
                                + paragraphs.get(2).text());
                        return;
                    } else if (gfaqsError.text().contains("401 Error")) {
                        AllInOneV2.wtl("status code 401");
                        if (lastDesc == NetDesc.LOGIN_S2) {
                            forceSkipAIOCleanup();
                            get(NetDesc.BOARD_JUMPER, "/boards");
                        } else {
                            Elements paragraphs = doc.getElementsByTag("p");
                            aio.genError("401 Error", paragraphs.get(1).text()
                                    + "\n\n" + paragraphs.get(2).text());
                        }
                        return;
                    }
                }

                Element firstHeader = doc.getElementsByTag("h1").first();
                if (firstHeader != null && firstHeader.text().equals("408 Request Time-out")) {
                    AllInOneV2.wtl("status code 408");
                    aio.genError("408 Error", "Your browser didn't send a complete request in time.");
                    return;
                }

                if (doc.title().equals("GameFAQs - 503 - Temporarily Unavailable")) {
                    aio.genError("503 Error", "GameFAQs is experiencing some temporary difficulties with " +
                            "the site. Probably because of something they did. Please wait a few " +
                            "seconds before refreshing this page to try again.");

                    return;
                }

                if (resUrl.contains("account_suspended.html")) {
                    aio.genError("Account Suspended", "Your account seems to be suspended. Please " +
                            "log in to your account in a web browser for more details.");

                    return;
                }

                if (resUrl.contains("account_banned.html")) {
                    aio.genError("Account Banned", "Your account seems to be banned. Please " +
                            "log in to your account in a web browser for more details.");

                    return;
                }

                if (resUrl.contains("welcome.php")) {
                    aio.genError("New Account", "It looks like this is a new account. Welcome to GameFAQs! " +
                            "There are some ground rules you'll have to go over first before you can get " +
                            "access to the message boards. Please log in to your account in a web browser " +
                            "and access the message boards there to view and accept the site terms and rules.");

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
                    case DLTMSG_S1:
                    case DLTMSG_S2:
                    case EDIT_MSG:
                    case POSTMSG_S1:
                    case POSTMSG_S2:
                    case POSTMSG_S3:
                    case POSTTPC_S1:
                    case POSTTPC_S2:
                    case POSTTPC_S3:
                    case VERIFY_ACCOUNT_S1:
                    case VERIFY_ACCOUNT_S2:
                        AllInOneV2.wtl("addToHistory unchanged: " + addToHistory);
                        break;

                    case TAG_USER:
                    case MARKMSG_S1:
                    case MARKMSG_S2:
                    case CLOSE_TOPIC:
                    case SEND_PM_S1:
                    case SEND_PM_S2:
                        AllInOneV2.wtl("setting addToHistory to false based on current NetDesc");
                        addToHistory = false;
                        break;
                }

                if (addToHistory) {
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
                            case UNSPECIFIED:
                                AllInOneV2.wtl("beginning history addition");
                                int[] vLoc = aio.getScrollerVertLoc();
                                hAdapter.insertHistory(lastPath, lastDesc.name(), lastResBodyAsBytes, vLoc[0], vLoc[1]);
                                AllInOneV2.wtl("finished history addition");
                                break;

                            case TAG_USER:
                            case MARKMSG_S1:
                            case MARKMSG_S2:
                            case CLOSE_TOPIC:
                            case DLTMSG_S1:
                            case DLTMSG_S2:
                            case LOGIN_S1:
                            case LOGIN_S2:
                            case EDIT_MSG:
                            case POSTMSG_S1:
                            case POSTMSG_S2:
                            case POSTMSG_S3:
                            case POSTTPC_S1:
                            case POSTTPC_S2:
                            case POSTTPC_S3:
                            case VERIFY_ACCOUNT_S1:
                            case VERIFY_ACCOUNT_S2:
                            case SEND_PM_S1:
                            case SEND_PM_S2:
                                AllInOneV2.wtl("not adding to history");
                                break;

                        }
                    }
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
                    case UNSPECIFIED:
                    case LOGIN_S1:
                    case LOGIN_S2:
                    case EDIT_MSG:
                    case POSTMSG_S1:
                    case POSTMSG_S2:
                    case POSTMSG_S3:
                    case POSTTPC_S1:
                    case POSTTPC_S2:
                    case POSTTPC_S3:
                    case VERIFY_ACCOUNT_S1:
                    case VERIFY_ACCOUNT_S2:
                        AllInOneV2.wtl("beginning lastDesc, lastRes, etc. setting");
                        lastDesc = desc;
                        lastResBodyAsBytes = result.getResult().bytes;
                        lastPath = resUrl;
                        AllInOneV2.wtl("finishing lastDesc, lastRes, etc. setting");
                        break;


                    case TAG_USER:
                    case MARKMSG_S1:
                    case MARKMSG_S2:
                    case CLOSE_TOPIC:
                    case DLTMSG_S1:
                    case DLTMSG_S2:
                    case SEND_PM_S1:
                    case SEND_PM_S2:
                        AllInOneV2.wtl("not setting lastDesc, lastRes, etc.");
                        break;

                }

                // reset history flag
                addToHistory = true;

                switch (desc) {
                    case LOGIN_S1:
                        AllInOneV2.wtl("session hNR determined this is login step 1");
                        String loginKey = doc.getElementsByAttributeValue("name", "key").attr("value");

                        HashMap<String, List<String>> loginData = new HashMap<String, List<String>>();
                        // "EMAILADDR", user, "PASSWORD", password, "path", lastPath, "key", key
                        loginData.put("EMAILADDR", Arrays.asList(user));
                        loginData.put("PASSWORD", Arrays.asList(password));
                        loginData.put("path", Arrays.asList(lastPath));
                        loginData.put("key", Arrays.asList(loginKey));

                        AllInOneV2.wtl("finishing login step 1, sending step 2");
                        post(NetDesc.LOGIN_S2, "/user/login.html", loginData);
                        break;

                    case LOGIN_S2:
                        AllInOneV2.wtl("session hNR determined this is login step 2");
                        aio.setAMPLinkVisible(userCanViewAMP());

                        if (initUrl != null) {
                            AllInOneV2.wtl("loading previous page");
                            get(initDesc, initUrl);
                        } else if (userCanViewAMP() && AllInOneV2.getSettingsPref().getBoolean("startAtAMP", false)) {
                            AllInOneV2.wtl("loading AMP");
                            get(NetDesc.AMP_LIST, AllInOneV2.buildAMPLink());
                        } else {
                            AllInOneV2.wtl("loading board jumper");
                            get(NetDesc.BOARD_JUMPER, "/boards");
                        }

                        break;

                    case POSTMSG_S1:
                    case EDIT_MSG:
                        AllInOneV2.wtl("session hNR determined this is post message step 1");
                        String msg1Key = doc.getElementsByAttributeValue("name", "key").attr("value");

                        String sig;
                        if (desc == NetDesc.EDIT_MSG)
                            sig = AllInOneV2.EMPTY_STRING;
                        else
                            sig = aio.getSig();

                        HashMap<String, List<String>> msg1Data = new HashMap<String, List<String>>();
                        msg1Data.put("messagetext", Arrays.asList(aio.getSavedPostBody()));
                        msg1Data.put("custom_sig", Arrays.asList(sig));
                        msg1Data.put("post", Arrays.asList((userHasAdvancedPosting() ? "Post without Preview" : "Preview Message")));
                        msg1Data.put("key", Arrays.asList(msg1Key));

                        Elements msg1Error = doc.getElementsContainingOwnText("There was an error posting your message:");
                        if (!msg1Error.isEmpty()) {
                            AllInOneV2.wtl("there was an error in post msg step 1, ending early");
                            aio.postError(msg1Error.first().parent().parent().text());
                            aio.postExecuteCleanup(desc);
                        } else {
                            AllInOneV2.wtl("finishing post message step 1, sending step 2");
                            post((userHasAdvancedPosting() ? NetDesc.POSTMSG_S3 : NetDesc.POSTMSG_S2), lastPath, msg1Data);
                        }
                        break;

                    case POSTMSG_S2:
                        AllInOneV2.wtl("session hNR determined this is post message step 2");
                        String msg2Key = doc.getElementsByAttributeValue("name", "key").attr("value");
                        String msgPost_id = doc.getElementsByAttributeValue("name", "post_id").attr("value");
                        String msgUid = doc.getElementsByAttributeValue("name", "uid").attr("value");

                        HashMap<String, List<String>> msg2Data = new HashMap<String, List<String>>();
                        msg2Data.put("post", Arrays.asList("Post Message"));
                        msg2Data.put("key", Arrays.asList(msg2Key));
                        msg2Data.put("post_id", Arrays.asList(msgPost_id));
                        msg2Data.put("uid", Arrays.asList(msgUid));

                        Elements msg2Error = doc.getElementsContainingOwnText("There was an error posting your message:");
                        Elements msg2AutoFlag = doc.getElementsContainingOwnText("There were one or more potential problems with your message:");
                        if (!msg2Error.isEmpty()) {
                            AllInOneV2.wtl("there was an error in post msg step 2, ending early");
                            aio.postError(msg2Error.first().parent().parent().text());
                            aio.postExecuteCleanup(desc);
                        } else if (!msg2AutoFlag.isEmpty()) {
                            AllInOneV2.wtl("autoflag got tripped in post msg step 2, showing autoflag dialog");
                            String msg = msg2AutoFlag.first().parent().parent().text();
                            showAutoFlagWarning(lastPath, msg2Data, NetDesc.POSTMSG_S3, msg);
                        } else {
                            AllInOneV2.wtl("finishing post message step 2, sending step 3");
                            post(NetDesc.POSTMSG_S3, lastPath, msg2Data);
                        }
                        break;

                    case POSTMSG_S3:
                        AllInOneV2.wtl("session hNR determined this is post message step 3 (if jumping from 1 to 3, then app is quick posting)");

                        Elements msg3AutoFlag = doc.getElementsContainingOwnText("There were one or more potential problems with your message:");
                        Elements msg3Error = doc.getElementsContainingOwnText("There was an error posting your message:");
                        if (!msg3Error.isEmpty()) {
                            AllInOneV2.wtl("there was an error in post msg step 3, ending early");
                            aio.postError(msg3Error.first().parent().parent().text());
                            aio.postExecuteCleanup(desc);
                        } else if (!msg3AutoFlag.isEmpty()) {
                            AllInOneV2.wtl("autoflag got tripped in post msg step 3, getting data and showing autoflag dialog");
                            String msg = msg3AutoFlag.first().parent().parent().text();

                            String msg3Key = doc.getElementsByAttributeValue("name", "key").attr("value");
                            String msg3Post_id = doc.getElementsByAttributeValue("name", "post_id").attr("value");
                            String msg3Uid = doc.getElementsByAttributeValue("name", "uid").attr("value");

                            HashMap<String, List<String>> msg3Data = new HashMap<String, List<String>>();
                            msg3Data.put("post", Arrays.asList("Post Message"));
                            msg3Data.put("key", Arrays.asList(msg3Key));
                            msg3Data.put("post_id", Arrays.asList(msg3Post_id));
                            msg3Data.put("uid", Arrays.asList(msg3Uid));

                            showAutoFlagWarning(lastPath, msg3Data, NetDesc.POSTMSG_S3, msg);
                            postErrorDetected = true;
                        } else {
                            AllInOneV2.wtl("finishing post message step 3, refreshing topic");
                            lastDesc = NetDesc.TOPIC;
                            aio.enableGoToUrlDefinedPost();
                            Crouton.showText(aio, "Message posted.", Theming.croutonStyle());
                            processTopicsAndMessages(doc, resUrl, NetDesc.TOPIC);
                        }
                        break;

                    case POSTTPC_S1:
                        AllInOneV2.wtl("session hNR determined this is post topic step 1");
                        String tpc1Key = doc.getElementsByAttributeValue("name", "key").attr("value");

                        HashMap<String, List<String>> tpc1Data = new HashMap<String, List<String>>();
                        tpc1Data.put("topictitle", Arrays.asList(aio.getSavedPostTitle()));
                        tpc1Data.put("messagetext", Arrays.asList(aio.getSavedPostBody()));
                        tpc1Data.put("custom_sig", Arrays.asList(aio.getSig()));
                        tpc1Data.put("post", Arrays.asList((userHasAdvancedPosting() ? "Post without Preview" : "Preview Message")));
                        tpc1Data.put("key", Arrays.asList(tpc1Key));

                        if (aio.isUsingPoll()) {
                            tpc1Data.put("poll_text", Arrays.asList(aio.getPollTitle()));
                            for (int x = 0; x < 10; x++) {
                                if (aio.getPollOptions()[x].length() != 0)
                                    tpc1Data.put("poll_option_" + (x + 1), Arrays.asList(aio.getPollOptions()[x]));
                                else
                                    x = 11;
                            }
                            tpc1Data.put("min_level", Arrays.asList(aio.getPollMinLevel()));
                        }

                        Elements tpc1Error = doc.getElementsContainingOwnText("There was an error posting your message:");
                        if (!tpc1Error.isEmpty()) {
                            AllInOneV2.wtl("there was an error in post topic step 1, ending early");
                            aio.postError(tpc1Error.first().parent().parent().text());
                            aio.postExecuteCleanup(desc);
                        } else {
                            AllInOneV2.wtl("finishing post topic step 1, sending step 2");
                            post((userHasAdvancedPosting() ? NetDesc.POSTTPC_S3 : NetDesc.POSTTPC_S2), lastPath, tpc1Data);
                        }
                        break;

                    case POSTTPC_S2:
                        AllInOneV2.wtl("session hNR determined this is post topic step 2");
                        String tpc2Key = doc.getElementsByAttributeValue("name", "key").attr("value");
                        String tpcPost_id = doc.getElementsByAttributeValue("name", "post_id").attr("value");
                        String tpcUid = doc.getElementsByAttributeValue("name", "uid").attr("value");

                        HashMap<String, List<String>> tpc2Data = new HashMap<String, List<String>>();
                        tpc2Data.put("post", Arrays.asList("Post Message"));
                        tpc2Data.put("key", Arrays.asList(tpc2Key));
                        tpc2Data.put("post_id", Arrays.asList(tpcPost_id));
                        tpc2Data.put("uid", Arrays.asList(tpcUid));

                        if (aio.isUsingPoll()) {
                            tpc2Data.put("poll_text", Arrays.asList(aio.getPollTitle()));
                            for (int x = 0; x < 10; x++) {
                                if (aio.getPollOptions()[x].length() != 0)
                                    tpc2Data.put("poll_option_" + (x + 1), Arrays.asList(aio.getPollOptions()[x]));
                                else
                                    x = 11;
                            }
                            tpc2Data.put("min_level", Arrays.asList(aio.getPollMinLevel()));
                        }

                        Elements tpc2Error = doc.getElementsContainingOwnText("There was an error posting your message:");
                        Elements tpc2AutoFlag = doc.getElementsContainingOwnText("There were one or more potential problems with your message:");
                        if (!tpc2Error.isEmpty()) {
                            AllInOneV2.wtl("there was an error in post topic step 2, ending early");
                            aio.postError(tpc2Error.first().parent().parent().text());
                            aio.postExecuteCleanup(desc);
                        } else if (!tpc2AutoFlag.isEmpty()) {
                            AllInOneV2.wtl("autoflag got tripped in post msg step 2, showing autoflag dialog");
                            String msg = tpc2AutoFlag.first().parent().parent().text();
                            showAutoFlagWarning(lastPath, tpc2Data, NetDesc.POSTTPC_S3, msg);
                        } else {
                            AllInOneV2.wtl("finishing post topic step 2, sending step 3");
                            post(NetDesc.POSTTPC_S3, lastPath, tpc2Data);
                        }
                        break;

                    case POSTTPC_S3:
                        AllInOneV2.wtl("session hNR determined this is post topic step 3 (if jumping from 1 to 3, then app is quick posting)");

                        Elements tpc3AutoFlag = doc.getElementsContainingOwnText("There were one or more potential problems with your message:");
                        Elements tpc3Error = doc.getElementsContainingOwnText("There was an error posting your message:");
                        if (!tpc3Error.isEmpty()) {
                            AllInOneV2.wtl("there was an error in post topic step 3, ending early");
                            aio.postError(tpc3Error.first().parent().parent().text());
                            aio.postExecuteCleanup(desc);
                        } else if (!tpc3AutoFlag.isEmpty()) {
                            AllInOneV2.wtl("autoflag got tripped in post msg step 3, getting data and showing autoflag dialog");
                            String msg = tpc3AutoFlag.first().parent().parent().text();

                            String tpc3Key = doc.getElementsByAttributeValue("name", "key").attr("value");
                            String tpc3Post_id = doc.getElementsByAttributeValue("name", "post_id").attr("value");
                            String tpc3Uid = doc.getElementsByAttributeValue("name", "uid").attr("value");

                            HashMap<String, List<String>> tpc3Data = new HashMap<String, List<String>>();
                            tpc3Data.put("post", Arrays.asList("Post Message"));
                            tpc3Data.put("key", Arrays.asList(tpc3Key));
                            tpc3Data.put("post_id", Arrays.asList(tpc3Post_id));
                            tpc3Data.put("uid", Arrays.asList(tpc3Uid));

                            showAutoFlagWarning(lastPath, tpc3Data, NetDesc.POSTTPC_S3, msg);
                            postErrorDetected = true;
                        } else {
                            AllInOneV2.wtl("finishing post topic step 3, processing new topic");
                            lastDesc = NetDesc.TOPIC;
                            Crouton.showText(aio, "Topic posted.", Theming.croutonStyle());
                            processTopicsAndMessages(doc, resUrl, NetDesc.TOPIC);
                        }
                        break;

                    case MARKMSG_S1:
                        if (doc.select("p:contains(you selected is no longer available for viewing.)").isEmpty()) {
                            HashMap<String, List<String>> markData = new HashMap<String, List<String>>();
                            markData.put("reason", Arrays.asList(aio.getReportCode()));
                            markData.put("key", Arrays.asList(doc.select("input[name=key]").first().attr("value")));

                            post(NetDesc.MARKMSG_S2, resUrl + "?action=mod", markData);
                        } else
                            Crouton.showText(aio, "The topic has already been removed!", Theming.croutonStyle());

                        break;

                    case MARKMSG_S2:
                        //This message has been marked for moderation.
                        if (!doc.select("p:contains(This message has been marked for moderation.)").isEmpty())
                            Crouton.showText(aio, "Message marked successfully.", Theming.croutonStyle());
                        else
                            Crouton.showText(aio, "There was an error marking the message.", Theming.croutonStyle());

                        refresh();
                        break;

                    case DLTMSG_S1:
                        String delKey = doc.getElementsByAttributeValue("name", "key").attr("value");
                        HashMap<String, List<String>> delData = new HashMap<String, List<String>>();
                        delData.put("YES", Arrays.asList("Delete this Post"));
                        delData.put("key", Arrays.asList(delKey));

                        post(NetDesc.DLTMSG_S2, resUrl + "?action=delete", delData);
                        break;

                    case DLTMSG_S2:
                        goBack(true);
                        break;

                    case CLOSE_TOPIC:
                        Crouton.showText(aio, "Topic closed successfully.", Theming.croutonStyle());
                        goBack(true);
                        break;


                    case SEND_PM_S1:
                        String pmKey = doc.getElementsByAttributeValue("name", "key").attr("value");

                        HashMap<String, List<String>> pmData = new HashMap<String, List<String>>();
                        pmData.put("key", Arrays.asList(pmKey));
                        pmData.put("to", Arrays.asList(aio.savedTo));
                        pmData.put("subject", Arrays.asList(aio.savedSubject));
                        pmData.put("message", Arrays.asList(aio.savedMessage));
                        pmData.put("submit", Arrays.asList("Send Message"));

                        post(NetDesc.SEND_PM_S2, "/pm/new", pmData);
                        break;

                    case SEND_PM_S2:
                        if (doc.select("input[name=subject]").isEmpty()) {
                            aio.pmCleanup(true, null);
                        } else {
                            String error = doc.select("form[action=/pm/new]").first().previousElementSibling().text();
                            aio.pmCleanup(false, error);
                        }
                        break;

                    case TOPIC:
                        AllInOneV2.wtl("session hNR determined this is a topic");
                        processTopicsAndMessages(doc, resUrl, NetDesc.TOPIC);
                        break;

                    case MESSAGE_DETAIL:
                        AllInOneV2.wtl("session hNR determined this is a message");
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
                    case TAG_USER:
                    case MODHIST:
                    case PM_INBOX:
                    case PM_INBOX_DETAIL:
                    case PM_OUTBOX:
                    case PM_OUTBOX_DETAIL:
                    case VERIFY_ACCOUNT_S1:
                    case VERIFY_ACCOUNT_S2:
                        AllInOneV2.wtl("session hNR determined this should be handled by AIO");
                        aio.processContent(desc, doc, resUrl);
                        break;
                }
            } else {
                // connection failed for some reason, probably timed out
                AllInOneV2.wtl("res was null in session hNR");
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
                    "the phone.");
        } catch (ConnectionClosedException connClosedEx) {
            aio.genError("Connection Closed", "The connection was closed before the the response was completed.");
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

        AllInOneV2.wtl("session hNR finishing, desc: " + desc.name());
    }

    private void processTopicsAndMessages(Document doc, String resUrl, NetDesc successDesc) {
        if (!doc.select("p:contains(no longer available for viewing)").isEmpty()) {
            if (successDesc == NetDesc.TOPIC)
                Crouton.showText(aio, "The topic you selected is no longer available for viewing.", Theming.croutonStyle());
            else if (successDesc == NetDesc.MESSAGE_DETAIL)
                Crouton.showText(aio, "The message you selected is no longer available for viewing.", Theming.croutonStyle());

            AllInOneV2.wtl("topic or message is no longer available, treat response as a board");
            aio.processContent(NetDesc.BOARD, doc, resUrl);
        } else {
            AllInOneV2.wtl("handle the topic or message in AIO");
            aio.processContent(successDesc, doc, resUrl);
        }
    }

    private boolean skipAIOCleanup = false;

    public void forceSkipAIOCleanup() {
        AllInOneV2.wtl("forcing AIO cleanup skip");
        skipAIOCleanup = true;
    }

    private boolean postErrorDetected = false;

    public void postExecuteCleanup(NetDesc desc) {
        switch (desc) {
            case AMP_LIST:
            case TRACKED_TOPICS:
            case BOARD:
            case BOARD_JUMPER:
            case TOPIC:
            case MESSAGE_DETAIL:
            case USER_DETAIL:
            case TAG_USER:
            case MODHIST:
            case PM_INBOX:
            case PM_INBOX_DETAIL:
            case PM_OUTBOX:
            case PM_OUTBOX_DETAIL:
            case CLOSE_TOPIC:
            case GAME_SEARCH:
            case BOARD_LIST:
            case UNSPECIFIED:
                if (!skipAIOCleanup)
                    aio.postExecuteCleanup(desc);

                break;

            case POSTMSG_S3:
            case POSTTPC_S3:
                if (!postErrorDetected)
                    aio.postExecuteCleanup((desc == NetDesc.POSTMSG_S3 ? NetDesc.TOPIC : NetDesc.BOARD));

                break;

            case LOGIN_S1:
            case LOGIN_S2:
            case MARKMSG_S1:
            case MARKMSG_S2:
            case DLTMSG_S1:
            case DLTMSG_S2:
            case EDIT_MSG:
            case POSTMSG_S1:
            case POSTMSG_S2:
            case POSTTPC_S1:
            case POSTTPC_S2:
            case VERIFY_ACCOUNT_S1:
            case VERIFY_ACCOUNT_S2:
            case SEND_PM_S1:
            case SEND_PM_S2:
                break;
        }

        skipAIOCleanup = false;
        postErrorDetected = false;
    }

    public boolean canGoBack() {
        return hAdapter.hasHistory();
    }

    public void goBack(boolean forceReload) {
        History h = hAdapter.pullHistory();

        applySavedScroll = true;
        savedScrollVal = h.getVertPos();

        if (forceReload || AllInOneV2.getSettingsPref().getBoolean("reloadOnBack", false)) {
            forceNoHistoryAddition();
            AllInOneV2.wtl("going back in history, refreshing: " + h.getDesc().name() + " " + h.getPath());
            get(h.getDesc(), h.getPath());
        } else {
            AllInOneV2.wtl("going back in history: " + h.getDesc().name() + " " + h.getPath());
            lastDesc = h.getDesc();
            lastResBodyAsBytes = h.getResBodyAsBytes();
            lastPath = h.getPath();

            aio.processContent(lastDesc, Jsoup.parse(new String(lastResBodyAsBytes), lastPath), lastPath);
        }
    }

    public void openHistoryDB() {
        hAdapter.open();
    }

    public void closeHistoryDB() {
        hAdapter.close();
    }

    public void refresh() {
        forceNoHistoryAddition();
        AllInOneV2.wtl("refreshing: " + lastDesc.name() + " " + lastPath);
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

        AllInOneV2.wtl("user level: " + userLevel);
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
            // check if AMP
            else if (url.contains("myposts.php")) {
                return NetDesc.AMP_LIST;
            }
            // check if this is a board or topic url
            else if (url.contains("/boards")) {
                if (url.contains("/users/")) {
                    return NetDesc.USER_DETAIL;
                } else if (url.contains("boardlist.php")) {
                    return NetDesc.BOARD_LIST;
                } else if (url.contains("modhist.php")) {
                    return NetDesc.MODHIST;
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
