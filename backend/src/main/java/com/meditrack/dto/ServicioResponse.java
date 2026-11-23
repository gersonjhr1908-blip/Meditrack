package com.meditrack.dto;

import com.meditrack.model.Servicio;

public record ServicioResponse(
        Integer idServicio,
        Integer idArea,
        String nombre,
        String descripcion,
        String estado
) {

    public static ServicioResponse desdeModelo(Servicio servicio) {
        return new ServicioResponse(
                servicio.idServicio(),
                servicio.idArea(),
                servicio.nombre(),
                servicio.descripcion(),
                servicio.estado()
        );
    }
}
