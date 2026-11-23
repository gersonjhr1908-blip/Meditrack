package com.meditrack.resources;

import com.meditrack.dto.LoginRequest;
import com.meditrack.dto.LoginResponse;
import com.meditrack.dto.UsuarioResponse;
import com.meditrack.model.Usuario;
import com.meditrack.service.AuthService;
import com.meditrack.service.JwtService;
import com.meditrack.repository.UsuarioRepository;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.logging.Logger;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Path("/api/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final Logger LOG =
            Logger.getLogger(AuthResource.class);

    private static final Set<String> ROLES_GENERALES =
            Set.of("PACIENTE", "TUTOR");

    private static final Set<String> ROLES_PERSONAL =
            Set.of("ADMINISTRADOR", "MEDICO", "ENFERMERO");

    @Inject
    AuthService authService;

    @Inject
    JwtService jwtService;

    @Inject
    UsuarioRepository usuarioRepository;

    // ==========================================
    // 1. LOGIN GENERAL
    // POST /api/auth/login
    // ==========================================

    @POST
    @Path("/login")
    public Response login(LoginRequest request) {

        return autenticarPorTipo(
                request,
                ROLES_GENERALES
        );
    }

    // ==========================================
    // 2. LOGIN PERSONAL
    // POST /api/auth/login-personal
    // ==========================================

    @POST
    @Path("/login-personal")
    public Response loginPersonal(LoginRequest request) {

        return autenticarPorTipo(
                request,
                ROLES_PERSONAL
        );
    }

    // ==========================================
    // 3. PROCESAR AUTENTICACION
    // ==========================================

    private Response autenticarPorTipo(
            LoginRequest request,
            Set<String> rolesPermitidos) {

        // Validar solicitud

        if (request == null) {
            return error(
                    Response.Status.BAD_REQUEST,
                    "Los datos de autenticación son obligatorios."
            );
        }

        String identificador = request.identificador();
        String contrasena = request.contrasena();

        if (identificador == null
                || identificador.isBlank()
                || contrasena == null
                || contrasena.isBlank()) {

            return error(
                    Response.Status.BAD_REQUEST,
                    "Identificador y contraseña son obligatorios."
            );
        }

        try {

            // Autenticar usuario

            Optional<Usuario> resultado = authService.autenticar(
                    identificador.trim(),
                    contrasena
            );

            if (resultado.isEmpty()) {

                return error(
                        Response.Status.UNAUTHORIZED,
                        "Credenciales incorrectas."
                );
            }

            Usuario usuario = resultado.get();

            usuarioRepository.registrarUltimoAcceso(usuario.idUsuario());

            // Validar rol

            if (usuario.rol() == null
                    || !rolesPermitidos.contains(usuario.rol())) {

                return error(
                        Response.Status.FORBIDDEN,
                        "No tienes permiso para acceder desde este portal."
                );
            }

            // Generar JWT

            String token = jwtService.generarToken(usuario);

            if (token == null || token.isBlank()) {

                LOG.error(
                        "El servicio de autenticación generó un token vacío."
                );

                return error(
                        Response.Status.INTERNAL_SERVER_ERROR,
                        "No fue posible completar la autenticación."
                );
            }

            // Construir respuesta

            LoginResponse respuesta = new LoginResponse(
                    token,
                    "Bearer",
                    1800,
                    UsuarioResponse.desdeModelo(usuario)
            );

            return Response.ok(respuesta).build();

        } catch (SQLException e) {

            LOG.error(
                    "Error de base de datos durante la autenticación.",
                    e
            );

            return error(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "Error interno al procesar la autenticación."
            );
        }
    }

    // ==========================================
    // 4. RESPUESTAS DE ERROR
    // ==========================================

    private Response error(
            Response.Status estado,
            String mensaje) {

        return Response.status(estado)
                .entity(Map.of("mensaje", mensaje))
                .build();
    }
}
