package com.agendaarduino;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddRoutinesActivity extends AppCompatActivity {

    private Spinner spinnerLabelRoutine, spinnerRecordatoryRoutine;
    private EditText etTitleRoutine,etDescriptionRoutine;

    private TextView tvButtonSelectDaysRoutine, tvTimeRoutine;

    private ImageButton buttonAddTimeRoutine, buttonNewLabelRoutine, buttonRecordatoryRoutine;
    private int selectedHour, selectedMinute;

    private ArrayList<String> labels = new ArrayList<>();
    private ArrayAdapter<String> labelAdapter;
    private Button buttonSaveRoutine;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_routines);

        inicialice();

        // Configuración de spinner para las opciones de recordatorio
        String[] recordatoryOptions = {"Sin recordatorio", "15 minutos", "30 minutos", "1 hora"};
        ArrayAdapter<String> recordatoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                recordatoryOptions
        );
        recordatoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRecordatoryRoutine.setAdapter(recordatoryAdapter);

        // Configuración de spinner para las opciones de las etiquetas
        labelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);
        labelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLabelRoutine.setAdapter(labelAdapter);

        spinnerLabelRoutine.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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



        // TextView para seleccionar los días de la semana
        tvButtonSelectDaysRoutine.setOnClickListener(v -> openSelectDaysRoutine());

        // Botón para mostrar selectores de hora, etiquetas y recordatorios
        buttonAddTimeRoutine.setOnClickListener(view -> showTimePickerDialog());
        buttonNewLabelRoutine.setOnClickListener(view -> showAddLabelDialog());
        buttonRecordatoryRoutine.setOnClickListener(view -> showTimePickerDialog());
        tvTimeRoutine.setOnClickListener(view -> showTimePickerDialog());

        // Botón guardar routine
        buttonSaveRoutine.setOnClickListener(view -> saveRoutine());

    }

    private void openSelectDaysRoutine() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.popup_days, null);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.showAtLocation(tvButtonSelectDaysRoutine, Gravity.CENTER, 0, 0);

        CheckBox chkMonday = popupView.findViewById(R.id.chkMonday);
        CheckBox chkTuesday = popupView.findViewById(R.id.chkTuesday);
        CheckBox chkWednesday = popupView.findViewById(R.id.chkWednesday);
        CheckBox chkThursday = popupView.findViewById(R.id.chkThursday);
        CheckBox chkFriday = popupView.findViewById(R.id.chkFriday);
        CheckBox chkSaturday = popupView.findViewById(R.id.chkSaturday);
        CheckBox chkSunday = popupView.findViewById(R.id.chkSunday);
        Button btnDone = popupView.findViewById(R.id.btnDone);

        popupWindow.showAsDropDown(tvButtonSelectDaysRoutine);

        btnDone.setOnClickListener(doneView -> {
            List<String> selectedDays = new ArrayList<>();
            if (chkMonday.isChecked()) selectedDays.add("Lunes");
            if (chkTuesday.isChecked()) selectedDays.add("Martes");
            if (chkWednesday.isChecked()) selectedDays.add("Miércoles");
            if (chkThursday.isChecked()) selectedDays.add("Jueves");
            if (chkFriday.isChecked()) selectedDays.add("Viernes");
            if (chkSaturday.isChecked()) selectedDays.add("Sábado");
            if (chkSunday.isChecked()) selectedDays.add("Domingo");

            if (!selectedDays.isEmpty()) {
                tvButtonSelectDaysRoutine.setText(String.join(", ", selectedDays));
            } else {
                tvButtonSelectDaysRoutine.setText(R.string.seleccionar_dias);
            }

            popupWindow.dismiss();
        });
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

    private void saveRoutine(){
        String routineTitle = etTitleRoutine.getText().toString();
        String routineDescription = etDescriptionRoutine.getText().toString();
        String routineDay = tvButtonSelectDaysRoutine.getText().toString();
        String routineTime = tvTimeRoutine.getText().toString();
        String routineLabel = spinnerLabelRoutine.getSelectedItem().toString();
        String routineRecordatory = spinnerRecordatoryRoutine.getSelectedItem().toString();

        if(routineTitle.isEmpty() || routineDay.isEmpty() || routineTime.isEmpty()
                || routineLabel.isEmpty() || routineRecordatory.isEmpty()){
            Toast.makeText(this, "Please fill in the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if(routineDescription.isEmpty()){
            routineDescription = "Sin descripción";
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null){
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        Routine routine = new Routine();
        routine.setTitle(routineTitle);
        routine.setDescription(routineDescription);
        routine.setDaysOfWeek(routineDay);
        routine.setTime(routineTime);
        routine.setLabel(routineLabel);
        routine.setRecordatory(routineRecordatory);
        routine.setStatusDay("pendiente");
        routine.setIdUser(userId);

        saveRoutineToFirebase(routine);
    }

    private void saveRoutineToFirebase(Routine routine) {
        DocumentReference documentReference = Utility.getCollectionReferenceForRoutines().document();
        String routineId = documentReference.getId();
        routine.setIdRoutine(routineId);

        documentReference.set(routine).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(AddRoutinesActivity.this, "Rutina guardada exitosamente", Toast.LENGTH_SHORT).show();
                clearForm();
                navigateToMainActivity();
            } else {
                Toast.makeText(AddRoutinesActivity.this, "Error al guardar la rutina", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearForm() {
        etTitleRoutine.setText("");
        etDescriptionRoutine.setText("");
        tvButtonSelectDaysRoutine.setText("");
        tvTimeRoutine.setText("");
        spinnerLabelRoutine.setSelection(0);
        spinnerRecordatoryRoutine.setSelection(0);
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(AddRoutinesActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            selectedHour = hourOfDay;
            selectedMinute = minute;
            tvTimeRoutine.setText(String.format("%02d:%02d", hourOfDay, minute));
        }, selectedHour, selectedMinute, true);
        timePickerDialog.show();
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
                spinnerLabelRoutine.setSelection(labels.indexOf(label));
            }
            Toast.makeText(this, "Etiqueta guardada y añadida al Spinner", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error al guardar la etiqueta", Toast.LENGTH_SHORT).show());
    }

    private void inicialice(){
        spinnerLabelRoutine = findViewById(R.id.spinnerLabelRoutine);
        spinnerRecordatoryRoutine = findViewById(R.id.spinnerRecordatoryRoutine);

        etTitleRoutine = findViewById(R.id.etTitleRoutine);
        etDescriptionRoutine = findViewById(R.id.etDescriptionRoutine);

        tvButtonSelectDaysRoutine = findViewById(R.id.tvButtonSelectDaysRoutine);
        tvTimeRoutine = findViewById(R.id.tvTimeRoutine);

        buttonAddTimeRoutine = findViewById(R.id.buttonAddTimeRoutine);
        buttonNewLabelRoutine = findViewById(R.id.buttonNewLabelRoutine);
        buttonRecordatoryRoutine = findViewById(R.id.buttonRecordatoryRoutine);
        buttonSaveRoutine = findViewById(R.id.buttonSaveRoutine);
    }


}