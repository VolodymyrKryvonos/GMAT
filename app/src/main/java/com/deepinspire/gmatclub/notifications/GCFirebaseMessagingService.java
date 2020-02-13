package com.deepinspire.gmatclub.notifications;

import com.deepinspire.gmatclub.storage.Injection;
import com.deepinspire.gmatclub.utils.Storage;
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
        //int count = (int) Storage.getBadgeCount(getApplicationContext()) + 1;
        //Storage.saveBadgeCount(getApplicationContext(), count);
        if (Injection.getRepository(getApplicationContext()).logged() && remoteMessage.getData().size() > 0) {
            Notifications notifications = new Notifications(getApplicationContext());
            notifications.send(remoteMessage);
        } else if (Injection.getRepository(getApplicationContext()).logged() && remoteMessage.getNotification() != null) {
            Notifications notifications = new Notifications(getApplicationContext());
            notifications.send(remoteMessage);
        }
    }
}