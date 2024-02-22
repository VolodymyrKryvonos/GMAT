package com.deepinspire.gmatclub.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.deepinspire.gmatclub.R;
import com.deepinspire.gmatclub.api.AuthException;
import com.deepinspire.gmatclub.auth.LoginActivity;
import com.deepinspire.gmatclub.web.WebActivity;

/**
 * Created by dmytro mytsko on 23.03.18.
 */
public class ViewHelper {
    public static AlertDialog alertDialog;

    public static void showForgotPasswordDialog(final Activity activity) {
        if (activity == null || activity.isFinishing() | activity.isDestroyed())
            return;

        LayoutInflater inflaterAlertDialog = LayoutInflater.from(activity);

        @SuppressLint("InflateParams") final View dialogLayout = inflaterAlertDialog.inflate(R.layout.alert_dialog_forgot_password, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(dialogLayout);

        alertDialog = builder.create();
        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(true);

        alertDialog.setOnCancelListener(DialogInterface::dismiss);

        alertDialog.setOnDismissListener(dialog -> {
            InputMethodManager manager = ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE));
            if (manager != null)
                manager.hideSoftInputFromWindow((activity.getWindow().getDecorView().getApplicationWindowToken()), 0);
        });

        final ProgressBar progressbar = dialogLayout.findViewById(R.id.loading);
        final ScrollView signInLayout = dialogLayout.findViewById(R.id.signInLayout);

        TextView signInButton = dialogLayout.findViewById(R.id.submitButton);

        signInButton.setOnClickListener(v -> {
            TextView errorMessage = dialogLayout.findViewById(R.id.message);

            EditText forgotPasswordInputEmail = dialogLayout.findViewById(R.id.forgotPasswordInputEmail);

            //forgotPasswordInputEmail.addTextChangedListener(new FieldWatcher(forgotPasswordInputEmail, activity, dialogLayout));

            String email = forgotPasswordInputEmail.getText().toString();

            boolean validEmail = Validator.validEmail(email);

            if (validEmail) {
                errorMessage.setVisibility(View.GONE);

                forgotPasswordInputEmail.setBackgroundResource(R.drawable.border_bottom_1dp);

                alertDialog.setCancelable(false);
                alertDialog.setCanceledOnTouchOutside(false);

                setLoadingIndicator(true, progressbar, signInLayout);

                if (activity instanceof WebActivity) {
                    ((WebActivity) activity).forgotPassword(email);
                } else {
                    ((LoginActivity) activity).forgotPassword(email);
                }

                setLoadingIndicator(true, progressbar, signInLayout);
            } else {
                errorMessage.setVisibility(View.VISIBLE);

                forgotPasswordInputEmail.setBackgroundResource(R.drawable.border_bottom_1dp_error);

                if (forgotPasswordInputEmail.requestFocus()) {
                    activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        setLoadingIndicator(false, progressbar, signInLayout);

        alertDialog.show();
    }

    public static void showResetPasswordDialog(final Activity activity) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed())
            return;

        LayoutInflater inflaterAlertDialog = LayoutInflater.from(activity);

        @SuppressLint("InflateParams") final View dialogLayout = inflaterAlertDialog.inflate(R.layout.alert_dialog_reset_password, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(dialogLayout);

        alertDialog = builder.create();
        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(false);

        alertDialog.setOnCancelListener(DialogInterface::dismiss);

        alertDialog.setOnDismissListener(dialog -> {
            InputMethodManager manager = ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE));
            if (manager != null)
                manager.hideSoftInputFromWindow((activity.getWindow().getDecorView().getApplicationWindowToken()), 0);
        });

        final ProgressBar progressbar = dialogLayout.findViewById(R.id.loading);
        final ScrollView signInLayout = dialogLayout.findViewById(R.id.signInLayout);

        LinearLayout btnResetLayout = dialogLayout.findViewById(R.id.btnResetLayout);
        btnResetLayout.setOnClickListener(v -> {
            setLoadingIndicator(true, progressbar, signInLayout);
            ((LoginActivity) activity).resetPassword(((EditText) dialogLayout.findViewById(R.id.resetPasswordInputEmail)).getText().toString());
        });

        LinearLayout btnNotNowLayout = dialogLayout.findViewById(R.id.btnNotNowLayout);
        btnNotNowLayout.setOnClickListener(v -> {
            if (alertDialog != null && alertDialog.isShowing())
                alertDialog.dismiss();
        });

        setLoadingIndicator(false, progressbar, signInLayout);

        alertDialog.show();
    }

    public static void showProfileAuthDialog(final WebActivity activity) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed())
            return;
        LayoutInflater inflaterAlertDialog = LayoutInflater.from(activity);
        @SuppressLint("InflateParams") View dialogLayout = inflaterAlertDialog.inflate(R.layout.alert_dialog_profile_auth, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(dialogLayout);

        final AlertDialog alertDialog = builder.create();

        alertDialog.show();

        alertDialog.setOnCancelListener(DialogInterface::dismiss);

        alertDialog.setOnDismissListener(dialog -> activity.changeProfileIconColor(R.color.mainOrange));

        TextView btnProfile = dialogLayout.findViewById(R.id.btnProfile);
        TextView btnMyPosts = dialogLayout.findViewById(R.id.btnMyPosts);
        TextView btnMyBookmarks = dialogLayout.findViewById(R.id.btnMyBookmarks);
        TextView btnMyErrorLog = dialogLayout.findViewById(R.id.btnMyErrorLog);
        LinearLayout btnSettings = dialogLayout.findViewById(R.id.btnSettingsLayout);
        LinearLayout btnSettingsNotifications = dialogLayout.findViewById(R.id.btnSettingsNotificationsLayout);
        LinearLayout btnLogout = dialogLayout.findViewById(R.id.btnLogoutLayout);

        btnProfile.setOnClickListener(v -> {
            activity.openPageById("profile");
            alertDialog.hide();
        });

        btnMyPosts.setOnClickListener(v -> {
            activity.openPageById("myPosts");
            alertDialog.hide();
        });

        btnMyBookmarks.setOnClickListener(v -> {
            activity.openPageById("myBookmarks");
            alertDialog.hide();
        });

        btnMyErrorLog.setOnClickListener(v -> {
            activity.openPageById("myErrorLog");
            alertDialog.hide();
        });

        btnSettings.setOnClickListener(v -> {
            activity.openPageById("settings");
            alertDialog.hide();
        });


        btnSettingsNotifications.setOnClickListener(v -> {
            activity.openPageById("settingsNotifications");
            alertDialog.hide();
        });

        btnLogout.setOnClickListener(v -> {
            activity.openPageById("logout");
            alertDialog.hide();
        });
    }

    public static void showProfileAnonDialog(final WebActivity activity) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed())
            return;
        LayoutInflater inflaterAlertDialog = LayoutInflater.from(activity);
        @SuppressLint("InflateParams") View dialogLayout = inflaterAlertDialog.inflate(R.layout.alert_dialog_profile_anon, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(dialogLayout);

        alertDialog = builder.create();
        alertDialog.show();

        alertDialog.setOnCancelListener(DialogInterface::dismiss);

        alertDialog.setOnDismissListener(dialog -> activity.changeProfileIconColor(R.color.mainOrange));

        TextView btnRegister = dialogLayout.findViewById(R.id.btnRegister);
        TextView btnSignIn = dialogLayout.findViewById(R.id.btnSignIn);
        LinearLayout btnSignInGoogle = dialogLayout.findViewById(R.id.btnSignInGoogleLayout);
        LinearLayout btnSignInFacebook = dialogLayout.findViewById(R.id.btnSignInGoogleFacebookLayout);

        btnRegister.setOnClickListener(v -> {
            activity.openPageById("register");
            if (alertDialog != null && alertDialog.isShowing())
                alertDialog.dismiss();
        });

        btnSignIn.setOnClickListener(v -> {
            if (alertDialog != null && alertDialog.isShowing())
                alertDialog.dismiss();
            alertDialog = null;
            activity.openPageById("signIn");
        });

        btnSignInGoogle.setOnClickListener(v -> {
            activity.openPageById("signInGoogle");
            //alertDialog.hide();
        });

        btnSignInFacebook.setOnClickListener(v -> activity.openPageById("signInFacebook"));
    }

    public static void showLeaveAppDialog(final WebActivity activity) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed())
            return;

        LayoutInflater inflaterAlertDialog = LayoutInflater.from(activity);
        @SuppressLint("InflateParams") View dialogLayout = inflaterAlertDialog.inflate(R.layout.alert_dialog_leave_app, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(dialogLayout);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();

        alertDialog.setOnCancelListener(DialogInterface::dismiss);

        Button leaveButton = dialogLayout.findViewById(R.id.leaveButton);
        Button cancelButton = dialogLayout.findViewById(R.id.cancelButton);

        leaveButton.setOnClickListener(v -> {
            alertDialog.dismiss();
            activity.goPreviousActivity();
            //activity.onBackPressed();
            //alertDialog.hide();
        });

        cancelButton.setOnClickListener(v -> {
            if (alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
            // alertDialog=null;
        });
    }

    /*
        public static void showErrorDialog(final AuthActivity activity) {
            if (activity == null || activity.isFinishing() || activity.isDestroyed())
                return;
            LayoutInflater inflaterAlertDialog = LayoutInflater.from(activity);
            @SuppressLint("InflateParams") final View dialogLayout = inflaterAlertDialog.inflate(R.layout.alert_dialog_error, null);

            //((TextView)dialogLayout.findViewById(R.id.alert_dialog_title)).setText(title);
            //((TextView)dialogLayout.findViewById(R.id.alert_dialog_message)).setText(message);


            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setView(dialogLayout);

            final AlertDialog alertDialog = builder.create();
            //alertDialog.setCancelable(false);
            //alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();

            alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    dialogInterface.dismiss();
                }
            });

            Button buttonAlertDialogConfirm = dialogLayout.findViewById(R.id.btnOk);

            buttonAlertDialogConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.hide();
                }
            });
        }

        public static void showFacebookSignInDialog(final LoginActivity activity) {
            if (activity == null || activity.isFinishing() || activity.isDestroyed())
                return;
            LayoutInflater inflaterAlertDialog = LayoutInflater.from(activity);
            @SuppressLint("InflateParams") View dialogLayout = inflaterAlertDialog.inflate(R.layout.alert_dialog_profile_anon, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setView(dialogLayout);

            alertDialog = builder.create();

            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);

            alertDialog.show();

            alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    dialogInterface.dismiss();
                }
            });

            setLoadingIndicator(true);
        }
    */
    private static void setLoadingIndicator(boolean active, ProgressBar progressBar, ScrollView contentLayout) {
        if (active) {
            progressBar.setVisibility(View.VISIBLE);
            contentLayout.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            contentLayout.setVisibility(View.VISIBLE);
        }
    }

    public static void showError(AuthException exception) {
        try {
            if (alertDialog != null) {
                ProgressBar progressbar = alertDialog.findViewById(R.id.loading);
                ScrollView signInLayout = alertDialog.findViewById(R.id.signInLayout);
                TextView message;

                switch (exception.getType()) {
                    case "login":
                        alertDialog.setCancelable(true);
                        alertDialog.setCanceledOnTouchOutside(true);

                        message = alertDialog.findViewById(R.id.errorMessage);
                        if (message != null)
                            message.setText(/*"Incorrect Login and Password"*/exception.getMessage());

                        if (!exception.getAction().equals("UNKNOWN_HOST")) {
                            EditText signInInputUsername = alertDialog.findViewById(R.id.signInInputUsername);
                            EditText signInInputPassword = alertDialog.findViewById(R.id.signInInputPassword);

                            TextView signInInputPasswordForgot = alertDialog.findViewById(R.id.signInInputPasswordForgot);

                            signInInputUsername.setHintTextColor(ContextCompat.getColor(alertDialog.getContext(), R.color.red));
                            signInInputUsername.setBackgroundResource(R.drawable.border_bottom_1dp_error);

                            signInInputPassword.setHintTextColor(ContextCompat.getColor(alertDialog.getContext(), R.color.red));
                            signInInputPassword.setBackgroundResource(R.drawable.border_bottom_1dp_error);

                            signInInputPasswordForgot.setTextColor(ContextCompat.getColor(alertDialog.getContext(), R.color.red));
                        }

                        message.setVisibility(View.VISIBLE);
                        progressbar.setVisibility(View.GONE);
                        signInLayout.setVisibility(View.VISIBLE);
                        break;
                    case "forgotPassword":
                        alertDialog.setCancelable(true);
                        alertDialog.setCanceledOnTouchOutside(true);

                        message = alertDialog.findViewById(R.id.message);

                        if (!exception.getAction().equals("UNKNOWN_HOST")) {
                            EditText forgotPasswordInputEmail = alertDialog.findViewById(R.id.forgotPasswordInputEmail);

                            forgotPasswordInputEmail.setHintTextColor(ContextCompat.getColor(alertDialog.getContext(), R.color.red));
                            forgotPasswordInputEmail.setBackgroundResource(R.drawable.border_bottom_1dp_error);

                            message.setText(R.string.msg_unknown_host);
                        } else {
                            message.setText(exception.getMessage());
                        }

                        message.setVisibility(View.VISIBLE);
                        progressbar.setVisibility(View.GONE);
                        signInLayout.setVisibility(View.VISIBLE);
                        break;
                    case "signInFacebook":
                        alertDialog.findViewById(R.id.signInLayout).setVisibility(View.GONE);
                        alertDialog.findViewById(R.id.loading).setVisibility(View.GONE);
                        alertDialog.findViewById(R.id.message).setVisibility(View.VISIBLE);
                        alertDialog.setCancelable(true);
                        alertDialog.setCanceledOnTouchOutside(true);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showSuccess(String type) {
        if (alertDialog != null) {
            ProgressBar progressbar;
            TextView message;
            ScrollView signInLayout;

            switch (type) {
                case "forgotPassword":
                    message = alertDialog.findViewById(R.id.message);
                    message.setText(R.string.email_success_send);
                    message.setTextColor(ContextCompat.getColor(alertDialog.getContext(), R.color.grey_A50));
                    message.setVisibility(View.VISIBLE);

                    LinearLayout forgotPasswordForm = alertDialog.findViewById(R.id.forgotPasswordForm);
                    forgotPasswordForm.setVisibility(View.GONE);

                    progressbar = alertDialog.findViewById(R.id.loading);
                    progressbar.setVisibility(View.GONE);

                    signInLayout = alertDialog.findViewById(R.id.signInLayout);
                    signInLayout.setVisibility(View.VISIBLE);

                    alertDialog.setCancelable(true);
                    alertDialog.setCanceledOnTouchOutside(true);
                    break;
                case "resetPassword":
                    message = alertDialog.findViewById(R.id.message);
                    message.setText(R.string.email_success_send);
                    message.setTextColor(ContextCompat.getColor(alertDialog.getContext(), R.color.grey_A50));
                    message.setVisibility(View.VISIBLE);

                    LinearLayout resetPasswordMessageForm = alertDialog.findViewById(R.id.resetPasswordMessageForm);
                    resetPasswordMessageForm.setVisibility(View.GONE);

                    LinearLayout resetPasswordEmailForm = alertDialog.findViewById(R.id.resetPasswordEmailForm);
                    resetPasswordEmailForm.setVisibility(View.GONE);

                    LinearLayout resetPasswordButtonsLayout = alertDialog.findViewById(R.id.resetPasswordButtonsLayout);
                    resetPasswordButtonsLayout.setVisibility(View.GONE);

                    progressbar = alertDialog.findViewById(R.id.loading);
                    progressbar.setVisibility(View.GONE);

                    signInLayout = alertDialog.findViewById(R.id.signInLayout);
                    signInLayout.setVisibility(View.VISIBLE);

                    alertDialog.setCancelable(true);
                    alertDialog.setCanceledOnTouchOutside(true);
                    break;
            }
        }
    }

    public static void setLoadingIndicator(boolean loading) {
        if (alertDialog != null) {
            final ProgressBar progressbar = alertDialog.findViewById(R.id.loading);
            final ScrollView signInLayout = alertDialog.findViewById(R.id.signInLayout);

            if (loading) {
                alertDialog.setCancelable(false);
                alertDialog.setCanceledOnTouchOutside(false);
                progressbar.setVisibility(View.VISIBLE);
                signInLayout.setVisibility(View.GONE);
            } else {
                alertDialog.setCancelable(true);
                alertDialog.setCanceledOnTouchOutside(true);
                progressbar.setVisibility(View.GONE);
                signInLayout.setVisibility(View.VISIBLE);
            }
        }

    }

    public static void showOfflineDialog(final WebActivity activity) {
        /*
        if (activity == null || activity.isFinishing() || activity.isDestroyed())
            return;
        LayoutInflater inflaterAlertDialog = LayoutInflater.from(activity);
        @SuppressLint("InflateParams") View dialogLayout = inflaterAlertDialog.inflate(R.layout.alert_dialog_offline, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(dialogLayout);

        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
            alertDialog = null;
        }

        alertDialog = builder.create();
        alertDialog.show();

        alertDialog.setCancelable(false);

        alertDialog.setOnCancelListener(DialogInterface::dismiss);

        alertDialog.setOnDismissListener(DialogInterface::dismiss);
        TextView tvMessage = dialogLayout.findViewById(R.id.networkMessage);
        tvMessage.setScroller(new Scroller(activity));
        tvMessage.setVerticalScrollBarEnabled(true);
        tvMessage.setMovementMethod(new ScrollingMovementMethod());

        TextView btnNetworkTryAgain = dialogLayout.findViewById(R.id.btnNetworkTryAgain);
        TextView btnNetworkSettings = dialogLayout.findViewById(R.id.btnNetworkSettings);

        btnNetworkTryAgain.setOnClickListener(v -> activity.tryAgain());

        btnNetworkSettings.setOnClickListener(v -> activity.openDeviceSettings());

         */
    }
}