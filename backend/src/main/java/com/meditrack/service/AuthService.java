package com.meditrack.service;

import com.meditrack.model.Usuario;
import com.meditrack.repository.UsuarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.util.Optional;

@ApplicationScoped
public class AuthService {

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    PasswordService passwordService;

    public Optional<Usuario> autenticar(
            String correo,
            String contrasena) throws SQLException {

        if (correo == null || correo.isBlank()
                || contrasena == null || contrasena.isBlank()) {
            return Optional.empty();
        }

        Optional<Usuario> resultado =
                usuarioRepository.buscarPorIdentificador(correo.trim());
        if (resultado.isEmpty()) {
            return Optional.empty();
        }

        Usuario usuario = resultado.get();

        if (!"ACTIVO".equalsIgnoreCase(usuario.estado())) {
            return Optional.empty();
        }

        boolean contrasenaValida = passwordService.verificar(
                contrasena,
                usuario.passwordHash()
        );

        if (!contrasenaValida) {
            return Optional.empty();
        }

        return Optional.of(usuario);
    }
}