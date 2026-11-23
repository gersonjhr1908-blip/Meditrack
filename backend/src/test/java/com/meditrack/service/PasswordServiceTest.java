package com.meditrack.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class PasswordServiceTest {

    @Inject
    PasswordService passwordService;

    @Test
    void debeGenerarHashValido() {
        String contrasena = "MediTrack2026*";

        String hash = passwordService.generarHash(contrasena);

        assertNotNull(hash);
        assertEquals(60, hash.length());
        assertTrue(hash.startsWith("$2a$12$"));
    }

    @Test
    void debeAceptarContrasenaCorrecta() {
        String contrasena = "MediTrack2026*";

        String hash = passwordService.generarHash(contrasena);

        assertTrue(
                passwordService.verificar(contrasena, hash)
        );
    }

    @Test
    void debeRechazarContrasenaIncorrecta() {
        String hash = passwordService.generarHash("MediTrack2026*");

        assertFalse(
                passwordService.verificar("OtraClave2026*", hash)
        );
    }

    @Test
    void debeRechazarHashInvalido() {
        assertFalse(
                passwordService.verificar(
                        "MediTrack2026*",
                        "hash-invalido"
                )
        );
    }
}