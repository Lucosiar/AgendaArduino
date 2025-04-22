package com.agendaarduino;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
}
