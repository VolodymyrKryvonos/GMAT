package com.deepinspire.gmatclub.auth;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.deepinspire.gmatclub.GCConfig;
import com.deepinspire.gmatclub.R;
import com.deepinspire.gmatclub.api.Api;
import com.deepinspire.gmatclub.api.AuthException;
import com.deepinspire.gmatclub.utils.ViewHelper;
import com.deepinspire.gmatclub.web.WebActivity;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;

import java.util.Arrays;

public class AuthActivity extends AppCompatActivity implements IAuthContract.View, View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {
    private IAuthContract.Presenter presenter;

    private CallbackManager callbackManager;

    private GoogleSignInClient mGoogleSignInClient;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPresenter(new AuthPresenter(this.getApplication(), this));

        setContentView(R.layout.activity_auth);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ((LinearLayout) findViewById(R.id.layoutSignInGoogle)).setOnClickListener(this);
        ((LinearLayout) findViewById(R.id.layoutSignInFacebook)).setOnClickListener(this);
        ((LinearLayout) findViewById(R.id.layoutSignIn)).setOnClickListener(this);
        ((LinearLayout) findViewById(R.id.layoutViewAsGuest)).setOnClickListener(this);
        ((LinearLayout) findViewById(R.id.layoutRegister)).setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("241911688286-hdgjh2o7dg42155d31ts4m9vitq8nf0h.apps.googleusercontent.com")
                //.requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                //.requestProfile()
                .build();

        /*mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();*/

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    protected void onStart() {
        super.onStart();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent;

        switch(view.getId()) {
            case R.id.layoutSignInGoogle:
               // mGoogleApiClient.connect();
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, GCConfig.GOOGLE_SIGN_IN);
                break;
            case R.id.layoutSignInFacebook:
                ViewHelper.showFacebookSignInDialog(AuthActivity.this);

                ViewHelper.setLoadingIndicator(true);

                callbackManager = CallbackManager.Factory.create();

                LoginManager.getInstance().registerCallback(callbackManager,
                        new FacebookCallback<LoginResult>() {
                            @Override
                            public void onSuccess(LoginResult loginResult) {
                                AccessToken token = AccessToken.getCurrentAccessToken();

                                if(token != null) {
                                    String idToken = token.getUserId();
                                    String accessToken = token.getToken();

                                    long expiresIn = token.getExpires().getTime();

                                    callbackManager = null;

                                    presenter.signIn("facebook", idToken, accessToken, Long.toString(expiresIn));
                                }
                            }

                            @Override
                            public void onCancel() {
                                if(ViewHelper.alertDialog != null) {
                                    ViewHelper.alertDialog.dismiss();
                                }
                            }

                            @Override
                            public void onError(FacebookException exception) {
                                AuthException ex = new AuthException(new Exception("Failed sign in facebook"), "signInFacebook");
                                ViewHelper.showError(ex);
                            }
                        });

                LoginManager.getInstance().logInWithReadPermissions(AuthActivity.this, Arrays.asList("public_profile"));
                break;
            case R.id.layoutSignIn:
                ViewHelper.showLoginDialog(AuthActivity.this);
                break;
            case R.id.layoutViewAsGuest:
                openWebSite(Api.FORUM_URL);
                break;
            case R.id.layoutRegister:
                openWebSite(Api.FORUM_REGISTER_URL);
                break;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();

        if (view != null && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) && view instanceof EditText && !view.getClass().getName().startsWith("android.webkit.")) {
            int scrcoords[] = new int[2];
            view.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + view.getLeft() - scrcoords[0];
            float y = ev.getRawY() + view.getTop() - scrcoords[1];
            if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom())
                ((InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow((this.getWindow().getDecorView().getApplicationWindowToken()), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    public void signIn(String login, String password) {
        presenter.signIn(login, password);
    }

    public void forgotPassword(String email) {
        presenter.forgotPassword(email);
    }

    public void setPresenter(IAuthContract.Presenter presenter) {
        this.presenter = presenter;
    }

    public void openWebSite(String url) {
        Intent intent;
        intent = new Intent(AuthActivity.this, WebActivity.class);
        intent.setData(Uri.parse(url));
        startActivity(intent);

        if(ViewHelper.alertDialog != null) {
            ViewHelper.alertDialog.dismiss();
            ViewHelper.alertDialog = null;
        }
    }

    public void showSuccess(String type) {
        ViewHelper.showSuccess(type);
    }

    public void showError(AuthException exception) {
        ViewHelper.showError(exception);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode ==  FacebookSdk.getCallbackRequestCodeOffset()) {
            if(callbackManager != null) {
                callbackManager.onActivityResult(requestCode, resultCode, data);
            }
        } else {
            switch(requestCode) {
                case GCConfig.GOOGLE_SIGN_IN:
                    //GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    handleSignInResult(task);
                    break;
            }
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            if(account != null) {
                String email = account.getEmail();
                String idToken = account.getIdToken();
                String id  = account.getId();
            }
        } catch (ApiException e) {
            String message = e.getMessage();
            Toast.makeText(AuthActivity.this, message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}