package com.example.meditrack2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.meditrack2.Login.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private carga_de_ingreso_main carga;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener( findViewById(R.id.main), (v, insets) -> {
        Insets systemBars = insets.getInsets(  WindowInsetsCompat.Type.systemBars()   );
         v.setPadding(  systemBars.left, systemBars.top, systemBars.right,systemBars.bottom       );
           return insets;
                }
        );

        progressBar = findViewById(R.id.progressBar);

        carga = new carga_de_ingreso_main(progressBar);

        carga.iniciarCarga(() -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    LoginActivity.class
            );

            startActivity(intent);

            finish();
        });
    }
}