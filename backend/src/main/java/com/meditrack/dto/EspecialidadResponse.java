package com.meditrack.dto;

import com.meditrack.model.Especialidad;

public record EspecialidadResponse(
    Integer idEspecialidad,
    String nombre,
    String descripcion,
    String estado
) {
    public static EspecialidadResponse desdeModelo(
        Especialidad especialidad){

        return new EspecialidadResponse(
            especialidad.idEspecialidad(),
            especialidad.nombre(),
            especialidad.descripcion(),
            especialidad.estado()
        );
        
    }
    
}
