package com.meditrack.service;

import com.meditrack.model.Usuario;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class AuthServiceTest {

    @Inject
    AuthService authService;

    @Test
    void debeAutenticarUsuarioConCorreo()
            throws SQLException {

        Optional<Usuario> resultado = authService.autenticar(
                "prueba.login@meditrack.com",
                "MediTrack_Test2026*"
        );

        assertTrue(resultado.isPresent());
        assertEquals(7, resultado.get().idUsuario());
        assertEquals("PACIENTE", resultado.get().rol());
    }

    @Test
    void debeAutenticarUsuarioConDni()
            throws SQLException {

        Optional<Usuario> resultado = authService.autenticar(
                "99999999",
                "MediTrack_Test2026*"
        );

        assertTrue(resultado.isPresent());
        assertEquals(7, resultado.get().idUsuario());
        assertEquals("PACIENTE", resultado.get().rol());
    }

    @Test
    void debeRechazarContrasenaIncorrecta()
            throws SQLException {

        Optional<Usuario> resultado = authService.autenticar(
                "prueba.login@meditrack.com",
                "ContrasenaIncorrecta"
        );

        assertTrue(resultado.isEmpty());
    }

    @Test
    void debeRechazarUsuarioInexistente()
            throws SQLException {

        Optional<Usuario> resultado = authService.autenticar(
                "00000000",
                "CualquierClave2026"
        );

        assertTrue(resultado.isEmpty());
    }
}