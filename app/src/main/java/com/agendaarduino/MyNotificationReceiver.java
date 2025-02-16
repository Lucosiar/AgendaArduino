package com.agendaarduino;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class MyNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("AlarmError NotificationDebug", "onReceive ejecutado - se recibió el Broadcast.");

        String title = intent.getStringExtra("title");
        String body = intent.getStringExtra("body");

        if (title == null || body == null) {
            Log.e("AlarmError NotificationDebug", "Título o cuerpo de la notificación son nulos.");
            return;
        }

        Log.d("AlarmError NotificationDebug", "Mostrando notificación: " + title + " - " + body);

        // Verificar si se tiene el permiso para enviar notificaciones (Android 13+)
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.e("AlarmError NotificationReceiver", "No se tienen permisos para mostrar notificaciones.");
            return; // No continuar si no se tiene el permiso
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_id")
                .setSmallIcon(R.drawable.notes_icon)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());

        Log.d("AlarmError NotificationDebug", "Notificación enviada correctamente.");
    }
}
