package com.example.meditrack2.Login;

import android.os.Bundle;
import android.widget.*;
import com.example.meditrack2.api.ApiClient;
import org.json.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.meditrack2.R;

public class AccesoPersonalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_acceso_personal);
        Button boton = findViewById(R.id.btnIniciarsesion);
        EditText correo = findViewById(R.id.identificadorPersonal);
        EditText clave = findViewById(R.id.clavePersonal);
        boton.setOnClickListener(v -> {
            String identificador = correo.getText().toString().trim();
            String password = clave.getText().toString();
            if (identificador.isEmpty()) { correo.setError("Ingresa tu DNI o correo"); return; }
            if (password.isEmpty()) { clave.setError("Ingresa tu contraseña"); return; }
            boton.setEnabled(false);
            new Thread(() -> {
                try {
                    JSONObject datos = new JSONObject().put("identificador", identificador).put("contrasena", password);
                    JSONObject respuesta = new JSONObject(ApiClient.loginPersonal(datos));
                    JSONObject usuario = respuesta.getJSONObject("usuario");
                    getSharedPreferences("sesion_meditrack", MODE_PRIVATE).edit().clear()
                        .putString("access_token", respuesta.getString("accessToken"))
                        .putInt("id_usuario", usuario.getInt("idUsuario"))
                        .putString("nombres", usuario.getString("nombres")).putString("rol", usuario.getString("rol"))
                        .putString("correo", usuario.optString("correo")).putString("dni", usuario.optString("dni"))
                        .putString("apellidos", usuario.optString("apellidos")).apply();
                    runOnUiThread(() -> { startActivity(new android.content.Intent(this, PanelPersonal.class)); finish(); });
                } catch (Exception ex) { runOnUiThread(() -> { boton.setEnabled(true); Toast.makeText(this,ex.getMessage(),Toast.LENGTH_LONG).show(); }); }
            }).start();
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}