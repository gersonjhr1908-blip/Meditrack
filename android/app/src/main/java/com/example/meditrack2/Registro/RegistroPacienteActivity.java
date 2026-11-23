package com.example.meditrack2.Registro;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.meditrack2.R;
import com.example.meditrack2.api.ApiConfig;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegistroPacienteActivity extends AppCompatActivity {

    private TextInputEditText txtnombrepaciente;
    private TextInputEditText txtapellidopaciente;
    private TextInputEditText txtdnipaciente;
    private TextInputEditText txtcorreopaciente;
    private TextInputEditText txttelefonopaciente;
    private TextInputEditText txtsexopaciente;
    private TextInputEditText txtfechapaciente;
    private TextInputEditText txtcontrasenapaciente;
    private TextInputEditText txtconfirmarcontrasenapaciente;

    private Button btnresgistrarpaciente;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro_paciente);
        findViewById(R.id.imageButton2).setOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        inicializarCampos();

        btnresgistrarpaciente.setOnClickListener(v -> registrarPaciente());
        txtsexopaciente.setOnClickListener(v -> mostrarOpcionesSexo());
        txtfechapaciente.setOnClickListener(v -> mostrarSelectorFecha());
    }

    private void inicializarCampos() {

        txtnombrepaciente = findViewById(R.id.txtnombrepaciente);
        txtapellidopaciente = findViewById(R.id.txtapellidopaciente);
        txtdnipaciente = findViewById(R.id.txtdnipaciente);
        txtcorreopaciente = findViewById(R.id.txtcorreopaciente);
        txttelefonopaciente = findViewById(R.id.txttelefonopaciente);
        txtsexopaciente = findViewById(R.id.txtsexopaciente);
        txtfechapaciente = findViewById(R.id.txtfechapaciente);
        txtcontrasenapaciente = findViewById(R.id.txtcontrasenapaciente);
        txtconfirmarcontrasenapaciente =
                findViewById(R.id.txtconfirmarcontrasenapaciente);

        btnresgistrarpaciente =
                findViewById(R.id.btnRegsistrofamilia);
    }

    private void registrarPaciente() {

        String nombre = obtenerTexto(txtnombrepaciente);
        String apellidos = obtenerTexto(txtapellidopaciente);
        String dni = obtenerTexto(txtdnipaciente);
        String correo = obtenerTexto(txtcorreopaciente);
        String telefono = obtenerTexto(txttelefonopaciente);
        String sexo = obtenerTexto(txtsexopaciente);
        String fecha = obtenerTexto(txtfechapaciente);
        String contrasena = obtenerTexto(txtcontrasenapaciente);
        String confirmarContrasena =
                obtenerTexto(txtconfirmarcontrasenapaciente);

        // NOMBRE
        if (nombre.isEmpty()) {
            txtnombrepaciente.setError("El nombre es obligatorio");
            txtnombrepaciente.requestFocus();
            return;
        }

        // APELLIDOS
        if (apellidos.isEmpty()) {
            txtapellidopaciente.setError("Los apellidos son obligatorios");
            txtapellidopaciente.requestFocus();
            return;
        }

        // DNI
        if (dni.isEmpty()) {
            txtdnipaciente.setError("El DNI es obligatorio");
            txtdnipaciente.requestFocus();
            return;
        }

        if (!dni.matches("\\d{8}")) {
            txtdnipaciente.setError("El DNI debe tener 8 dígitos");
            txtdnipaciente.requestFocus();
            return;
        }

        // CORREO
        if (correo.isEmpty()) {
            txtcorreopaciente.setError("El correo es obligatorio");
            txtcorreopaciente.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            txtcorreopaciente.setError("Ingresa un correo válido");
            txtcorreopaciente.requestFocus();
            return;
        }

        // TELEFONO
        if (telefono.isEmpty()) {
            txttelefonopaciente.setError("El teléfono es obligatorio");
            txttelefonopaciente.requestFocus();
            return;
        }

        if (!telefono.matches("\\d{9}")) {
            txttelefonopaciente.setError("El teléfono debe tener 9 dígitos");
            txttelefonopaciente.requestFocus();
            return;
        }

        // SEXO
        if (sexo.isEmpty()) {
            txtsexopaciente.setError("El sexo es obligatorio");
            txtsexopaciente.requestFocus();
            return;
        }

        // FECHA
        if (fecha.isEmpty()) {
            txtfechapaciente.setError(
                    "La fecha de nacimiento es obligatoria"
            );
            txtfechapaciente.requestFocus();
            return;
        }

        // CONTRASEÑA
        if (contrasena.isEmpty()) {
            txtcontrasenapaciente.setError(
                    "La contraseña es obligatoria"
            );
            txtcontrasenapaciente.requestFocus();
            return;
        }

        if (contrasena.length() < 6) {
            txtcontrasenapaciente.setError(
                    "Mínimo 6 caracteres"
            );
            txtcontrasenapaciente.requestFocus();
            return;
        }

        // CONFIRMAR CONTRASEÑA
        if (confirmarContrasena.isEmpty()) {
            txtconfirmarcontrasenapaciente.setError(
                    "Confirma tu contraseña"
            );
            txtconfirmarcontrasenapaciente.requestFocus();
            return;
        }

        if (!contrasena.equals(confirmarContrasena)) {
            txtconfirmarcontrasenapaciente.setError(
                    "Las contraseñas no coinciden"
            );
            txtconfirmarcontrasenapaciente.requestFocus();
            return;
        }

        btnresgistrarpaciente.setEnabled(false);
        executor.execute(() -> crearCuentaEnServidor(
                nombre,
                apellidos,
                dni,
                correo,
                telefono,
                sexo,
                fecha,
                contrasena
        ));
    }

    private void crearCuentaEnServidor(
            String nombre,
            String apellidos,
            String dni,
            String correo,
            String telefono,
            String sexo,
            String fecha,
            String contrasena
    ) {
        HttpURLConnection conexion = null;

        try {
            URL url = new URL(ApiConfig.BASE_URL + "/api/registros/pacientes");
            conexion = (HttpURLConnection) url.openConnection();
            conexion.setRequestMethod("POST");
            conexion.setConnectTimeout(10000);
            conexion.setReadTimeout(10000);
            conexion.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
            conexion.setRequestProperty("Accept", "application/json");
            conexion.setDoOutput(true);

            JSONObject solicitud = new JSONObject();
            solicitud.put("nombres", nombre);
            solicitud.put("apellidos", apellidos);
            solicitud.put("dni", dni);
            solicitud.put("correo", correo);
            solicitud.put("telefono", telefono);
            solicitud.put("sexo", sexo);
            solicitud.put("fechaNacimiento", fecha);
            solicitud.put("contrasena", contrasena);

            try (OutputStream salida = conexion.getOutputStream()) {
                salida.write(solicitud.toString().getBytes(StandardCharsets.UTF_8));
            }

            int codigo = conexion.getResponseCode();
            InputStream flujo = codigo >= 200 && codigo < 300
                    ? conexion.getInputStream()
                    : conexion.getErrorStream();
            String respuesta = leerRespuesta(flujo);

            if (codigo == HttpURLConnection.HTTP_CREATED) {
                mainHandler.post(this::mostrarCuentaCreada);
                return;
            }

            String mensaje = "No fue posible registrar el paciente.";
            if (!respuesta.isBlank()) {
                mensaje = new JSONObject(respuesta).optString("mensaje", mensaje);
            }
            String mensajeFinal = mensaje;
            mainHandler.post(() -> mostrarError(mensajeFinal));
        } catch (Exception error) {
            mainHandler.post(() -> mostrarError(
                    "No se pudo conectar al backend. Verifica que Quarkus esté activo."
            ));
        } finally {
            if (conexion != null) {
                conexion.disconnect();
            }
        }
    }

    private void mostrarOpcionesSexo() {
        String[] opciones = {
                "Masculino",
                "Femenino",
                "Prefiero no decirlo"
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("Selecciona una opción")
                .setItems(opciones, (dialogo, posicion) ->
                        txtsexopaciente.setText(opciones[posicion]))
                .show();
    }

    private void mostrarSelectorFecha() {
        long ayer = MaterialDatePicker.todayInUtcMilliseconds() - 86_400_000L;
        CalendarConstraints limites = new CalendarConstraints.Builder()
                .setEnd(ayer)
                .setValidator(DateValidatorPointBackward.before(ayer))
                .build();
        MaterialDatePicker<Long> selector = MaterialDatePicker.Builder
                .datePicker()
                .setCalendarConstraints(limites)
                .setTitleText("Selecciona tu fecha de nacimiento")
                .build();

        selector.addOnPositiveButtonClickListener(fechaSeleccionada -> {
            SimpleDateFormat formato = new SimpleDateFormat(
                    "dd/MM/yyyy",
                    Locale.getDefault()
            );
            formato.setTimeZone(TimeZone.getTimeZone("UTC"));
            txtfechapaciente.setText(formato.format(new Date(fechaSeleccionada)));
        });

        selector.show(getSupportFragmentManager(), "fecha_nacimiento");
    }

    private String leerRespuesta(InputStream flujo) throws Exception {
        if (flujo == null) {
            return "";
        }
        StringBuilder respuesta = new StringBuilder();
        try (BufferedReader lector = new BufferedReader(
                new InputStreamReader(flujo, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                respuesta.append(linea);
            }
        }
        return respuesta.toString();
    }

    private void mostrarError(String mensaje) {
        btnresgistrarpaciente.setEnabled(true);
        new AlertDialog.Builder(this)
                .setTitle("No se pudo crear la cuenta")
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", null)
                .show();
    }

    private void mostrarCuentaCreada() {

        new AlertDialog.Builder(this)
                .setTitle("Cuenta creada")
                .setMessage(
                        "Tu cuenta de paciente se creó correctamente."
                )
                .setPositiveButton("Continuar", (dialog, which) -> {

                    Intent intent = new Intent(
                            RegistroPacienteActivity.this,
                            com.example.meditrack2.Login.LoginActivity.class
                    );

                    intent.setFlags(
                            Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                    );

                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private String obtenerTexto(TextInputEditText campo) {

        if (campo.getText() == null) {
            return "";
        }

        return campo.getText().toString().trim();
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
