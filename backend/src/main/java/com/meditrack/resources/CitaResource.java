package com.meditrack.resources;

import com.meditrack.dto.CitaRequest;
import com.meditrack.service.CitaService;

import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.jwt.JsonWebToken;

import jakarta.inject.Inject;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.SQLException;
import java.util.Map;

@Path("/api/citas")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
public class CitaResource {

    @Inject
    CitaService citaService;

    @Inject
    JsonWebToken jwt;

    @GET
    @RolesAllowed("PACIENTE")
    public Response listarMisCitas() {
        try {
            return Response.ok(citaService.listarMisCitas(jwt.getSubject())).build();
        } catch (IllegalArgumentException error) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("mensaje", "La sesión no identifica un paciente válido."))
                    .build();
        } catch (SQLException error) {
            return Response.serverError()
                    .entity(Map.of("mensaje", "No se pudieron consultar tus citas."))
                    .build();
        }
    }

    // ==========================================
    // 1. LISTAR ESPECIALIDADES
    // GET /api/citas/especialidades
    // ==========================================

    @GET
    @Path("/especialidades")
    public Response listarEspecialidades() {

        try {
            return Response.ok(
                    citaService.listarEspecialidades()
            ).build();

        } catch (SQLException e) {
            return Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR
            ).entity(Map.of(
                    "mensaje",
                    "Error al consultar las especialidades."
            )).build();
        }
    }

    // ==========================================
    // 2. LISTAR MEDICOS POR ESPECIALIDAD
    // GET /api/citas/medicos/{idEspecialidad}
    // ==========================================

    @GET
    @Path("/medicos/{idEspecialidad}")
    public Response listarMedicosPorEspecialidad(
            @PathParam("idEspecialidad") Integer idEspecialidad) {

        try {
            return Response.ok(
                    citaService.listarMedicosPorEspecialidad(
                            idEspecialidad
                    )
            ).build();

        } catch (IllegalArgumentException e) {
            return Response.status(
                    Response.Status.BAD_REQUEST
            ).entity(Map.of(
                    "mensaje",
                    e.getMessage()
            )).build();

        } catch (SQLException e) {
            return Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR
            ).entity(Map.of(
                    "mensaje",
                    "Error al consultar los médicos."
            )).build();
        }
    }

    // ==========================================
    // 3. LISTAR SERVICIOS
    // GET /api/citas/servicios
    // ==========================================

    @GET
    @Path("/servicios")
    public Response listarServicios() {

        try {
            return Response.ok(
                    citaService.listarServicios()
            ).build();

        } catch (SQLException e) {
            return Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR
            ).entity(Map.of(
                    "mensaje",
                    "Error al consultar los servicios."
            )).build();
        }
    }

    // ==========================================
    // 4. REGISTRAR CITA
    // POST /api/citas
    // ==========================================

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registrarCita(CitaRequest request) {

        try {

            var cita = citaService.registrarCita(
                    request,
                    jwt.getSubject(),
                    jwt.getGroups()
            );

            return Response.status(
                    Response.Status.CREATED
            ).entity(cita).build();

        } catch (IllegalArgumentException e) {

            return Response.status(
                    Response.Status.BAD_REQUEST
            ).entity(Map.of(
                    "mensaje",
                    e.getMessage()
            )).build();

        } catch (SecurityException e) {

            return Response.status(
                    Response.Status.FORBIDDEN
            ).entity(Map.of(
                    "mensaje",
                    e.getMessage()
            )).build();

        } catch (IllegalStateException e) {

            return Response.status(
                    Response.Status.CONFLICT
            ).entity(Map.of(
                    "mensaje",
                    e.getMessage()
            )).build();

        } catch (SQLException e) {

            return Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR
            ).entity(Map.of(
                    "mensaje",
                    "Error al registrar la cita."
            )).build();
        }
    }
}
