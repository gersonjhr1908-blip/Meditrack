package com.example.meditrack2.Citas;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.meditrack2.R;
import com.example.meditrack2.api.ApiClient;
import org.json.JSONObject;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

public class Vinvular_paciente extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vinvular_paciente);
        findViewById(R.id.btnVolverVincularPaciente).setOnClickListener(v -> finish());
        EditText codigo = findViewById(R.id.etCodigoVinculacionPaciente);
        Button enviar = findViewById(R.id.btnEnviarSolicitudVinculacion);
        enviar.setOnClickListener(v -> {
            String valor = codigo.getText() == null ? "" : codigo.getText().toString().trim();
            if (valor.isEmpty()) { codigo.setError("Ingresa el código"); return; }
            enviar.setEnabled(false);
            new Thread(() -> {
                try {
                    JSONObject cuerpo = new JSONObject().put("codigo", valor);
                    ApiClient.solicitar(getApplicationContext(), "/api/vinculacion/aceptar", cuerpo);
                    runOnUiThread(() -> {
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Solicitud enviada")
                            .setMessage("El paciente debe aceptar tu solicitud.")
                            .setCancelable(false)
                            .setPositiveButton("Aceptar", (dialogo, boton) -> finish())
                            .show();

                    });
                } catch (Exception error) {
                    runOnUiThread(() -> {
                        enviar.setEnabled(true);
                        Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }, "aceptar-vinculacion").start();
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
