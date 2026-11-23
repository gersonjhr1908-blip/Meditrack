package com.meditrack.resources;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Map;

@Path("/api/auth")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
public class AuthPerfilResource {

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/perfil")
    public Map<String, Object> obtenerPerfil() {

        return Map.of(
                "idUsuario", jwt.getSubject(),
                "correo", jwt.getName(),
                "roles", jwt.getGroups()
        );
    }
}
