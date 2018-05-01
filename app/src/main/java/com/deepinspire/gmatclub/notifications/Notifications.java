package com.deepinspire.gmatclub.notifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import com.deepinspire.gmatclub.R;
import com.deepinspire.gmatclub.web.WebActivity;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by dmytro mytsko on 20.04.18.
 */
public class Notifications {
    private static final int LIMIT = 18;

    Context ctx;

    NotificationManager notificationsManager = null;

    NotificationCompat.Builder builder  = null;

    public Notifications(Context ctx) {
        this.ctx = ctx;

        getNotificationsManager();

        getNotificationBuilder();
    }

    private NotificationManager getNotificationsManager() {
        if(notificationsManager == null) {
            notificationsManager = (NotificationManager) ctx.getSystemService(ctx.NOTIFICATION_SERVICE);
        }

        return notificationsManager;
    }

    private NotificationCompat.Builder getNotificationBuilder() {
        if(builder == null) {
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                String channelId = "gcNotifications";
                builder = new NotificationCompat.Builder(ctx, channelId);
            } else {
                builder = new NotificationCompat.Builder(ctx);
            }
        }

        return builder;
    }

    public void send(RemoteMessage remoteMessage) {
        RemoteMessage.Notification notification = remoteMessage.getNotification();

        Map<String, String> data = remoteMessage.getData();

        int sentTime = (int) (remoteMessage.getSentTime() / 1000);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            clean();
        }

        Bitmap bitMap = BitmapFactory.decodeResource(ctx.getResources(), R.mipmap.app_icon);

        Intent intent = new Intent(ctx, WebActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);// || Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.setData(Uri.parse(data.get("url")));

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        String title = notification.getTitle();
        String body = notification.getBody();

        Bundle extra = new Bundle();
        extra.putString("url", data.get("url"));

        Notification notify = builder
                .setSmallIcon(R.mipmap.app_icon)
                .setLargeIcon(bitMap)
                .setContentTitle(title)
                .setContentText(body)
                .setSound(defaultSound)
                .setExtras(extra)
                .setStyle((new NotificationCompat.BigTextStyle()).bigText(body))
                .setContentIntent(PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_ONE_SHOT))
                .setAutoCancel(true)
                .setOngoing(true)
                .build();

        notificationsManager.notify(sentTime, notify);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void clean() {
        StatusBarNotification[] notifications = getNotificationsManager().getActiveNotifications();

        TreeMap<Long, Integer> sortedNotifications = new TreeMap<>();

        if(notifications.length > LIMIT) {
            for(int i = 0; i < notifications.length; i++) {
                final long postTime = notifications[i].getPostTime();
                final int id = notifications[i].getId();

                if (0 == id) {
                    getNotificationsManager().cancelAll();
                    return;
                }

                sortedNotifications.put(postTime, id);
            }

            while (sortedNotifications.size() > LIMIT) {
                final Map.Entry<Long, Integer> firstEntry = sortedNotifications.firstEntry();
                getNotificationsManager().cancel(firstEntry.getValue());
                sortedNotifications.remove(firstEntry.getKey());
            }
        }
    }
}