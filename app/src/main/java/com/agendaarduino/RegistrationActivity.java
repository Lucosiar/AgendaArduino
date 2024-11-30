package com.agendaarduino;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class RegistrationActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etRePassword;
    private Button buttonRegistration;

    private TextView tvHaveAccount, tvLoginRegister;

    private ProgressBar progressBarRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);

        etEmail = findViewById(R.id.etEmailRegister);
        etPassword = findViewById(R.id.etPasswordRegister);
        etRePassword = findViewById(R.id.etRePasswordRegister);
        buttonRegistration = findViewById(R.id.buttonRegistration);
        tvLoginRegister = findViewById(R.id.tvLoginRegister);
        tvHaveAccount = findViewById(R.id.tvHaveAccount);
        progressBarRegister = findViewById(R.id.progressBarRegister);


        // Boton de registro
        buttonRegistration.setOnClickListener(v-> createAccount());

        //Boton volver atrás
        tvLoginRegister.setOnClickListener(v-> changeWindow());
        

    }

    private void changeWindow() {
        Intent i = new Intent(RegistrationActivity.this, LoginActivity.class);
        startActivity(i);
    }

    private void createAccount() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String rePassword = etRePassword.getText().toString();

        boolean isValidated = validateData(email, password, rePassword);

        if(!isValidated){
            return;
        }
        createAccountInFirebase(email, password);
    }

    private void createAccountInFirebase(String email, String password) {
        changeInProgress(true);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            changeInProgress(false);
            if(task.isSuccessful()){
                Toast.makeText(RegistrationActivity.this, "Registro completado", Toast.LENGTH_SHORT).show();
                firebaseAuth.getCurrentUser().sendEmailVerification();
                firebaseAuth.signOut();
                finish();
            }else {
                // Registro fallido
                String errorMessage;
                try {
                    throw task.getException();
                } catch (FirebaseAuthWeakPasswordException e) {
                    errorMessage = "La contraseña es demasiado débil.";
                } catch (FirebaseAuthInvalidCredentialsException e) {
                    errorMessage = "El correo electrónico es inválido.";
                } catch (FirebaseAuthUserCollisionException e) {
                    errorMessage = "Ya existe una cuenta con este correo.";
                } catch (Exception e) {
                    errorMessage = "El registro falló. Inténtalo nuevamente.";
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changeInProgress(boolean inProgress){
        if(inProgress){
            progressBarRegister.setVisibility(View.VISIBLE);
            buttonRegistration.setVisibility(View.GONE);
            tvLoginRegister.setVisibility(View.GONE);
            tvHaveAccount.setVisibility(View.GONE);

        }else{
            progressBarRegister.setVisibility(View.GONE);
            buttonRegistration.setVisibility(View.VISIBLE);
            tvLoginRegister.setVisibility(View.VISIBLE);
            tvHaveAccount.setVisibility(View.VISIBLE);
        }
    }

    public boolean validateData(String email, String password, String rePassword) {
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etEmail.setError("Email is invalid");
            return false;
        }
        if(password.length()<6){
            etPassword.setError("Password must be at least 6 characters");
            return false;
        }
        if(!password.equals(rePassword)){
            etRePassword.setError("Passwords do not match");
            return false;
        }
        return true;
    }

}