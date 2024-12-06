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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class AddEventActivity extends AppCompatActivity {

    private EditText etTitle, etDescription;
    private Button buttonSaveEvent;
    private Spinner spinnerLabel;
    private ImageButton buttonNewLabel, buttonAddDate, buttonAddTime;
    private TextView tvDate, tvTime;

    private ArrayList<String> labels = new ArrayList<>();
    private ArrayAdapter<String>labelAdapter;
    private int selectedHour, selectedMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        initialize();

        Log.d("AddEventActivity", "onCreate: Inicializando Spinner y adaptador");
        labelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);
        labelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLabel.setAdapter(labelAdapter);

        Log.d("AddEventActivity", "onCreate: Configurando OnItemSelectedListener para el Spinner");
        spinnerLabel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("Spinner", "Etiqueta seleccionada: " + labels.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d("Spinner", "Nada seleccionado en el Spinner");
            }
        });

        Log.d("AddEventActivity", "onCreate: Cargando etiquetas desde Firestore");
        loadLabelsFromFirestore();

        buttonSaveEvent.setOnClickListener((view -> saveEvent()));
        buttonNewLabel.setOnClickListener((view -> showAddLabelDialog()));
        buttonAddDate.setOnClickListener((view -> showDatePickerDialog()));
        buttonAddTime.setOnClickListener((view -> showTimePickerDialog()));


    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("AddEventActivity", "onResume: Recargando etiquetas desde Firestore");
        loadLabelsFromFirestore();
    }

    public void initialize(){
        Log.d("AddEventActivity", "Inicializando vistas");
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        tvDate = findViewById(R.id.tvDate);
        tvTime = findViewById(R.id.tvTime);

        spinnerLabel = findViewById(R.id.spinnerLabel);

        buttonSaveEvent = findViewById(R.id.buttonSaveEvent);
        buttonNewLabel = findViewById(R.id.buttonNewLabel);
        buttonAddDate = findViewById(R.id.buttonAddDate);
        buttonAddTime = findViewById(R.id.buttonAddTime);
    }

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            selectedHour = hourOfDay;
            selectedMinute = minute;
            tvTime.setText(String.format("%02d:%02d", hourOfDay, minute));
        }, selectedHour, selectedMinute, true);
        timePickerDialog.show();
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            tvDate.setText(String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay));
        }, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showAddLabelDialog() {
        Log.d("AddEventActivity", "Mostrando cuadro de diálogo para añadir etiqueta");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Añadir etiqueta");

        final EditText input = new EditText(this);
        input.setHint("Nueva etiqueta");
        builder.setView(input);

        builder.setPositiveButton("Añadir", (dialog, which) -> {
            String newLabel = input.getText().toString().trim();
            Log.d("AddEventActivity", "Etiqueta ingresada: " + newLabel);
            if (!newLabel.isEmpty() && !labels.contains(newLabel)) {
                newLabel = capitalizeFirstLetter(newLabel);
                saveLabelToFirebase(newLabel);
            } else {
                Log.w("AddEventActivity", "Etiqueta inválida o ya existente");
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
        Log.d("AddEventActivity", "Guardando etiqueta en Firebase: " + label);
        DocumentReference docRef = Utility.getCollectionReferenceForLabels().document(label);
        docRef.set(new HashMap<>()).addOnSuccessListener(aVoid -> {
            Log.d("AddEventActivity", "Etiqueta guardada con éxito");
            if (!labels.contains(label)) {
                labels.add(label);
                labelAdapter.notifyDataSetChanged();
                spinnerLabel.setSelection(labels.indexOf(label));
            }
            Toast.makeText(this, "Etiqueta guardada y añadida al Spinner", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error al guardar la etiqueta", Toast.LENGTH_SHORT).show());
    }

    private void loadLabelsFromFirestore() {
        Log.d("AddEventActivity", "Cargando etiquetas desde Firestore");
        labels.clear();
        labels.add("Sin etiqueta");

        Utility.getCollectionReferenceForLabels().get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    String label = document.getId();
                    Log.d("AddEventActivity", "Etiqueta encontrada: " + label);
                    if (!labels.contains(label)) {
                        labels.add(label);
                    }
                }
            }
            labelAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e ->{
                    Log.e("AddEventActivity", "Error al guardar etiqueta", e);
                    Toast.makeText(this, "Error al cargar etiquetas", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveEvent() {
        String eventTitle = etTitle.getText().toString();
        String eventDescription = etDescription.getText().toString();
        String eventDate = tvDate.getText().toString();
        String eventTime = tvTime.getText().toString();
        String eventLabel = spinnerLabel.getSelectedItem().toString();

        Log.d("EventLabel", eventLabel);
        Log.d("EventLabel", eventTitle);
        Log.d("EventLabel", eventDescription);
        Log.d("EventLabel", eventDate);
        Log.d("EventLabel", eventTime);

        if(eventTitle.isEmpty() || eventDescription.isEmpty() || eventDate.isEmpty() || eventTime.isEmpty() || eventLabel.isEmpty() ||
        eventTitle == null || eventDescription == null || eventDate == null || eventTime == null || eventLabel == null){
            Log.w("AddEventActivity", "Campos incompletos");
            Toast.makeText(this, "Please fill in the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Event event = new Event();
        event.setTitle(eventTitle);
        event.setDescription(eventDescription);
        event.setDate(eventDate);
        event.setTime(eventTime);
        event.setLabel(eventLabel);
        event.setStatus("pendiente");

        saveEventToFirebase(event);
    }

    private void saveEventToFirebase(Event event) {
        Log.d("AddEventActivity", "Guardando evento en Firebase: " + event);
        DocumentReference documentReference = Utility.getCollectionReferenceForEvents().document();
        documentReference.set(event).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("MiLog", "Event saved successfully: " + event.getTitle());
                Toast.makeText(AddEventActivity.this, "Evento guardado exitosamente", Toast.LENGTH_SHORT).show();
                clearForm();
                navigateToMainActivity();
            } else {
                Log.e("MiLog", "Error saving event", task.getException());
                Toast.makeText(AddEventActivity.this, "Error al guardar el evento", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearForm() {
        etTitle.setText("");
        etDescription.setText("");
        tvDate.setText("");
        tvTime.setText("");
        spinnerLabel.setSelection(0);
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(AddEventActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Finaliza la actividad actual
    }
}