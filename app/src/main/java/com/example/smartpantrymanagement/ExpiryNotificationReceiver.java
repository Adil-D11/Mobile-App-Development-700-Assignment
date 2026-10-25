
package com.example.smartpantrymanagement;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ExpiryNotificationReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "expiry_notifications";

    @Override
    public void onReceive(Context context, Intent intent) {
        String ingredient = intent.getStringExtra("ingredient");

        if (ingredient == null || ingredient.trim().isEmpty()) {
            return;
        }

        // Android 8.0 and newer require a notification channel.
        NotificationManager manager =
                (NotificationManager) context.getSystemService(
                        Context.NOTIFICATION_SERVICE
                );

        if (manager == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Expiry Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            channel.setDescription(
                    "Reminders for ingredients expiring soon"
            );

            manager.createNotificationChannel(channel);
        }

        // Android 13 and newer require notification permission.
        if (Build.VERSION.SDK_INT >= 33 &&
                context.checkSelfPermission(
                        Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(
                                android.R.drawable.ic_dialog_info
                        )
                        .setContentTitle("Ingredient expiring soon")
                        .setContentText(
                                "Your " + ingredient
                                        + " expires in 2 days!"
                        )
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(
                ingredient.hashCode(),
                notification.build()
        );
    }
}

