package ca.nicksalt.appdoc;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by nick on 2017-12-05.
 */

public class Receiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getStringExtra("test").equals("eye")) {
            showNotification(context, "Eye", new Intent(context, EyeTestActivity.class), R.drawable
                    .ic_eye_test);
        } else if (intent.getStringExtra("test").equals("colour")) {
            showNotification(context, "Colour", new Intent(context, ColourTestActivity.class), R.drawable.ic_palette);
        } else if (intent.getStringExtra("test").equals("hearing")) {
            showNotification(context, "Hearing", new Intent(context, ColourTestActivity.class), R.drawable.ic_hearing);
        }
    }

    public void showNotification(Context context, String test, Intent testIntent, int logo) {
        final String NOTIFICATION_CHANNEL_ID = "appdoc_channel";
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "appdoc",
                    importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
        Intent intent = new Intent(context, SplashActivity.class);
        PendingIntent splash = PendingIntent.getActivity(context, (int)System.currentTimeMillis(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent testActivity = PendingIntent.getActivity(context, (int)System.currentTimeMillis() + 1, testIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(logo)
                .setContentTitle("Time to Complete Your Next " + test + " Test")
                .setContentText("Tap this notification to start test!");
        if(FirebaseAuth.getInstance().getCurrentUser() != null)
            mBuilder.setContentIntent(testActivity);
        else
            mBuilder.setContentIntent(splash);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);

        mNotificationManager.notify(0, mBuilder.build());
    }
}
