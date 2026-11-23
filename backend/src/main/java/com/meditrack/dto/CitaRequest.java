package com.meditrack.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record CitaRequest(
        Integer idPaciente,
        String tipoCita,
        Integer idMedico,
        Integer idEspecialidad,
        Integer idServicio,
        LocalDate fecha,
        LocalTime hora,
        String motivo
) {
}
