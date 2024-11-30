package com.agendaarduino;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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

public class AddEventActivity extends AppCompatActivity {

    private EditText etTitle, etDescription;
    private Button buttonSaveEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_event);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        buttonSaveEvent = findViewById(R.id.buttonSaveEvent);

        buttonSaveEvent.setOnClickListener((view -> saveEvent()));

    }

    private void saveEvent() {
        String eventTitle = etTitle.getText().toString();
        String eventDescription = etDescription.getText().toString();

        if(eventTitle==null || eventDescription==null || eventTitle.isEmpty() || eventDescription.isEmpty()){
            Toast.makeText(this, "Please enter a title and description", Toast.LENGTH_SHORT).show();
            return;
        }

        Event event = new Event();
        event.setTitle(eventTitle);
        event.setDescription(eventDescription);

        saveEventToFirebase(event);

    }

    private void saveEventToFirebase(Event event) {
        DocumentReference documentReference;
        documentReference = Utility.getCollectionReferenceForEvents().document();

        documentReference.set(event).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(AddEventActivity.this, "Event saved successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddEventActivity.this, "Failed while saving event", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}