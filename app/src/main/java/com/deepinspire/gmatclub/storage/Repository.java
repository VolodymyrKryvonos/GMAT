package com.deepinspire.gmatclub.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.deepinspire.gmatclub.GCConfig;
import com.deepinspire.gmatclub.api.Api;
import com.deepinspire.gmatclub.api.ApiClient;
import com.deepinspire.gmatclub.api.ApiInterface;
import com.deepinspire.gmatclub.api.AuthException;
import com.facebook.login.LoginManager;
import com.google.firebase.iid.FirebaseInstanceId;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by dmytro mytsko on 26.03.18.
 */
public class Repository implements IStorage {
    private static final String TAG = Repository.class.getSimpleName();

    private static final String APP_PREFERENCES = "settings";

    private static Repository INSTANCE = null;

    private Context context = null;

    private User user = null;

    private HashMap<String, Object> cache = new HashMap<>();

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    private boolean cachedUserLogin = false;

    private SharedPreferences sharedPreferences;

    private Repository(Context ctx) {
        context = ctx;

        cachedUserLogin = false;

        user = new User();

        cache.put("user", user);
    }

    public static Repository getInstance(Context ctx) {
        if(INSTANCE == null) {
            INSTANCE = new Repository(ctx);
        }

        return INSTANCE;
    }

    public void signIn(@NonNull final String username, @NonNull final String password,  @NonNull final ICallbackAuth callback) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    LinkedHashMap<String, RequestBody> mp= new LinkedHashMap<>();

                    RequestBody rb;

                    rb = RequestBody.create(MediaType.parse("text/plain"), username);
                    mp.put("username", rb);

                    rb = RequestBody.create(MediaType.parse("text/plain"), password);
                    mp.put("password", rb);

                    rb = RequestBody.create(MediaType.parse("text/plain"), "index.php");
                    mp.put("redirect", rb);

                    rb = RequestBody.create(MediaType.parse("text/plain"), "Login");
                    mp.put("login", rb);

                    ApiInterface apiService = (new ApiClient()).getClient().create(ApiInterface.class);

                    String username = "guest";
                    String password = "GCTesterNew1";

                    String base = username + ":" + password;

                    String authHeader = "Basic " + Base64.encodeToString(base.getBytes(), Base64.NO_WRAP);

                    Call<String> call = apiService.signIn(authHeader, mp);

                    call.enqueue(new retrofit2.Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            if (!response.isSuccessful()) {
                                AuthException exc = new AuthException(new Exception("login or password failed"), "login");
                                callback.onError(exc);
                            } else {
                               if(logged()) {
                                   callback.onSuccess();
                               } else {
                                   AuthException exc = new AuthException(new Exception("login or password failed"), "login");
                                   callback.onError(exc);
                               }
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            AuthException exc = new AuthException(new Exception(t.getMessage()), "login");
                            callback.onError(exc);
                        }
                    });

                } catch (final Exception exception) {
                    AuthException exc = new AuthException(new Exception("login or password failed"), "login");
                    callback.onError(exc);
                }
            }
        });
    }

    public void forgotPassword(@NonNull final String email, @NonNull final ICallbackAuth callback) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    LinkedHashMap<String, RequestBody> mp= new LinkedHashMap<>();

                    RequestBody rb;

                    rb = RequestBody.create(MediaType.parse("text/plain"), email);
                    mp.put("email", rb);

                    rb = RequestBody.create(MediaType.parse("text/plain"), "Submit");
                    mp.put("submit", rb);

                    ApiInterface apiService = (new ApiClient()).getClient().create(ApiInterface.class);

                    String username = "guest";
                    String password = "GCTesterNew1";

                    String base = username + ":" + password;

                    String authHeader = "Basic " + Base64.encodeToString(base.getBytes(), Base64.NO_WRAP);

                    Call<String> call = apiService.forgotPassword(authHeader, mp);

                    call.enqueue(new retrofit2.Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            if (!response.isSuccessful()) {
                                AuthException exc = new AuthException(new Exception("Failed send information for current email"), "forgotPassword");
                                callback.onError(exc);
                            } else {
                                callback.onSuccess();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            AuthException exc = new AuthException(new Exception(t.getMessage()), "forgotPassword");
                            callback.onError(exc);
                        }
                    });

                } catch (final Exception exception) {
                    AuthException exc = new AuthException(new Exception("Failed send information for current email"), "forgotPassword");
                    callback.onError(exc);
                }
            }
        });
    }

    public void updateNotify(@NonNull final int count, @NonNull final ICallbackAuth callback) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, String> params= new HashMap<>();

                    if(count == 0) {
                        params.put("_", Long.toString(new Date().getTime()));
                        params.put("action", "update");
                        params.put("cb", Long.toString(new Date().getTime()));
                        params.put("group", "");
                        params.put("refresh", "0");
                        params.put("type", "all");
                        params.put("unwatched", "0");
                    } else {
                        params.put("_", Long.toString(new Date().getTime()));
                        params.put("action", "update");
                        params.put("cb", Long.toString(new Date().getTime()));
                        params.put("data", "group_general");
                        params.put("group", "group_general");
                        params.put("refresh", "0");
                        params.put("type", "group");
                        params.put("unwatched", "0");
                    }

                    ApiInterface apiService = (new ApiClient()).getClient().create(ApiInterface.class);

                    String username = "guest";
                    String password = "GCTesterNew1";

                    String base = username + ":" + password;

                    String authHeader = "Basic " + Base64.encodeToString(base.getBytes(), Base64.NO_WRAP);

                    Call<ResponseBody> call = apiService.updateNotify(authHeader, params);

                    call.enqueue(new retrofit2.Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (!response.isSuccessful()) {
                                AuthException ex = new AuthException(new Exception("Failed updating notify"), "updateNotify");
                                callback.onError(ex);
                            } else {
                               if(logged()) {
                                   callback.onSuccess();
                               } else {
                                   AuthException ex = new AuthException(new Exception("Login or password failed"), "login");
                                   callback.onError(ex);
                               }
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            AuthException ex = new AuthException(new Exception("Failed updating notify"), "updateNotify");
                            callback.onError(ex);
                        }
                    });

                } catch (final Exception exception) {
                    AuthException ex = new AuthException(new Exception("Failed updating notify"), "updateNotify");
                    callback.onError(ex);
                }
            }
        });
    }

    public void signInSocial(
            @NonNull final String provider,
            @NonNull final String idToken,
            @NonNull final String accessToken,
            @NonNull final String expiresIn,
            @NonNull final ICallbackAuth callback) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, String> params = new HashMap<>();

                    Long uTime = Long.parseLong(expiresIn);

                    Date date = new java.util.Date(uTime);

                    SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");

                    String formattedDate = sdf.format(date);

                    params.put("provider", provider);
                    params.put("token_type", provider);
                    params.put("access_token", accessToken);
                    params.put("expires_in", formattedDate);
                    /*params.put("mode", "login");*/

                    ApiInterface apiService = (new ApiClient()).getClient().create(ApiInterface.class);

                    LinkedHashMap<String, RequestBody> mp = new LinkedHashMap<>();

                    RequestBody rb;

                    rb = RequestBody.create(MediaType.parse("text/plain"), "Login");
                    mp.put("login", rb);

                    String username = "guest";
                    String password = "GCTesterNew1";

                    String base = username + ":" + password;

                    String authHeader = "Basic " + Base64.encodeToString(base.getBytes(), Base64.NO_WRAP);

                    Call<ResponseBody> call = apiService.signInSocial(authHeader, params, mp);

                    call.enqueue(new retrofit2.Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (!response.isSuccessful()) {
                                LoginManager.getInstance().logOut();
                                AuthException ex = new AuthException(new Exception(response.errorBody().toString()), "login");
                                callback.onError(ex);
                            } else {
                               if(logged()) {
                                   callback.onSuccess();
                               } else {
                                   LoginManager.getInstance().logOut();
                                   AuthException ex = new AuthException(new Exception("Login or password failed"), "login");
                                   callback.onError(ex);
                               }
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            LoginManager.getInstance().logOut();
                            AuthException ex = new AuthException(new Exception(t.getMessage()), "login");
                            callback.onError(ex);
                        }
                    });

                } catch (final Exception exception) {
                    LoginManager.getInstance().logOut();
                    AuthException ex = new AuthException(new Exception(exception.getMessage()), "login");
                    callback.onError(ex);
                }
            }
        });
    }

    public void subscribeNotifications(
            @NonNull final String idToken,
            @NonNull final boolean subscribe,
            @NonNull final ICallbackAuth callback
    ) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, String> params = new HashMap<>();

                    params.put("id", idToken);
                    params.put("vendor", "Google");

                    if(!subscribe) {
                        params.put("signout", "1");
                    }

                    ApiInterface apiService = (new ApiClient()).getClient().create(ApiInterface.class);

                    LinkedHashMap<String, RequestBody> mp = new LinkedHashMap<>();

                    RequestBody rb;

                    rb = RequestBody.create(MediaType.parse("text/plain"), idToken);
                    mp.put("id", rb);

                    rb = RequestBody.create(MediaType.parse("text/plain"), "Google");
                    mp.put("vendor", rb);

                    if(!subscribe) {
                        rb = RequestBody.create(MediaType.parse("text/plain"), "1");
                        mp.put("signout", rb);
                    }

                    String username = "guest";
                    String password = "GCTesterNew1";

                    String base = username + ":" + password;

                    String authHeader = "Basic " + Base64.encodeToString(base.getBytes(), Base64.NO_WRAP);

                    Call<ResponseBody> call = apiService.register(authHeader, params, mp);

                    call.enqueue(new retrofit2.Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (!response.isSuccessful()) {
                                AuthException ex = new AuthException(new Exception(response.errorBody().toString()), "register");
                                callback.onError(ex);
                            } else {
                                callback.onSuccess();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            AuthException ex = new AuthException(new Exception(t.getMessage()), "register");
                            callback.onError(ex);
                        }
                    });

                } catch (final Exception exception) {
                    AuthException ex = new AuthException(new Exception(exception.getMessage()), "register");
                    callback.onError(ex);
                }
            }
        });
    }

    public boolean logged() {
        return logged(false);
    }

    public boolean logged(boolean refreshToken) {
        CookieManager webviewCookieManager = CookieManager.getInstance();

        String cookiesString = webviewCookieManager.getCookie(Api.HOME_URL);

        if (cookiesString != null && !cookiesString.isEmpty()) {
            SharedPreferences.Editor editor = getSharedPreferences().edit();
            editor.putString(GCConfig.COOKIES, cookiesString);
            editor.apply();
        } else {
            cookiesString = getSharedPreferences().getString(GCConfig.COOKIES, "");
        }

        String[] cookieHeaders = cookiesString.split(";");

        for(String cookie : cookieHeaders) {
            String[] c = cookie.split("=", 2);

            if(c[0].trim().equals("phpbb3_2oaiz_u")) {
                long userId = Long.parseLong(c[1].trim());
                boolean previouseLogged = user.getLogged();
                boolean currentLogged = 1 < Long.parseLong(c[1].trim());

                user.setId(userId);
                user.setLogged(currentLogged);

                if((currentLogged && currentLogged != previouseLogged) || refreshToken) {
                    String token = FirebaseInstanceId.getInstance().getToken();

                    subscribeNotifications(token,true, new IStorage.ICallbackAuth() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Success subscribe notifications");
                        }

                        @Override
                        public void onError(AuthException exc) {
                            Log.d(TAG, "Error subscribe notifications");
                        }
                    });
                }
            }
        }

        return user.getLogged();
    }

    @SuppressWarnings("deprecation")
    public void logout(@NonNull final ICallback callback) {
        try {
            subscribeNotifications(
                FirebaseInstanceId.getInstance().getToken(), false, new IStorage.ICallbackAuth() {
                    @Override
                    public void onSuccess() {
                        clearInformationForUser();
                        callback.onSuccess(cache);
                    }

                    @Override
                    public void onError(AuthException exc) {
                        callback.onError();
                    }
                });
        } catch(Exception e) {
            callback.onError();
        }
    }

    private void clearInformationForUser() {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.remove(GCConfig.COOKIES);
        editor.apply();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }

        user.setLogged(false);

        cachedUserLogin = false;
    }

    public User getUser() {
        return user;
    }

    private SharedPreferences getSharedPreferences() {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(GCConfig.GMATCLUB, Context.MODE_PRIVATE);
        }
        return sharedPreferences;
    }
}