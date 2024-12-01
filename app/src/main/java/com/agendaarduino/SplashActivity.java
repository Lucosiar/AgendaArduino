package com.agendaarduino;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Crear un Handler asociado al Main Looper
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if(currentUser == null){
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }else{
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
            finish();
        }, 1000);

    }
}