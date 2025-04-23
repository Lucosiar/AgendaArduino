package com.agendaarduino;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EditRoutineActivity extends AppCompatActivity {

    private EditText etTitleRoutine, etDescriptionRoutine;
    private TextView tvTimeRoutine, tvButtonSelectDaysRoutine;
    private Spinner spinnerLabelRoutine, spinnerRecordatoryRoutine;
    private Button btnUpdateRoutine;
    private ImageButton buttonAddTimeRoutine, buttonNewLabelRoutine, buttonRecordatoryRoutine;
    private TextView tvCheckListRoutine;

    private String routineId;
    private List<String> selectedDays = new ArrayList<>();
    private List<String> userLabels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_routine);

        initViews();
        loadRoutineData();
        setupListeners();
        loadUserLabels();
        setupRecordatorySpinner();
    }

    private void initViews() {
        etTitleRoutine = findViewById(R.id.etTitleRoutine);
        etDescriptionRoutine = findViewById(R.id.etDescriptionRoutine);
        tvTimeRoutine = findViewById(R.id.tvTimeRoutine);
        tvButtonSelectDaysRoutine = findViewById(R.id.tvButtonSelectDaysRoutine);
        spinnerLabelRoutine = findViewById(R.id.spinnerLabelRoutine);
        spinnerRecordatoryRoutine = findViewById(R.id.spinnerRecordatoryRoutine);
        btnUpdateRoutine = findViewById(R.id.btnUpdateRoutine);
        buttonAddTimeRoutine = findViewById(R.id.buttonAddTimeRoutine);
        buttonNewLabelRoutine = findViewById(R.id.buttonNewLabelRoutine);
        buttonRecordatoryRoutine = findViewById(R.id.buttonRecordatoryRoutine);
        tvCheckListRoutine = findViewById(R.id.tvCheckListRoutine);
    }

    private void setupListeners() {
        buttonAddTimeRoutine.setOnClickListener(v -> showTimePickerDialog());
        buttonRecordatoryRoutine.setOnClickListener(v -> showTimePickerDialog());

        buttonNewLabelRoutine.setOnClickListener(v -> showNewLabelDialog());

        tvButtonSelectDaysRoutine.setOnClickListener(v -> showDaysSelectionDialog());

        btnUpdateRoutine.setOnClickListener(v -> updateRoutineInFirebase());
    }

    private void loadRoutineData() {
        Intent intent = getIntent();
        routineId = intent.getStringExtra("routineId");

        etTitleRoutine.setText(intent.getStringExtra("routineTitle"));
        etDescriptionRoutine.setText(intent.getStringExtra("routineDescription"));
        tvTimeRoutine.setText(intent.getStringExtra("routineTime"));

        String routineDays = intent.getStringExtra("routineDays");
        if (routineDays != null) {
            selectedDays = Arrays.asList(intent.getStringExtra("routineDays").split(","));
        } else {
            selectedDays = new ArrayList<>();
        }
        tvButtonSelectDaysRoutine.setText(TextUtils.join(", ", selectedDays));

        // Aquí es donde añades los ítems de la checklist al TextView
        List<String> checklistItems = Arrays.asList("Ejemplo 1", "Ejemplo 2");
        tvCheckListRoutine.setText(TextUtils.join("\n• ", checklistItems));
    }


    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
            tvTimeRoutine.setText(selectedTime);
        }, hour, minute, true).show();
    }

    private void showNewLabelDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nueva etiqueta");

        final EditText input = new EditText(this);
        input.setHint("Nombre de la etiqueta");
        builder.setView(input);

        builder.setPositiveButton("Añadir", (dialog, which) -> {
            String newLabel = input.getText().toString().trim();
            if (!newLabel.isEmpty()) {
                userLabels.add(newLabel);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userLabels);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerLabelRoutine.setAdapter(adapter);
                spinnerLabelRoutine.setSelection(userLabels.indexOf(newLabel));
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void showDaysSelectionDialog() {
        String[] days = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        boolean[] checked = new boolean[days.length];

        for (int i = 0; i < days.length; i++) {
            checked[i] = selectedDays.contains(days[i]);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecciona días")
                .setMultiChoiceItems(days, checked, (dialog, which, isChecked) -> {
                    if (isChecked) {
                        selectedDays.add(days[which]);
                    } else {
                        selectedDays.remove(days[which]);
                    }
                })
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    tvButtonSelectDaysRoutine.setText(TextUtils.join(", ", selectedDays));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void loadUserLabels() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("labels")
                .whereEqualTo("idUser", userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    userLabels.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String label = doc.getString("name");
                        userLabels.add(label);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userLabels);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerLabelRoutine.setAdapter(adapter);

                    String labelFromIntent = getIntent().getStringExtra("routineLabel");
                    if (labelFromIntent != null) {
                        int index = userLabels.indexOf(labelFromIntent);
                        if (index >= 0) spinnerLabelRoutine.setSelection(index);
                    }
                });
    }

    private void setupRecordatorySpinner() {
        String[] options = {"Sin recordatorio", "Misma hora", "15 minutos", "30 minutos", "1 hora", "07:00", "08:00", "09:00"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRecordatoryRoutine.setAdapter(adapter);

        String recordatoryFromIntent = getIntent().getStringExtra("routineRecordatory");
        if (recordatoryFromIntent != null) {
            int index = Arrays.asList(options).indexOf(recordatoryFromIntent);
            if (index >= 0) spinnerRecordatoryRoutine.setSelection(index);
        }
    }

    private void updateRoutineInFirebase() {
        String title = etTitleRoutine.getText().toString().trim();
        String description = etDescriptionRoutine.getText().toString().trim();
        String time = tvTimeRoutine.getText().toString().trim();
        String label = spinnerLabelRoutine.getSelectedItem().toString();
        String recordatory = spinnerRecordatoryRoutine.getSelectedItem().toString();
        String hourCalculate = calculateHour(time, recordatory);
        String days = TextUtils.join(",", selectedDays);

        FirebaseFirestore.getInstance()
                .collection("routines")
                .document(routineId)
                .update(
                        "title", title,
                        "description", description,
                        "time", time,
                        "label", label,
                        "recordatory", recordatory,
                        "hourCalculate", hourCalculate,
                        "days", days
                )
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Rutina actualizada", Toast.LENGTH_SHORT).show();
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
}



