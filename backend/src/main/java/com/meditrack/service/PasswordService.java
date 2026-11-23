package com.meditrack.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.mindrot.jbcrypt.BCrypt;

@ApplicationScoped
public class PasswordService {

    private static final int COSTE_BCRYPT = 12;

    public String generarHash(String contrasena) {

        if (contrasena == null || contrasena.isBlank()) {
            throw new IllegalArgumentException(
                    "La contraseña es obligatoria."
            );
        }

        return BCrypt.hashpw(
                contrasena,
                BCrypt.gensalt(COSTE_BCRYPT)
        );
    }

    public boolean verificar(
            String contrasena,
            String hashAlmacenado) {

        if (contrasena == null || hashAlmacenado == null) {
            return false;
        }

        try {
            return BCrypt.checkpw(
                    contrasena,
                    hashAlmacenado
            );
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}