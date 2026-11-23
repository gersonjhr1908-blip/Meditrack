package com.meditrack.resources;

import java.sql.SQLException;
import java.util.Optional;

import com.meditrack.dto.UsuarioResponse;
import com.meditrack.service.UsuarioService;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

@Path("/api/usuarios")
public class UsuarioResource {

    @Inject
    UsuarioService usuarioService;

    @GET
    @Path("/buscar")
    public Response buscarPorIdentificador(
            @QueryParam("identificador") String identificador) {

        try {
            Optional<UsuarioResponse> resultado =
                    usuarioService.buscarPorCorreo(identificador);

            if (resultado.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"mensaje\":\"Usuario no encontrado.\"}")
                        .build();
            }

            return Response.ok(resultado.get()).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"mensaje\":\"" + e.getMessage() + "\"}")
                    .build();

        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"mensaje\":\"Error al consultar la base de datos.\"}")
                    .build();
        }
    }
}