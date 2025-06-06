package com.deepinspire.gmatclub.auth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.deepinspire.gmatclub.GCConfig;
import com.deepinspire.gmatclub.R;
import com.deepinspire.gmatclub.api.Api;
import com.deepinspire.gmatclub.api.AuthException;
import com.deepinspire.gmatclub.utils.FieldWatcher;
import com.deepinspire.gmatclub.utils.Storage;
import com.deepinspire.gmatclub.utils.Validator;
import com.deepinspire.gmatclub.utils.ViewHelper;
import com.deepinspire.gmatclub.web.WebActivity;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.deepinspire.gmatclub.GCConfig.GOOGLE;

public class LoginActivity extends AppCompatActivity implements ILoginContract.View, View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    public static String LOGIN_TAG = LoginActivity.class.getSimpleName();
    private ILoginContract.Presenter presenter;

    private static CallbackManager callbackManager;

    private GoogleSignInClient mGoogleSignInClient;

    private GoogleApiClient mGoogleApiClient;

    private FirebaseAuth mAuth;

    private ProgressBar progressbar;
    private ScrollView signInLayout;

    private TextView signInInputPasswordForgot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPresenter(new LoginPresenter(getApplication(), this));

        setContentView(R.layout.activity_login);


        progressbar = (ProgressBar) findViewById(R.id.loading);
        signInLayout = (ScrollView) findViewById(R.id.signInLayout);

        signInInputPasswordForgot = (TextView) findViewById(R.id.signInInputPasswordForgot);
        signInInputPasswordForgot.setOnClickListener(this);
        View btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        btnGoogleLogin.setOnClickListener(v -> googleLoginAction());
        LinearLayout signInButtonLayout = (LinearLayout) findViewById(R.id.signInButtonLayout);
        signInButtonLayout.setOnClickListener(this);

        LinearLayout signInButtonFacebookLayout = (LinearLayout) findViewById(R.id.signInButtonFacebookLayout);
        signInButtonFacebookLayout.setOnClickListener(this);

        setLoadingIndicator(false);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (presenter.logged(this)) {
            Intent intent = new Intent(this, WebActivity.class);

            intent.setData(Uri.parse(Api.FORUM_URL));

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
        }/* else {
            //initGoogleSignIn();

            //mGoogleApiClient.disconnect();
            //

            //Auth.GoogleSignInApi.signOut(mGoogleApiClient);
            //mGoogleSignInClient.signOut();
                    /*.addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // ...
                        }
                    });*/

        //mGoogleSignInClient.revokeAccess();
                    /*.addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // ...
                        }
                    });*/
        // }
    }

    void googleLoginAction() {
       /* if(true)
            throw new RuntimeException("TEST!!!");

        */
        initGoogleSignIn();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GCConfig.GOOGLE_SIGN_IN);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layoutSignInGoogle:
                initGoogleSignIn();
                //Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, GCConfig.GOOGLE_SIGN_IN);
                break;
            case R.id.layoutSignInFacebook:
            case R.id.signInButtonFacebookLayout:
                signInFacebook();
                break;
            case R.id.layoutSignIn:
                if (presenter.availableAuth()) {
                    openWebSite(Api.FORUM_URL);
                } else {
                    ViewHelper.showResetPasswordDialog(LoginActivity.this);
                }
                break;
            case R.id.layoutViewAsGuest:
                openWebSite(Api.FORUM_URL);
                break;
            case R.id.layoutRegister:
                openWebSite(Api.FORUM_REGISTER_URL);
                break;
            case R.id.signInInputPasswordForgot:
                ViewHelper.showForgotPasswordDialog(LoginActivity.this);
                break;
            case R.id.signInButtonLayout:
                startProccessAuth();
                break;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        InputMethodManager manager = ((InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE));
        if (manager != null)
            manager.hideSoftInputFromWindow((this.getWindow().getDecorView().getApplicationWindowToken()), 0);

        return super.dispatchTouchEvent(ev);
    }

    public void signIn(String login, String password) {
        Storage.saveLoginEmail(getApplicationContext(), login);
        Storage.saveLoginPassword(getApplicationContext(), password);
        presenter.signIn(this, login, password);
    }

    public void signInFacebook() {
        //ViewHelper.showFacebookSignInDialog(LoginActivity.this);

        ViewHelper.setLoadingIndicator(true);

        callbackManager = CallbackManager.Factory.create();
        LoginManager manager = LoginManager.getInstance();
        if (manager != null) {
            manager.logOut();

            manager.registerCallback(callbackManager,
                    new FacebookCallback<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult loginResult) {
                            AccessToken token = AccessToken.getCurrentAccessToken();

                            if (token != null) {
                                String idToken = token.getToken();//token.getUserId();
                                String accessToken = token.getToken();

                                long expiresIn = token.getExpires().getTime();

                                callbackManager = null;

                                Storage.saveFacebookIdToken(getApplicationContext(), idToken);
                                Storage.saveFacebookAccessToken(getApplicationContext(), accessToken);
                                presenter.signIn(LoginActivity.this, "facebook", idToken, accessToken, Long.toString(expiresIn));
                            }
                        }

                        @Override
                        public void onCancel() {
                        }

                        @Override
                        public void onError(FacebookException exception) {
                            AuthException ex = new AuthException(new Exception("Failed sign in facebook"), "signInFacebook");
                            ViewHelper.showError(ex);
                        }
                    });

            manager.logInWithReadPermissions(LoginActivity.this, Collections.singletonList("public_profile"));
        }
    }

    public void forgotPassword(String email) {
        presenter.forgotPassword(email);
    }

    public void resetPassword(String email) {
        presenter.resetPassword(email);
    }

    public void setPresenter(ILoginContract.Presenter presenter) {
        this.presenter = presenter;
    }

    public void openWebSite(String url) {
        Intent intent;
        intent = new Intent(LoginActivity.this, WebActivity.class);
        intent.setData(Uri.parse(url));
        startActivity(intent);

        if (ViewHelper.alertDialog != null) {
            ViewHelper.alertDialog.dismiss();
            ViewHelper.alertDialog = null;
        }
    }

    public void showSuccess(String type) {
        ViewHelper.showSuccess(type);
    }

    public void showError(AuthException exception) {
        if ("showForgotPassword".equals(exception.getAction())) {
            setLoadingIndicator(false);
            ViewHelper.showResetPasswordDialog(LoginActivity.this);
        } else {
            showErrorUI(exception);
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FacebookSdk.getCallbackRequestCodeOffset()) {
            if (callbackManager != null) {
                callbackManager.onActivityResult(requestCode, resultCode, data);
            }
        } else if (resultCode == Activity.RESULT_OK && requestCode == GCConfig.GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    /*
        public void parseToken(Intent data) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {
                final GoogleSignInAccount account = result.getSignInAccount();

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String scope = "oauth2:" + Scopes.EMAIL + " " + Scopes.PROFILE;
                            String accessToken = GoogleAuthUtil.getToken(getApplicationContext(), account.getAccount(), scope, new Bundle());

                            String idToken = account.getIdToken();
                            Long expiresIn = (new Date()).getTime() + account.getExpirationTimeSecs();

                            presenter.signIn(LoginActivity.this, "google", accessToken, accessToken, String.valueOf(expiresIn));
                            presenter.signIn(LoginActivity.this, "google", idToken, idToken, String.valueOf(expiresIn));
                            presenter.signIn(LoginActivity.this, "google", idToken, accessToken, String.valueOf(expiresIn));
                            presenter.signIn(LoginActivity.this, "google", accessToken, accessToken, String.valueOf(expiresIn));

                            //presenter.getTokenInfo(idToken);
                            //presenter.getTokenInfo(accessToken);

                            Log.d("TOKEN", "accessToken:" + accessToken); //accessToken:ya29.Gl...

                        } catch (IOException | GoogleAuthException e) {
                            e.printStackTrace();
                        }
                    }
                };
                AsyncTask.execute(runnable);

            }
        }
    */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            if (account != null) fireBaseAuthWithGoogle(account);

        } catch (ApiException e) {
            String message = e.getMessage();
            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
        }
    }

    private void fireBaseAuthWithGoogle(final GoogleSignInAccount acct) {

        final AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        //final String t = acct.getIdToken();
        mAuth = FirebaseAuth.getInstance();
        if (mAuth != null) {
            mAuth.signOut();


            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        FirebaseUser mUser = mAuth.getCurrentUser();
                        if (task.isSuccessful() && mUser != null) {
                            mUser.getIdToken(true)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            Runnable runnable = () -> {
                                                try {
                                                    String scope = "oauth2:" + Scopes.EMAIL + " " + Scopes.PROFILE;
                                                    String accessToken = GoogleAuthUtil.getToken(getApplicationContext(), acct.getAccount(), scope, new Bundle());

                                                    String idToken = acct.getIdToken();
                                                    Long expiresIn = (new Date()).getTime() ;//+ acct.getExpirationTimeSecs();
                                                    Storage.saveGoogleIdToken(getApplicationContext(), idToken);
                                                    Storage.saveGoogleAccessToken(getApplicationContext(), accessToken);
                                                    presenter.signInUseGoogleAccount(LoginActivity.this, GOOGLE, idToken, accessToken, String.valueOf(expiresIn));

                                                } catch (IOException | GoogleAuthException e) {
                                                    e.printStackTrace();
                                                }
                                            };
                                            AsyncTask.execute(runnable);

                                        } else {
                                            Log.w(LOGIN_TAG, task1.getException());
                                        }
                                    });
                        } else {
                            Log.w(LOGIN_TAG, task.getException());
                        }
                    });
        }
    }

    /*public void getTokenInfo() {
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormEncodingBuilder()
                .add("grant_type", "authorization_code")
                .add("client_id", "<Your-client-id>")   // something like : ...apps.googleusercontent.com
                .add("client_secret", "{Your-client-secret}")
                .add("redirect_uri","")
                .add("code", "4/4-GMMhmHCXhWEzkobqIHGG_EnNYYsAkukHspeYUk9E8") // device code.
                .add("id_token", idTokenString) // This is what we received in Step 5, the jwt token.
                .build();

        final Request request = new Request.Builder()
                .url("https://www.googleapis.com/oauth2/v4/token")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Request request, final IOException e) {
                Log.e("FAILE", e.toString());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    final String message = jsonObject.toString(5);
                    Log.i(LOG_TAG, message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }*/

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /*public void validateIdToken() {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                // Specify the CLIENT_ID of the app that accesses the backend:
                .setAudience(Collections.singletonList(CLIENT_ID))
                // Or, if multiple clients access the backend:
                //.setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
                .build();

        // (Receive idTokenString by HTTPS POST)

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            Payload payload = idToken.getPayload();

            // Print user identifier
            String userId = payload.getSubject();
            System.out.println("User ID: " + userId);

            // Get profile information from payload
            String email = payload.getEmail();
            boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            String locale = (String) payload.get("locale");
            String familyName = (String) payload.get("family_name");
            String givenName = (String) payload.get("given_name");

            // Use or store profile information
            // ...

        } else {
            System.out.println("Invalid ID token.");
        }
    }*/

    private void initGoogleSignIn() {
        if (mGoogleApiClient == null) {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
        }
    }

    private void setLoadingIndicator(boolean loading) {
        if (loading) {
            progressbar.setVisibility(View.VISIBLE);
            signInLayout.setVisibility(View.GONE);
        } else {
            progressbar.setVisibility(View.GONE);
            signInLayout.setVisibility(View.VISIBLE);
        }
    }

    public void showErrorUI(AuthException exception) {
        ProgressBar progressbar = (ProgressBar) findViewById(R.id.loading);
        ScrollView signInLayout = (ScrollView) findViewById(R.id.signInLayout);
        TextView message;

        switch (exception.getType()) {
            case "login":
                message = (TextView) findViewById(R.id.errorMessage);

                message.setText(/*"Incorrect Login and Password"*/exception.getMessage());

                if (!exception.getAction().equals("UNKNOWN_HOST")) {
                    EditText signInInputUsername = (EditText) findViewById(R.id.signInInputUsername);
                    EditText signInInputPassword = (EditText) findViewById(R.id.signInInputPassword);

                    TextView signInInputPasswordForgot = (TextView) findViewById(R.id.signInInputPasswordForgot);

                    signInInputUsername.setHintTextColor(ContextCompat.getColor(LoginActivity.this, R.color.red));
                    signInInputUsername.setBackgroundResource(R.drawable.border_bottom_1dp_error);

                    signInInputPassword.setHintTextColor(ContextCompat.getColor(LoginActivity.this, R.color.red));
                    signInInputPassword.setBackgroundResource(R.drawable.border_bottom_1dp_error);

                    signInInputPasswordForgot.setTextColor(ContextCompat.getColor(LoginActivity.this, R.color.red));
                }

                message.setVisibility(View.VISIBLE);
                progressbar.setVisibility(View.GONE);
                signInLayout.setVisibility(View.VISIBLE);
                break;
            case "signInFacebook":
                ((ScrollView) findViewById(R.id.signInLayout)).setVisibility(View.GONE);
                ((ProgressBar) findViewById(R.id.loading)).setVisibility(View.GONE);
                if (findViewById(R.id.message) != null)
                    ((TextView) findViewById(R.id.message)).setVisibility(View.VISIBLE);
                break;
        }
    }

    private void startProccessAuth() {
        TextView errorMessage = (TextView) findViewById(R.id.errorMessage);

        EditText signInInputUsername = (EditText) findViewById(R.id.signInInputUsername);
        EditText signInInputPassword = (EditText) findViewById(R.id.signInInputPassword);

        signInInputUsername.addTextChangedListener(new FieldWatcher(signInInputUsername, LoginActivity.this));
        signInInputPassword.addTextChangedListener(new FieldWatcher(signInInputPassword, LoginActivity.this));

        String username = signInInputUsername.getText().toString();
        String password = signInInputPassword.getText().toString();

        boolean validUsername = Validator.validUsername(username);
        boolean validPassword = Validator.validPassword(password);

        if (validUsername && validPassword) {
            errorMessage.setVisibility(View.GONE);
            errorMessage.setText("");

            signInInputUsername.setHintTextColor(ContextCompat.getColor(getApplicationContext(), R.color.grey_A50));
            signInInputUsername.setBackgroundResource(R.drawable.border_bottom_1dp);

            signInInputPassword.setHintTextColor(ContextCompat.getColor(getApplicationContext(), R.color.grey_A50));
            signInInputPassword.setBackgroundResource(R.drawable.border_bottom_1dp);

            signInInputPasswordForgot.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.grey_A50));

            setLoadingIndicator(true);

            signIn(username, password);
        } else {
            Map<String, String> messages = new HashMap<String, String>() {{
                put("login", "Incorrect Login");
                put("password", "Incorrect Password");
                put("loginpassword", "Incorrect Login and Password");
            }};

            StringBuilder keyMessageError = new StringBuilder();

            if (!validUsername) {
                keyMessageError.append("login");

                signInInputUsername.setBackgroundResource(R.drawable.border_bottom_1dp_error);

                if (signInInputUsername.requestFocus()) {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }

            if (!validPassword) {
                keyMessageError.append("password");

                signInInputPassword.setBackgroundResource(R.drawable.border_bottom_1dp_error);

                signInInputPasswordForgot.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));

                if (signInInputPassword.requestFocus()) {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }

            String message = messages.get(keyMessageError.toString());

            if (message != null) {
                errorMessage.setText(message);
                errorMessage.setVisibility(View.VISIBLE);
            }
        }
    }
}