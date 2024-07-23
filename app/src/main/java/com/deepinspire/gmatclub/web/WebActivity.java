package com.deepinspire.gmatclub.web;

import static com.deepinspire.gmatclub.api.Api.CHAT_URL;
import static com.deepinspire.gmatclub.notifications.Notifications.INPUT_URL;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.StatFs;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.HttpAuthHandler;
import android.webkit.JavascriptInterface;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.deepinspire.gmatclub.BuildConfig;
import com.deepinspire.gmatclub.GCConfig;
import com.deepinspire.gmatclub.R;
import com.deepinspire.gmatclub.api.Api;
import com.deepinspire.gmatclub.api.AuthException;
import com.deepinspire.gmatclub.auth.AuthActivity;
import com.deepinspire.gmatclub.auth.LoginActivity;
import com.deepinspire.gmatclub.utils.BadgeDrawable;
import com.deepinspire.gmatclub.utils.CustomRatingDialog;
import com.deepinspire.gmatclub.utils.GCWebView;
import com.deepinspire.gmatclub.utils.Storage;
import com.deepinspire.gmatclub.utils.StringUtils;
import com.deepinspire.gmatclub.utils.ViewHelper;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.github.sealstudios.fab.FloatingActionButton;
import com.github.sealstudios.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebActivity extends AppCompatActivity implements
        IWebContract.View, SwipeRefreshLayout.OnRefreshListener, View.OnClickListener, View.OnTouchListener {

    private static final int FILECHOOSER_RESULTCODE = 100;
    InstallReferrerClient referrerClient;

    //  private static final int PICK_FROM_CAMERA = 101;

    private static final int DEVICE_SETTINGS = 102;
    public static final int REQUEST_CODE_FOR_DOWNLOAD = 121;
    public static String LATEST_URL = "latest url";

    private IWebContract.Presenter presenter;

    private boolean showQuizzesState = false;

    private int viewAdded = 0;

    private GCWebView webView;

    private ProgressBar progressView;

    private final Map<String, String> requestExtraHeaders = new HashMap<>();

    // private LinearLayout feedbackLayout;

    private SwipeRefreshLayout swipe;

    private DrawerLayout mDrawerLayout;

    //private ProgressBar progressBar;

    private View btnAddLayout;
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
    private TextView menuChatCount;

    // private CookieManager cookieManager;

    // private ActionBarDrawerToggle toggle;

    //  private BadgeDrawerToggle badgeToggle;

    private String activeUrl = null;

    private NavigationView nv;

    private int highlightedItemMenu = -1;

    private CallbackManager callbackManager;

    private ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener;

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    private final Handler mUiHandler = new Handler();

    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mUploadMessages;
    private Uri mCapturedImageURI = null;

    private boolean openedKeyboard = false;

    private ActionBar ab = null;
    View activityRootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPresenter(new WebPresenter(this.getApplicationContext(), this));

        setContentView(R.layout.activity_web);

        installReferrerClientAction();


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ab = getSupportActionBar();
        if (Storage.getBadgeCount(getApplicationContext()) > 0)
            ab.setHomeAsUpIndicator(setBadgeCount(this, R.drawable.ic_menu, Storage.getBadgeCount(getApplicationContext())));
        else ab.setHomeAsUpIndicator(setBadgeCount(this, R.drawable.ic_menu, 0));
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowTitleEnabled(false);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        /*badgeToggle = new BadgeDrawerToggle(this, mDrawerLayout,-1,-1);
        badgeToggle.setBadgeText("7");*/

        initNavigationView();

        // feedbackLayout = findViewById(R.id.feedbackLayout);

        progressView = findViewById(R.id.progressbar);

        swipe = findViewById(R.id.swipe);
        swipe.setOnRefreshListener(this);

        swipe.getViewTreeObserver()
                .addOnScrollChangedListener(mOnScrollChangedListener =
                        () -> {
                            if (webView != null && swipe != null)
                                swipe.setEnabled(webView.getScrollY() == 0);

                        });

        // progressBar = findViewById(R.id.loading);

        activityRootView = findViewById(R.id.top_parent);

        activityRootView.getViewTreeObserver()
                .addOnGlobalLayoutListener(() -> {
                    int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();

                    if (heightDiff > dpToPx(WebActivity.this, 200)) {
                        openedKeyboard = true;
                        showBtnAdd(false);
                    } else {
                        if (openedKeyboard && logged()) {
                            openedKeyboard = false;

                            String url = webView.getUrl();

                            showBtnAdd(url != null &&
                                    (!url.contains(Api.UCP_URL) || !url.contains("mode=compose")) &&
                                    !url.contains(Api.TESTS_URL));
                        }
                    }
                });


        btnAddLayout = findViewById(R.id.btnAddMenuLayout);

        btnAddLayout.setOnClickListener(this);

        btnAdd = findViewById(R.id.btnAddMenu);

        btnAdd.setAnimationDelayPerItem(0);

        btnAdd.setAnimated(false);

        btnAdd.setOnMenuButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toogleAddMenu(!btnAdd.isOpened());
            }
        });

        btnAddTopic = findViewById(R.id.btnAddTopic);
        btnAddTopic.setOnClickListener(this);
        //btnAddTopic.setVisibility(View.GONE);

        btnAddPm = findViewById(R.id.btnAddPm);
        btnAddPm.setOnClickListener(this);
        //btnAddPm.setVisibility(View.GONE);

        btnAddChat = findViewById(R.id.btnAddChat);
        btnAddChat.setOnClickListener(this);
        //btnAddChat.setVisibility(View.GONE);

        btnAddSchool = findViewById(R.id.btnAddSchool);
        btnAddSchool.setOnClickListener(this);
        //btnAddSchool.setVisibility(View.GONE);

        toolbarTitleLayout = findViewById(R.id.toolbarTitleLayout);

        toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitleLayout = findViewById(R.id.toolbarTitleLayout);
        toolbarTitleLayout.setOnClickListener(this);

        toolbarShare = findViewById(R.id.toolbarShare);
        toolbarShare.setOnClickListener(this);

        toolbarSearch = findViewById(R.id.toolbarSearch);
        toolbarSearch.setOnClickListener(this);

        toolbarProfile = findViewById(R.id.toolbarProfile);
        toolbarProfile.setOnClickListener(this);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        searchView = findViewById(R.id.toolbarSearchView);//.getActionView();
        if (searchManager != null)
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
                    openPage(Api.FORUM_SEARCH + encodedStringBuilder.toString());
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

        nv = findViewById(R.id.nav_view);

        countNotifications = nv.findViewById(R.id.menu_notifications_count);
        countPMs = nv.findViewById(R.id.menu_pms_count);
        menuChatCount = nv.findViewById(R.id.menu_chat_count);

        countNotifications.setVisibility(View.GONE);
        countPMs.setVisibility(View.GONE);

        showMenu();

        if (savedInstanceState == null) {
            initWebView();
        }

        initRequestExtraHeaders();

        Intent intent = getIntent();

        if (intent != null) {
            onNewIntent(intent);
        }

        initLeaveFeedbackAction();
    }

    private Drawable setBadgeCount(Context context, int res, int badgeCount) {
        LayerDrawable icon = (LayerDrawable) ContextCompat.getDrawable(context, R.drawable.ic_badge_drawable);
        Drawable mainIcon = ContextCompat.getDrawable(context, res);
        BadgeDrawable badge = new BadgeDrawable(context);
        badge.setCount(String.valueOf(badgeCount));
        if (icon != null) {
            icon.mutate();
            icon.setDrawableByLayerId(R.id.ic_badge, badge);
            icon.setDrawableByLayerId(R.id.ic_main_icon, mainIcon);
        }

        return icon;
    }

    private void initLeaveFeedbackAction() {
        CustomRatingDialog.Config config = new CustomRatingDialog.Config(2, 10);
        config.setTitle(R.string.leave_feedback_tittle_text);
        config.setMessage(R.string.leave_feedback_message_text);
        config.setYesButtonText(R.string.leave_feedback_yes_btn_text);
        config.setNoButtonText(R.string.leave_feedback_no_btn_text);
        config.setCancelButtonText(R.string.leave_feedback_cancel_btn_text);
        //CustomRatingDialog.showRateDialog(this);
        CustomRatingDialog.onCreate(this);
        CustomRatingDialog.init(config);
        CustomRatingDialog.showRateDialogIfNeeded(this);
        CustomRatingDialog.setCallback(new CustomRatingDialog.Callback() {
            @Override
            public void onYesClicked() {

            }

            @Override
            public void onNoClicked() {

            }

            @Override
            public void onCancelClicked() {

            }

            @Override
            public void onLeaveFeedbackClicked() {
                sendFeedBackWithEmail();
            }
        });
    }

    private void sendFeedBackWithEmail() {
        mDrawerLayout.closeDrawers();

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));

        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.email_support)});
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_feedback));
        intent.putExtra(Intent.EXTRA_TEXT, "body of email");

        try {
            startActivityForResult(Intent.createChooser(intent, "Send mail..."), GCConfig.EMAIL);
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(getApplicationContext(), "There are no email clients installed.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // if (mConnectivityChangeReceiver != null) unregisterReceiver(mConnectivityChangeReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (webView == null)
            initWebView();
        presenter.getChatNotifications(this);
        /*NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int count = notificationManager.getActiveNotifications().length;
            //Storage.saveBadgeCount(getApplicationContext(), count);
        }*/
        if (Storage.getBadgeCount(getApplicationContext()) > 0)
            ab.setHomeAsUpIndicator(setBadgeCount(this, R.drawable.ic_menu, Storage.getBadgeCount(getApplicationContext())));
        else ab.setHomeAsUpIndicator(setBadgeCount(this, R.drawable.ic_menu, 0));
        /*IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(mConnectivityChangeReceiver, intentFilter);*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        swipe.getViewTreeObserver().removeOnScrollChangedListener(mOnScrollChangedListener);
        referrerClient.endConnection();
    }

    private void highlightMenuItemOnClick(int id) {
        if (highlightedItemMenu == -1) {
            highlightOnClick(id, true);
        } else {
            highlightOnClick(highlightedItemMenu, false);
            highlightOnClick(id, true);
        }

        highlightedItemMenu = id;
    }

    private void highlightOnClick(int id, boolean activate) {
        int backgroundColor = activate ? R.color.main : R.color.mainBackgroundNavigationView;
        int textColor = activate ? R.color.white : R.color.main;

        switch (id) {
            case R.id.menu_pms:
                nv.findViewById(id).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), backgroundColor));
                ((TextView) nv.findViewById(R.id.menu_pms_text)).setTextColor(ContextCompat.getColor(getApplicationContext(), textColor));
                ((TextView) nv.findViewById(R.id.menu_pms_count)).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                break;
            case R.id.menu_chat_container:
                nv.findViewById(R.id.menu_chat_container).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), backgroundColor));
                ((TextView) nv.findViewById(R.id.menu_chat)).setTextColor(ContextCompat.getColor(getApplicationContext(), textColor));
                ((TextView) nv.findViewById(R.id.menu_chat_count)).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                break;
            case R.id.menu_notifications:
                nv.findViewById(id).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), backgroundColor));
                ((TextView) nv.findViewById(R.id.menu_notifications_text)).setTextColor(ContextCompat.getColor(getApplicationContext(), textColor));
                ((TextView) nv.findViewById(R.id.menu_notifications_count)).setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                break;
            default: {
                nv.findViewById(id).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), backgroundColor));
                ((TextView) nv.findViewById(id)).setTextColor(ContextCompat.getColor(getApplicationContext(), textColor));
            }
        }
    }

    /*
        private void highlightOnTouchDown(int id) {
            int backgroundColor = R.color.mainHighlightTouchNavigationView;
            int textColor = R.color.main;

            switch (id) {
                case R.id.menu_pms:
                    nv.findViewById(id).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), backgroundColor));
                    ((TextView) nv.findViewById(R.id.menu_pms_count)).setTextColor(ContextCompat.getColor(getApplicationContext(), textColor));
                    break;
                case R.id.menu_notifications:
                    nv.findViewById(id).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), backgroundColor));
                    ((TextView) nv.findViewById(R.id.menu_notifications_count)).setTextColor(ContextCompat.getColor(getApplicationContext(), textColor));
                    break;
                default: {
                    nv.findViewById(id).setBackgroundColor(ContextCompat.getColor(getApplicationContext(), backgroundColor));
                    ((TextView) nv.findViewById(id)).setTextColor(ContextCompat.getColor(getApplicationContext(), textColor));
                }
            }
        }
    */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toolbarShare:
                String url = webView.getUrl();

                if (url != null) {
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
                FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this, new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String token) {
                        StringUtils.copyToClipboard(WebActivity.this, token);
                    }
                });

                if (logged()) {
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
                openPage(CHAT_URL);
                break;
            case R.id.btnAddSchool:
                toogleAddMenu(false);
                this.activeUrl = Api.FORUM_ADD_NEW_SCHOOL;
                openPage(Api.FORUM_ADD_NEW_SCHOOL);
                break;
            case R.id.btnAddMenuLayout:
                if (btnAdd.isOpened()) {
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
        if (item.getItemId() == android.R.id.home) {
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

        if (presenter.checkAccessNetwork(this)) {
            destroyOfflineAlertDialog();
            if (webView != null) {
                String url = webView.getUrl();
                if (url != null) {
                    if (url.contains(Api.FORUM_URL + "/mchat.php"))
                        swipe.setEnabled(false);
                    else {
                        swipe.setEnabled(true);
                        webView.loadUrl(webView.getUrl(), getRequestExtraHeaders());
                    }
                } else {
                    Intent intent = getIntent();
                    onNewIntent(intent);
                }
            }

        } else {
            ViewHelper.showOfflineDialog(WebActivity.this);
            swipe.setRefreshing(false);
        }
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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (webView != null) {
            outState.putString(LATEST_URL, webView.getUrl());
            // Save WebView state to a file
            saveWebViewStateToFile();
        }
    }

    private void saveWebViewStateToFile() {
        try {
            FileOutputStream fos = openFileOutput("webviewState.dat", Context.MODE_PRIVATE);
            Bundle webViewBundle = new Bundle();
            webView.saveState(webViewBundle);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(webViewBundle);
            oos.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (webView != null) {
            String latestUrl = savedInstanceState.getString(LATEST_URL);
            if (latestUrl != null) {
                webView.loadUrl(latestUrl);
            }
            // Restore WebView state from a file
            restoreWebViewStateFromFile();
        } else {
            initWebView();
            String latestUrl = savedInstanceState.getString(LATEST_URL);
            if (latestUrl != null && !LATEST_URL.equals(Api.PRACTICE_URL)) {
                webView.loadUrl(latestUrl);
            }
        }
    }

    private void restoreWebViewStateFromFile() {
        try {
            FileInputStream fis = openFileInput("webviewState.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);
            Bundle webViewBundle = (Bundle) ois.readObject();
            webView.restoreState(webViewBundle);
            ois.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();

        if (view != null) {
            if ((ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) && view instanceof EditText && !view.getClass().getName().startsWith("android.webkit.")) {
                int[] scrcoords = new int[2];

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

            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    switch (view.getId()) {
                        case R.id.menu_forum:
                        case R.id.menu_pms:
                        case R.id.menu_notifications:
                        case R.id.menu_practice:
                        case R.id.menu_decision_tracker:
                        case R.id.menu_mba_discussions:
                        case R.id.menu_chat_container:
                        case R.id.menu_reviews:
                        case R.id.menu_deals_discounts:
                        case R.id.menu_advanced_search:
                        case R.id.menu_feedback:
                            break;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }

        }

        return super.dispatchTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return true;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FacebookSdk.getCallbackRequestCodeOffset()) {
            if (callbackManager != null) {
                callbackManager.onActivityResult(requestCode, resultCode, data);
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE) {

            if (null == mUploadMessage && null == mUploadMessages) {
                return;
            }

            if (null != mUploadMessage) {
                handleUploadMessage(requestCode, resultCode, data);

            } else if (mUploadMessages != null) {
                handleUploadMessages(requestCode, resultCode, data);
            }
        } else {
            switch (requestCode) {
                case GCConfig.GOOGLE_SIGN_IN:
                case GCConfig.EMAIL:
                    break;
                case DEVICE_SETTINGS:
                    tryAgain();
                    break;
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void downloadFileAction() {

        webView.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    //Do this, if permission granted
                    downloadFile(url, userAgent, contentDisposition, mimeType);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_NOT_EXPORTED);
                    } else {
                        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                    }

                } else {
                    //Do this, if there is no permission
                    ActivityCompat.requestPermissions(WebActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE_FOR_DOWNLOAD
                    );

                }
            }
        });
    }

    /*Download attachment functionality*/

    private void downloadFile(String url, String userAgent, String contentDisposition, String mimeType) {
        String downloadFileName = URLUtil.guessFileName(url, contentDisposition, mimeType);
        String[] fileNameList = contentDisposition.split("UTF-8''");
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.allowScanningByMediaScanner();
        if (fileNameList.length > 1) {
            String mimeTypeMap = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(fileNameList[1]));
            if (mimeTypeMap != null)
                request.setMimeType(mimeTypeMap);
            else {
                if (fileNameList[1].contains(".xls"))
                    request.setMimeType("application/vnd.ms-excel");
                else if (fileNameList[1].contains(".xlsm"))
                    request.setMimeType("application/vnd.ms-excel.sheet.macroEnabled.12");
                else if (fileNameList[1].contains(".pptx"))
                    request.setMimeType("application/vnd.openxmlformats-officedocument.presentationml.presentation");
                else if (fileNameList[1].contains(".xlsx"))
                    request.setMimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                else if (fileNameList[1].contains(".zip"))
                    request.setMimeType("application/zip");
                else if (fileNameList[1].contains(".rar"))
                    request.setMimeType("application/x-rar-compressed");
                else if (fileNameList[1].contains(".vsd"))
                    request.setMimeType("application/vnd.visio");
                else if (fileNameList[1].contains(".exe"))
                    request.setMimeType("application/octet-stream");
            }
        }
        String cookies = CookieManager.getInstance().getCookie(url);
        request.addRequestHeader("cookie", cookies);
        request.addRequestHeader("User-Agent", userAgent);
        request.setDescription("Downloading File...");
        Environment.getExternalStorageDirectory();
        if (fileNameList.length > 1)
            request.setTitle(fileNameList[1]);
        else
            request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        if (fileNameList.length > 1) {
            downloadFileName = fileNameList[1];
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, downloadFileName);
        } else
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, downloadFileName);

        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        if (dm != null) {
            dm.enqueue(request);
            Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_LONG).show();
        }
    }

    /*Open file after successful downloading*/

    private BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            //openFile(downloadFileName);
        }
    };

    /*
        protected void openFile(String fileName) {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + File.separator +
                    fileName);
            //Uri path = Uri.fromFile(file);
            Uri path = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", file);
            Intent pdfOpenintent = new Intent(Intent.ACTION_VIEW);
            pdfOpenintent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pdfOpenintent.setDataAndType(path, "application/pdf");
            try {
                this.startActivity(pdfOpenintent);
            } catch (ActivityNotFoundException e) {
            }
        }
    */
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        try {
            Uri uri = intent.getData();

            String url;

            if (uri != null) {
                url = uri.toString();
            } else {
                url = intent.getStringExtra("url");
            }

            if (intent.getStringExtra(INPUT_URL) != null)
                url = intent.getStringExtra(INPUT_URL);

            if (url.equals(Api.FORUM_URL)) {
                highlightMenuItemOnClick(R.id.menu_forum);
            } else if (url.equals(CHAT_URL)) {
                highlightMenuItemOnClick(R.id.menu_chat_container);
            } else if (url.equals(Api.PRACTICE_URL)) {
                highlightMenuItemOnClick(R.id.menu_practice);
            } else if (url.equals(Api.PM_NEW_URL)) {
                highlightMenuItemOnClick(R.id.menu_pms);
            } else if (url.equals(Api.DECISION_TRACKER)) {
                highlightMenuItemOnClick(R.id.menu_decision_tracker);
            } else if (url.equals(Api.MBA_DISCUSSIONS)) {
                highlightMenuItemOnClick(R.id.menu_mba_discussions);
            } else if (url.equals(Api.DEALS_URL)) {
                highlightMenuItemOnClick(R.id.menu_deals_discounts);
            }

            openPage(url);
        } catch (NullPointerException e) {
        }
    }

    public void setPresenter(IWebContract.Presenter presenter) {
        this.presenter = presenter;
    }

    public void goByMenu(View view) {
        switch (view.getId()) {
            case R.id.menu_forum:
                showQuizzesState = false;
                openPageFromHamburgerMenu(R.id.menu_forum, Api.FORUM_URL);
                break;
            case R.id.menu_pms:
                showQuizzesState = false;
                openPageFromHamburgerMenu(R.id.menu_pms, Api.PM_URL);
                break;
            case R.id.menu_notifications:
                showQuizzesState = false;
                //Storage.saveBadgeCount(getApplicationContext(), 0);
                openPageFromHamburgerMenu(R.id.menu_notifications, Api.FORUM_NOTIFICATIONS/*"file:///android_asset/notifications.html"*/);
                break;
            case R.id.menu_practice:
                showQuizzesState = true;
                //initPracticeView();
                openPageFromHamburgerMenu(R.id.menu_practice, Api.PRACTICE_URL);
                break;
            case R.id.menu_decision_tracker:
                showQuizzesState = false;
                openPageFromHamburgerMenu(R.id.menu_decision_tracker, Api.DECISION_TRACKER);
                break;
            case R.id.menu_mba_discussions:
                showQuizzesState = false;
                openPageFromHamburgerMenu(R.id.menu_mba_discussions, Api.MBA_DISCUSSIONS);
                break;
            case R.id.menu_chat_container:
                showQuizzesState = false;
                openPageFromHamburgerMenu(R.id.menu_chat_container, CHAT_URL);
                break;
            case R.id.menu_reviews:
                showQuizzesState = false;
                openPageFromHamburgerMenu(R.id.menu_reviews, Api.REVIEWS_URL);
                break;
            case R.id.menu_deals_discounts:
                showQuizzesState = false;
                openPageFromHamburgerMenu(R.id.menu_deals_discounts, Api.DEALS_URL);
                break;
            case R.id.menu_advanced_search:
                showQuizzesState = false;
                openPageFromHamburgerMenu(R.id.menu_advanced_search, Api.FORUM_SEARCH);
                break;
            case R.id.menu_feedback:
                if ("greprepclub".equals(BuildConfig.FLAVOR)) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri data = Uri.parse("mailto:support@greprepclub.com?subject=" + getResources().getString(R.string.app_feedback));
                    intent.setData(data);
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(this, "Email client not found", Toast.LENGTH_LONG).show();
                    }
                    break;
                }
                showQuizzesState = false;
                openPageFromHamburgerMenu(R.id.menu_feedback, Api.LEAVE_FEEDBACK);
                /*mDrawerLayout.closeDrawers();

                Intent intent = new Intent(Intent.ACTION_SEND);

                intent.setType("message/rfc822");

                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@gmatclub.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "GMAT Club Android Application Feedback");
                intent.putExtra(Intent.EXTRA_TEXT, "body of email");

                try {
                    startActivityForResult(Intent.createChooser(intent, "Send mail..."), GCConfig.EMAIL);
                } catch (ActivityNotFoundException exception) {
                    Toast.makeText(getApplicationContext(), "There are no email clients installed.", Toast.LENGTH_LONG).show();
                }*/
                break;
        }

        if (!showQuizzesState) {
            View v = webView.findViewById(R.id.quizzes_container);
            if (v != null)
                v.setVisibility(View.GONE);
        } else {

            new Handler().postDelayed(() -> {
                View v = webView.findViewById(R.id.quizzes_container);
                if (v != null)
                    v.setVisibility(View.VISIBLE);
            }, 700);
        }
    }


    @SuppressLint({"AddJavascriptInterface", "SetJavaScriptEnabled"})
    private void initWebView() {
        webView = findViewById(R.id.webView);

        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDatabaseEnabled(true);
        //settings.setDatabasePath("/data/data/" + webView.getContext().getPackageName() + "/databases/");
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setLoadWithOverviewMode(true);

        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }
        downloadFileAction();

        settings.setAllowFileAccessFromFileURLs(true); //Maybe you don't need this rule
        settings.setAllowUniversalAccessFromFileURLs(true);

        String device = "Android" + " " + getString(R.string.app_name) + "/";

       /* try {
            device += "/" + getPackageManager().getPackageInfo(getApplication().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
*/
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

        presenter.getNotifications(this);
        webView.addJavascriptInterface(new GCJavascriptInterface(this), "GCAndroid");
        webView.addJavascriptInterface(new AjaxHandler(this), "ajaxHandler");
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                //Toast.makeText(getApplicationContext(), consoleMessage.message(), Toast.LENGTH_LONG).show();
                Log.d("MyApplication", consoleMessage.message() + " -- From line "
                        + consoleMessage.lineNumber() + " of "
                        + consoleMessage.sourceId());
                return super.onConsoleMessage(consoleMessage);
            }

            @Override
            public void onProgressChanged(WebView view, int progress) {
                progressView.setProgress(progress);
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

            void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                mUploadMessage = uploadMsg;
                openImageChooser();
            }

            // For Lollipop 5.0+ Devices

            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                mUploadMessages = filePathCallback;
                openImageChooser();
                return true;
            }

            // openFileChooser for Android < 3.0

            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
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
                /*if(url.contains("google-analytics.com")) {
                    Uri uri = Uri.parse(url);
                    String cd8 = uri.getQueryParameter("cd8");

                    Toast.makeText(WebActivity.this, cd8, Toast.LENGTH_LONG).show();
                }*/
            }

            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                onPageStartedAction(url);
                Log.e("onPageStarted", "onPageStarted() called");
            }

            public void onPageFinished(WebView view, String url) {
                Log.e("onPageFinished", "onPageFinished() called");
                onPageFinishedAction(url);
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

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Log.d("WEBVIEW", "REQ: " + request.getUrl().toString());
                WebResourceResponse resp = super.shouldInterceptRequest(view, request);
                if (resp == null) {
                    Log.d("WEBVIEW", "RESP:EMPTY");
                } else
                    Log.d("WEBVIEW", "RESP: " + resp.toString());
                return resp;
            }

            @Override
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                //view.loadUrl(request.getUrl().toString(), getRequestExtraHeaders());
                return analyseUrl(view, request.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //view.loadUrl(url, getRequestExtraHeaders());
                return analyseUrl(view, url);
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (errorCode == -2) {
                    presenter.setError(WebPresenter.ERROR_CONNECT);
                }
                //presenter.setError(errorCode);
                //Toast.makeText(WebActivity.this, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }

            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                int errorCode = rerr.getErrorCode();

                if (errorCode == -2) {
                    presenter.setError(WebPresenter.ERROR_CONNECT);
                }
                //int error = rerr.getErrorCode();
                //-2
                //net::ERR_ADDRESS_UNREACHABLE
                // Redirect to deprecated method, so you can use it in all SDK versions
                onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
            }

            private boolean analyseUrl(WebView view, String url) {
                try {
                    if (url != null && !(url.startsWith(Api.HOME_URL) || url.startsWith(Api.HOME_URL.replace("https", "http")))) {
                        view.getContext().startActivity(
                                new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                        return true;
                    } else {
                        return false;
                    }
                } catch (Exception e) {
                    return false;
                }
            }
        });
    }

    private void onPageFinishedAction(String url) {
        Log.e("onPageFinishedAction", "onPageFinishedAction() called with: url = [" + url + "]");
        if (isFinishing() || isDestroyed())
            return;
        if (presenter.checkAccessNetwork(this)) {
            /*if (webView.getUrl().equals(OLD_CHAT_LINK))
                swipe.setEnabled(false);
            else swipe.setEnabled(true);*/

            destroyOfflineAlertDialog();
            progressView.setVisibility(View.GONE);

            if (activeUrl != null && url.contains(activeUrl)) {
                activeUrl = null;

                webView.evaluateJavascript(
                        "(function(){document.querySelector('#statusesDecTracker .addMyInfo.btn').click();})()",
                        value -> {
                            swipe.setRefreshing(false);
                            setLoadingIndicator(false);
                        }
                );

                webView.stopLoading();
            } else if (url.contains(Api.HOME_URL)) {
                presenter.logged(this);
                swipe.setRefreshing(false);
                setLoadingIndicator(false);

                if (url.contains(Api.QUIZ) && url.contains("s=")) {
                    swipe.setEnabled(false);
                }

            } else if (url.contains(Api.PM_URL)) {
                presenter.updatePMs();
                updateCountMessages();
            }
        } else {
            ViewHelper.showOfflineDialog(WebActivity.this);
            swipe.setRefreshing(false);
            setLoadingIndicator(false);
        }

    }

    private void onPageStartedAction(String url) {
        if (presenter.checkAccessNetwork(this)) {
            destroyOfflineAlertDialog();
            progressView.setVisibility(View.VISIBLE);
            //getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            //toolbar.setVisibility(View.GONE);
            //WebActivity.this.getSupportActionBar().set
            //https://gmatclub.org/tests-beta/test/welcome.html?id=1121737
            //https://gmatclub.org/tests-beta/test-1065456.html
            //https://gmatclub.org/tests-beta/test/endExam.html?id=1065456
            //Toast.makeText(WebActivity.this, url, Toast.LENGTH_LONG).show();
            //webView.destroyDrawingCache();
            if (url.equals(Api.FORUM_URL + "?style=1Федьковича 60А2") || url.equals(Api.FORUM_URL + "/?style=12")) {
                changeTitleColor(R.color.white);
                changeProfileIconColor(R.color.mainOrange);
            } else if (url.contains(Api.PROFILE)) {
                changeTitleColor(R.color.mainOrange);
                changeProfileIconColor(R.color.white);
            } else {
                changeTitleColor(R.color.mainOrange);
                changeProfileIconColor(R.color.mainOrange);
            }

            swipe.setRefreshing(false);
            setLoadingIndicator(true);

            updateVisibilityToolbar(url);
        } else {
            ViewHelper.showOfflineDialog(WebActivity.this);
            swipe.setRefreshing(false);
        }
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

            String username = "";
            String password = "";

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
        String device = "Android" +  /*+ Build.MODEL + " " + Build.VERSION.RELEASE +*/ " " + getString(R.string.app_name) + "/";// + getString(R.string.app_name);

       /* try {
            device += "/" + getPackageManager().getPackageInfo(getApplication().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
*/
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
        swipe.setEnabled(!url.equals(CHAT_URL));

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }

        if (webView != null) {
            webView.loadUrl("javascript:document.open();document.close();");
            //webView.loadUrl("about:blank");
            webView.loadUrl(url, getRequestExtraHeaders());
        }
    }

    public void setLoadingIndicator(boolean active) {
        if (active) {
            //progressBar.setVisibility(View.VISIBLE);
            //feedbackLayout.setVisibility(View.GONE);
            //swipe.setVisibility(View.GONE);
            showBtnAdd(false);
        } else {
            hideKeyboard();

            if (logged()) {
                toogleAddMenu(false);

                String url = webView.getUrl();

                showBtnAdd(url != null && (!url.contains(Api.UCP_URL) || !url.contains("mode=compose")) && !url.contains(Api.TESTS_URL));


            } else {
                showBtnAdd(false);
            }

            //progressBar.setVisibility(View.GONE);
            //feedbackLayout.setVisibility(View.GONE);
            //swipe.setVisibility(View.VISIBLE);
        }
    }

    private boolean logged() {
        return this.presenter.logged(this);
    }

    public void openPageById(String id) {
        switch (id) {
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
                this.presenter.logout(this);
                break;
            case "register":
                openPage(Api.FORUM_REGISTER_URL);
                break;
            case "signIn":
                if (presenter.availableAuth()) {
                    openLoginPage();
                } else {
                    ViewHelper.showForgotPasswordDialog(WebActivity.this);
                }
                break;
            case "newPost":
                //openPage(Api.FORUM_SEARCH_NEW_POSTS);
                break;
            case "newPm":
                openPage(Api.PM_URL);
                break;
            case "newChat":
                openPage(CHAT_URL);
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
        Storage.saveGoogleIdToken(getApplicationContext(), "");
        Storage.saveGoogleAccessToken(getApplicationContext(), "");
        Storage.saveFacebookIdToken(getApplicationContext(), "");
        Storage.saveFacebookAccessToken(getApplicationContext(), "");
        Storage.saveLoginEmail(getApplicationContext(), "");
        Storage.saveLoginPassword(getApplicationContext(), "");
        startActivity(new Intent(WebActivity.this, AuthActivity.class));
        finish();
    }

    public void reload() {
        webView.reload();
        showMenu();
    }

    public void showSuccess(String type) {
        if (type.equals("login")) {
            if (ViewHelper.alertDialog != null) {
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
        this.presenter.signIn(this, username, password);
    }

    public void signInFacebook() {
        ViewHelper.setLoadingIndicator(true);

        callbackManager = CallbackManager.Factory.create();

        if (LoginManager.getInstance() != null) {
            LoginManager.getInstance().logOut();
        }

        Objects.requireNonNull(LoginManager.getInstance()).registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        //LoginManager lm = LoginManager.getInstance();

                        AccessToken token = AccessToken.getCurrentAccessToken();

                        if (token != null) {
                            String idToken = token.getUserId();
                            String accessToken = token.getToken();

                            long expiresIn = token.getExpires().getTime();

                            callbackManager = null;

                            presenter.signIn(WebActivity.this, "facebook", idToken, accessToken, Long.toString(expiresIn));
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
            if (webView != null)
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    ViewHelper.showLeaveAppDialog(WebActivity.this);
                }
        }
    }

    public void showMenu() {
        if (this.presenter.logged(this)) {
            nv.findViewById(R.id.menu_forum).setVisibility(View.VISIBLE);
            nv.findViewById(R.id.menu_pms).setVisibility(View.VISIBLE);
            nv.findViewById(R.id.menu_notifications).setVisibility(View.VISIBLE);
            nv.findViewById(R.id.menu_practice).setVisibility(View.VISIBLE);
            nv.findViewById(R.id.menu_chat_container).setVisibility(View.VISIBLE);
            nv.findViewById(R.id.menu_reviews).setVisibility(View.VISIBLE);
            nv.findViewById(R.id.menu_deals_discounts).setVisibility(View.VISIBLE);
            nv.findViewById(R.id.menu_advanced_search).setVisibility(View.VISIBLE);
            nv.findViewById(R.id.menu_decision_tracker).setVisibility(View.VISIBLE);
            nv.findViewById(R.id.menu_feedback).setVisibility(View.VISIBLE);
            nv.findViewById(R.id.menu_mba_discussions).setVisibility(View.VISIBLE);

            toogleAddMenu(false);

            btnAddLayout.setVisibility(View.VISIBLE);
            btnAdd.setVisibility(View.VISIBLE);
        } else {
            nv.findViewById(R.id.menu_forum).setVisibility(View.VISIBLE);
            nv.findViewById(R.id.menu_pms).setVisibility(View.GONE);
            nv.findViewById(R.id.menu_notifications).setVisibility(View.GONE);
            nv.findViewById(R.id.menu_practice).setVisibility(View.VISIBLE);
            nv.findViewById(R.id.menu_chat_container).setVisibility(View.VISIBLE);
            nv.findViewById(R.id.menu_reviews).setVisibility(View.VISIBLE);
            nv.findViewById(R.id.menu_deals_discounts).setVisibility(View.VISIBLE);
            nv.findViewById(R.id.menu_advanced_search).setVisibility(View.VISIBLE);
            nv.findViewById(R.id.menu_decision_tracker).setVisibility(View.VISIBLE);
            nv.findViewById(R.id.menu_feedback).setVisibility(View.VISIBLE);
            nv.findViewById(R.id.menu_mba_discussions).setVisibility(View.VISIBLE);

            btnAddLayout.setVisibility(View.GONE);
            btnAdd.setVisibility(View.GONE);

            toogleAddMenu(false);
        }
    }

    private void initNavigationView() {
        mDrawerLayout = findViewById(R.id.drawer_layout);

        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                if (mDrawerLayout.isDrawerOpen(drawerView)) {
                    changeIconHomeForNavigationMenu(R.color.mainOrange);
                } else {
                    changeIconHomeForNavigationMenu(R.color.white);
                    //Storage.saveBadgeCount(getApplicationContext(),0);
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
        if (logged()) {
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
        runOnUiThread(() -> {
            //  int notificationsUnwatchedCount = presenter.getCountUnwatchedNotifications();
            int pmUnwatchedCount = presenter.getCountUnwatchedPMs();

            /*if (notificationsUnwatchedCount > 0) {
                countNotifications.setText(Integer.toString(notificationsUnwatchedCount));
                countNotifications.setVisibility(View.VISIBLE);
                ShortcutBadger.applyCount(WebActivity.this, notificationsUnwatchedCount);
            } else {
                countNotifications.setText("");
                countNotifications.setVisibility(View.GONE);
                ShortcutBadger.removeCount(WebActivity.this);
            }*/

            if (pmUnwatchedCount > 0) {
                countPMs.setText(String.valueOf(pmUnwatchedCount));
                countPMs.setVisibility(View.VISIBLE);
            } else {
                countPMs.setText("");
                countPMs.setVisibility(View.GONE);
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
            executor.submit(() -> {
                if (message != null) {
                    try {
                        final JSONObject mNotify = new JSONObject(message);

                        if (!mNotify.isNull("action")) {
                            final String action = mNotify.getString("action");

                            runOnUiThread(() -> {
                                if ("showLoginPage".equals(action)) {
                                    openLoginPage();
                                }
                            });
                        } else {
                            presenter.saveNotifications(message);
                        }
                    } catch (JSONException e) {
                        Log.e(mContext.getClass().getName(), e.getMessage() == null ? "" : e.getMessage());
                    }
                }
            });
        }

        @JavascriptInterface
        public void notifications(final String data) {
            executor.submit(() -> {
                try {
                    final JSONObject mNotify = new JSONObject(data);

                    switch (mNotify.getString("action")) {
                        case "pageLoaded":
                            presenter.getNotifications(WebActivity.this);
                            break;
                        case "renderDone":
                            runOnUiThread(() -> {
                                swipe.setRefreshing(false);
                                setLoadingIndicator(false);

                                if (presenter.getCountUnwatchedNotifications() > 0) {
                                    presenter.updateNotify(WebActivity.this);
                                }
                            });
                            break;
                        case "request":
                            String params = (mNotify.isNull("params")) ? null : mNotify.getString("params");
                            String idNotify = (mNotify.isNull("id")) ? null : mNotify.getString("id");

                            presenter.updateNotify(WebActivity.this, params, idNotify);
                            break;
                    }
                } catch (JSONException exception) {
                    runOnUiThread(() -> {
                        swipe.setRefreshing(false);
                        setLoadingIndicator(false);
                        Toast.makeText(WebActivity.this, "List of notifications are corrupt. Please go to the page later...", Toast.LENGTH_LONG).show();
                    });
                }
            });
        }

    }

    public void sendNotificationsForPage(final String notifications) {
        runOnUiThread(() -> webView.evaluateJavascript(
                "(function(){if(window.notifications && window.notifications.render) {window.notifications.render(" + notifications + ");}})()",
                value -> {
                    //Toast.makeText(WebActivity.this, "Render notifications", Toast.LENGTH_LONG).show();
                }
        ));
    }

    private void changeIconHomeForNavigationMenu(int iconColor) {
        iconColor = ContextCompat.getColor(getApplicationContext(), iconColor);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            Drawable dr = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_menu);
            if (dr != null) {
                Drawable wrapDrawable = DrawableCompat.wrap(dr);
                DrawableCompat.setTint(wrapDrawable, iconColor);
                ab.setHomeAsUpIndicator(wrapDrawable);
            }
        }
    }

    public void changeTitleColor(int color) {
        toolbarTitle.setTextColor(ContextCompat.getColor(getApplicationContext(), color));
    }

    public void changeProfileIconColor(int color) {
        toolbarProfile.setColorFilter(ContextCompat.getColor(getApplicationContext(), color), android.graphics.PorterDuff.Mode.MULTIPLY);
    }

    private void toogleAddMenu(boolean open) {
        if (open) {
            String url = webView.getUrl();

            setEnabledBtnAddTopic(true);
            setEnabledBtnAddPm(true);
            setEnabledBtnAddChat(true);
            setEnabledBtnAddSchool(true);

            if (url != null) {
                if (url.contains(Api.FORUM_NEW_POSTS)) {
                    setEnabledBtnAddTopic(false);
                } else if (url.contains(Api.PM_NEW_URL)) {
                    setEnabledBtnAddPm(false);
                } else if (url.contains(Api.FORUM_URL + "/mchat.php")) {
                    setEnabledBtnAddChat(false);
                } else if (url.contains(Api.FORUM_ADD_NEW_SCHOOL)) {
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
            mUiHandler.postDelayed(() -> {
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) btnAddLayout.getLayoutParams();
                lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                Log.d("DIMENS", "DIMENS:h=" + lp.height + " w=" + lp.width);
                btnAddLayout.setLayoutParams(lp);
                btnAddLayout.requestLayout();
            }, 0);
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

            mUiHandler.postDelayed(() -> {
                if (!btnAdd.isOpened()) {
                    //hideBtnAddAllItems();
                /*btnAdd.getChildAt(0).setVisibility(View.GONE);
                btnAdd.getChildAt(1).setVisibility(View.GONE);
                btnAdd.getChildAt(2).setVisibility(View.GONE);
                btnAdd.getChildAt(3).setVisibility(View.GONE);*/
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) btnAddLayout.getLayoutParams();
                    lp.height = 0;
                    lp.width = 0;
                    Log.d("DIMENS", "DIMENS:h=" + lp.height + " w=" + lp.width);
                    btnAddLayout.setLayoutParams(lp);
                    btnAddLayout.requestLayout();

                    btnAddLayout.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.transparent));
                    //btnAdd.requestLayout();
                }

                //Toast.makeText(WebActivity.this, "height:" + btnAdd.getHeight(), Toast.LENGTH_SHORT).show();
                //Toast.makeText(WebActivity.this, "width:" + btnAdd.getWidth(), Toast.LENGTH_SHORT).show();
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

//    public DeviceInfo getDeviceInformation(Activity activity) {
//        //PackageInfo info = callActivity.getPackageManager().getPackageInfo(callActivity.getApplication().getPackageName(), 0);
//        //info.versionName
//        //info.versionCode+
//
//        /* Device:*/
//        String board = android.os.Build.BOARD;
//        String brand = android.os.Build.BRAND;
//        String device = android.os.Build.DEVICE;
//        String model = android.os.Build.MODEL;
//        String product = android.os.Build.PRODUCT;
//        String tags = android.os.Build.TAGS;
//
//        // OS:
//        String buildRelease = android.os.Build.VERSION.RELEASE + ", Inc: '" + android.os.Build.VERSION.INCREMENTAL + "'";
//        String displayBuild = android.os.Build.DISPLAY;
//        String fingerPrint = android.os.Build.FINGERPRINT;
//        String buildID = android.os.Build.ID;
//        String time = String.valueOf(android.os.Build.TIME);
//        String type = android.os.Build.TYPE;
//        String user = android.os.Build.USER;
//
//        //Density:
//        DisplayMetrics metrics = new DisplayMetrics();
//        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
//        String density = String.valueOf(metrics.density);
//        String densityDpi = String.valueOf(metrics.densityDpi);
//        String scaledDensity = String.valueOf(metrics.scaledDensity);
//        String xdpi = String.valueOf(metrics.xdpi);
//        String ydpi = String.valueOf(metrics.ydpi);
//        //Density reference:
//        String densityDefault = String.valueOf(DisplayMetrics.DENSITY_DEFAULT);
//        String densityLow = String.valueOf(DisplayMetrics.DENSITY_LOW);
//        String densityMedium = String.valueOf(DisplayMetrics.DENSITY_MEDIUM);
//        String densityHigh = String.valueOf(DisplayMetrics.DENSITY_HIGH);
//
//        //"** Screen:");
//        String heightPixels = String.valueOf(metrics.heightPixels);
//        String widthPixels = String.valueOf(metrics.widthPixels);
//
//        // String heading = "RAM Information";
//        long totalRamValue = totalRamMemorySize();
//        long freeRamValue = freeRamMemorySize();
//        long usedRamValue = totalRamValue - freeRamValue;
//        String ram = "usedRam=" + formatSize(usedRamValue) + " MB, " +
//                "freeRam=" + formatSize(freeRamValue) + " MB," +
//                "totalRam=" + formatSize(totalRamValue) + " MB";
//
//        // String internalMemoryTitle = "Internal Memory Information";
//        long totalInternalValue = getTotalInternalMemorySize();
//        long freeInternalValue = getAvailableInternalMemorySize();
//        long usedInternalValue = totalInternalValue - freeInternalValue;
//        String internalMemory = "usedInternal=" + formatSize(usedInternalValue) + " ," +
//                "freeInternal=" + formatSize(freeInternalValue) + " ," +
//                "totalInternal=" + formatSize(totalInternalValue);
//
//        // String externalMemoryTitle = "External Memory Information";
//        long totalExternalValue = getTotalExternalMemorySize();
//        long freeExternalValue = getAvailableExternalMemorySize();
//        long usedExternalValue = totalExternalValue - freeExternalValue;
//        String externalMemory = "usedExternal=" + formatSize(usedExternalValue) + " , " +
//                "freeExternal=" + formatSize(freeExternalValue) + " ," +
//                "totalExternal=" + formatSize(totalExternalValue);
//
//        DeviceInfo di = new DeviceInfo();
//
//        di.setBoard(board);
//        di.setBrand(brand);
//        di.setDevice(device);
//        di.setModel(model);
//        di.setProduct(product);
//        di.setTags(tags);
//        di.setBuildRelease(buildRelease);
//        di.setDisplayBuild(displayBuild);
//        di.setFingerPrint(fingerPrint);
//        di.setBuildID(buildID);
//
//
//        di.setTime(time);
//        di.setType(type);
//        di.setUser(user);
//        di.setDensity(density);
//        di.setDensityDpi(densityDpi);
//        di.setScaledDensity(scaledDensity);
//        di.setXdpi(xdpi);
//        di.setYdpi(ydpi);
//        di.setDensityDefault(densityDefault);
//        di.setDensityLow(densityLow);
//        di.setDensityMedium(densityMedium);
//        di.setDensityHigh(densityHigh);
//        di.setHeightPixels(heightPixels);
//        di.setWidthPixels(widthPixels);
//        di.setRam(ram);
//        di.setInternalMemory(internalMemory);
//        di.setExternalMemory(externalMemory);
//
//        return di;
//    }

    private long freeRamMemorySize() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null)
            activityManager.getMemoryInfo(mi);

        return mi.availMem / 1048576L;
    }

//    private long totalRamMemorySize() {
//        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
//        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//        if (activityManager != null)
//            activityManager.getMemoryInfo(mi);
//        return mi.totalMem / 1048576L;
//    }

    public static boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    }

    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        return availableBlocks * blockSize;
    }

    public static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        return totalBlocks * blockSize;
    }

    public static long getAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long availableBlocks = stat.getAvailableBlocksLong();
            return availableBlocks * blockSize;
        } else {
            return 0;
        }
    }

    public static long getTotalExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getBlockCountLong();
            return totalBlocks * blockSize;
        } else {
            return 0;
        }
    }

//    public static String formatSize(long size) {
//        String suffix = null;
//
//        if (size >= 1024) {
//            suffix = " KB";
//            size /= 1024;
//            if (size >= 1024) {
//                suffix = " MB";
//                size /= 1024;
//            }
//        }
//        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));
//
//        int commaOffset = resultBuffer.length() - 3;
//        while (commaOffset > 0) {
//            resultBuffer.insert(commaOffset, ',');
//            commaOffset -= 3;
//        }
//        if (suffix != null) resultBuffer.append(suffix);
//        return resultBuffer.toString();
//    }

    private void hideKeyboard() {
        InputMethodManager manager = ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE));
        if (manager != null)
            manager.hideSoftInputFromWindow((getWindow().getDecorView().getApplicationWindowToken()), 0);
    }

    private void showBtnAdd(boolean show) {
        if (show) {
            btnAddLayout.setVisibility(View.VISIBLE);
            btnAdd.setVisibility(View.VISIBLE);
        } else {
            btnAddLayout.setVisibility(View.GONE);
            btnAdd.setVisibility(View.GONE);
        }
    }

    private void showBtnAddTopic(boolean show) {
        if (show) {
            btnAddTopic.setVisibility(View.VISIBLE);
            btnAddTopic.setLabelVisibility(View.VISIBLE);
        } else {
            btnAddTopic.setVisibility(View.GONE);
            btnAddTopic.setLabelVisibility(View.GONE);
        }
    }

    private void showBtnAddPm(boolean show) {
        if (show) {
            btnAddPm.setVisibility(View.VISIBLE);
            btnAddPm.setLabelVisibility(View.VISIBLE);
        } else {
            btnAddPm.setVisibility(View.GONE);
            btnAddPm.setLabelVisibility(View.GONE);
        }
    }

    private void showBtnAddChat(boolean show) {
        if (show) {
            btnAddChat.setVisibility(View.VISIBLE);
            btnAddChat.setLabelVisibility(View.VISIBLE);
        } else {
            btnAddChat.setVisibility(View.GONE);
            btnAddChat.setLabelVisibility(View.GONE);
        }
    }

    private void showBtnAddSchool(boolean show) {
        if (show) {
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

    private void showBtnAddAllItems() {
        showBtnAddTopic(true);
        showBtnAddPm(true);
        showBtnAddChat(true);
        showBtnAddSchool(true);
    }

    private void hideBtnAddAllItems() {
        showBtnAddTopic(false);
        showBtnAddPm(false);
        showBtnAddChat(false);
        showBtnAddSchool(false);
    }

    /*
        private void openFileDialog(ValueCallback<Uri> uploadMsg) {
            Toast.makeText(getApplicationContext(), "openFile", Toast.LENGTH_LONG).show();
            mUploadMessage = uploadMsg;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "File Chooser"), FILECHOOSER_RESULTCODE);
        }
    */
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

            File file = new File(imageStorageDir + File.separator + "IMG_" + System.currentTimeMillis() + ".jpg");
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
            if (resultCode == RESULT_OK) {
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
            if (resultCode == RESULT_OK) {
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

    @Override
    public void updateUnreadNotification(int notificationsUnread) {
        runOnUiThread(() -> {
            Storage.saveBadgeCount(this, notificationsUnread);
            if (notificationsUnread > 0) {
                countNotifications.setVisibility(View.VISIBLE);
                countNotifications.setText(String.valueOf(notificationsUnread));
                ab.setHomeAsUpIndicator(setBadgeCount(this, R.drawable.ic_menu, notificationsUnread));
            } else {
                countNotifications.setVisibility(View.GONE);
                ab.setHomeAsUpIndicator(setBadgeCount(this, R.drawable.ic_menu, 0));
            }
        });
    }

    @Override
    public void showChatNotificationCount(String count) {
        int cnt;
        try {
            cnt = Integer.parseInt(count);
        } catch (NumberFormatException e) {
            cnt = 0;
        }
        Storage.saveBadgeCount(this, Storage.getBadgeCount(this) + cnt);
        if (cnt > 0) {
            menuChatCount.setText(count);
            menuChatCount.setVisibility(View.VISIBLE);
        } else {
            menuChatCount.setText("");
            menuChatCount.setVisibility(View.GONE);
        }
    }

    public void tryAgain() {
        presenter.resetError();
        destroyOfflineAlertDialog();
        webView.reload();
    }

    public static float dpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }

    private void updateVisibilityToolbar(String url) {
        int state = (url.contains(Api.TEST_URL) ? View.GONE : View.VISIBLE);

        findViewById(R.id.toolbar).setVisibility(state);
    }

    private void openLoginPage() {
        Intent intent = new Intent(this, LoginActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
    }

    private void destroyOfflineAlertDialog() {
        if (isDestroyed() || isFinishing())
            return;
        try {
            if (ViewHelper.alertDialog != null && ViewHelper.alertDialog.isShowing()) {
                ViewHelper.alertDialog.dismiss();
                ViewHelper.alertDialog = null;
            }
        } catch (Exception ignored) {

        }
    }
    /*private final BroadcastReceiver mConnectivityChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

            String state = "connected";

            if(!isConnected) {
                presenter.setError(WebPresenter.ERROR_CONNECT);
            } else {
                if(presenter.getError() == WebPresenter.ERROR_CONNECT) {
                    presenter.resetError();
                }
            }

            webView.setNetworkAvailable(isConnected);
            Toast.makeText(getApplicationContext(), state, Toast.LENGTH_LONG).show();
        }
    };*/

    /*Referrer API to securely retrieve referral content from Google Play.
     * Related to changes Google Play Store's policy March 1, 2020.*/

    private void installReferrerClientAction() {
        referrerClient = InstallReferrerClient.newBuilder(this).build();
        referrerClient.startConnection(new InstallReferrerStateListener() {
            @Override
            public void onInstallReferrerSetupFinished(int responseCode) {
                switch (responseCode) {
                    case InstallReferrerClient.InstallReferrerResponse.OK:
                        getReferrerDetails();
                        break;
                    case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                        // API not available on the current Play Store app.
                        break;
                    case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                        // Connection couldn't be established.
                        break;
                }
            }

            @Override
            public void onInstallReferrerServiceDisconnected() {
                // restart the connection by calling the startConnection() method.
            }
        });
    }

    private void getReferrerDetails() {
        ReferrerDetails response;
        try {
            //Data for referrer details
            response = referrerClient.getInstallReferrer();
            String referrerUrl = response.getInstallReferrer();
            long referrerClickTime = response.getReferrerClickTimestampSeconds();
            long appInstallTime = response.getInstallBeginTimestampSeconds();
            boolean instantExperienceLaunched = response.getGooglePlayInstantParam();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public class AjaxHandler {

        private static final String TAG = "AjaxHandler";
        private final Context context;

        public AjaxHandler(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void ajaxBegin() {
            Log.w(TAG, "AJAX Begin");
            Toast.makeText(context, "AJAX Begin", Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public void ajaxDone() {
            Log.w(TAG, "AJAX Done");
            Toast.makeText(context, "AJAX Done", Toast.LENGTH_SHORT).show();
        }
    }

}