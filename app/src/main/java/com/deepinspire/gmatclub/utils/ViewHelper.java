package com.deepinspire.gmatclub.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
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

import com.deepinspire.gmatclub.R;
import com.deepinspire.gmatclub.api.AuthException;
import com.deepinspire.gmatclub.auth.AuthActivity;
import com.deepinspire.gmatclub.web.WebActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dmytro mytsko on 23.03.18.
 */
public class ViewHelper {
    public static  AlertDialog alertDialog;

    public static void showLoginDialog(final Activity activity) {
        LayoutInflater inflaterAlertDialog = LayoutInflater.from(activity);
        final View dialogLayout = inflaterAlertDialog.inflate(R.layout.alert_dialog_login, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(dialogLayout);

        alertDialog = builder.create();
        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(true);

        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                ((InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE)).
                        hideSoftInputFromWindow((activity.getWindow().getDecorView().getApplicationWindowToken()), 0);
            }
        });

        final ProgressBar progressbar = (ProgressBar) dialogLayout.findViewById(R.id.loading);
        final ScrollView signInLayout = (ScrollView) dialogLayout.findViewById(R.id.signInLayout);

        final TextView signInInputPasswordForgot = (TextView) dialogLayout.findViewById(R.id.signInInputPasswordForgot);

        signInInputPasswordForgot.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                alertDialog = null;
                showForgotPasswordDialog(activity);
            }
        });

        TextView signInButton = (TextView) dialogLayout.findViewById(R.id.signInButton);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView errorMessage = (TextView) dialogLayout.findViewById(R.id.errorMessage);


                EditText signInInputUsername = (EditText) dialogLayout.findViewById(R.id.signInInputUsername);
                EditText signInInputPassword = (EditText) dialogLayout.findViewById(R.id.signInInputPassword);

                signInInputUsername.addTextChangedListener(new FieldWatcher(signInInputUsername, activity, dialogLayout));
                signInInputPassword.addTextChangedListener(new FieldWatcher(signInInputPassword, activity, dialogLayout));

                String username = signInInputUsername.getText().toString();
                String password = signInInputPassword.getText().toString();

                boolean validUsername = Validator.validUsername(username);
                boolean validPassword = Validator.validPassword(password);

                if(validUsername && validPassword) {
                    errorMessage.setVisibility(View.GONE);
                    errorMessage.setText("");

                    signInInputUsername.setHintTextColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.grey_A50));
                    signInInputUsername.setBackgroundResource(R.drawable.border_bottom_1dp);

                    signInInputPassword.setHintTextColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.grey_A50));
                    signInInputPassword.setBackgroundResource(R.drawable.border_bottom_1dp);

                    signInInputPasswordForgot.setTextColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.grey_A50));

                    alertDialog.setCancelable(false);
                    alertDialog.setCanceledOnTouchOutside(false);

                    setLoadingIndicator(true, progressbar, signInLayout);

                    if(activity instanceof AuthActivity) {
                        ((AuthActivity) activity).signIn(username, password);
                    } else {
                        ((WebActivity) activity).signIn(username, password);
                    }
                } else {
                    Map<String, String> messages = new HashMap<String, String>(){{
                        put("login", "Incorrect Login");
                        put("password", "Incorrect Password");
                        put("loginpassword", "Incorrect Login and Password");
                    }};

                    StringBuffer keyMessageError = new StringBuffer("");

                    if(!validUsername) {
                        keyMessageError.append("login");

                        signInInputUsername.setBackgroundResource(R.drawable.border_bottom_1dp_error);

                        if (signInInputUsername.requestFocus()) {
                            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        }
                    }

                    if(!validPassword) {
                        keyMessageError.append("password");

                        signInInputPassword.setBackgroundResource(R.drawable.border_bottom_1dp_error);

                        signInInputPasswordForgot.setTextColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.red));

                        if (signInInputPassword.requestFocus()) {
                            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        }
                    }

                    String message = messages.get(keyMessageError.toString());

                    if(message != null) {
                        errorMessage.setText(message);
                        errorMessage.setVisibility(View.VISIBLE);
                    }
                }

            }
        });

        setLoadingIndicator(false, progressbar, signInLayout);

        alertDialog.show();
    }

    public static void showForgotPasswordDialog(final Activity activity) {
        LayoutInflater inflaterAlertDialog = LayoutInflater.from(activity);

        final View dialogLayout = inflaterAlertDialog.inflate(R.layout.alert_dialog_forgot_password, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(dialogLayout);

        alertDialog = builder.create();
        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(true);

        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                ((InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE)).
                        hideSoftInputFromWindow((activity.getWindow().getDecorView().getApplicationWindowToken()), 0);
            }
        });

        final ProgressBar progressbar = (ProgressBar) dialogLayout.findViewById(R.id.loading);
        final ScrollView signInLayout = (ScrollView) dialogLayout.findViewById(R.id.signInLayout);

        TextView signInButton = (TextView) dialogLayout.findViewById(R.id.submitButton);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView errorMessage = (TextView) dialogLayout.findViewById(R.id.message);

                EditText forgotPasswordInputEmail = (EditText) dialogLayout.findViewById(R.id.forgotPasswordInputEmail);

                forgotPasswordInputEmail.addTextChangedListener(new FieldWatcher(forgotPasswordInputEmail, activity, dialogLayout));

                String email = forgotPasswordInputEmail.getText().toString();

                boolean validEmail= Validator.validEmail(email);

                if(validEmail) {
                    errorMessage.setVisibility(View.GONE);

                    forgotPasswordInputEmail.setBackgroundResource(R.drawable.border_bottom_1dp);

                    alertDialog.setCancelable(false);
                    alertDialog.setCanceledOnTouchOutside(false);

                    setLoadingIndicator(true, progressbar, signInLayout);

                    if(activity instanceof WebActivity) {
                        ((WebActivity) activity).forgotPassword(email);
                    } else {
                        ((AuthActivity) activity).forgotPassword(email);
                    }

                    setLoadingIndicator(true, progressbar, signInLayout);
                } else {
                    errorMessage.setVisibility(View.VISIBLE);

                    forgotPasswordInputEmail.setBackgroundResource(R.drawable.border_bottom_1dp_error);

                    if (forgotPasswordInputEmail.requestFocus()) {
                        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
            }
        });

        setLoadingIndicator(false, progressbar, signInLayout);

        alertDialog.show();
    }

    public static void showProfileAuthDialog(final WebActivity activity) {
        LayoutInflater inflaterAlertDialog = LayoutInflater.from(activity);
        View dialogLayout = inflaterAlertDialog.inflate(R.layout.alert_dialog_profile_auth, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(dialogLayout);

        final AlertDialog alertDialog = builder.create();

        alertDialog.show();

        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                activity.changeProfileIconColor(R.color.mainOrange);
            }
        });

        TextView btnProfile = (TextView) dialogLayout.findViewById(R.id.btnProfile);
        TextView btnMyPosts = (TextView)dialogLayout.findViewById(R.id.btnMyPosts);
        TextView btnMyBookmarks = (TextView)dialogLayout.findViewById(R.id.btnMyBookmarks);
        LinearLayout btnSettings = (LinearLayout) dialogLayout.findViewById(R.id.btnSettingsLayout);
        LinearLayout btnLogout = (LinearLayout)dialogLayout.findViewById(R.id.btnLogoutLayout);

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.openPageById("profile");
                alertDialog.hide();
            }
        });

        btnMyPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.openPageById("myPosts");
                alertDialog.hide();
            }
        });

        btnMyBookmarks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.openPageById("myBookmarks");
                alertDialog.hide();
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.openPageById("settings");
                alertDialog.hide();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.openPageById("logout");
                alertDialog.hide();
            }
        });
    }

    public static void showProfileAnonDialog(final WebActivity activity) {
        LayoutInflater inflaterAlertDialog = LayoutInflater.from(activity);
        View dialogLayout = inflaterAlertDialog.inflate(R.layout.alert_dialog_profile_anon, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(dialogLayout);

        alertDialog = builder.create();
        alertDialog.show();

        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                activity.changeProfileIconColor(R.color.mainOrange);
            }
        });

        TextView btnRegister = (TextView) dialogLayout.findViewById(R.id.btnRegister);
        TextView btnSignIn = (TextView) dialogLayout.findViewById(R.id.btnSignIn);
        LinearLayout btnSignInGoogle = (LinearLayout) dialogLayout.findViewById(R.id.btnSignInGoogleLayout);
        LinearLayout btnSignInFacebook = (LinearLayout) dialogLayout.findViewById(R.id.btnSignInGoogleFacebookLayout);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.openPageById("register");
                alertDialog.dismiss();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                alertDialog = null;
                activity.openPageById("signIn");
            }
        });

        btnSignInGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.openPageById("signInGoogle");
                //alertDialog.hide();
            }
        });

        btnSignInFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.openPageById("signInFacebook");
            }
        });
    }

    public static void showLeaveAppDialog(final WebActivity activity) {
        LayoutInflater inflaterAlertDialog = LayoutInflater.from(activity);
        View dialogLayout = inflaterAlertDialog.inflate(R.layout.alert_dialog_leave_app, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(dialogLayout);

        final AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();

        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                dialogInterface.dismiss();
            }
        });

        Button leaveButton = (Button) dialogLayout.findViewById(R.id.leaveButton);
        Button cancelButton = (Button) dialogLayout.findViewById(R.id.cancelButton);

        leaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.goPreviousActivity();
                //activity.onBackPressed();
                //alertDialog.hide();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    public static void showErrorDialog(final AuthActivity activity) {
        LayoutInflater inflaterAlertDialog = LayoutInflater.from(activity);
        final View dialogLayout = inflaterAlertDialog.inflate(R.layout.alert_dialog_error, null);

        //((TextView)dialogLayout.findViewById(R.id.alert_dialog_title)).setText(title);
        //((TextView)dialogLayout.findViewById(R.id.alert_dialog_message)).setText(message);


        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(dialogLayout);

        final AlertDialog alertDialog = builder.create();
        //alertDialog.setCancelable(false);
        //alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                dialogInterface.dismiss();
            }
        });

        Button buttonAlertDialogConfirm = (Button) dialogLayout.findViewById(R.id.btnOk);

        buttonAlertDialogConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.hide();
            }
        });
    }

    public static void showFacebookSignInDialog(final AuthActivity activity) {
        LayoutInflater inflaterAlertDialog = LayoutInflater.from(activity);
        View dialogLayout = inflaterAlertDialog.inflate(R.layout.alert_dialog_profile_anon, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(dialogLayout);

        alertDialog = builder.create();

        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        alertDialog.show();

        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                dialogInterface.dismiss();
            }
        });

        setLoadingIndicator(true);
    }


    public static void setLoadingIndicator(boolean active, ProgressBar progressBar, ScrollView contentLayout) {
        if (active) {
            progressBar.setVisibility(View.VISIBLE);
            contentLayout.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            contentLayout.setVisibility(View.VISIBLE);
        }
    }

    public static void showError(AuthException exception) {
        if(alertDialog != null) {
            ProgressBar progressbar = (ProgressBar) alertDialog.findViewById(R.id.loading);
            ScrollView signInLayout = (ScrollView) alertDialog.findViewById(R.id.signInLayout);
            TextView message;

            switch(exception.getType()) {
                case "login":
                    alertDialog.setCancelable(true);
                    alertDialog.setCanceledOnTouchOutside(true);

                    message = (TextView) alertDialog.findViewById(R.id.errorMessage);

                    message.setText(/*"Incorrect Login and Password"*/exception.getMessage());

                    if(!exception.getAction().equals("UNKNOWN_HOST")) {
                        EditText signInInputUsername = (EditText) alertDialog.findViewById(R.id.signInInputUsername);
                        EditText signInInputPassword = (EditText) alertDialog.findViewById(R.id.signInInputPassword);

                        TextView signInInputPasswordForgot = (TextView) alertDialog.findViewById(R.id.signInInputPasswordForgot);

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

                    message = (TextView) alertDialog.findViewById(R.id.message);

                    if(!exception.getAction().equals("UNKNOWN_HOST")) {
                        EditText forgotPasswordInputEmail = (EditText) alertDialog.findViewById(R.id.forgotPasswordInputEmail);

                        forgotPasswordInputEmail.setHintTextColor(ContextCompat.getColor(alertDialog.getContext(), R.color.red));
                        forgotPasswordInputEmail.setBackgroundResource(R.drawable.border_bottom_1dp_error);

                        message.setText("The information submitted could not be found.");
                    } else {
                        message.setText(exception.getMessage());
                    }

                    message.setVisibility(View.VISIBLE);
                    progressbar.setVisibility(View.GONE);
                    signInLayout.setVisibility(View.VISIBLE);
                    break;
                case "signInFacebook":
                    ((ScrollView) alertDialog.findViewById(R.id.signInLayout)).setVisibility(View.GONE);
                    ((ProgressBar) alertDialog.findViewById(R.id.loading)).setVisibility(View.GONE);
                    ((TextView) alertDialog.findViewById(R.id.message)).setVisibility(View.VISIBLE);
                    alertDialog.setCancelable(true);
                    alertDialog.setCanceledOnTouchOutside(true);
                    break;
            }
        }
    }

    public static void showSuccess(String type) {
        if(alertDialog != null) {
            switch(type) {
                case "forgotPassword":
                    ProgressBar progressbar = (ProgressBar) alertDialog.findViewById(R.id.loading);
                    ScrollView signInLayout = (ScrollView) alertDialog.findViewById(R.id.signInLayout);

                    TextView message = (TextView) alertDialog.findViewById(R.id.message);
                    LinearLayout forgotPasswordForm = (LinearLayout) alertDialog.findViewById(R.id.forgotPasswordForm);

                    message.setText("Your password has been sent successfully to your original e-mail address.");
                    message.setTextColor(ContextCompat.getColor(alertDialog.getContext(), R.color.grey_A50));

                    message.setVisibility(View.VISIBLE);
                    forgotPasswordForm.setVisibility(View.GONE);
                    progressbar.setVisibility(View.GONE);
                    signInLayout.setVisibility(View.VISIBLE);

                    alertDialog.setCancelable(true);
                    alertDialog.setCanceledOnTouchOutside(true);
                    break;
            }
        }
    }

    public static void setLoadingIndicator(boolean loading) {
        if(alertDialog != null) {
            final ProgressBar progressbar = (ProgressBar) alertDialog.findViewById(R.id.loading);
            final ScrollView signInLayout = (ScrollView) alertDialog.findViewById(R.id.signInLayout);

            if(loading) {
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

}