package com.agendaarduino;
import android.app.PendingIntent;
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

        Log.d("AlarmErrorNotificationDebug", "onReceive ejecutado - se recibió el Broadcast.");

        String title = intent.getStringExtra("title");
        String body = intent.getStringExtra("body");
        String id = intent.getStringExtra("id");
        String type = intent.getStringExtra("type");

        if (title == null || body == null || id == null || type == null) {
            if(title==null && body == null){
                Log.e("AlarmErrorNotificationDebug", "Datos de titulo y body.");
            }
            if(id==null){
                Log.e("AlarmErrorNotificationDebug", "ID vacio.");
            }
            if(type==null){
                Log.e("AlarmErrorNotificationDebug", "Type vacio");
            }
            Log.e("AlarmErrorNotificationDebug", "Datos de la notificación incompletos.");
            return;
        }

        Log.d("AlarmErrorNotificationDebug", "Mostrando notificación: " + title + " - " + body);

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.e("AlarmErrorNotificationReceiver", "No se tienen permisos para mostrar notificaciones.");
            return;
        }

        // PendingIntent para cada acción
        Intent completeIntent = new Intent(context, NotificationActionReceiver.class);
        completeIntent.setAction("COMPLETE");
        completeIntent.putExtra("id", id);
        completeIntent.putExtra("type", type);
        PendingIntent completePendingIntent = PendingIntent.getBroadcast(context, 0, completeIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent snoozeIntent = new Intent(context, NotificationActionReceiver.class);
        snoozeIntent.setAction("SNOOZE");
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(context, 1, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Intent dismissIntent = new Intent(context, NotificationActionReceiver.class);
        dismissIntent.setAction("DISMISS");
        dismissIntent.putExtra("id", id);
        dismissIntent.putExtra("type", type);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(context, 2, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Construir la notificación con las acciones
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_id")
                .setSmallIcon(R.drawable.notes_icon)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(R.drawable.ic_complete_notificacion, "Completar", completePendingIntent)
                .addAction(R.drawable.ic_complete_notificacion, "Posponer", snoozePendingIntent)
                .addAction(R.drawable.ic_complete_notificacion, "Ignorar", dismissPendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());

        Log.d("AlarmErrorNotificationDebug", "Notificación enviada correctamente.");
    }
}
