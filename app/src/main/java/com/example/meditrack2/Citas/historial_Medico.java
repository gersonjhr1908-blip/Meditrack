
package com.example.meditrack2.Citas;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.meditrack2.R;
import com.example.meditrack2.api.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.concurrent.Executors;
import com.google.android.material.card.MaterialCardView;

import androidx.cardview.widget.CardView;

public class historial_Medico extends AppCompatActivity {
    private final java.util.concurrent.ExecutorService executor=Executors.newSingleThreadExecutor();

    private TextView tvResumen;
    private TextView tvDocumentos;
    private View contenidoHistorial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_historial_medico);

        // CONFIGURACIÓN DE LA PANTALLA
        View main = findViewById(R.id.main);

        if (main != null) {
            ViewCompat.setOnApplyWindowInsetsListener(main, (v, insets) -> {
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
            });
        }

        // BOTÓN VOLVER
        ImageButton btnVolver = findViewById(R.id.btnVolverHistorial);

        if (btnVolver != null) {
            btnVolver.setOnClickListener(v -> finish());
        }

        // PESTAÑAS
        tvResumen = findViewById(R.id.tvResumenHistorial);
        tvDocumentos = findViewById(R.id.tvDocumentosHistorial);
        contenidoHistorial = findViewById(R.id.scrollHistorial);

        if (tvResumen != null) {
            tvResumen.setOnClickListener(v -> mostrarResumen());
        }

        if (tvDocumentos != null) {
            tvDocumentos.setOnClickListener(v -> mostrarDocumentos());
        }

        // TARJETAS DEL HISTORIAL
        configurarTarjeta(R.id.cardDiabetes, "Diabetes tipo 2");
        configurarTarjeta(R.id.cardGlucosaHistorial, "Glucosa");
        configurarTarjeta(R.id.cardPresionHistorial, "Presión arterial");
        configurarTarjeta(R.id.cardPesoHistorial, "Peso");
        configurarTarjeta(R.id.cardImcHistorial, "Índice de masa corporal");
        configurarTarjeta(R.id.cardUltimaConsulta, "Última consulta");

        mostrarResumen();
        cargarHistorial();
    }

    private void cargarHistorial(){ executor.execute(() -> { try {
        JSONArray datos=new JSONArray(ApiClient.solicitar(getApplicationContext(),"/api/paciente/historial",null));
        runOnUiThread(() -> {
            if(datos.length()==0) return;
            try { JSONObject d=datos.getJSONObject(0);
                TextView consulta=findViewById(R.id.tvConsultaGeneral); TextView fecha=findViewById(R.id.tvFechaConsulta);
                if(consulta!=null) consulta.setText(d.optString("motivo","Atención médica"));
                if(fecha!=null) fecha.setText(d.optString("fecha","")+" · "+d.optString("medico",""));
            } catch(Exception ignored){}
        });
    } catch(Exception e){ runOnUiThread(() -> Toast.makeText(this,"No se pudo cargar el historial.",Toast.LENGTH_SHORT).show()); }}); }
    @Override protected void onDestroy(){executor.shutdownNow();super.onDestroy();}

    // MOSTRAR RESUMEN
    private void mostrarResumen() {
        if (contenidoHistorial != null) {
            contenidoHistorial.setVisibility(View.VISIBLE);
        }

        if (tvResumen != null) {
            tvResumen.setBackgroundColor(0xFF08B6B3);
            tvResumen.setTextColor(0xFFFFFFFF);
        }

        if (tvDocumentos != null) {
            tvDocumentos.setBackgroundColor(0x00000000);
            tvDocumentos.setTextColor(0xFF54749B);
        }
    }

    // MOSTRAR DOCUMENTOS
    private void mostrarDocumentos() {
        if (tvResumen != null) {
            tvResumen.setBackgroundColor(0x00000000);
            tvResumen.setTextColor(0xFF54749B);
        }

        if (tvDocumentos != null) {
            tvDocumentos.setBackgroundColor(0xFF08B6B3);
            tvDocumentos.setTextColor(0xFFFFFFFF);
        }

        Toast.makeText(
                this,
                "La sección de documentos se implementará después",
                Toast.LENGTH_SHORT
        ).show();
    }

    // CONFIGURAR TARJETAS
    private void configurarTarjeta(int id, String nombre) {
        CardView tarjeta = findViewById(id);

        if (tarjeta != null) {
            tarjeta.setOnClickListener(v ->
                    Toast.makeText(
                            historial_Medico.this,
                            "Detalle de " + nombre,
                            Toast.LENGTH_SHORT
                    ).show()
            );
        }
    }
}
