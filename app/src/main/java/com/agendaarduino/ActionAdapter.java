package com.agendaarduino;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ActionAdapter extends RecyclerView.Adapter<ActionAdapter.ActionViewHolder> {

    private final Context context;
    private List<Action> actionsList;
    private final OnActionClickListener onActionClickListener;

    public interface OnActionClickListener {
        void onActionClick(Action action);
    }

    public ActionAdapter(Context context, List<Action> actionsList, OnActionClickListener onActionClickListener) {
        this.context = context;
        this.actionsList = new ArrayList<>(new HashSet<>(actionsList));
        this.onActionClickListener = onActionClickListener;
        sortEventsAndRoutinesByDateAndTime();
    }

    // Método para ordenar acciones por fecha y hora
    private void sortEventsAndRoutinesByDateAndTime() {
        if (actionsList == null) return;

        Collections.sort(actionsList, (action1, action2) -> {
            LocalDateTime dateTime1;
            LocalDateTime dateTime2;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

            if (action1 instanceof Event) {
                Event event1 = (Event) action1;
                dateTime1 = LocalDateTime.parse(event1.getDate() + " " + event1.getTime(), formatter);
            } else if (action1 instanceof Routine) {
                Routine routine1 = (Routine) action1;
                dateTime1 = LocalDateTime.now()
                        .withHour(Integer.parseInt(routine1.getTime().split(":")[0]))
                        .withMinute(Integer.parseInt(routine1.getTime().split(":")[1]));
            } else {
                return 0;
            }

            if (action2 instanceof Event) {
                Event event2 = (Event) action2;
                dateTime2 = LocalDateTime.parse(event2.getDate() + " " + event2.getTime(), formatter);
            } else if (action2 instanceof Routine) {
                Routine routine2 = (Routine) action2;
                dateTime2 = LocalDateTime.now()
                        .withHour(Integer.parseInt(routine2.getTime().split(":")[0]))
                        .withMinute(Integer.parseInt(routine2.getTime().split(":")[1]));
            } else {
                return 0;
            }

            return dateTime1.compareTo(dateTime2);
        });
    }

    @NonNull
    @Override
    public ActionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false);
        return new ActionViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ActionViewHolder holder, int position) {
        Action action = actionsList.get(position);

        // Configurar las vistas del adaptador
        holder.tvTitle.setText(action.getTitle());
        holder.tvDescription.setText(action.getDescription());
        holder.tvTime.setText(action.getTime());
        holder.tvLabel.setText(action.getLabel());

        holder.tvLabel.setVisibility(action.getLabel() == null
                || action.getLabel().isEmpty()
                || "Sin etiqueta".equals(action.getLabel()) ? View.INVISIBLE : View.VISIBLE);
        holder.tvDescription.setVisibility("Sin descripción".equals(action.getDescription()) ? View.GONE : View.VISIBLE);

        holder.cbCircle.setChecked("completado".equals(action.getStatus()));

        LocalDate fechaActual = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String fechaHoy = fechaActual.format(formatter);

        if (action instanceof Event) {
            Event event = (Event) action;
            LocalDate fechaEvento = LocalDate.parse(event.getDate(), formatter);

            if (fechaEvento.isBefore(fechaActual)) {
                holder.actionLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.background_recycler_delayed));
            } else {
                holder.actionLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.backgroud_recycler));
            }

            if (fechaHoy.equals(event.getDate())) {
                holder.tvDate.setVisibility(View.INVISIBLE);
                holder.tvLabel.setVisibility(View.GONE);
            } else {
                holder.tvDate.setText(event.getDate());
                holder.tvDate.setVisibility(View.VISIBLE);
                holder.tvLabel.setVisibility(
                        action.getLabel() == null ||
                                action.getLabel().isEmpty() ||
                                "Sin etiqueta".equals(action.getLabel()) ? View.INVISIBLE : View.VISIBLE
                );
            }

            fetchCheckListItems(event.getIdEvent(), holder.recyclerViewList);

        } else if (action instanceof Routine) {
            Routine routine = (Routine) action;

            // Verificar si la rutina corresponde al día actual
            DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE", new Locale("es", "ES"));
            String todayDayOfWeek = fechaActual.format(dayFormatter).toLowerCase();
        }

        holder.cbCircle.setOnCheckedChangeListener(null);
        holder.cbCircle.setChecked("completado".equals(action.getStatus()));
        holder.cbCircle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String newStatus = isChecked ? "completado" : "pendiente";

            // Actualizar el estado en la base de datos
            if (action instanceof Event) {
                updateEventStatus((Event) action, newStatus);
            } else if (action instanceof Routine) {
                updateRoutineStatus((Routine) action, newStatus);
            }

            action.setStatus(newStatus);
            notifyItemChanged(position);
        });

        // Configura el clic en los elementos
        holder.itemView.setOnClickListener(v -> showEventInfoPopup(action));

    }

    // Get las notificaciones
    private void fetchCheckListItems(String actionId, RecyclerView recyclerView) {
        Utility.getCollectionReferenceForChecklist()
                .whereEqualTo("actionId", actionId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ChecklistItem> checklistItems = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        ChecklistItem item = document.toObject(ChecklistItem.class);
                        item.setIdChecklist(document.getId());
                        checklistItems.add(item);
                    }

                    // Configurar RecyclerView con ChecklistItemAdapter
                    ChecklistItemAdapter checklistAdapter = new ChecklistItemAdapter(context, checklistItems);
                    recyclerView.setLayoutManager(new LinearLayoutManager(context));
                    recyclerView.setAdapter(checklistAdapter);
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error obteniendo checklist", e));
    }

    // Actualizar estado para eventos
    private void updateEventStatus(Event event, String newStatus) {
        if (event.getIdEvent() != null) {
            Utility.getCollectionReferenceForEvents()
                    .document(event.getIdEvent())
                    .update("status", newStatus)
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Estado actualizado", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show());
        }
    }

    // Actualizar estado para rutinas
    private void updateRoutineStatus(Routine routine, String newStatus) {
        if (routine.getIdRoutine() != null) {
            Utility.getCollectionReferenceForRoutines()
                    .document(routine.getIdRoutine())
                    .update("status", newStatus)
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Estado actualizado", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show());
        }
    }

    // Método para mostrar el popup con la información del action
    private void showEventInfoPopup(Action action) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.popup_action_info);

        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        TextView tvStatus = dialog.findViewById(R.id.tvStatus);
        TextView tvDescription = dialog.findViewById(R.id.tvDescription);
        TextView tvDate = dialog.findViewById(R.id.tvDate);
        TextView tvTime = dialog.findViewById(R.id.tvTime);
        TextView tvHourCalculate = dialog.findViewById(R.id.tvHourCalculate);
        Button btnEdit = dialog.findViewById(R.id.btnEdit);
        Button btnDelete = dialog.findViewById(R.id.btnDelete);
        Button btnClose = dialog.findViewById(R.id.btnClose);

        tvTitle.setText(action.getTitle());
        tvStatus.setText(action.getStatus());
        tvDescription.setText(action.getDescription());
        tvHourCalculate.setText(action.getHourCalculate());


        if (action instanceof Event) {
            Event event = (Event) action;
            tvDate.setText(event.getDate());
            tvTime.setText(event.getTime());
        } else if (action instanceof Routine) {
            Routine routine = (Routine) action;
            tvTime.setText(routine.getTime());
            tvDate.setVisibility(View.GONE);
        }

        btnEdit.setOnClickListener(v -> {
            editAction(action);
            dialog.dismiss();
        });

        btnDelete.setOnClickListener(v -> {
            deleteAction(action);
            dialog.dismiss();
        });

        btnClose.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void editAction(Action action) {
        // Implementa la lógica para editar el evento o rutina
        Toast.makeText(context, "Editar acción: " + action.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private void deleteAction(Action action) {
        // Implementa la lógica para borrar el evento o rutina
        Toast.makeText(context, "Borrar acción: " + action.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount() {
        return actionsList.size();
    }

    public void setActionList(List<Action> newActionList) {
        Map<String, Action> actionMap = new HashMap<>();
        for (Action action : newActionList) {
            String id = action instanceof Event ? ((Event) action).getIdEvent() : ((Routine) action).getIdRoutine();
            if (id != null) {
                actionMap.put(id, action);
            }
        }
        this.actionsList = new ArrayList<>(actionMap.values());
        sortEventsAndRoutinesByDateAndTime();
        notifyDataSetChanged();
    }

    class ActionViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle, tvLabel, tvDescription, tvTime, tvDate;
        private CheckBox cbCircle;
        private LinearLayout actionLayout;

        private RecyclerView recyclerViewList;

        public ActionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTime = itemView.findViewById(R.id.tvTime);
            cbCircle = itemView.findViewById(R.id.cbCircle);
            actionLayout = itemView.findViewById(R.id.actionLayout);
            tvDate = itemView.findViewById(R.id.tvDate);
            recyclerViewList = itemView.findViewById(R.id.recyclerCheckList);

        }
    }
}