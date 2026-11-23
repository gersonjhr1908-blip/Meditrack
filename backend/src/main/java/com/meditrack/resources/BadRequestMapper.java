package com.meditrack.resources;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

import java.util.Map;

@Provider
public class BadRequestMapper implements ExceptionMapper<BadRequestException> {

    private static final Logger LOG = Logger.getLogger(BadRequestMapper.class);

    @Override
    public Response toResponse(BadRequestException error) {
        LOG.error("Solicitud HTTP inválida.", error);

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of(
                        "mensaje",
                        "El servidor no pudo leer los datos enviados por la aplicación."
                ))
                .build();
    }
}
