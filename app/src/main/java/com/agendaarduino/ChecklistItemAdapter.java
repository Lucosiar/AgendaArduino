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

    public ChecklistItemAdapter(Context context, List<ChecklistItem> checklistItemList) {
        this.context = context;
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

        //Agregar Listener al cbCircle
        holder.cbCircle.setOnClickListener(v -> {
            String newStatus = holder.cbCircle.isChecked() ? "completado" : "pendiente";
            item.setStatus(newStatus);
            updateChecklistItemStatus(item);
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return checklistItemList.size();
    }

    private void updateChecklistItemStatus(ChecklistItem item) {
        if (item.getChecklistId() == null) {
            Log.e("Firestore", "ID del checklist es null para el item: " + item.getTitle());
            return;
        }

        Utility.getCollectionReferenceForChecklist()
                .document(item.getChecklistId())
                .update("status", item.getStatus())
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Estado del checklist actualizado");
                    checkIfAllChecklistItemsCompleted(item.getActionId());
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error al actualizar el estado del checklist", e));
    }

    private void checkIfAllChecklistItemsCompleted(String actionId) {
        Utility.getCollectionReferenceForChecklist()
                .whereEqualTo("actionId", actionId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean allCompleted = true;
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        ChecklistItem checklistItem = document.toObject(ChecklistItem.class);
                        if (!"completado".equals(checklistItem.getStatus())) {
                            allCompleted = false;
                            break;
                        }
                    }
                    if (allCompleted) {
                        updateEventStatusToCompleted(actionId);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error al verificar el estado del checklist", e));
    }

    private void updateEventStatusToCompleted(String actionId) {
        Utility.getCollectionReferenceForEvents()
                .whereEqualTo("idEvent", actionId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String eventId = document.getId();
                        Utility.getCollectionReferenceForEvents()
                                .document(eventId)
                                .update("status", "completado")
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Estado del evento actualizado a completado"))
                                .addOnFailureListener(e -> Log.e("Firestore", "Error al actualizar el estado del evento", e));
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error al obtener el evento", e));
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
