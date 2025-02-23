package com.agendaarduino;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

public class NotificationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String id = intent.getStringExtra("id");
        String type = intent.getStringExtra("type");

        if(id == null  || type == null){
            Log.e("NotificationAction", "ID o tipo no proporcionados");
            return;
        }

        if ("COMPLETE".equals(action)) {
            updateStatusToCompleted(context, id, type);
        } else if ("SNOOZE".equals(action)) {
            // Terminar logica para postponer la tarea (Debería abrir la pantla de editar evento / rutina)
            Log.d("NotificationAction", "Tarea pospuesta");
        } else if ("DISMISS".equals(action)) {
            NotificationManagerCompat.from(context).cancel(1);
        }
    }

    private void updateStatusToCompleted(Context context, String id, String type) {
        if ("Event".equals(type)) {
            Utility.getCollectionReferenceForEvents()
                    .document(id)
                    .update("status", "completado")
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Estado del evento actualizado a completado"))
                    .addOnFailureListener(e -> Log.e("Firestore", "Error al actualizar el estado del evento", e));
        } else if ("Routine".equals(type)) {
            Utility.getCollectionReferenceForRoutines()
                    .document(id)
                    .update("status", "completado")
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "Estado de la rutina actualizado a completado"))
                    .addOnFailureListener(e -> Log.e("Firestore", "Error al actualizar el estado de la rutina", e));
        }
    }
}



