package com.deepinspire.gmatclub.web;

import android.content.Context;

import com.deepinspire.gmatclub.api.AuthException;
import com.deepinspire.gmatclub.storage.IStorage;
import com.deepinspire.gmatclub.storage.Injection;
import com.deepinspire.gmatclub.storage.Repository;
import com.deepinspire.gmatclub.storage.User;

import java.util.Map;

/**
 * Created by dmytro mytsko on 21.03.18.
 */
public class WebPresenter implements IWebContract.Presenter {
    private static final String TAG = WebPresenter.class.getSimpleName();

    private IWebContract.View view;

    private Repository repository;

    private int countUnwatchedNotifications = 0;
    private int countUnwatchedPMs = 0;

    WebPresenter(Context ctx, IWebContract.View view) {
        this.repository = Injection.getRepository(ctx);

        this.view = view;

        this.countUnwatchedNotifications = 0;
        this.countUnwatchedPMs = 0;
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
        this.repository.updateNotify(countUnwatchedNotifications, new IStorage.ICallbackAuth() {
            @Override
            public void onSuccess() {
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

    public void updatePMs() {
        setCountUnwatchedPMs(0);
    }

    public User getUser() {
        return repository.getUser();
    }
}