package com.meditrack.repository;

import com.meditrack.model.Usuario;
import com.meditrack.dto.RegistroPacienteResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Optional;

@ApplicationScoped
public class UsuarioRepository {

    @Inject
    DataSource dataSource;

    public void registrarUltimoAcceso(int idUsuario) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE dbo.USUARIO SET ultimo_acceso = GETDATE() WHERE id_usuario = ?")) {
            statement.setInt(1, idUsuario);
            statement.executeUpdate();
        }
    }

    private static final String SQL_BUSCAR_POR_IDENTIFICADOR = """
            SELECT
                id_usuario,
                dni,
                nombres,
                apellidos,
                correo,
                password_hash,
                rol,
                estado
            FROM dbo.USUARIO
            WHERE correo = ? OR dni = ?
            """;

    public Optional<Usuario> buscarPorIdentificador(
            String identificador) throws SQLException {

        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(
                            SQL_BUSCAR_POR_IDENTIFICADOR
                    )
        ) {

            statement.setString(1, identificador);
            statement.setString(2, identificador);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {

                    Usuario usuario = new Usuario(
                            result.getInt("id_usuario"),
                            result.getString("dni"),
                            result.getString("nombres"),
                            result.getString("apellidos"),
                            result.getString("correo"),
                            result.getString("password_hash"),
                            result.getString("rol"),
                            result.getString("estado")
                    );

                    return Optional.of(usuario);
                }

                return Optional.empty();
            }
        }
    }

    public RegistroPacienteResponse crearPaciente(
            String dni,
            String nombres,
            String apellidos,
            String correo,
            String telefono,
            String sexo,
            LocalDate fechaNacimiento,
            String passwordHash) throws SQLException {

        String insertarUsuario = """
                INSERT INTO dbo.USUARIO
                    (dni, nombres, apellidos, telefono, correo, password_hash,
                     rol, estado, fecha_creacion, sexo)
                VALUES (?, ?, ?, ?, ?, ?, 'PACIENTE', 'ACTIVO', GETDATE(), ?)
                """;
        String insertarPaciente = """
                INSERT INTO dbo.PACIENTE
                    (id_usuario, codigo_paciente, fecha_nacimiento)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            try {
                Integer idUsuario;
                try (PreparedStatement statement = connection.prepareStatement(
                        insertarUsuario, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setString(1, dni);
                    statement.setString(2, nombres);
                    statement.setString(3, apellidos);
                    statement.setString(4, telefono);
                    statement.setString(5, correo);
                    statement.setString(6, passwordHash);
                    statement.setString(7, sexo);
                    statement.executeUpdate();

                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("No se pudo obtener el identificador del usuario.");
                        }
                        idUsuario = keys.getInt(1);
                    }
                }

                String codigoPaciente = String.format("PAC-%06d", idUsuario);
                Integer idPaciente;
                try (PreparedStatement statement = connection.prepareStatement(
                        insertarPaciente, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setInt(1, idUsuario);
                    statement.setString(2, codigoPaciente);
                    statement.setObject(3, fechaNacimiento);
                    statement.executeUpdate();

                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("No se pudo obtener el identificador del paciente.");
                        }
                        idPaciente = keys.getInt(1);
                    }
                }

                connection.commit();
                return new RegistroPacienteResponse(
                        idUsuario,
                        idPaciente,
                        codigoPaciente,
                        "Paciente registrado correctamente."
                );
            } catch (SQLException error) {
                connection.rollback();
                throw error;
            }
        }
    }
}
