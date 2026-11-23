package com.example.meditrack2.api;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.meditrack2.Login.LoginActivity;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class ApiClient {

    private ApiClient() {
    }

    // Ejecutar desde un hilo de trabajo.
    public static String solicitar(Context contexto, String ruta, JSONObject datos)
            throws Exception {
        String token = contexto.getSharedPreferences("sesion_meditrack", Context.MODE_PRIVATE)
                .getString("access_token", "");
        if (token.isEmpty()) {
            throw new ErrorApi(401, "Inicia sesión nuevamente para consultar tus citas.");
        }
        return enviar(ruta, datos, token);
    }

    public static String loginPersonal(JSONObject datos) throws Exception {
        return enviar("/api/auth/login-personal", datos, "");
    }

    public static String registrarFamiliar(JSONObject datos) throws Exception {
        return enviar("/api/registros/familiares", datos, "");
    }

    public static String eliminar(Context contexto, String ruta) throws Exception {
        String token = contexto.getSharedPreferences("sesion_meditrack", Context.MODE_PRIVATE)
                .getString("access_token", "");
        if (token.isEmpty()) throw new ErrorApi(401, "Inicia sesión nuevamente.");
        HttpURLConnection conexion=(HttpURLConnection)new URL(ApiConfig.BASE_URL+ruta).openConnection();
        try {
            conexion.setRequestMethod("DELETE");
            conexion.setConnectTimeout(10000);
            conexion.setReadTimeout(15000);
            conexion.setRequestProperty("Accept","application/json");
            conexion.setRequestProperty("Authorization","Bearer "+token);
            int status=conexion.getResponseCode();
            if(status<200 || status>=300) throw new ErrorApi(status,"No se pudo eliminar el vínculo (HTTP " + status + ").");
            return "";
        } finally { conexion.disconnect(); }
    }

    private static String enviar(String ruta, JSONObject datos, String token) throws Exception {
        HttpURLConnection conexion = (HttpURLConnection)
                new URL(ApiConfig.BASE_URL + ruta).openConnection();
        try {
            String metodo = datos == null ? "GET" : "POST";
            if (ruta.startsWith("DELETE ")) { metodo = "DELETE"; ruta = ruta.substring(7); }
            conexion.setRequestMethod(metodo);
            conexion.setConnectTimeout(10000);
            conexion.setReadTimeout(15000);
            conexion.setInstanceFollowRedirects(false);
            conexion.setRequestProperty("Accept", "application/json");
            if (!token.isEmpty()) conexion.setRequestProperty("Authorization", "Bearer " + token);
            if (datos != null) {
                conexion.setRequestProperty("Content-Type", "application/json");
                conexion.setDoOutput(true);
                try (OutputStream salida = conexion.getOutputStream()) {
                    salida.write(datos.toString().getBytes(StandardCharsets.UTF_8));
                }
            }

            int codigo = conexion.getResponseCode();
            InputStream flujo = codigo >= 200 && codigo < 300
                    ? conexion.getInputStream() : conexion.getErrorStream();
            String cuerpo = "";
            if (flujo != null) {
                try (InputStream entrada = flujo;
                     ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
                    byte[] bloque = new byte[4096];
                    int cantidad;
                    while ((cantidad = entrada.read(bloque)) != -1) {
                        salida.write(bloque, 0, cantidad);
                    }
                    cuerpo = new String(salida.toByteArray(), StandardCharsets.UTF_8);
                }
            }
            if (codigo < 200 || codigo >= 300) {
                String mensaje = "El servidor respondió " + codigo + ".";
                if (codigo == 401) {
                    mensaje = "Tu sesión venció. Inicia sesión nuevamente.";
                } else if (cuerpo.startsWith("{")) {
                    mensaje = new JSONObject(cuerpo).optString("mensaje", mensaje);
                }
                throw new ErrorApi(codigo, mensaje);
            }
            return cuerpo;
        } finally {
            conexion.disconnect();
        }
    }

    // Llamar desde el hilo principal.
    public static void mostrarError(Activity actividad, Exception error) {
        String mensaje = error instanceof ErrorApi
                ? error.getMessage()
                : "No se pudo completar la conexión. Revisa Quarkus e inténtalo otra vez.";
        Toast.makeText(actividad, mensaje, Toast.LENGTH_LONG).show();
        if (error instanceof ErrorApi && ((ErrorApi) error).codigo == 401) {
            actividad.getSharedPreferences("sesion_meditrack", Context.MODE_PRIVATE)
                    .edit().clear().apply();
            Intent intent = new Intent(actividad, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            actividad.startActivity(intent);
            actividad.finish();
        }
    }

    public static final class ErrorApi extends IOException {
        public final int codigo;

        public ErrorApi(int codigo, String mensaje) {
            super(mensaje);
            this.codigo = codigo;
        }
    }
}
