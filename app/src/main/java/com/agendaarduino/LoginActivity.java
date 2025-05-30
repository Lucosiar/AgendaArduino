package com.agendaarduino;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmailLogin, etPasswordLogin;
    private Button buttonLogin;
    private TextView tvNotAccount, tvRegistrate, tvPasswordRemember;
    
    private ImageView imageChrome;

    private SignInClient mSignInClient;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        variablesInitialization();

        mAuth = FirebaseAuth.getInstance();

        mSignInClient = Identity.getSignInClient(this);

        buttonLogin.setOnClickListener(v -> login());
        tvNotAccount.setOnClickListener(v -> navigateToRegister());
        tvRegistrate.setOnClickListener(v -> navigateToRegister());
        tvPasswordRemember.setOnClickListener(v -> forgotPassword());
        imageChrome.setOnClickListener(v -> loginWithGoogle());
    }

    public void variablesInitialization(){
        etEmailLogin = findViewById(R.id.etEmailLogin);
        etPasswordLogin = findViewById(R.id.etPasswordLogin);
        buttonLogin = findViewById(R.id.buttonLogin);
        tvNotAccount = findViewById(R.id.tvNotAccount);
        tvRegistrate = findViewById(R.id.tvRegistrate);
        tvPasswordRemember = findViewById(R.id.tvPasswordRemember);
        imageChrome= findViewById(R.id.imageChrome);
    }

    private void loginWithGoogle() {
        // Inicio de sesion con Google
    }

    private void forgotPassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recuperar contraseña");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setHint("Introduce tu correo electrónico");
        builder.setView(input);

        builder.setPositiveButton("Enviar", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (!TextUtils.isEmpty(email)) {
                sendPasswordResetEmail(email);
            } else {
                Toast.makeText(LoginActivity.this, "Por favor, introduce un correo válido", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void sendPasswordResetEmail(String email) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Correo de recuperación enviado", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Error: " + task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void login(){
        String email = etEmailLogin.getText().toString();
        String password = etPasswordLogin.getText().toString();

        boolean isValidate = validateData(email, password);
        if(!isValidate){
            return;
        }
        loginAccountInFirebase(email, password);
    }

    private void loginAccountInFirebase(String email, String password) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d("MiLogin", "Login correcto");
                    if(firebaseAuth.getCurrentUser().isEmailVerified()){
                        Log.d("MiLogin", "Email verificado");
                        navigateToMain();
                    }else{
                        Toast.makeText(LoginActivity.this, "Email no verificado", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Log.d("MiLogin", "Login fallido: " + task.getException());
                    Toast.makeText(LoginActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void navigateToMain(){
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
    }

    private void navigateToRegister(){
        Intent i = new Intent(LoginActivity.this, RegistrationActivity.class);
        startActivity(i);
    }

    private boolean validateData(String email, String password){
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etEmailLogin.setError("Email is invalid");
            return false;
        }
        if(password.length() < 6){
            etPasswordLogin.setError("Password length is invalid");
            return false;
        }
        return true;
    }
}