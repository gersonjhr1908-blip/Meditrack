
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

import com.example.meditrack2.Citas.Perfil_familiar;
import com.example.meditrack2.Citas.CitasPacienteActivity;
import com.example.meditrack2.Citas.Medicamentos;
import com.example.meditrack2.Citas.Tratamientos;
import com.example.meditrack2.Citas.historial_Medico;
import com.example.meditrack2.Citas.Vinvular_paciente;
import com.example.meditrack2.api.ApiClient;
import org.json.JSONArray;
import com.example.meditrack2.R;

public class inicio_familiar extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inicio_familiar);
        String nombres = getSharedPreferences("sesion_meditrack", MODE_PRIVATE)
                .getString("nombres", "");
        TextView saludo = findViewById(R.id.textView30);
        saludo.setText(nombres.isEmpty() ? "Hola" : "Hola, " + nombres);
        cargarPacienteVinculado();

        View vistaPrincipal = findViewById(R.id.main);

        if (vistaPrincipal != null) {
            ViewCompat.setOnApplyWindowInsetsListener(
                    vistaPrincipal,
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
        }

        // ELEMENTOS DEL PERFIL EN EL MENÚ INFERIOR
        ImageView imagenPerfil = findViewById(R.id.imgperfilfamiliar);
        TextView textoPerfil = findViewById(R.id.txtperfilfamiliar);

        // ACCIÓN PARA ABRIR EL PERFIL DEL FAMILIAR
        View.OnClickListener abrirPerfilFamiliar = v -> {
            Intent intent = new Intent(
                    inicio_familiar.this,
                    Perfil_familiar.class
            );

            startActivity(intent);
        };

        if (imagenPerfil != null) {
            imagenPerfil.setOnClickListener(abrirPerfilFamiliar);
        }

        if (textoPerfil != null) {
            textoPerfil.setOnClickListener(abrirPerfilFamiliar);
        }

        configurarAcceso(R.id.imageView24, CitasPacienteActivity.class);
        configurarAcceso(R.id.textView38, CitasPacienteActivity.class);
        configurarAcceso(R.id.imageView27, Tratamientos.class);
        configurarAcceso(R.id.textView44, Tratamientos.class);
        configurarAcceso(R.id.imageView25, Medicamentos.class);
        configurarAcceso(R.id.textView40, Medicamentos.class);
        configurarAcceso(R.id.imageView28, historial_Medico.class);
        configurarAcceso(R.id.textView45, historial_Medico.class);
        configurarAcceso(R.id.imageView29, Vinvular_paciente.class);
        configurarAcceso(R.id.textView46, Vinvular_paciente.class);
    }

    private void configurarAcceso(int id, Class<?> destino) {
        View vista = findViewById(id);
        if (vista != null) vista.setOnClickListener(v ->
                startActivity(new Intent(this, destino)));
    }

    private void cargarPacienteVinculado() {
        new Thread(() -> {
            try {
                JSONArray datos = new JSONArray(ApiClient.solicitar(this, "/api/vinculacion/pacientes", null));
                runOnUiThread(() -> {
                    TextView titulo=findViewById(R.id.textView34);
                    TextView detalle=findViewById(R.id.textView35);
                    if (datos.length() > 0) {
                        org.json.JSONObject p=datos.optJSONObject(0);
                        titulo.setText("Paciente vinculado");
                        detalle.setText(p.optString("nombres")+" "+p.optString("apellidos"));
                    }
                });
            } catch (Exception ignored) { }
        }).start();
    }
}
