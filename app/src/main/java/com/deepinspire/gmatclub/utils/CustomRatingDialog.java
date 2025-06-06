package com.deepinspire.gmatclub.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.RatingBar;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.deepinspire.gmatclub.R;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * /**
 * Created by Andriy Lykhtey on 2019-10-07.
 */
public class CustomRatingDialog {

    private static final String TAG = CustomRatingDialog.class.getSimpleName();

    private static final String PREF_NAME = "RateThisApp";
    private static final String KEY_INSTALL_DATE = "rta_install_date";
    private static final String KEY_LAUNCH_TIMES = "rta_launch_times";
    private static final String KEY_OPT_OUT = "rta_opt_out";
    private static final String KEY_ASK_LATER_DATE = "rta_ask_later_date";
    private static final float MAX_RATE_COUNT_STAR = 5f;

    private static Date mInstallDate = new Date();
    private static int mLaunchTimes = 0;
    private static boolean mOptOut = false;
    private static Date mAskLaterDate = new Date();

    private static CustomRatingDialog.Config sConfig = new CustomRatingDialog.Config();
    private static CustomRatingDialog.Callback sCallback = null;
    // Weak ref to avoid leaking the context
    private static WeakReference<AlertDialog> sDialogRef = null;

    /**
     * If true, print LogCat
     */
    public static final boolean DEBUG = false;

    /**
     * Initialize RateThisApp configuration.
     *
     * @param config Configuration object.
     */
    public static void init(CustomRatingDialog.Config config) {
        sConfig = config;
    }

    /**
     * Set callback instance.
     * The callback will receive yes/no/later events.
     *
     * @param callback
     */
    public static void setCallback(CustomRatingDialog.Callback callback) {
        sCallback = callback;
    }

    /**
     * Call this API when the launcher activity is launched.<br>
     * It is better to call this API in onCreate() of the launcher activity.
     *
     * @param context Context
     */
    public static void onCreate(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        // If it is the first launch, save the date in shared preference.
        if (pref.getLong(KEY_INSTALL_DATE, 0) == 0L) {
            storeInstallDate(context, editor);
        }
        // Increment launch times
        int launchTimes = pref.getInt(KEY_LAUNCH_TIMES, 0);
        launchTimes++;
        editor.putInt(KEY_LAUNCH_TIMES, launchTimes);
        log("Launch times; " + launchTimes);

        editor.apply();

        mInstallDate = new Date(pref.getLong(KEY_INSTALL_DATE, 0));
        mLaunchTimes = pref.getInt(KEY_LAUNCH_TIMES, 0);
        mOptOut = pref.getBoolean(KEY_OPT_OUT, false);
        mAskLaterDate = new Date(pref.getLong(KEY_ASK_LATER_DATE, 0));

        printStatus(context);
    }

    /**
     * This API is deprecated.
     * You should call onCreate instead of this API in Activity's onCreate().
     *
     * @param context
     */
    @Deprecated
    public static void onStart(Context context) {
        onCreate(context);
    }

    /**
     * Show the rate dialog if the criteria is satisfied.
     *
     * @param context Context
     * @return true if shown, false otherwise.
     */
    public static boolean showRateDialogIfNeeded(final Context context) {
        if (shouldShowRateDialog()) {
            showRateDialog(context);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Show the rate dialog if the criteria is satisfied.
     *
     * @param context Context
     * @param themeId Theme ID
     * @return true if shown, false otherwise.
     */
    public static boolean showRateDialogIfNeeded(final Context context, int themeId) {
        if (shouldShowRateDialog()) {
            showRateDialog(context, themeId);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check whether the rate dialog should be shown or not.
     * Developers may call this method directly if they want to show their own view instead of
     * dialog provided by this library.
     *
     * @return
     */
    public static boolean shouldShowRateDialog() {
        if (mOptOut) {
            return false;
        } else {
            if (mLaunchTimes >= sConfig.mCriteriaLaunchTimes) {
                return true;
            }
            long threshold = TimeUnit.DAYS.toMillis(sConfig.mCriteriaInstallDays);   // msec
            if (new Date().getTime() - mInstallDate.getTime() >= threshold &&
                    new Date().getTime() - mAskLaterDate.getTime() >= threshold) {
                return true;
            }
            return false;
        }
    }

    /**
     * Show the rate dialog
     *
     * @param context
     */
    public static void showRateDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        showRateDialog(context, builder);
    }

    /**
     * Show the rate dialog
     *
     * @param context
     * @param themeId
     */
    public static void showRateDialog(final Context context, int themeId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, themeId);
        showRateDialog(context, builder);
    }

    /**
     * Stop showing the rate dialog
     *
     * @param context
     */
    public static void stopRateDialog(final Context context) {
        setOptOut(context, true);
    }

    /**
     * Get count number of the rate dialog launches
     *
     * @return
     */
    public static int getLaunchCount(final Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(KEY_LAUNCH_TIMES, 0);
    }

    private static void showRateDialog(final Context context, final AlertDialog.Builder builder) {
        if (sDialogRef != null && sDialogRef.get() != null) {
            // Dialog is already present
            return;
        }

        LinearLayout linearLayout = new LinearLayout(context);
        final RatingBar rating = new RatingBar(context);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lp.topMargin = 16;
        linearLayout.setGravity(Gravity.CENTER);
        rating.setLayoutParams(lp);
        rating.setNumStars(5);
        rating.setStepSize(1);

        linearLayout.addView(rating);

        builder.setView(linearLayout);

        int titleId = sConfig.mTitleId != 0 ? sConfig.mTitleId : R.string.dialog_title;
        int messageId = sConfig.mMessageId != 0 ? sConfig.mMessageId : R.string.dialog_message;
        int cancelButtonID = sConfig.mCancelButton != 0 ? sConfig.mCancelButton : R.string.dialog_cancel;
        int thanksButtonID = sConfig.mNoButtonId != 0 ? sConfig.mNoButtonId : R.string.dialog_no;
        int rateButtonID = sConfig.mYesButtonId != 0 ? sConfig.mYesButtonId : R.string.dialog_ok;
        builder.setTitle(titleId);
        builder.setMessage(messageId);
        builder.setCancelable(sConfig.mCancelable);
        builder.setPositiveButton(rateButtonID, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openGooglePlay(context);
            }
        });
        builder.setNeutralButton(cancelButtonID, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (sCallback != null) {
                    sCallback.onCancelClicked();
                }
                clearSharedPreferences(context);
                storeAskLaterDate(context);
            }
        });
        builder.setNegativeButton(thanksButtonID, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (sCallback != null) {
                    sCallback.onNoClicked();
                }
                setOptOut(context, true);
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (sCallback != null) {
                    sCallback.onCancelClicked();
                }
                clearSharedPreferences(context);
                storeAskLaterDate(context);
            }
        });
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                sDialogRef.clear();
            }
        });
        sDialogRef = new WeakReference<>(builder.show());

        rating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (rating == MAX_RATE_COUNT_STAR && sDialogRef != null) {
                    openGooglePlay(context);
                    sDialogRef.get().dismiss();
                } else if (sDialogRef != null) {
                    if (sCallback != null) sCallback.onLeaveFeedbackClicked();
                    sDialogRef.get().dismiss();
                    clearSharedPreferences(context);
                    storeAskLaterDate(context);
                }
            }
        });

    }

    private static void openGooglePlay(Context context) {
        if (sCallback != null) {
            sCallback.onYesClicked();
        }
        String appPackage = context.getPackageName();
        String url = "market://details?id=" + appPackage;
        if (!TextUtils.isEmpty(sConfig.mUrl)) {
            url = sConfig.mUrl;
        }
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
        }
        setOptOut(context, true);
    }

    /**
     * Clear data in shared preferences.<br>
     * This API is called when the "Later" is pressed or canceled.
     *
     * @param context
     */
    private static void clearSharedPreferences(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(KEY_INSTALL_DATE);
        editor.remove(KEY_LAUNCH_TIMES);
        editor.apply();
    }

    /**
     * Set opt out flag.
     * If it is true, the rate dialog will never shown unless app data is cleared.
     * This method is called when Yes or No is pressed.
     *
     * @param context
     * @param optOut
     */
    private static void setOptOut(final Context context, boolean optOut) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(KEY_OPT_OUT, optOut);
        editor.apply();
        mOptOut = optOut;
    }

    /**
     * Store install date.
     * Install date is retrieved from package manager if possible.
     *
     * @param context
     * @param editor
     */
    private static void storeInstallDate(final Context context, SharedPreferences.Editor editor) {
        Date installDate = new Date();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            PackageManager packMan = context.getPackageManager();
            try {
                PackageInfo pkgInfo = packMan.getPackageInfo(context.getPackageName(), 0);
                installDate = new Date(pkgInfo.firstInstallTime);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        editor.putLong(KEY_INSTALL_DATE, installDate.getTime());
        log("First install: " + installDate.toString());
    }

    /**
     * Store the date the user asked for being asked again later.
     *
     * @param context
     */
    private static void storeAskLaterDate(final Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(KEY_ASK_LATER_DATE, System.currentTimeMillis());
        editor.apply();
    }

    /**
     * Print values in SharedPreferences (used for debug)
     *
     * @param context
     */
    private static void printStatus(final Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        log("*** RateThisApp Status ***");
        log("Install Date: " + new Date(pref.getLong(KEY_INSTALL_DATE, 0)));
        log("Launch Times: " + pref.getInt(KEY_LAUNCH_TIMES, 0));
        log("Opt out: " + pref.getBoolean(KEY_OPT_OUT, false));
    }

    /**
     * Print log if enabled
     *
     * @param message
     */
    private static void log(String message) {
        if (DEBUG) {
            Log.v(TAG, message);
        }
    }

    /**
     * RateThisApp configuration.
     */
    public static class Config {
        private String mUrl = null;
        private int mCriteriaInstallDays;
        private int mCriteriaLaunchTimes;
        private int mTitleId = 0;
        private int mMessageId = 0;
        private int mYesButtonId = 0;
        private int mNoButtonId = 0;
        private int mCancelButton = 0;
        private boolean mCancelable = true;

        /**
         * Constructor with default criteria.
         */
        public Config() {
            this(1, 10);
        }

        /**
         * Constructor.
         *
         * @param criteriaInstallDays
         * @param criteriaLaunchTimes
         */
        public Config(int criteriaInstallDays, int criteriaLaunchTimes) {
            this.mCriteriaInstallDays = criteriaInstallDays;
            this.mCriteriaLaunchTimes = criteriaLaunchTimes;
        }

        /**
         * Set title string ID.
         *
         * @param stringId
         */
        public void setTitle(@StringRes int stringId) {
            this.mTitleId = stringId;
        }

        /**
         * Set message string ID.
         *
         * @param stringId
         */
        public void setMessage(@StringRes int stringId) {
            this.mMessageId = stringId;
        }

        /**
         * Set rate now string ID.
         *
         * @param stringId
         */
        public void setYesButtonText(@StringRes int stringId) {
            this.mYesButtonId = stringId;
        }

        /**
         * Set no thanks string ID.
         *
         * @param stringId
         */
        public void setNoButtonText(@StringRes int stringId) {
            this.mNoButtonId = stringId;
        }

        /**
         * Set cancel string ID.
         *
         * @param stringId
         */
        public void setCancelButtonText(@StringRes int stringId) {
            this.mCancelButton = stringId;
        }

        /**
         * Set navigation url when user clicks rate button.
         * Typically, url will be https://play.google.com/store/apps/details?id=PACKAGE_NAME for Google Play.
         *
         * @param url
         */
        public void setUrl(String url) {
            this.mUrl = url;
        }

        public void setCancelable(boolean cancelable) {
            this.mCancelable = cancelable;
        }
    }

    /**
     * Callback of dialog click event
     */
    public interface Callback {
        /**
         * "Rate now" event
         */
        void onYesClicked();

        /**
         * "No, thanks" event
         */
        void onNoClicked();

        /**
         * "Later" event
         */
        void onCancelClicked();

        void onLeaveFeedbackClicked();
    }
}
