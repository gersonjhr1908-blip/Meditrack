package com.meditrack.service;

import com.meditrack.dto.CitaRequest;
import com.meditrack.dto.CitaListadoResponse;
import com.meditrack.dto.CitaResponse;
import com.meditrack.dto.EspecialidadResponse;
import com.meditrack.dto.MedicoResponse;
import com.meditrack.dto.ServicioResponse;

import com.meditrack.repository.CitaRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@ApplicationScoped
public class CitaService {

    @Inject
    CitaRepository citaRepository;

    public List<CitaListadoResponse> listarMisCitas(String idUsuarioJwt)
            throws SQLException {
        Integer idUsuario = Integer.valueOf(idUsuarioJwt);
        return citaRepository.listarPorUsuario(idUsuario);
    }

    // ==========================================
    // 1. LISTAR ESPECIALIDADES
    // ==========================================

    public List<EspecialidadResponse> listarEspecialidades()
            throws SQLException {

        return citaRepository.listarEspecialidades()
                .stream()
                .map(EspecialidadResponse::desdeModelo)
                .toList();
    }

    // ==========================================
    // 2. LISTAR MEDICOS POR ESPECIALIDAD
    // ==========================================

    public List<MedicoResponse> listarMedicosPorEspecialidad(
            Integer idEspecialidad) throws SQLException {

        if (idEspecialidad == null || idEspecialidad <= 0) {
            throw new IllegalArgumentException(
                    "El identificador de la especialidad no es válido."
            );
        }

        return citaRepository
                .listarMedicosPorEspecialidad(idEspecialidad)
                .stream()
                .map(MedicoResponse::desdeModelo)
                .toList();
    }

    // ==========================================
    // 3. LISTAR SERVICIOS
    // ==========================================

    public List<ServicioResponse> listarServicios()
            throws SQLException {

        return citaRepository.listarServicios()
                .stream()
                .map(ServicioResponse::desdeModelo)
                .toList();
    }

    // ==========================================
    // 4. REGISTRAR CITA
    // ==========================================

    public CitaResponse registrarCita(
            CitaRequest request,
            String idUsuarioJwt,
            Set<String> roles) throws SQLException {

        // Validar autenticación
        if (idUsuarioJwt == null || idUsuarioJwt.isBlank()) {
            throw new SecurityException(
                    "No se pudo identificar al usuario autenticado."
            );
        }

        if (roles == null || roles.isEmpty()) {
            throw new SecurityException(
                    "El usuario no tiene un rol válido."
            );
        }

        // Validar solicitud
        if (request == null) {
            throw new IllegalArgumentException(
                    "Los datos de la cita son obligatorios."
            );
        }

        // Validar fecha y hora
        if (request.fecha() == null || request.hora() == null) {
            throw new IllegalArgumentException(
                    "La fecha y hora de la cita son obligatorias."
            );
        }

        LocalDateTime fechaHoraCita = LocalDateTime.of(
                request.fecha(),
                request.hora()
        );

        if (!fechaHoraCita.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "La fecha y hora de la cita deben ser futuras."
            );
        }

        // Obtener usuario desde JWT
        Integer idUsuario;

        try {
            idUsuario = Integer.valueOf(idUsuarioJwt);
        } catch (NumberFormatException e) {
            throw new SecurityException(
                    "El identificador del usuario autenticado no es válido."
            );
        }

        Integer idPaciente = null;
        Integer idTutor = null;

        // ==========================================
        // IDENTIFICAR TIPO DE USUARIO
        // ==========================================

        if (roles.contains("PACIENTE")) {

            // El paciente se obtiene desde el JWT.
            // Se ignora el idPaciente enviado por Android.

            idPaciente = citaRepository
                    .obtenerIdPacientePorUsuario(idUsuario);

            if (idPaciente == null) {
                throw new IllegalArgumentException(
                        "No se encontró el perfil del paciente."
                );
            }

        } else if (roles.contains("TUTOR")) {

            // Obtener tutor desde el JWT.

            idTutor = citaRepository
                    .obtenerIdTutorPorUsuario(idUsuario);

            if (idTutor == null) {
                throw new IllegalArgumentException(
                        "No se encontró el perfil del tutor."
                );
            }

            // El tutor debe seleccionar al paciente.

            idPaciente = request.idPaciente();

            if (idPaciente == null || idPaciente <= 0) {
                throw new IllegalArgumentException(
                        "Debe seleccionar un paciente para reservar la cita."
                );
            }

        } else {

            throw new SecurityException(
                    "Este usuario no tiene permiso para registrar citas."
            );
        }

        // ==========================================
        // REGISTRAR CITA
        // ==========================================

        var cita = citaRepository.registrarCita(
                request,
                idPaciente,
                idTutor
        );

        return CitaResponse.desdeModelo(cita);
    }
}
