package com.meditrack.resources;

import com.meditrack.service.PasswordService;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.Locale;
import java.nio.charset.StandardCharsets;

@Path("/api/registros/familiares")
@Consumes("application/json")
@Produces("application/json")
public class RegistroFamiliarResource {
    @Inject DataSource dataSource;
    @Inject PasswordService passwords;

    public record Datos(String nombres, String apellidos, String dni, String correo,
                        String telefono, String sexo, String relacion, String contrasena) {}

    @POST
    @PermitAll
    public Response registrar(Datos datos) {
        try {
            if (datos == null) return respuesta(400, "Completa el formulario.");
            String dni = requerido(datos.dni());
            String telefono = requerido(datos.telefono());
            String correo = requerido(datos.correo()).toLowerCase(Locale.ROOT);
            String nombres = requerido(datos.nombres());
            String apellidos = requerido(datos.apellidos());
            String sexo = requerido(datos.sexo());
            String relacion = requerido(datos.relacion());
            String clave = requerido(datos.contrasena());
            if (!dni.matches("[0-9]{8}") || !telefono.matches("[0-9]{9}")
                    || !correo.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                return respuesta(400, "Revisa el DNI, teléfono y correo.");
            }
            if (clave.length() < 8 || clave.getBytes(StandardCharsets.UTF_8).length > 72
                    || !clave.matches(".*[A-Z].*") || !clave.matches(".*[a-z].*")
                    || !clave.matches(".*[0-9].*")) {
                return respuesta(400, "Usa al menos 8 caracteres con mayúscula, minúscula y número.");
            }
            String hash = passwords.generarHash(clave);
            try (var conexion = dataSource.getConnection()) {
                conexion.setAutoCommit(false);
                try {
                    int usuario;
                    try (var insertar = conexion.prepareStatement("""
                            INSERT INTO dbo.USUARIO
                            (dni, nombres, apellidos, telefono, correo, password_hash, rol, estado, fecha_creacion, sexo)
                            VALUES (?, ?, ?, ?, ?, ?, 'TUTOR', 'ACTIVO', GETDATE(), ?)
                            """, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                        String[] valores = {dni, nombres, apellidos, telefono, correo, hash, sexo};
                        for (int i = 0; i < valores.length; i++) insertar.setString(i + 1, valores[i]);
                        insertar.executeUpdate();
                        try (var claves = insertar.getGeneratedKeys()) {
                            if (!claves.next()) throw new SQLException("Identificador de usuario ausente");
                            usuario = claves.getInt(1);
                        }
                    }
                    try (var insertar = conexion.prepareStatement(
                            "INSERT INTO dbo.TUTOR (id_usuario, relacion) VALUES (?, ?)")) {
                        insertar.setInt(1, usuario);
                        insertar.setString(2, relacion);
                        insertar.executeUpdate();
                    }
                    conexion.commit();
                } catch (SQLException error) {
                    conexion.rollback();
                    throw error;
                }
            }
            return respuesta(201, "Cuenta familiar creada.");
        } catch (IllegalArgumentException error) {
            return respuesta(400, "Completa todos los campos.");
        } catch (SQLException error) {
            if (error.getErrorCode() == 2601 || error.getErrorCode() == 2627) {
                return respuesta(409, "El DNI o correo ya está registrado.");
            }
            org.jboss.logging.Logger.getLogger(RegistroFamiliarResource.class)
                    .errorf("Error SQL en registro familiar. Código %d", error.getErrorCode());
            return respuesta(500, "No se pudo guardar la cuenta familiar.");
        }
    }

    private static String requerido(String valor) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException();
        return valor.trim();
    }

    private static Response respuesta(int estado, String mensaje) {
        return Response.status(estado).entity(Map.of("mensaje", mensaje)).build();
    }
}
