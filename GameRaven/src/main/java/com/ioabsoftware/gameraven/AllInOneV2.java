package com.ioabsoftware.gameraven;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ioabsoftware.gameraven.db.HighlightListDBHelper;
import com.ioabsoftware.gameraven.db.HighlightedUser;
import com.ioabsoftware.gameraven.networking.NetDesc;
import com.ioabsoftware.gameraven.networking.Session;
import com.ioabsoftware.gameraven.prefs.HeaderSettings;
import com.ioabsoftware.gameraven.prefs.SettingsAccount;
import com.ioabsoftware.gameraven.prefs.SettingsHighlightedUsers;
import com.ioabsoftware.gameraven.util.AccountManager;
import com.ioabsoftware.gameraven.util.DocumentParser;
import com.ioabsoftware.gameraven.util.Theming;
import com.ioabsoftware.gameraven.views.BaseRowData;
import com.ioabsoftware.gameraven.views.BaseRowData.ReadStatus;
import com.ioabsoftware.gameraven.views.MarqueeToolbar;
import com.ioabsoftware.gameraven.views.ViewAdapter;
import com.ioabsoftware.gameraven.views.rowdata.AMPRowData;
import com.ioabsoftware.gameraven.views.rowdata.BoardRowData;
import com.ioabsoftware.gameraven.views.rowdata.BoardRowData.BoardType;
import com.ioabsoftware.gameraven.views.rowdata.GameSearchRowData;
import com.ioabsoftware.gameraven.views.rowdata.HeaderRowData;
import com.ioabsoftware.gameraven.views.rowdata.MessageRowData;
import com.ioabsoftware.gameraven.views.rowdata.PMDetailRowData;
import com.ioabsoftware.gameraven.views.rowdata.PMRowData;
import com.ioabsoftware.gameraven.views.rowdata.TopicRowData;
import com.ioabsoftware.gameraven.views.rowdata.TopicRowData.TopicType;
import com.ioabsoftware.gameraven.views.rowdata.TrackedTopicRowData;
import com.ioabsoftware.gameraven.views.rowdata.UserDetailRowData;
import com.ioabsoftware.gameraven.views.rowview.MessageRowView;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.melnykov.fab.FloatingActionButton;

import org.acra.ACRA;
import org.acra.ACRAConfiguration;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.codechimp.apprater.AppRater;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import de.keyboardsurfer.android.widget.crouton.Crouton;

@SuppressLint("RtlHardcoded")
public class AllInOneV2 extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static final int SEND_PM_DIALOG = 102;
    public static final int MESSAGE_ACTION_DIALOG = 103;
    public static final int REPORT_MESSAGE_DIALOG = 104;
    public static final int POLL_OPTIONS_DIALOG = 105;
    public static final int CHANGE_LOGGED_IN_DIALOG = 106;

    public static final String EMPTY_STRING = "";

    public static String defaultSig;

    private Session session = null;

    public Session getSession() {
        return session;
    }

    private String boardID;
    private String topicID;
    private String messageIDForEditing;
    private String postPostUrl;

    private String savedPostBody;

    public String getSavedPostBody() {
        return savedPostBody;
    }

    private String savedPostTitle;

    public String getSavedPostTitle() {
        return savedPostTitle;
    }

    private static SharedPreferences settings = null;

    public static SharedPreferences getSettingsPref() {
        return settings;
    }

    private LinearLayout titleWrapper;
    private EditText postTitle;
    private EditText postBody;

    private TextView titleCounter;
    private TextView bodyCounter;

    private Button postSubmitButton;
    private Button postCancelButton;
    private Button pollButton;

    private View pollSep;

    private boolean pollUse = false;

    public boolean isUsingPoll() {
        return pollUse;
    }

    private String pollTitle = EMPTY_STRING;

    public String getPollTitle() {
        return pollTitle;
    }

    private String[] pollOptions = new String[10];

    public String[] getPollOptions() {
        return pollOptions;
    }

    private int pollMinLevel = -1;

    public String getPollMinLevel() {
        return Integer.toString(pollMinLevel);
    }


    private LinearLayout postWrapper;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView contentList;

    private String tlUrl;

    private enum PostMode {ON_BOARD, ON_TOPIC, NEW_PM}

    private PostMode pMode;

    private enum FavMode {ON_BOARD, ON_TOPIC}

    private FavMode fMode;
    private String favKey;

    private Button pageLabel;
    private Button firstPage, prevPage, nextPage, lastPage;
    private String firstPageUrl, prevPageUrl, nextPageUrl, lastPageUrl, jumperPageUrl;
    private AlertDialog.Builder jumperDialogBuilder;
    private NetDesc pageJumperDesc;

    private View pageJumperWrapper;

    @SuppressWarnings("ConstantConditions")
    public int[] getScrollerVertLoc() {
        try {
            int firstVis = contentList.getFirstVisiblePosition();
            return new int[]{firstVis, contentList.getChildAt(0).getTop()};
        } catch (NullPointerException npe) {
            return new int[]{0, 0};
        }
    }


    private static HighlightListDBHelper hlDB;

    public static HighlightListDBHelper getHLDB() {
        return hlDB;
    }

    private DrawerLayout drawerLayout;
    private ScrollView drawerPullout;
    private ActionBarDrawerToggle drawerToggle;

    private FloatingActionButton fab;

    private static AllInOneV2 me;

    public static AllInOneV2 get() {
        return me;
    }

    private Theming themingInstance;


    /**
     * *******************************************
     * START METHODS
     * ********************************************
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        me = this;
        settings = PreferenceManager.getDefaultSharedPreferences(this);

        // get an instance of Theming to ensure values don't get GC'd
        // Will they get GC'd? I have no idea. Better safe than sorry.
        themingInstance = new Theming();

        Theming.preInit(settings);
        setTheme(Theming.theme());
        Theming.init(this, settings);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(Theming.colorPrimaryDark());
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.allinonev2);

        Theming.colorOverscroll(this);

        AccountManager.init(this);

        setSupportActionBar((MarqueeToolbar) findViewById(R.id.aioToolbar));
        ActionBar aBar = getSupportActionBar();
        assert aBar != null : "Action bar is null";

        aBar.setDisplayHomeAsUpEnabled(true);
        aBar.setDisplayShowTitleEnabled(true);

        drawerLayout = (DrawerLayout) findViewById(R.id.aioDrawerLayout);
        drawerPullout = (ScrollView) findViewById(R.id.dwrScroller);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                drawerPullout.scrollTo(0, 0);
            }
        };

        drawerLayout.setDrawerListener(drawerToggle);


        findViewById(R.id.dwrChangeAcc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
                showDialog(CHANGE_LOGGED_IN_DIALOG);
            }
        });

        boardListButton = (Button) findViewById(R.id.dwrBoardJumper);
        boardListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
                session.get(NetDesc.BOARD_JUMPER, "/boards");
            }
        });

        findViewById(R.id.dwrAMPList).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
                session.get(NetDesc.AMP_LIST, buildAMPLink());
            }
        });

        findViewById(R.id.dwrTrackedTopics).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
                session.get(NetDesc.TRACKED_TOPICS, "/boards/tracked");
            }
        });

        findViewById(R.id.dwrPMInbox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
                session.get(NetDesc.PM_INBOX, "/pm/");
            }
        });

        findViewById(R.id.dwrCopyCurrURL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.content.ClipboardManager clipboard =
                        (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("simple text", session.getLastPath()));
                drawerLayout.closeDrawers();
                Crouton.showText(AllInOneV2.this, "URL copied to clipboard.", Theming.croutonStyle());
            }
        });

        findViewById(R.id.dwrHighlightList).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
                startActivity(new Intent(AllInOneV2.this, SettingsHighlightedUsers.class));
            }
        });

        findViewById(R.id.dwrSettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
                startActivity(new Intent(AllInOneV2.this, HeaderSettings.class));
            }
        });

        findViewById(R.id.dwrExit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllInOneV2.this.finish();
            }
        });

        aBar.setDisplayHomeAsUpEnabled(true);
        aBar.setHomeButtonEnabled(true);

        if (!settings.contains("defaultAccount")) {
            // settings need to be set to default
            PreferenceManager.setDefaultValues(this, R.xml.prefsaccountsnotifs, false);
            PreferenceManager.setDefaultValues(this, R.xml.prefsadvanced, false);
            PreferenceManager.setDefaultValues(this, R.xml.prefsgeneral, false);
            PreferenceManager.setDefaultValues(this, R.xml.prefstheming, false);
            Editor sEditor = settings.edit();
            sEditor.putString("defaultAccount", HeaderSettings.NO_DEFAULT_ACCOUNT)
                    .putString("timezone", TimeZone.getDefault().getID())
                    .apply();
        }

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.ptr_layout);
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setColorSchemeColors(Theming.colorPrimary(), Theming.colorPrimaryDark());
        swipeRefreshLayout.setOnRefreshListener(this);

        contentList = (ListView) findViewById(R.id.aioMainList);

        titleWrapper = (LinearLayout) findViewById(R.id.aioPostTitleWrapper);
        postTitle = (EditText) findViewById(R.id.aioPostTitle);
        postBody = (EditText) findViewById(R.id.aioPostBody);
        titleCounter = (TextView) findViewById(R.id.aioPostTitleCounter);
        bodyCounter = (TextView) findViewById(R.id.aioPostBodyCounter);

        pageJumperWrapper = findViewById(R.id.aioHeader);
        firstPage = (Button) findViewById(R.id.aioFirstPage);
        firstPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                session.get(pageJumperDesc, firstPageUrl);
            }
        });
        prevPage = (Button) findViewById(R.id.aioPreviousPage);
        prevPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                session.get(pageJumperDesc, prevPageUrl);
            }
        });
        nextPage = (Button) findViewById(R.id.aioNextPage);
        nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                session.get(pageJumperDesc, nextPageUrl);
            }
        });
        lastPage = (Button) findViewById(R.id.aioLastPage);
        lastPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                session.get(pageJumperDesc, lastPageUrl);
            }
        });
        pageLabel = (Button) findViewById(R.id.aioPageLabel);
        pageLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jumperDialogBuilder.show();
            }
        });

        Theming.setTextSizeBases(((TextView) findViewById(R.id.dwrChangeAccHeader)).getTextSize(),
                ((TextView) findViewById(R.id.dwrChangeAcc)).getTextSize(),
                firstPage.getTextSize(),
                pageLabel.getTextSize());

        postTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                titleCounter.setText(StringEscapeUtils.escapeHtml4(s.toString()).length() + "/80");
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
                // GFAQs adds 13(!) characters onto bodies when they have a sig, apparently.
                int length = StringEscapeUtils.escapeHtml4(s.toString()).length() + getSig().length() + 13;
                bodyCounter.setText(length + "/4096");
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

        postSubmitButton = (Button) findViewById(R.id.aioPostDo);
        postCancelButton = (Button) findViewById(R.id.aioPostCancel);
        pollButton = (Button) findViewById(R.id.aioPollOptions);
        pollSep = findViewById(R.id.aioPollSep);

        postWrapper = (LinearLayout) findViewById(R.id.aioPostWrapper);

        if (BuildConfig.DEBUG) wtl("creating default sig");
        defaultSig = "Posted with GameRaven *grver*";

        if (BuildConfig.DEBUG) wtl("getting css directory");
        File cssDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/gameraven");
        if (!cssDirectory.exists()) {
            if (BuildConfig.DEBUG) wtl("css directory does not exist, creating");
            if (cssDirectory.mkdir())
                if (BuildConfig.DEBUG) wtl("css directory created");
                else if (BuildConfig.DEBUG) wtl("css directory creation failed");
        }

        if (BuildConfig.DEBUG) wtl("starting db creation");
        hlDB = new HighlightListDBHelper(this);

        View foot = new View(this);
        foot.setMinimumHeight(Theming.convertDPtoPX(this, 80));
        contentList.addFooterView(foot);

        adapterRows.add(new HeaderRowData("Loading..."));
        contentList.setAdapter(viewAdapter);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pMode == PostMode.ON_BOARD)
                    postSetup(false);
                else if (pMode == PostMode.ON_TOPIC)
                    postSetup(true);
                else if (pMode == PostMode.NEW_PM)
                    pmSetup(EMPTY_STRING, EMPTY_STRING, EMPTY_STRING);
            }
        });
        fab.setVisibility(View.GONE);

        AppRater.app_launched(this);

        if (BuildConfig.DEBUG) wtl("onCreate finishing");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getData() != null && intent.getData().getPath() != null) {
            String url = intent.getData().getPath();
            NetDesc desc = Session.determineNetDesc(url);
            if (desc != NetDesc.UNSPECIFIED)
                session.get(desc, url);
            else
                Crouton.showText(this, "Page not recognized: " + url, Theming.croutonStyle());
        }
    }

    private boolean firstResume = true;

    @Override
    protected void onResume() {
        if (BuildConfig.DEBUG) wtl("onResume fired");
        super.onResume();

        swipeRefreshLayout.setEnabled(settings.getBoolean("enablePTR", false));
        contentList.setFastScrollEnabled(settings.getBoolean("enableFastScroll", true));

        int lastUpdateYear = settings.getInt("lastUpdateYear", 0);
        int lastUpdateYearDay = settings.getInt("lastUpdateYearDay", 0);
        Time now = new Time();
        now.setToNow();
        if (lastUpdateYear != now.year || lastUpdateYearDay != now.yearDay) {
            if (BuildConfig.DEBUG) wtl("checking for update");
            try {
                if (BuildConfig.DEBUG) wtl("my version is " + BuildConfig.VERSION_CODE);
                Ion.with(this)
                        .load("GET", "http://ioabsoftware.com/gameraven/latest.txt")
                        .asString()
                        .setCallback(new FutureCallback<String>() {
                            @Override
                            public void onCompleted(Exception e, String result) {
                                if (NumberUtils.isNumber(result)) {
                                    int netVersion = Integer.valueOf(result);
                                    if (BuildConfig.DEBUG) wtl("net version is " + netVersion);

                                    if (netVersion > BuildConfig.VERSION_CODE) {
                                        AlertDialog.Builder b = new AlertDialog.Builder(AllInOneV2.this);
                                        b.setTitle("New Version Found");
                                        b.setMessage("Open Google Play Market to download new version? Note that although " +
                                                "care is taken to make sure this notification only goes out once the update " +
                                                "has spread to all Google servers, there is still a chance the update may not " +
                                                "show up in the Play Store at first. Rest assured, there is a new version. " +
                                                "It just hasn't reached your local Google Play server yet.");
                                        b.setPositiveButton("Yes", new OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent i = new Intent(Intent.ACTION_VIEW);
                                                i.setData(Uri.parse("market://details?id=com.ioabsoftware.gameraven"));
                                                AllInOneV2.this.startActivity(i);
                                            }
                                        });
                                        b.setNegativeButton("No", null);
                                        b.show();
                                    }
                                }
                            }
                        });

                settings.edit().putInt("lastUpdateYear", now.year).putInt("lastUpdateYearDay", now.yearDay).apply();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        if (Theming.updateTextScale(settings.getInt("textScale", 100) / 100f)) {
            int px = TypedValue.COMPLEX_UNIT_PX;

            firstPage.setTextSize(px, Theming.getScaledPJButtonTextSize());
            prevPage.setTextSize(px, Theming.getScaledPJButtonTextSize());
            nextPage.setTextSize(px, Theming.getScaledPJButtonTextSize());
            lastPage.setTextSize(px, Theming.getScaledPJButtonTextSize());
            pageLabel.setTextSize(px, Theming.getScaledPJLabelTextSize());

            ((TextView) findViewById(R.id.dwrChangeAccHeader)).setTextSize(px, Theming.getScaledDwrHeaderTextSize());
            ((TextView) findViewById(R.id.dwrChangeAcc)).setTextSize(px, Theming.getScaledDwrButtonTextSize());
            ((TextView) findViewById(R.id.dwrNavHeader)).setTextSize(px, Theming.getScaledDwrHeaderTextSize());
            boardListButton.setTextSize(px, Theming.getScaledDwrButtonTextSize());
            ((TextView) findViewById(R.id.dwrAMPList)).setTextSize(px, Theming.getScaledDwrButtonTextSize());
            ((TextView) findViewById(R.id.dwrTrackedTopics)).setTextSize(px, Theming.getScaledDwrButtonTextSize());
            ((TextView) findViewById(R.id.dwrPMInbox)).setTextSize(px, Theming.getScaledDwrButtonTextSize());
            ((TextView) findViewById(R.id.dwrFuncHeader)).setTextSize(px, Theming.getScaledDwrHeaderTextSize());
            ((TextView) findViewById(R.id.dwrCopyCurrURL)).setTextSize(px, Theming.getScaledDwrButtonTextSize());
            ((TextView) findViewById(R.id.dwrHighlightList)).setTextSize(px, Theming.getScaledDwrButtonTextSize());
            ((TextView) findViewById(R.id.dwrSettings)).setTextSize(px, Theming.getScaledDwrButtonTextSize());
            ((TextView) findViewById(R.id.dwrExit)).setTextSize(px, Theming.getScaledDwrButtonTextSize());
        }

        MessageRowView.setUsingAvatars(settings.getBoolean("usingAvatars", false));

        if (session == null) {
            String initUrl = null;
            NetDesc initDesc = null;
            if (firstResume) {
                Uri uri = getIntent().getData();
                if (uri != null && uri.getScheme() != null && uri.getHost() != null) {
                    if (uri.getScheme().equals("http") && uri.getHost().contains("gamefaqs.com")) {
                        initUrl = uri.getPath();
                        initDesc = Session.determineNetDesc(initUrl);
                    }
                }
            }

            String defaultAccount = settings.getString("defaultAccount", HeaderSettings.NO_DEFAULT_ACCOUNT);
            String resumeAccount = settings.getString("resumeSessionUser", HeaderSettings.NO_DEFAULT_ACCOUNT);
            boolean resumeSession = settings.getBoolean("resumeSession", false);

            if (firstResume && resumeSession && AccountManager.containsUser(this, resumeAccount)) {
                session = new Session(this, resumeAccount, AccountManager.getPassword(this, resumeAccount), Session.RESUME_INIT_URL, NetDesc.UNSPECIFIED);
            } else if (AccountManager.containsUser(this, defaultAccount)) {
                if (BuildConfig.DEBUG) wtl("starting new session from onResume, logged in");
                session = new Session(this, defaultAccount, AccountManager.getPassword(this, defaultAccount), initUrl, initDesc);
            } else {
                if (BuildConfig.DEBUG) wtl("starting new session from onResume, no login");
                session = new Session(this, null, null, initUrl, initDesc);
            }
        } else {
            if (settings.getBoolean("reloadOnResume", false)) {
                    if (BuildConfig.DEBUG)
                            wtl("session exists, reload on resume is true, refreshing page");
                    isRoR = true;
                    session.refresh();
                }
        }

        if (!settings.contains("beenWelcomed")) {
            settings.edit().putBoolean("beenWelcomed", true).apply();
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setTitle("Welcome!");
            b.setMessage("Would you like to view the quick start help files? This dialog won't be shown again.");
            b.setPositiveButton("Yes", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://ioabsoftware.com/gameraven/quickstart.html")));
                }
            });
            b.setNegativeButton("No", null);
            b.show();
        }

        firstResume = false;

        if (BuildConfig.DEBUG) wtl("onResume finishing");
    }

    @Override
    protected void onStop () {
        Editor e = settings.edit();
        e.putBoolean("resumeSession", true);
        session.addHistoryBeforeStop();
        if (Session.isLoggedIn())
            e.putString("resumeSessionUser", Session.getUser());

        e.apply();

        session.closeHistoryDB();

        super.onStop();
    }

    @Override
    protected void onRestart () {
        super.onRestart();
        session.openHistoryDB();
        session.popHistory();
    }

    @Override
    protected void onDestroy() {
        if (BuildConfig.DEBUG) wtl("Destroying!");
        Crouton.clearCroutonsForActivity(this);

        if (isFinishing())
            settings.edit().putBoolean("resumeSession", false).apply();

        super.onDestroy();
    }

    private AlertDialog loginDialog;
    public void showLoggingInDialog(String user) {
        if (loginDialog == null) {
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            ProgressBar spinner = new ProgressBar(this);
            spinner.setIndeterminate(true);
            b.setView(spinner);
            loginDialog = b.create();
        }

        loginDialog.setTitle("Logging in as " + user + "...");
        loginDialog.show();
    }

    public void dismissLoginDialog() {
        loginDialog.dismiss();
    }

    private boolean needToSetNavList = true;

    public void disableNavList() {
        findViewById(R.id.dwrNavWrapper).setVisibility(View.GONE);
        needToSetNavList = true;
    }

    public void setNavList(boolean isLoggedIn) {
        findViewById(R.id.dwrNavWrapper).setVisibility(View.VISIBLE);
        if (isLoggedIn)
            findViewById(R.id.dwrLoggedInNav).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.dwrLoggedInNav).setVisibility(View.GONE);

        needToSetNavList = false;
    }

    @Override
    public boolean onSearchRequested() {
        if (searchIcon != null && searchIcon.isVisible())
            MenuItemCompat.expandActionView(searchIcon);

        return false;
    }

    public void toggleMenu() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT))
            drawerLayout.closeDrawers();
        else
            drawerLayout.openDrawer(Gravity.LEFT);
    }

    @Override
    public boolean onKeyUp(int keyCode, @NotNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            toggleMenu();
            return true;
        } else {
            return super.onKeyUp(keyCode, event);
        }
    }

    @Override
    public void onRefresh() {
        refreshClicked(null);
    }

    private MenuItem refreshIcon, replyIcon, pmInboxIcon, pmOutboxIcon,
            addFavIcon, remFavIcon, searchIcon, topicListIcon, sendUserPMIcon, tagUserIcon;

    /**
     * Adds menu items
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        searchIcon = menu.findItem(R.id.search);
        topicListIcon = menu.findItem(R.id.topicList);
        addFavIcon = menu.findItem(R.id.addFav);
        remFavIcon = menu.findItem(R.id.remFav);
        pmInboxIcon = menu.findItem(R.id.pmInbox);
        pmOutboxIcon = menu.findItem(R.id.pmOutbox);
        sendUserPMIcon = menu.findItem(R.id.sendUserPM);
        tagUserIcon = menu.findItem(R.id.tagUser);
        replyIcon = menu.findItem(R.id.reply);
        refreshIcon = menu.findItem(R.id.refresh);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchIcon);
        if (searchView != null) {
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

            SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
                public boolean onQueryTextChange(String newText) {
                    // just do default
                    return false;
                }

                public boolean onQueryTextSubmit(String query) {
                    try {
                        String encodedQuery = URLEncoder.encode(query, DocumentParser.CHARSET_NAME);
                        if (session.getLastDesc() == NetDesc.BOARD) {
                            if (BuildConfig.DEBUG) wtl("searching board for query");
                            session.get(NetDesc.BOARD, session.getLastPathWithoutData() + "?search=" + encodedQuery);
                        } else if (session.getLastDesc() == NetDesc.BOARD_JUMPER || session.getLastDesc() == NetDesc.GAME_SEARCH) {
                            if (BuildConfig.DEBUG) wtl("searching for games");
                            session.get(NetDesc.GAME_SEARCH, "/search/index.html?game=" + encodedQuery);
                        }
                    } catch (UnsupportedEncodingException e) {
                        throw new AssertionError(DocumentParser.CHARSET_NAME + " is unknown");
                        // shouldn't ever happen
                    }
                    return true;
                }
            };

            searchView.setOnQueryTextListener(queryTextListener);
        }

        return true;
    }

    UserDetailRowData userDetailData;

    /**
     * fires when a menu option is selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.search:
                onSearchRequested();
                return true;

            case R.id.addFav:
                AlertDialog.Builder afb = new AlertDialog.Builder(this);
                afb.setNegativeButton("No", null);

                final HashMap<String, List<String>> afData = new HashMap<>();
                afData.put("key", Collections.singletonList(favKey));

                final String afPath = session.getLastPath().replace("/boards/", "/boardaction/");

                switch (fMode) {
                    case ON_BOARD:
                        afb.setTitle("Add Board to Favorites?");
                        afb.setPositiveButton("Yes", new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                afData.put("action", Collections.singletonList("addfav"));
                                session.post(NetDesc.BOARD, afPath, afData);
                            }
                        });
                        break;
                    case ON_TOPIC:
                        afb.setTitle("Track Topic?");
                        afb.setPositiveButton("Yes", new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                afData.put("action", Collections.singletonList("tracktopic"));
                                session.post(NetDesc.TOPIC, afPath, afData);
                            }
                        });
                        break;
                }

                afb.show();

                return true;

            case R.id.remFav:
                AlertDialog.Builder rfb = new AlertDialog.Builder(this);
                rfb.setNegativeButton("No", null);

                final HashMap<String, List<String>> rfData = new HashMap<>();
                rfData.put("key", Collections.singletonList(favKey));

                final String rfPath = session.getLastPath().replace("/boards/", "/boardaction/");

                switch (fMode) {
                    case ON_BOARD:
                        rfb.setTitle("Remove Board from Favorites?");
                        rfb.setPositiveButton("Yes", new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                rfData.put("action", Collections.singletonList("remfav"));
                                session.post(NetDesc.BOARD, rfPath, rfData);
                            }
                        });
                        break;
                    case ON_TOPIC:
                        rfb.setTitle("Stop Tracking Topic?");
                        rfb.setPositiveButton("Yes", new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                rfData.put("action", Collections.singletonList("stoptrack"));
                                session.post(NetDesc.TOPIC, rfPath, rfData);
                            }
                        });
                        break;
                }

                rfb.show();

                return true;

            case R.id.topicList:
                session.get(NetDesc.BOARD, tlUrl);
                return true;

            case R.id.pmInbox:
                session.get(NetDesc.PM_INBOX, "/pm/");
                return true;

            case R.id.pmOutbox:
                session.get(NetDesc.PM_OUTBOX, "/pm/sent");
                return true;

            case R.id.sendUserPM:
                pmSetup(userDetailData.getName(), null, null);
                return true;

            case R.id.tagUser:
                AlertDialog.Builder b = new AlertDialog.Builder(this);
                b.setTitle("Set " + userDetailData.getName() + "'s Tag");
                b.setMessage("User tags can be up to 30 characters long and cannot contain any banned words.");

                final EditText tagText = new EditText(this);
                tagText.setHint(R.string.user_tag_hint);
                tagText.setText(userDetailData.getTagText());
                b.setView(tagText);

                b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HashMap<String, List<String>> data = new HashMap<String, List<String>>();
                        data.put("key", Arrays.asList(userDetailData.getTagKey()));
                        assert tagText.getText() != null : "tagText.getText() is null";
                        data.put("tag_text", Arrays.asList(tagText.getText().toString()));
                        data.put("tag_submit", Arrays.asList("Tag this User"));

                        hideSoftKeyboard(tagText);
                        AllInOneV2.get().getSession().post(NetDesc.TAG_USER, userDetailData.getTagPath(), data);
                    }
                });
                b.show();
                return true;

            case R.id.reply:
                pmSetup(replyTo, replySubject, EMPTY_STRING);
                return true;

            case R.id.refresh:
                if (session.getLastPath() == null) {
                    if (Session.isLoggedIn()) {
                        if (BuildConfig.DEBUG)
                            wtl("starting new session from case R.id.refresh, logged in");
                        session = new Session(this, Session.getUser(), AccountManager.getPassword(this, Session.getUser()));
                    } else {
                        if (BuildConfig.DEBUG)
                            wtl("starting new session from R.id.refresh, no login");
                        session = new Session(this);
                    }
                } else
                    session.refresh();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setMenuItemVisibility(MenuItem item, boolean visible) {
        if (item != null)
            item.setVisible(visible);
    }

    private void setMenuItemEnabled(MenuItem item, boolean enabled) {
        if (item != null)
            item.setEnabled(enabled);
    }

    private void setAllMenuItemsExceptRefreshVisibility(boolean visible) {
        setMenuItemVisibility(searchIcon, visible);
        setMenuItemVisibility(replyIcon, visible);
        setMenuItemVisibility(pmInboxIcon, visible);
        setMenuItemVisibility(pmOutboxIcon, visible);
        setMenuItemVisibility(sendUserPMIcon, visible);
        setMenuItemVisibility(tagUserIcon, visible);
        setMenuItemVisibility(addFavIcon, visible);
        setMenuItemVisibility(remFavIcon, visible);
        setMenuItemVisibility(topicListIcon, visible);

        if (visible)
            fab.setVisibility(View.VISIBLE);
        else
            fab.setVisibility(View.GONE);
    }

    private void setAllMenuItemsEnabled(boolean enabled) {
        setMenuItemEnabled(refreshIcon, enabled);
        setMenuItemEnabled(searchIcon, enabled);
        setMenuItemEnabled(replyIcon, enabled);
        setMenuItemEnabled(pmInboxIcon, enabled);
        setMenuItemEnabled(pmOutboxIcon, enabled);
        setMenuItemEnabled(sendUserPMIcon, enabled);
        setMenuItemEnabled(tagUserIcon, enabled);
        setMenuItemEnabled(addFavIcon, enabled);
        setMenuItemEnabled(remFavIcon, enabled);
        setMenuItemEnabled(topicListIcon, enabled);

        fab.setEnabled(enabled);
    }

    public void setLoginName(String name) {
        ((TextView) findViewById(R.id.dwrChangeAcc)).setText(name + " (Click to Change)");
    }

    private void hideSoftKeyboard(View inputView) {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                hideSoftInputFromWindow(inputView.getWindowToken(), 0);
    }

    public void postError(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setTitle("There was a problem with your post...");
        builder.setPositiveButton("Ok", null);
        builder.show();

        ptrCleanup();
    }

    public void genError(String errorTitle, String errorMsg, String buttonText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(errorMsg);
        builder.setTitle(errorTitle);
        builder.setPositiveButton(buttonText, null);
        builder.show();

        ptrCleanup();
    }

    public void noNetworkConnection() {
        genError("No Network Connection", "Couldn't establish network connection. Check your network settings, then try again.",
                "Dismiss");
    }

    public void timeoutCleanup(NetDesc desc) {
        String msg;
        String title;
        String posButtonText;
        boolean retrySub = false;
        switch (desc) {
            case LOGIN_S1:
            case LOGIN_S2:
                title = "Login Timeout";
                msg = "Login timed out, press retry to try again.";
                posButtonText = "Retry";
                break;
            case POSTMSG_S1:
            case POSTMSG_S3:
            case POSTTPC_S1:
            case POSTTPC_S3:
                postTimeoutCleanup();
                return;
            default:
                retrySub = true;
                title = "Timeout";
                msg = "Connection timed out, press retry to try again.";
                posButtonText = "Retry";
                break;

        }
        final boolean retry = retrySub;

        AlertDialog.Builder b = new AlertDialog.Builder(AllInOneV2.this);
        b.setTitle(title);
        b.setMessage(msg);
        b.setPositiveButton(posButtonText, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (retry)
                    session.get(session.getLastAttemptedDesc(), session.getLastAttemptedPath());
                else
                    refreshClicked(new View(AllInOneV2.this));
            }
        });
        b.setNegativeButton("Dismiss", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                postExecuteCleanup(session.getLastDesc());
            }
        });
        b.show();
    }

    private void postTimeoutCleanup() {
        AlertDialog.Builder b = new AlertDialog.Builder(AllInOneV2.this);
        b.setTitle("Post Timeout");
        b.setMessage("Post timed out. Refresh the page to check if your post made it through. Dismissing " +
                "and posting again without first checking if the post went through may result in the post " +
                "being submitted twice.");

        b.setPositiveButton("Refresh", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                session.get(Session.determineNetDesc(postPostUrl), postPostUrl);
            }
        });

        b.setNeutralButton("Copy Post", null);

        b.setNegativeButton("Dismiss", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                session.setLastPathAndDesc(postPostUrl, Session.determineNetDesc(postPostUrl));
                ptrCleanup();
            }
        });

        b.setCancelable(false);
        final AlertDialog d = b.create();
        d.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                d.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        android.content.ClipboardManager clipboard =
                                (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("simple text", savedPostBody));

                        Crouton.showText(AllInOneV2.this,
                                "Message body copied to clipboard.",
                                Theming.croutonStyle(),
                                (ViewGroup) v.getParent().getParent());
                    }
                });
            }
        });
        d.show();
    }

    private boolean isRoR = false;
    private void postInterfaceCleanup() {
        if (!isRoR && postWrapper.getVisibility() == View.VISIBLE) {
            if (BuildConfig.DEBUG) wtl("postInterfaceCleanup fired --NEL");
            postWrapper.setVisibility(View.GONE);
            pollButton.setVisibility(View.GONE);
            pollSep.setVisibility(View.GONE);
            postBody.setText(null);
            postTitle.setText(null);
            clearPoll();
            messageIDForEditing = null;

            fab.setVisibility(View.VISIBLE);

            hideSoftKeyboard(postBody);


            if (!pageLabel.getText().toString().equals("~ 1 / 1 ~"))
                pageJumperWrapper.setVisibility(View.VISIBLE);
        }
    }

    private void ptrCleanup() {
        swipeRefreshLayout.setRefreshing(false);
        setAllMenuItemsEnabled(true);
        if (postWrapper.getVisibility() == View.VISIBLE) {
            postSubmitButton.setEnabled(true);
            postCancelButton.setEnabled(true);
            pollButton.setEnabled(true);
        }
    }

    public void setAMPLinkVisible(boolean visible) {
        if (visible)
            findViewById(R.id.dwrAMPWrapper).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.dwrAMPWrapper).setVisibility(View.GONE);
    }

    public void preExecuteSetup(NetDesc desc) {
        if (BuildConfig.DEBUG) wtl("GRAIO dPreES fired --NEL, desc: " + desc.name());

        if (desc != NetDesc.POSTMSG_S1 && desc != NetDesc.POSTTPC_S1 && desc != NetDesc.EDIT_MSG)
            postInterfaceCleanup();

        swipeRefreshLayout.setRefreshing(true);
        setAllMenuItemsEnabled(false);
    }

    /*
     * *****************************************
     * START HNR
     * *****************************************
     */

    ArrayList<BaseRowData> adapterRows = new ArrayList<BaseRowData>();
    ViewAdapter viewAdapter = new ViewAdapter(this, adapterRows);

    @SuppressLint("SetJavaScriptEnabled")
    public void processContent(NetDesc desc, Document doc, String resUrl) {

        if (BuildConfig.DEBUG) wtl("GRAIO hNR fired, desc: " + desc.name());

        swipeRefreshLayout.setEnabled(false);

        if (searchIcon != null)
            searchIcon.collapseActionView();

        setAllMenuItemsExceptRefreshVisibility(false);

        adapterRows.clear();

        boolean isDefaultAcc = Session.getUser() != null &&
                Session.getUser().equals(settings.getString("defaultAccount", HeaderSettings.NO_DEFAULT_ACCOUNT));

        if (BuildConfig.DEBUG) wtl("setting board, topic, message id to null");
        boardID = null;
        topicID = null;
        messageIDForEditing = null;

        Element tbody;
        Element pj;
        String headerTitle;
        String firstPage = null;
        String prevPage = null;
        int[] pagesInfo = new int[]{1, 1};
        String nextPage = null;
        String lastPage = null;
        String pagePrefix = null;

        if (BuildConfig.DEBUG) wtl("checking for board quick list");
        Element boardsDropdown = null;
        for (Element e : doc.select("ul.masthead_mygames_subnav")) {
            if (e.previousElementSibling().ownText().equals("My Boards")) {
                boardsDropdown = e;
                break;
            }
        }
        if (boardsDropdown != null) {
            Elements dItems = boardsDropdown.getElementsByTag("a");
            boardQuickListOptions = new String[dItems.size()];
            boardQuickListLinks = new String[dItems.size()];
            int x = 0;
            for (Element e : dItems) {
                boardQuickListOptions[x] = e.text();
                boardQuickListLinks[x] = e.attr("href");
                x++;
            }

            if (!boardListButton.isLongClickable()) {
                boardListButton.setText(getResources().getString(R.string.board_jumper) +
                        getResources().getString(R.string.board_jumper_quick_list));

                boardListButton.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        showBoardQuickList();
                        return true;
                    }
                });
            }

        } else if (boardListButton.isLongClickable()) {
            boardListButton.setText(getResources().getString(R.string.board_jumper));
            boardListButton.setLongClickable(false);
        }

        contentList.setDividerHeight(Theming.convertDPtoPX(this, 1));

        switch (desc) {
            case BOARD_JUMPER:
            case LOGIN_S2:
                updateHeaderNoJumper("Board Jumper", NetDesc.BOARD_JUMPER);
                adapterRows.add(new HeaderRowData("Announcements"));

                setMenuItemVisibility(searchIcon, true);

                processBoards(doc, true);
                break;

            case BOARD_LIST:
                updateHeaderNoJumper(doc.getElementsByTag("th").get(4).text(), NetDesc.BOARD_LIST);
                processBoards(doc, true);
                break;

            case PM_INBOX:
            case PM_OUTBOX:
                tbody = doc.getElementsByTag("tbody").first();

                boolean isInbox = false;
                if (desc == NetDesc.PM_INBOX)
                    isInbox = true;

                if (isInbox)
                    headerTitle = Session.getUser() + "'s PM Inbox";
                else
                    headerTitle = Session.getUser() + "'s PM Outbox";

                if (tbody != null) {
                    pj = doc.select("ul.paginate").first();

                    if (pj != null) {
                        pagesInfo = getPageJumperInfo(pj);

                        if (isInbox)
                            pagePrefix = "/pm/?page=";
                        else
                            pagePrefix = "/pm/sent?page=";

                        if (pagesInfo[0] > 1) {
                            firstPage = pagePrefix + 0;
                            prevPage = pagePrefix + (pagesInfo[0] - 2);
                        }
                        if (pagesInfo[0] != pagesInfo[1]) {
                            nextPage = pagePrefix + pagesInfo[0];
                            lastPage = pagePrefix + (pagesInfo[1] - 1);
                        }
                    }

                    updateHeader(headerTitle, firstPage, prevPage, pagesInfo[0],
                            pagesInfo[1], nextPage, lastPage, pagePrefix, desc);

                    if (isDefaultAcc && isInbox)
                        NotifierService.dismissPMNotif(this);

                    for (Element row : tbody.getElementsByTag("tr")) {
                        Elements cells = row.children();
                        // [icon] [sender] [subject] [time] [check]
                        boolean isOld = true;
                        if (cells.get(0).children().first().hasClass("icon-circle"))
                            isOld = false;
                        String sender = cells.get(1).text();
                        Element subjectLinkElem = cells.get(2).children().first();
                        String subject = subjectLinkElem.text();
                        String link = subjectLinkElem.attr("href");
                        String time = cells.get(3).text();

                        adapterRows.add(new PMRowData(subject, sender, time, link, isOld, isInbox));
                    }
                } else {
                    updateHeaderNoJumper(headerTitle, desc);
                    adapterRows.add(new HeaderRowData("There are no private messages here at this time."));
                }

                fab.setVisibility(View.VISIBLE);
                pMode = PostMode.NEW_PM;

                if (isInbox)
                    setMenuItemVisibility(pmOutboxIcon, true);
                else
                    setMenuItemVisibility(pmInboxIcon, true);

                break;

            case PM_INBOX_DETAIL:
            case PM_OUTBOX_DETAIL:
                String pmTitle = doc.select("h2.title").first().text();

                String pmMessage = doc.select("div.body").first().outerHtml();

                Element foot = doc.select("div.foot").first();
                foot.child(1).remove();
                String pmFoot = foot.outerHtml();

                //Sent by: P4wn4g3 on 6/1/2013 2:15:55 PM
                String footText = foot.text();

                String sender = footText.substring(9, footText.indexOf(" on "));

                updateHeaderNoJumper(pmTitle, desc);

                if (desc == NetDesc.PM_INBOX_DETAIL) {
                    replyTo = sender;
                    if (!pmTitle.startsWith("Re: "))
                        replySubject = "Re: " + pmTitle;
                    else
                        replySubject = pmTitle;

                    setMenuItemVisibility(replyIcon, true);
                }

                adapterRows.add(new PMDetailRowData(sender, pmTitle, pmMessage + pmFoot));
                break;

            case AMP_LIST:
                if (BuildConfig.DEBUG) wtl("GRAIO hNR determined this is an amp response");

                tbody = doc.getElementsByTag("tbody").first();

                headerTitle = Session.getUser() + "'s Active Messages";

                if (doc.select("ul.paginate").size() > 1) {
                    pj = doc.select("ul.paginate").get(1);
                    if (pj != null && !pj.hasClass("user")
                            && !pj.hasClass("tsort")) {

                        pagesInfo = getPageJumperInfo(pj);

                        pagePrefix = buildAMPLink() + "&page=";
                        if (pagesInfo[0] > 1) {
                            firstPage = pagePrefix + 0;
                            prevPage = pagePrefix + (pagesInfo[0] - 2);
                        }
                        if (pagesInfo[0] != pagesInfo[1]) {
                            nextPage = pagePrefix + pagesInfo[0];
                            lastPage = pagePrefix + (pagesInfo[1] - 1);
                        }
                    }
                }
                updateHeader(headerTitle, firstPage, prevPage, pagesInfo[0],
                        pagesInfo[1], nextPage, lastPage, pagePrefix, NetDesc.AMP_LIST);

                if (isDefaultAcc)
                    NotifierService.dismissAMPNotif(this);

                if (!tbody.children().isEmpty()) {
                    if (settings.getBoolean("notifsEnable", false) && isDefaultAcc) {
                        Element lPost = doc.select("td.lastpost").first();
                        if (lPost != null) {
                            try {
                                String lTime = lPost.text();
                                Date newDate;
                                lTime = lTime.replace("Last:", EMPTY_STRING);
                                if (lTime.contains("AM") || lTime.contains("PM"))
                                    newDate = new SimpleDateFormat("MM'/'dd hh':'mmaa", Locale.US).parse(lTime);
                                else
                                    newDate = new SimpleDateFormat("MM'/'dd'/'yyyy", Locale.US).parse(lTime);
                                long newTime = newDate.getTime();
                                long oldTime = settings.getLong("notifsLastPost", 0);
                                if (newTime > oldTime) {
                                    if (BuildConfig.DEBUG) wtl("time is newer");
                                    settings.edit().putLong("notifsLastPost", newTime).apply();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    for (Element row : tbody.children()) {
                        // [board] [read status] [title] [msg] [last post] [your last post]
                        Elements cells = row.children();
                        String board = cells.get(0).text();
                        Element titleLinkElem = cells.get(2).child(0);
                        String title = titleLinkElem.text();
                        String link = titleLinkElem.attr("href");
                        String mCount = cells.get(3).textNodes().get(0).text().trim();
                        Element lPostLinkElem = cells.get(4).child(1);
                        String lPost = lPostLinkElem.text();
                        String lPostLink = lPostLinkElem.attr("href");

                        ReadStatus status = ReadStatus.UNREAD;
                        String tImg = cells.get(1).child(0).className();
                        if (tImg.endsWith("_read"))
                            status = ReadStatus.READ;
                        else if (tImg.endsWith("_unread")) {
                            status = ReadStatus.NEW_POST;
                            lPostLink = cells.get(1).child(0).attr("href");
                        }

                        adapterRows.add(new AMPRowData(title, board, lPost, mCount, link,
                                lPostLink, status));
                    }
                } else {
                    adapterRows.add(new HeaderRowData("You have no active messages at this time."));
                }

                if (BuildConfig.DEBUG) wtl("amp response block finished");
                break;

            case TRACKED_TOPICS:
                headerTitle = Session.getUser() + "'s Tracked Topics";
                updateHeaderNoJumper(headerTitle, desc);

                if (isDefaultAcc)
                    NotifierService.dismissTTNotif(this);

                tbody = doc.getElementsByTag("tbody").first();

                if (tbody != null) {
                    for (Element row : tbody.children()) {
                        // [remove] [title] [board name] [msgs] [last [pst]
                        Elements cells = row.children();

                        int rsMod = 0;
                        if (cells.size() == 6)
                            rsMod = 1;

                        String removeLink = cells.get(0).child(0)
                                .attr("href");
                        String topicLink = cells.get(1 + rsMod).child(0)
                                .attr("href");
                        String topicText = cells.get(1 + rsMod).text();
                        String board = cells.get(2 + rsMod).text();
                        String msgs = cells.get(3 + rsMod).text();
                        String lPostLink = cells.get(4 + rsMod).child(0)
                                .attr("href");
                        String lPostText = cells.get(4 + rsMod).text();

                        ReadStatus status = ReadStatus.UNREAD;
                        if (rsMod == 1) {
                            String tImg = cells.get(1).child(0).className();
                            if (tImg.endsWith("_read"))
                                status = ReadStatus.READ;
                            else if (tImg.endsWith("_unread"))
                                status = ReadStatus.NEW_POST;
                        }

                        adapterRows.add(new TrackedTopicRowData(board, topicText, lPostText,
                                msgs, topicLink, removeLink, lPostLink, status));
                    }
                } else {
                    adapterRows.add(new HeaderRowData("You have no tracked topics at this time."));
                }
                break;

            case BOARD:
                if (BuildConfig.DEBUG) wtl("GRAIO hNR determined this is a board response");

                if (BuildConfig.DEBUG) wtl("setting board id");
                boardID = parseBoardID(resUrl);

                boolean isSplitList = false;
                if (doc.getElementsByTag("th").first() != null) {
                    if (doc.getElementsByTag("th").first().text().equals("Board Title")) {
                        if (BuildConfig.DEBUG) wtl("is actually a split board list");

                        updateHeaderNoJumper(doc.select("h1.page-title").first().text(), NetDesc.BOARD);

                        processBoards(doc, false);

                        isSplitList = true;
                    }
                }

                if (!isSplitList) {
                    String searchQuery = EMPTY_STRING;
                    String searchPJAddition = EMPTY_STRING;
                    if (resUrl.contains("search=")) {
                        if (BuildConfig.DEBUG) wtl("board search url: " + resUrl);
                        searchQuery = resUrl.substring(resUrl.indexOf("search=") + 7);
                        int i = searchQuery.indexOf('&');
                        if (i != -1)
                            searchQuery = searchQuery.replace(searchQuery.substring(i), EMPTY_STRING);

                        searchPJAddition = "&search=" + searchQuery;
                        try {
                            searchQuery = URLDecoder.decode(searchQuery, DocumentParser.CHARSET_NAME);
                        } catch (UnsupportedEncodingException e) {
                            throw new AssertionError(DocumentParser.CHARSET_NAME + " is unknown");
                            // should never happen
                        }
                    }

                    Element headerElem = doc.getElementsByClass("page-title").first();
                    if (headerElem != null)
                        headerTitle = headerElem.text();
                    else
                        headerTitle = "GFAQs Cache Error, Board Title Not Found";

                    if (searchQuery.length() > 0)
                        headerTitle += " (search: " + searchQuery + ")";

                    if (doc.select("ul.paginate").size() > 1) {
                        pj = doc.select("ul.paginate").get(1);
                        if (pj != null && !pj.hasClass("user")) {
                            pagesInfo = getPageJumperInfo(pj);

                            pagePrefix = "boards/" + boardID + "?page=";
                            if (pagesInfo[0] > 1) {
                                firstPage = pagePrefix + 0 + searchPJAddition;
                                prevPage = pagePrefix + (pagesInfo[0] - 2) + searchPJAddition;
                            }
                            if (pagesInfo[0] != pagesInfo[1]) {
                                nextPage = pagePrefix + pagesInfo[0] + searchPJAddition;
                                lastPage = pagePrefix + (pagesInfo[1] - 1) + searchPJAddition;

                                if (pagesInfo[0] > pagesInfo[1]) {
                                    session.forceNoHistoryAddition();
                                    session.forceSkipAIOCleanup();
                                    Crouton.showText(this, "Page count higher than page amount, going to last page...", Theming.croutonStyle());
                                    session.get(NetDesc.BOARD, lastPage);
                                    return;
                                }
                            }
                        }
                    }
                    updateHeader(headerTitle, firstPage, prevPage, pagesInfo[0], pagesInfo[1], nextPage,
                            lastPage, pagePrefix + searchPJAddition, NetDesc.BOARD);

                    setMenuItemVisibility(searchIcon, true);

                    if (Session.isLoggedIn()) {
                        Element favbtn = doc.getElementsByClass("user").first().getElementsByAttributeValueStarting("onclick", "post_click").first();
                        if (favbtn != null) {
                            String favtext = favbtn.text().toLowerCase();
                            String onclick = favbtn.attr("onclick");
                            int endPoint = onclick.lastIndexOf('\'');
                            int startPoint = onclick.lastIndexOf('\'', endPoint - 1) + 1;
                            favKey = onclick.substring(startPoint, endPoint);
                            fMode = FavMode.ON_BOARD;
                            if (favtext.contains("add to favorites"))
                                setMenuItemVisibility(addFavIcon, true);
                            else if (favtext.contains("remove favorite"))
                                setMenuItemVisibility(remFavIcon, true);
                        }

                        updatePostingRights(doc, false);
                    }

                    Element splitList = doc.select("p:contains(this is a split board)").first();
                    if (splitList != null) {
                        String splitListLink = splitList.child(0).attr("href");
                        adapterRows.add(new BoardRowData("This is a Split Board.", "Click here to return to the Split List.",
                                null, null, null, splitListLink, BoardType.SPLIT));
                    }

                    Element table = doc.select("table.board").first();
                    if (table != null && !table.select("td").first().hasAttr("colspan")) {

                        table.getElementsByTag("col").get(2).remove();
                        table.getElementsByTag("th").get(2).remove();
                        table.getElementsByTag("col").get(0).remove();
                        table.getElementsByTag("th").get(0).remove();

                        if (BuildConfig.DEBUG) wtl("board row parsing start");
                        boolean skipFirst = true;
                        Set<String> hlUsers = hlDB.getHighlightedUsers().keySet();
                        for (Element row : table.getElementsByTag("tr")) {
                            if (!skipFirst) {
                                Elements cells = row.getElementsByTag("td");
                                // cells = [image] [title] [author] [post count] [last post]
                                String tImg = cells.get(0).child(0).className();
                                Element titleLinkElem = cells.get(1).child(0);
                                String title = titleLinkElem.text();
                                String tUrl = titleLinkElem.attr("href");
                                String tc = cells.get(2).text();
                                Element lPostLinkElem = cells.get(4).child(0);
                                String lastPost = lPostLinkElem.text();
                                String lpUrl = lPostLinkElem.attr("href");
                                String mCount = cells.get(3).text();

                                TopicType type = TopicType.NORMAL;
                                if (tImg.contains("poll"))
                                    type = TopicType.POLL;
                                else if (tImg.contains("sticky"))
                                    type = TopicType.PINNED;
                                else if (tImg.contains("closed"))
                                    type = TopicType.LOCKED;
                                else if (tImg.contains("archived"))
                                    type = TopicType.ARCHIVED;

                                if (BuildConfig.DEBUG) wtl(tImg + ", " + type.name());

                                ReadStatus status = ReadStatus.UNREAD;
                                if (tImg.endsWith("_read"))
                                    status = ReadStatus.READ;
                                else if (tImg.endsWith("_unread")) {
                                    status = ReadStatus.NEW_POST;
                                    lpUrl = cells.get(0).child(0).attr("href");
                                }

                                int hlColor = 0;
                                if (hlUsers.contains(tc.toLowerCase(Locale.US))) {
                                    HighlightedUser hUser = hlDB.getHighlightedUsers().get(tc.toLowerCase(Locale.US));
                                    hlColor = hUser.getColor();
                                    tc += " (" + hUser.getLabel() + ")";
                                }

                                adapterRows.add(new TopicRowData(title, tc, lastPost, mCount, tUrl,
                                        lpUrl, type, status, hlColor));
                            } else
                                skipFirst = false;
                        }
                        if (BuildConfig.DEBUG) wtl("board row parsing end");
                    } else {
                        adapterRows.add(new HeaderRowData("There are no topics at this time."));
                    }
                }

                if (BuildConfig.DEBUG) wtl("board response block finished");
                break;

            case TOPIC:
                contentList.setDividerHeight(0);
                boardID = parseBoardID(resUrl);
                topicID = parseTopicID(resUrl);

                tlUrl = "boards/" + boardID;
                if (BuildConfig.DEBUG) wtl(tlUrl);
                setMenuItemVisibility(topicListIcon, true);

                Element headerElem = doc.getElementsByClass("title").first();
                if (headerElem != null)
                    headerTitle = headerElem.text();
                else
                    headerTitle = "GFAQs Cache Error, Title Not Found";

                if (headerTitle.equals("Log In to GameFAQs")) {
                    headerElem = doc.getElementsByClass("title").get(1);
                    if (headerElem != null)
                        headerTitle = headerElem.text();
                }

                if (doc.select("ul.paginate").size() > 1) {
                    pj = doc.select("ul.paginate").get(1);
                    if (pj != null && !pj.hasClass("user")) {
                        pagesInfo = getPageJumperInfo(pj);

                        pagePrefix = "boards/" + boardID + "/" + topicID + "?page=";
                        if (pagesInfo[0] > 1) {
                            firstPage = pagePrefix + 0;
                            prevPage = pagePrefix + (pagesInfo[0] - 2);
                        }
                        if (pagesInfo[0] != pagesInfo[1]) {
                            nextPage = pagePrefix + pagesInfo[0];
                            lastPage = pagePrefix + (pagesInfo[1] - 1);

                            if (pagesInfo[0] > pagesInfo[1]) {
                                session.forceNoHistoryAddition();
                                session.forceSkipAIOCleanup();
                                Crouton.showText(this, "Page count higher than page amount, going to last page...", Theming.croutonStyle());
                                session.get(NetDesc.TOPIC, lastPage);
                                return;
                            }
                        }
                    }
                }
                updateHeader(headerTitle, firstPage, prevPage, pagesInfo[0],
                        pagesInfo[1], nextPage, lastPage, pagePrefix, NetDesc.TOPIC);

                if (Session.isLoggedIn()) {
                    Element favbtn = doc.getElementsByClass("user").first().getElementsByAttributeValueStarting("onclick", "post_click").first();
                    if (favbtn != null) {
                        String favtext = favbtn.text().toLowerCase();
                        String onclick = favbtn.attr("onclick");
                        int endPoint = onclick.lastIndexOf('\'');
                        int startPoint = onclick.lastIndexOf('\'', endPoint - 1) + 1;
                        favKey = onclick.substring(startPoint, endPoint);
                        fMode = FavMode.ON_TOPIC;
                        if (favtext.contains("track topic"))
                            setMenuItemVisibility(addFavIcon, true);
                        else if (favtext.contains("stop tracking"))
                            setMenuItemVisibility(remFavIcon, true);
                    }

                    updatePostingRights(doc, true);
                }

                String goToThisPost = null;
                if (goToUrlDefinedPost) {
                    if (resUrl.indexOf('#') != -1)
                        goToThisPost = resUrl.substring(resUrl.indexOf('#'));
                    else
                        // goToUrlDefinedPost is true when there is no url defined post, oops
                        goToUrlDefinedPost = false;
                }

                Elements rows = doc.select("table.board").first().getElementsByTag("tr");
                int rowCount = rows.size();

                int msgIndex = 0;

                Set<String> hlUsers = hlDB.getHighlightedUsers().keySet();
                for (int x = 0; x < rowCount; x++) {
                    Element row = rows.get(x);

                    if (row.select("div.msg_deleted").isEmpty()) {

                        String user;
                        String postNum;
                        String postTime;
                        String mID = null;
                        String userTitles = EMPTY_STRING;
                        Element msgBody;

                        boolean canReport = false, canDelete = false, canEdit = false, canQuote = false;

                        Element infoBox = row.select("div.msg_infobox").first();
                        user = infoBox.getElementsByTag("b").first().text();

                        Element userInfo = infoBox.select("span.user_info").first();
                        if (userInfo != null)
                            userTitles = " " + userInfo.text();

                        Element userTag = infoBox.select("span.tag").first();
                        if (userTag != null)
                            userTitles += " (" + userTag.text() + ")";

                        postTime = infoBox.select("span.post_time").first().text();

                        Element number = infoBox.select("span.message_num").first();
                        postNum = number.text();
                        if (!number.children().isEmpty()) {
                            mID = parseMessageID(number.child(0).attr("href"));
                        }

                        msgBody = row.select("div.msg_body").first();

                        Element msgBelow = row.select("div.msg_below").first();
                        Element edited = msgBelow.select("span.edited").first();
                        if (edited != null)
                            userTitles += " (edited)";

                        Element belowOptions = msgBelow.select("span.options").first();
                        if (belowOptions != null) {
                            String options = belowOptions.text();
                            if (options.contains("report"))
                                canReport = true;
                            if (options.contains("delete"))
                                canDelete = true;
                            if (options.contains("edit"))
                                canEdit = true;
                            if (options.contains("quote"))
                                canQuote = true;
                        }

                        int hlColor = 0;
                        if (hlUsers.contains(user.toLowerCase(Locale.US))) {
                            HighlightedUser hUser = hlDB
                                    .getHighlightedUsers().get(
                                            user.toLowerCase(Locale.US));
                            hlColor = hUser.getColor();
                            userTitles += " (" + hUser.getLabel() + ")";
                        }

                        if (goToUrlDefinedPost) {
                            if (postNum.equals(goToThisPost))
                                goToThisIndex = msgIndex;
                        }

                        if (BuildConfig.DEBUG) wtl("creating messagerowdata object");
                        adapterRows.add(new MessageRowData(user, userTitles, postNum,
                                postTime, msgBody, boardID, topicID, mID, hlColor, canReport, canDelete, canEdit, canQuote));
                    } else {
                        String postNum = row.select("span.message_num").first().text();
                        if (goToUrlDefinedPost) {
                            if (postNum.equals(goToThisPost))
                                goToThisIndex = msgIndex;
                        }

                        adapterRows.add(new MessageRowData(true, postNum));
                    }

                    msgIndex++;
                }

                break;

            case MESSAGE_DETAIL:
                updateHeaderNoJumper("Message Detail", NetDesc.MESSAGE_DETAIL);

                boardID = parseBoardID(resUrl);
                topicID = parseTopicID(resUrl);
                String mID = parseMessageID(resUrl);

                Elements msgRows = doc.select("td.msg");
                adapterRows.add(new HeaderRowData("Current Version"));

                MessageRowData msg;

                int msgRowCount = msgRows.size();
                for (int x = 0; x < msgRowCount; x++) {
                    if (x == 1)
                        adapterRows.add(new HeaderRowData("Previous Version(s)"));

                    Element currRow = msgRows.get(x);
                    Element msgInfobox = currRow.select("div.msg_infobox").first();
                    Element msgBody = currRow.select("div.msg_body").first();

                    String user = msgInfobox.getElementsByTag("b").first().text();
                    String postTime = msgInfobox.select("span.post_time").first().text();

                    msg = new MessageRowData(user, EMPTY_STRING, "#" + (msgRowCount - x), postTime,
                            msgBody, boardID, topicID, mID, 0, false, false, false, false);
                    msg.disableTopClick();
                    adapterRows.add(msg);
                }

//                Elements msgDRows = doc.getElementsByTag("tr");
//
//                String user = msgDRows.first().child(0).child(0).text();
//
//                adapterRows.add(new HeaderRowData("Current Version"));
//
//                Element currRow, body;
//                MessageRowData msg;
//                String postTime;
//                String mID = parseMessageID(resUrl);
//                for (int x = 0; x < msgDRows.size(); x++) {
//                    if (x == 1)
//                        adapterRows.add(new HeaderRowData("Previous Version(s)"));
//                    else {
//                        currRow = msgDRows.get(x);
//
//                        if (currRow.child(0).textNodes().size() > 1)
//                            postTime = currRow.child(0).textNodes().get(1).text();
//                        else
//                            postTime = currRow.child(0).textNodes().get(0).text();
//
//                        body = currRow.child(1);
//                        msg = new MessageRowData(user, null, null, postTime, body, boardID, topicID, mID, 0, "");
//                        msg.disableTopClick();
//                        adapterRows.add(msg);
//                    }
//                }

                break;

            case TAG_USER:
                if (BuildConfig.DEBUG) wtl("starting check for user tag success");
                Element error = doc.getElementsByClass("error").first();
                if (error == null) {
                    Crouton.showText(this, "User tag updated successfully.", Theming.croutonStyle());
                } else {
                    AlertDialog.Builder b = new AlertDialog.Builder(this);
                    b.setTitle("There was an error tagging the user...");
                    b.setMessage("Error message from GameFAQs:\n\n" + error.text());
                    b.setPositiveButton("OK", null);
                    b.show();
                }
            case USER_DETAIL:
                if (BuildConfig.DEBUG) wtl("starting user detail processing");
                tbody = doc.select("table.board").first().getElementsByTag("tbody").first();
                String name = null;
                String ID = null;
                String level = null;
                String creation = null;
                String lVisit = null;
                String sig = null;
                String karma = null;
                String AMP = null;
                String tagKey = null;
                String tagText = null;
                for (Element row : tbody.children()) {
                    String label = row.child(0).text().toLowerCase(Locale.US);
                    if (BuildConfig.DEBUG) wtl("user detail row label: " + label);
                    if (label.equals("user name"))
                        name = row.child(1).text();
                    else if (label.equals("user id"))
                        ID = row.child(1).text();
                    else if (label.equals("board user level")) {
                        level = row.child(1).html();
                        if (BuildConfig.DEBUG) wtl("set level: " + level);
                    } else if (label.equals("account created"))
                        creation = row.child(1).text();
                    else if (label.equals("last visit"))
                        lVisit = row.child(1).text();
                    else if (label.equals("signature"))
                        sig = row.child(1).html();
                    else if (label.equals("karma"))
                        karma = row.child(1).text();
                    else if (label.equals("active messages posted"))
                        AMP = row.child(1).text();
                }

                if (Session.isLoggedIn()) {
                    Element button = doc.select("input.btn").first();
                    if (button != null && button.attr("value").startsWith("Send a PM to"))
                        setMenuItemVisibility(sendUserPMIcon, true);

                    setMenuItemVisibility(tagUserIcon, true);
                    tagKey = doc.getElementsByAttributeValue("name", "key").attr("value");
                    tagText = doc.getElementsByAttributeValue("name", "tag_text").attr("value");
                    if (tagText == null) tagText = "";
                }

                updateHeaderNoJumper(name + "'s Details", NetDesc.USER_DETAIL);

                userDetailData = new UserDetailRowData(name, ID, level, creation, lVisit, sig,
                        karma, AMP, tagKey, tagText, resUrl);
                adapterRows.add(userDetailData);
                break;

            case GAME_SEARCH:
                if (BuildConfig.DEBUG) wtl("GRAIO hNR determined this is a game search response");

                if (BuildConfig.DEBUG) wtl("game search url: " + resUrl);

                String searchQuery = resUrl.substring(resUrl.indexOf("game=") + 5);
                int i = searchQuery.indexOf("&");
                if (i != -1)
                    searchQuery = searchQuery.replace(searchQuery.substring(i), EMPTY_STRING);

                int pageIndex = resUrl.indexOf("page=");
                if (pageIndex != -1) {
                    String currPage = resUrl.substring(pageIndex + 5);
                    i = currPage.indexOf("&");
                    if (i != -1)
                        currPage = currPage.replace(currPage.substring(i), EMPTY_STRING);
                    pagesInfo[0] = Integer.parseInt(currPage) + 1;
                } else {
                    pagesInfo[0] = 1;
                }

                if (pagesInfo[0] > 1) {
                    firstPage = "/search/index.html?game=" + searchQuery + "&page=0";
                    prevPage = "/search/index.html?game=" + searchQuery + "&page=" + (pagesInfo[0] - 2);
                }
                if (!doc.getElementsByClass("icon-angle-right").isEmpty()) {
                    nextPage = "/search/index.html?game=" + searchQuery + "&page=" + (pagesInfo[0]);
                }

                try {
                    headerTitle = "Searching games: " + URLDecoder.decode(searchQuery, DocumentParser.CHARSET_NAME) + EMPTY_STRING;
                } catch (UnsupportedEncodingException e) {
                    throw new AssertionError(DocumentParser.CHARSET_NAME + " is unknown");
                    // should never happen
                }

                updateHeader(headerTitle, firstPage, prevPage, pagesInfo[0],
                        -1, nextPage, lastPage, pagePrefix, NetDesc.GAME_SEARCH);

                setMenuItemVisibility(searchIcon, true);

                Elements gameSearchTables = doc.select("table.results");
                int tCount = gameSearchTables.size();
                int tCounter = 0;
                if (!gameSearchTables.isEmpty()) {
                    for (Element table : gameSearchTables) {
                        tCounter++;
                        if (tCounter < tCount)
                            adapterRows.add(new HeaderRowData("Best Matches"));
                        else
                            adapterRows.add(new HeaderRowData("Good Matches"));

                        String prevPlatform = EMPTY_STRING;

                        if (BuildConfig.DEBUG) wtl("board row parsing start");
                        for (Element row : table.getElementsByTag("tr")) {
                            if (row.parent().tagName().equals("tbody")) {
                                Elements cells = row.getElementsByTag("td");
                                // cells = [platform] [title] [faqs] [codes] [saves] [revs] [mygames] [q&a] [pics] [vids] [board]
                                String platform = cells.get(0).text();
                                String bName = cells.get(1).text();
                                String bUrl = cells.get(9).child(0).attr("href");
                                if (platform.codePointAt(0) == ('\u00A0')) {
                                    platform = prevPlatform;
                                } else {
                                    prevPlatform = platform;
                                }
                                adapterRows.add(new GameSearchRowData(bName, platform, bUrl));
                            }
                        }

                        if (BuildConfig.DEBUG) wtl("board row parsing end");
                    }
                } else {
                    adapterRows.add(new HeaderRowData("No results."));
                }

                if (BuildConfig.DEBUG) wtl("game search response block finished");
                break;

            default:
                if (BuildConfig.DEBUG) wtl("GRAIO hNR determined response type is unhandled");
                getSupportActionBar().setTitle("Page unhandled - " + resUrl);
                break;
        }

        Element pmInboxLink = doc.select("div.masthead_user").first().select("a[href=/pm/]").first();
        String pmButtonLabel = getResources().getString(R.string.pm_inbox);
        if (pmInboxLink != null) {
            String text = pmInboxLink.text();
            int count = 0;

            if (text.contains("(")) {
                count = Integer.parseInt(text.substring(text.indexOf('(') + 1, text.indexOf(')')));
                int prevCount = settings.getInt("unreadPMCount", 0);

                if (count > prevCount) {
                    if (count > 1)
                        Crouton.showText(this, "You have " + count + " unread PMs", Theming.croutonStyle());
                    else
                        Crouton.showText(this, "You have 1 unread PM", Theming.croutonStyle());
                }

                pmButtonLabel += " (" + count + ")";
            }

            settings.edit().putInt("unreadPMCount", count).apply();
            if (isDefaultAcc)
                settings.edit().putInt("notifsUnreadPMCount", count).apply();
        }

        ((Button) findViewById(R.id.dwrPMInbox)).setText(pmButtonLabel);

        Element trackedLink = doc.select("div.masthead_user").first().select("a[href=/boards/tracked]").first();
        String ttButtonLabel = getResources().getString(R.string.tracked_topics);
        if (trackedLink != null) {
            String text = trackedLink.text();
            int count = 0;

            if (text.contains("(")) {
                count = Integer.parseInt(text.substring(text.indexOf('(') + 1, text.indexOf(')')));
                int prevCount = settings.getInt("unreadTTCount", 0);

                if (count > prevCount) {
                    if (count > 1)
                        Crouton.showText(this, "You have " + count + " unread tracked topics", Theming.croutonStyle());
                    else
                        Crouton.showText(this, "You have 1 unread tracked topic", Theming.croutonStyle());
                }

                ttButtonLabel += " (" + count + ")";
            }

            settings.edit().putInt("unreadTTCount", count).apply();
            if (isDefaultAcc)
                settings.edit().putInt("notifsUnreadTTCount", count).apply();
        }

        ((Button) findViewById(R.id.dwrTrackedTopics)).setText(ttButtonLabel);

        swipeRefreshLayout.setEnabled(settings.getBoolean("enablePTR", false));

        viewAdapter.notifyDataSetChanged();

        if (consumeGoToUrlDefinedPost() && !Session.applySavedScroll) {
            contentList.post(new Runnable() {
                @Override
                public void run() {
                    contentList.setSelection(goToThisIndex);
                }
            });

        } else if (Session.applySavedScroll) {
            contentList.post(new Runnable() {
                @Override
                public void run() {
                    contentList.setSelectionFromTop(Session.savedScrollVal[0], Session.savedScrollVal[1]);
                    Session.applySavedScroll = false;
                }
            });

        } else {
            contentList.post(new Runnable() {
                @Override
                public void run() {
                    contentList.setSelectionAfterHeaderView();
                }
            });
        }

        if (swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);

        if (BuildConfig.DEBUG) wtl("GRAIO hNR finishing");
    }

    /*
     * ********************************
     * END HNR
     * ********************************
     */

    private void processBoards(Document pRes, boolean includeBoardCategories) {
        Elements homeTables = pRes.select("table.board");

        boolean skippedFirst = false;
        for (Element row : homeTables.first().getElementsByTag("tr")) {
            if (skippedFirst) {
                if (row.hasClass("head")) {
                    adapterRows.add(new HeaderRowData(row.text()));
                } else {
                    // [title + link] [topics] [msgs] [last post]
                    Elements cells = row.children();
                    Element titleCell = cells.get(0);

                    String lvlReq = EMPTY_STRING;
                    if (!titleCell.textNodes().isEmpty())
                        lvlReq = titleCell.textNodes().get(0).toString();

                    String title = titleCell.child(0).text() + lvlReq;

                    String boardDesc = null;
                    if (titleCell.children().size() > 2)
                        boardDesc = titleCell.child(2).text();

                    String link = titleCell.children().first().attr("href");
                    if (link.isEmpty())
                        link = null;

                    String tCount = null;
                    String mCount = null;
                    String lPost = null;

                    BoardType bvt;

                    if (cells.size() > 3) {
                        tCount = cells.get(1).text();
                        mCount = cells.get(2).text();
                        lPost = cells.get(3).text();

                        bvt = BoardType.NORMAL;
                    } else
                        bvt = BoardType.SPLIT;

                    adapterRows.add(new BoardRowData(title, boardDesc, lPost, tCount, mCount, link, bvt));
                }
            } else {
                skippedFirst = true;
            }
        }

        if (includeBoardCategories && homeTables.size() > 1) {
            int rowX = 0;
            for (Element row : homeTables.get(1).getElementsByTag("tr")) {
                rowX++;
                if (rowX > 2) {
                    Element cell = row.child(0);
                    String title = cell.child(0).text();
                    String link = cell.child(0).attr("href");
                    String boardDesc = cell.child(2).text();
                    adapterRows.add(new BoardRowData(title, boardDesc, null, null, null, link, BoardType.LIST));
                } else {
                    if (rowX == 1) {
                        adapterRows.add(new HeaderRowData("Message Board Categories"));
                    }
                }
            }
        }
    }

    private int[] getPageJumperInfo(Element pj) {
        int[] i = new int[]{1, 1};
        if (pj != null && !pj.hasClass("user") && !pj.hasClass("tsort")) {
            String currPage, pageCount;

            int x = 0;
            String pjText = pj.child(x).text();
            while (pjText.contains("First") || pjText.contains("Previous")) {
                x++;
                pjText = pj.child(x).text();
            }
            // Page [dropdown] of 4
            // Page 1 of 3
            if (pj.getElementsByTag("select").isEmpty()) {
                int ofIndex = pjText.indexOf(" of ");
                currPage = pjText.substring(5, ofIndex); // "Page ".length = 5

                int pageCountEnd = pjText.length();
                pageCount = pjText.substring(ofIndex + 4, pageCountEnd);
            } else {
                currPage = pj.select("option[selected=selected]").first().text();
                pageCount = pj.getElementsByTag("option").last().text();
            }

            i[0] = Integer.parseInt(currPage);
            i[1] = Integer.parseInt(pageCount);
        }
        return i;
    }

    private void updatePostingRights(Document pRes, boolean onTopic) {
        if (onTopic) {
            if (pRes.getElementsByClass("user").first().text().contains("Post New Message")) {
                fab.setVisibility(View.VISIBLE);
                pMode = PostMode.ON_TOPIC;
            }
        } else {
            if (pRes.getElementsByClass("user").first().text().contains("New Topic")) {
                fab.setVisibility(View.VISIBLE);
                pMode = PostMode.ON_BOARD;
            }
        }
    }

    public void postExecuteCleanup(NetDesc desc) {
        if (BuildConfig.DEBUG)
            wtl("GRAIO dPostEC --NEL, desc: " + (desc == null ? "null" : desc.name()));

        if (needToSetNavList) {
            setNavList(Session.isLoggedIn());
        }

        ptrCleanup();
        if (desc == NetDesc.BOARD || desc == NetDesc.TOPIC)
            postInterfaceCleanup();

        if (isRoR)
            isRoR = false;

        System.gc();
    }

    private boolean goToUrlDefinedPost = false;
    private int goToThisIndex = 0;

    public void enableGoToUrlDefinedPost() {
        goToUrlDefinedPost = true;
    }

    private boolean consumeGoToUrlDefinedPost() {
        boolean temp = goToUrlDefinedPost;
        goToUrlDefinedPost = false;
        return temp;
    }

    private void updateHeader(String titleIn, String firstPageIn, String prevPageIn, int currPage,
                              int pageCount, String nextPageIn, String lastPageIn,
                              String jumperPageIn, NetDesc desc) {

        if (pageCount == 1) {
            updateHeaderNoJumper(titleIn, desc);
            return;
        }

        getSupportActionBar().setTitle(titleIn);

        if (currPage == -1) {
            pageJumperWrapper.setVisibility(View.GONE);
            pageLabel.setText("~ 1 / 1 ~");
        } else {
            pageJumperWrapper.setVisibility(View.VISIBLE);
            pageJumperDesc = desc;

            if (firstPageIn != null) {
                firstPageUrl = firstPageIn;
                firstPage.setEnabled(true);
            } else {
                firstPage.setEnabled(false);
            }

            if (prevPageIn != null) {
                prevPageUrl = prevPageIn;
                prevPage.setEnabled(true);
            } else {
                prevPage.setEnabled(false);
            }

            if (nextPageIn != null) {
                nextPageUrl = nextPageIn;
                nextPage.setEnabled(true);
            } else {
                nextPage.setEnabled(false);
            }

            if (lastPageIn != null) {
                lastPageUrl = lastPageIn;
                lastPage.setEnabled(true);
            } else {
                lastPage.setEnabled(false);
            }

            if (pageCount != -1) {
                jumperPageUrl = jumperPageIn;

                final String[] items = new String[pageCount];
                for (int x = 0; x < pageCount; x++) {
                    items[x] = String.valueOf(x + 1);
                }

                if (jumperDialogBuilder == null) {
                    jumperDialogBuilder = new AlertDialog.Builder(this);
                    jumperDialogBuilder.setTitle("Select a page...");
                }

                jumperDialogBuilder.setItems(items, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int x = jumperPageUrl.indexOf("?page=") + 6;
                        if (x == 5) // -1 + 6 = 5, when "?page=" is not found
                            x = jumperPageUrl.indexOf("&page=") + 6;

                        String go = jumperPageUrl.substring(0, x) + which + jumperPageUrl.substring(x);
                        if (BuildConfig.DEBUG) wtl("jumper dialog url: " + go);
                        session.get(pageJumperDesc, go);
                    }
                });

                pageLabel.setEnabled(true);
                pageLabel.setText("~ " + currPage + " / " + pageCount + " ~");
            } else {
                pageLabel.setEnabled(false);
                pageLabel.setText(currPage + " / ???");
            }
        }
    }

    private void updateHeaderNoJumper(String title, NetDesc desc) {
        updateHeader(title, null, null, -1, -1, null, null, null, desc);
    }

    private MessageRowView clickedMsg;
    private String quoteSelection;

    public void messageMenuClicked(MessageRowView msg) {
        clickedMsg = msg;
        quoteSelection = clickedMsg.getSelection();

        showDialog(MESSAGE_ACTION_DIALOG);
    }

    private void editPostSetup(String msg, String msgID) {
        postBody.setText(msg);
        messageIDForEditing = msgID;
        postSetup(true);
    }

    private void quoteSetup(String user, String msg) {
        if (BuildConfig.DEBUG) wtl("quoteSetup fired");
        String quotedMsg = "<cite>" + user + " posted...</cite>\n" + "<quote>" + msg + "</quote>\n\n";

        int start = Math.max(postBody.getSelectionStart(), 0);
        int end = Math.max(postBody.getSelectionEnd(), 0);

        assert postBody.getText() != null : "postBody.getText() is null";
        postBody.getText().replace(Math.min(start, end), Math.max(start, end), quotedMsg);

        if (postWrapper.getVisibility() != View.VISIBLE)
            postSetup(true);
        else
            postBody.setSelection(Math.min(start, end) + quotedMsg.length());

        if (BuildConfig.DEBUG) wtl("quoteSetup finishing");
    }

    private void postSetup(boolean postingOnTopic) {
        findViewById(R.id.aioHTMLScroller).scrollTo(0, 0);
        pageJumperWrapper.setVisibility(View.GONE);
        fab.setVisibility(View.GONE);
        postSubmitButton.setEnabled(true);
        postCancelButton.setEnabled(true);

        if (postingOnTopic) {
            titleWrapper.setVisibility(View.GONE);
            postBody.requestFocus();
            assert postBody.getText() != null : "postBody.getText() is null";
            postBody.setSelection(postBody.getText().length());
        } else {
            titleWrapper.setVisibility(View.VISIBLE);
            if (Session.userHasAdvancedPosting()) {
                pollButton.setEnabled(true);
                pollButton.setVisibility(View.VISIBLE);
                pollSep.setVisibility(View.VISIBLE);
            }
            postTitle.requestFocus();
        }

        postWrapper.setVisibility(View.VISIBLE);
        postPostUrl = session.getLastPath();
        if (postPostUrl.contains("#"))
            postPostUrl = postPostUrl.substring(0, postPostUrl.indexOf('#'));
    }

    public void postCancel(View view) {
        if (BuildConfig.DEBUG) wtl("postCancel fired --NEL");
        if (settings.getBoolean("confirmPostCancel", false)) {
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setMessage("Cancel this post?");
            b.setPositiveButton("Yes", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    postInterfaceCleanup();
                }
            });
            b.setNegativeButton("No", null);
            b.show();
        } else
            postInterfaceCleanup();
    }

    public void postPollOptions(View view) {
        showDialog(POLL_OPTIONS_DIALOG);
    }

    public void postDo(View view) {
        if (BuildConfig.DEBUG) wtl("postDo fired");
        if (settings.getBoolean("confirmPostSubmit", false)) {
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setMessage("Submit this post?");
            b.setPositiveButton("Yes", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    postSubmit();
                }
            });
            b.setNegativeButton("No", null);
            b.show();
        } else
            postSubmit();
    }

    private void postSubmit() {
        assert postBody.getText() != null : "postBody.getText() is null";
        assert postTitle.getText() != null : "postTitle.getText() is null";

        if (titleWrapper.getVisibility() == View.VISIBLE) {
            if (BuildConfig.DEBUG) wtl("posting on a board");
            // posting on a board
            String path = Session.ROOT + "/boards/post?board=" + boardID;
            int i = path.indexOf('-');
            path = path.substring(0, i);
            if (BuildConfig.DEBUG) wtl("post path: " + path);
            savedPostBody = postBody.getText().toString();
            if (BuildConfig.DEBUG) wtl("saved post body: " + savedPostBody);
            savedPostTitle = postTitle.getText().toString();
            if (BuildConfig.DEBUG) wtl("saved post title: " + savedPostTitle);
            if (BuildConfig.DEBUG) wtl("sending topic");
            postSubmitButton.setEnabled(false);
            pollButton.setEnabled(false);
            postCancelButton.setEnabled(false);
            if (pollUse)
                path += "&poll=1";

            session.get(NetDesc.POSTTPC_S1, path);
        } else {
            // posting on a topic
            if (BuildConfig.DEBUG) wtl("posting on a topic");
            String path = Session.ROOT + "/boards/post?board=" + boardID + "&topic=" + topicID;
            if (messageIDForEditing != null)
                path += "&message=" + messageIDForEditing;

            if (BuildConfig.DEBUG) wtl("post path: " + path);
            savedPostBody = postBody.getText().toString();
            if (BuildConfig.DEBUG) wtl("saved post body: " + savedPostBody);
            if (BuildConfig.DEBUG) wtl("sending post");
            postSubmitButton.setEnabled(false);
            postCancelButton.setEnabled(false);
            if (messageIDForEditing != null)
                session.get(NetDesc.EDIT_MSG, path);
            else
                session.get(NetDesc.POSTMSG_S1, path);
        }
    }

    private String reportCode;

    public String getReportCode() {
        return reportCode;
    }

    /**
     * creates dialogs
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;

        switch (id) {

            case SEND_PM_DIALOG:
                dialog = createSendPMDialog();
                break;

            case MESSAGE_ACTION_DIALOG:
                dialog = createMessageActionDialog();
                break;

            case REPORT_MESSAGE_DIALOG:
                dialog = createReportMessageDialog();
                break;

            case POLL_OPTIONS_DIALOG:
                dialog = createPollOptionsDialog();
                break;

            case CHANGE_LOGGED_IN_DIALOG:
                dialog = createChangeLoggedInDialog();
                break;
        }

        return dialog;
    }

    private Dialog createPollOptionsDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);

        b.setTitle("Poll Options");
        LayoutInflater inflater = getLayoutInflater();

        @SuppressLint("InflateParams")
        final View v = inflater.inflate(R.layout.polloptions, null);
        b.setView(v);
        b.setCancelable(false);

        final EditText[] options = new EditText[10];

        assert v != null : "v is null";
        final CheckBox poUse = (CheckBox) v.findViewById(R.id.poUse);
        final EditText poTitle = (EditText) v.findViewById(R.id.poTitle);
        options[0] = (EditText) v.findViewById(R.id.po1);
        options[1] = (EditText) v.findViewById(R.id.po2);
        options[2] = (EditText) v.findViewById(R.id.po3);
        options[3] = (EditText) v.findViewById(R.id.po4);
        options[4] = (EditText) v.findViewById(R.id.po5);
        options[5] = (EditText) v.findViewById(R.id.po6);
        options[6] = (EditText) v.findViewById(R.id.po7);
        options[7] = (EditText) v.findViewById(R.id.po8);
        options[8] = (EditText) v.findViewById(R.id.po9);
        options[9] = (EditText) v.findViewById(R.id.po10);
        final Spinner minLevel = (Spinner) v.findViewById(R.id.poMinLevel);

        poUse.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                poTitle.setEnabled(isChecked);
                for (int x = 0; x < 10; x++)
                    options[x].setEnabled(isChecked);
            }
        });

        for (int x = 0; x < 10; x++)
            options[x].setText(pollOptions[x]);

        minLevel.setSelection(pollMinLevel);
        poTitle.setText(pollTitle);
        poUse.setChecked(pollUse);

        b.setPositiveButton("Save", new OnClickListener() {
            @Override
            @SuppressWarnings("ConstantConditions")
            public void onClick(DialogInterface dialog, int which) {
                pollUse = poUse.isChecked();
                pollTitle = poTitle.getText().toString();
                pollMinLevel = minLevel.getSelectedItemPosition();

                for (int x = 0; x < 10; x++) {
                    pollOptions[x] = options[x].getText().toString();
                }
            }
        });

        b.setNegativeButton("Cancel", null);

        b.setNeutralButton("Clear", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearPoll();
            }
        });

        Dialog dialog = b.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                removeDialog(POLL_OPTIONS_DIALOG);
            }
        });
        return dialog;
    }

    private void clearPoll() {
        pollUse = false;
        pollTitle = EMPTY_STRING;
        for (int x = 0; x < 10; x++)
            pollOptions[x] = EMPTY_STRING;
        pollMinLevel = -1;
    }


    private Dialog createReportMessageDialog() {
        AlertDialog.Builder reportMsgBuilder = new AlertDialog.Builder(this);
        reportMsgBuilder.setTitle("Report Message");

        final String[] reportOptions;
        if (clickedMsg.getPostNum().equals("1"))
            reportOptions = getResources().getStringArray(R.array.msgReportReasonsWithOffTopic);
        else
            reportOptions = getResources().getStringArray(R.array.msgReportReasons);

        reportMsgBuilder.setItems(reportOptions, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reportCode = getResources().getStringArray(R.array.msgReportCodes)[which];

                /*
                <form action="http://www.gamefaqs.com/features/board_mark/pick.php" method="post">
                <input type="hidden" name="b" value="848">
                <input type="hidden" name="t" value="71881473">
                <input type="hidden" name="m" value="821951056">
                <input type="hidden" name="r" value="8">
                <input type="hidden" name="rt" value="Testing">
                <input type="hidden" name="i" value="0">
                <input type="hidden" name="key" value="[session key]">
                <input type="submit">
                </form>
                 */

                HashMap<String, List<String>> markData = new HashMap<>();
                markData.put("b", Collections.singletonList(boardID));
                markData.put("t", Collections.singletonList(topicID));
                markData.put("m", Collections.singletonList(clickedMsg.getMessageID()));
                markData.put("r", Collections.singletonList(reportCode));
                markData.put("rt", Collections.singletonList(EMPTY_STRING));
                markData.put("i", Collections.singletonList("0"));
                markData.put("key", Collections.singletonList(session.getSessionKey()));

                session.post(NetDesc.MARKMSG, "/features/board_mark/pick.php", markData);


//                session.get(NetDesc.MARKMSG_S1, clickedMsg.getMessageDetailLink());
            }
        });

        reportMsgBuilder.setNegativeButton("Cancel", null);

        Dialog dialog = reportMsgBuilder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                removeDialog(REPORT_MESSAGE_DIALOG);
            }
        });
        return dialog;
    }

    private Dialog createMessageActionDialog() {
        AlertDialog.Builder msgActionBuilder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();

        @SuppressLint("InflateParams")
        final View v = inflater.inflate(R.layout.msgaction, null);

        msgActionBuilder.setView(v);

        msgActionBuilder.setTitle("Message Actions");

        ArrayList<String> listBuilder = new ArrayList<String>();

        if (clickedMsg.getMessageID() != null) {
            if (clickedMsg.isEdited())
                listBuilder.add("View Previous Version(s)");
            else
                listBuilder.add("Message Detail");
        }

        if (clickedMsg.canQuote())
            listBuilder.add("Quote");
        if (clickedMsg.canEdit())
            listBuilder.add("Edit");
        if (clickedMsg.canDelete())
            listBuilder.add("Delete");
        if (clickedMsg.canReport())
            listBuilder.add("Report");

        listBuilder.add("Highlight User");
        listBuilder.add("User Details");

        assert v != null : "v is null";
        ListView lv = (ListView) v.findViewById(R.id.maList);
        final LinearLayout wrapper = (LinearLayout) v.findViewById(R.id.maWrapper);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        adapter.addAll(listBuilder);

        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) parent.getItemAtPosition(position);
                assert selected != null : "selected is null";
                if (selected.equals("View Previous Version(s)") || selected.equals("Message Detail")) {
                    session.get(NetDesc.MESSAGE_DETAIL, clickedMsg.getMessageDetailLink());

                } else if (selected.equals("Quote")) {
                    String msg = (quoteSelection != null ? quoteSelection : clickedMsg.getMessageForQuoting());
                    quoteSetup(clickedMsg.getUser(), msg);

                } else if (selected.equals("Edit")) {
                    editPostSetup(clickedMsg.getMessageForEditing(), clickedMsg.getMessageID());

                } else if (selected.equals("Delete")) {
                    HashMap<String, List<String>> delData = new HashMap<>();
                    delData.put("action", Collections.singletonList("delete"));
                    delData.put("key", Collections.singletonList(session.getSessionKey()));

                    session.post(NetDesc.DELETEMSG,
                            clickedMsg.getMessageDetailLink().replace("/boards/", "/boardaction/"), delData);

                } else if (selected.equals("Report")) {
                    showDialog(REPORT_MESSAGE_DIALOG);

                } else if (selected.equals("Highlight User")) {
                    HighlightedUser user = hlDB.getHighlightedUsers().get(clickedMsg.getUser().toLowerCase(Locale.US));
                    HighlightListDBHelper.showHighlightUserDialog(AllInOneV2.this, user, clickedMsg.getUser(), null);

                } else if (selected.equals("User Details")) {
                    session.get(NetDesc.USER_DETAIL, clickedMsg.getUserDetailLink());

                } else {
                    Crouton.showText(AllInOneV2.this, "not recognized: " + selected, Theming.croutonStyle());
                }

                dismissDialog(MESSAGE_ACTION_DIALOG);
            }
        });

        msgActionBuilder.setNegativeButton("Cancel", null);

        Dialog dialog = msgActionBuilder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                removeDialog(MESSAGE_ACTION_DIALOG);
            }
        });

        dialog.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                if (quoteSelection != null)
                    Crouton.showText(AllInOneV2.this, "Selected text prepped for quoting.", Theming.croutonStyle(), wrapper);
            }
        });

        return dialog;
    }

    private LinearLayout pmSending;

    private Dialog createSendPMDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();

        @SuppressLint("InflateParams")
        final View v = inflater.inflate(R.layout.sendpm, null);

        b.setView(v);
        b.setTitle("Send Private Message");
        b.setCancelable(false);

        assert v != null : "v is null";
        final EditText to = (EditText) v.findViewById(R.id.spTo);
        final EditText subject = (EditText) v.findViewById(R.id.spSubject);
        final EditText message = (EditText) v.findViewById(R.id.spMessage);
        pmSending = (LinearLayout) v.findViewById(R.id.spFootWrapper);

        to.setText(savedTo);
        subject.setText(savedSubject);
        message.setText(savedMessage);

        b.setPositiveButton("Send", null);
        b.setNegativeButton("Cancel", null);

        final AlertDialog d = b.create();
        d.setOnShowListener(new OnShowListener() {
            @Override
            @SuppressWarnings("ConstantConditions")
            public void onShow(DialogInterface dialog) {
                d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String toContent = to.getText().toString();
                        String subjectContent = subject.getText().toString();
                        String messageContent = message.getText().toString();

                        if (toContent.length() > 0) {
                            if (subjectContent.length() > 0) {
                                if (messageContent.length() > 0) {
                                    savedTo = toContent;
                                    savedSubject = subjectContent;
                                    savedMessage = messageContent;

                                    pmSending.setVisibility(View.VISIBLE);

                                    session.get(NetDesc.SEND_PM_S1, "/pm/new");

                                } else
                                    Crouton.showText(AllInOneV2.this,
                                            "The message can't be empty.",
                                            Theming.croutonStyle(),
                                            (ViewGroup) to.getParent());
                            } else
                                Crouton.showText(AllInOneV2.this,
                                        "The subject can't be empty.",
                                        Theming.croutonStyle(),
                                        (ViewGroup) to.getParent());
                        } else
                            Crouton.showText(AllInOneV2.this,
                                    "The recipient can't be empty.",
                                    Theming.croutonStyle(),
                                    (ViewGroup) to.getParent());
                    }
                });
            }
        });

        d.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                pmSending = null;
                removeDialog(SEND_PM_DIALOG);
            }
        });
        return d;
    }

    private static final String LOG_OUT_LABEL = "*Log Out*";

    private Dialog createChangeLoggedInDialog() {
        AlertDialog.Builder accountChanger = new AlertDialog.Builder(this);

        String[] keys = AccountManager.getUsernames(this);

        final String[] usernames = new String[keys.length + 1];
        usernames[0] = LOG_OUT_LABEL;
        System.arraycopy(keys, 0, usernames, 1, keys.length);

        final String currUser = Session.getUser();
        int selected = 0;

        for (int x = 1; x < usernames.length; x++) {
            if (usernames[x].equals(currUser))
                selected = x;
        }

        accountChanger.setTitle("Pick an Account");
        accountChanger.setSingleChoiceItems(usernames, selected, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String selUser = usernames[item];
                if (selUser.equals(LOG_OUT_LABEL) && currUser != null)
                    session = new Session(AllInOneV2.this);

                else {
                    if (!selUser.equals(currUser) && !selUser.equals(LOG_OUT_LABEL))
                        if (session.hasNetworkConnection())
                            session = new Session(AllInOneV2.this,
                                    selUser,
                                    AccountManager.getPassword(AllInOneV2.this, selUser),
                                    session.getLastPath(),
                                    session.getLastDesc());
                        else
                            noNetworkConnection();
                }

                dismissDialog(CHANGE_LOGGED_IN_DIALOG);
            }
        });

        accountChanger.setNegativeButton("Cancel", null);

        accountChanger.setPositiveButton("Manage Accounts", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(AllInOneV2.this, SettingsAccount.class));
            }
        });


        final AlertDialog d = accountChanger.create();
        d.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                removeDialog(CHANGE_LOGGED_IN_DIALOG);
            }
        });
        return d;
    }

    private Button boardListButton;
    private String[] boardQuickListOptions;
    private String[] boardQuickListLinks;

    private void showBoardQuickList() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("My Boards");
        b.setNegativeButton("Cancel", null);
        b.setItems(boardQuickListOptions, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                session.get(NetDesc.BOARD, boardQuickListLinks[which]);
            }
        });
        drawerLayout.closeDrawers();
        b.show();
    }

    private String replyTo, replySubject;
    public String savedTo, savedSubject, savedMessage;

    public void pmSetup(String toIn, String subjectIn, String messageIn) {
        if (toIn != null && !toIn.equals("null"))
            savedTo = toIn;
        else
            savedTo = EMPTY_STRING;

        if (subjectIn != null && !subjectIn.equals("null"))
            savedSubject = subjectIn;
        else
            savedSubject = EMPTY_STRING;

        if (messageIn != null && !messageIn.equals("null"))
            savedMessage = messageIn;
        else
            savedMessage = EMPTY_STRING;

        try {
            savedTo = URLDecoder.decode(savedTo, DocumentParser.CHARSET_NAME);
            savedSubject = URLDecoder.decode(savedSubject, DocumentParser.CHARSET_NAME);
            savedMessage = URLDecoder.decode(savedMessage, DocumentParser.CHARSET_NAME);
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(DocumentParser.CHARSET_NAME + " is unknown");
            // should never happen
        }

        showDialog(SEND_PM_DIALOG);
    }

    public void pmCleanup(boolean wasSuccessful, String error) {
        if (wasSuccessful) {
            Crouton.showText(this, "PM sent.", Theming.croutonStyle());
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(pmSending.getWindowToken(), 0);

            dismissDialog(SEND_PM_DIALOG);
        } else {
            Crouton.showText(this, error, Theming.croutonStyle(), (ViewGroup) pmSending.getParent());
            pmSending.setVisibility(View.GONE);
        }
    }


    public void refreshClicked(View view) {
        if (BuildConfig.DEBUG) wtl("refreshClicked fired --NEL");
        if (view != null)
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

        if (session.getLastPath() == null) {
            if (Session.isLoggedIn()) {
                if (BuildConfig.DEBUG) wtl("starting new session from refreshClicked, logged in");
                session = new Session(this, Session.getUser(), AccountManager.getPassword(this, Session.getUser()));
            } else {
                if (BuildConfig.DEBUG) wtl("starting new session from refreshClicked, no login");
                session = new Session(this);
            }
        } else
            session.refresh();
    }

    public String getSig() {
        String sig = EMPTY_STRING;

        if (session != null) {
            if (Session.isLoggedIn())
                sig = settings.getString("customSig" + Session.getUser(), EMPTY_STRING);
        }

        if (sig.length() == 0)
            sig = settings.getString("customSig", EMPTY_STRING);

        if (sig.length() == 0)
            sig = defaultSig;

        return sig.replace("*grver*", BuildConfig.VERSION_NAME);
    }

    private static long lastNano = 0;

    public static void wtl(String msg) {
        if (BuildConfig.DEBUG) {
            long currNano = System.nanoTime();

            msg = msg.replaceAll("\\\\n", "(nl)");

            long elapsed;
            if (lastNano == 0)
                elapsed = 0;
            else
                elapsed = currNano - lastNano;

            elapsed = elapsed / 1000000;

            if (elapsed > 100)
                Log.w("logger", "time since previous log was over 100 milliseconds");

            lastNano = System.nanoTime();

            msg = elapsed + "// " + msg;
            Log.d("logger", msg);
        }
    }

    public void tryCaught(String url, String desc, Throwable e, String source) {
        if (!BuildConfig.DEBUG) {
            ACRAConfiguration config = ACRA.getConfig();
            config.setResToastText(R.string.bug_toast_text);

            ACRA.getErrorReporter().putCustomData("URL", url);
            ACRA.getErrorReporter().putCustomData("NetDesc", desc);
            ACRA.getErrorReporter().putCustomData("Page Source", StringEscapeUtils.escapeJava(source));
            ACRA.getErrorReporter().putCustomData("Last Attempted Path", session.getLastAttemptedPath());
            ACRA.getErrorReporter().putCustomData("Last Attempted Desc", session.getLastAttemptedDesc().toString());
            ACRA.getErrorReporter().handleException(e);

            config.setResToastText(R.string.crash_toast_text);
        }
        else {
            Log.e("tryCaught", "", e);
        }
    }

    private String parseBoardID(String url) {
        if (BuildConfig.DEBUG) wtl("parseBoardID fired");
        // board example: http://www.gamefaqs.com/boards/400-current-events
        String boardUrl = url.substring(Session.ROOT.length() + 8);

        int i = boardUrl.indexOf('/');
        if (i != -1) {
            String replacer = boardUrl.substring(i);
            boardUrl = boardUrl.replace(replacer, EMPTY_STRING);
        }

        i = boardUrl.indexOf('?');
        if (i != -1) {
            String replacer = boardUrl.substring(i);
            boardUrl = boardUrl.replace(replacer, EMPTY_STRING);
        }
        i = boardUrl.indexOf('#');
        if (i != -1) {
            String replacer = boardUrl.substring(i);
            boardUrl = boardUrl.replace(replacer, EMPTY_STRING);
        }

        if (BuildConfig.DEBUG) wtl("boardID: " + boardUrl);
        return boardUrl;
    }

    private String parseTopicID(String url) {
        if (BuildConfig.DEBUG) wtl("parseTopicID fired");
        // topic example: http://www.gamefaqs.com/boards/400-current-events/64300205
        String topicUrl = url.substring(url.indexOf('/', Session.ROOT.length() + 8) + 1);
        int i = topicUrl.indexOf('/');
        if (i != -1) {
            String replacer = topicUrl.substring(i);
            topicUrl = topicUrl.replace(replacer, EMPTY_STRING);
        }
        i = topicUrl.indexOf('?');
        if (i != -1) {
            String replacer = topicUrl.substring(i);
            topicUrl = topicUrl.replace(replacer, EMPTY_STRING);
        }
        i = topicUrl.indexOf('#');
        if (i != -1) {
            String replacer = topicUrl.substring(i);
            topicUrl = topicUrl.replace(replacer, EMPTY_STRING);
        }
        if (BuildConfig.DEBUG) wtl("topicID: " + topicUrl);
        return topicUrl;
    }

    private String parseMessageID(String url) {
        if (BuildConfig.DEBUG) wtl("parseMessageID fired");
        String msgID = url.substring(url.lastIndexOf('/') + 1);
        if (BuildConfig.DEBUG) wtl("messageIDForEditing: " + msgID);
        return msgID;
    }


    @Override
    public void onBackPressed() {
        if (searchIcon != null && searchIcon.isActionViewExpanded()) {
            searchIcon.collapseActionView();
        } else if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawers();
        } else if (postWrapper.getVisibility() == View.VISIBLE) {
            postCancel(postCancelButton);
        } else {
            goBack();
        }
    }

    private void goBack() {
        if (session != null && session.canGoBack()) {
            if (BuildConfig.DEBUG) wtl("back pressed, history exists, going back");
            session.goBack(false);
        } else {
            if (BuildConfig.DEBUG) wtl("back pressed, no history, exiting app");
            finish();
        }
    }

    public static String buildAMPLink() {
        return "/boards/myposts.php?lp=" + settings.getString("ampSortOption", "-1");
    }

    private void censorWord(StringBuilder builder, String textLower, String word) {
        int length = word.length();
        String replacement = "";

        for (int x = 0; x < length - 1; x++)
            replacement += '*';

        while (textLower.contains(word)) {
            int start = textLower.indexOf(word);
            int end = start + length;
            builder.replace(start + 1, end, replacement);
            textLower = textLower.replaceFirst(word, replacement + '*');
        }
    }

    @SuppressWarnings("ConstantConditions")
    public void htmlButtonClicked(View view) {
        String open = ((TextView) view).getText().toString();
        String close = "</" + open.substring(1);

        int start = Math.max(postBody.getSelectionStart(), 0);
        int end = Math.max(postBody.getSelectionEnd(), 0);

        String insert;
        if (start != end)
            insert = open + postBody.getText().subSequence(start, end) + close;
        else
            insert = open + close;

        postBody.getText().replace(Math.min(start, end), Math.max(start, end), insert, 0, insert.length());
    }
}
