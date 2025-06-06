package com.deepinspire.gmatclub.auth;

import static com.deepinspire.gmatclub.notifications.Notifications.INPUT_URL;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.deepinspire.gmatclub.GCConfig;
import com.deepinspire.gmatclub.R;
import com.deepinspire.gmatclub.api.Api;
import com.deepinspire.gmatclub.api.AuthException;
import com.deepinspire.gmatclub.utils.Storage;
import com.deepinspire.gmatclub.utils.ViewHelper;
import com.deepinspire.gmatclub.web.WebActivity;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Date;

public class AuthActivity extends AppCompatActivity implements IAuthContract.View, View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {
    private IAuthContract.Presenter presenter;

    private CallbackManager callbackManager;

    private GoogleSignInClient mGoogleSignInClient;

    private GoogleApiClient mGoogleApiClient;

    private FirebaseAuth mAuth;

    private String url;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null)
            url = getIntent().getStringExtra(INPUT_URL);
        setPresenter(new AuthPresenter(this.getApplication(), this));

        setContentView(R.layout.activity_auth);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ((LinearLayout) findViewById(R.id.layoutSignInGoogle)).setOnClickListener(this);
        ((LinearLayout) findViewById(R.id.layoutSignInFacebook)).setOnClickListener(this);
        ((LinearLayout) findViewById(R.id.layoutSignIn)).setOnClickListener(this);
        ((LinearLayout) findViewById(R.id.layoutViewAsGuest)).setOnClickListener(this);
        ((LinearLayout) findViewById(R.id.layoutRegister)).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (presenter.logged(this)) {
            Intent intent = new Intent(this, WebActivity.class);

            intent.setData(Uri.parse(Api.FORUM_URL));

            intent.putExtra(INPUT_URL, url);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(AuthActivity.this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        200
                );
            }
            if (!TextUtils.isEmpty(Storage.getGoogleIdToken(getApplicationContext())) &&
                    !TextUtils.isEmpty(Storage.getGoogleAccessToken(getApplicationContext()))) {
                Long expiresIn = (new Date()).getTime() + 432000000;
                presenter.signIn(this, "google", Storage.getGoogleIdToken(getApplicationContext()), Storage.getGoogleAccessToken(getApplicationContext()), String.valueOf(expiresIn));
            }

            if (!TextUtils.isEmpty(Storage.getFacebookIdToken(getApplicationContext())) &&
                    !TextUtils.isEmpty(Storage.getFacebookAccessToken(getApplicationContext()))) {
                Long expiresIn = (new Date()).getTime() + 432000000;
                presenter.signIn(this, "facebook", Storage.getFacebookIdToken(getApplicationContext()), Storage.getFacebookAccessToken(getApplicationContext()), String.valueOf(expiresIn));
            }

            if (!TextUtils.isEmpty(Storage.getLoginEmail(getApplicationContext())) &&
                    !TextUtils.isEmpty(Storage.getLoginPassword(getApplicationContext()))) {
                presenter.signIn(this, Storage.getLoginEmail(getApplicationContext()), Storage.getLoginPassword(getApplicationContext()));
            }
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
        }
    }


    @Override
    public void onClick(View view) {
        Intent intent;

        switch (view.getId()) {
            case R.id.layoutSignInGoogle:
                initGoogleSignIn();

                //Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();

                startActivityForResult(signInIntent, GCConfig.GOOGLE_SIGN_IN);
                break;
            case R.id.layoutSignInFacebook:
                signInFacebook();
                break;
            case R.id.layoutSignIn:
                openLoginPage();
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
        ((InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow((this.getWindow().getDecorView().getApplicationWindowToken()), 0);

        return super.dispatchTouchEvent(ev);
    }

    public void signIn(String login, String password) {
        presenter.signIn(this, login, password);
    }

    public void signInFacebook() {
       /* ViewHelper.showFacebookSignInDialog(AuthActivity.this);

        ViewHelper.setLoadingIndicator(true);

        callbackManager = CallbackManager.Factory.create();

        if(LoginManager.getInstance() != null){
            LoginManager.getInstance().logOut();
        }

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        AccessToken token = AccessToken.getCurrentAccessToken();

                        if(token != null) {
                            String idToken = token.getToken();//token.getUserId();
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
    */
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
        intent.putExtra(INPUT_URL, url);
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
        switch (exception.getAction()) {
            case "showForgotPassword":
                if (ViewHelper.alertDialog != null) {
                    ViewHelper.alertDialog.dismiss();
                }
                ViewHelper.showForgotPasswordDialog(AuthActivity.this);
                break;
            default: {
                ViewHelper.showError(exception);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == FacebookSdk.getCallbackRequestCodeOffset()) {
            if (callbackManager != null) {
                callbackManager.onActivityResult(requestCode, resultCode, data);
            }
        } else {
            switch (requestCode) {
                case GCConfig.GOOGLE_SIGN_IN:
                    //GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                    //GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                    //GoogleSignInAccount acct = result.getSignInAccount();
                    //String authCode = acct.getServerAuthCode();
                    //Long expiresIn  = (new Date()).getTime() + acct.getExpirationTimeSecs();
                    //presenter.signIn("google", authCode, authCode, String.valueOf(expiresIn));
                    //Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    //handleSignInResult(task);
                    //GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                    //GoogleSignInResult result1 = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                    // parseToken(data);

                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    handleSignInResult(task);
                    break;
            }
        }
    }
/*
    public void parseToken(Intent data) {
        final GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

        if (result != null && result.isSuccess()) {
            final GoogleSignInAccount account = result.getSignInAccount();

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        String scope = "oauth2:" + Scopes.EMAIL + " " + Scopes.PROFILE;
                        String accessToken = GoogleAuthUtil.getToken(getApplicationContext(), account.getAccount(), scope, new Bundle());

                        String idToken = account.getIdToken();

                        Long expiresIn = (new Date()).getTime() + account.getExpirationTimeSecs();

                        presenter.signIn(AuthActivity.this, "google", accessToken, accessToken, String.valueOf(expiresIn));
                        presenter.signIn(AuthActivity.this, "google", idToken, idToken, String.valueOf(expiresIn));
                        presenter.signIn(AuthActivity.this, "google", idToken, accessToken, String.valueOf(expiresIn));
                        presenter.signIn(AuthActivity.this, "google", accessToken, accessToken, String.valueOf(expiresIn));

                        //presenter.getTokenInfo(idToken);
                        //presenter.getTokenInfo(accessToken);

                        Log.d("TOKEN", "accessToken:" + accessToken); //accessToken:ya29.Gl...

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (GoogleAuthException e) {
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

            if (account != null) {
                //String id  = account.getId();
                //String email = account.getEmail();
                //Set<Scope> scope = account.getRequestedScopes();

                //String idToken = account.getIdToken();
                //Long expiresIn  = (new Date()).getTime() + account.getExpirationTimeSecs();

                //String credential = GoogleAuthUtil.getToken(getApplicationContext(), (Account) account, null);

                // String accessToken = GoogleAuthUtil.getToken(getApplicationContext(), account.getAccount(), scope, new Bundle());

                //Toast.makeText(getApplicationContext(), idToken, Toast.LENGTH_LONG).show();

                //presenter.signIn("google", idToken, idToken, String.valueOf(expiresIn));

                //presenter.getTokenInfo(idToken);

                firebaseAuthWithGoogle(account);

            }
        } catch (ApiException e) {
            String message = e.getMessage();
            Toast.makeText(AuthActivity.this, message, Toast.LENGTH_LONG).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        //Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        mAuth = FirebaseAuth.getInstance();

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                Task<GetTokenResult> idToken = user.getIdToken(false);
                                Task<GetTokenResult> idToken1 = user.getIdToken(true);

                               // FirebaseUser user1 = mAuth.getCurrentUser();

                                FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

                                mUser.getIdToken(true)
                                        .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                            public void onComplete(@NonNull Task<GetTokenResult> task) {
                                                if (task.isSuccessful()) {
                                                    String idToken = task.getResult().getToken();

                                                    Long expiresIn = (new Date()).getTime() + task.getResult().getExpirationTimestamp();

                                                    presenter.signIn(AuthActivity.this, "google", task.getResult().getToken(), task.getResult().getToken(), String.valueOf(expiresIn));

                                                    // Send token to your backend via HTTPS
                                                    // ...
                                                } else {
                                                    // Handle error -> task.getException();
                                                }
                                            }
                                        });
                            }
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "signInWithCredential:failure", task.getException());
                            //Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
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
    public void onConnectionFailed(ConnectionResult connectionResult) {
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

    private GoogleApiClient initGoogleSignIn() {
        if (mGoogleApiClient == null) {
            // mGoogleApiClient.connect();

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    //.requestIdToken("241911688286-hdgjh2o7dg42155d31ts4m9vitq8nf0h.apps.googleusercontent.com")
                    .requestIdToken(getString(R.string.default_web_client_id))
                    //.requestIdToken("241911688286-hdgjh2o7dg42155d31ts4m9vitq8nf0h.apps.googleusercontent.com")
                    //.requestIdToken("241911688286-p0000000hdgjh2o7dg42155d31ts4m9vitq8nf0h.apps.googleusercontent.com")
                    //.requestIdToken("789008364480-jr2io8r51h0eegmdvuu0bv1abt6bpppt.apps.googleusercontent.com")
                    //.requestServerAuthCode("789008364480-jr2io8r51h0eegmdvuu0bv1abt6bpppt.apps.googleusercontent.com")
                    //.requestIdToken(getString(R.string.server_client_id))
                    //.requestProfile()
                    //.requestId()
                    //.requestIdToken("241911688286-hdgjh2o7dg42155d31ts4m9vitq8nf0h.apps.googleusercontent.com")
                    //.requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    //.requestScopes(new Scope(Scopes.PLUS_LOGIN))
                    //.requestProfile()
                    .build();

                /*mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .enableAutoManage(this, this)
                        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                        .build();*/

            //mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

            //Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            //startActivityForResult(signInIntent, GCConfig.GOOGLE_SIGN_IN);

            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
        }

        return mGoogleApiClient;
    }

    private void openLoginPage() {
        Intent intent = new Intent(this, LoginActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
    }
}