package com.deepinspire.gmatclub.notifications;

import com.deepinspire.gmatclub.storage.Injection;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by dmytro mytsko on 20.04.18.
 */
public class GCFirebaseMessagingService extends FirebaseMessagingService {
    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (Injection.getRepository(getApplicationContext()).logged(this) && remoteMessage.getData().size() > 0) {
            Notifications notifications = new Notifications(getApplicationContext());
            notifications.send(remoteMessage);
        } else if (Injection.getRepository(getApplicationContext()).logged(this) && remoteMessage.getNotification() != null) {
            Notifications notifications = new Notifications(getApplicationContext());
            notifications.send(remoteMessage);
        }
    }
}