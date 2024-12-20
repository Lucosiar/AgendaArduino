package com.agendaarduino;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class ActionAdapter extends RecyclerView.Adapter<ActionAdapter.ActionViewHolder> {

    private final Context context;
    private List<Action> actionsList;
    private final OnActionClickListener onActionClickListener;

    public interface OnActionClickListener {
        void onActionClick(Action action);
    }

    public ActionAdapter(Context context, List<Action> actionsList, OnActionClickListener onActionClickListener) {
        this.context = context;
        this.actionsList = actionsList;
        this.onActionClickListener = onActionClickListener;
    }



    // ordenar por fecha y hora
    private void sortEventsAndRoutinesByDateAndTime() {
        if(actionsList == null) return;

        Collections.sort(actionsList, (action1, action2) -> {
            LocalDateTime dateTime1;
            LocalDateTime dateTime2;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

            if (action1 instanceof Event) {
                Event event1 = (Event) action1;
                dateTime1 = LocalDateTime.parse(event1.getDate() + " " + event1.getTime(), formatter);
            } else if (action1 instanceof Routine) {
                Routine routine1 = (Routine) action1;
                dateTime1 = LocalDateTime.now().withHour(Integer.parseInt(routine1.getTime().split(":")[0]))
                        .withMinute(Integer.parseInt(routine1.getTime().split(":")[1]));
            } else {
                return 0; // Si no es ni Event ni Routine
            }

            if (action2 instanceof Event) {
                Event event2 = (Event) action2;
                dateTime2 = LocalDateTime.parse(event2.getDate() + " " + event2.getTime(), formatter);
            } else if (action2 instanceof Routine) {
                Routine routine2 = (Routine) action2;
                dateTime2 = LocalDateTime.now().withHour(Integer.parseInt(routine2.getTime().split(":")[0]))
                        .withMinute(Integer.parseInt(routine2.getTime().split(":")[1]));
            } else {
                return 0;
            }

            return dateTime1.compareTo(dateTime2);
        });
        notifyDataSetChanged();
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

        holder.tvTitle.setText(action.getTitle());
        holder.tvDescription.setText(action.getDescription());
        holder.tvTime.setText(action.getTime());
        holder.tvLabel.setText(action.getLabel());

        if(action.getLabel() != null && !action.getLabel().isEmpty()){
            holder.tvLabel.setText(action.getLabel());
        }else{
            holder.tvLabel.setVisibility(View.INVISIBLE);
        }

        if ("Sin descripción".equals(action.getDescription())) {
            holder.tvDescription.setVisibility(View.GONE);
        } else {
            holder.tvDescription.setVisibility(View.VISIBLE);
            holder.tvDescription.setText(action.getDescription());
        }

        holder.cbCircle.setChecked("completado".equals(action.getStatus()));

        holder.cbCircle.setChecked("completado".equals(action.getStatus()));

        LocalDate fechaActual = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String fechaHoy = fechaActual.format(formatter);

        if (action instanceof Event) {
            Log.d("PRUEBALOG", "Esta acción es un evento: " + ((Event) action).getTitle());
            Event event = (Event) action;
            LocalDate fechaEvento = LocalDate.parse(event.getDate(), formatter);

            if (fechaEvento.isBefore(fechaActual)) {
                Log.d("PRUEBALOG", "El evento está atrasado: " + event.getTitle());
                holder.actionLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.background_recycler_delayed));
            } else {
                Log.d("PRUEBALOG", "El evento es de hoy o futuro: " + event.getTitle());
                holder.actionLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.backgroud_recycler));
            }

            if (fechaHoy.equals(event.getDate())) {
                holder.tvDate.setVisibility(View.INVISIBLE);
            } else {
                holder.tvDate.setText(event.getDate());
                holder.tvDate.setVisibility(View.VISIBLE);
            }
        } else {
            Log.d("PRUEBALOG", "Esta acción no es un evento: " + action.getClass().getSimpleName());
            holder.tvDate.setVisibility(View.GONE);
        }





        holder.cbCircle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String newStatus = isChecked ? "completado" : "pendiente";
            action.setStatus(newStatus);

            // Actualización para eventos
            if (action instanceof Event) {
                Event event = (Event) action;
                if (event.getIdEvent() != null) {
                    Utility.getCollectionReferenceForEvents()
                            .document(event.getIdEvent())
                            .update("status", newStatus)
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(context, "Estado actualizado", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show());
                }
            }

            // Actualización para rutinas
            if (action instanceof Routine) {
                Routine routine = (Routine) action;
                if (routine.getIdRoutine() != null) {
                    Utility.getCollectionReferenceForRoutines()
                            .document(routine.getIdRoutine())
                            .update("status", newStatus)
                            .addOnSuccessListener(aVoid ->
                                    Toast.makeText(context, "Estado actualizado", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e ->
                                    Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show());
                }
            }
        });




    }

    @Override
    public int getItemCount() {
        return actionsList.size();
    }

    public void setActionList(List<Action> newActionList) {
        this.actionsList = newActionList;
        sortEventsAndRoutinesByDateAndTime();
        notifyDataSetChanged();
    }

    class ActionViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle, tvLabel, tvDescription, tvTime, tvDate;
        private CheckBox cbCircle;
        private LinearLayout actionLayout;

        public ActionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTime = itemView.findViewById(R.id.tvTime);
            cbCircle = itemView.findViewById(R.id.cbCircle);
            actionLayout = itemView.findViewById(R.id.actionLayout);
            tvDate = itemView.findViewById(R.id.tvDate);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    onActionClickListener.onActionClick(actionsList.get(position));
                }
            });
        }
    }
}