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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import kotlin.OptionalExpectation;

public class MainActivity extends AppCompatActivity {

    private ImageButton buttonAddEvent;
    private TextView tvDiaActual;
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private ImageView buttonSettings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonAddEvent = findViewById(R.id.buttonAddEvent);
        tvDiaActual = findViewById(R.id.tvDiaActual);
        recyclerView = findViewById(R.id.recyclerView);
        buttonSettings = findViewById(R.id.buttonSettings);

        LocalDate fechaActual = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaFormateada = fechaActual.format(formatter);
        tvDiaActual.setText(fechaFormateada);

        buttonAddEvent.setOnClickListener(v-> newEvent());
        buttonSettings.setOnClickListener(v -> openSettings());
        setUpRecyclerView();

        loadUserEvent();
    }

    private void openSettings() {
        PopupMenu popupMenu = new PopupMenu(MainActivity.this, buttonSettings);
        popupMenu.getMenuInflater().inflate(R.menu.main_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_settings) {
                Toast.makeText(MainActivity.this, "Ajustes seleccionados", Toast.LENGTH_SHORT).show();
                return true;
            } else if (item.getItemId() == R.id.action_logout) {
                logout();
                return true;
            } else {
                return false;
            }
        });
        popupMenu.show();
    }

    private void logout() {
        Toast.makeText(MainActivity.this, "Cerrando sesión", Toast.LENGTH_SHORT).show();

        FirebaseAuth.getInstance().signOut();
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }


    private void setUpRecyclerView() {
        List<Event> eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(this, eventList, event -> editEvent());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(eventAdapter);
    }

    private void loadUserEvent(){
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Utility.getCollectionReferenceForEvents()
                .whereEqualTo("idUser", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> eventList = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            eventList.add(event);
                        }
                    }
                    eventAdapter.setEventList(eventList);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar eventos", Toast.LENGTH_SHORT).show()
                );

    }

    private void newEvent() {
        Intent i = new Intent(MainActivity.this, AddEventActivity.class);
        startActivity(i);
    }

    private void editEvent(){
        Intent i = new Intent(MainActivity.this, EditEventActivity.class);
        startActivity(i);
    }
}