package com.agendaarduino;

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
        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_id")
                    .setSmallIcon(R.drawable.notes_icon)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(1, builder.build());
        } catch (SecurityException e) {
            // Manejar la excepción, por ejemplo, registrándola
            Log.e("NotificationError", "Error al mostrar la notificación", e);
        }
    }
}

