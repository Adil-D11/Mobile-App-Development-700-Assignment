
package com.example.smartpantrymanagement;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ExpiryNotificationScheduler {

    private static final long TWO_DAYS =
            2L * 24 * 60 * 60 * 1000;

    public static void scheduleNotification(
            Context context,
            long ingredientId,
            String ingredient,
            String expiryDate) {

        // Don't schedule alerts if the user disabled them.
        boolean enabled = context.getSharedPreferences(
                "SmartPantrySettings",
                Context.MODE_PRIVATE
        ).getBoolean("ExpiryNotificationsEnabled", true);

        if (!enabled || expiryDate == null || expiryDate.trim().isEmpty()) {
            return;
        }

        SimpleDateFormat format =
                new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        format.setLenient(false);

        try {
            Date parsedDate = format.parse(expiryDate);

            if (parsedDate == null) {
                return;
            }

            Calendar expiry = Calendar.getInstance();
            expiry.setTime(parsedDate);

            // Use local midnight on the expiry date.
            expiry.set(Calendar.HOUR_OF_DAY, 0);
            expiry.set(Calendar.MINUTE, 0);
            expiry.set(Calendar.SECOND, 0);
            expiry.set(Calendar.MILLISECOND, 0);

            long triggerTime = expiry.getTimeInMillis() - TWO_DAYS;

            // Don't schedule a reminder that's already in the past.
            if (triggerTime <= System.currentTimeMillis()) {
                return;
            }

            Intent intent = new Intent(
                    context,
                    ExpiryNotificationReceiver.class
            );

            intent.putExtra("ingredient", ingredient);
            intent.putExtra("ingredientId", ingredientId);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    (int) ingredientId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                            | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager =
                    (AlarmManager) context.getSystemService(
                            Context.ALARM_SERVICE
                    );

            if (alarmManager == null) {
                return;
            }

            // Prefer inexact alarms to avoid requiring exact-alarm access.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
            } else {
                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}

