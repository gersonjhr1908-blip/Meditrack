package com.example.meditrack2;

import android.os.Handler;
import android.widget.ProgressBar;

public class carga_de_ingreso_main {

    private ProgressBar progressBar;
    private Handler handler;
    private int progreso = 0;

    public carga_de_ingreso_main(ProgressBar progressBar) {
        this.progressBar = progressBar;
        this.handler = new Handler();
    }

    public void iniciarCarga(Runnable alTerminar) {

        progreso = 0;
        progressBar.setProgress(0);

        handler.postDelayed(new Runnable() {

            @Override
            public void run() {

                progreso++;

                progressBar.setProgress(progreso);

                if (progreso < 100) {

                    handler.postDelayed(this, 30);

                } else {

                    alTerminar.run();
                }
            }

        }, 30);
    }
}