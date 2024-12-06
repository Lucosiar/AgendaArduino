package com.agendaarduino;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle, tvLabel, tvDescription, tvTime;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvTime = itemView.findViewById(R.id.tvTime);

            // Configuración de clics
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
        }
    }
}