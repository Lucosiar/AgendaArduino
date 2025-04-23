package com.agendaarduino;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditEventActivity extends AppCompatActivity {

    private EditText etTitleEvent, etDescriptionEvent;
    private TextView tvDateEvent, tvTimeEvent;
    private Spinner spinnerLabelEvent, spinnerRecordatoryEvent;
    private Button btnUpdateEvent;
    private ImageButton buttonNewLabelEvent, buttonAddDateEvent, buttonAddTimeEvent, buttonRecordatoryEvent;
    private String eventId;
    private ArrayList<String> labels = new ArrayList<>();
    private ArrayAdapter<String> labelAdapter;
    private String[] recordatoryOptions = {"Sin recordatorio", "15 minutos", "30 minutos", "1 hora", "Misma hora", "Personalizado"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        etTitleEvent = findViewById(R.id.etTitleEvent);
        etDescriptionEvent = findViewById(R.id.etDescriptionEvent);
        tvDateEvent = findViewById(R.id.tvDateEvent);
        tvTimeEvent = findViewById(R.id.tvTimeEvent);
        spinnerLabelEvent = findViewById(R.id.spinnerLabelEvent);
        spinnerRecordatoryEvent = findViewById(R.id.spinnerRecordatoryEvent);
        btnUpdateEvent = findViewById(R.id.btnUpdateEvent);
        buttonNewLabelEvent = findViewById(R.id.buttonNewLabelEvent);

        // Adaptador de etiquetas
        labelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);
        labelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLabelEvent.setAdapter(labelAdapter);

        // Adaptador de recordatorios
        ArrayAdapter<String> recordatoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, recordatoryOptions);
        recordatoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRecordatoryEvent.setAdapter(recordatoryAdapter);

        btnUpdateEvent.setOnClickListener(v -> updateEventInFirebase());
        buttonNewLabelEvent.setOnClickListener(v -> showAddLabelDialog());

        loadEventData();
        loadLabelsFromFirestore();
    }

    private void loadEventData() {
        Intent intent = getIntent();
        eventId = intent.getStringExtra("eventId");

        etTitleEvent.setText(intent.getStringExtra("eventTitle"));
        etDescriptionEvent.setText(intent.getStringExtra("eventDescription"));
        tvDateEvent.setText(intent.getStringExtra("eventDate"));
        tvTimeEvent.setText(intent.getStringExtra("eventTime"));

        // Guardamos etiquetas y recordatorios para usar luego
        String label = intent.getStringExtra("eventLabel");
        String recordatory = intent.getStringExtra("eventRecordatory");

        spinnerLabelEvent.setTag(label);
        spinnerRecordatoryEvent.setSelection(getIndex(spinnerRecordatoryEvent, recordatory));
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    private int getIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(value)) return i;
        }
        return 0;
    }

    private void updateEventInFirebase() {
        String title = etTitleEvent.getText().toString();
        String description = etDescriptionEvent.getText().toString();
        String date = tvDateEvent.getText().toString();
        String time = tvTimeEvent.getText().toString();
        String label = spinnerLabelEvent.getSelectedItem().toString();
        String recordatory = spinnerRecordatoryEvent.getSelectedItem().toString();
        String hourCalculate = calculateHour(time, recordatory);

        FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId)
                .update(
                        "title", title,
                        "description", description,
                        "date", date,
                        "time", time,
                        "label", label,
                        "recordatory", recordatory,
                        "hourCalculate", hourCalculate
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Evento actualizado", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show());
    }

    private String calculateHour(String time, String recordatory) {
        try {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            if(recordatory.equalsIgnoreCase("Misma hora")){
                return time;
            }

            // Si el recordatorio es una hora específica, validamos su formato y lo usamos directamente
            if (recordatory.matches("\\d{2}:\\d{2}")) {
                LocalTime recordatoryTime = LocalTime.parse(recordatory, timeFormatter);
                return recordatoryTime.format(timeFormatter);
            }

            LocalTime eventTime = LocalTime.parse(time, timeFormatter);
            int minutesToSubtract = 0;

            if (recordatory.equals("15 minutos")) {
                minutesToSubtract = 15;
            } else if (recordatory.equals("30 minutos")) {
                minutesToSubtract = 30;
            } else if (recordatory.equals("1 hora")) {
                minutesToSubtract = 60;
            } else if (recordatory.equalsIgnoreCase("Sin recordatorio")) {
                return "0";
            }

            LocalTime calculatedTime = eventTime.minusMinutes(minutesToSubtract);
            return calculatedTime.format(timeFormatter);

        } catch (Exception e) {
            Log.e("AddEventActivity", "Error al calcular la hora del recordatorio", e);
            return time;
        }
    }

    private void showAddLabelDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Añadir etiqueta");

        final EditText input = new EditText(this);
        input.setHint("Nueva etiqueta");
        builder.setView(input);

        builder.setPositiveButton("Añadir", (dialog, which) -> {
            String newLabel = input.getText().toString().trim();
            if (!newLabel.isEmpty() && !labels.contains(newLabel)) {
                newLabel = capitalizeFirstLetter(newLabel);
                saveLabelToFirebase(newLabel);
            } else {
                Toast.makeText(this, "Etiqueta inválida o ya existente", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void saveLabelToFirebase(String label) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        Map<String, Object> labelData = new HashMap<>();
        labelData.put("userId", userId);

        DocumentReference docRef = Utility.getCollectionReferenceForLabels().document(label);
        docRef.set(labelData).addOnSuccessListener(aVoid -> {
            if (!labels.contains(label)) {
                labels.add(label);
                labelAdapter.notifyDataSetChanged();
                spinnerLabelEvent.setSelection(labels.indexOf(label));
            }
            Toast.makeText(this, "Etiqueta guardada y añadida al Spinner", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error al guardar la etiqueta", Toast.LENGTH_SHORT).show());
    }

    private void loadLabelsFromFirestore() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        labels.clear();
        labels.add("Sin etiqueta");

        Utility.getCollectionReferenceForLabels()
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String label = document.getId();
                        if (!labels.contains(label)) {
                            labels.add(label);
                        }
                    }
                    labelAdapter.notifyDataSetChanged();

                    // Seleccionar la etiqueta guardada
                    String savedLabel = (String) spinnerLabelEvent.getTag();
                    if (savedLabel != null) {
                        int index = getIndex(spinnerLabelEvent, savedLabel);
                        spinnerLabelEvent.setSelection(index);
                    }

                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar etiquetas", Toast.LENGTH_SHORT).show());
    }


}
