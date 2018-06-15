package com.deepinspire.gmatclub.api;

import android.net.Uri;
import android.webkit.ValueCallback;

/**
 * Created by dmytro mytsko on 22.03.18.
 */
public class Api {
    public static String DOMAIN = "gmatclub.com";

    public static String HOME_URL = "https://" + DOMAIN;

    public static final String FORUM_URL = HOME_URL + "/forum";

    public static final String FORUM_LOGIN_URL = HOME_URL + "/forum/ucp.php?mode=login";

    public static final String FORUM_FORGOT_PASSWORD_URL = HOME_URL + "/forum/ucp.php?mode=sendpassword";

    //    public static final String FORUM_LOGOUT_URL = HOME_URL + "/forum/ucp.php?mode=logout";
    public static final String FORUM_REGISTER_URL = HOME_URL + "/forum/ucp.php?mode=register";

    public static final String FORUM_NOTIFICATIONS = HOME_URL + "/forum/mobileAppNotifications.html";

    public static final String PROFILE = HOME_URL + "/forum/members";

    public static final String FORUM_GET_NOTIFICATIONS_JSON = HOME_URL + "/forum/notify.php?action=get";
    public static final String FORUM_UPDATE_NOTIFICATIONS = HOME_URL + "/forum/notify.php?action=update";

    public static final String FORUM_PREFS_NAME = "forum";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    public static final String COOKIES = "cookies";

    public static final String TESTS_URL = HOME_URL + "/tests-beta";
    public static final String PRACTICE_URL = HOME_URL + "/practice.php";
    public static final String DEALS_URL = HOME_URL + "/marketplace";
    public static final String REVIEWS_URL = HOME_URL + "/reviews";
    public static final String CHAT_URL = HOME_URL + "/gchat";
    public static final String UCP_URL = HOME_URL + "/forum/ucp.php";
    public static final String UCP_MY_POSTS_URL = UCP_URL + "?i=291";
    public static final String UCP_MY_BOOKMARKS_URL = UCP_URL + "?i=main&mode=bookmarks";
    public static final String UCP_MY_ERROR_LOG_URL = UCP_URL + "?i=error_log&mode=error_log";
    public static final String UCP_MY_SETTINGS_URL = UCP_URL + "?i=301";
    public static final String UCP_FORUM_SETTINGS_URL = UCP_URL + "?i=profile&mode=notice_settings";

    public static String PM_URL = UCP_URL + "?i=pm&amp;folder=inbox";
    public static String PM_NEW_URL = UCP_URL + "?i=pm&mode=compose";

    public static final String FORUM_SEARCH = HOME_URL + "/forum/advanced-search/";

    public static final String FORUM_SEARCH_MY_POSTS = FORUM_URL + "/search.php?search_id=egosearch";
    public static final String FORUM_SEARCH_UNANSWERED = FORUM_URL + "/search.php?search_id=unanswered";
    public static final String FORUM_SEARCH_NEW_POSTS = FORUM_URL + "/search.php?search_id=newposts";
    public static final String FORUM_NEW_POSTS = FORUM_URL + "/create_topic.php";
    public static final String FORUM_GOOGLE_SEARCH = FORUM_URL + "/search-results.xhtml";


    public static final String FORUM_ADD_NEW_SCHOOL = FORUM_URL + "/decision-tracker.html?fl=nav";
}