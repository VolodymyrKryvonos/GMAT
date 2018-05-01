package com.deepinspire.gmatclub.web;

import com.deepinspire.gmatclub.IBasePresenter;
import com.deepinspire.gmatclub.IBaseView;
import com.deepinspire.gmatclub.api.AuthException;
import com.deepinspire.gmatclub.storage.User;

/**
 * Created by dmytro mytsko on 21.03.18.
 */
public interface IWebContract {
    interface View extends IBaseView<Presenter> {
        void showSuccess(String type);
        void showError(AuthException exception);
        void reload();
        void logout();
        void goPreviousActivity();
        void openPageById(String id);
    }

    interface Presenter extends IBasePresenter {
        void signIn(String login, String password);
        void signIn(String provider, String idToken, String accessToken, String expiresIn);
        void forgotPassword(String email);
        void logout();
        boolean logged();
        void setCountUnwatchedNotifications(int count);
        void setCountUnwatchedPMs(int count);
        int getCountUnwatchedNotifications();
        int getCountUnwatchedPMs();
        void updateNotify();
        void updatePMs();
        User getUser();
    }
}