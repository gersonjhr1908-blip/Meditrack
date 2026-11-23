package com.example.meditrack2.Login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.meditrack2.Inicio.inicio_PacienteActivity;
import com.example.meditrack2.Inicio.inicio_familiar;
import com.example.meditrack2.R;
import com.example.meditrack2.Registro.Tipo_de_usuario;
import com.example.meditrack2.api.ApiConfig;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText txtDniCorreo;
    private CheckBox recordarCuenta;
    private TextInputEditText txtContrasenaLogin;
    private Button btnIniciarsesion;
    private GoogleSignInClient googleClient;
    private final ActivityResultLauncher<Intent> googleLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() == null) { btnIniciarsesion.setEnabled(true); return; }
                try {
                    GoogleSignIn.getSignedInAccountFromIntent(result.getData())
                            .getResult(ApiException.class);
                    com.google.android.gms.auth.api.signin.GoogleSignInAccount cuenta =
                            GoogleSignIn.getSignedInAccountFromIntent(result.getData()).getResult(ApiException.class);
                    if (cuenta == null || cuenta.getIdToken() == null) throw new IllegalStateException("No se recibió el token de Google.");
                    enviarTokenGoogle(cuenta.getIdToken());
                } catch (Exception e) { mostrarErrorDeAcceso("No se pudo iniciar sesión con Google."); }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (vista, insets) -> {
                    Insets barras = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );
                    vista.setPadding(barras.left, barras.top, barras.right, barras.bottom);
                    return insets;
                }
        );

        txtDniCorreo = findViewById(R.id.txtDniCorreo);
        recordarCuenta = findViewById(R.id.checkBox2);
        String cuentaRecordada = getSharedPreferences("acceso_recordado", MODE_PRIVATE)
                .getString("identificador", "");
        txtDniCorreo.setText(cuentaRecordada);
        recordarCuenta.setChecked(!cuentaRecordada.isEmpty());
        recordarCuenta.setOnCheckedChangeListener((boton, marcada) -> {
            if (!marcada) {
                getSharedPreferences("acceso_recordado", MODE_PRIVATE)
                        .edit().remove("identificador").apply();
            }
        });
        txtContrasenaLogin = findViewById(R.id.txtContrasenaLogin);
        btnIniciarsesion = findViewById(R.id.btnIniciarsesion);

        TextView registrarse = findViewById(R.id.registrarce);
        ImageView btnGoogle = findViewById(R.id.imageView7);

        btnIniciarsesion.setOnClickListener(vista -> iniciarSesion());
        registrarse.setOnClickListener(vista ->
                startActivity(new Intent(this, Tipo_de_usuario.class)));
        btnGoogle.setOnClickListener(vista -> iniciarSesionGoogle());
        GoogleSignInOptions opcionesGoogle = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        googleClient = GoogleSignIn.getClient(this, opcionesGoogle);
    }

    private void iniciarSesion() {
        String identificador = obtenerTexto(txtDniCorreo).trim();
        String contrasena = obtenerTexto(txtContrasenaLogin).trim();

        if (identificador.isEmpty()) {
            txtDniCorreo.setError("Ingresa tu DNI o correo");
            txtDniCorreo.requestFocus();
            return;
        }

        if (contrasena.isEmpty()) {
            txtContrasenaLogin.setError("Ingresa tu contraseña");
            txtContrasenaLogin.requestFocus();
            return;
        }

        btnIniciarsesion.setEnabled(false);
        new Thread(() -> consultarLogin(identificador, contrasena)).start();
    }

    private void consultarLogin(String identificador, String contrasena) {
        HttpURLConnection conexion = null;

        try {
            URL url = new URL(ApiConfig.BASE_URL + "/api/auth/login");
            conexion = (HttpURLConnection) url.openConnection();
            conexion.setRequestMethod("POST");
            conexion.setConnectTimeout(10000);
            conexion.setReadTimeout(10000);
            conexion.setInstanceFollowRedirects(false);
            conexion.setRequestProperty("Content-Type", "application/json");
            conexion.setRequestProperty("Accept", "application/json");
            conexion.setDoOutput(true);

            JSONObject datos = new JSONObject();
            datos.put("identificador", identificador);
            datos.put("contrasena", contrasena);

            try (OutputStream salida = conexion.getOutputStream()) {
                salida.write(datos.toString().getBytes(StandardCharsets.UTF_8));
            }

            int estado = conexion.getResponseCode();
            if (estado != HttpURLConnection.HTTP_OK) {
                String mensaje = estado == HttpURLConnection.HTTP_UNAUTHORIZED
                        ? "DNI, correo o contraseña incorrectos, o cuenta inactiva."
                        : "El backend respondió con el código " + estado + ".";
                mostrarErrorDeAcceso(mensaje);
                return;
            }

            JSONObject respuesta;
            try (InputStream entrada = conexion.getInputStream();
                 ByteArrayOutputStream cuerpo = new ByteArrayOutputStream()) {
                byte[] bloque = new byte[4096];
                int cantidad;
                while ((cantidad = entrada.read(bloque)) != -1) {
                    cuerpo.write(bloque, 0, cantidad);
                }
                respuesta = new JSONObject(
                        new String(cuerpo.toByteArray(), StandardCharsets.UTF_8)
                );
            }
            JSONObject perfil = respuesta.getJSONObject("usuario");
            String nombres = perfil.getString("nombres");
            String rol = perfil.getString("rol");

            getSharedPreferences("sesion_meditrack", MODE_PRIVATE)
                    .edit()
                    .putString("access_token", respuesta.getString("accessToken"))
                    .putInt("id_usuario", perfil.getInt("idUsuario"))
                    .putString("nombres", nombres)
                    .putString("apellidos", perfil.optString("apellidos", ""))
                    .putString("dni", perfil.optString("dni", ""))
                    .putString("correo", perfil.optString("correo", ""))
                    .putString("rol", rol)
                    .apply();

            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                btnIniciarsesion.setEnabled(true);
                if (recordarCuenta.isChecked()) {
                    getSharedPreferences("acceso_recordado", MODE_PRIVATE)
                            .edit().putString("identificador", identificador).apply();
                }
                mostrarInicioExitoso(nombres, rol);
            });
        } catch (IOException error) {
            mostrarErrorDeAcceso(
                    "No se pudo conectar con Quarkus. Revisa que esté activo "
                            + "y que el puerto del emulador esté redirigido."
            );
        } catch (JSONException error) {
            mostrarErrorDeAcceso("La respuesta del backend no tiene el formato esperado.");
        } finally {
            if (conexion != null) {
                conexion.disconnect();
            }
        }
    }

    private void mostrarInicioExitoso(String nombres, String rol) {
        Class<?> destino;

        if ("PACIENTE".equalsIgnoreCase(rol)) {
            destino = inicio_PacienteActivity.class;
        } else if ("TUTOR".equalsIgnoreCase(rol) || "FAMILIAR".equalsIgnoreCase(rol)) {
            destino = inicio_familiar.class;
        } else {
            mostrarErrorDeAcceso("La pantalla de inicio de este tipo de usuario está pendiente.");
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Bienvenido")
                .setMessage("Hola " + nombres + ", has iniciado sesión correctamente.")
                .setPositiveButton("Continuar", (dialogo, boton) -> {
                    startActivity(new Intent(this, destino));
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void mostrarErrorDeAcceso(String mensaje) {
        runOnUiThread(() -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }

            btnIniciarsesion.setEnabled(true);
            new AlertDialog.Builder(this)
                    .setTitle("No se pudo iniciar sesión")
                    .setMessage(mensaje)
                    .setPositiveButton("Aceptar", null)
                    .show();
        });
    }

    private void iniciarSesionGoogle() {
        btnIniciarsesion.setEnabled(false);
        googleClient.signOut().addOnSuccessListener(this, unused -> googleLauncher.launch(googleClient.getSignInIntent()))
                .addOnFailureListener(this, error -> mostrarErrorDeAcceso("No se pudo abrir el selector de cuentas de Google."));
    }

    private void enviarTokenGoogle(String token) {
        new Thread(() -> {
            try {
                HttpURLConnection c=(HttpURLConnection)new URL(ApiConfig.BASE_URL+"/api/auth/google").openConnection();
                c.setRequestMethod("POST"); c.setConnectTimeout(10000); c.setReadTimeout(15000);
                c.setRequestProperty("Content-Type","application/json"); c.setDoOutput(true);
                JSONObject body=new JSONObject().put("idToken",token);
                try(OutputStream out=c.getOutputStream()){out.write(body.toString().getBytes(StandardCharsets.UTF_8));}
                int code=c.getResponseCode();
                if(code!=200){
                    String detalle = "";
                    InputStream error = c.getErrorStream();
                    if (error != null) {
                        try {
                            JSONObject cuerpoError = new JSONObject(leer(error));
                            detalle = cuerpoError.optString("mensaje", "");
                        } catch (Exception ignorado) { }
                    }
                    if (code == 404) detalle = "Registra primero ese correo en MediTrack para vincularlo con Google.";
                    if (detalle.isEmpty()) detalle = "No se pudo validar Google (" + code + ").";
                    mostrarErrorDeAcceso(detalle);
                    return;
                }
                JSONObject respuesta=new JSONObject(leer(c.getInputStream())); JSONObject perfil=respuesta.getJSONObject("usuario");
                getSharedPreferences("sesion_meditrack",MODE_PRIVATE).edit().putString("access_token",respuesta.getString("accessToken"))
                        .putInt("id_usuario",perfil.getInt("idUsuario")).putString("nombres",perfil.optString("nombres",""))
                        .putString("apellidos",perfil.optString("apellidos","")).putString("dni",perfil.optString("dni",""))
                        .putString("correo",perfil.optString("correo","")).putString("rol",perfil.getString("rol")).apply();
                runOnUiThread(() -> mostrarInicioExitoso(perfil.optString("nombres",""),perfil.optString("rol","")));
            } catch(Exception e){ mostrarErrorDeAcceso("No se pudo conectar con Quarkus para validar Google."); }
        }).start();
    }

    private String leer(InputStream in) throws IOException { try(InputStream entrada=in; ByteArrayOutputStream b=new ByteArrayOutputStream()){byte[] x=new byte[4096];int n;while((n=entrada.read(x))!=-1)b.write(x,0,n);return b.toString(StandardCharsets.UTF_8);}}

    private String obtenerTexto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString();
    }
}
