package com.agendaarduino;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ChecklistItemAdapter extends RecyclerView.Adapter<ChecklistItemAdapter.ChecklistViewHolder> {
    private List<ChecklistItem> checklistItemList;
    private Context context;
    private String actionId;

    public ChecklistItemAdapter(Context context, List<ChecklistItem> checklistItemList) {
        this.context = context;
        this.checklistItemList = checklistItemList;
        this.actionId = actionId;

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

        holder.cbCircle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String newStatus = isChecked ? "completado" : "pendiente";
            item.setStatus(newStatus);

            Utility.getCollectionReferenceForChecklist()
                    .whereEqualTo("actionId", actionId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<ChecklistItem> checklistItems = queryDocumentSnapshots.toObjects(ChecklistItem.class);
                        checklistItemList.clear();
                        checklistItemList.addAll(checklistItems);
                        notifyDataSetChanged(); // Actualiza la vista del RecyclerView
                    })
                    .addOnFailureListener(e -> Log.e("ChecklistError", "Error al cargar checklist", e));
        });
    }

    @Override
    public int getItemCount() {
        return checklistItemList.size();
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
