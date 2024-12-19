package com.agendaarduino;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddEventActivity extends AppCompatActivity {

    private EditText etTitleEvent, etDescriptionEvent;
    private Button buttonSaveEvent;
    private Spinner spinnerLabelEvent, spinnerRecordatoryEvent;
    private ImageButton buttonNewLabelEvent, buttonAddDateEvent, buttonAddTimeEvent, buttonRecordatoryEvent;
    private TextView tvDateEvent, tvTimeEvent;
    private ArrayList<String> labels = new ArrayList<>();
    private ArrayAdapter<String>labelAdapter;
    private int selectedHour, selectedMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        initialize();

        // Configuración de spinner para las opciones de recordatorio
        String[] recordatoryOptions = {"Sin recordatorio", "15 minutos", "30 minutos", "1 hora"};
        ArrayAdapter<String> recordatoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                recordatoryOptions
        );
        recordatoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRecordatoryEvent.setAdapter(recordatoryAdapter);

        // Configuración de spinner para las opciones de las etiquetas
        labelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);
        labelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLabelEvent.setAdapter(labelAdapter);

        spinnerLabelEvent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("Spinner", "Etiqueta seleccionada: " + labels.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("Spinner", "Nada seleccionado en el Spinner");
            }
        });

        // Cargar las etiquetas del usuario desde Firestore
        loadLabelsFromFirestore();

        // Boton de guardar evento
        buttonSaveEvent.setOnClickListener((view -> saveEvent()));

        // Boton para crear una nueva etiqueta
        buttonNewLabelEvent.setOnClickListener((view -> showAddLabelDialog()));

        // Boton para mostrar los selectores para la fecha y hora
        buttonAddDateEvent.setOnClickListener((view -> showDatePickerDialog()));
        buttonAddTimeEvent.setOnClickListener((view -> showTimePickerDialog()));
        buttonRecordatoryEvent.setOnClickListener((view -> showTimePickerDialog()));

        // Textview que muestra también los selectores para la fecha y hora
        tvDateEvent.setOnClickListener((view -> showDatePickerDialog()));
        tvTimeEvent.setOnClickListener((view -> showTimePickerDialog()));

    }


    @Override
    protected void onResume() {
        super.onResume();
        loadLabelsFromFirestore();
    }

    public void initialize(){
        etTitleEvent = findViewById(R.id.etTitleEvent);
        etDescriptionEvent = findViewById(R.id.etDescriptionEvent);
        tvDateEvent = findViewById(R.id.tvDateEvent);
        tvTimeEvent = findViewById(R.id.tvTimeEvent);

        spinnerLabelEvent = findViewById(R.id.spinnerLabelEvent);
        spinnerRecordatoryEvent = findViewById(R.id.spinnerRecordatoryEvent);

        buttonSaveEvent = findViewById(R.id.buttonSaveEvent);
        buttonNewLabelEvent = findViewById(R.id.buttonNewLabelEvent);
        buttonAddDateEvent = findViewById(R.id.buttonAddDateEvent);
        buttonAddTimeEvent = findViewById(R.id.buttonAddTimeEvent);
        buttonRecordatoryEvent = findViewById(R.id.buttonRecordatoryEvent);
    }

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            selectedHour = hourOfDay;
            selectedMinute = minute;
            tvTimeEvent.setText(String.format("%02d:%02d", hourOfDay, minute));
        }, selectedHour, selectedMinute, true);
        timePickerDialog.show();
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            tvDateEvent.setText(String.format("%04d/%02d/%02d", selectedYear, selectedMonth + 1, selectedDay));
        }, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
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

    private String capitalizeFirstLetter(String newLabel) {
        if(newLabel == null || newLabel.isEmpty()){
            return newLabel;
        }
        return newLabel.substring(0, 1).toUpperCase() + newLabel.substring(1).toLowerCase();
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
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            String label = document.getId();
                            if (!labels.contains(label)) {
                                labels.add(label);
                            }
                        }
                    }
                    labelAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar etiquetas", Toast.LENGTH_SHORT).show();
                });
    }


    private void saveEvent() {
        String eventTitle = etTitleEvent.getText().toString();
        String eventDescription = etDescriptionEvent.getText().toString();
        String eventDate = tvDateEvent.getText().toString();
        String eventTime = tvTimeEvent.getText().toString();
        String eventLabel = spinnerLabelEvent.getSelectedItem().toString();
        String eventRecordatory = spinnerRecordatoryEvent.getSelectedItem().toString();

        if(eventTitle.isEmpty() || eventDate.isEmpty() || eventTime.isEmpty() || eventLabel.isEmpty() || eventRecordatory.isEmpty()){
            Toast.makeText(this, "Please fill in the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if(eventDescription.isEmpty()){
            eventDescription = "Sin descripción";
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null){
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        Event event = new Event();
        event.setTitle(eventTitle);
        event.setDescription(eventDescription);
        event.setDate(eventDate);
        event.setTime(eventTime);
        event.setLabel(eventLabel);
        event.setRecordatory(eventRecordatory);
        event.setStatus("pendiente");
        event.setIdUser(userId);

        saveEventToFirebase(event);
    }

    private void saveEventToFirebase(Event event) {
        DocumentReference documentReference = Utility.getCollectionReferenceForEvents().document();
        String eventId = documentReference.getId();
        event.setIdEvent(eventId);

        documentReference.set(event).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(AddEventActivity.this, "Evento guardado exitosamente", Toast.LENGTH_SHORT).show();
                clearForm();
                navigateToMainActivity();
            } else {
                Toast.makeText(AddEventActivity.this, "Error al guardar el evento", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearForm() {
        etTitleEvent.setText("");
        etDescriptionEvent.setText("");
        tvDateEvent.setText("");
        tvTimeEvent.setText("");
        spinnerLabelEvent.setSelection(0);
        spinnerRecordatoryEvent.setSelection(0);
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(AddEventActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}