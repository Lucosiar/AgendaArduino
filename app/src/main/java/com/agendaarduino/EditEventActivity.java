package com.agendaarduino;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditEventActivity extends AppCompatActivity {

    private static final String[] RECORDATORY_OPTIONS = {
            "Sin recordatorio", "15 minutos", "30 minutos", "1 hora", "Misma hora", "Personalizado"
    };

    private EditText etTitleEvent, etDescriptionEvent;
    private TextView tvDateEvent, tvTimeEvent;
    private Spinner spinnerLabelEvent, spinnerRecordatoryEvent;
    private Button btnUpdateEvent;
    private ImageButton buttonNewLabelEvent, buttonAddDateEvent, buttonAddTimeEvent, buttonRecordatoryEvent;

    private ArrayAdapter<String> labelAdapter;
    private final ArrayList<String> labels = new ArrayList<>();

    private int selectedHour, selectedMinute;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        initViews();
        setupSpinners();
        setupListeners();

        loadEventData();
        loadLabelsFromFirestore();
    }

    private void initViews() {
        etTitleEvent = findViewById(R.id.etTitleEvent);
        etDescriptionEvent = findViewById(R.id.etDescriptionEvent);
        tvDateEvent = findViewById(R.id.tvDateEvent);
        tvTimeEvent = findViewById(R.id.tvTimeEvent);
        spinnerLabelEvent = findViewById(R.id.spinnerLabelEvent);
        spinnerRecordatoryEvent = findViewById(R.id.spinnerRecordatoryEvent);
        btnUpdateEvent = findViewById(R.id.btnUpdateEvent);
        buttonNewLabelEvent = findViewById(R.id.buttonNewLabelEvent);
        buttonAddDateEvent = findViewById(R.id.buttonAddDateEvent);
        buttonAddTimeEvent = findViewById(R.id.buttonAddTimeEvent);
        buttonRecordatoryEvent = findViewById(R.id.buttonRecordatoryEvent);
    }

    private void setupSpinners() {
        // Spinner de etiquetas
        labelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);
        labelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLabelEvent.setAdapter(labelAdapter);

        // Spinner de recordatorio
        ArrayAdapter<String> recordatoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, RECORDATORY_OPTIONS);
        recordatoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRecordatoryEvent.setAdapter(recordatoryAdapter);
    }

    private void setupListeners() {
        btnUpdateEvent.setOnClickListener(v -> updateEventInFirebase());
        buttonNewLabelEvent.setOnClickListener(v -> showAddLabelDialog());
        buttonAddDateEvent.setOnClickListener(v -> showDatePickerDialog());
        buttonAddTimeEvent.setOnClickListener(v -> showTimePickerDialogTime());
        buttonRecordatoryEvent.setOnClickListener(v -> showTimePickerDialogRecordatory());
    }

    private void loadEventData() {
        Intent intent = getIntent();
        eventId = intent.getStringExtra("eventId");

        etTitleEvent.setText(intent.getStringExtra("eventTitle"));
        etDescriptionEvent.setText(intent.getStringExtra("eventDescription"));
        tvDateEvent.setText(intent.getStringExtra("eventDate"));
        tvTimeEvent.setText(intent.getStringExtra("eventTime"));

        // Guardamos etiqueta y recordatorio para seleccionar después
        spinnerLabelEvent.setTag(intent.getStringExtra("eventLabel"));
        spinnerRecordatoryEvent.setSelection(getIndex(spinnerRecordatoryEvent, intent.getStringExtra("eventRecordatory")));
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
            tvDateEvent.setText(String.format("%04d/%02d/%02d", year, month + 1, day));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    private void showTimePickerDialogTime() {
        new TimePickerDialog(this, (view, hour, minute) -> {
            selectedHour = hour;
            selectedMinute = minute;
            tvTimeEvent.setText(String.format("%02d:%02d", hour, minute));
        }, selectedHour, selectedMinute, true).show();
    }

    private void showTimePickerDialogRecordatory() {
        new TimePickerDialog(this, (view, hour, minute) -> {
            String customTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerRecordatoryEvent.getAdapter();

            if (adapter.getPosition(customTime) == -1) {
                adapter.insert(customTime, adapter.getCount() - 1);
            }

            spinnerRecordatoryEvent.setSelection(adapter.getPosition(customTime));
        }, 12, 0, true).show();
    }

    private void showAddLabelDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Añadir etiqueta");

        final EditText input = new EditText(this);
        input.setHint("Nueva etiqueta");
        builder.setView(input);

        builder.setPositiveButton("Añadir", (dialog, which) -> {
            String newLabel = capitalizeFirstLetter(input.getText().toString().trim());
            if (!newLabel.isEmpty() && !labels.contains(newLabel)) {
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

        Utility.getCollectionReferenceForLabels().document(label)
                .set(labelData)
                .addOnSuccessListener(aVoid -> {
                    labels.add(label);
                    labelAdapter.notifyDataSetChanged();
                    spinnerLabelEvent.setSelection(labels.indexOf(label));
                    Toast.makeText(this, "Etiqueta añadida", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al guardar etiqueta", Toast.LENGTH_SHORT).show());
    }

    private void loadLabelsFromFirestore() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        labels.clear();
        labels.add("Sin etiqueta");

        Utility.getCollectionReferenceForLabels()
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String label = doc.getId();
                        if (!labels.contains(label)) {
                            labels.add(label);
                        }
                    }
                    labelAdapter.notifyDataSetChanged();

                    String savedLabel = (String) spinnerLabelEvent.getTag();
                    if (savedLabel != null) {
                        spinnerLabelEvent.setSelection(getIndex(spinnerLabelEvent, savedLabel));
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar etiquetas", Toast.LENGTH_SHORT).show());
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
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show());
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

    private String capitalizeFirstLetter(String input) {
        return input.isEmpty() ? input : input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    private int getIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) return i;
        }
        return 0;
    }
}





