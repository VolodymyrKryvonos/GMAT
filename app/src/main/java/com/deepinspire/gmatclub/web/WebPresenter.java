package com.deepinspire.gmatclub.web;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.deepinspire.gmatclub.api.Api;
import com.deepinspire.gmatclub.api.AuthException;
import com.deepinspire.gmatclub.storage.IStorage;
import com.deepinspire.gmatclub.storage.Injection;
import com.deepinspire.gmatclub.storage.Repository;
import com.deepinspire.gmatclub.storage.User;
import com.deepinspire.gmatclub.utils.Storage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by dmytro mytsko on 21.03.18.
 */
public class WebPresenter implements IWebContract.Presenter {
    private static final String TAG = WebPresenter.class.getSimpleName();

    public Context context;

    public static final int ERROR_CONNECT = -2;

    private IWebContract.View view;

    private Repository repository;

    private int countUnwatchedNotifications = 0;
    private int countUnwatchedPMs = 0;

    private String notifications = null;

    private int errorCode = 0;

    WebPresenter(Context ctx, IWebContract.View view) {
        this.repository = Injection.getRepository(ctx);

        this.context = ctx;
        this.view = view;

        this.countUnwatchedNotifications = 0;
        this.countUnwatchedPMs = 0;

        this.errorCode = 0;
    }

    public void start() {}

    public void logout() {
        repository.logout(new IStorage.ICallback() {
            @Override
            public void onSuccess(Map<String, Object> cache) {
                view.logout();
            }

            @Override
            public void onError() {
                AuthException ex = new AuthException(new Exception("Logout failed"), "logout");
                view.showError(ex);
            }
        });
    }

    public boolean logged() {
        return this.repository.logged();
    }

    public void signIn(String username, String password) {
        repository.signIn(username, password, new IStorage.ICallbackAuth() {
            @Override
            public void onSuccess() {
                view.reload();
            }

            @Override
            public void onError(AuthException exception) {
                view.showError(exception);
            }
        });
    }

    public void signIn(String provider, String idToken, String accessToken, String expiresIn) {
        repository.signInSocial(provider, idToken, accessToken, expiresIn, new IStorage.ICallbackAuth() {
            @Override
            public void onSuccess() {
                view.showSuccess("login");
            }

            @Override
            public void onError(AuthException exception) {
                view.showError(exception);
            }
        });
    }

    public void forgotPassword(String email) {
        repository.forgotPassword(email, new IStorage.ICallbackAuth() {
            @Override
            public void onSuccess() {
                view.showSuccess("forgotPassword");
            }

            @Override
            public void onError(AuthException exception) {
                view.showError(exception);
            }
        });
    }

    public void setCountUnwatchedNotifications(int count) {
        this.countUnwatchedNotifications = count;
    }

    public void setCountUnwatchedPMs(int count) {
        this.countUnwatchedPMs = count;
    }

    public int getCountUnwatchedNotifications() {
        return this.countUnwatchedNotifications;
    }

    public int getCountUnwatchedPMs() {
        return this.countUnwatchedPMs;
    }

    public void updateNotify() {
        this.repository.updateNotify(countUnwatchedNotifications, null, new IStorage.ICallbackNotifications() {
            @Override
            public void onSuccess(String notifications) {
                setCountUnwatchedNotifications(0);
                view.openPageById("notifications");
            }
            @Override
            public void onError(AuthException exception) {
                AuthException ex = new AuthException(new Exception(exception.getMessage()), "notification:update");
                view.showError(ex);
            }
        });
    }

    public void updateNotify(String params, final String id) {
        this.repository.updateNotify(countUnwatchedNotifications, params,  new IStorage.ICallbackNotifications() {
            @Override
            public void onSuccess(String notifications) {
                saveNotifications("{\"group_general\": " + notifications + "}");
                updateNotifications(id);
            }
            @Override
            public void onError(AuthException exception) {
                AuthException ex = new AuthException(new Exception(exception.getMessage()), "notification:update");
                view.showError(ex);
            }
        });
    }

    public void setNotifications(String notifications) {
        this.notifications = notifications;
    }

    public void getNotifications() {
        if(this.notifications == null) {
            this.repository.getNotifications(null,  new IStorage.ICallbackNotifications() {
                @Override
                public void onSuccess(String notifications) {
                    saveNotifications("{\"group_general\": " + notifications + "}");
                }
                @Override
                public void onError(AuthException exception) {
                    AuthException ex = new AuthException(new Exception(exception.getMessage()), "notification:update");
                    view.showError(ex);
                }
            });
        } else {
            view.sendNotificationsForPage(notifications);
        }
    }

    public void getChatNotifications() {

            this.repository.getChatNotifications(null,  new IStorage.ICallbackNotifications() {
                @Override
                public void onSuccess(String notifications) {

                    if (notifications != null){
                        String chatNotificationCount = notifications.split(",")[1].replace("]","");
                        view.showChatNotificationCount(chatNotificationCount);
                    }
                }
                @Override
                public void onError(AuthException exception) {
                    AuthException ex = new AuthException(new Exception(exception.getMessage()), "notification:update");
                    view.showError(ex);
                }
            });

    }

    public void updatePMs() {
        setCountUnwatchedPMs(0);
    }

    public User getUser() {
        return repository.getUser();
    }

    private void updateNotifications(String id) {
        try {
            if (this.notifications != null) {
                JSONArray notifications = (new JSONObject(this.notifications)).getJSONArray("group_general");

                for (int i = 0; i < notifications.length(); i++) {
                    JSONObject notification = notifications.getJSONObject(i);

                    if(id == null || (!notification.isNull("id_notify") && notification.getString("id_notify").equals(id))) {
                        if (!notification.isNull("unread")) {
                            notification.put("unread", false);
                        }

                        if (!notification.isNull("unwatched")) {
                            notification.put("unwatched", false);
                        }
                    }
                }

                setNotifications("{\"group_general\": " + notifications.toString() + "}");
            }
        } catch (JSONException exception) {
            /*runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    swipe.setRefreshing(false);
                    setLoadingIndicator(false);
                    Toast.makeText(WebActivity.this, "List of notifications are corrupt. Please go to the page later...", Toast.LENGTH_LONG).show();
                }
            });*/
        }
    }

    public void saveNotifications(String message) {

        try {
            final JSONObject mNotify = new JSONObject(message);

            if(!mNotify.isNull("group_general")) {
                int notificationsUnwatched = 0;
                int notificationsUnread = 0;

                final JSONArray notifications = mNotify.getJSONArray("group_general");

                setNotifications("{\"group_general\": "+   mNotify.getString("group_general")+"}");

                for(int i = 0; i < notifications.length(); i++) {
                    JSONObject notify = notifications.getJSONObject(i);

                    if(notify.getBoolean("unwatched")) {
                        notificationsUnwatched++;
                    }
                    if(notify.getBoolean("unread")) {
                        notificationsUnread++;
                    }
                }

                setCountUnwatchedNotifications(notificationsUnwatched);
                Storage.saveBadgeCount(context, notificationsUnread);
                view.updateUnreadNotification(notificationsUnread);
            }

            if(!mNotify.isNull("privateMessages")) {
                int privateMessagesUnwatched = 0;

                final JSONObject privateMessages = mNotify.getJSONObject("privateMessages");

                if(!privateMessages.isNull("count")) {
                    privateMessagesUnwatched = privateMessages.getInt("count");
                }

                setCountUnwatchedPMs(privateMessagesUnwatched);
            }

            view.updateCountMessages();
        } catch (JSONException e) {
            //Log.e(mContext.getClass().getName(), e.getMessage());
        }
    }

    public boolean checkAccessNetwork() {
        return (repository.isOnline() && (getError() != WebPresenter.ERROR_CONNECT));
    }

    public void setError(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getError() {
        return this.errorCode;
    }

    public void resetError() {
        this.errorCode = 0;
    }

    public boolean availableAuth() {
        return (repository.getUser().getCountFailedAuth() <= Api.AUTH_AVAILABLE_COUNT_FAILED_REQUESTS);
    }
}