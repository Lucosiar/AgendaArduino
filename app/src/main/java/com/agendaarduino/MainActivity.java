package com.agendaarduino;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.type.DateTime;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ImageButton buttonNewAction, buttonShowCaledar;
    private TextView tvDiaActual;
    private RecyclerView recyclerView;
    private ActionAdapter actionAdapter;
    private ImageView buttonSettings;
    private static final int REQUEST_CODE_VIBRATE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkExactAlarmPermission();
        inicialice();
        inicialiceFirebase();

        // Obtener la fecha actual
        LocalDate fechaActual = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Obtener día de la semana
        DayOfWeek diaSemana = fechaActual.getDayOfWeek();
        String nombreDia = diaSemana.getDisplayName(java.time.format.TextStyle.FULL, new Locale("es", "ES"));

        // Convertir la primera letra a mayúscula y el resto a minúscula
        nombreDia = nombreDia.substring(0, 1).toUpperCase() + nombreDia.substring(1).toLowerCase();

        // Mostrar día y fecha actual
        String fechaFormateada = nombreDia + " - " + fechaActual.format(formatter);
        tvDiaActual.setText(fechaFormateada);

        // Botón nuevo evento / rutina
        buttonNewAction.setOnClickListener(v-> openNewAction());

        // Botón ver calendario
        buttonShowCaledar.setOnClickListener(v -> openCalendar());

        // Botón de ajustes
        buttonSettings.setOnClickListener(v -> openSettings());
        setUpRecyclerView();

        loadUserAction();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_VIBRATE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido
            } else {
                // Permiso denegado
                Toast.makeText(this, "Permiso VIBRATE denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Log.w("AlarmError", "Solicitando permiso para alarmas exactas...");
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }

    // Botón nuevo evento / rutina
    private void openNewAction(){
        PopupMenu popupMenu = new PopupMenu(MainActivity.this, buttonNewAction);
        popupMenu.getMenuInflater().inflate(R.menu.main_menu_action, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.add_events) {
                newEvent();
                return true;
            } else if (item.getItemId() == R.id.add_routines) {
                newRoutine();
                return true;
            } else{
                return false;
            }
        });
        popupMenu.show();
    }

    private void openCalendar(){
        Intent i = new Intent(MainActivity.this, CalendarActivity.class);
        startActivity(i);
    }

    // Abrir menú superior
    private void openSettings() {
        PopupMenu popupMenu = new PopupMenu(MainActivity.this, buttonSettings);
        popupMenu.getMenuInflater().inflate(R.menu.main_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_settings) {
                goToSettings();
                return true;
            } else if (item.getItemId() == R.id.action_logout) {
                logout();
                return true;
            }
            else {
                return false;
            }
        });
        popupMenu.show();
    }

    // Cerrar sesión
    private void logout() {
        Toast.makeText(MainActivity.this, "Cerrando sesión", Toast.LENGTH_SHORT).show();

        FirebaseAuth.getInstance().signOut();
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    // Ir a pantalla de ajustes
    private void goToSettings(){
        Intent i = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(i);
    }

    // Preparamos el recycler para mostrar acciones
    private void setUpRecyclerView() {
        List<Action> actionList = new ArrayList<>();
        actionAdapter = new ActionAdapter(this, actionList, action -> {
            // De momento no se hace nada
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(actionAdapter);
    }

    // Cargamos el usuario y sus acciones
    private void loadUserAction() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        LocalDate fechaActual = LocalDate.now();
        String diaActual = fechaActual.getDayOfWeek()
                .getDisplayName(TextStyle.FULL, new Locale("es", "ES"));

        diaActual = diaActual.substring(0, 1).toUpperCase() + diaActual.substring(1).toLowerCase();

        List<Action> allActions = new ArrayList<>();

        Utility.getCollectionReferenceForEvents()
                .whereEqualTo("idUser", currentUserId)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("DEBUG", "Error al cargar eventos", e);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        List<Action> tempEventList = new ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Event event = doc.toObject(Event.class);
                            if (event != null) {
                                Log.d("DEBUG", "Evento cargado: " + event.getTitle() + " - Fecha: " + event.getDate());

                                LocalDate eventDate = LocalDate.parse(event.getDate(), DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                                if(eventDate.isEqual(fechaActual) || (eventDate.isBefore(fechaActual) && !event.getStatus().equals("completado"))){
                                    tempEventList.add(event);
                                }
                            }
                        }
                        allActions.clear();
                        allActions.addAll(tempEventList);

                        // Cargar rutinas después de cargar los eventos
                        loadUserRoutinesForToday(currentUserId, allActions);
                    }
                });
    }

    // Cargamos las rutinas del usuario para el día de hoy
    private void loadUserRoutinesForToday(String userId, List<Action> todayActions) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE", new Locale("es", "ES"));

        String todayDayOfWeek = today.format(dayFormatter);

        Utility.getCollectionReferenceForRoutines()
                .whereEqualTo("idUser", userId)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(MainActivity.this, "Error al cargar rutinas", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        List<Action> tempRoutineList = new ArrayList<>(todayActions);
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Routine routine = doc.toObject(Routine.class);
                            if (routine != null) {
                                if (routine.getDaysOfWeek().toLowerCase().contains(todayDayOfWeek.toLowerCase())) {
                                    tempRoutineList.add(routine);
                                }
                            }
                        }
                        actionAdapter.setActionList(tempRoutineList);
                    }
                });
    }

    // Creación de nuevos eventos
    private void newEvent() {
        Intent i = new Intent(MainActivity.this, AddEventActivity.class);
        startActivity(i);
    }

    // Creación de nuevas rutinas
    private void newRoutine() {
        Intent i = new Intent(MainActivity.this, AddRoutinesActivity.class);
        startActivity(i);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notely";
            String description = "Channel Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("channel_id", name, importance);
            channel.setDescription(description);
            channel.setShowBadge(true);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void inicialiceFirebase(){
        FirebaseApp.initializeApp(this);
        createNotificationChannel();
    }

    private void inicialice(){
        buttonNewAction = findViewById(R.id.buttonNewAction);
        tvDiaActual = findViewById(R.id.tvDiaActual);
        recyclerView = findViewById(R.id.recyclerView);
        buttonSettings = findViewById(R.id.buttonSettings);
        buttonShowCaledar = findViewById(R.id.buttonShowCaledar);

    }
}