package com.meditrack.model;

public record Servicio(
        Integer idServicio,
        Integer idArea,
        String nombre,
        String descripcion,
        String estado
) {
}
