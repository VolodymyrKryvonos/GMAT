package com.deepinspire.gmatclub.api;

import com.deepinspire.gmatclub.BuildConfig;

public class Api {
    public static String DOMAIN = BuildConfig.HOST;//"gmatclub.com";

    public static String HOME_URL = "https://" + DOMAIN;

    public static final String FORUM_URL = HOME_URL + "/forum";

    public static final String FORUM_LOGIN_URL = HOME_URL + "/forum/ucp.php?mode=login";

    public static final String FORUM_FORGOT_PASSWORD_URL = HOME_URL + "/forum/ucp.php?mode=sendpassword";

    //    public static final String FORUM_LOGOUT_URL = HOME_URL + "/forum/ucp.тphp?mode=logout";
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
    public static final String TEST_URL = HOME_URL + "/tests-beta/test";
    public static final String PRACTICE_URL = HOME_URL + BuildConfig.PAGE_PRACTICE;
    public static final String DEALS_URL = HOME_URL + "/marketplace";
    public static final String REVIEWS_URL = HOME_URL + "/reviews";
    public static final String CHAT_URL = HOME_URL + "/forum/mchat.php?menu=true";
    public static final String UCP_URL = HOME_URL + "/forum/ucp.php";
    public static final String UCP_MY_POSTS_URL = UCP_URL + "?i=291";
    public static final String UCP_MY_BOOKMARKS_URL = UCP_URL + "?i=main&mode=bookmarks";
    public static final String UCP_MY_ERROR_LOG_URL = UCP_URL + "?i=error_log&mode=error_log";
    public static final String UCP_MY_SETTINGS_URL = UCP_URL + "?i=301";
    public static final String UCP_FORUM_SETTINGS_URL = UCP_URL + "?i=profile&mode=notice_settings";

    public static String PM_URL = UCP_URL + "?i=pm&amp;folder=inbox";
    public static String PM_NEW_URL = UCP_URL + "?i=pm&mode=compose";

    public static final String FORUM_SEARCH = HOME_URL + "/forum/search.php?terms=both&sc=1&sk=re&sd=a&d_s=1&sf=all&sr=posts&t=0&keywords=";

    public static final String FORUM_SEARCH_MY_POSTS = FORUM_URL + "/search.php?search_id=egosearch";
    //public static final String FORUM_SEARCH_UNANSWERED = FORUM_URL + "/search.php?search_id=unanswered";
    //public static final String FORUM_SEARCH_NEW_POSTS = FORUM_URL + "/search.php?search_id=newposts";
    public static final String FORUM_NEW_POSTS = FORUM_URL + "/create_topic.php";
    public static final String FORUM_GOOGLE_SEARCH = FORUM_URL + "/search-results.xhtml";


    public static final String FORUM_ADD_NEW_SCHOOL = FORUM_URL + "/decision-tracker.html?fl=nav";

    public static final int AUTH_AVAILABLE_COUNT_FAILED_REQUESTS = 3;

    public static final String DECISION_TRACKER = FORUM_URL + "/decision-tracker.html";
    public static final String MBA_DISCUSSIONS = FORUM_URL + BuildConfig.PAGE_MBA;
    public static final String LEAVE_FEEDBACK = "https://docs.google.com/forms/d/e/1FAIpQLSc86Cr25gbazXAriZyYNrcCxXhm_pu8FJznFNCKk8nzaHEn0g/viewform";
   // public static final String OLD_CHAT_LINK = "https://gmatclub.com/forum/mchat.php";
    public  static  final String POLICY_URL= HOME_URL+BuildConfig.PAGE_POLICY;
}