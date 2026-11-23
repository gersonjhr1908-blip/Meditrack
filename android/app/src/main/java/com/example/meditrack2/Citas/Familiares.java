
package com.example.meditrack2.Citas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.meditrack2.R;
import com.example.meditrack2.api.ApiClient;
import org.json.JSONArray;

public class Familiares extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // CONFIGURACIÓN DE LA PANTALLA
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_familiares);
        findViewById(R.id.btnVolverFamiliares).setOnClickListener(v -> finish());

        View vistaPrincipal = findViewById(R.id.main);

        if (vistaPrincipal != null) {
            ViewCompat.setOnApplyWindowInsetsListener(
                    vistaPrincipal, (v, insets) -> {

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

        // ACCIÓN: ABRIR VINCULACIÓN DE FAMILIAR
        View.OnClickListener abrirVinculacion = v -> {
            Intent intent = new Intent(
                    Familiares.this,
                    Vincular_Familiar.class
            );
            startActivity(intent);
        };

        // BOTÓN: VINCULAR FAMILIAR
        // Este ID debe coincidir con el del XML.
        View botonVincular = findViewById(R.id.btnGenerarCodigoVinculacion);

        if (botonVincular != null) {
            botonVincular.setOnClickListener(abrirVinculacion);
        } else {
            Toast.makeText(
                    this,
                    "Revisa el ID del botón Vincular familiar en el XML",
                    Toast.LENGTH_LONG
            ).show();
        }
        cargarFamiliares();
        findViewById(R.id.btnAceptarFamiliar).setVisibility(View.GONE);
        findViewById(R.id.btnEliminarFamiliar).setVisibility(View.GONE);
    }

    private void cargarFamiliares() {
        new Thread(() -> {
            try {
                JSONArray datos = new JSONArray(ApiClient.solicitar(this, "/api/vinculacion/familiares", null));
                JSONArray solicitudes = new JSONArray(ApiClient.solicitar(this, "/api/vinculacion/solicitudes", null));
                runOnUiThread(() -> {
                    TextView lista=findViewById(R.id.tvFamiliaresVinculados);
                    Button eliminar=findViewById(R.id.btnEliminarFamiliar);
                    eliminar.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_delete, 0, 0, 0);
                    if (datos.length()>0) {
                        org.json.JSONObject f=datos.optJSONObject(0);
                        lista.setText(f.optString("nombres")+" "+f.optString("apellidos")+" · "+f.optString("estado"));
                        lista.setTag(f.optInt("id"));
                        eliminar.setVisibility(View.VISIBLE);
                        eliminar.setOnClickListener(v -> new AlertDialog.Builder(this)
                                .setTitle("Quitar vínculo")
                                .setMessage("¿Quieres quitar este familiar vinculado?")
                                .setNegativeButton("Cancelar",null)
                                .setPositiveButton("Eliminar",(d,w)->eliminarVinculo(lista.getTag().toString()))
                                .show());
                    } else {
                        lista.setText("Aún no hay familiares vinculados para mostrar.");
                        eliminar.setVisibility(View.GONE);
                    }
                    LinearLayout contenedor=findViewById(R.id.contenedorSolicitudes);
                    TextView vacio=findViewById(R.id.tvSinSolicitudes);
                    if(solicitudes.length()==0) {
                        vacio.setVisibility(View.VISIBLE);
                    } else {
                        vacio.setVisibility(View.GONE);
                        for(int i=0;i<solicitudes.length();i++) {
                            final org.json.JSONObject s=solicitudes.optJSONObject(i);
                            TextView texto=new TextView(this);
                            texto.setText(s.optString("nombres")+" "+s.optString("apellidos"));
                            texto.setTextColor(0xff183B70); texto.setTextSize(15);
                            texto.setPadding(24,20,12,12);
                            contenedor.addView(texto);
                            Button rechazar=new Button(this);
                            rechazar.setText("Rechazar");
                            LinearLayout acciones = new LinearLayout(this);
                            Button aceptar = new Button(this);
                            aceptar.setText("Aceptar");
                            acciones.addView(aceptar);
                            acciones.addView(rechazar);
                            contenedor.addView(acciones);
                            aceptar.setVisibility(View.VISIBLE);
                            aceptar.setOnClickListener(v->decidir(s.optInt("id"),true));
                            rechazar.setOnClickListener(v->decidir(s.optInt("id"),false));
                        }
                    }
                });
            } catch (Exception error) { runOnUiThread(() -> Toast.makeText(this,error.getMessage(),Toast.LENGTH_LONG).show()); }
        }).start();
    }

    private void decidir(int id, boolean aprobar) {
        new Thread(()->{try {
            org.json.JSONObject body=new org.json.JSONObject().put("idTutorPaciente",id).put("aceptar",aprobar);
            ApiClient.solicitar(this,"/api/vinculacion/decidir",body);
            runOnUiThread(()->{Toast.makeText(this,aprobar?"Solicitud aceptada.":"Solicitud rechazada.",Toast.LENGTH_SHORT).show(); recreate();});
        } catch(Exception e){runOnUiThread(()->Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show());}}).start();
    }

    private void eliminarVinculo(String id) {
        new Thread(()->{try {
            ApiClient.eliminar(this,"/api/vinculacion/familiares/"+id);
            runOnUiThread(()->{Toast.makeText(this,"Vínculo eliminado.",Toast.LENGTH_SHORT).show(); recreate();});
        } catch(Exception e){runOnUiThread(()->Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG).show());}}).start();
    }
}
