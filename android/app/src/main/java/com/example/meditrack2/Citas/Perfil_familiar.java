
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

import com.example.meditrack2.R;
import com.example.meditrack2.Entity.Familiar;
import com.example.meditrack2.Entity.Usuario;
import com.example.meditrack2.Repository.UsuarioRepository;
import com.example.meditrack2.Inicio.inicio_familiar;
import com.example.meditrack2.Login.LoginActivity;
import android.view.View;

public class Perfil_familiar extends AppCompatActivity {

    private ImageView imgFotoPerfil;
    private TextView tvNombrePerfil;
    private TextView tvTipoUsuarioPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil);

        View vistaPrincipal = findViewById(R.id.main);

        if (vistaPrincipal != null) {
            ViewCompat.setOnApplyWindowInsetsListener(
                    vistaPrincipal,
                    (v, insets) -> {
                        Insets barras = insets.getInsets(
                                WindowInsetsCompat.Type.systemBars()
                        );

                        v.setPadding(
                                barras.left,
                                barras.top,
                                barras.right,
                                barras.bottom
                        );

                        return insets;
                    }
            );
        }

        imgFotoPerfil = findViewById(R.id.imgFotoPerfil);
        tvNombrePerfil = findViewById(R.id.tvNombrePerfil);
        tvTipoUsuarioPerfil = findViewById(R.id.tvTipoUsuarioPerfil);

        cargarDatosPerfil();
        configurarBotones();
    }

    private void cargarDatosPerfil() {
        int usuarioId = getIntent().getIntExtra("usuario_id", -1);

        Familiar familiar =
                UsuarioRepository.obtenerFamiliarPorUsuarioId(usuarioId);

        if (familiar != null) {
            Usuario usuario =
                    UsuarioRepository.obtenerUsuarioPorId(
                            familiar.getUsuarioId()
                    );

            if (usuario != null) {
                String nombre = usuario.getNombre() == null
                        ? ""
                        : usuario.getNombre();

                String apellidos = usuario.getApellidos() == null
                        ? ""
                        : usuario.getApellidos();

                String nombreCompleto =
                        (nombre + " " + apellidos).trim();

                tvNombrePerfil.setText(
                        nombreCompleto.isEmpty()
                                ? "Familiar"
                                : nombreCompleto
                );

                tvTipoUsuarioPerfil.setText("Familiar");
                return;
            }
        }

        // No inventar datos cuando no se encuentra la cuenta.
        tvNombrePerfil.setText("Perfil familiar");
        tvTipoUsuarioPerfil.setText(
                "No se encontraron los datos de la cuenta"
        );
    }

    private void configurarBotones() {

        // VOLVER A LA PANTALLA ANTERIOR
        findViewById(R.id.btnVolverPerfil).setOnClickListener(v ->
                finish()
        );

        // INFORMACIÓN PERSONAL
        findViewById(R.id.cardInformacionPersonal).setOnClickListener(v ->
                mostrarMensaje("Información personal: pendiente de implementar")
        );

        // DATOS DE SALUD
        findViewById(R.id.cardDatosSaludPerfil).setOnClickListener(v ->
                mostrarMensaje(
                        "Los datos de salud corresponden al paciente autorizado"
                )
        );

        // FAMILIARES
        findViewById(R.id.cardFamiliaresPerfil).setOnClickListener(v ->
                mostrarMensaje("Gestión de familiares: pendiente de implementar")
        );

        // CONFIGURACIÓN
        findViewById(R.id.cardConfiguracionPerfil).setOnClickListener(v ->
                mostrarMensaje("Configuración: pendiente de implementar")
        );

        // NOTIFICACIONES
        findViewById(R.id.cardNotificacionesPerfil).setOnClickListener(v ->
                mostrarMensaje("Notificaciones: pendiente de implementar")
        );

        // SEGURIDAD
        findViewById(R.id.cardSeguridadPerfil).setOnClickListener(v ->
                mostrarMensaje("Seguridad: pendiente de implementar")
        );

        // CERRAR SESIÓN
        findViewById(R.id.cardCerrarSesionPerfil).setOnClickListener(v -> {
            Intent intent = new Intent(
                    Perfil_familiar.this,
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

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(
                this,
                mensaje,
                Toast.LENGTH_SHORT
        ).show();
    }
}
