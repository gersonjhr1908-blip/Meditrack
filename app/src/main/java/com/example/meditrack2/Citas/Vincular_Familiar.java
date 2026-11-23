package com.example.meditrack2.Citas;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.meditrack2.R;
import com.example.meditrack2.api.ApiClient;
import org.json.JSONObject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.security.SecureRandom;
import java.util.Locale;

public class Vincular_Familiar extends AppCompatActivity {

    // ELEMENTOS DE LA PANTALLA
    private TextView tvCodigoVinculacion;
    private TextView tvGenerarNuevoCodigo;
    private TextView tvValidezCodigo;

    private ImageView imgCodigoQRVinculacion;
    private ImageView btnVolverVincularFamiliar;
    private Button btnCompartirCodigo;

    // CONTROL DEL CÓDIGO
    private CountDownTimer temporizador;
    private String codigoActual = "";
    private long venceEn = 0L;

    // CONFIGURACIÓN
    private static final long DURACION_CODIGO = 10 * 60 * 1000L;

    private static final String CARACTERES =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final SecureRandom random = new SecureRandom();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vincular_familiar);

        // CONFIGURAR BARRAS DEL SISTEMA
        View vistaPrincipal = findViewById(R.id.main);

        if (vistaPrincipal != null) {
            ViewCompat.setOnApplyWindowInsetsListener(
                    vistaPrincipal,
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

        // CONECTAR ELEMENTOS DEL XML
        tvCodigoVinculacion =
                findViewById(R.id.tvCodigoVinculacion);

        tvGenerarNuevoCodigo =
                findViewById(R.id.tvGenerarNuevoCodigo);

        tvValidezCodigo =
                findViewById(R.id.tvValidezCodigo);

        imgCodigoQRVinculacion =
                findViewById(R.id.imgCodigoQRVinculacion);

        btnCompartirCodigo =
                findViewById(R.id.btnCompartirCodigo);

        btnVolverVincularFamiliar =
                findViewById(R.id.btnVolverVincularFamiliar);

        // EVENTO: VOLVER
        if (btnVolverVincularFamiliar != null) {
            btnVolverVincularFamiliar.setOnClickListener(
                    v -> finish()
            );
        }

        // EVENTO: GENERAR NUEVO CÓDIGO
        if (tvGenerarNuevoCodigo != null) {
            tvGenerarNuevoCodigo.setOnClickListener(
                    v -> generarCodigo()
            );
        }

        // EVENTO: COMPARTIR CÓDIGO
        if (btnCompartirCodigo != null) {
            btnCompartirCodigo.setOnClickListener(
                    v -> compartirCodigo()
            );
        }

        // GENERAR EL PRIMER CÓDIGO Y SU QR
        generarCodigo();
    }

    // GENERAR CÓDIGO DE VINCULACIÓN
    private void generarCodigo() {

        // CANCELAR TEMPORIZADOR ANTERIOR
        if (temporizador != null) {
            temporizador.cancel();
        }

        new Thread(() -> {
            try {
                JSONObject respuesta = new JSONObject(ApiClient.solicitar(getApplicationContext(), "/api/vinculacion/generar", new JSONObject()));
                runOnUiThread(() -> mostrarCodigoServidor(respuesta.optString("codigo", "")));
            } catch (Exception error) {
                runOnUiThread(() -> Toast.makeText(this, "No se pudo generar el código. Revisa Quarkus.", Toast.LENGTH_LONG).show());
            }
        }, "generar-vinculacion").start();
    }

    private void mostrarCodigoServidor(String codigo) {
        if (codigo.isBlank()) return;
        if (temporizador != null) temporizador.cancel();
        codigoActual = codigo;
        venceEn = System.currentTimeMillis() + DURACION_CODIGO;
        tvCodigoVinculacion.setText("•  " + codigoActual + "  •");
        generarQR(codigoActual);
        actualizarTemporizador();
    }

    private void generarCodigoLocal() {
        StringBuilder codigo = new StringBuilder("MT-");

        for (int i = 0; i < 6; i++) {
            codigo.append(
                    CARACTERES.charAt(
                            random.nextInt(CARACTERES.length())
                    )
            );
        }

        codigoActual = codigo.toString();

        // DEFINIR CADUCIDAD
        venceEn = System.currentTimeMillis() + DURACION_CODIGO;

        // MOSTRAR CÓDIGO
        tvCodigoVinculacion.setText(
                "•  " + codigoActual + "  •"
        );

        // GENERAR QR REAL CON EL MISMO CÓDIGO
        generarQR(codigoActual);

        // INICIAR CUENTA REGRESIVA
        actualizarTemporizador();

        Toast.makeText(
                this,
                "Nuevo código de demostración generado",
                Toast.LENGTH_SHORT
        ).show();
    }

    // GENERAR QR REAL
    private void generarQR(String codigo) {

        try {
            int ancho = 400;
            int alto = 400;

            BitMatrix matriz = new MultiFormatWriter().encode(
                    codigo,
                    BarcodeFormat.QR_CODE,
                    ancho,
                    alto
            );

            Bitmap bitmap = Bitmap.createBitmap(
                    ancho,
                    alto,
                    Bitmap.Config.ARGB_8888
            );

            for (int x = 0; x < ancho; x++) {
                for (int y = 0; y < alto; y++) {

                    bitmap.setPixel(
                            x,
                            y,
                            matriz.get(x, y)
                                    ? Color.BLACK
                                    : Color.WHITE
                    );
                }
            }

            // MOSTRAR QR EN LA PANTALLA
            imgCodigoQRVinculacion.setImageBitmap(bitmap);

            imgCodigoQRVinculacion.setContentDescription(
                    "QR de vinculación: " + codigo
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "No se pudo generar el código QR",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    // MOSTRAR TIEMPO RESTANTE
    private void actualizarTemporizador() {

        if (temporizador != null) {
            temporizador.cancel();
        }

        temporizador = new CountDownTimer(
                DURACION_CODIGO,
                1000
        ) {

            @Override
            public void onTick(long millisUntilFinished) {

                long minutos = millisUntilFinished / 60000;
                long segundos =
                        (millisUntilFinished % 60000) / 1000;

                tvValidezCodigo.setText(
                        String.format(
                                Locale.getDefault(),
                                "Caduca en %02d:%02d",
                                minutos,
                                segundos
                        )
                );
            }

            @Override
            public void onFinish() {

                codigoActual = "";

                // OCULTAR CÓDIGO CADUCADO
                tvCodigoVinculacion.setText("Código caducado");

                tvValidezCodigo.setText(
                        "Genera un nuevo código para continuar"
                );

                // ELIMINAR QR CADUCADO
                imgCodigoQRVinculacion.setImageDrawable(null);

                venceEn = 0L;
            }

        }.start();
    }

    // COMPARTIR Y COPIAR CÓDIGO
    private void compartirCodigo() {

        // COMPROBAR CADUCIDAD
        if (codigoActual.isEmpty()
                || System.currentTimeMillis() >= venceEn) {

            Toast.makeText(
                    this,
                    "El código ha caducado. Genera uno nuevo.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        // COPIAR AL PORTAPAPELES
        ClipboardManager clipboard =
                (ClipboardManager) getSystemService(
                        Context.CLIPBOARD_SERVICE
                );

        if (clipboard != null) {

            clipboard.setPrimaryClip(
                    ClipData.newPlainText(
                            "Código de MediTrack",
                            codigoActual
                    )
            );
        }

        // COMPARTIR INVITACIÓN
        Intent compartir = new Intent(Intent.ACTION_SEND);

        compartir.setType("text/plain");

        compartir.putExtra(
                Intent.EXTRA_TEXT,
                "Te invito a vincularte conmigo en MediTrack. "
                        + "Mi código de invitación es: "
                        + codigoActual
                        + ". Código de demostración válido por "
                        + "10 minutos desde su generación."
        );

        startActivity(
                Intent.createChooser(
                        compartir,
                        "Compartir invitación de MediTrack"
                )
        );
    }

    @Override
    protected void onDestroy() {

        if (temporizador != null) {
            temporizador.cancel();
        }

        super.onDestroy();
    }
}
