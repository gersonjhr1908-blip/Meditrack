package com.meditrack.model;

public record Medico(
    Integer idMedico,
    String dni,
    String nombres,
    String apellidos,
    String codigoProfesional,
    Integer idEspecialidad,
    String especialidad,
    boolean especialidadPrincipal

) {
    
}
