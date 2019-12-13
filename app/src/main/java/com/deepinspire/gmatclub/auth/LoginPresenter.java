package com.deepinspire.gmatclub.auth;

import android.content.Context;

import com.deepinspire.gmatclub.api.Api;
import com.deepinspire.gmatclub.api.AuthException;
import com.deepinspire.gmatclub.storage.IStorage;
import com.deepinspire.gmatclub.storage.Injection;
import com.deepinspire.gmatclub.storage.Repository;

/**
 * Created by dmytro mytsko on 21.03.18.
 */
public class LoginPresenter implements ILoginContract.Presenter {

    private ILoginContract.View view;

    private Repository repository = null;

    LoginPresenter(Context ctx, ILoginContract.View view) {
        this.repository = Injection.getRepository(ctx);

        this.view = view;
    }

    public void start() {
    }

    public boolean logged() {
        return this.repository.logged();
    }

    public void signIn(String username, String password) {
        repository.signIn(username, password, new IStorage.ICallbackAuth() {
            @Override
            public void onSuccess() {
                view.openWebSite(Api.FORUM_URL);
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


    public void resetPassword(String email) {
        repository.forgotPassword(email, new IStorage.ICallbackAuth() {
            @Override
            public void onSuccess() {
                view.showSuccess("resetPassword");
            }

            @Override
            public void onError(AuthException exception) {
                view.showError(exception);
            }
        });
    }

    public void signIn(final String provider, String idToken, String accessToken, String expiresIn) {
        repository.signInSocial(provider, idToken, accessToken, expiresIn, new IStorage.ICallbackAuth() {
            @Override
            public void onSuccess() {
                view.openWebSite(Api.FORUM_URL);
            }

            @Override
            public void onError(AuthException exception) {
                view.openWebSite("https://gmatclub.com/forum/oauthorize.php?provider=" + provider);
            }
        });
    }

    public void signInUseGoogleAccount(final String provider, String idToken, String accessToken, String expiresIn) {
        repository.signInSocialWithGoogle(provider, idToken, accessToken, expiresIn, new IStorage.ICallbackAuth() {
            @Override
            public void onSuccess() {
                view.openWebSite(Api.FORUM_URL);
            }

            @Override
            public void onError(AuthException exception) {
                view.openWebSite("https://gmatclub.com/forum/oauthorize.php?provider=" + provider);
            }
        });
    }

    public void getTokenInfo(String code) {
        repository.getTokenInfo(code, new IStorage.ICallbackAuth() {
            @Override
            public void onSuccess() {
                view.openWebSite(Api.FORUM_URL);
            }

            @Override
            public void onError(AuthException exception) {
                AuthException ex = new AuthException(new Exception("Failed sign in facebook"), "signInFacebook");
                view.showError(ex);
            }
        });
    }

    public boolean isOnline() {
        return repository.isOnline();
    }

    public boolean availableAuth() {
        return (repository.getUser().getCountFailedAuth() <= Api.AUTH_AVAILABLE_COUNT_FAILED_REQUESTS);
    }
}
