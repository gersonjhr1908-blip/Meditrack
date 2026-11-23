package com.meditrack.repository;

import com.meditrack.model.Usuario;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class UsuarioRepositoryTest {

    @Inject
    UsuarioRepository usuarioRepository;

    @Test
    void debeEncontrarUsuarioPorCorreo() throws SQLException {

        Optional<Usuario> resultado =
                usuarioRepository.buscarPorIdentificador(
                        "juan.paciente@meditrack.com"
                );

        assertTrue(resultado.isPresent());

        Usuario usuario = resultado.get();

        assertEquals(4, usuario.idUsuario());
        assertEquals("PACIENTE", usuario.rol());
        assertEquals("ACTIVO", usuario.estado());
    }

    @Test
    void debeEncontrarUsuarioPorDni() throws SQLException {

        Optional<Usuario> resultado =
                usuarioRepository.buscarPorIdentificador("99999999");

        assertTrue(resultado.isPresent());

        Usuario usuario = resultado.get();

        assertEquals(7, usuario.idUsuario());
        assertEquals("PACIENTE", usuario.rol());
        assertEquals("ACTIVO", usuario.estado());
    }

    @Test
    void noDebeEncontrarUsuarioInexistente() throws SQLException {

        Optional<Usuario> resultado =
                usuarioRepository.buscarPorIdentificador(
                        "00000000"
                );

        assertTrue(resultado.isEmpty());
    }
}