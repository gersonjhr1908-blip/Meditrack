
package com.example.meditrack2.Citas;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.meditrack2.R;
import com.example.meditrack2.api.ApiClient;
import org.json.JSONObject;
import java.util.concurrent.Executors;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Calendar;
import java.util.Locale;

public class NuevoRecordatorio extends AppCompatActivity {
    private final java.util.concurrent.ExecutorService executor=Executors.newSingleThreadExecutor();

    private Spinner spTipoRecordatorio;
    private Spinner spFrecuenciaRecordatorio;
    private EditText etNombreRecordatorio;
    private EditText etDetalleRecordatorio;
    private MaterialButton btnHoraRecordatorio;
    private MaterialButton btnFechaRecordatorio;
    private SwitchMaterial switchNuevoRecordatorio;

    private String horaSeleccionada = "";
    private String fechaSeleccionada = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nuevo_recordatorio);
        if (android.os.Build.VERSION.SDK_INT >= 33) requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 7001);

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

        // OBTENER LOS CONTROLES
        ImageButton btnVolver =
                findViewById(R.id.btnVolverNuevoRecordatorio);

        spTipoRecordatorio =
                findViewById(R.id.spTipoRecordatorio);

        spFrecuenciaRecordatorio =
                findViewById(R.id.spFrecuenciaRecordatorio);

        etNombreRecordatorio =
                findViewById(R.id.etNombreRecordatorio);

        etDetalleRecordatorio =
                findViewById(R.id.etDetalleRecordatorio);

        btnHoraRecordatorio =
                findViewById(R.id.btnHoraRecordatorio);

        btnFechaRecordatorio =
                findViewById(R.id.btnFechaRecordatorio);

        switchNuevoRecordatorio =
                findViewById(R.id.switchNuevoRecordatorio);

        MaterialButton btnGuardar =
                findViewById(R.id.btnGuardarRecordatorio);

        MaterialButton btnCancelar =
                findViewById(R.id.btnCancelarRecordatorio);

        // OPCIONES DEL TIPO DE RECORDATORIO
        String[] tipos = {
                "Seleccionar tipo",
                "Medicación",
                "Medición de glucosa",
                "Cita médica",
                "Ejercicio",
                "Otro"
        };

        ArrayAdapter<String> adaptadorTipos =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        tipos
                );

        adaptadorTipos.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spTipoRecordatorio.setAdapter(adaptadorTipos);

        // OPCIONES DE FRECUENCIA
        String[] frecuencias = {
                "Seleccionar frecuencia",
                "Una vez",
                "Todos los días",
                "Días laborables",
                "Cada semana"
        };

        ArrayAdapter<String> adaptadorFrecuencias =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        frecuencias
                );

        adaptadorFrecuencias.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spFrecuenciaRecordatorio.setAdapter(adaptadorFrecuencias);

        // VOLVER
        btnVolver.setOnClickListener(v -> finish());

        // CANCELAR
        btnCancelar.setOnClickListener(v -> finish());

        // SELECCIONAR HORA
        btnHoraRecordatorio.setOnClickListener(v ->
                mostrarSelectorHora()
        );

        // SELECCIONAR FECHA
        btnFechaRecordatorio.setOnClickListener(v ->
                mostrarSelectorFecha()
        );

        // GUARDAR
        btnGuardar.setOnClickListener(v ->
                guardarRecordatorio()
        );
    }

    private void mostrarSelectorHora() {

        Calendar calendario = Calendar.getInstance();

        int hora = calendario.get(Calendar.HOUR_OF_DAY);
        int minuto = calendario.get(Calendar.MINUTE);

        TimePickerDialog selectorHora = new TimePickerDialog(
                this,
                (view, horaElegida, minutoElegido) -> {

                    horaSeleccionada = String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            horaElegida,
                            minutoElegido
                    );

                    btnHoraRecordatorio.setText(
                            "Hora: " + horaSeleccionada
                    );
                },
                hora,
                minuto,
                false
        );

        selectorHora.setTitle("Seleccionar hora");
        selectorHora.show();
    }

    private void mostrarSelectorFecha() {

        Calendar calendario = Calendar.getInstance();

        int anio = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int dia = calendario.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog selectorFecha = new DatePickerDialog(
                this,
                (view, anioElegido, mesElegido, diaElegido) -> {

                    fechaSeleccionada = String.format(
                            Locale.getDefault(),
                            "%02d/%02d/%04d",
                            diaElegido,
                            mesElegido + 1,
                            anioElegido
                    );

                    btnFechaRecordatorio.setText(
                            "Fecha: " + fechaSeleccionada
                    );
                },
                anio,
                mes,
                dia
        );

        selectorFecha.getDatePicker().setMinDate(
                calendario.getTimeInMillis()
        );

        selectorFecha.setTitle("Seleccionar fecha");
        selectorFecha.show();
    }

    private void guardarRecordatorio() {

        String nombre = etNombreRecordatorio.getText()
                .toString().trim();

        String detalle = etDetalleRecordatorio.getText()
                .toString().trim();

        int posicionTipo = spTipoRecordatorio.getSelectedItemPosition();
        int posicionFrecuencia =
                spFrecuenciaRecordatorio.getSelectedItemPosition();

        // VALIDAR NOMBRE
        if (nombre.isEmpty()) {
            etNombreRecordatorio.setError(
                    "Ingresa el nombre del recordatorio"
            );
            etNombreRecordatorio.requestFocus();
            return;
        }

        // VALIDAR TIPO
        if (posicionTipo == 0) {
            Toast.makeText(
                    this,
                    "Selecciona el tipo de recordatorio",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // VALIDAR HORA
        if (horaSeleccionada.isEmpty()) {
            Toast.makeText(
                    this,
                    "Selecciona la hora del recordatorio",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // VALIDAR FECHA
        if (fechaSeleccionada.isEmpty()) {
            Toast.makeText(
                    this,
                    "Selecciona la fecha del recordatorio",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // VALIDAR FRECUENCIA
        if (posicionFrecuencia == 0) {
            Toast.makeText(
                    this,
                    "Selecciona la frecuencia",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String fechaApi=fechaSeleccionada.substring(6,10)+"-"+fechaSeleccionada.substring(3,5)+"-"+fechaSeleccionada.substring(0,2);
        executor.execute(() -> { try {
            JSONObject datos=new JSONObject().put("nombre",nombre).put("detalle",detalle)
                    .put("tipo",spTipoRecordatorio.getSelectedItem().toString())
                    .put("frecuencia",spFrecuenciaRecordatorio.getSelectedItem().toString())
                    .put("hora",horaSeleccionada).put("fecha",fechaApi).put("activo",switchNuevoRecordatorio.isChecked());
            ApiClient.solicitar(getApplicationContext(),"/api/paciente/recordatorios",datos);
            runOnUiThread(() -> { programarNotificacion(nombre,detalle); setResult(RESULT_OK,new Intent()); Toast.makeText(this,"Recordatorio guardado correctamente",Toast.LENGTH_SHORT).show(); finish(); });
        } catch(Exception e){ runOnUiThread(() -> ApiClient.mostrarError(this,e)); }});
    }

    private void programarNotificacion(String nombre,String detalle){
        try {
            String[] f=fechaSeleccionada.split("/"); String[] h=horaSeleccionada.split(":");
            Calendar cuando=Calendar.getInstance(); cuando.set(Integer.parseInt(f[2]),Integer.parseInt(f[1])-1,Integer.parseInt(f[0]),Integer.parseInt(h[0]),Integer.parseInt(h[1]),0);
            if(cuando.getTimeInMillis()<=System.currentTimeMillis()) return;
            Intent intent=new Intent(this,RecordatorioReceiver.class).putExtra("id",nombre.hashCode()).putExtra("mensaje",nombre+(detalle.isEmpty()?"":" · "+detalle));
            PendingIntent pendiente=PendingIntent.getBroadcast(this,Math.abs(nombre.hashCode()),intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
            AlarmManager alarm=(AlarmManager)getSystemService(ALARM_SERVICE);
            alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,cuando.getTimeInMillis(),pendiente);
        } catch(Exception ignored) { }
    }

    @Override protected void onDestroy(){ executor.shutdownNow(); super.onDestroy(); }
}
