package com.meditrack.model;

public record Usuario(
        Integer idUsuario,
        String dni,
        String nombres,
        String apellidos,
        String correo,
        String passwordHash,
        String rol,
        String estado
) {
}
