package com.agendaarduino;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.BuildConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity {

    private TextView tvUserEmail, tvAppVersion;
    private Button btnChangePassword, btnEnable2FA, btnDeleteAccount, btnFAQ, btnContactSupport;
    private Switch switchNotifications, switchTheme;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        inicialize();

        // Mostrar la versión de la app
        String versionName = "2.5.0";
        tvAppVersion.setText("Versión: " + versionName);

        // Mostrar el correo electrónico del usuario
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            tvUserEmail.setText(user.getEmail());
        }

        // Configuración de la contraseña
        btnChangePassword.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(currentUser.getEmail())
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(SettingsActivity.this, "Se ha enviado el enlace para cambiar la contraseña.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SettingsActivity.this, "Error al enviar el enlace para cambiar la contraseña.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // Configuración de notificaciones
        switchNotifications.setChecked(getNotificationStatus());
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> setNotificationStatus(isChecked));

        // Configuración de tema
        switchTheme.setChecked(isDarkModeEnabled());
        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> setDarkMode(isChecked));

        // Activar autenticación de dos factores
        btnEnable2FA.setOnClickListener(v -> {
            // Aquí puedes implementar la lógica para habilitar 2FA
            Toast.makeText(SettingsActivity.this, "Funcionalidad de 2FA no implementada aún.", Toast.LENGTH_SHORT).show();
        });

        // Eliminar cuenta
        btnDeleteAccount.setOnClickListener(v -> {
            new AlertDialog.Builder(SettingsActivity.this)
                    .setTitle("Confirmación")
                    .setMessage("¿Estás seguro de que deseas eliminar tu cuenta?")
                    .setPositiveButton("Eliminar", (dialog, which) -> deleteAccount())
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        // Navegar a Preguntas Frecuentes
        btnFAQ.setOnClickListener(v -> {
            new AlertDialog.Builder(SettingsActivity.this)
                    .setTitle("Preguntas Frecuentes")
                    .setMessage("¿Cómo cambio mi contraseña?\n- Ve a la sección de ajustes y selecciona 'Cambiar Contraseña'.\n\n" +
                            "¿Cómo habilito la autenticación de dos factores?\n- Dirígete a la sección de seguridad en los ajustes para activar 2FA.\n\n" +
                            "¿Cómo elimino mi cuenta?\n- Puedes eliminar tu cuenta desde los ajustes, en la opción 'Eliminar Cuenta'.\n\n" +
                            "Si tienes más dudas, contáctanos en lca.developer.apps@gmail.com")
                    .setPositiveButton("Aceptar", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .setCancelable(true)
                    .show();
        });


        // Navegar a Contactar con Soporte
        btnContactSupport.setOnClickListener(v -> {
            new AlertDialog.Builder(SettingsActivity.this)
                    .setTitle("Contactar con Soporte")
                    .setMessage("Si necesitas ayuda, por favor contáctanos a través de las siguientes opciones:")
                    .setPositiveButton("Correo Electrónico", (dialog, which) -> {
                        // Abrir la aplicación de Gmail
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "lca.developer.apps@gmail.com", null));
                        startActivity(Intent.createChooser(emailIntent, "Enviar correo"));
                    })
                    .setNeutralButton("LinkedIn", (dialog, which) -> {
                        Intent linkedinIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/lucia-cosio-artime-c16012022/"));
                        startActivity(linkedinIntent);
                    })
                    .setNegativeButton("Centro de Ayuda", (dialog, which) -> {
                        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://lucosiar.es/"));
                        startActivity(webIntent);
                    })
                    .setCancelable(true)
                    .show();
        });
    }

    // Método para obtener el estado de las notificaciones
    private boolean getNotificationStatus() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        return prefs.getBoolean("notifications_enabled", true); // Predeterminado en true
    }

    // Método para guardar el estado de las notificaciones
    private void setNotificationStatus(boolean isEnabled) {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("notifications_enabled", isEnabled);
        editor.apply();
    }

    // Método para verificar si el modo oscuro está habilitado
    private boolean isDarkModeEnabled() {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    // Método para activar o desactivar el modo oscuro
    private void setDarkMode(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Guardar preferencia en SharedPreferences
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("dark_mode", isDarkMode);
        editor.apply();
    }

    // Método para eliminar la cuenta del usuario
    private void deleteAccount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SettingsActivity.this, "Cuenta eliminada exitosamente", Toast.LENGTH_SHORT).show();
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(SettingsActivity.this, "Error al eliminar la cuenta", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void inicialize(){
        tvUserEmail = findViewById(R.id.tvUserEmail);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnEnable2FA = findViewById(R.id.btnEnable2FA);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        btnFAQ = findViewById(R.id.btnFAQ);
        btnContactSupport = findViewById(R.id.btnContactSupport);
        switchNotifications = findViewById(R.id.switchNotifications);
        switchTheme = findViewById(R.id.switchTheme);
        tvAppVersion = findViewById(R.id.tvAppVersion);
    }
}