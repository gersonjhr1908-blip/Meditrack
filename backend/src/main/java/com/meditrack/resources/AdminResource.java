package com.meditrack.resources;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.Map;

@Path("/api/admin")
@Produces(MediaType.APPLICATION_JSON)
public class AdminResource {

    @GET
    @Path("/acceso")
    @RolesAllowed("ADMINISTRADOR")
    public Map<String, String> verificarAcceso() {

        return Map.of(
                "mensaje", "Acceso administrativo autorizado."
        );
    }
}
