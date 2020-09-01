package com.deepinspire.gmatclub.web;

import android.content.Context;

import androidx.annotation.NonNull;

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
        void updateCountMessages();
        void sendNotificationsForPage(String notifications);
        void tryAgain();
        void openDeviceSettings();
        void updateUnreadNotification(int notificationsUnread);
        void showChatNotificationCount(String count);
    }

    interface Presenter extends IBasePresenter {
        void signIn(@NonNull Context context, String login, String password);
        void signIn(@NonNull Context context, String provider, String idToken, String accessToken, String expiresIn);
        void forgotPassword(String email);
        void logout(@NonNull Context context);
        boolean logged(@NonNull Context context);
        void setCountUnwatchedNotifications(int count);
        void setCountUnwatchedPMs(int count);
        void setNotifications(String notifications);
        void saveNotifications(String notifications);
        void getNotifications(@NonNull Context context);
        void getChatNotifications(@NonNull Context context);
        int getCountUnwatchedNotifications();
        int getCountUnwatchedPMs();
        void updateNotify(@NonNull Context context);
        void updateNotify(@NonNull Context context,String params, String id);
        void updatePMs();
        User getUser();
        boolean checkAccessNetwork(@NonNull Context context);
        void setError(int errorCode);
        int getError();
        void resetError();
        boolean availableAuth();
    }
}