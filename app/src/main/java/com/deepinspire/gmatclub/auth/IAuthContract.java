package com.deepinspire.gmatclub.auth;

import android.content.Context;

import androidx.annotation.NonNull;

import com.deepinspire.gmatclub.IBasePresenter;
import com.deepinspire.gmatclub.IBaseView;
import com.deepinspire.gmatclub.api.AuthException;

/**
 * Created by dmytro mytsko on 21.03.18.
 */
public interface IAuthContract {
    interface View extends IBaseView<Presenter> {
        void openWebSite(String types);
        void showSuccess(String type);
        void showError(AuthException exception);
    }

    interface Presenter extends IBasePresenter {
        boolean logged(@NonNull Context context);
        void signIn(@NonNull Context context,String login, String password);
        void signIn(@NonNull Context context,String provider, String idToken, String accessToken, String expiresIn);
        void forgotPassword(String email);
        void getTokenInfo(String code);
        boolean isOnline(@NonNull Context context);
        boolean availableAuth();
    }
}
