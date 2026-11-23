package com.meditrack.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meditrack.dto.RegistroPacienteRequest;
import com.meditrack.service.RegistroPacienteService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.logging.Logger;

import java.sql.SQLException;
import java.util.Map;

@Path("/api/registros/pacientes")
@Produces(MediaType.APPLICATION_JSON)
public class RegistroPacienteResource {

    private static final Logger LOG =
            Logger.getLogger(RegistroPacienteResource.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    RegistroPacienteService registroPacienteService;

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    public Response registrar(String contenido) {
        try {
            RegistroPacienteRequest request = objectMapper.readValue(
                    contenido,
                    RegistroPacienteRequest.class
            );
            return Response.status(Response.Status.CREATED)
                    .entity(registroPacienteService.registrar(request))
                    .build();
        } catch (JsonProcessingException error) {
            LOG.error("JSON inválido recibido desde Android.", error);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of(
                            "mensaje",
                            "Los datos enviados por Android no tienen formato JSON válido."
                    ))
                    .build();
        } catch (IllegalArgumentException error) {
            LOG.warnf("Registro de paciente rechazado: %s", error.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("mensaje", error.getMessage()))
                    .build();
        } catch (SQLException error) {
            if (error.getErrorCode() == 2601 || error.getErrorCode() == 2627) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(Map.of("mensaje", "El DNI o correo ya está registrado."))
                        .build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("mensaje", "No fue posible registrar al paciente."))
                    .build();
        }
    }
}
