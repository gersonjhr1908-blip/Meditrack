package com.meditrack.dto;

public record LoginRequest(
        String identificador,
        String contrasena
) {
}