package com.example.meditrack2.Login;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.meditrack2.api.ApiClient;
import org.json.*;

public class PanelPersonal extends AppCompatActivity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        var session = getSharedPreferences("sesion_meditrack", MODE_PRIVATE);
        ScrollView scroll = new ScrollView(this);
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        int pad = Math.round(24 * getResources().getDisplayMetrics().density);
        panel.setPadding(pad, pad*2, pad, pad);
        scroll.addView(panel);
        setContentView(scroll);
        TextView titulo = new TextView(this);
        titulo.setTextSize(24);
        titulo.setText("Hola, " + session.getString("nombres", ""));
        panel.addView(titulo);
        TextView agenda = new TextView(this);
        panel.addView(agenda);
        if ("MEDICO".equals(session.getString("rol",""))) {
            agenda.setText("Cargando citas…");
            new Thread(() -> {
                try {
                    JSONArray citas = new JSONArray(ApiClient.solicitar(this,"/api/personal/agenda",null));
                    StringBuilder texto = new StringBuilder("Mis citas\n");
                    if (citas.length()==0) texto.append("Aún no tienes citas asignadas.");
                    for(int i=0;i<citas.length();i++) texto.append("\n").append(citas.getString(i)).append("\n");
                    runOnUiThread(() -> agenda.setText(texto.toString()));
                } catch(Exception e) { runOnUiThread(() -> agenda.setText(e.getMessage())); }
            }).start();
        }
        Button salir = new Button(this);
        salir.setText("Cerrar sesión");
        panel.addView(salir);
        salir.setOnClickListener(v -> { session.edit().clear().apply(); finish(); });
    }
}
