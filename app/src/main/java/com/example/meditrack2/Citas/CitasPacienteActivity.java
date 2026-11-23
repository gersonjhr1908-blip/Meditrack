package com.example.meditrack2.Citas;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.meditrack2.R;
import com.example.meditrack2.api.ApiClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CitasPacienteActivity extends AppCompatActivity {

    private TextView tabProximas;
    private TextView tabHistorial;
    private TextView mensajeLista;
    private LinearLayout listaCitas;
    private JSONArray citas = new JSONArray();
    private boolean verHistorial;
    private boolean cargando = true;
    private String errorCarga = "";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle estado) {
        super.onCreate(estado);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_citas_paciente);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (vista, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            vista.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        tabProximas = findViewById(R.id.textView56);
        tabHistorial = findViewById(R.id.textView57);
        listaCitas = findViewById(R.id.listaCitas);
        mensajeLista = findViewById(R.id.tvSinCitas);
        listaCitas.removeView(mensajeLista);
        listaCitas.removeAllViews();
        listaCitas.addView(mensajeLista);
        verHistorial = estado != null && estado.getBoolean("historial", false);

        ActivityResultLauncher<Intent> lanzadorNuevaCita = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), resultado -> {
                    if (resultado.getResultCode() == RESULT_OK) {
                        verHistorial = false;
                        cargarCitas();
                    }
                });
        Button agendar = findViewById(R.id.btnAgendarNuevaCita);
        agendar.setOnClickListener(v ->
                lanzadorNuevaCita.launch(new Intent(this, NuevaCita.class)));
        findViewById(R.id.imageButton3).setOnClickListener(v -> finish());
        mensajeLista.setOnClickListener(v -> {
            if (!cargando && !errorCarga.isEmpty()) {
                cargarCitas();
            }
        });
        tabProximas.setOnClickListener(v -> {
            verHistorial = false;
            mostrarCitas();
        });
        tabHistorial.setOnClickListener(v -> {
            verHistorial = true;
            mostrarCitas();
        });
        cargarCitas();
    }

    private void cargarCitas() {
        cargando = true;
        errorCarga = "";
        mostrarCitas();
        executor.execute(() -> {
            try {
                JSONArray respuesta = new JSONArray(ApiClient.solicitar(
                        getApplicationContext(), "/api/citas", null));
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    citas = respuesta;
                    cargando = false;
                    mostrarCitas();
                });
            } catch (Exception error) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    cargando = false;
                    errorCarga = "No se pudieron cargar tus citas. Toca aquí para reintentar.";
                    mostrarCitas();
                    ApiClient.mostrarError(this, error);
                });
            }
        });
    }

    private void mostrarCitas() {
        tabProximas.setBackgroundColor(verHistorial ? 0xFFEAF4FA : 0xFF08B6B3);
        tabProximas.setTextColor(verHistorial ? 0xFF55769C : Color.WHITE);
        tabHistorial.setBackgroundColor(verHistorial ? 0xFF08B6B3 : 0xFFEAF4FA);
        tabHistorial.setTextColor(verHistorial ? Color.WHITE : 0xFF55769C);
        listaCitas.removeAllViews();
        listaCitas.addView(mensajeLista);
        mensajeLista.setVisibility(View.VISIBLE);
        if (cargando) {
            mensajeLista.setText("Cargando tus citas…");
            return;
        }
        if (!errorCarga.isEmpty()) {
            mensajeLista.setText(errorCarga);
            return;
        }

        int cantidad = 0;
        // Las próximas se muestran primero por fecha; el historial desde la más reciente.
        for (int paso = 0; paso < citas.length(); paso++) {
            int indice = verHistorial ? citas.length() - 1 - paso : paso;
            JSONObject cita = citas.optJSONObject(indice);
            if (cita != null && cita.optBoolean("proxima") != verHistorial) {
                agregarTarjeta(cita);
                cantidad++;
            }
        }
        mensajeLista.setText(verHistorial
                ? "Aún no hay citas en tu historial."
                : "Aún no tienes próximas citas.");
        mensajeLista.setVisibility(cantidad == 0 ? View.VISIBLE : View.GONE);
    }

    private void agregarTarjeta(JSONObject cita) {
        View tarjeta = getLayoutInflater().inflate(R.layout.item_cita, listaCitas, false);
        String fecha = cita.optString("fecha", "");
        String[] partes = fecha.split("-");
        String[] meses = {"ENE", "FEB", "MAR", "ABR", "MAY", "JUN",
                "JUL", "AGO", "SEP", "OCT", "NOV", "DIC"};
        TextView dia = tarjeta.findViewById(R.id.tvDia);
        TextView mes = tarjeta.findViewById(R.id.tvMes);
        if (partes.length == 3) {
            int numeroMes = Integer.parseInt(partes[1]);
            dia.setText(partes[2]);
            mes.setText(meses[numeroMes - 1] + " " + partes[0]);
        }
        TextView especialidad = tarjeta.findViewById(R.id.tvEspecialidad);
        TextView medico = tarjeta.findViewById(R.id.tvMedico);
        TextView hora = tarjeta.findViewById(R.id.tvHora);
        TextView estado = tarjeta.findViewById(R.id.tvEstado);
        especialidad.setText(cita.optString("especialidad"));
        medico.setText(cita.optString("medico"));
        hora.setText(cita.optString("hora"));
        estado.setText(cita.optString("estado"));
        listaCitas.addView(tarjeta);
    }

    @Override
    protected void onSaveInstanceState(Bundle estado) {
        super.onSaveInstanceState(estado);
        estado.putBoolean("historial", verHistorial);
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }
}
