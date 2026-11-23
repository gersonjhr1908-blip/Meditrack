package com.example.meditrack2.Citas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.meditrack2.Inicio.inicio_PacienteActivity;
import com.example.meditrack2.R;
import com.example.meditrack2.api.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.View;
import java.util.concurrent.Executors;

public class Tratamientos extends AppCompatActivity {
    private final java.util.concurrent.ExecutorService executor=Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tratamientos);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {
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

        ImageButton btnVolver =
                findViewById(R.id.btnVolverTratamientos);

        btnVolver.setOnClickListener(v -> finish());
        cargarTratamientos();
    }

    private void cargarTratamientos(){ executor.execute(() -> { try {
        JSONArray datos=new JSONArray(ApiClient.solicitar(getApplicationContext(),"/api/paciente/tratamientos",null));
        runOnUiThread(() -> {
            TextView nombre=findViewById(R.id.tvNombreTratamiento);
            TextView vacio=findViewById(R.id.tvSinTratamientos);
            if(datos.length()==0){ if(vacio!=null) vacio.setVisibility(View.VISIBLE); return; }
            if(vacio!=null) vacio.setVisibility(View.GONE);
            try { JSONObject d=datos.getJSONObject(0); nombre.setText(d.optString("nombre","Tratamiento"));
                TextView detalle=findViewById(R.id.tvDetalleTratamiento);
                if(detalle!=null) detalle.setText(d.optString("diagnostico_referencia", "") + "  •  " + d.optString("estado", ""));
                ViewGroup bloque=findViewById(R.id.contenedorTratamientos);
                if(bloque!=null) {
                    bloque.setVisibility(View.VISIBLE);
                    for(int i=1;i<bloque.getChildCount();i++) bloque.getChildAt(i).setVisibility(View.GONE);
                }
            } catch(Exception ignored){}
        });
    } catch(Exception e){ runOnUiThread(() -> android.widget.Toast.makeText(this,"No se pudieron cargar los tratamientos.",android.widget.Toast.LENGTH_SHORT).show()); }}); }
    @Override protected void onDestroy(){executor.shutdownNow();super.onDestroy();}
}
