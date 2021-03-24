package com.deepinspire.gmatclub;

import androidx.multidex.MultiDexApplication;

import com.deepinspire.gmatclub.notifications.Notifications;
import com.facebook.FacebookSdk;

/**
 * Created by dmytro mytsko on 21.03.18.
 */
public class Application extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        initApplication();
        Notifications.createNotificationChannel(this);
    }

    //TODO: first init before start application
    private void initApplication() {
        FacebookSdk.sdkInitialize(getApplicationContext());
    }
}
