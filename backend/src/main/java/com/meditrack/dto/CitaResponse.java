package com.meditrack.dto;

import com.meditrack.model.Cita;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

public record CitaResponse(
        Integer idCita,
        Integer idPaciente,
        Integer idTutor,
        String tipoCita,
        Integer idMedico,
        Integer idEspecialidad,
        Integer idServicio,
        LocalDate fecha,
        LocalTime hora,
        String motivo,
        String estado,
        LocalDateTime fechaCreacion
) {

    public static CitaResponse desdeModelo(Cita cita) {
        return new CitaResponse(
                cita.idCita(),
                cita.idPaciente(),
                cita.idTutor(),
                cita.tipoCita(),
                cita.idMedico(),
                cita.idEspecialidad(),
                cita.idServicio(),
                cita.fecha(),
                cita.hora(),
                cita.motivo(),
                cita.estado(),
                cita.fechaCreacion()
        );
    }
}
