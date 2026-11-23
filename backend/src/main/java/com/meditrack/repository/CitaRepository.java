package com.meditrack.repository;

import com.meditrack.dto.CitaRequest;
import com.meditrack.dto.CitaListadoResponse;
import com.meditrack.model.Cita;
import com.meditrack.model.Especialidad;
import com.meditrack.model.Medico;
import com.meditrack.model.Servicio;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class CitaRepository {

    @Inject
    DataSource dataSource;

    public List<CitaListadoResponse> listarPorUsuario(Integer idUsuario)
            throws SQLException {
        String sql = """
                SELECT c.id_cita,
                    COALESCE(e.nombre, s.nombre, c.tipo_cita) AS especialidad,
                    COALESCE(u.nombres + ' ' + u.apellidos, '') AS medico,
                    c.fecha, c.hora, c.motivo, c.estado,
                    CAST(CASE WHEN c.estado IN ('PENDIENTE', 'CONFIRMADA')
                        AND (c.fecha > CAST(GETDATE() AS DATE)
                            OR (c.fecha = CAST(GETDATE() AS DATE)
                                AND c.hora > CAST(GETDATE() AS TIME)))
                        THEN 1 ELSE 0 END AS BIT) AS proxima
                FROM dbo.CITA c
                INNER JOIN dbo.PACIENTE paciente ON paciente.id_paciente = c.id_paciente
                LEFT JOIN dbo.ESPECIALIDAD e ON e.id_especialidad = c.id_especialidad
                LEFT JOIN dbo.SERVICIO s ON s.id_servicio = c.id_servicio
                LEFT JOIN dbo.MEDICO m ON m.id_medico = c.id_medico
                LEFT JOIN dbo.PERSONAL p ON p.id_personal = m.id_personal
                LEFT JOIN dbo.USUARIO u ON u.id_usuario = p.id_usuario
                WHERE paciente.id_usuario = ?
                ORDER BY c.fecha, c.hora, c.id_cita
                """;
        List<CitaListadoResponse> citas = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idUsuario);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    citas.add(new CitaListadoResponse(
                            result.getInt("id_cita"),
                            result.getString("especialidad"),
                            result.getString("medico"),
                            result.getDate("fecha").toLocalDate(),
                            result.getTime("hora").toLocalTime(),
                            result.getString("motivo"),
                            result.getString("estado"),
                            result.getBoolean("proxima")
                    ));
                }
            }
        }
        return citas;
    }

    // ==========================================
    // 1. CONSULTAR ESPECIALIDADES ACTIVAS
    // ==========================================

    private static final String SQL_ESPECIALIDADES = """
            SELECT id_especialidad, nombre, descripcion, estado
            FROM dbo.ESPECIALIDAD
            WHERE estado = 'ACTIVA'
            ORDER BY nombre
            """;

    public List<Especialidad> listarEspecialidades()
            throws SQLException {

        List<Especialidad> especialidades = new ArrayList<>();

        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(SQL_ESPECIALIDADES);
            ResultSet result = statement.executeQuery()
        ) {
            while (result.next()) {
                especialidades.add(new Especialidad(
                        result.getInt("id_especialidad"),
                        result.getString("nombre"),
                        result.getString("descripcion"),
                        result.getString("estado")
                ));
            }
        }

        return especialidades;
    }

    // ==========================================
    // 2. CONSULTAR MEDICOS POR ESPECIALIDAD
    // ==========================================

    private static final String SQL_MEDICOS_POR_ESPECIALIDAD = """
            SELECT
                m.id_medico,
                u.dni,
                u.nombres,
                u.apellidos,
                m.codigo_profesional,
                e.id_especialidad,
                e.nombre AS especialidad,
                me.es_principal
            FROM dbo.MEDICO m
            INNER JOIN dbo.PERSONAL p
                ON p.id_personal = m.id_personal
            INNER JOIN dbo.USUARIO u
                ON u.id_usuario = p.id_usuario
            INNER JOIN dbo.MEDICO_ESPECIALIDAD me
                ON me.id_medico = m.id_medico
            INNER JOIN dbo.ESPECIALIDAD e
                ON e.id_especialidad = me.id_especialidad
            WHERE e.id_especialidad = ?
              AND e.estado = 'ACTIVA'
              AND u.estado = 'ACTIVO'
              AND p.estado_laboral = 'ACTIVO'
              AND me.fecha_inicio <= CAST(GETDATE() AS DATE)
              AND (
                    me.fecha_fin IS NULL
                    OR me.fecha_fin >= CAST(GETDATE() AS DATE)
              )
            ORDER BY me.es_principal DESC, u.apellidos, u.nombres
            """;

    public List<Medico> listarMedicosPorEspecialidad(
            Integer idEspecialidad) throws SQLException {

        List<Medico> medicos = new ArrayList<>();

        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(
                            SQL_MEDICOS_POR_ESPECIALIDAD)
        ) {
            statement.setInt(1, idEspecialidad);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    medicos.add(new Medico(
                            result.getInt("id_medico"),
                            result.getString("dni"),
                            result.getString("nombres"),
                            result.getString("apellidos"),
                            result.getString("codigo_profesional"),
                            result.getInt("id_especialidad"),
                            result.getString("especialidad"),
                            result.getBoolean("es_principal")
                    ));
                }
            }
        }

        return medicos;
    }

    // ==========================================
    // 3. CONSULTAR SERVICIOS ACTIVOS
    // ==========================================

    private static final String SQL_SERVICIOS = """
            SELECT id_servicio, id_area, nombre, descripcion, estado
            FROM dbo.SERVICIO
            WHERE estado = 'ACTIVO'
            ORDER BY nombre
            """;

    public List<Servicio> listarServicios() throws SQLException {

        List<Servicio> servicios = new ArrayList<>();

        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(SQL_SERVICIOS);
            ResultSet result = statement.executeQuery()
        ) {
            while (result.next()) {
                servicios.add(new Servicio(
                        result.getInt("id_servicio"),
                        result.getInt("id_area"),
                        result.getString("nombre"),
                        result.getString("descripcion"),
                        result.getString("estado")
                ));
            }
        }

        return servicios;
    }

    // ==========================================
    // 4. OBTENER ID PACIENTE POR ID USUARIO
    // ==========================================

    public Integer obtenerIdPacientePorUsuario(Integer idUsuario)
            throws SQLException {

        String sql = """
                SELECT id_paciente
                FROM dbo.PACIENTE
                WHERE id_usuario = ?
                """;

        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {
            statement.setInt(1, idUsuario);

            try (ResultSet result = statement.executeQuery()) {
                return result.next()
                        ? result.getInt("id_paciente")
                        : null;
            }
        }
    }

    // ==========================================
    // 5. OBTENER ID TUTOR POR ID USUARIO
    // ==========================================

    public Integer obtenerIdTutorPorUsuario(Integer idUsuario)
            throws SQLException {

        String sql = """
                SELECT id_tutor
                FROM dbo.TUTOR
                WHERE id_usuario = ?
                """;

        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {
            statement.setInt(1, idUsuario);

            try (ResultSet result = statement.executeQuery()) {
                return result.next()
                        ? result.getInt("id_tutor")
                        : null;
            }
        }
    }

    // ==========================================
    // 6. VERIFICAR VINCULACION TUTOR-PACIENTE
    // ==========================================

    private boolean tieneVinculacionAceptada(
            Connection connection,
            Integer idTutor,
            Integer idPaciente) throws SQLException {

        String sql = """
                SELECT 1
                FROM dbo.TUTOR_PACIENTE WITH (UPDLOCK, HOLDLOCK)
                WHERE id_tutor = ?
                  AND id_paciente = ?
                  AND estado = 'ACEPTADA'
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, idTutor);
            statement.setInt(2, idPaciente);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    // ==========================================
    // 7. VALIDAR MEDICO Y ESPECIALIDAD
    // ==========================================

    private boolean medicoTieneEspecialidadActiva(
            Connection connection,
            Integer idMedico,
            Integer idEspecialidad) throws SQLException {

        String sql = """
                SELECT 1
                FROM dbo.MEDICO m
                INNER JOIN dbo.PERSONAL p
                    ON p.id_personal = m.id_personal
                INNER JOIN dbo.USUARIO u
                    ON u.id_usuario = p.id_usuario
                INNER JOIN dbo.MEDICO_ESPECIALIDAD me
                    ON me.id_medico = m.id_medico
                INNER JOIN dbo.ESPECIALIDAD e
                    ON e.id_especialidad = me.id_especialidad
                WHERE m.id_medico = ?
                  AND e.id_especialidad = ?
                  AND u.estado = 'ACTIVO'
                  AND p.estado_laboral = 'ACTIVO'
                  AND e.estado = 'ACTIVA'
                  AND me.fecha_inicio <= CAST(GETDATE() AS DATE)
                  AND (
                        me.fecha_fin IS NULL
                        OR me.fecha_fin >= CAST(GETDATE() AS DATE)
                  )
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, idMedico);
            statement.setInt(2, idEspecialidad);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    // ==========================================
    // 8. VALIDAR SERVICIO ACTIVO
    // ==========================================

    private boolean servicioActivo(
            Connection connection,
            Integer idServicio) throws SQLException {

        String sql = """
                SELECT 1
                FROM dbo.SERVICIO
                WHERE id_servicio = ?
                  AND estado = 'ACTIVO'
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, idServicio);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    // ==========================================
    // 9. BLOQUEO TRANSACCIONAL DE HORARIO
    // ==========================================

    private void bloquearHorario(
            Connection connection,
            String recurso) throws SQLException {

        String sql = """
                DECLARE @resultado INT;

                EXEC @resultado = sys.sp_getapplock
                    @Resource = ?,
                    @LockMode = 'Exclusive',
                    @LockOwner = 'Transaction',
                    @LockTimeout = 10000;

                SELECT @resultado AS resultado;
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, recurso);

            boolean hayResultado = statement.execute();

            while (!hayResultado && statement.getUpdateCount() != -1) {
                hayResultado = statement.getMoreResults();
            }

            if (!hayResultado) {
                throw new SQLException(
                        "No se pudo obtener el bloqueo del horario."
                );
            }

            try (ResultSet result = statement.getResultSet()) {

                if (!result.next() || result.getInt("resultado") < 0) {
                    throw new SQLException(
                            "No se pudo reservar el horario solicitado."
                    );
                }
            }
        }
    }

    // ==========================================
    // 10. COMPROBAR DISPONIBILIDAD
    // ==========================================

    private boolean horarioOcupado(
            Connection connection,
            CitaRequest request) throws SQLException {

        String sql;

        if ("CONSULTA_MEDICA".equals(request.tipoCita())) {

            sql = """
                    SELECT TOP 1 1
                    FROM dbo.CITA
                    WHERE id_medico = ?
                      AND fecha = ?
                      AND hora = CAST(? AS TIME)
                      AND estado IN ('PENDIENTE', 'CONFIRMADA')
                    """;

        } else {

            sql = """
                    SELECT TOP 1 1
                    FROM dbo.CITA
                    WHERE id_servicio = ?
                      AND fecha = ?
                      AND hora = CAST(? AS TIME)
                      AND estado IN ('PENDIENTE', 'CONFIRMADA')
                    """;
        }

        Integer idRecurso =
                "CONSULTA_MEDICA".equals(request.tipoCita())
                        ? request.idMedico()
                        : request.idServicio();

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, idRecurso);
            statement.setDate(2,
                    java.sql.Date.valueOf(request.fecha()));
            statement.setTime(3,
                    java.sql.Time.valueOf(request.hora()));

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    // ==========================================
    // 11. REGISTRAR CITA
    // ==========================================

    public Cita registrarCita(
            CitaRequest request,
            Integer idPaciente,
            Integer idTutor) throws SQLException {

        if (request == null || idPaciente == null) {
            throw new IllegalArgumentException(
                    "La información de la cita no es válida."
            );
        }

        boolean consultaMedica =
                "CONSULTA_MEDICA".equals(request.tipoCita());

        boolean servicio =
                "SERVICIO".equals(request.tipoCita());

        if (!consultaMedica && !servicio) {
            throw new IllegalArgumentException(
                    "El tipo de cita no es válido."
            );
        }

        if (consultaMedica &&
                (request.idMedico() == null
                || request.idEspecialidad() == null
                || request.idServicio() != null)) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un médico y una especialidad."
            );
        }

        if (servicio &&
                (request.idServicio() == null
                || request.idMedico() != null
                || request.idEspecialidad() != null)) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un servicio válido."
            );
        }

        if (request.fecha() == null || request.hora() == null) {
            throw new IllegalArgumentException(
                    "La fecha y la hora son obligatorias."
            );
        }

        if (request.motivo() != null &&
                request.motivo().length() > 250) {

            throw new IllegalArgumentException(
                    "El motivo no puede superar los 250 caracteres."
            );
        }

        try (Connection connection = dataSource.getConnection()) {

            connection.setAutoCommit(false);

            try {

                // Validar relación del tutor dentro de la transacción.
                if (idTutor != null &&
                        !tieneVinculacionAceptada(
                                connection, idTutor, idPaciente)) {

                    throw new IllegalArgumentException(
                            "No tienes autorización para reservar "
                                    + "citas para este paciente."
                    );
                }

                // Validar médico o servicio.
                if (consultaMedica &&
                        !medicoTieneEspecialidadActiva(
                                connection,
                                request.idMedico(),
                                request.idEspecialidad())) {

                    throw new IllegalArgumentException(
                            "El médico no está activo o no pertenece "
                                    + "a la especialidad seleccionada."
                    );
                }

                if (servicio &&
                        !servicioActivo(
                                connection,
                                request.idServicio())) {

                    throw new IllegalArgumentException(
                            "El servicio seleccionado no está activo."
                    );
                }

                // Construir identificador único del recurso y horario.
                String recurso = consultaMedica
                        ? "CITA:MEDICO:"
                        + request.idMedico()
                        : "CITA:SERVICIO:"
                        + request.idServicio();

                recurso += ":"
                        + request.fecha()
                        + ":"
                        + request.hora();

                // Bloqueo exclusivo dentro de la transacción.
                bloquearHorario(connection, recurso);

                // Comprobar que nadie haya reservado el horario.
                if (horarioOcupado(connection, request)) {

                    throw new IllegalStateException(
                            "El horario seleccionado ya está ocupado."
                    );
                }

                // Insertar y recuperar la cita creada.
                String sql = """
                        INSERT INTO dbo.CITA (
                            id_paciente,
                            id_tutor,
                            tipo_cita,
                            id_medico,
                            id_especialidad,
                            id_servicio,
                            fecha,
                            hora,
                            motivo,
                            estado
                        )
                        OUTPUT
                            INSERTED.id_cita,
                            INSERTED.id_paciente,
                            INSERTED.id_tutor,
                            INSERTED.tipo_cita,
                            INSERTED.id_medico,
                            INSERTED.id_especialidad,
                            INSERTED.id_servicio,
                            INSERTED.fecha,
                            INSERTED.hora,
                            INSERTED.motivo,
                            INSERTED.estado,
                            INSERTED.fecha_creacion
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'CONFIRMADA')
                        """;

                Cita cita;

                try (PreparedStatement statement =
                             connection.prepareStatement(sql)) {

                    statement.setInt(1, idPaciente);

                    if (idTutor == null) {
                        statement.setNull(2, Types.INTEGER);
                    } else {
                        statement.setInt(2, idTutor);
                    }

                    statement.setString(3, request.tipoCita());

                    if (request.idMedico() == null) {
                        statement.setNull(4, Types.INTEGER);
                    } else {
                        statement.setInt(4, request.idMedico());
                    }

                    if (request.idEspecialidad() == null) {
                        statement.setNull(5, Types.INTEGER);
                    } else {
                        statement.setInt(5, request.idEspecialidad());
                    }

                    if (request.idServicio() == null) {
                        statement.setNull(6, Types.INTEGER);
                    } else {
                        statement.setInt(6, request.idServicio());
                    }

                    statement.setDate(7,
                            java.sql.Date.valueOf(request.fecha()));

                    statement.setTime(8,
                            java.sql.Time.valueOf(request.hora()));

                    statement.setString(9, request.motivo());

                    try (ResultSet result = statement.executeQuery()) {

                        if (!result.next()) {
                            throw new SQLException(
                                    "No se pudo recuperar la cita registrada."
                            );
                        }

                        Timestamp fechaCreacion =
                                result.getTimestamp("fecha_creacion");

                        cita = new Cita(
                                result.getInt("id_cita"),
                                result.getInt("id_paciente"),
                                (Integer) result.getObject("id_tutor"),
                                result.getString("tipo_cita"),
                                (Integer) result.getObject("id_medico"),
                                (Integer) result.getObject("id_especialidad"),
                                (Integer) result.getObject("id_servicio"),
                                result.getDate("fecha").toLocalDate(),
                                result.getTime("hora").toLocalTime(),
                                result.getString("motivo"),
                                result.getString("estado"),
                                fechaCreacion.toLocalDateTime()
                        );
                    }
                }

                connection.commit();

                return cita;

            } catch (Exception e) {

                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                if (e instanceof SQLException sqlException) {
                    throw sqlException;
                }

                if (e instanceof IllegalArgumentException argumentException) {
                    throw argumentException;
                }

                if (e instanceof IllegalStateException stateException) {
                    throw stateException;
                }

                throw new SQLException(
                        "Error al registrar la cita.",
                        e
                );

            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException ignored) {
                    // La conexión se cerrará al salir del try-with-resources.
                }
            }
        }
    }
}
