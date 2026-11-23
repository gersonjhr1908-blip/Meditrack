package com.meditrack.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record CitaListadoResponse(
        Integer idCita,
        String especialidad,
        String medico,
        LocalDate fecha,
        LocalTime hora,
        String motivo,
        String estado,
        boolean proxima
) {
}
