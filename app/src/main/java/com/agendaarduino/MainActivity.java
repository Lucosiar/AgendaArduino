package com.agendaarduino;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {

    private Button buttonAddEvent, buttonRegistrationPrueba;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth.getInstance().setLanguageCode("es");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();


        buttonAddEvent = findViewById(R.id.buttonAddEvent);
        buttonRegistrationPrueba = findViewById(R.id.buttonRegistrationPrueba);

        buttonRegistrationPrueba.setOnClickListener(v-> change());


    }

    private void change() {
        Intent i = new Intent(MainActivity.this, RegistrationActivity.class);
        startActivity(i);
    }
}