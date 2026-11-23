
package com.example.meditrack2.Citas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.cardview.widget.CardView;

import com.example.meditrack2.R;
import com.example.meditrack2.api.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.concurrent.Executors;
import com.google.android.material.button.MaterialButton;

public class Medicamentos extends AppCompatActivity {
    private final java.util.concurrent.ExecutorService executor = Executors.newSingleThreadExecutor();

    private TextView tvActivos;
    private TextView tvFinalizados;

    private View listaMedicamentos;

    private MaterialButton btnAgregar;
    private ImageButton btnVolver;

    private CardView cardMetformina;
    private CardView cardInsulina;
    private CardView cardOmeprazol;
    private CardView cardAtorvastatina;

    private boolean mostrarActivos = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_medicamentos);

        // Ajustar la pantalla a las barras del sistema
        View principal = findViewById(R.id.main);

        if (principal != null) {
            ViewCompat.setOnApplyWindowInsetsListener(
                    principal,
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

        // PESTAÑAS
        tvActivos = findViewById(R.id.tvMedicamentosActivos);
        tvFinalizados = findViewById(R.id.tvMedicamentosFinalizados);

        // TARJETAS DE MEDICAMENTOS
        cardMetformina = findViewById(R.id.cardMetformina);
        cardInsulina = findViewById(R.id.cardInsulina);
        cardOmeprazol = findViewById(R.id.cardOmeprazol);
        cardAtorvastatina = findViewById(R.id.cardAtorvastatina);

        // CONTENEDOR DE LA LISTA
        listaMedicamentos = findViewById(R.id.scrollMedicamentos);

        // BOTONES
        btnAgregar = findViewById(R.id.btnAgregarMedicamento);
        btnVolver = findViewById(R.id.btnVolverMedicamentos);

        // ABRIR FORMULARIO AGREGAR MEDICAMENTO
        btnAgregar.setOnClickListener(v -> {
            Intent intent = new Intent(
                    Medicamentos.this,
                    AgregarMedicamentos.class
            );

            startActivity(intent);
        });

        // VOLVER A LA PANTALLA ANTERIOR
        btnVolver.setOnClickListener(v -> finish());

        // CAMBIAR ENTRE PESTAÑAS
        tvActivos.setOnClickListener(v -> mostrarPestanaActivos());

        tvFinalizados.setOnClickListener(v -> mostrarPestanaFinalizados());

        // DETALLES DE MEDICAMENTOS
        configurarDetalle(
                R.id.btnDetalleMetformina,
                "Metformina",
                "Dosis de ejemplo: 500 mg"
        );

        configurarDetalle(
                R.id.btnDetalleInsulina,
                "Insulina",
                "Dosis de ejemplo: 10 UI"
        );

        configurarDetalle(
                R.id.btnDetalleOmeprazol,
                "Omeprazol",
                "Dosis de ejemplo: 20 mg"
        );

        configurarDetalle(
                R.id.btnDetalleAtorvastatina,
                "Atorvastatina",
                "Dosis de ejemplo: 10 mg"
        );

        // MOSTRAR ACTIVOS AL ENTRAR
        mostrarPestanaActivos();
        cargarMedicamentos();
    }

    private void cargarMedicamentos() {
        executor.execute(() -> {
            try {
                JSONArray datos = new JSONArray(ApiClient.solicitar(getApplicationContext(), "/api/paciente/medicamentos", null));
                runOnUiThread(() -> aplicarMedicamentos(datos));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "No se pudieron cargar los medicamentos.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void aplicarMedicamentos(JSONArray datos) {
        TextView vacio=findViewById(R.id.tvSinMedicamentos);
        if(vacio!=null) vacio.setVisibility(datos.length()==0?View.VISIBLE:View.GONE);
        CardView[] tarjetas={cardMetformina,cardInsulina,cardOmeprazol,cardAtorvastatina};
        int[] nombres={R.id.tvMetformina,R.id.tvInsulina,R.id.tvOmeprazol,R.id.tvAtorvastatina};
        int[] dosis={R.id.tvDosisMetformina,R.id.tvDosisInsulina,R.id.tvDosisOmeprazol,R.id.tvDosisAtorvastatina};
        int[] frecuencias={R.id.tvFrecuenciaMetformina,R.id.tvFrecuenciaInsulina,R.id.tvFrecuenciaOmeprazol,R.id.tvFrecuenciaAtorvastatina};
        for(int i=0;i<tarjetas.length;i++) tarjetas[i].setVisibility(View.GONE);
        ViewGroup contenedor=(ViewGroup)cardMetformina.getParent();
        if(contenedor!=null) contenedor.setVisibility(datos.length()==0?View.GONE:View.VISIBLE);
        for(int i=0;i<datos.length() && i<tarjetas.length;i++) try {
            JSONObject d=datos.getJSONObject(i); tarjetas[i].setVisibility(View.VISIBLE);
            ((TextView)findViewById(nombres[i])).setText(d.optString("nombre","Medicamento"));
            ((TextView)findViewById(dosis[i])).setText("Dosis: "+d.optString("dosis","-")+" "+d.optString("unidad_dosis",""));
            ((TextView)findViewById(frecuencias[i])).setText(d.optString("frecuencia",""));
        } catch(Exception ignored) { }
    }

    @Override protected void onDestroy(){ executor.shutdownNow(); super.onDestroy(); }

    // PESTAÑA ACTIVOS
    private void mostrarPestanaActivos() {
        mostrarActivos = true;

        actualizarEstiloPestanas();

        listaMedicamentos.setVisibility(View.VISIBLE);

        cardMetformina.setVisibility(View.VISIBLE);
        cardInsulina.setVisibility(View.VISIBLE);
        cardOmeprazol.setVisibility(View.VISIBLE);
        cardAtorvastatina.setVisibility(View.VISIBLE);

        btnAgregar.setVisibility(View.VISIBLE);
    }

    // PESTAÑA FINALIZADOS
    private void mostrarPestanaFinalizados() {
        mostrarActivos = false;

        actualizarEstiloPestanas();

        listaMedicamentos.setVisibility(View.VISIBLE);
        btnAgregar.setVisibility(View.GONE);

        Toast.makeText(
                this,
                "Todavía no hay medicamentos finalizados.",
                Toast.LENGTH_SHORT
        ).show();
    }

    // ACTUALIZAR ESTILO DE LAS PESTAÑAS
    private void actualizarEstiloPestanas() {

        if (mostrarActivos) {

            tvActivos.setBackgroundResource(
                    R.drawable.bg_nav_selected
            );

            tvActivos.setTextColor(
                    android.graphics.Color.WHITE
            );

            tvFinalizados.setBackgroundResource(
                    android.R.color.transparent
            );

            tvFinalizados.setTextColor(
                    android.graphics.Color.rgb(89, 118, 157)
            );

        } else {

            tvFinalizados.setBackgroundResource(
                    R.drawable.bg_nav_selected
            );

            tvFinalizados.setTextColor(
                    android.graphics.Color.WHITE
            );

            tvActivos.setBackgroundResource(
                    android.R.color.transparent
            );

            tvActivos.setTextColor(
                    android.graphics.Color.rgb(89, 118, 157)
            );
        }
    }

    // MOSTRAR DETALLES DE CADA MEDICAMENTO
    private void configurarDetalle(
            int idVista,
            String nombre,
            String dosis
    ) {
        View detalle = findViewById(idVista);

        if (detalle != null) {
            detalle.setOnClickListener(v ->
                    new AlertDialog.Builder(Medicamentos.this)
                            .setTitle(nombre)
                            .setMessage(
                                    dosis
                                            + "\n\nEstos datos son de demostración. "
                                            + "Consulta siempre tu receta médica."
                            )
                            .setPositiveButton("Cerrar", null)
                            .show()
            );
        }
    }
}
