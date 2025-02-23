package com.agendaarduino;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Manejar la notificación recibida
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            // Mostrar la notificación
            showNotification(title, body);
        }
    }

    private void showNotification(String title, String body) {

        Intent completeIntent = new Intent(this, NotificationActionReceiver.class);
        completeIntent.setAction("COMPLETE_ACTION");
        PendingIntent completePendingIntent = PendingIntent.getBroadcast(
                this, 0, completeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent postponeIntent = new Intent(this, NotificationActionReceiver.class);
        postponeIntent.setAction("POSTPONE_ACTION");
        PendingIntent postponePendingIntent = PendingIntent.getBroadcast(
                this, 0, postponeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent ignoreIntent = new Intent(this, NotificationActionReceiver.class);
        ignoreIntent.setAction("IGNORE_ACTION");
        PendingIntent ignorePendingIntent = PendingIntent.getBroadcast(
                this, 0, ignoreIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);


        // Construir la notificación con acciones
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_id")
                .setSmallIcon(R.drawable.notes_icon)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(R.drawable.notes_icon, "Completar", completePendingIntent)
                .addAction(R.drawable.notes_icon, "Posponer", postponePendingIntent)
                .addAction(R.drawable.notes_icon, "Ignorar", ignorePendingIntent);

        // Mostrar la notificación
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());

    }
}
