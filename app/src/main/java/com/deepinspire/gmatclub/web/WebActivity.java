package com.deepinspire.gmatclub.web;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.os.StatFs;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.HttpAuthHandler;
import android.webkit.JavascriptInterface;
import android.webkit.MimeTypeMap;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deepinspire.gmatclub.GCConfig;
import com.deepinspire.gmatclub.R;
import com.deepinspire.gmatclub.api.Api;
import com.deepinspire.gmatclub.api.AuthException;
import com.deepinspire.gmatclub.auth.AuthActivity;
import com.deepinspire.gmatclub.storage.DeviceInfo;
import com.deepinspire.gmatclub.utils.GCWebView;
import com.deepinspire.gmatclub.utils.ViewHelper;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.github.clans.fab.Label;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WebActivity extends AppCompatActivity implements
        IWebContract.View, SwipeRefreshLayout.OnRefreshListener, View.OnClickListener, View.OnTouchListener {

    private static final int FILECHOOSER_RESULTCODE  = 100;

    private static final int PICK_FROM_CAMERA = 101;

    private static final int DEVICE_SETTINGS = 102;

    private IWebContract.Presenter presenter;

    private GCWebView webView;

    private Map<String, String> requestExtraHeaders = new HashMap<>();

    private LinearLayout feedbackLayout;

    private SwipeRefreshLayout swipe;

    private DrawerLayout mDrawerLayout;

    private ProgressBar progressBar;

    private LinearLayout btnAddLayout;
    private FloatingActionMenu btnAdd;

    private FloatingActionButton btnAddTopic;
    private FloatingActionButton btnAddPm;
    private FloatingActionButton btnAddChat;
    private FloatingActionButton btnAddSchool;

    private ImageView toolbarShare;
    private ImageView toolbarProfile;
    private ImageView toolbarSearch;

    private LinearLayout toolbarTitleLayout;
    private TextView toolbarTitle;

    private SearchView searchView;

    private TextView countNotifications;
    private TextView countPMs;

    private CookieManager cookieManager;

    private ActionBarDrawerToggle toggle;

    private String activeUrl = null;

    private NavigationView nv;

    private int highlightedItemMenu = -1;

    private CallbackManager callbackManager;

    private ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener;

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    private Handler mUiHandler = new Handler();

    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadMessages;
    private Uri mCapturedImageURI = null;

    private boolean openedKeyboard = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);*/

        setPresenter(new WebPresenter(this.getApplicationContext(),this));

        setContentView(R.layout.activity_web);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowTitleEnabled(false);

        initNavigationView(toolbar);

        feedbackLayout = (LinearLayout) findViewById(R.id.feedbackLayout);

        swipe = (SwipeRefreshLayout) findViewById(R.id.swipe);
        swipe.setOnRefreshListener(this);

        swipe
            .getViewTreeObserver()
            .addOnScrollChangedListener(mOnScrollChangedListener =
            new ViewTreeObserver.OnScrollChangedListener() {
                @Override
                public void onScrollChanged() {
                    if (webView.getScrollY() == 0)
                        swipe.setEnabled(true);
                    else
                        swipe.setEnabled(false);

                }
            });

        progressBar = (ProgressBar) findViewById(R.id.loading);

        final View activityRootView = findViewById(R.id.top_parent);

        activityRootView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();

                        if (heightDiff > dpToPx(WebActivity.this, 200)) {
                            openedKeyboard = true;
                            showBtnAdd(false);
                        } else {
                            if (openedKeyboard && logged()) {
                                openedKeyboard = false;

                                String url = webView.getUrl();

                                if(url == null ||
                                        (url.contains(Api.UCP_URL) && url.contains("mode=compose")) ||
                                        url.contains(Api.TESTS_URL)) {
                                    showBtnAdd(false);
                                } else {
                                    showBtnAdd(true);
                                }
                            }
                        }
                    }
        });


        btnAddLayout = (LinearLayout) findViewById(R.id.btnAddMenuLayout);

        btnAddLayout.setOnClickListener(this);

        btnAdd = (FloatingActionMenu) findViewById(R.id.btnAddMenu);

        btnAdd.setAnimationDelayPerItem(0);

        btnAdd.setAnimated(false);

        btnAdd.setOnMenuButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnAdd.isOpened()) {
                    toogleAddMenu(false);
                } else {
                    toogleAddMenu(true);
                }
            }
        });

        btnAddTopic = (FloatingActionButton) findViewById(R.id.btnAddTopic);
        btnAddTopic.setOnClickListener(this);
        //btnAddTopic.setVisibility(View.GONE);

        btnAddPm = (FloatingActionButton) findViewById(R.id.btnAddPm);
        btnAddPm.setOnClickListener(this);
        //btnAddPm.setVisibility(View.GONE);

        btnAddChat = (FloatingActionButton) findViewById(R.id.btnAddChat);
        btnAddChat.setOnClickListener(this);
        //btnAddChat.setVisibility(View.GONE);

        btnAddSchool = (FloatingActionButton) findViewById(R.id.btnAddSchool);
        btnAddSchool.setOnClickListener(this);
        //btnAddSchool.setVisibility(View.GONE);

        toolbarTitleLayout = (LinearLayout) findViewById(R.id.toolbarTitleLayout);

        toolbarTitle = (TextView) findViewById(R.id.toolbarTitle);
        toolbarTitleLayout = (LinearLayout) findViewById(R.id.toolbarTitleLayout);
        toolbarTitleLayout.setOnClickListener(this);

        toolbarShare = (ImageView) findViewById(R.id.toolbarShare);
        toolbarShare.setOnClickListener(this);

        toolbarSearch = (ImageView) findViewById(R.id.toolbarSearch);
        toolbarSearch.setOnClickListener(this);

        toolbarProfile = (ImageView) findViewById(R.id.toolbarProfile);
        toolbarProfile.setOnClickListener(this);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        searchView = (SearchView) findViewById(R.id.toolbarSearchView);//.getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                StringBuilder encodedStringBuilder = new StringBuilder();
                try {
                    encodedStringBuilder
                            .append("?q=")
                            .append(URLEncoder.encode(query, "UTF-8"));
                    encodedStringBuilder
                            .append("&cx=")
                            .append(URLEncoder.encode("009332468639467955845:h-z9cduzcoi", "UTF-8"));
                    encodedStringBuilder
                            .append("&cof=")
                            .append(URLEncoder.encode("FORID:10", "UTF-8"));
                    encodedStringBuilder
                            .append("&ie=")
                            .append(URLEncoder.encode("UTF-8", "UTF-8"));
                    openPage(Api.FORUM_GOOGLE_SEARCH + encodedStringBuilder.toString());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                return true;
            }
        });

        searchView.setVisibility(View.GONE);

        nv = (NavigationView) findViewById(R.id.nav_view);

        countNotifications = (TextView) nv.findViewById(R.id.menu_notifications_count);
        countPMs = (TextView) nv.findViewById(R.id.menu_pms_count);

        countNotifications.setVisibility(View.GONE);
        countPMs.setVisibility(View.GONE);

        showMenu();

        if (savedInstanceState == null) {
            initWebView();
        }

        initRequestExtraHeaders();

        Intent intent  = getIntent();

        if(intent != null) {
            onNewIntent(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        swipe.getViewTreeObserver().removeOnScrollChangedListener(mOnScrollChangedListener);
    }

    private void highlightMenuItemOnClick(int id) {
        if(highlightedItemMenu == -1) {
            highlightOnClick(id, true);
        } else {
            highlightOnClick(highlightedItemMenu, false);
            highlightOnClick(id, true);
        }

        highlightedItemMenu = id;
    }

    private void highlightOnClick(int id, boolean activate) {
        int backgroundColor =  activate ? R.color.main : R.color.mainBackgroundNavigationView;
        int textColor = activate ? R.color.white : R.color.main;

        switch(id) {
            case R.id.menu_pms:
                ((RelativeLayout) nv.findViewById(id)).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), backgroundColor));
                ((TextView) nv.findViewById(R.id.menu_pms_text)).setTextColor(ContextCompat.getColor(getApplicationContext(), textColor));
                ((TextView) nv.findViewById(R.id.menu_pms_count)).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.mainOrange));
                break;
            case R.id.menu_notifications:
                ((RelativeLayout) nv.findViewById(id)).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), backgroundColor));
                ((TextView) nv.findViewById(R.id.menu_notifications_text)).setTextColor(ContextCompat.getColor(getApplicationContext(), textColor));
                ((TextView) nv.findViewById(R.id.menu_notifications_count)).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.mainOrange));

                break;
            default: {
                ((TextView) nv.findViewById(id)).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), backgroundColor));
                ((TextView) nv.findViewById(id)).setTextColor(ContextCompat.getColor(getApplicationContext(), textColor));
            }
        }
    }

    private void highlightOnTouchDown(int id) {
        int backgroundColor =  R.color.mainHighlightTouchNavigationView;
        int textColor = R.color.main;

        switch(id) {
            case R.id.menu_pms:
                ((RelativeLayout) nv.findViewById(id)).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), backgroundColor));
                ((TextView) nv.findViewById(R.id.menu_pms_count)).setTextColor(ContextCompat.getColor(getApplicationContext(), textColor));
                break;
            case R.id.menu_notifications:
                ((RelativeLayout) nv.findViewById(id)).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), backgroundColor));
                ((TextView) nv.findViewById(R.id.menu_notifications_count)).setTextColor(ContextCompat.getColor(getApplicationContext(), textColor));
                break;
            default: {
                ((TextView) nv.findViewById(id)).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), backgroundColor));
                ((TextView) nv.findViewById(id)).setTextColor(ContextCompat.getColor(getApplicationContext(), textColor));
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.toolbarShare:
                String url = webView.getUrl();

                if(url != null) {
                    url = url.replace("?style=12", "").replace("&style=12", "");

                    Intent sendIntent = new Intent();
                    sendIntent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                    sendIntent.setAction(Intent.ACTION_SEND);

                    sendIntent.putExtra(Intent.EXTRA_TEXT, url);
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                }
                break;
            case R.id.toolbarProfile:
                changeProfileIconColor(R.color.white);

                if(logged()) {
                    ViewHelper.showProfileAuthDialog(WebActivity.this);
                } else {
                    ViewHelper.showProfileAnonDialog(WebActivity.this);
                }
                break;
            case R.id.toolbarSearch:
                toogleAddMenu(false);
                toolbarShare.setVisibility(View.GONE);
                toolbarTitleLayout.setVisibility(View.GONE);
                toolbarTitle.setVisibility(View.GONE);
                toolbarSearch.setVisibility(View.GONE);
                searchView.setVisibility(View.VISIBLE);
                searchView.onActionViewExpanded();
                break;
            case R.id.btnAddTopic:
                toogleAddMenu(false);
                //openPage(Api.FORUM_SEARCH_NEW_POSTS);
                openPage(Api.FORUM_NEW_POSTS);
                break;
            case R.id.btnAddPm:
                toogleAddMenu(false);
                openPage(Api.PM_NEW_URL);
                break;
            case R.id.btnAddChat:
                toogleAddMenu(false);
                openPage(Api.CHAT_URL);
                break;
            case R.id.btnAddSchool:
                toogleAddMenu(false);
                this.activeUrl = Api.FORUM_ADD_NEW_SCHOOL;
                openPage(Api.FORUM_ADD_NEW_SCHOOL);
                break;
            case R.id.btnAddMenuLayout:
                if(btnAdd.isOpened()) {
                    toogleAddMenu(false);
                    btnAddLayout.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.transparent));
                    btnAddLayout.getLayoutParams().height = btnAdd.getHeight();
                    btnAddLayout.getLayoutParams().width = btnAdd.getWidth();
                    btnAddLayout.requestLayout();
                }
                break;
            case R.id.toolbarTitleLayout:
                highlightMenuItemOnClick(R.id.menu_forum);

                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                }

                openPage(Api.FORUM_URL);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        //swipe.setRefreshing(false);
        webView.loadUrl(webView.getUrl(), getRequestExtraHeaders());
        //webView.reload();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_main, menu);

        //getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        /*MenuItem item = menu.findItem(R.id.action_search);
        ImageView imageView = new ImageView(WebActivity.this);
        imageView.setMaxHeight(24);
        imageView.setMaxWidth(24);
        imageView.setImageResource(R.drawable.ic_search_white_24dp);
        item.setActionView(imageView);*/

        /*SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) findViewById(R.id.toolbarSearchHidden);//.getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));*/

        //Drawable searchIcon = getResources().getDrawable(R.drawable.ic_search_white_24dp_1x);

        // Read your drawable from somewhere
        /*Drawable dr = getResources().getDrawable(R.drawable.ic_search_white_24dp);
        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
            // Scale it to 50 x 50
        Drawable d = new BitmapDrawable(
                getResources(),
                Bitmap.createScaledBitmap(
                        bitmap,
                        Math.round(bitmap.getWidth()*.45f),
                        Math.round(bitmap.getHeight() * .45f),
                        true
                )
        );*/
        // Set your new, scaled drawable "d"

        /*int searchImgId = android.support.v7.appcompat.R.id.search_button; // I used the explicit layout ID of searchview's ImageView
        ImageView v = (ImageView) searchView.findViewById(searchImgId);
        v.setImageDrawable(d);*/

        /*int searchImgId = getResources().getIdentifier("android:id/action_search", null, null);
        ImageView ivIcon = (ImageView) searchView.findViewById(searchImgId);

        if(ivIcon!=null)
            ivIcon.setImageResource(R.drawable.ic_search_white_24dp_1x);*/

        /*ImageView v = (ImageView) searchView.findViewById(R.id.action_search);
        v.setImageResource(R.drawable.ic_search_white_24dp_1x);*/

        /*searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                StringBuilder encodedStringBuilder = new StringBuilder();
                try {
                    encodedStringBuilder
                            .append("?q=")
                            .append(URLEncoder.encode(query, "UTF-8"));
                    encodedStringBuilder
                            .append("&cx=")
                            .append(URLEncoder.encode("009332468639467955845:h-z9cduzcoi", "UTF-8"));
                    encodedStringBuilder
                            .append("&cof=")
                            .append(URLEncoder.encode("FORID:10", "UTF-8"));
                    encodedStringBuilder
                            .append("&ie=")
                            .append(URLEncoder.encode("UTF-8", "UTF-8"));
                    openPage(Api.FORUM_GOOGLE_SEARCH + encodedStringBuilder.toString());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                return true;
            }
        });*/

        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();

        if (view != null) {
            if((ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) && view instanceof EditText && !view.getClass().getName().startsWith("android.webkit.")) {
                int scrcoords[] = new int[2];

                view.getLocationOnScreen(scrcoords);

                float x = ev.getRawX() + view.getLeft() - scrcoords[0];
                float y = ev.getRawY() + view.getTop() - scrcoords[1];

                if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom()) {
                    searchView.setVisibility(View.GONE);
                    toolbarSearch.setVisibility(View.VISIBLE);
                    toolbarTitleLayout.setVisibility(View.VISIBLE);
                    toolbarTitle.setVisibility(View.VISIBLE);
                    toolbarShare.setVisibility(View.VISIBLE);
                    searchView.onActionViewCollapsed();

                    toogleAddMenu(false);
                }
            }

            switch(ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    switch (view.getId()) {
                        case R.id.menu_forum:
                        case R.id.menu_pms:
                        case R.id.menu_notifications:
                        case R.id.menu_practice:
                        case R.id.menu_chat:
                        case R.id.menu_reviews:
                        case R.id.menu_deals_discounts:
                        case R.id.menu_advanced_search:
                        case R.id.menu_new_posts:
                        case R.id.menu_feedback:
                        case R.id.menu_unanswered:
                            highlightOnTouchDown(view.getId());
                            break;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            
        }

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouch(View v,MotionEvent event) {
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == FacebookSdk.getCallbackRequestCodeOffset()) {
            if(callbackManager != null) {
                callbackManager.onActivityResult(requestCode, resultCode, data);
            }
        } else if(requestCode == FILECHOOSER_RESULTCODE) {

            if (null == mUploadMessage && null == mUploadMessages) {
                return;
            }

            if (null != mUploadMessage) {
                handleUploadMessage(requestCode, resultCode, data);

            } else if (mUploadMessages != null) {
                handleUploadMessages(requestCode, resultCode, data);
            }
        } else {
            switch(requestCode) {
                case GCConfig.GOOGLE_SIGN_IN:
                    break;
                    case GCConfig.EMAIL:
                    break;
                case DEVICE_SETTINGS:
                    if(ViewHelper.alertDialog != null) {
                        ViewHelper.alertDialog.dismiss();
                        ViewHelper.alertDialog = null;
                    }
                    tryAgain();
                    break;
            }
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Uri uri = intent.getData();

        String url = null;

        if(uri != null) {
            url = uri.toString();
        } else {
            url = intent.getStringExtra("url");
        }

        if(url.equals(Api.FORUM_URL)) {
            highlightMenuItemOnClick(R.id.menu_forum);
        }

        openPage(url);
    }

    public void setPresenter(IWebContract.Presenter presenter) {
        this.presenter = presenter;
    }

    public void goByMenu(View view) {
        switch (view.getId()) {
            case R.id.menu_forum:
                openPageFromHamburgerMenu(R.id.menu_forum, Api.FORUM_URL);
                break;
            case R.id.menu_pms:
                openPageFromHamburgerMenu(R.id.menu_pms, Api.PM_URL);
                break;
            case R.id.menu_notifications:
                openPageFromHamburgerMenu(R.id.menu_notifications, Api.FORUM_NOTIFICATIONS/*"file:///android_asset/notifications.html"*/);
                break;
            case R.id.menu_practice:
                openPageFromHamburgerMenu(R.id.menu_practice, Api.PRACTICE_URL);
                break;
            case R.id.menu_chat:
                openPageFromHamburgerMenu(R.id.menu_chat, Api.CHAT_URL);
                break;
            case R.id.menu_reviews:
                openPageFromHamburgerMenu(R.id.menu_reviews, Api.REVIEWS_URL);
                break;
            case R.id.menu_deals_discounts:
                openPageFromHamburgerMenu(R.id.menu_deals_discounts, Api.DEALS_URL);
                break;
            case R.id.menu_advanced_search:
                openPageFromHamburgerMenu(R.id.menu_advanced_search, Api.FORUM_SEARCH);
                break;
            case R.id.menu_new_posts:
                openPageFromHamburgerMenu(R.id.menu_new_posts, Api.FORUM_SEARCH_NEW_POSTS);
                break;
            case R.id.menu_unanswered:
                openPageFromHamburgerMenu(R.id.menu_unanswered, Api.FORUM_SEARCH_UNANSWERED);
                break;
            case R.id.menu_feedback:
                mDrawerLayout.closeDrawers();

                Intent intent = new Intent(Intent.ACTION_SEND);

                intent.setType("message/rfc822");

                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@gmatclub.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "GMAT Club Android Application Feedback");
                intent.putExtra(Intent.EXTRA_TEXT, "body of email");

                try {
                    startActivityForResult(Intent.createChooser(intent, "Send mail..."), GCConfig.EMAIL);
                } catch (ActivityNotFoundException exception) {
                    Toast.makeText(getApplicationContext(), "There are no email clients installed.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @SuppressLint({"AddJavascriptInterface", "SetJavaScriptEnabled"})
    private void initWebView() {
        webView = (GCWebView) findViewById(R.id.webView);

        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDatabaseEnabled(true);
        //settings.setDatabasePath("/data/data/" + webView.getContext().getPackageName() + "/databases/");
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        settings.setAppCacheEnabled(false);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        //settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        //settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        settings.setAllowFileAccessFromFileURLs(true); //Maybe you don't need this rule
        settings.setAllowUniversalAccessFromFileURLs(true);

        String device = "Android" + " " + "GMAT Club Forum";

        try {
            device += "/" + getPackageManager().getPackageInfo(getApplication().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        settings.setUserAgentString(device);

        //settings.setLoadWithOverviewMode(true);
        //settings.setUseWideViewPort(true);

        //settings.setSupportZoom(true);

        /*settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);*/

        //webView.zoomBy(0.1f);
        //settings.setDomStorageEnabled(true);
        //settings.setUserAgentString("Android GMAT Club Forum/1.0.0");
        //settings.setSupportZoom(true);
        //This the the enabling of the zoom controls
        //settings.setBuiltInZoomControls(true);
        //This will zoom out the WebView
        //settings.setUseWideViewPort(true);
        //settings.setLoadWithOverviewMode(true);
        //webView.setInitialScale(1);
        //webView.setWebContentsDebuggingEnabled(true);

        //settings.setRenderPriority(WebSettings.RenderPriority.HIGH);

        //webView.zoomIn();
        //webView.zoomOut();

        //settings.setLoadWithOverviewMode(true);
        //webView.setInitialScale(100);

        //webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);

        //webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // chromium, enable hardware acceleration
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        webView.addJavascriptInterface(new GCJavascriptInterface(this), "GCAndroid");

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                //Toast.makeText(getApplicationContext(), consoleMessage.message(), Toast.LENGTH_LONG).show();
                 /*Log.d("MyApplication", consoleMessage.message() + " -- From line "
                        + consoleMessage.lineNumber() + " of "
                        + consoleMessage.sourceId());*/
                return super.onConsoleMessage(consoleMessage);
            }

            @Override
            public void onProgressChanged(WebView view, int progress) {
                /*if (progress == 100) {
                    switch(presenter.getError()) {initWeb
                        case  WebViewClient.ERROR_HOST_LOOKUP:
                        case  WebViewClient.ERROR_CONNECT:
                            ViewHelper.showOfflineDialog(WebActivity.this);
                            break;
                        default: {
                            if(ViewHelper.alertDialog != null) {
                                ViewHelper.alertDialog.dismiss();
                                ViewHelper.alertDialog = null;
                            }
                        }
                    }
                }*/
            }

            // openFileChooser for Android 3.0+

            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType){
                mUploadMessage = uploadMsg;
                openImageChooser();
            }

            // For Lollipop 5.0+ Devices

            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                mUploadMessages = filePathCallback;
                openImageChooser();
                return true;
            }

            // openFileChooser for Android < 3.0

            public void openFileChooser(ValueCallback<Uri> uploadMsg){
                openFileChooser(uploadMsg, "");
            }

            //openFileChooser for other Android versions

            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                openFileChooser(uploadMsg, acceptType);
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                //handler.proceed("deepdesi", "deepcomp");
                handler.proceed("guest", "GCTesterNew1");
            }

            public void onLoadResource(WebView view, String url) {
                //Toast.makeText(WebActivity.this, "onLoadResource", Toast.LENGTH_SHORT).show();
            }

            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                //webView.destroyDrawingCache();
                if(url.equals(Api.FORUM_URL + "?style=12") || url.equals(Api.FORUM_URL + "/?style=12")) {
                    changeTitleColor(R.color.white);
                    changeProfileIconColor(R.color.mainOrange);
                } else if(url.contains(Api.PROFILE)) {
                    changeTitleColor(R.color.mainOrange);
                    changeProfileIconColor(R.color.white);
                } else {
                    changeTitleColor(R.color.mainOrange);
                    changeProfileIconColor(R.color.mainOrange);
                }

                swipe.setRefreshing(false);
                setLoadingIndicator(true);
            }

            public void onPageFinished(WebView view, String url) {
                switch(presenter.getError()) {
                    case  WebViewClient.ERROR_HOST_LOOKUP:
                    case  WebViewClient.ERROR_CONNECT:
                        presenter.setError(0);
                        ViewHelper.showOfflineDialog(WebActivity.this);
                        break;
                    default: {
                        if(activeUrl != null && url.contains(activeUrl)) {
                            activeUrl = null;

                            webView.evaluateJavascript(
                                    "(function(){document.querySelector('#statusesDecTracker .addMyInfo.btn').click();})()",
                                    new ValueCallback<String>() {
                                        @Override
                                        public void onReceiveValue(String value) {
                                            swipe.setRefreshing(false);
                                            setLoadingIndicator(false);
                                        }
                                    }
                            );

                            webView.stopLoading();
                        } else if(url.contains(Api.HOME_URL)) {
                            presenter.logged();
                            swipe.setRefreshing(false);
                            setLoadingIndicator(false);
                        } else if(url.contains(Api.PM_URL)) {
                            presenter.updatePMs();
                            updateCountMessages();
                        }

                        if(ViewHelper.alertDialog != null) {
                            ViewHelper.alertDialog.dismiss();
                            ViewHelper.alertDialog = null;
                        }
                    }
                }

            }

            /*@SuppressWarnings("deprecation") // From API 21 we should use another overload
            @Override
            public WebResourceResponse shouldInterceptRequest(@NonNull WebView view, @NonNull String url) {
                return handleRequestViaOkHttp(url);
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public WebResourceResponse shouldInterceptRequest(@NonNull WebView view, @NonNull WebResourceRequest request) {
                return handleRequestViaOkHttp(request.getUrl().toString());
            }*/

            /*@Override
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                //view.loadUrl(request.getUrl().toString(), getRequestExtraHeaders());
                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //view.loadUrl(url, getRequestExtraHeaders());
                return true;
            }*/

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                presenter.setError(errorCode);
                //Toast.makeText(WebActivity.this, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }

            @TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                // Redirect to deprecated method, so you can use it in all SDK versions
                onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
            }
        });
    }

    /*static boolean urlShouldBeHandledByWebView(@NonNull String url) {
        // file: Resolve requests to local files such as files from cache folder via WebView itself
        return url.startsWith("file:");
    }

    @NonNull
    private WebResourceResponse handleRequestViaOkHttp(@NonNull String url) {
        try {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .build();

            String username = "guest";
            String password = "GCTesterNew1";

            String base = username + ":" + password;

            String authHeader = "Basic " + Base64.encodeToString(base.getBytes(), Base64.NO_WRAP);
            //OkHttpClient.Builder okHttpClient1 = new OkHttpClient().newBuilder();\

            // On Android API >= 21 you can get request method and headers
            // As I said, we need to only display "simple" page with resources
            // So it's GET without special headers
            final Call call = okHttpClient.newCall(new Request.Builder()
                    .addHeader("Authorization", authHeader)
                    .addHeader("My-Agent", getRequestExtraHeaders().get("My-Agent"))
                    .url(url)
                    .build()
            );

            final Response response = call.execute();

            return new WebResourceResponse(
                    getMimeType(url),
                    //response.header("content-type", "text/html"), // You can set something other as default content-type
                    response.header("content-encoding", "utf-8"),  // Again, you can set another encoding as default
                    response.body().byteStream()
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);

        if (extension != null) {

            switch (extension) {
                case "js":
                    return "text/javascript";
                case "woff":
                    return "application/font-woff";
                case "woff2":
                    return "application/font-woff2";
                case "ttf":
                    return "application/x-font-ttf";
                case "eot":
                    return "application/vnd.ms-fontobject";
                case "svg":
                    return "image/svg+xml";
            }

            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }

        return type;
    }*/

    private void initRequestExtraHeaders() {
        String device = "Android" + " " + Build.MODEL + " " + Build.VERSION.RELEASE + " " + "GMAT Club Forum" ;// + getString(R.string.app_name);

        try {
            device += "/" + getPackageManager().getPackageInfo(getApplication().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        requestExtraHeaders.put("My-Agent", device);
    }

    private Map<String, String> getRequestExtraHeaders() {
        return requestExtraHeaders;
    }

    private void openPageFromHamburgerMenu(int id, String url) {
        mDrawerLayout.closeDrawers();
        highlightMenuItemOnClick(id);
        openPage(url);
    }

    private void openPage(String url) {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }

        webView.loadUrl("javascript:document.open();document.close();");
        //webView.loadUrl("about:blank");
        webView.loadUrl(url, getRequestExtraHeaders());
    }

    public void setLoadingIndicator(boolean active) {
        if (active) {
            //progressBar.setVisibility(View.VISIBLE);
            //feedbackLayout.setVisibility(View.GONE);
            //swipe.setVisibility(View.GONE);
            showBtnAdd(false);
        } else {
            hideKeyboard();

            if(logged()) {
                toogleAddMenu(false);

                String url = webView.getUrl();

                if(url == null || (url.contains(Api.UCP_URL) && url.contains("mode=compose")) || url.contains(Api.TESTS_URL)) {
                    showBtnAdd(false);
                } else {
                    showBtnAdd(true);
                }


            } else {
                showBtnAdd(false);
            }

            //progressBar.setVisibility(View.GONE);
            //feedbackLayout.setVisibility(View.GONE);
            //swipe.setVisibility(View.VISIBLE);
        }
    }

    private boolean logged() {
        return this.presenter.logged();
    }

    public void openPageById(String id) {
        switch(id) {
            case "notifications":
                updateCountMessages();
                //openPage(Api.FORUM_NOTIFICATIONS);
                break;
            case "pms":
                openPage(Api.PM_URL);
                break;
            case "profile":
                long userId = presenter.getUser().getId();
                openPage(Api.PROFILE + "/member-" + userId + ".html");
                break;
            case "myPosts":
                openPage(Api.UCP_MY_POSTS_URL);
                break;
            case "myBookmarks":
                openPage(Api.UCP_MY_BOOKMARKS_URL);
                break;
            case "myErrorLog":
                openPage(Api.UCP_MY_ERROR_LOG_URL);
                break;
            case "settings":
                openPage(Api.UCP_FORUM_SETTINGS_URL);
                break;
            case "settingsNotifications":
                final Intent i = new Intent();
                i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                i.addCategory(Intent.CATEGORY_DEFAULT);
                i.setData(Uri.parse("package:" + getPackageName()));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(i);
            break;
            case "logout":
                this.presenter.logout();
                break;
            case "register":
                openPage(Api.FORUM_REGISTER_URL);
                break;
            case "signIn":
                ViewHelper.showLoginDialog(WebActivity.this);
                break;
            case "newPost":
                openPage(Api.FORUM_SEARCH_NEW_POSTS);
                break;
            case "newPm":
                openPage(Api.PM_URL);
                break;
            case "newChat":
                openPage(Api.CHAT_URL);
                break;
            case "newSchool":
                this.activeUrl = Api.FORUM_ADD_NEW_SCHOOL;
                openPage(Api.FORUM_ADD_NEW_SCHOOL);
                break;
            case "signInGoogle":
                Toast.makeText(this, "Sign In Google", Toast.LENGTH_LONG).show();
                break;
            case "signInFacebook":
                signInFacebook();
                break;

        }
    }

    public void logout() {
        startActivity(new Intent(WebActivity.this, AuthActivity.class));
        finish();
    }

    public void reload() {
        webView.reload();
        showMenu();
    }

    public void showSuccess(String type) {
        if(type.equals("login")) {
            if(ViewHelper.alertDialog != null) {
                ViewHelper.alertDialog.dismiss();
                ViewHelper.alertDialog = null;
                reload();
            }
        } else {
            ViewHelper.showSuccess(type);
        }
    }

    public void showError(AuthException exception) {
        ViewHelper.showError(exception);
    }

    public void signIn(String username, String password) {
        this.presenter.signIn(username, password);
    }

    public void signInFacebook() {
        ViewHelper.setLoadingIndicator(true);

        callbackManager = CallbackManager.Factory.create();

        if(LoginManager.getInstance() != null){
            LoginManager.getInstance().logOut();
        }
        
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        //LoginManager lm = LoginManager.getInstance();

                        AccessToken token = AccessToken.getCurrentAccessToken();

                        if(token != null) {
                            String idToken = token.getUserId();
                            String accessToken = token.getToken();

                            long expiresIn = token.getExpires().getTime();

                            callbackManager = null;

                            presenter.signIn("facebook", idToken, accessToken, Long.toString(expiresIn));
                        } else {
                            AuthException ex = new AuthException(new Exception("Failed sign in facebook"), "signInFacebook");
                            showError(ex);
                        }
                    }

                    @Override
                    public void onCancel() {
                        ViewHelper.setLoadingIndicator(false);
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        ViewHelper.setLoadingIndicator(false);
                        AuthException ex = new AuthException(new Exception(exception.getMessage()), "signInFacebook");
                        showError(ex);
                    }
                });

        LoginManager.getInstance().logInWithReadPermissions(WebActivity.this, Arrays.asList("public_profile"));
    }

    public void forgotPassword(String email) {
        presenter.forgotPassword(email);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                ViewHelper.showLeaveAppDialog(WebActivity.this);
            }
        }
    }

    public void showMenu() {
        if(this.presenter.logged()) {
            ((TextView) nv.findViewById(R.id.menu_forum)).setVisibility(View.VISIBLE);
            ((RelativeLayout) nv.findViewById(R.id.menu_pms)).setVisibility(View.VISIBLE);
            ((RelativeLayout) nv.findViewById(R.id.menu_notifications)).setVisibility(View.VISIBLE);
            ((TextView) nv.findViewById(R.id.menu_practice)).setVisibility(View.VISIBLE);
            ((TextView) nv.findViewById(R.id.menu_chat)).setVisibility(View.VISIBLE);
            ((TextView) nv.findViewById(R.id.menu_reviews)).setVisibility(View.VISIBLE);
            ((TextView) nv.findViewById(R.id.menu_deals_discounts)).setVisibility(View.VISIBLE);
            ((TextView) nv.findViewById(R.id.menu_advanced_search)).setVisibility(View.VISIBLE);
            ((TextView) nv.findViewById(R.id.menu_new_posts)).setVisibility(View.VISIBLE);
            ((TextView) nv.findViewById(R.id.menu_feedback)).setVisibility(View.VISIBLE);
            ((TextView) nv.findViewById(R.id.menu_unanswered)).setVisibility(View.VISIBLE);

            toogleAddMenu(false);

            btnAddLayout.setVisibility(View.VISIBLE);
            btnAdd.setVisibility(View.VISIBLE);
        } else {
            ((TextView) nv.findViewById(R.id.menu_forum)).setVisibility(View.VISIBLE);
            ((RelativeLayout) nv.findViewById(R.id.menu_pms)).setVisibility(View.GONE);
            ((RelativeLayout) nv.findViewById(R.id.menu_notifications)).setVisibility(View.GONE);
            ((TextView) nv.findViewById(R.id.menu_practice)).setVisibility(View.VISIBLE);
            ((TextView) nv.findViewById(R.id.menu_chat)).setVisibility(View.VISIBLE);
            ((TextView) nv.findViewById(R.id.menu_reviews)).setVisibility(View.VISIBLE);
            ((TextView) nv.findViewById(R.id.menu_deals_discounts)).setVisibility(View.VISIBLE);
            ((TextView) nv.findViewById(R.id.menu_advanced_search)).setVisibility(View.VISIBLE);
            ((TextView) nv.findViewById(R.id.menu_new_posts)).setVisibility(View.GONE);
            ((TextView) nv.findViewById(R.id.menu_feedback)).setVisibility(View.VISIBLE);
            ((TextView) nv.findViewById(R.id.menu_unanswered)).setVisibility(View.VISIBLE);

            btnAddLayout.setVisibility(View.GONE);
            btnAdd.setVisibility(View.GONE);

            toogleAddMenu(false);
        }
    }

    private void initNavigationView(final Toolbar toolbar) {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                if(mDrawerLayout.isDrawerOpen(drawerView)) {
                    changeIconHomeForNavigationMenu(R.color.mainOrange);
                } else {
                    changeIconHomeForNavigationMenu(R.color.white);
                }
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
    }

    public void goPreviousActivity() {
        if(logged()) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            startActivity(new Intent(WebActivity.this, AuthActivity.class));
            finish();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    public void updateCountMessages() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int notificationsUnwatchedCount  = presenter.getCountUnwatchedNotifications();
                int pmUnwatchedCount  = presenter.getCountUnwatchedPMs();

                if(notificationsUnwatchedCount > 0) {
                    countNotifications.setText(Integer.toString(notificationsUnwatchedCount));
                    countNotifications.setVisibility(View.VISIBLE);
                } else {
                    countNotifications.setText("");
                    countNotifications.setVisibility(View.GONE);
                }

                if(pmUnwatchedCount > 0) {
                    countPMs.setText(Integer.toString(pmUnwatchedCount));
                    countPMs.setVisibility(View.VISIBLE);
                } else {
                    countPMs.setText("");
                    countPMs.setVisibility(View.GONE);
                }
            }
        });
    }

    public class GCJavascriptInterface {
        Context mContext;

        GCJavascriptInterface(Context ctx) {
            this.mContext = ctx;
        }

        @JavascriptInterface
        public void sendMessage(final String message) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    if(message != null) {
                        presenter.saveNotifications(message);
                        /*try {
                            final JSONObject mNotify = new JSONObject(message);

                            if(!mNotify.isNull("action")) {
                                final String action = mNotify.getString("action");

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        switch(action) {
                                            case "showButtonPlus":
                                                showBtnAdd(true);
                                                break;
                                            case "hideButtonPlus":
                                                showBtnAdd(false);
                                                break;
                                        }
                                    }
                                });
                            } else {
                                presenter.saveNotifications(message);
                            }
                        } catch (JSONException e) {
                            Log.e(mContext.getClass().getName(), e.getMessage());
                        }*/
                    }
                }
            });
        }

        @JavascriptInterface
        public void notifications(final String data) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        final JSONObject mNotify = new JSONObject(data);

                        switch(mNotify.getString("action")) {
                            case "pageLoaded":
                                presenter.getNotifications();
                                break;
                            case "renderDone":
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        swipe.setRefreshing(false);
                                        setLoadingIndicator(false);

                                        if(presenter.getCountUnwatchedNotifications() > 0) {
                                            presenter.updateNotify();
                                        }
                                    }
                                });
                                break;
                            case "request":
                                String params = (mNotify.isNull("params")) ? null : mNotify.getString("params");
                                String idNotify = (mNotify.isNull("id")) ? null : mNotify.getString("id");

                                presenter.updateNotify(params, idNotify);
                                break;
                        }
                    } catch (JSONException exception) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                swipe.setRefreshing(false);
                                setLoadingIndicator(false);
                                Toast.makeText(WebActivity.this, "List of notifications are corrupt. Please go to the page later...", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });
        }

    }

    public void sendNotificationsForPage(final String notifications) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.evaluateJavascript(
                        "(function(){if(window.notifications && window.notifications.render) {window.notifications.render("+notifications+");}})()",
                        new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                //Toast.makeText(WebActivity.this, "Render notifications", Toast.LENGTH_LONG).show();
                            }
                        }
                );
            }
        });
    }

    private void changeIconHomeForNavigationMenu(int iconColor) {
        iconColor = ContextCompat.getColor(getApplicationContext(), iconColor);

        ActionBar ab =  getSupportActionBar();

        Drawable dr = ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_menu);

        Drawable wrapDrawable = DrawableCompat.wrap(dr);

        DrawableCompat.setTint(wrapDrawable, iconColor);

        ab.setHomeAsUpIndicator(wrapDrawable);
    }

    public void changeTitleColor(int color) {
        toolbarTitle.setTextColor(ContextCompat.getColor(getApplicationContext(), color));
    }

    public void changeProfileIconColor(int color) {
        toolbarProfile.setColorFilter(ContextCompat.getColor(getApplicationContext(), color), android.graphics.PorterDuff.Mode.MULTIPLY);
    }

    private void toogleAddMenu(boolean open) {
        if(open) {
            String url  = webView.getUrl();

            setEnabledBtnAddTopic(true);
            setEnabledBtnAddPm(true);
            setEnabledBtnAddChat(true);
            setEnabledBtnAddSchool(true);

            if(url != null) {
                if(url.contains(Api.FORUM_NEW_POSTS)) {
                    setEnabledBtnAddTopic(false);
                } else if(url.contains(Api.PM_NEW_URL)) {
                    setEnabledBtnAddPm(false);
                } else if(url.contains(Api.FORUM_URL + "/mchat.php")) {
                    setEnabledBtnAddChat(false);
                } else if(url.contains(Api.FORUM_ADD_NEW_SCHOOL)) {
                    setEnabledBtnAddSchool(false);
                }
            }

            //btnAdd.showMenuButton(true);
            //btnAdd.showMenu(true);

            //btnAdd.open(true);

            //btnAdd.showMenuButton(true);

           // btnAddTopic.show(true);
            //btnAdd.addMenuButton(btnAddTopic);
            //btnAdd.setAnimated(false);
            //btnAdd.open(false);
            //btnAdd.showMenu(false);
            btnAddLayout.getLayoutParams().height = mDrawerLayout.getHeight();
            btnAddLayout.getLayoutParams().width = mDrawerLayout.getWidth();
            btnAddLayout.requestLayout();

            btnAddLayout.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.background_gradient_white));

            showBtnAddAllItems();

            btnAdd.open(false);
            /*btnAdd.addMenuButton(btnAddTopic);
            btnAdd.addMenuButton(btnAddSchool);
            btnAdd.addMenuButton(btnAddChat);
            btnAdd.addMenuButton(btnAddPm);*/
            //btnAddTopic.showButtonInMenu(false);
            //btnAddTopic.setVisibility(View.GONE);
            //btnAddTopic.showButtonInMenu(true);
        } else {
            //hideBtnAddAllItems();

            //btnAdd.setAnimated(false);
            //btnAdd.close(false);
            hideBtnAddAllItems();

            btnAdd.close(false);

            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(!btnAdd.isOpened()) {
                        //hideBtnAddAllItems();
                    /*btnAdd.getChildAt(0).setVisibility(View.GONE);
                    btnAdd.getChildAt(1).setVisibility(View.GONE);
                    btnAdd.getChildAt(2).setVisibility(View.GONE);
                    btnAdd.getChildAt(3).setVisibility(View.GONE);*/

                        btnAddLayout.getLayoutParams().height = btnAdd.getHeight();
                        btnAddLayout.getLayoutParams().width = btnAdd.getWidth();
                        btnAddLayout.requestLayout();

                        btnAddLayout.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.transparent));
                        //btnAdd.requestLayout();
                    }

                    //Toast.makeText(WebActivity.this, "height:" + btnAdd.getHeight(), Toast.LENGTH_SHORT).show();
                    //Toast.makeText(WebActivity.this, "width:" + btnAdd.getWidth(), Toast.LENGTH_SHORT).show();
                }
            }, 0);
            //hideBtnAddAllItems();

            //btnAdd.getLayoutParams().height = btnAdd.getHeight();
            //btnAdd.getLayoutParams().width = btnAdd.getWidth();
            //btnAdd.removeAllMenuButtons();
            //btnAdd.removeAllMenuButtons();
            //btnAddTopic.showButtonInMenu(false);
            //btnAdd.hideMenu(true);
            //btnAdd.hideMenuButton(true);
            //hideBtnAddAllItems();
            //btnAdd.close(true);

            //hideBtnAddAllItems();

            //btnAdd.hideMenuButton(true);

            //btnAddTopic.showButtonInMenu(false);


            //hideBtnAddAllItems();

            /*ViewGroup.MarginLayoutParams p = (CoordinatorLayout.LayoutParams)    btnAddTopic.getLayoutParams();

            //p.setAnchorId(View.NO_ID);
            p.width = 0;
            p.height = 0;
            btnAddTopic.setLayoutParams(p);*/
            //btnAddTopic.hide(false);
            //btnAddTopic.setVisibility(View.GONE);

            /*ViewGroup.LayoutParams p = (CoordinatorLayout.LayoutParams) btnAddTopic.getLayoutParams();
            p.setAnchorId(View.NO_ID);
            btnAddTopic.setLayoutParams(p);
            btnAddTopic.setVisibility(View.GONE);*/

            //Toast.makeText(WebActivity.this, "height:" + btnAdd.getHeight(), Toast.LENGTH_SHORT).show();
            //Toast.makeText(WebActivity.this, "width:" + btnAdd.getWidth(), Toast.LENGTH_SHORT).show();

            //btnAdd.getLayoutParams().height = 20;
            //btnAdd.getLayoutParams().width = 20;
        }
    }

    public DeviceInfo getDeviceInformation(Activity activity) {
        //PackageInfo info = callActivity.getPackageManager().getPackageInfo(callActivity.getApplication().getPackageName(), 0);
        //info.versionName
        //info.versionCode+

        /* Device:*/
        String board = android.os.Build.BOARD;
        String brand = android.os.Build.BRAND;
        String device =  android.os.Build.DEVICE;
        String model = android.os.Build.MODEL;
        String product = android.os.Build.PRODUCT;
        String tags = android.os.Build.TAGS;

        // OS:
        String buildRelease =  android.os.Build.VERSION.RELEASE + ", Inc: '"+android.os.Build.VERSION.INCREMENTAL+"'";
        String displayBuild = android.os.Build.DISPLAY;
        String fingerPrint = android.os.Build.FINGERPRINT;
        String buildID = android.os.Build.ID;
        String time = String.valueOf(android.os.Build.TIME);
        String type = android.os.Build.TYPE;
        String user = android.os.Build.USER;

        //Density:
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        String density = String.valueOf(metrics.density);
        String densityDpi = String.valueOf(metrics.densityDpi);
        String scaledDensity = String.valueOf(metrics.scaledDensity);
        String xdpi = String.valueOf(metrics.xdpi);
        String ydpi = String.valueOf(metrics.ydpi);
        //Density reference:
        String densityDefault = String.valueOf(DisplayMetrics.DENSITY_DEFAULT);
        String densityLow = String.valueOf(DisplayMetrics.DENSITY_LOW);
        String densityMedium = String.valueOf(DisplayMetrics.DENSITY_MEDIUM);
        String densityHigh = String.valueOf(DisplayMetrics.DENSITY_HIGH);

        //"** Screen:");
        String heightPixels = String.valueOf(metrics.heightPixels);
        String widthPixels = String.valueOf(metrics.widthPixels);

        String heading = "RAM Information";
        long totalRamValue = totalRamMemorySize();
        long freeRamValue = freeRamMemorySize();
        long usedRamValue = totalRamValue - freeRamValue;
        String ram = "usedRam=" + formatSize(usedRamValue) + " MB, " +
                "freeRam=" + formatSize(freeRamValue) + " MB," +
                "totalRam=" +  formatSize(totalRamValue) + " MB";

        String internalMemoryTitle = "Internal Memory Information";
        long totalInternalValue = getTotalInternalMemorySize();
        long freeInternalValue = getAvailableInternalMemorySize();
        long usedInternalValue = totalInternalValue - freeInternalValue;
        String internalMemory = "usedInternal=" + formatSize(usedInternalValue) + " ," +
                "freeInternal=" + formatSize(freeInternalValue) + " ," +
                "totalInternal=" +  formatSize(totalInternalValue);

        String externalMemoryTitle = "External Memory Information";
        long totalExternalValue = getTotalExternalMemorySize();
        long freeExternalValue = getAvailableExternalMemorySize();
        long usedExternalValue = totalExternalValue - freeExternalValue;
        String externalMemory = "usedExternal=" + formatSize(usedExternalValue) + " , " +
                "freeExternal=" + formatSize(freeExternalValue) + " ," +
                "totalExternal=" +  formatSize(totalExternalValue);

        DeviceInfo di = new DeviceInfo();

        di.setBoard(board);
        di.setBrand(brand);
        di.setDevice(device);
        di.setModel(model);
        di.setProduct(product);
        di.setTags(tags);
        di.setBuildRelease(buildRelease);
        di.setDisplayBuild(displayBuild);
        di.setFingerPrint(fingerPrint);
        di.setBuildID(buildID);


        di.setTime(time);
        di.setType(type);
        di.setUser(user);
        di.setDensity(density);
        di.setDensityDpi(densityDpi);
        di.setScaledDensity(scaledDensity);
        di.setXdpi(xdpi);
        di.setYdpi(ydpi);
        di.setDensityDefault(densityDefault);
        di.setDensityLow(densityLow);
        di.setDensityMedium(densityMedium);
        di.setDensityHigh(densityHigh);
        di.setHeightPixels(heightPixels);
        di.setWidthPixels(widthPixels);
        di.setRam(ram);
        di.setInternalMemory(internalMemory);
        di.setExternalMemory(externalMemory);

        return di;
    }

    private long freeRamMemorySize() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        long availableMegs = mi.availMem / 1048576L;

        return availableMegs;
    }

    private long totalRamMemorySize() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        long availableMegs = mi.totalMem / 1048576L;
        return availableMegs;
    }

    public static boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    public static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return totalBlocks * blockSize;
    }

    public static long getAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return availableBlocks * blockSize;
        } else {
            return 0;
        }
    }

    public static long getTotalExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            return totalBlocks * blockSize;
        } else {
            return 0;
        }
    }

    public static String formatSize(long size) {
        String suffix = null;

        if (size >= 1024) {
            suffix = " KB";
            size /= 1024;
            if (size >= 1024) {
                suffix = " MB";
                size /= 1024;
            }
        }
        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }
        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }

    private void hideKeyboard() {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                hideSoftInputFromWindow((getWindow().getDecorView().getApplicationWindowToken()), 0);
    }

    private void showBtnAdd(boolean show) {
        if(show) {
            btnAddLayout.setVisibility(View.VISIBLE);
            btnAdd.setVisibility(View.VISIBLE);
        } else {
            btnAddLayout.setVisibility(View.GONE);
            btnAdd.setVisibility(View.GONE);
        }
    }

    private void showBtnAddTopic(boolean show) {
        if(show) {
            btnAddTopic.setVisibility(View.VISIBLE);
            btnAddTopic.setLabelVisibility(View.VISIBLE);
        } else {
            btnAddTopic.setVisibility(View.GONE);
            btnAddTopic.setLabelVisibility(View.GONE);
        }
    }

    private void showBtnAddPm(boolean show) {
        if(show) {
            btnAddPm.setVisibility(View.VISIBLE);
            btnAddPm.setLabelVisibility(View.VISIBLE);
        } else {
            btnAddPm.setVisibility(View.GONE);
            btnAddPm.setLabelVisibility(View.GONE);
        }
    }

    private void showBtnAddChat(boolean show) {
        if(show) {
            btnAddChat.setVisibility(View.VISIBLE);
            btnAddChat.setLabelVisibility(View.VISIBLE);
        } else {
            btnAddChat.setVisibility(View.GONE);
            btnAddChat.setLabelVisibility(View.GONE);
        }
    }

    private void showBtnAddSchool(boolean show) {
        if(show) {
            btnAddSchool.setVisibility(View.VISIBLE);
            btnAddSchool.setLabelVisibility(View.VISIBLE);
        } else {
            btnAddSchool.setVisibility(View.GONE);
            btnAddSchool.setLabelVisibility(View.GONE);
        }
    }

    private void setEnabledBtnAddTopic(boolean enable) {
        btnAddTopic.setEnabled(enable);
    }

    private void setEnabledBtnAddPm(boolean enable) {
        btnAddPm.setEnabled(enable);
    }

    private void setEnabledBtnAddChat(boolean enable) {
       btnAddChat.setEnabled(enable);
    }

    private void setEnabledBtnAddSchool(boolean enable) {
        btnAddSchool.setEnabled(enable);
    }

    private void showBtnAddAllItems () {
        showBtnAddTopic(true);
        showBtnAddPm(true);
        showBtnAddChat(true);
        showBtnAddSchool(true);
    }

    private void hideBtnAddAllItems () {
        showBtnAddTopic(false);
        showBtnAddPm(false);
        showBtnAddChat(false);
        showBtnAddSchool(false);
    }

    private void openFileDialog(ValueCallback<Uri>  uploadMsg) {
        Toast.makeText(getApplicationContext(), "openFile", Toast.LENGTH_LONG).show();
        mUploadMessage = uploadMsg;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "File Chooser"), FILECHOOSER_RESULTCODE);
    }

    private void openImageChooser() {
        try {
            /*if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                // Callback onRequestPermissionsResult interceptadona Activity MainActivity
                ActivityCompat.requestPermissions(WebActivity.this, new String[]{Manifest.permission.CAMERA}, PICK_FROM_CAMERA);
            } else {*/
                File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "FolderName");

                if (!imageStorageDir.exists()) {
                    imageStorageDir.mkdirs();
                }

                File file = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
                mCapturedImageURI = Uri.fromFile(file);

                final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);

                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");

                Intent chooserIntent = Intent.createChooser(i, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{/*captureIntent*/});

                startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
            //}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleUploadMessage(int requestCode, int resultCode, Intent intent) {
        Uri result = null;
        try {
            if (resultCode != RESULT_OK) {
                result = null;
            } else {
                // retrieve from the private variable if the intent is null

                result = intent == null ? mCapturedImageURI : intent.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mUploadMessage.onReceiveValue(result);
        mUploadMessage = null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void handleUploadMessages(int requestCode, int resultCode, Intent intent) {
        Uri[] results = null;

        try {
            if (resultCode != RESULT_OK) {
                results = null;
            } else {
                if (intent != null) {
                    String dataString = intent.getDataString();
                    ClipData clipData = intent.getClipData();

                    if (clipData != null) {
                        results = new Uri[clipData.getItemCount()];

                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            ClipData.Item item = clipData.getItemAt(i);
                            results[i] = item.getUri();
                        }
                    }

                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                } else {
                    results = new Uri[]{mCapturedImageURI};
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mUploadMessages.onReceiveValue(results);
        mUploadMessages = null;
    }

    public void openDeviceSettings() {
        startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), DEVICE_SETTINGS);
    }

    public void tryAgain() {
        webView.reload();
    }

    public static float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }
}