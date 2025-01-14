package com.agendaarduino;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChecklistItemAdapter extends RecyclerView.Adapter<ChecklistItemAdapter.ChecklistViewHolder> {
    private List<ChecklistItem> checklistItemList;

    public ChecklistItemAdapter(List<ChecklistItem> checklistItemList) {
        this.checklistItemList = checklistItemList;
    }

    @NonNull
    @Override
    public ChecklistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_checklist_item, parent, false);
        return new ChecklistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChecklistViewHolder holder, int position) {
        ChecklistItem item = checklistItemList.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.cbCircle.setChecked("completado".equals(item.getStatus()));
    }

    @Override
    public int getItemCount() {
        return checklistItemList != null ? checklistItemList.size() : 0;
    }

    public static class ChecklistViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        CheckBox cbCircle;
        public ChecklistViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitleChecklist);
            cbCircle = itemView.findViewById(R.id.cbCircle);
        }
    }
}
