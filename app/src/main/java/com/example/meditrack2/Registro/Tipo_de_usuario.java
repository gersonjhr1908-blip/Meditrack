package com.example.meditrack2.Registro;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.meditrack2.R;
import com.example.meditrack2.Login.AccesoPersonalActivity;

public class Tipo_de_usuario extends AppCompatActivity {

    private CardView cardPaciente;
    private CardView cardFamiliar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tipo_de_usuario);
        findViewById(R.id.imageButton).setOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
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

        // PACIENTE
        cardPaciente = findViewById(R.id.cardPaciente);

        cardPaciente.setOnClickListener(v -> {

            Intent intent = new Intent(
                    Tipo_de_usuario.this,
                    RegistroPacienteActivity.class
            );

            startActivity(intent);
        });

        // FAMILIAR
        cardFamiliar = findViewById(R.id.cardFamiliar);

        cardFamiliar.setOnClickListener(v -> {

            Intent intent = new Intent(
                    Tipo_de_usuario.this,
                    RegistroFamiliarActivity.class
            );

            startActivity(intent);
        });

        // ACCESO DE PERSONAL
        findViewById(R.id.btnAccesoPersonal).setOnClickListener(v -> {

            Intent intent = new Intent(
                    Tipo_de_usuario.this,
                    AccesoPersonalActivity.class
            );

            startActivity(intent);
        });
    }
}
