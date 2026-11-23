
package com.example.meditrack2.Citas;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.meditrack2.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.example.meditrack2.Citas.NuevoRecordatorio;
import com.example.meditrack2.api.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import java.util.concurrent.Executors;

public class Recordatorios extends AppCompatActivity {
    private final java.util.concurrent.ExecutorService executor=Executors.newSingleThreadExecutor();

    private SwitchMaterial switchMedicacion;
    private SwitchMaterial switchGlucosa;
    private SwitchMaterial switchCita;
    private SwitchMaterial switchEjercicio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recordatorios);

        // Ajustar la pantalla a las barras del sistema
        android.view.View main = findViewById(R.id.main);

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

        // Botón para regresar
        ImageButton btnVolver =
                findViewById(R.id.btnVolverRecordatorios);

        btnVolver.setOnClickListener(v -> finish());

        // Obtener los interruptores
        switchMedicacion = findViewById(R.id.switchMedicacion);
        switchGlucosa = findViewById(R.id.switchGlucosa);
        switchCita = findViewById(R.id.switchCita);
        switchEjercicio = findViewById(R.id.switchEjercicio);

        // Permitir activar y desactivar los recordatorios
        configurarInterruptor(switchMedicacion, "Medicación");
        configurarInterruptor(switchGlucosa, "Medición de glucosa");
        configurarInterruptor(switchCita, "Cita médica");
        configurarInterruptor(switchEjercicio, "Ejercicio");

        // Botón para crear un recordatorio
        // BOTÓN: NUEVO RECORDATORIO
        MaterialButton btnNuevo = findViewById(R.id.btnNuevoRecordatorio);

        btnNuevo.setOnClickListener(v -> {
            Intent intent = new Intent(
                    Recordatorios.this,
                    NuevoRecordatorio.class
            );

            startActivity(intent);
        });
        cargarRecordatorios();
    }

    private void cargarRecordatorios(){ executor.execute(() -> { try { JSONArray datos=new JSONArray(ApiClient.solicitar(getApplicationContext(),"/api/paciente/recordatorios",null)); runOnUiThread(() -> aplicarRecordatorios(datos)); } catch(Exception e){ runOnUiThread(() -> ApiClient.mostrarError(this,e)); }}); }
    private void aplicarRecordatorios(JSONArray datos){
        int[] cards={R.id.cardRecordatorio1,R.id.cardRecordatorio2,R.id.cardRecordatorio3,R.id.cardRecordatorio4};
        int[] nombres={R.id.tvMedicacion,R.id.tvGlucosa,R.id.tvCitaMedica,R.id.tvEjercicio};
        int[] detalles={R.id.tvDetalleMedicacion,R.id.tvDetalleGlucosa,R.id.tvDetalleCita,R.id.tvDetalleEjercicio};
        int[] horas={R.id.tvHoraMedicacion,R.id.tvHoraGlucosa,R.id.tvHoraCita,R.id.tvHoraEjercicio};
        for(int id:cards){View v=findViewById(id);if(v!=null)v.setVisibility(View.GONE);}
        TextView vacio=findViewById(R.id.tvSinRecordatorios); vacio.setVisibility(datos.length()==0?View.VISIBLE:View.GONE);
        ViewGroup contenedor = (ViewGroup) findViewById(R.id.cardRecordatorio1).getParent();
        if (contenedor != null) contenedor.setVisibility(datos.length() == 0 ? View.GONE : View.VISIBLE);
        for(int i=0;i<datos.length()&&i<4;i++) try{JSONObject d=datos.getJSONObject(i);((CardView)findViewById(cards[i])).setVisibility(View.VISIBLE);
            ((TextView)findViewById(nombres[i])).setText(d.optString("nombre","Recordatorio"));
            ((TextView)findViewById(detalles[i])).setText(d.optString("detalle","")+" · "+d.optString("frecuencia",""));
            ((TextView)findViewById(horas[i])).setText(d.optString("fecha","")+" · "+d.optString("hora",""));
        }catch(Exception ignored){}
    }
    @Override protected void onResume(){ super.onResume(); if(switchMedicacion!=null) cargarRecordatorios(); }
    @Override protected void onDestroy(){ executor.shutdownNow(); super.onDestroy(); }

    private void configurarInterruptor(
            SwitchMaterial interruptor,
            String nombre) {

        if (interruptor == null) {
            return;
        }

        // El interruptor debe responder al toque
        interruptor.setClickable(true);
        interruptor.setFocusable(true);

        interruptor.setOnCheckedChangeListener((buttonView, activado) -> {
            String mensaje = activado
                    ? nombre + ": recordatorio activado"
                    : nombre + ": recordatorio desactivado";

            Toast.makeText(
                    Recordatorios.this,
                    mensaje,
                    Toast.LENGTH_SHORT
            ).show();
        });
    }
}
