package com.agendaarduino;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class EditRoutineActivity extends AppCompatActivity {

    private TextView tvTimeRoutine;
    private Spinner spinnerLabelRoutine, spinnerRecordatoryRoutine;
    private Button btnUpdateRoutine;
    private String routineId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_routine);

        tvTimeRoutine = findViewById(R.id.tvTimeRoutine);
        spinnerLabelRoutine = findViewById(R.id.spinnerLabelRoutine);
        spinnerRecordatoryRoutine = findViewById(R.id.spinnerRecordatoryRoutine);
        btnUpdateRoutine = findViewById(R.id.btnUpdateRoutine);

        loadRoutineData();

        btnUpdateRoutine.setOnClickListener(v -> updateRoutineInFirebase());
    }

    private void loadRoutineData() {
        Intent intent = getIntent();
        routineId = intent.getStringExtra("routineId");

        tvTimeRoutine.setText(intent.getStringExtra("routineTime"));
        spinnerLabelRoutine.setSelection(getIndex(spinnerLabelRoutine, intent.getStringExtra("routineLabel")));
        spinnerRecordatoryRoutine.setSelection(getIndex(spinnerRecordatoryRoutine, intent.getStringExtra("routineRecordatory")));
    }

    private int getIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(value)) {
                return i;
            }
        }
        return 0;
    }

    private void updateRoutineInFirebase() {
        String time = tvTimeRoutine.getText().toString();
        String label = spinnerLabelRoutine.getSelectedItem().toString();
        String recordatory = spinnerRecordatoryRoutine.getSelectedItem().toString();
        String hourCalculate = calculateHour(time, recordatory);

        FirebaseFirestore.getInstance()
                .collection("routines")
                .document(routineId)
                .update(
                        "time", time,
                        "label", label,
                        "recordatory", recordatory,
                        "hourCalculate", hourCalculate
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
