package com.agendaarduino;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class DayActionAdapter extends RecyclerView.Adapter<DayActionAdapter.DayActionViewHolder> {

    private final Context context;
    private final List<Action> actions;

    public DayActionAdapter(Context context, List<Action> actions) {
        this.context = context;
        this.actions = actions;
    }

    @NonNull
    @Override
    public DayActionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_day_item, parent, false);
        return new DayActionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayActionViewHolder holder, int position) {
        Action action = actions.get(position);

        holder.tvTitle.setText(action.getTitle());
        holder.tvDescription.setText(action.getDescription());
        holder.tvTime.setText(action.getTime());
        holder.tvLabel.setText(action.getLabel());

        // Ocultar descripción si es "Sin descripción"
        holder.tvDescription.setVisibility(
                "Sin descripción".equals(action.getDescription()) ? View.GONE : View.VISIBLE
        );

        holder.tvLabel.setVisibility(
                "Sin etiqueta".equals(action.getLabel()) ? View.GONE : View.VISIBLE
        );

        // Popup para mostrar más infomración del evento
        holder.itemView.setOnClickListener(v -> showActionInfoPopup(action));
    }

    @Override
    public int getItemCount() {
        return actions.size();
    }

    public static class DayActionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvTime, tvLabel;
        RecyclerView recyclerCheckList;

        public DayActionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTime = itemView.findViewById(R.id.tvTime);
            recyclerCheckList = itemView.findViewById(R.id.recyclerCheckList);
            tvLabel = itemView.findViewById(R.id.tvLabel);
        }
    }

    private void showActionInfoPopup(Action action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View popupView = LayoutInflater.from(context).inflate(R.layout.popup_action_info, null);

        // Referencias a las vistas
        TextView tvTitle = popupView.findViewById(R.id.tvTitle);
        TextView tvStatus = popupView.findViewById(R.id.tvStatus);
        TextView tvDescription = popupView.findViewById(R.id.tvDescription);
        TextView tvDate = popupView.findViewById(R.id.tvDate);
        TextView tvTime = popupView.findViewById(R.id.tvTime);
        TextView tvHourCalculate = popupView.findViewById(R.id.tvHourCalculate);
        // TextView checklist = popupView.findViewById(R.id.checklist);
        ImageView btnEdit = popupView.findViewById(R.id.btnEdit);
        ImageView btnDelete = popupView.findViewById(R.id.btnDelete);
        Button btnClose = popupView.findViewById(R.id.btnClose);

        // Rellenar los campos
        tvTitle.setText(action.getTitle());
        tvTime.setText("Hora: " + action.getTime());
        tvStatus.setText(action.getStatus());

        if (action.getHourCalculate().equals("0")) {
            tvHourCalculate.setVisibility(View.GONE);
        } else {
            tvHourCalculate.setText("Recordatorio: " + action.getHourCalculate());
        }

        // Para el campo Date: depende si es Event o Routine
        if (action instanceof Event) {
            tvDate.setText("Fecha: " + ((Event) action).getDate());
        } else if (action instanceof Routine) {
            String daysOfWeek = ((Routine) action).getDaysOfWeek();
            if ("Lunes, Martes, Miércoles, Jueves, Viernes, Sábado, Domingo".equalsIgnoreCase(daysOfWeek.trim())) {
                tvDate.setText("Días: Todos los días");
            } else {
                tvDate.setText("Días: " + daysOfWeek);
            }
        }

        if ("Sin descripción".equals(action.getDescription().trim())) {
            tvDescription.setVisibility(View.GONE);
        } else {
            tvDescription.setText(action.getDescription());
        }

        builder.setView(popupView);
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Botón Editar
        btnEdit.setOnClickListener(v -> {
            dialog.dismiss();
            if (action instanceof Event) {
                Intent intent = new Intent(context, EditEventActivity.class);
                intent.putExtra("eventId", ((Event) action).getIdEvent());
                context.startActivity(intent);
            } else if (action instanceof Routine) {
                Intent intent = new Intent(context, EditRoutineActivity.class);
                intent.putExtra("routineId", ((Routine) action).getIdRoutine());
                context.startActivity(intent);
            }
        });

        // Botón Editar
        btnEdit.setOnClickListener(v -> {
            dialog.dismiss();
            editAction(action);
        });

        // Botón Eliminar
        btnDelete.setOnClickListener(v -> {
            dialog.dismiss();
            deleteAction(action);
        });

    }

    private void editAction(Action action) {
        Intent intent;

        if (action instanceof Event) {
            Event event = (Event) action;

            intent = new Intent(context, EditEventActivity.class);
            intent.putExtra("eventId", event.getIdEvent());
            intent.putExtra("eventTitle", event.getTitle());
            intent.putExtra("eventDescription", event.getDescription());
            intent.putExtra("eventDate", event.getDate());
            intent.putExtra("eventTime", event.getTime());
            intent.putExtra("eventLabel", event.getLabel());
            intent.putExtra("eventRecordatory", event.getRecordatory());
            intent.putExtra("eventStatus", event.getStatus());

        } else if (action instanceof Routine) {
            Routine routine = (Routine) action;

            intent = new Intent(context, EditRoutineActivity.class);
            intent.putExtra("routineId", routine.getIdRoutine());
            intent.putExtra("routineTime", routine.getTime());
            intent.putExtra("routineLabel", routine.getLabel());
            intent.putExtra("routineRecordatory", routine.getRecordatory());
            intent.putExtra("routineStatus", routine.getStatus());
        } else {
            return;
        }

        context.startActivity(intent);
    }

    private void deleteAction(Action action) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (action instanceof Event) {
            Event event = (Event) action;
            String eventId = event.getIdEvent();

            if (eventId != null && !eventId.isEmpty()) {
                db.collection("events").document(eventId)
                        .delete()
                        .addOnSuccessListener(aVoid -> Toast.makeText(context, "Evento eliminado", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(context, "Error al eliminar evento", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(context, "ID del evento no disponible", Toast.LENGTH_SHORT).show();
            }

        } else if (action instanceof Routine) {
            Routine routine = (Routine) action;
            String routineId = routine.getIdRoutine();

            if (routineId != null && !routineId.isEmpty()) {
                db.collection("routines").document(routineId)
                        .delete()
                        .addOnSuccessListener(aVoid -> Toast.makeText(context, "Rutina eliminada", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(context, "Error al eliminar rutina", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(context, "ID de la rutina no disponible", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
