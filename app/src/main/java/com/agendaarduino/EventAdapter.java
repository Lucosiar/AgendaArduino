package com.agendaarduino;

import android.content.Context;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final Context context;
    private final List<Event> eventList;
    private final OnEventClickListener onEventClickListener;

    // Interfaz para manejar clics en los eventos
    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public EventAdapter(Context context, List<Event> eventList, OnEventClickListener onEventClickListener) {
        this.context = context;
        this.eventList = eventList;
        this.onEventClickListener = onEventClickListener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_note_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.tvTitle.setText(event.getTitle());
        holder.tvDescription.setText(event.getDescription());
        holder.tvTime.setText(event.getTime());

        if (event.getLabel() != null && !event.getLabel().isEmpty()) {
            holder.tvLabel.setText(event.getLabel());
        } else {
            holder.tvLabel.setText("Sin etiqueta");
        }

        holder.cbCircle.setChecked("completado".equals(event.getStatus()));

        LocalDate fechaActual = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String fechaHoy = fechaActual.format(formatter);

        LocalDate fechaEvento = LocalDate.parse(event.getDate(), formatter);
        if (fechaEvento.isBefore(fechaActual)) {
            holder.eventLayout.setBackground(ContextCompat.getDrawable(context, R.drawable.background_recycler_delayed));
        } else {
            holder.eventLayout.setBackgroundResource(R.drawable.backgroud_recycler);
        }

        if (fechaHoy.equals(event.getDate())) {
            holder.tvDate.setVisibility(View.INVISIBLE);
        } else {
            holder.tvDate.setText(event.getDate());
            holder.tvDate.setVisibility(View.VISIBLE);
        }

        holder.cbCircle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String newStatus = isChecked ? "completado" : "pendiente";
            event.setStatus(newStatus);

            if (event.getId() != null) {
                Utility.getCollectionReferenceForEvents()
                        .document(event.getId()) // Usa el ID único para identificar el documento
                        .update("status", newStatus)
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(context, "Estado actualizado", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle, tvLabel, tvDescription, tvTime, tvDate;
        private CheckBox cbCircle;
        private LinearLayout eventLayout;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTime = itemView.findViewById(R.id.tvTime);
            cbCircle = itemView.findViewById(R.id.cbCircle);
            tvDate  = itemView.findViewById(R.id.tvDate);
            eventLayout = itemView.findViewById(R.id.eventLayout);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    onEventClickListener.onEventClick(eventList.get(position));
                }
            });
        }

        public void bind(Event event) {
            tvTitle.setText(event.getTitle());
            tvLabel.setText(event.getLabel());
            tvDescription.setText(event.getDescription());
            tvTime.setText(event.getTime());
            tvDate.setText(event.getDate());
        }
    }
}