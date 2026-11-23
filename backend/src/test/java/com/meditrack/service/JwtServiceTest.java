package com.meditrack.service;

import com.meditrack.model.Usuario;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class JwtServiceTest {

    @Inject
    JwtService jwtService;

    @Test
    void debeGenerarTokenJwt() {

        Usuario usuario = new Usuario(
                7,
                "99999999",
                "Usuario",
                "Prueba MediTrack",
                "prueba.login@meditrack.com",
                "hash-no-utilizado-en-esta-prueba",
                "PACIENTE",
                "ACTIVO"
        );

        String token = jwtService.generarToken(usuario);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals(3, token.split("\\.").length);
    }
}
