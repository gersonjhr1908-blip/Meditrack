package com.meditrack.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record Cita(
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
    
}
