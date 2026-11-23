package com.example.meditrack2.Citas;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.meditrack2.R;
import com.example.meditrack2.api.ApiClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NuevaCita extends AppCompatActivity {

    private Spinner spEspecialidad;
    private Spinner spMedico;
    private EditText etMotivo;
    private Button btnFecha;
    private Button btnHora;
    private Button btnGuardar;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private String fechaSeleccionada = "";
    private String horaSeleccionada = "";
    private int especialidadActual;
    private int especialidadRestaurada;
    private int medicoRestaurado;
    private boolean guardando;

    @Override
    protected void onCreate(Bundle estado) {
        super.onCreate(estado);
        setContentView(R.layout.activity_nueva_cita);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (vista, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            vista.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return insets;
        });

        spEspecialidad = findViewById(R.id.spEspecialidad);
        spMedico = findViewById(R.id.etMedico);
        etMotivo = findViewById(R.id.etMotivo);
        btnFecha = findViewById(R.id.btnFecha);
        btnHora = findViewById(R.id.btnHora);
        btnGuardar = findViewById(R.id.btnGuardar);

        if (estado != null) {
            fechaSeleccionada = estado.getString("fecha", "");
            horaSeleccionada = estado.getString("hora", "");
            especialidadRestaurada = estado.getInt("especialidad", 0);
            medicoRestaurado = estado.getInt("medico", 0);
            if (!fechaSeleccionada.isEmpty()) {
                btnFecha.setText(fechaVisible(fechaSeleccionada));
            }
            if (!horaSeleccionada.isEmpty()) {
                btnHora.setText(horaSeleccionada);
            }
        }

        btnGuardar.setEnabled(false);
        mostrarOpciones(spEspecialidad, new ArrayList<>(), "Seleccionar especialidad");
        mostrarOpciones(spMedico, new ArrayList<>(), "Seleccionar médico");
        findViewById(R.id.btnCancelar).setOnClickListener(v -> finish());
        btnFecha.setOnClickListener(v -> seleccionarFecha());
        btnHora.setOnClickListener(v -> seleccionarHora());
        btnGuardar.setOnClickListener(v -> guardarCita());

        spEspecialidad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> padre, View vista, int posicion, long id) {
                Opcion opcion = (Opcion) padre.getItemAtPosition(posicion);
                especialidadActual = opcion.id;
                btnGuardar.setEnabled(false);
                cargarMedicos(opcion.id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> padre) {
                btnGuardar.setEnabled(false);
            }
        });
        cargarEspecialidades();
    }

    private void cargarEspecialidades() {
        executor.execute(() -> {
            try {
                JSONArray datos = new JSONArray(ApiClient.solicitar(
                        getApplicationContext(), "/api/citas/especialidades", null));
                List<Opcion> opciones = new ArrayList<>();
                for (int i = 0; i < datos.length(); i++) {
                    JSONObject item = datos.getJSONObject(i);
                    opciones.add(new Opcion(item.getInt("idEspecialidad"), item.getString("nombre")));
                }
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    mostrarOpciones(spEspecialidad, opciones, "Seleccionar especialidad");
                    seleccionarOpcion(spEspecialidad, especialidadRestaurada);
                });
            } catch (Exception error) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    mostrarOpciones(spEspecialidad, new ArrayList<>(), "Seleccionar especialidad");
                    ApiClient.mostrarError(this, error);
                });
            }
        });
    }

    private void cargarMedicos(int idEspecialidad) {
        mostrarOpciones(spMedico, new ArrayList<>(), "Seleccionar médico");
        if (idEspecialidad == 0) {
            mostrarOpciones(spMedico, new ArrayList<>(), "Seleccionar médico");
            return;
        }
        executor.execute(() -> {
            try {
                JSONArray datos = new JSONArray(ApiClient.solicitar(getApplicationContext(),
                        "/api/citas/medicos/" + idEspecialidad, null));
                List<Opcion> opciones = new ArrayList<>();
                for (int i = 0; i < datos.length(); i++) {
                    JSONObject item = datos.getJSONObject(i);
                    opciones.add(new Opcion(item.getInt("idMedico"),
                            item.getString("nombres") + " " + item.getString("apellidos")));
                }
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed() || especialidadActual != idEspecialidad) {
                        return;
                    }
                    mostrarOpciones(spMedico, opciones, "Seleccionar médico");
                    seleccionarOpcion(spMedico, medicoRestaurado);
                    btnGuardar.setEnabled(!opciones.isEmpty() && !guardando);
                });
            } catch (Exception error) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed() || especialidadActual != idEspecialidad) {
                        return;
                    }
                    mostrarOpciones(spMedico, new ArrayList<>(), "Seleccionar médico");
                    ApiClient.mostrarError(this, error);
                });
            }
        });
    }

    private void mostrarOpciones(Spinner spinner, List<Opcion> opciones, String vacio) {
        List<Opcion> elementos = new ArrayList<>(opciones);
        // Siempre dejamos una opción inicial neutra: Android selecciona el primer
        // elemento de un Spinner automáticamente y no debe escoger médico por el usuario.
        elementos.add(0, new Opcion(0, vacio));
        ArrayAdapter<Opcion> adaptador = new ArrayAdapter<>(
                this, R.layout.item_cita_spinner, elementos);
        adaptador.setDropDownViewResource(R.layout.item_cita_spinner_dropdown);
        spinner.setAdapter(adaptador);
        spinner.setSelection(0, false);
        spinner.setEnabled(!opciones.isEmpty() && !guardando);
    }

    private void seleccionarOpcion(Spinner spinner, int id) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (((Opcion) spinner.getItemAtPosition(i)).id == id) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void seleccionarFecha() {
        Calendar hoy = Calendar.getInstance();
        DatePickerDialog dialogo = new DatePickerDialog(this, (vista, year, month, day) -> {
            fechaSeleccionada = String.format(Locale.ROOT, "%04d-%02d-%02d", year, month + 1, day);
            btnFecha.setText(fechaVisible(fechaSeleccionada));
        }, hoy.get(Calendar.YEAR), hoy.get(Calendar.MONTH), hoy.get(Calendar.DAY_OF_MONTH));
        dialogo.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialogo.show();
    }

    private String fechaVisible(String fecha) {
        String[] partes = fecha.split("-");
        return partes[2] + "/" + partes[1] + "/" + partes[0];
    }

    private void seleccionarHora() {
        Calendar ahora = Calendar.getInstance();
        new TimePickerDialog(this, (vista, hora, minuto) -> {
            horaSeleccionada = String.format(Locale.ROOT, "%02d:%02d", hora, minuto);
            btnHora.setText(horaSeleccionada);
        }, ahora.get(Calendar.HOUR_OF_DAY), ahora.get(Calendar.MINUTE), true).show();
    }

    private void guardarCita() {
        Opcion especialidad = (Opcion) spEspecialidad.getSelectedItem();
        Opcion medico = (Opcion) spMedico.getSelectedItem();
        String motivo = etMotivo.getText().toString().trim();
        if (guardando || especialidad == null || medico == null
                || especialidad.id == 0 || medico.id == 0) {
            return;
        }
        if (fechaSeleccionada.isEmpty() || horaSeleccionada.isEmpty()) {
            Toast.makeText(this, "Selecciona la fecha y la hora de tu cita.", Toast.LENGTH_LONG).show();
            return;
        }
        if (motivo.isEmpty() || motivo.length() > 250) {
            etMotivo.setError("Ingresa un motivo de hasta 250 caracteres.");
            return;
        }

        guardando = true;
        String fecha = fechaSeleccionada;
        String hora = horaSeleccionada;
        btnGuardar.setEnabled(false);
        spEspecialidad.setEnabled(false);
        spMedico.setEnabled(false);
        executor.execute(() -> {
            try {
                JSONObject datos = new JSONObject();
                datos.put("tipoCita", "CONSULTA_MEDICA");
                datos.put("idEspecialidad", especialidad.id);
                datos.put("idMedico", medico.id);
                datos.put("fecha", fecha);
                datos.put("hora", hora + ":00");
                datos.put("motivo", motivo);
                String respuesta = ApiClient.solicitar(getApplicationContext(), "/api/citas", datos);
                new JSONObject(respuesta).getInt("idCita");
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    setResult(RESULT_OK, new Intent());
                    Toast.makeText(this, "Cita guardada correctamente.", Toast.LENGTH_LONG).show();
                    finish();
                });
            } catch (Exception error) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    guardando = false;
                    btnGuardar.setEnabled(true);
                    spEspecialidad.setEnabled(true);
                    spMedico.setEnabled(true);
                    ApiClient.mostrarError(this, error);
                });
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle estado) {
        super.onSaveInstanceState(estado);
        estado.putString("fecha", fechaSeleccionada);
        estado.putString("hora", horaSeleccionada);
        estado.putInt("especialidad", especialidadActual);
        Opcion medico = (Opcion) spMedico.getSelectedItem();
        estado.putInt("medico", medico == null ? 0 : medico.id);
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }

    private static final class Opcion {
        final int id;
        final String nombre;

        Opcion(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        @Override
        public String toString() {
            return nombre;
        }
    }
}
