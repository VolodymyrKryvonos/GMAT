package com.deepinspire.gmatclub;

import android.support.multidex.MultiDexApplication;

import com.facebook.FacebookSdk;

/**
 * Created by dmytro mytsko on 21.03.18.
 */
public class Application extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        initApplication();
    }

    //TODO: first init before start application
    private void initApplication() {
        FacebookSdk.sdkInitialize(getApplicationContext());
    }
}