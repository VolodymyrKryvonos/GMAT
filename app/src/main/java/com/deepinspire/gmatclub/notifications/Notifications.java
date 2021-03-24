package com.deepinspire.gmatclub.notifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.deepinspire.gmatclub.R;
import com.deepinspire.gmatclub.splash.SplashActivity;
import com.deepinspire.gmatclub.utils.Storage;
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

    NotificationCompat.Builder builder = null;
    public static final String INPUT_URL = "url";

    public Notifications(Context ctx) {
        this.ctx = ctx;

        getNotificationsManager();

        getNotificationBuilder(ctx);
    }

    private NotificationManager getNotificationsManager() {
        if (notificationsManager == null) {
            notificationsManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return notificationsManager;
    }

    private NotificationCompat.Builder getNotificationBuilder(Context context) {
        if (builder == null) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                String channelId = context.getResources().getString(R.string.app_channel);
                builder = new NotificationCompat.Builder(ctx, channelId);
            } else {
                builder = new NotificationCompat.Builder(ctx);
            }
        }

        return builder;
    }

    public void send(RemoteMessage remoteMessage) {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


        }
        RemoteMessage.Notification notification = remoteMessage.getNotification();

        Map<String, String> data = remoteMessage.getData();

        int sentTime = (int) (remoteMessage.getSentTime() / 1000);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            clean();
        }

        //Bitmap bitMap = BitmapFactory.decodeResource(ctx.getResources(), R.mipmap.app_icon);

        Intent intent = new Intent(ctx, SplashActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);// || Intent.FLAG_ACTIVITY_NEW_TASK);

        if (data.get("url") != null){
            intent.setData(Uri.parse(data.get("url")));
            intent.putExtra(INPUT_URL, data.get("url"));
            intent.setAction(data.get("url"));
        }

        String title = notification.getTitle();
        String body = notification.getBody();

        Bundle extra = new Bundle();
        if (data.get("url") != null)
            extra.putString("url", data.get("url"));

        builder
                .setSmallIcon(R.mipmap.ic_notification)
                .setColor(ctx.getResources().getColor(R.color.main))
                .setNumber(Storage.getBadgeCount(ctx))
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                //.setLargeIcon(bitMap)
                .setContentTitle(title)
                .setContentText(body)
                .setExtras(extra)
                .setStyle((new NotificationCompat.BigTextStyle()).bigText(body))
                .setContentIntent(PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_ONE_SHOT))
                .setAutoCancel(true)
                .setOngoing(false);

        if (notification.getSound() != null) {
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }

        notificationsManager.notify(sentTime, builder.build());
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void clean() {
        StatusBarNotification[] notifications = getNotificationsManager().getActiveNotifications();

        TreeMap<Long, Integer> sortedNotifications = new TreeMap<>();

        if (notifications.length > LIMIT) {
            for (int i = 0; i < notifications.length; i++) {
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
    public static void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            String channelId = context.getResources().getString(R.string.app_channel);
            NotificationChannel notificationChannel = new NotificationChannel(
                    channelId, channelId, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(ContextCompat.getColor(context, R.color.blue_100));
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
          //  notificationChannel.setSound(defaultSoundUri, null);
            //           notificationChannel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

}