package com.agendaarduino;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.firestore.FirebaseFirestore;

public class NotificationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String id = intent.getStringExtra("id");
        String type = intent.getStringExtra("type");

        if (id == null || type == null) {
            Log.e("NotificationAction", "ID o tipo no proporcionados");
            return;
        }

        if ("COMPLETE".equals(action)) {
            // Lógica para completar la tarea
            updateStatusToCompleted(context, id, type);
            NotificationManagerCompat.from(context).cancel(1);
            // Mostrar un Toast
            Toast.makeText(context, "Acción completada", Toast.LENGTH_SHORT).show();
        } else if ("SNOOZE".equals(action)) {
            // Lógica para posponer la tarea (no implementada por ahora)
            Log.d("NotificationAction", "Tarea pospuesta (no implementada)");
        } else if ("DISMISS".equals(action)) {
            // Lógica para ignorar la notificación
            NotificationManagerCompat.from(context).cancel(1);
            Log.d("NotificationAction", "Notificación ignorada");
        }
    }

    private void updateStatusToCompleted(Context context, String id, String type) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if ("Event".equals(type)) {
            db.collection("events")
                    .document(id)
                    .update("status", "completado")
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Estado del evento actualizado a completado"))
                    .addOnFailureListener(e -> Log.e("Firestore", "Error al actualizar el estado del evento", e));
        } else if ("Routine".equals(type)) {
            db.collection("routine")
                    .document(id)
                    .update("status", "completado")
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Estado de la rutina actualizado a completado"))
                    .addOnFailureListener(e -> Log.e("Firestore", "Error al actualizar el estado de la rutina", e));
        }
    }
}

