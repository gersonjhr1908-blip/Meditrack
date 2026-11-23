
package com.example.meditrack2.Inicio;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.meditrack2.Citas.CitasPacienteActivity;
import com.example.meditrack2.Citas.Medicamentos;
import com.example.meditrack2.Citas.Recordatorios;
import com.example.meditrack2.Citas.Tratamientos;
import com.example.meditrack2.Citas.historial_Medico;
import com.example.meditrack2.Citas.Familiares;
import com.example.meditrack2.R;
import com.example.meditrack2.Citas.Perfil_paciente;
import com.example.meditrack2.Citas.NuevaCita;
import com.example.meditrack2.api.ApiClient;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class inicio_PacienteActivity extends AppCompatActivity {

    private final ExecutorService executorCitas = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // CONFIGURACIÓN DE LA PANTALLA
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inicio_paciente);
        String nombres = getSharedPreferences(
                "sesion_meditrack",
                MODE_PRIVATE
        ).getString("nombres", "");

        TextView saludo = findViewById(R.id.textView30);
        saludo.setText(nombres.isEmpty() ? "Hola" : "Hola, " + nombres);
        findViewById(R.id.imageView33).setOnClickListener(v ->
                startActivity(new Intent(this, NuevaCita.class)));
        View.OnClickListener verCitas = v ->
                startActivity(new Intent(this, CitasPacienteActivity.class));
        findViewById(R.id.verTodasCitas).setOnClickListener(verCitas);
        findViewById(R.id.textView51).setOnClickListener(verCitas);

        View vistaPrincipal = findViewById(R.id.main);

        if (vistaPrincipal != null) {
            ViewCompat.setOnApplyWindowInsetsListener(
                    vistaPrincipal, (v, insets) -> {
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
            // ABRIR EL MENÚ DEL PERFIL DEL PACIENTE
            View.OnClickListener abrirPerfil = v -> {
                Intent intent = new Intent(
                        inicio_PacienteActivity.this,
                        Perfil_paciente.class
                );

                // Enviar los datos si fueron recibidos desde el login
                String nombre = getIntent().getStringExtra("nombre_usuario");
                String tipo = getIntent().getStringExtra("tipo_usuario");

                if (nombre != null) {
                    intent.putExtra("nombre_usuario", nombre);
                }

                if (tipo != null) {
                    intent.putExtra("tipo_usuario", tipo);
                }

                startActivity(intent);
            };

// Conectar el perfil si existe alguno de estos elementos
            ImageView imagenPerfil = findViewById(R.id.imgperfilfamiliar);
            TextView textoPerfil = findViewById(R.id.txtperfilfamiliar);

            if (imagenPerfil != null) {
                imagenPerfil.setOnClickListener(abrirPerfil);
            }

            if (textoPerfil != null) {
                textoPerfil.setOnClickListener(abrirPerfil);
            }
        }

        // ACCESO RÁPIDO: CITAS
        ImageView iconoCitas = findViewById(R.id.imageView24);
        TextView textoCitas = findViewById(R.id.textView38);

        // MENÚ INFERIOR: CITAS
        ImageView iconoCitasMenu = findViewById(R.id.imageView32);
        TextView textoCitasMenu = findViewById(R.id.textView53);

        // ACCESO RÁPIDO: TRATAMIENTOS
        ImageView iconoTratamientos = findViewById(R.id.imageView27);
        TextView textoTratamientos = findViewById(R.id.textView44);

        // ACCESO RÁPIDO: MEDICAMENTOS
        ImageView iconoMedicamentos = findViewById(R.id.imageView25);
        TextView textoMedicamentos = findViewById(R.id.textView40);

        // ACCESO RÁPIDO: RECORDATORIOS
        ImageView iconoRecordatorios = findViewById(R.id.imgrecordatorios);
        TextView textoRecordatorios = findViewById(R.id.txtRecordatorios);

        // ACCESO RÁPIDO: HISTORIAL MÉDICO
        ImageView iconoHistorial = findViewById(R.id.imageView28);
        TextView textoHistorial = findViewById(R.id.textView45);

        // MENÚ INFERIOR: HISTORIAL MÉDICO
        ImageView iconoHistorialMenu = findViewById(R.id.imageView34);
        TextView textoHistorialMenu = findViewById(R.id.textView54);

        // ACCESO RÁPIDO: FAMILIARES
        ImageView iconoFamiliares = findViewById(R.id.imageView29);
        TextView textoFamiliares = findViewById(R.id.textView46);

        // ACCIÓN: ABRIR CITAS
        View.OnClickListener abrirCitas = v -> {
            Intent intent = new Intent(
                    inicio_PacienteActivity.this,
                    CitasPacienteActivity.class
            );
            startActivity(intent);
        };

        // ACCIÓN: ABRIR TRATAMIENTOS
        View.OnClickListener abrirTratamientos = v -> {
            Intent intent = new Intent(
                    inicio_PacienteActivity.this,
                    Tratamientos.class
            );
            startActivity(intent);
        };

        // ACCIÓN: ABRIR MEDICAMENTOS
        View.OnClickListener abrirMedicamentos = v -> {
            Intent intent = new Intent(
                    inicio_PacienteActivity.this,
                    Medicamentos.class
            );
            startActivity(intent);
        };

        // ACCIÓN: ABRIR RECORDATORIOS
        View.OnClickListener abrirRecordatorios = v -> {
            Intent intent = new Intent(
                    inicio_PacienteActivity.this,
                    Recordatorios.class
            );
            startActivity(intent);
        };

        // ACCIÓN: ABRIR HISTORIAL MÉDICO
        View.OnClickListener abrirHistorial = v -> {
            Intent intent = new Intent(
                    inicio_PacienteActivity.this,
                    historial_Medico.class
            );
            startActivity(intent);
        };

        // ACCIÓN: ABRIR FAMILIARES
        View.OnClickListener abrirFamiliares = v -> {
            Intent intent = new Intent(
                    inicio_PacienteActivity.this,
                    Familiares.class
            );
            startActivity(intent);
        };

        // EVENTOS DE CITAS
        if (iconoCitas != null) {
            iconoCitas.setOnClickListener(abrirCitas);
        }
        if (textoCitas != null) {
            textoCitas.setOnClickListener(abrirCitas);
        }
        if (iconoCitasMenu != null) {
            iconoCitasMenu.setOnClickListener(abrirCitas);
        }
        if (textoCitasMenu != null) {
            textoCitasMenu.setOnClickListener(abrirCitas);
        }

        // EVENTOS DE TRATAMIENTOS
        if (iconoTratamientos != null) {
            iconoTratamientos.setOnClickListener(abrirTratamientos);
        }
        if (textoTratamientos != null) {
            textoTratamientos.setOnClickListener(abrirTratamientos);
        }

        // EVENTOS DE MEDICAMENTOS
        if (iconoMedicamentos != null) {
            iconoMedicamentos.setOnClickListener(abrirMedicamentos);
        }
        if (textoMedicamentos != null) {
            textoMedicamentos.setOnClickListener(abrirMedicamentos);
        }

        // EVENTOS DE RECORDATORIOS
        if (iconoRecordatorios != null) {
            iconoRecordatorios.setOnClickListener(abrirRecordatorios);
        }
        if (textoRecordatorios != null) {
            textoRecordatorios.setOnClickListener(abrirRecordatorios);
        }

        // EVENTOS DEL HISTORIAL MÉDICO
        if (iconoHistorial != null) {
            iconoHistorial.setOnClickListener(abrirHistorial);
        }
        if (textoHistorial != null) {
            textoHistorial.setOnClickListener(abrirHistorial);
        }
        if (iconoHistorialMenu != null) {
            iconoHistorialMenu.setOnClickListener(abrirHistorial);
        }
        if (textoHistorialMenu != null) {
            textoHistorialMenu.setOnClickListener(abrirHistorial);
        }

        // EVENTOS DE FAMILIARES
        if (iconoFamiliares != null) {
            iconoFamiliares.setOnClickListener(abrirFamiliares);
        }
        if (textoFamiliares != null) {
            textoFamiliares.setOnClickListener(abrirFamiliares);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView descripcion = findViewById(R.id.textView48);
        TextView medico = findViewById(R.id.textView49);
        TextView fecha = findViewById(R.id.textView50);
        descripcion.setText("Cargando tu próxima cita…");
        medico.setVisibility(View.GONE);
        fecha.setVisibility(View.GONE);
        executorCitas.execute(() -> {
            try {
                JSONArray citas = new JSONArray(ApiClient.solicitar(
                        getApplicationContext(), "/api/citas", null));
                JSONObject siguiente = null;
                for (int i = 0; i < citas.length(); i++) {
                    JSONObject cita = citas.getJSONObject(i);
                    if (cita.getBoolean("proxima")) {
                        siguiente = cita;
                        break;
                    }
                }
                JSONObject cita = siguiente;
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    if (cita == null) {
                        descripcion.setText("Aún no hay una próxima cita para mostrar");
                        return;
                    }
                    descripcion.setText(cita.optString("especialidad"));
                    medico.setText(cita.optString("medico"));
                    fecha.setText(cita.optString("fecha") + " · " + cita.optString("hora"));
                    medico.setVisibility(View.VISIBLE);
                    fecha.setVisibility(View.VISIBLE);
                });
            } catch (Exception error) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    descripcion.setText("No se pudo cargar tu próxima cita");
                    ApiClient.mostrarError(this, error);
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        executorCitas.shutdownNow();
        super.onDestroy();
    }
}
