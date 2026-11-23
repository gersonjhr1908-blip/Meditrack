package com.meditrack.service;

import com.meditrack.dto.UsuarioResponse;
import com.meditrack.repository.UsuarioRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.util.Optional;

@ApplicationScoped
public class UsuarioService {

    @Inject
    UsuarioRepository usuarioRepository;

    public Optional<UsuarioResponse> buscarPorCorreo(String correo)
            throws SQLException {

        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException(
                    "El correo es obligatorio."
            );
        }

        return usuarioRepository.buscarPorIdentificador(correo.trim())
                .map(UsuarioResponse::desdeModelo);
    }
}