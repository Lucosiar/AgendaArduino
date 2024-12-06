package com.agendaarduino;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    EventAdapter eventAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonAddEvent = findViewById(R.id.buttonAddEvent);
        tvDiaActual = findViewById(R.id.tvDiaActual);
        recyclerView = findViewById(R.id.recyclerView);

        LocalDate fechaActual = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaFormateada = fechaActual.format(formatter);

        // Muestra la fecha en el TextView
        tvDiaActual.setText(fechaFormateada);

        buttonAddEvent.setOnClickListener(v-> newEvent());
        
        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        List<Event> eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(this, eventList, event -> {
            // Manejo del clic en un evento
            Toast.makeText(this, "Evento: " + event.getTitle(), Toast.LENGTH_SHORT).show();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(eventAdapter);

        // Cargar eventos desde Firestore
        Utility.getCollectionReferenceForEvents()
                .orderBy("time", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            eventList.add(event);
                        }
                    }
                    eventAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar eventos", Toast.LENGTH_SHORT).show()
                );
    }

    private void newEvent() {
        Intent i = new Intent(MainActivity.this, AddEventActivity.class);
        startActivity(i);
    }

}