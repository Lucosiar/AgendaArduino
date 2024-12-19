package com.agendaarduino;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kotlin.OptionalExpectation;

public class MainActivity extends AppCompatActivity {

    private ImageButton buttonNewAction;
    private TextView tvDiaActual;
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private ImageView buttonSettings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inicialice();

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
        // Botón de ajustes
        buttonSettings.setOnClickListener(v -> openSettings());
        setUpRecyclerView();

        loadUserEvent();
    }

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
            } else if (item.getItemId() == R.id.prueba1) {
                Toast.makeText(MainActivity.this, "Prueba 1", Toast.LENGTH_SHORT).show();
                return true;
            }else{
                return false;
            }
        });
        popupMenu.show();
    }

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
            } else if(item.getItemId() == R.id.all_events){
                showAllEvents();
                return true;
            }
            else if(item.getItemId() == R.id.add_routines){
                showAllEvents();
                return true;
            }
            else {
                return false;
            }
        });
        popupMenu.show();
    }

    private void showAllEvents() {
        Intent i = new Intent(MainActivity.this, AllEventsActivity.class);
        startActivity(i);
    }

    private void logout() {
        Toast.makeText(MainActivity.this, "Cerrando sesión", Toast.LENGTH_SHORT).show();

        FirebaseAuth.getInstance().signOut();
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    private void goToSettings(){
        Intent i = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(i);
    }

    private void setUpRecyclerView() {
        List<Event> eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(this, eventList, event -> editEvent());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(eventAdapter);
    }

    private void loadUserEvent(){
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        LocalDate fechaActual = LocalDate.now();

        Utility.getCollectionReferenceForEvents()
                .whereEqualTo("idUser", currentUserId)  // Filtra por usuario
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(MainActivity.this, "Error al cargar eventos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        List<Event> eventList = new ArrayList<>();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Event event = doc.toObject(Event.class);
                            if (event != null) {
                                LocalDate fechaEvento = LocalDate.parse(event.getDate(), DateTimeFormatter.ofPattern("yyyy/MM/dd"));

                                // Filtra eventos de días anteriores solo si son "pendientes"
                                if (fechaEvento.isBefore(fechaActual) && "pendiente".equals(event.getStatus())) {
                                    eventList.add(event);
                                }
                                // En el caso del día actual, agregar todos los eventos sin importar el estado
                                else if (fechaEvento.equals(fechaActual)) {
                                    eventList.add(event);
                                }
                            }
                        }
                        eventAdapter.setEventList(eventList);
                    }
                });
    }

    private void newEvent() {
        Intent i = new Intent(MainActivity.this, AddEventActivity.class);
        startActivity(i);
    }

    private void newRoutine() {
        Intent i = new Intent(MainActivity.this, AddRoutinesActivity.class);
        startActivity(i);
    }

    private void editEvent(){
        Intent i = new Intent(MainActivity.this, EditEventActivity.class);
        startActivity(i);
    }

    private void inicialice(){
        // Inicialización
        buttonNewAction = findViewById(R.id.buttonNewAction);
        tvDiaActual = findViewById(R.id.tvDiaActual);
        recyclerView = findViewById(R.id.recyclerView);
        buttonSettings = findViewById(R.id.buttonSettings);
    }
}