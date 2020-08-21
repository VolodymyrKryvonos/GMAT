package com.deepinspire.gmatclub.notifications;

import com.deepinspire.gmatclub.storage.Injection;
import com.google.firebase.messaging.FirebaseMessagingService;

/**
 * Created by dmytro mytsko on 20.04.18.
 */
public class GCFirebaseInstanceIDService extends FirebaseMessagingService {
    private static final String TAG = GCFirebaseInstanceIDService.class.getSimpleName();

    public void onTokenRefresh() {
        Injection.getRepository(getApplicationContext()).logged(this,true);
    }
}