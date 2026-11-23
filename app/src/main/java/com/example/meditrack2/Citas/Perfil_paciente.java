
package com.example.meditrack2.Citas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.SharedPreferences;

import com.example.meditrack2.R;
import com.example.meditrack2.Inicio.inicio_PacienteActivity;
import com.example.meditrack2.Login.LoginActivity;


public class Perfil_paciente extends AppCompatActivity {

    private TextView tvNombre;
    private TextView tvTipo;
    private ImageView imgFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil_paciente);

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

        tvNombre = findViewById(R.id.tvNombreMenuPaciente);
        tvTipo = findViewById(R.id.tvTipoMenuPaciente);
        imgFoto = findViewById(R.id.imgFotoMenuPaciente);

        configurarPerfil();
        configurarMenu();
    }

    // MOSTRAR LOS DATOS DEL PACIENTE
    private void configurarPerfil() {
        SharedPreferences sesion = getSharedPreferences(
                "sesion_meditrack",
                MODE_PRIVATE
        );

        if (!sesion.contains("id_usuario")) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
            );
            startActivity(intent);
            finish();
            return;
        }

        String nombres = sesion.getString("nombres", "");
        String apellidos = sesion.getString("apellidos", "");
        String nombreCompleto = (nombres + " " + apellidos).trim();

        tvNombre.setText(nombreCompleto);
        tvTipo.setText("Paciente");

        TextView tvDni = findViewById(R.id.tvDniPerfilPaciente);
        TextView tvCorreo = findViewById(R.id.tvCorreoPerfilPaciente);

        tvDni.setText("DNI: " + sesion.getString("dni", ""));
        tvCorreo.setText(sesion.getString("correo", ""));
    }

    // CONFIGURAR LAS OPCIONES DEL MENÚ
    private void configurarMenu() {

        // INICIO
        findViewById(R.id.menuInicio).setOnClickListener(v -> {
            Intent intent = new Intent(
                    Perfil_paciente.this,
                    inicio_PacienteActivity.class
            );

            enviarDatosUsuario(intent);
            startActivity(intent);
            finish();
        });

        // CITAS
        findViewById(R.id.menuCitas).setOnClickListener(v ->
                abrirPantalla(CitasPacienteActivity.class)
        );

        // MEDICAMENTOS
        findViewById(R.id.menuMedicamentos).setOnClickListener(v ->
                abrirPantalla(Medicamentos.class)
        );

        // RECORDATORIOS
        findViewById(R.id.menuRecordatorios).setOnClickListener(v ->
                abrirPantalla(Recordatorios.class)
        );

        // TRATAMIENTOS
        findViewById(R.id.menuTratamientos).setOnClickListener(v ->
                abrirPantalla(Tratamientos.class)
        );

        // HISTORIAL MÉDICO
        findViewById(R.id.menuHistorial).setOnClickListener(v ->
                abrirPantalla(historial_Medico.class)
        );

        // FAMILIARES
        findViewById(R.id.menuFamiliares).setOnClickListener(v ->
                abrirPantalla(Familiares.class)
        );

        // MIS MÉDICOS
        findViewById(R.id.menuMisMedicos).setOnClickListener(v ->
                mostrarMensaje("La pantalla Mis médicos está pendiente")
        );

        // CONFIGURACIÓN
        findViewById(R.id.menuConfiguracion).setOnClickListener(v ->
                mostrarMensaje("La pantalla Configuración está pendiente")
        );

        // NOTIFICACIONES
        findViewById(R.id.menuNotificaciones).setOnClickListener(v ->
                mostrarMensaje("La pantalla Notificaciones está pendiente")
        );

        // CERRAR SESIÓN
        findViewById(R.id.menuCerrarSesion).setOnClickListener(v -> {
            getSharedPreferences("sesion_meditrack", MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply();
            Intent intent = new Intent(
                    Perfil_paciente.this,
                    LoginActivity.class
            );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();
        });
    }


    // ABRIR OTRA PANTALLA
    private void abrirPantalla(Class<?> actividad) {
        Intent intent = new Intent(Perfil_paciente.this, actividad);
        enviarDatosUsuario(intent);
        startActivity(intent);
    }

    // CONSERVAR LOS DATOS DEL USUARIO AL NAVEGAR
    private void enviarDatosUsuario(Intent intent) {
        String nombre = getIntent().getStringExtra("nombre_usuario");
        String tipo = getIntent().getStringExtra("tipo_usuario");

        if (nombre != null) {
            intent.putExtra("nombre_usuario", nombre);
        }

        if (tipo != null) {
            intent.putExtra("tipo_usuario", tipo);
        }
    }

    // MENSAJES INFORMATIVOS
    private void mostrarMensaje(String mensaje) {
        Toast.makeText(
                Perfil_paciente.this,
                mensaje,
                Toast.LENGTH_SHORT
        ).show();
    }
}
