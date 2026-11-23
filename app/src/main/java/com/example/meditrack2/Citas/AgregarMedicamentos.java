
package com.example.meditrack2.Citas;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.meditrack2.R;
import com.google.android.material.button.MaterialButton;

import java.util.Calendar;
import java.util.Locale;

public class AgregarMedicamentos extends AppCompatActivity {

    private EditText etNombre, etDosis, etObservaciones;
    private Spinner spUnidad, spFrecuencia;

    private MaterialButton btnHorario, btnFechaInicio, btnFechaFin;
    private String horario = "";
    private String fechaInicio = "";
    private String fechaFin = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_agregar_medicamentos);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );
                    return insets;
                });

        etNombre = findViewById(R.id.etNombreMedicamento);
        etDosis = findViewById(R.id.etDosisMedicamento);
        etObservaciones = findViewById(R.id.etObservacionesMedicamento);

        spUnidad = findViewById(R.id.spUnidadMedicamento);
        spFrecuencia = findViewById(R.id.spFrecuenciaMedicamento);

        btnHorario = findViewById(R.id.btnSeleccionarHorario);
        btnFechaInicio = findViewById(R.id.btnFechaInicioMedicamento);
        btnFechaFin = findViewById(R.id.btnFechaFinMedicamento);

        MaterialButton btnGuardar =
                findViewById(R.id.btnGuardarMedicamento);

        MaterialButton btnCancelar =
                findViewById(R.id.btnCancelarMedicamento);

        findViewById(R.id.btnVolverFormulario)
                .setOnClickListener(v -> finish());

        btnCancelar.setOnClickListener(v -> finish());

        btnHorario.setOnClickListener(v -> seleccionarHorario());
        btnFechaInicio.setOnClickListener(v -> seleccionarFecha(true));
        btnFechaFin.setOnClickListener(v -> seleccionarFecha(false));

        btnGuardar.setOnClickListener(v -> guardarMedicamento());
    }

    private void seleccionarHorario() {
        Calendar c = Calendar.getInstance();

        new TimePickerDialog(this, (view, hora, minuto) -> {
            horario = String.format(
                    Locale.getDefault(), "%02d:%02d", hora, minuto);
            btnHorario.setText(horario);
        }, c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE), true).show();
    }

    private void seleccionarFecha(boolean esInicio) {
        Calendar c = Calendar.getInstance();

        new DatePickerDialog(this, (view, anio, mes, dia) -> {
            String fecha = String.format(
                    Locale.getDefault(),
                    "%02d/%02d/%04d",
                    dia, mes + 1, anio);

            if (esInicio) {
                fechaInicio = fecha;
                btnFechaInicio.setText(fecha);
            } else {
                fechaFin = fecha;
                btnFechaFin.setText(fecha);
            }
        }, c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void guardarMedicamento() {
        String nombre = etNombre.getText().toString().trim();
        String dosis = etDosis.getText().toString().trim();
        String unidad = spUnidad.getSelectedItem().toString();
        String frecuencia = spFrecuencia.getSelectedItem().toString();
        String observaciones =
                etObservaciones.getText().toString().trim();

        if (nombre.isEmpty()) {
            etNombre.setError("Ingresa el nombre del medicamento");
            etNombre.requestFocus();
            return;
        }

        if (dosis.isEmpty()) {
            etDosis.setError("Ingresa la dosis");
            etDosis.requestFocus();
            return;
        }

        try {
            if (Double.parseDouble(dosis) <= 0) {
                etDosis.setError("La dosis debe ser mayor que cero");
                etDosis.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etDosis.setError("Ingresa una dosis válida");
            etDosis.requestFocus();
            return;
        }

        if (frecuencia.equals("Seleccionar frecuencia")) {
            Toast.makeText(this, "Selecciona la frecuencia",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (horario.isEmpty()) {
            Toast.makeText(this, "Selecciona el horario",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (fechaInicio.isEmpty()) {
            Toast.makeText(this, "Selecciona la fecha de inicio",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!fechaFin.isEmpty()) {
            try {
                java.text.SimpleDateFormat formato =
                        new java.text.SimpleDateFormat(
                                "dd/MM/yyyy", Locale.getDefault());
                formato.setLenient(false);

                if (formato.parse(fechaFin)
                        .before(formato.parse(fechaInicio))) {
                    Toast.makeText(this,
                            "La fecha final no puede ser anterior al inicio",
                            Toast.LENGTH_LONG).show();
                    return;
                }
            } catch (java.text.ParseException e) {
                Toast.makeText(this, "Revisa las fechas seleccionadas",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        android.content.Intent resultado =
                new android.content.Intent();

        resultado.putExtra("nombre", nombre);
        resultado.putExtra("dosis", dosis);
        resultado.putExtra("unidad", unidad);
        resultado.putExtra("frecuencia", frecuencia);
        resultado.putExtra("horario", horario);
        resultado.putExtra("fechaInicio", fechaInicio);
        resultado.putExtra("fechaFin", fechaFin);
        resultado.putExtra("observaciones", observaciones);

        setResult(RESULT_OK, resultado);

        Toast.makeText(this, "Medicamento registrado en el formulario",
                Toast.LENGTH_SHORT).show();

        finish();
    }
}
