package com.meditrack.service;

import com.meditrack.model.Usuario;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Set;

@ApplicationScoped
public class JwtService {

    private static final long DURACION_TOKEN = 1800;

    public String generarToken(Usuario usuario) {

        return Jwt.issuer("meditrack-api")
                .subject(usuario.idUsuario().toString())
                .upn(usuario.correo())
                .groups(Set.of(usuario.rol()))
                .expiresIn(DURACION_TOKEN)
                .sign();
    }
}