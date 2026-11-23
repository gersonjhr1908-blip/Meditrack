package com.meditrack.dto;

public record RegistroPacienteResponse(
        Integer idUsuario,
        Integer idPaciente,
        String codigoPaciente,
        String mensaje
) {
}
