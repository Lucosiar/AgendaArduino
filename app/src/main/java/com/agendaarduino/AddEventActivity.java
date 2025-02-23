package com.agendaarduino;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddEventActivity extends AppCompatActivity {

    private EditText etTitleEvent, etDescriptionEvent;
    private Button buttonSaveEvent;
    private Spinner spinnerLabelEvent, spinnerRecordatoryEvent;
    private ImageButton buttonNewLabelEvent, buttonAddDateEvent, buttonAddTimeEvent, buttonRecordatoryEvent;
    private TextView tvDateEvent, tvTimeEvent, tvCheckListEvent;
    private ArrayList<String> labels = new ArrayList<>();
    private ArrayAdapter<String>labelAdapter;
    private int selectedHour, selectedMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        initialize();

        // Configuración de spinner para las opciones de recordatorio
        String[] recordatoryOptions = {"Sin recordatorio", "15 minutos", "30 minutos", "1 hora", "Misma hora", "Personalizado"};
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

        // Spiner label event
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

        // Spinner recordatory event
        spinnerRecordatoryEvent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOption = (String) parent.getItemAtPosition(position);

                if ("Personalizado".equals(selectedOption)) {
                    showTimePickerDialogRecordatory();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada si no se selecciona nada
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
        buttonAddTimeEvent.setOnClickListener((view -> showTimePickerDialogTime()));
        buttonRecordatoryEvent.setOnClickListener((view -> showTimePickerDialogRecordatory()));

        // Textview que muestra también los selectores para la fecha y hora
        tvDateEvent.setOnClickListener((view -> showDatePickerDialog()));
        tvTimeEvent.setOnClickListener((view -> showTimePickerDialogTime()));

        tvCheckListEvent.setOnClickListener((view -> popUpCheckList()));

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
        tvCheckListEvent = findViewById(R.id.tvCheckListEvent);
    }

    private void popUpCheckList() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.popup_add_checklist, null);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true);

        popupWindow.showAtLocation(tvCheckListEvent, Gravity.CENTER, 0, 0);

        // Referencias a los elementos del popup
        EditText editTextChecklistItem = popupView.findViewById(R.id.editTextChecklistItem);
        Button btnDone = popupView.findViewById(R.id.buttonSaveChecklist);

        // Referencia al contenedor en activity_add_event.xml
        LinearLayout checkListContainer = findViewById(R.id.checkListContainer);

        // Scroll bar
        ScrollView scrollView = findViewById(R.id.scrollViewEvents);
        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.setHorizontalScrollBarEnabled(false);


        // Configurar el botón para agregar una nueva tarea al contenedor
        btnDone.setOnClickListener(v -> {
            String newTask = editTextChecklistItem.getText().toString().trim();

            if (!newTask.isEmpty()) {
                // Crear un nuevo TextView para mostrar la tarea
                TextView newTaskTextView = new TextView(this);
                newTaskTextView.setText(newTask);
                newTaskTextView.setTextSize(16);
                newTaskTextView.setPadding(10, 10, 10, 10);
                newTaskTextView.setTextColor(getResources().getColor(R.color.text_color, null));

                // Agregar el nuevo TextView al contenedor
                checkListContainer.addView(newTaskTextView);

                // Limpiar el EditText
                editTextChecklistItem.setText("");
            }

            // Cerrar el popup
            popupWindow.dismiss();
        });
    }

    private List<String> getChecklistItems() {
        List<String> checklistItems = new ArrayList<>();
        LinearLayout checkListContainer = findViewById(R.id.checkListContainer);

        for (int i = 0; i < checkListContainer.getChildCount(); i++) {
            View child = checkListContainer.getChildAt(i);
            if (child instanceof TextView) {
                String item = ((TextView) child).getText().toString();
                checklistItems.add(item);
            }
        }

        return checklistItems;
    }

    private void showTimePickerDialogTime() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            selectedHour = hourOfDay;
            selectedMinute = minute;
            tvTimeEvent.setText(String.format("%02d:%02d", hourOfDay, minute));
        }, selectedHour, selectedMinute, true);
        timePickerDialog.show();
    }

    private void showTimePickerDialogRecordatory() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            selectedHour = hourOfDay;
            selectedMinute = minute;

            String customTime = String.format("%02d:%02d", hourOfDay, minute);
            ((ArrayAdapter<String>) spinnerRecordatoryEvent.getAdapter())
                    .insert(customTime, spinnerRecordatoryEvent.getCount() - 1);
            spinnerRecordatoryEvent.setSelection(spinnerRecordatoryEvent.getCount() - 2);
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

        String hourCalculate = calculateHour(eventTime, eventRecordatory);

        Event event = new Event();
        event.setTitle(eventTitle);
        event.setDescription(eventDescription);
        event.setDate(eventDate);
        event.setTime(eventTime);
        event.setLabel(eventLabel);
        event.setRecordatory(eventRecordatory);
        event.setHourCalculate(hourCalculate);
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
                saveChecklistItems(eventId);
                scheduleEventNotification(event);
                clearForm();
                navigateToMainActivity();
            } else {
                Toast.makeText(AddEventActivity.this, "Error al guardar el evento", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void scheduleEventNotification(Event event) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            // Verificar si la API es >= 31 (Android 12) antes de llamar a canScheduleExactAlarms()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Log.w("AlarmError", "No se tienen permisos para programar alarmas exactas.");
                return; // No continuar si no se pueden programar alarmas exactas
            }

            Intent intent = new Intent(this, MyNotificationReceiver.class);
            intent.putExtra("title", event.getTitle());
            intent.putExtra("body", event.getDescription());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, event.getIdEvent().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            if (!event.getHourCalculate().equals("0")) {
                String dateTimeString = event.getDate() + " " + event.getHourCalculate();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
                LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatter);
                long triggerTime = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

                Log.d("AlarmError NotificationDebug", "Programando notificación: " + event.getTitle());
                Log.d("AlarmError NotificationDebug", "Fecha y hora del evento: " + dateTimeString);
                Log.d("AlarmError NotificationDebug", "Tiempo en milisegundos: " + triggerTime);
                Log.d("AlarmError NotificationDebug", "Hora actual en milisegundos: " + System.currentTimeMillis());

                try {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                    Log.d("AlarmError", "Alarma programada para: " + dateTimeString);
                } catch (SecurityException e) {
                    Log.e("AlarmError", "No se pudo programar la alarma exacta", e);
                }
            } else{
                Log.e("AlarmError", "La hora calculada es 0, no se programo ninguna alarma");
            }
        } else {
            Log.w("AlarmError", "AlarmManager es null.");
        }
    }


    private void saveChecklistItems(String eventId){
        List<String> checklistItems = getChecklistItems();
        CollectionReference checklistRef = Utility.getCollectionReferenceForChecklist();

        if(checklistItems.isEmpty() || checklistItems==null){
            return;
        }

        for (String item : checklistItems) {
            String checklistId = checklistRef.document().getId();

            ChecklistItem checklistItem = new ChecklistItem(
                    eventId,
                    item,
                    "pendiente"
            );
            checklistItem.setIdChecklist(checklistId);

            checklistRef.document(checklistId).set(checklistItem).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("Firestore", "Ítem del checklist guardado: " + item);
                } else {
                    Log.e("Firestore", "Error al guardar el ítem del checklist: " + item, task.getException());
                }
            });
        }
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

    private void clearForm() {
        etTitleEvent.setText("");
        etDescriptionEvent.setText("");
        tvDateEvent.setText("");
        tvTimeEvent.setText("");
        spinnerLabelEvent.setSelection(0);
        spinnerRecordatoryEvent.setSelection(0);
        tvCheckListEvent.setText("");
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(AddEventActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}