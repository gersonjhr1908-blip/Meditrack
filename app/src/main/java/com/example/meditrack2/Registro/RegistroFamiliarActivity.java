
package com.example.meditrack2.Registro;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.meditrack2.api.ApiClient;
import org.json.JSONObject;
import com.example.meditrack2.Login.LoginActivity;
import com.example.meditrack2.R;
import com.google.android.material.textfield.TextInputEditText;

public class RegistroFamiliarActivity extends AppCompatActivity {

    private TextInputEditText nombre, apellidos, dni, correo;
    private TextInputEditText telefono, sexo, relacion;
    private TextInputEditText contrasena, confirmarContrasena;
    private Button btnRegistrar;
    private ImageButton btnVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro_familiar);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {
                    Insets barras = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());
                    v.setPadding(barras.left, barras.top,
                            barras.right, barras.bottom);
                    return insets;
                });

        nombre = findViewById(R.id.txtnombrefamilia);
        apellidos = findViewById(R.id.txtapeliidofamilia);
        dni = findViewById(R.id.txtfamiliar);
        correo = findViewById(R.id.txtcorreofamiliar);
        telefono = findViewById(R.id.txttelefono);
        sexo = findViewById(R.id.txtsexofamlia);
        relacion = findViewById(R.id.txtrealcionpaciente);
        contrasena = findViewById(R.id.txtcontrasenafamlia);
        confirmarContrasena =
                findViewById(R.id.txtconfimarcontrafamilia);

        btnRegistrar = findViewById(R.id.btnRegsistrofamilia);
        btnVolver = findViewById(R.id.imageButton2);

        btnRegistrar.setOnClickListener(v -> registrarFamiliar());
        btnVolver.setOnClickListener(v -> finish());
    }

    private void registrarFamiliar() {

        String n = texto(nombre);
        String a = texto(apellidos);
        String d = texto(dni);
        String c = texto(correo);
        String t = texto(telefono);
        String s = texto(sexo);
        String r = texto(relacion);
        String p = texto(contrasena);
        String cp = texto(confirmarContrasena);

        if (n.isEmpty()) {
            error(nombre, "Ingresa tu nombre");
            return;
        }
        if (a.isEmpty()) {
            error(apellidos, "Ingresa tus apellidos");
            return;
        }
        if (!d.matches("\\d{8}")) {
            error(dni, "El DNI debe tener 8 dígitos");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(c).matches()) {
            error(correo, "Ingresa un correo válido");
            return;
        }
        if (!t.matches("\\d{9}")) {
            error(telefono, "El teléfono debe tener 9 dígitos");
            return;
        }
        if (s.isEmpty()) {
            error(sexo, "Ingresa tu sexo");
            return;
        }
        if (r.isEmpty()) {
            error(relacion, "Ingresa tu relación con el paciente");
            return;
        }
        if (p.length() < 8
                || !p.matches(".*[A-Z].*")
                || !p.matches(".*[a-z].*")
                || !p.matches(".*\\d.*")) {
            error(contrasena,
                    "Usa 8 caracteres, mayúscula, minúscula y número");
            return;
        }
        if (!p.equals(cp)) {
            error(confirmarContrasena,
                    "Las contraseñas no coinciden");
            return;
        }
        btnRegistrar.setEnabled(false);
        new Thread(() -> {
            try {
                JSONObject datos = new JSONObject();
                datos.put("nombres", n).put("apellidos", a).put("dni", d).put("correo", c)
                        .put("telefono", t).put("sexo", s).put("relacion", r).put("contrasena", p);
                ApiClient.registrarFamiliar(datos);
                runOnUiThread(() -> {
                    if (!isFinishing() && !isDestroyed()) mostrarCuentaCreada();
                });
            } catch (Exception error) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    btnRegistrar.setEnabled(true);
                    new AlertDialog.Builder(this)
                            .setTitle("No se pudo crear la cuenta")
                            .setMessage(error instanceof ApiClient.ErrorApi ? error.getMessage()
                                    : "Revisa que Quarkus esté activo e inténtalo de nuevo.")
                            .setPositiveButton("Aceptar", null)
                            .show();
                });
            }
        }, "registro-familiar").start();
    }

    private void mostrarCuentaCreada() {
        new AlertDialog.Builder(this)
                .setTitle("Cuenta creada")
                .setMessage("Tu cuenta familiar se creó correctamente.")
                .setPositiveButton("Continuar", (dialog, which) -> {
                    Intent intent = new Intent(
                            RegistroFamiliarActivity.this,
                            LoginActivity.class);
                    intent.setFlags(
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private String texto(TextInputEditText campo) {
        return campo.getText() == null
                ? "" : campo.getText().toString().trim();
    }

    private void error(TextInputEditText campo, String mensaje) {
        campo.setError(mensaje);
        campo.requestFocus();
    }
}
