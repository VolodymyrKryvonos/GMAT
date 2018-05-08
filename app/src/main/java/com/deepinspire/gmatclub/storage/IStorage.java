package com.deepinspire.gmatclub.storage;

import com.deepinspire.gmatclub.api.AuthException;

import java.util.Map;

/**
 * Created by dmytro mytsko on 26.03.18.
 */
public interface IStorage {
    interface ICallback {
        void onSuccess(Map<String, Object> cache);
        void onError();
    }

    interface ICallbackAuth {
        void onSuccess();
        void onError(AuthException exc);
    }

    interface ICallbackNotifications {
        void onSuccess(String notifications);
        void onError(AuthException exc);
    }
}