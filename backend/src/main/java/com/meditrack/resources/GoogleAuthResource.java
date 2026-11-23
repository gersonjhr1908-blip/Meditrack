package com.meditrack.resources;

import com.meditrack.dto.LoginResponse;
import com.meditrack.dto.UsuarioResponse;
import com.meditrack.model.Usuario;
import com.meditrack.repository.UsuarioRepository;
import com.meditrack.service.JwtService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import org.jboss.logging.Logger;

@Path("/api/auth/google")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GoogleAuthResource {
    private static final Logger LOG = Logger.getLogger(GoogleAuthResource.class);
    private static final String GOOGLE_WEB_CLIENT_ID = "258122979520-pskaev12nijuqht486cevvkkepmi22cl.apps.googleusercontent.com";
    private static final org.jose4j.jwt.consumer.JwtConsumer GOOGLE =
            new org.jose4j.jwt.consumer.JwtConsumerBuilder()
                    .setRequireExpirationTime().setRequireSubject()
                    .setExpectedIssuers(true, "accounts.google.com", "https://accounts.google.com")
                    .setExpectedAudience(GOOGLE_WEB_CLIENT_ID)
                    .setJwsAlgorithmConstraints(org.jose4j.jwa.AlgorithmConstraints.ConstraintType.PERMIT, "RS256")
                    .setVerificationKeyResolver(new org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver(
                            new org.jose4j.jwk.HttpsJwks("https://www.googleapis.com/oauth2/v3/certs")))
                    .build();
    @Inject UsuarioRepository usuarios;
    @Inject JwtService jwtService;
    @Inject ObjectMapper mapper;

    public record GoogleRequest(String idToken) {}

    @POST
    public Response autenticar(GoogleRequest request) {
        if (request == null || request.idToken() == null || request.idToken().isBlank())
            return error(Response.Status.BAD_REQUEST, "El token de Google es obligatorio.");
        try {
            var claims = GOOGLE.processToClaims(request.idToken());
            if (!"true".equalsIgnoreCase(String.valueOf(claims.getClaimValue("email_verified"))))
                return error(Response.Status.UNAUTHORIZED, "El correo de Google no está verificado.");
            String correo = claims.getStringClaimValue("email").trim();
            Optional<Usuario> encontrado = usuarios.buscarPorIdentificador(correo);
            if (encontrado.isEmpty()) return error(Response.Status.NOT_FOUND,
                    "Ese correo todavía no está vinculado a una cuenta MediTrack.");
            Usuario usuario = encontrado.get();
            if (!"ACTIVO".equalsIgnoreCase(usuario.estado())) return error(Response.Status.FORBIDDEN, "La cuenta está inactiva.");
            usuarios.registrarUltimoAcceso(usuario.idUsuario());
            return Response.ok(new LoginResponse(jwtService.generarToken(usuario), "Bearer", 1800,
                    UsuarioResponse.desdeModelo(usuario))).build();
        } catch (org.jose4j.jwt.consumer.InvalidJwtException e) {
            LOG.warnf("Google JWT validation failed, codes=%s", e.getErrorDetails().stream().map(d -> d.getErrorCode()).toList());
            return error(Response.Status.UNAUTHORIZED, "No se pudo verificar la credencial de Google.");
        } catch (Exception e) {
            LOG.errorf("Google authentication failed: %s: %s", e.getClass().getSimpleName(), "fallo interno");
            return error(Response.Status.BAD_GATEWAY, "No se pudo validar la cuenta de Google.");
        }
    }

    private Response error(Response.Status status, String message) {
        return Response.status(status).entity(Map.of("mensaje", message)).build();
    }
}
